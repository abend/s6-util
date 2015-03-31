/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import com.samsix.util.reflect.SimpleMethodInvoker;
import com.samsix.util.string.StringUtilities;



public class OsUtils
{
    private static Logger logger = null;

    private static Properties       _envVars;
    private static boolean          _logResourceProblems     = true;

    public static String[] IMAGE_EXTENSIONS = new String[] { "bmp", "gif", "jpg", "jpeg", "png", "tif", "tiff" };


    private OsUtils()
    {
        //    prevent instantiation
    }
    
    
    public static void deleteFolder(final File folder) {
        if (! folder.exists() ) {
            return;
        }
        
        File[] files = folder.listFiles();
        if(files != null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    

    public static void exit( final Throwable    ex )
    {
        String msg = ExceptionUtils.getTotalStackTrace( ex );

        Logger.getRootLogger().error( msg );

        System.exit( -1 );
    }


    public static String getEnvVar( final String    key )
    {
        try
        {
            if ( new JvmVersionUtils().isVersion6() )
            {
                return simpleGetEnvVar( key );
            }

            return getEnvVars().getProperty( key );
        }
        catch ( Throwable    ex )
        {
            //
            //    Don't use logger to report error
            //    because this code is used before
            //    logging is set and will lead to
            //    errors involving top.log not available
            //    yet when the log4j.properties file is
            //    parsed.
            //
            ex.printStackTrace();
            return null;
        }
    }


    private static String simpleGetEnvVar( final String  key ) throws UtilException
    {
        return (String) SimpleMethodInvoker.invokeStaticMethod( System.class, "getenv", key );
    }


    private static synchronized Properties getEnvVars()
        throws
            UtilException
    {
        if ( _envVars != null )
        {
            return _envVars;
        }

        Process    process = null;
        _envVars = new Properties();

        try
        {
            if ( OsUtils.isWindows() )
            {
                if ( OsUtils.isWindows9x() )
                {
                    process = Runtime.getRuntime().exec( "command.com /c set" );
                }
                else
                {
                    process = Runtime.getRuntime().exec( "cmd.exe /c set" );
                }
            }
            else
            {
                //
                //     Our last hope, we assume Unix
                //
                process = Runtime.getRuntime().exec( "env" );
            }

            BufferedReader bufferedReader = new BufferedReader
                ( new InputStreamReader( process.getInputStream() ) );

            int       idx;
            String    line;
            while ( ( line = bufferedReader.readLine() ) != null )
            {
                idx = line.indexOf( '=' );

                if ( idx > 0 )
                {
                    _envVars.setProperty( line.substring( 0, idx ),
                                          line.substring( idx + 1 ) );
                }
            }

            bufferedReader.close();

            return _envVars;
        }
        catch ( Throwable    ex )
        {
            throw new UtilException( "Cannot retrieve environment variables.", ex );
        }
        finally
        {
            if ( process != null )
            {
                process.destroy();
            }
        }
    }


    public static String getTopDir()
    {
        String    topDir = getEnvVar( "S6_TOP" );

        if ( !StringUtils.isBlank( topDir ) )
        {
            return topDir;
        }

        return getEnvVar( "S6_WORKSPACE" );
    }


    public static void execute( final List<String>    command,
                                final PrintStream     outputStream )
        throws
            IOException,
            InterruptedException
    {
        execute( command.toArray( new String[command.size()] ), outputStream );
    }


    public static void execute( final String[]    command )
        throws
            IOException,
            InterruptedException
    {
        execute( command, null, true );
    }


    public static void execute( final String[]    command,
                                final boolean     waitToFinish )
        throws
            IOException,
            InterruptedException
    {
        execute( command, null, waitToFinish );
    }


    public static void execute( final String[]       command,
                                final PrintStream    outputStream )
        throws
            IOException,
            InterruptedException
    {
        execute( command, outputStream, true );
    }


    public static void execute( final String[]       command,
                                final PrintStream    outputStream,
                                final boolean        waitToFinish )
        throws
            IOException,
            InterruptedException
    {
        //
        //    Now shell the command out to the command
        //    specified in the property file.
        //
        String    commandString;
        commandString = StringUtilities.arrayToString( command, " " );

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "About to run the command ["
                              + commandString
                              + "]" );
        }

        if ( outputStream != null )
        {
            //
            //    Echo the command so that we know what was executed to produce
            //    the output.
            //
            outputStream.println( "Executing [" + commandString + "]..." );
        }

        Process    process;
        process = Runtime.getRuntime().exec( command );

        if ( ! waitToFinish )
        {
            return;
        }

        //
        //    Read the stdout and stderr streams.
        //
        String    output;
        output = StringUtilities.inputStreamToString( process.getInputStream() );

        String    errMsg;
        errMsg = StringUtilities.inputStreamToString( process.getErrorStream() );
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Output:  " + output );
            getLogger().info( "Error:  " + errMsg );
        }

        if ( outputStream != null )
        {
            //
            //    Echo the command so that we know what was executed to produce
            //    the output.
            //
            outputStream.println( output );
            outputStream.println( errMsg );
            outputStream.flush();
        }


        //
        //    Wait for the process to complete.
        //    Since this *should* just be spooling
        //    call the process should finish immediately.
        //
        //    NOTE: This must happen AFTER the output streams (both error and
        //    out) are read completely.
        //
        process.waitFor();

        //
        //    This is to catch any errors that aren't
        //    thrown immediately by the exec command.
        //    If the exit status of our plot command
        //    is not zero then an error must have occurred.
        //    Let's throw an exception with the contents of the
        //    stderr stream.
        //
        if ( process.exitValue() != 0 )
        {
            //
            //    In case the program doesn't output to stderr but
            //    instead to stdout we will add this check.
            //    Hopefully, the error will at least be displayed
            //    in stdout.
            //
            String    msg;
            if ( StringUtils.isBlank( errMsg ) )
            {
                msg = output;
            }
            else
            {
                msg = errMsg;
            }

            throw new IOException( "Can't issue command ["
                                   + commandString
                                   + "] due to:\n"
                                   + msg );
        }

        //
        //    Need to add a check on outMsg to see if it contains
        //    the word "Error:" because the
        //    lpr command on Windows (for instance) returns an error code of zero
        //    sometimes in the case of an error such as "Error:  print
        //    server unreachable or specified printer does not exist."
        //    and in addition the error message goes to stdout.
        //    If these calls are being made from the python script
        //    and not directly here the stdout is not getting used and
        //    the messages are disappearing into the ether.  So this hack is useless
        //    in those cases.  But if called directly from java it should work.
        //
        if ( output.toUpperCase().indexOf( "ERROR:" ) >= 0 )
        {
            throw new IOException( "Can't issue command ["
                                   + commandString
                                   + "] due to:\n"
                                   + output );
        }
    }


    /**
     *     This method takes a string and converts it into a filename by replacing
     *     any spaces with underscores and then also truncating it if it is over
     *     a certain length.  At the time of the writing of this comment that was 48
     *     characters which was chosen by pulling a number out of my brain.
     */
    public static String fileNameFromString( final String    text )
    {
        String    value = text.replace( ' ', '_' );

        if ( value.length() < 48 )
        {
            return value;
        }

        return value.substring( 0, 47 );
    }


    /**
     *    Executes the given command, redirecting stdout to the given file
     *    and stderr to actual stderr
     *
     *    @param command
     *    @param file
     */
    public static int executeToFile( final String[]         command,
                                     final File             file )
        throws
            IOException,
            InterruptedException
    {
        return executeToFile( command, file, System.err );
    }


    public static void writeTextToFile( final String     filename,
                                        final String     data )
        throws
            IOException
    {
        writeTextToFile( new File( filename ), data );
    }


    public static void writeTextToFile( final File      file,
                                        final String    data )
        throws
            IOException
    {
        file.createNewFile();

        OutputStreamWriter    out = new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );

        out.append( data );
        out.close();
    }


    /**
     *    Executes the given command, redirecting stdout to the given file
     *    and stderr to the given stream
     *
     *    @param command
     *    @param file
     *    @param stderr
     */
    public static int executeToFile( final String[]         command,
                                     final File             file,
                                     final OutputStream     stderr )
        throws
            IOException,
            InterruptedException
    {
        return executeToStreams( command, new FileOutputStream( file ), stderr );
    }


    public static int executeToStreams( final String[]         command,
                                        final OutputStream     stdout,
                                        final OutputStream     stderr )
        throws
            IOException,
            InterruptedException
    {
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "About to run the command ["
                              + StringUtilities.arrayToString( command, " " )
                              + "]" );
        }

        Process    process;
        process = Runtime.getRuntime().exec( command );

        StreamRedirector    out = new StreamRedirector( process.getInputStream(),
                                                        stdout );
        StreamRedirector    err = new StreamRedirector( process.getErrorStream(),
                                                        stderr );

        out.start();
        err.start();

        process.waitFor();

        out.join();
        err.join();

        if( out.hasException() )
        {
            throw new IOException( "Failed to write out", out.getException() );
        }

        if( err.hasException() )
        {
            throw new IOException( "Failed to write err", err.getException() );
        }

        return process.exitValue();
    }


    public static BufferedReader openFile( final File    file )
        throws
            FileNotFoundException
    {
        return new BufferedReader( new FileReader( file ) );
    }


    /**
     *    Thread that consumes an InputStream and writes to an OutputStream
     *    until the InputStream reaches EOF
     */
    public static class StreamRedirector
        extends
            Thread
    {
        private final static int BUFFER_SIZE = 8192;

        private final InputStream     _input;
        private final OutputStream    _output;

        private Throwable             _exception = null;


        public StreamRedirector( final InputStream     input,
                                 final OutputStream    output )
        {
            super( "StreamRedirector [" + input + "]" );

            _input = new BufferedInputStream( input );
            _output = output;
        }


        @Override
        public void run()
        {
            final byte buffer[] = new byte[ BUFFER_SIZE ];

            try
            {
                int    length;

                while( ( length = _input.read( buffer, 0, BUFFER_SIZE ) ) > 0 )
                {
                    _output.write( buffer, 0, length );
                }
            }
            catch( Throwable    ex )
            {
                _exception = ex;
            }
            finally
            {
                try
                {
                    _input.close();
                }
                catch( Throwable    ioe )
                {
                    Logger.getLogger( getClass() )
                          .error( "Failed closing input", ioe );
                }

                try
                {
                    _output.close();
                }
                catch( Throwable    ioe )
                {
                    Logger.getLogger( getClass() )
                          .error( "Failed closing output", ioe );
                }
            }
        }


        public boolean hasException()
        {
            return _exception != null;
        }


        public Throwable getException()
        {
            return _exception;
        }
    }



    public static boolean isWindows()
    {
        return ( System.getProperty( "os.name" ).startsWith( "Windows" ) );
    }



    public static boolean isWindows9x()
    {
        return ( System.getProperty( "os.name" ).startsWith( "Windows 9" ) );
    }



    private static Logger getLogger()
    {
        if ( logger == null )
        {
            logger = Logger.getLogger( OsUtils.class );
        }

        return logger;
    }


    public static String readFileToString( final String filePath )
        throws
            IOException
    {
        return readFileToString( new File( filePath ) );
    }


    public static String readFileToString( final File file )
        throws
            IOException
    {
        return StringUtilities.inputStreamToString( new FileInputStream( file ) );
    }
    
    
    public static List<List<String>> readCSVFile( final String filePath ) throws IOException
    {
        BufferedReader reader;
        reader = new BufferedReader( new InputStreamReader( new FileInputStream( new File( filePath ) ) ) );
        
        List<List<String>> rows = new ArrayList<List<String>>();
        
        String line;
        while ( ( line = reader.readLine() ) != null )
        {
            rows.add(StringUtilities.split( line, ",", true ));
        }
        reader.close();

        return rows;

    }
    

    public static File getFile( final String    filePath )
    {
        String    path = filePath;
        if ( path.startsWith( "file://" ) )
        {
            path = path.substring( 7 );
        }
        else if ( path.startsWith( "file:" ) )
        {
            path = path.substring( 5 );
        }

        return new File( path );
    }



    public static void copyFile( final String    in,
                                 final String    out )
        throws
            IOException
    {
        FileInputStream    inStream = new FileInputStream( in );
        FileChannel    inChannel = inStream.getChannel();

        FileOutputStream    outStream = new FileOutputStream( out );
        FileChannel    outChannel = outStream.getChannel();

        try
        {
            inChannel.transferTo( 0, inChannel.size(), outChannel );
        }
        catch ( IOException    ex )
        {
            throw ex;
        }
        finally
        {
            if ( inStream != null )
            {
                inStream.close();
            }

            if ( outStream != null )
            {
                outStream.close();
            }
        }
    }


    public static void copy( final Reader reader,
                             final Writer writer )
        throws
            IOException
    {
        final char[] buffer = new char[4096];
        int len;

        while ( ( len = reader.read( buffer ) ) >= 0 )
        {
            writer.write( buffer, 0, len );
        }
    }


    public static void copyStreams( final InputStream     in,
                                    final OutputStream    out )
        throws
            IOException
    {
        final byte[] buffer = new byte[8192];
        int len;

        while ( ( len = in.read( buffer ) ) >= 0 )
        {
            out.write( buffer, 0, len );
        }
    }


    public static void copyAndCloseStreams( final InputStream     in,
                                            final OutputStream    out )
        throws
            IOException
    {
        try
        {
            copyStreams( in, out );
        }
        finally
        {
            try
            {
                in.close();
            }
            catch( Throwable    ex )
            {
                logger.error( "Failed to close inputstream", ex );
            }

            try
            {
                out.close();
            }
            catch( Throwable    ex )
            {
                logger.error( "Failed to close outputstream", ex );
            }
        }
    }


    public static boolean fileExists( final String    filePath )
    {
        return new File( filePath ).exists();
    }


    public static void delete( final File f )
    {
        if (!f.isDirectory())
        {
            //System.out.println("deleting file: " + f );
            f.delete();
            return;
        }

        String[] contents = f.list();

        if (contents != null)
        {
            for (int i = 0; i < contents.length; i++)
            {
                File thisFile = new File(f, contents[i]);
                delete(thisFile);
            }
        }

        //System.out.println("deleteing dir: " + f );
        f.delete();
    }


    public static boolean doesResourceExist( final String    resource )
    {
        URL    url;

        try
        {
            url = new URL( resource );
        }
        catch( MalformedURLException    ex )
        {
            getLogger().warn( "Invalid resource URL [" + resource + "], trying file", ex );

            File    file = new File( resource );

            try
            {
                url = file.toURI().toURL();
            }
            catch( MalformedURLException    ex2 )
            {
                getLogger().error( "Invalid resource URL [" + resource + "]", ex2 );
                return false;
            }
        }

        return doesResourceExist( url );
    }

    /**
     *      @return true if able to open a connection to the url
     */
    public static boolean doesResourceExist( final URL    url )
    {
        try
        {
            checkResourceAvailable( url );

            return true;
        }
        catch ( FileNotFoundException    fnfe )
        {
            //    Don't bother with file not found.
        }
        catch ( ConnectException    ce )
        {
            if ( _logResourceProblems )
            {
                getLogger().error( "Unable to access resource [" + url + "]: "
                                   + ce.getMessage() );
            }
        }
        catch ( Throwable    ex )
        {
            getLogger().error( "Error checking if resource [" + url + "] exists", ex );
        }

        return false;
    }


    /**
     *      @throws IOException if the resource isn't available.
     */
    public static void checkResourceAvailable( final URL    url )
        throws
            IOException
    {
        URLConnection    connection = url.openConnection();

        if( connection instanceof HttpURLConnection )
        {
            HttpURLConnection    httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod( "HEAD" );
        }

        connection.setDoOutput( false );

        //
        //    For file:/// urls, this throws a file not found exception
        //    if the file does not exist.
        //
        connection.connect();

        try
        {
            //
            //    For http, this throws a file not found exception on a
            //    404 result code.
            //
            connection.getInputStream();
        }
        finally
        {
            if( connection instanceof HttpURLConnection )
            {
                try
                {
                    ( (HttpURLConnection) connection ).disconnect();
                }
                catch( Throwable    ex )
                {
                    //    ignore
                }
            }
        }
    }


    public static void setLogResourceProblems( final boolean logResourceProblems )
    {
        _logResourceProblems = logResourceProblems;
    }


    public static String getDefaultCust()
    {
        return getEnvVar( "S6_CUST" );
    }


    public static String getDefaultPropFile( final String    custCode )
    {
        return "com.samsix.cust." + custCode + ".config.Boot";
    }


    /**
     *     Tries to make a connection to the host's port 7 (Echo)
     *     @returns time in milliseconds for response.  -1 if no response.
     */
    public static long timedPing( final String    server )
        throws
            UnknownHostException,
            IOException
    {
        StopWatch    stopwatch = new StopWatch();
        stopwatch.start();

        boolean    reachable;
        reachable = ping( server );

        stopwatch.stop();

        if ( reachable )
        {
            return stopwatch.getTime();
        }

        return -1;
    }


    /**
     *     Tries to make a connection to the host's port 7 (Echo)
     *     @returns true if server is reachable
     */
    public static boolean ping( final String    server )
        throws
            UnknownHostException,
            IOException
    {
        //
        //    The timeout here is not the amount of time that it will
        //    wait before determining if its unreachable but rather the
        //    maximum amount of time.  If determined to be unreachable
        //    before the timeout it will come back early and tell you that
        //    it's not reachable.
        //
        return InetAddress.getByName( server ).isReachable( 3000 );
    }


    public static String getFileExtension( final String    filePath )
    {
        int    index = filePath.lastIndexOf( "." );

        if ( index > 0 )
        {
            return filePath.substring( index + 1 );
        }

        return "";
    }


    /**
     * 
     * @return Root of file. The full path of the file minus it's extension.
     */
    public static String getFileRoot( final String    filePath )
    {
        int index = filePath.lastIndexOf( "." );

        if ( index >= 0 )
        {
            return filePath.substring( 0, index );
        }

        return filePath;
    }


    /**
     *     This is the name of the file minus it's extension and minus it's
     *     full path.
     */
    public static String getFileBaseName( final String    filePath )
    {
        String    root = getFileRoot( filePath );

        int index = root.lastIndexOf( File.separatorChar );

        if ( index >= 0 )
        {
            return root.substring( index + 1 );
        }

        return root;
    }


    /**
     *     This is the full name of the file minus it's path.
     */
    public static String getFileDirName( final String    filePath )
    {
        int index = filePath.lastIndexOf( File.separatorChar );

        //
        // In case it's a directory and the last character is a file separator
        // then strip it.
        //
        String path;
        if ( index >= 0 && index == filePath.length() - 1 ) {
            path = filePath.substring( 0, filePath.length() - 2 );
            index = path.lastIndexOf( File.separatorChar );
        } else {
            path = filePath;
        }

        if ( index >= 0 )
        {
            return path.substring( index + 1 );
        }

        return filePath;
    }


    /**
     * Obtained from http://stackoverflow.com/questions/617414/create-a-temporary-directory-in-java
     * Replace with Files.createTempDirectory() when moving to Java 7
     *
     * @return
     * @throws IOException
     */

    public static File createTempDirectory( final String    prefix )
        throws
            IOException
    {
        File tempDir;

        tempDir = File.createTempFile( prefix, null );

        if( ! ( tempDir.delete() ) )
        {
            throw new IOException( "Could not delete temp file: " + tempDir.getAbsolutePath() );
        }

        if( ! ( tempDir.mkdir() ) )
        {
            throw new IOException( "Could not create temp directory: " + tempDir.getAbsolutePath() );
        }

        return tempDir;
    }


    /**
     *     Check to make sure the user has not put .. anywhere in the path to allow overwriting
     *     of system files.
     *
     * @param file
     * @param base
     */
    public static void makeSecurityCheck( final File    file,
                                          final File    base )
    {
        if( ! file.getAbsolutePath().startsWith( base.getAbsolutePath() ) )
        {
            throw new IllegalArgumentException( "Illegal file path [" + file + "]" );
        }
    }
}

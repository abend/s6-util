/*
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.log;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.TTCCLayout;

import com.samsix.util.OsUtils;
import com.samsix.util.S6Key;
import com.samsix.util.string.AttrString;
import com.samsix.util.string.StringUtilities;


/**
 *    A runtime control for log4j logging.
 */
public class LoggerControl
{
    public static final String SQL_LOG    = "SqlLog";


    //
    //    DANGER: Can't use static logger here!
    //

    private static AttrString    _systemAttrString;
    private static boolean       _loggingBaseSet = false;
    private static Set<String>   _forcedLoggers = new HashSet<String>();

    private List<String>        _debugClasses   = new ArrayList<String>();
    private List<String>        _infoClasses    = new ArrayList<String>();
    private List<String>        _errorClasses   = new ArrayList<String>();
    private String              _location;


    public void afterPropertiesSet()
    {
        setLoggerLevel( _debugClasses,  Level.DEBUG );
        setLoggerLevel( _infoClasses,   Level.INFO );
        setLoggerLevel( _errorClasses,  Level.ERROR);
    }


    private void setLoggerLevel( final List<String>     loggerNameList,
                                 final Level            level )
    {
        for ( String loggerName : loggerNameList )
        {
            setLoggerLevel( loggerName, level );
        }
    }


    public void setLoggerLevel( final String   loggerName,
                                final Level    level )
    {
        Logger.getLogger( loggerName ).setLevel( level );
    }


    public void setLoggerLevel( final Class<?>    loggerClass,
                                final Level       level )
    {
        Logger.getLogger( loggerClass ).setLevel( level );
    }


    /**
     *      A variation of setLoggerLevel - this one will substitute
     *      "." for "\" or "/" in the logger name, and any case version of
     *      "sqlLog" or "sql" in the logger name will map to SqlLog.
     *      <br><br>Note that we don't want to check for the existence of
     *      a class matching the loggerName - loggers can be for other kinds of
     *      things besides classes.
     *
     *      @param name of logger
     *      @param level to set - if loggingLevel is unrecognized,
     *                  the level will be DEBUG
     */
    public static String setLoggingLevel( final String    name,
                                          final Level     level )
    {
        String    loggerName = name;

        //
        //    Substitute "." for "\" or "/" in the logger name if someone
        //    cut-and-pasted a directory.
        //
        //    For developers we could probably prune anything that was:
        //
        //    somepath/com/samsix/rest/of/path, to just com.samsix.rest.of.path
        //
        loggerName = StringUtils.replaceChars( loggerName, '\\', '.' );
        loggerName = StringUtils.replaceChars( loggerName, '/', '.' );

        //
        //    PITA doing camelCase
        //
        if ( "sqllog".equals( loggerName.toLowerCase() )
             || "sql".equals( loggerName.toLowerCase() ) )
        {
            loggerName = SQL_LOG;
        }

        Logger    theLogger  = Logger.getLogger( loggerName );

        System.out.println( "Changing logger for [" + theLogger.getName()
                            + "] from "
                            + theLogger.getLevel()
                            + " to "
                            + level );

        theLogger.setLevel( level );

        return theLogger.getName();
    }


    public void setDebug( final String  loggerName )
    {
        setLoggerLevel( loggerName, Level.DEBUG );
    }


    public void setInfo( final String  loggerName )
    {
        setLoggerLevel( loggerName, Level.INFO );
    }


    public void setError( final String  loggerName )
    {
        setLoggerLevel( loggerName, Level.ERROR );
    }


    //      Logger doesn't return typed enumerations
    @SuppressWarnings( "unchecked" )
    public void turnOffAllLogging()
    {
        Enumeration<Appender> enumeration = Logger.getRootLogger().getAllAppenders();

        while ( enumeration.hasMoreElements() )
        {
             enumeration.nextElement().close();
        }
    }


    //      Logger doesn't return typed enumerations
    @SuppressWarnings( "unchecked" )
    public static void listLoggers()
    {
        Enumeration<Logger>     loggers     = LogManager.getCurrentLoggers();

        while ( loggers.hasMoreElements() )
        {
            Logger logger = loggers.nextElement();

            if ( logger.getLevel() != null )
            {
                System.out.println( logger.getName() + ": " + logger.getLevel() );
            }
        }
    }


    public void setErrorClasses( final List<String> errorClasses )
    {
        _errorClasses = errorClasses;
    }


    public void setDebugClasses( final List<String> debugClasses )
    {
        _debugClasses = debugClasses;
    }


    public void setInfoClasses( final List<String> infoClasses )
    {
        _infoClasses = infoClasses;
    }


    public void setLocation( final String location )
    {
        _location = location;
    }


    private static Collection<Logger> getLoggers()
    {
        Collection<Logger>    loggers = new ArrayList<Logger>( 2 + _forcedLoggers.size() );
        loggers.add( LogManager.getRootLogger() );
        loggers.add( LogManager.getLogger( SQL_LOG ) );

        for( String    loggerName : _forcedLoggers )
        {
            loggers.add( Logger.getLogger( loggerName ) );
        }

        return loggers;
    }


    public static Collection<FileAppender> getFileAppenders()
    {
        Collection<FileAppender>    appenders = new ArrayList<FileAppender>();
        for ( Logger    logger : getLoggers() )
        {
            appenders.addAll( getFileAppenders( logger ) );
        }

        return appenders;
    }


    @SuppressWarnings( "unchecked" )
    private static Collection<FileAppender> getFileAppenders( final Logger    logger )
    {
        Collection<FileAppender>    appenders = new ArrayList<FileAppender>();
        Enumeration<Appender>       enumeration = logger.getAllAppenders();
        Appender                    appender;

        while ( enumeration.hasMoreElements() )
        {
            appender = enumeration.nextElement();

            if ( ! ( appender instanceof FileAppender ) )
            {
                continue;
            }

            appenders.add( (FileAppender) appender );
        }

        return appenders;
    }


    /**
     *    Redirect the loggers to a different file name.
     *    useful for servers.  Only changes FileAppenders.
     *    <p>
     * @param prefix - the <code>String</code> prefix of the file name
     *               the default logfile for nrg has this as the user's
     *               name, but since this is likely a server process
     *               it makes sense to make this the server's name
     *               instead of the users's name, especially if a user
     *               runs it who might also run NRG.
     */
    public static void redirectLoggers( final String    prefix )
    {
        if ( prefix == null )
        {
            return;
        }

        for ( Logger    logger : getLoggers() )
        {
            //
            //    Skip over any loggers that have been force-set
            //
            if( _forcedLoggers.contains( logger.getName() ) )
            {
                continue;
            }

            redirectLogger( logger, prefix );
        }
    }


    /**
     *    Forces a redirect of a particular logger to a separate file
     *
     *    @param logger
     *    @param prefix
     */
    public static void forceRedirectLogger( final Logger    logger,
                                            final String    prefix )
    {
        //
        //    Parent should be the root logger
        //
        Logger    parent = (Logger) logger.getParent();

        Layout    layout = null;
        boolean   append = false;

        if( parent != null )
        {
            Collection<FileAppender>    appenders = getFileAppenders( parent );

            if( ! appenders.isEmpty() )
            {
                FileAppender    appender = appenders.iterator().next();

                layout = appender.getLayout();
                append = appender.getAppend();
            }
        }

        if( layout == null )
        {
            //
            //    This shouldn't happen.
            //
            layout = new TTCCLayout();
        }

        setLoggingBase();
        String    logfile = System.getProperty( S6Key.logDir )
                            + File.separator
                            + prefix
                            + ".log";

        FileAppender    fileAppender;

        try
        {
            fileAppender = new FileAppender( layout, logfile, append );
        }
        catch ( Throwable    ex )
        {
            Logger.getLogger( LoggerControl.class )
                  .error( "can't create new file appender for forced log", ex );
            return;
        }

        logger.addAppender( fileAppender );
        logger.setAdditivity( false );
        _forcedLoggers.add( logger.getName() );
    }


    //      Until Logger.getAllAppenders() is genericized
    private static void redirectLogger( final Logger    logger,
                                        final String    prefix )
    {
        setLoggingBase();

        FileAppender          oldAppender;

        for ( FileAppender    fileAppender : getFileAppenders( logger ) )
        {
            logger.removeAppender( fileAppender );

            String    logFileName = fileAppender.getFile();
            if ( logFileName == null )
            {
                continue;
            }

            //
            //    Get the actual file name from the full path.
            //    So, if the name is "c:\logs\errors.log", we want "errors.log"
            //
            File      logFile = new File( logFileName );
            String    baseFileName = logFile.getName();

            //
            //    If it's empty or null, something is wrong with this path.
            //    Probably best to leave it alone.
            //
            if ( StringUtilities.isNullOrEmpty( baseFileName ) )
            {
                continue;
            }

            //
            //    Only add our prefix if the file isn't already prefixed that way.
            //
            String    addPrefix = "";
            if( ! baseFileName.startsWith( prefix + "." ) )
            {
                addPrefix = prefix + ".";
            }

            //
            //    Need to use the new top.log because the top.log that was set
            //    the log4j.property file was parsed is possibly wrong.
            //
            String    logfile = System.getProperty( S6Key.logDir )
                                + File.separator
                                + addPrefix
                                + baseFileName;

            oldAppender = fileAppender;

            try
            {
                fileAppender = new FileAppender( fileAppender.getLayout(),
                                                 logfile,
                                                 fileAppender.getAppend() );
            }
            catch ( Throwable    ex )
            {
                Logger.getLogger( LoggerControl.class )
                      .error( "can't create new file appender", ex );

                fileAppender = oldAppender;
            }

            logger.addAppender( fileAppender );
        }
    }


    public static AttrString getSystemAttrString()
    {
        if( _systemAttrString != null )
        {
            return _systemAttrString;
        }

        synchronized( LoggerControl.class )
        {
            AttrString    systemAttrString = new AttrString();

            for( Map.Entry<Object, Object>    prop : System.getProperties().entrySet() )
            {
                //
                //    For some arcane reason, Properties is a String,String map
                //    held in Object,Object. This logic follows the logic within
                //    the Java properties class itself.
                //
                if( ! ( prop.getKey() instanceof String
                        && prop.getValue() instanceof String ) )
                {
                    continue;
                }

                String    key = (String) prop.getKey();
                String    value = (String) prop.getValue();

                //
                //    Keep top.log out of the system properties map,
                //    otherwise we could have infinite recursion
                //
                if( S6Key.logDir.equals( key ) )
                {
                    continue;
                }

                systemAttrString.add( key, value );
            }

            //
            //    Add some of our own defaults
            //
            File    defaultDirectory;
            defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();
            if( defaultDirectory != null )
            {
                systemAttrString.add( "user.defaultDirectory",
                                      defaultDirectory.getAbsolutePath() );
            }
            else
            {
                systemAttrString.add( "user.defaultDirectory",
                                      System.getProperty( "user.home" ) );
            }

            _systemAttrString = systemAttrString;
        }

        return _systemAttrString;
    }


    /**
     *    This needs to happen BEFORE any loggers are initialized!
     */
    public static void setLoggingBase()
    {
        if( _loggingBaseSet )
        {
            return;
        }

        _loggingBaseSet = true;

        //
        //    First we want to default to a log directory if it doesn't
        //    exist in the properties.  This is so that the log4j.properties
        //    file can properly find a directory to write to and won't
        //    complain.
        //
        String    logDir = System.getProperty( S6Key.logDir );

        if ( logDir == null )
        {
            logDir = OsUtils.getEnvVar( "S6_LOGDIR" );

            if ( logDir == null )
            {
                //
                //    This is a shortcut for the user's default directory location.
                //    On windows, this is the documents directory.
                //    For further information, search for this in LoggerControl.java.
                //
                //    This exists because we can't specify properties inside an unsigned
                //    jnlp file without java flagging it as 'insecure' and showing the user
                //    a big warning, so this becomes the default. It shouldn't affect much as
                //    all platforms should have top.log set.
                //
                logDir = "${user.defaultDirectory}";
            }

            System.setProperty( S6Key.logDir, logDir );
        }

        //
        //    Parse out any attributes that might be specified (such as the user.defaultdirectory above)
        //
        logDir = getSystemAttrString().computeString( logDir, false );
        System.setProperty( S6Key.logDir, logDir );
    }


    /**
     * To be used only in development, not in production code. Essentially, turns on the logger
     * to dump out to the console. Use like this:
     * <pre>
     * public class InDevelopmentThing {
     *     private final static Logger logger = Logger.getLogger( InDevelopmentThing.class );
     *     static { LoggerControl.setDebugLogToConsole( logger ); }
     *     ...
     * }
     * </pre>
     *
     * This way, you can see all the debug messages in your eclipse development console.
     *
     * @param logger
     */
    public static void setDebugLogToConsole( final Logger logger )
    {
        logger.setLevel( Level.ALL );
        logger.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this )
                .append( "debugList",   _debugClasses )
                .append( "infoList",    _infoClasses )
                .append( "errorList",   _errorClasses )
                .append( "location",    _location )
                .toString();
    }
}

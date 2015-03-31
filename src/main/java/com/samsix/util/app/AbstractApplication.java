/*
 ***************************************************************************
 *
 * Copyright (c) 2001-2011 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.app;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.samsix.util.BasicVersionInfo;
import com.samsix.util.OsUtils;
import com.samsix.util.UtilException;
import com.samsix.util.VersionInfo;
import com.samsix.util.log.LoggerControl;
import com.samsix.util.string.S6ToStringStyle;
import com.samsix.util.string.StringUtilities;


/**
 *    Abstract class from which to subclass all applications.
 *    <p>
 *    Subclasses should contain static method main that calls launch.
 *    <p>
 *    Example usage:
 *    <p>
 *    <pre>
 *    public static void main( String    args[] )
 *    {
 *        launch( new <Subclass>(), args );
 *    }
 *    </pre>
 */
public abstract class AbstractApplication
{
    protected final static Logger logger;

    static {
        //
        //    This is the first invocation of a logger. Set the base logging
        //    preferences up so we get the logs in the right place.
        //
        LoggerControl.setLoggingBase();
        logger = Logger.getLogger( AbstractApplication.class );
    }
    
    private static String LOCALHOST = "localhost";

    private final Options    _options       = new Options();
    protected CommandLine    _commandLine;

    protected String         _configFile;
    protected String         _custCode;

    protected boolean        _debug;
    protected boolean        _dryrun;



    protected String getDefaultDbServer( final String    serverName )
    {
        if ( ! serverName.equals( LOCALHOST ) )
        {
            return serverName;
        }

        //
        //    Otherwise let's check the pghost env variable.
        //
        String dbServer = OsUtils.getEnvVar( "PGHOST" );

        if ( dbServer != null )
        {
            return dbServer;
        }

        //
        //    Finally let's just use the default server which might be localhost.
        //
        return getDefaultServer();
    }


    protected String getDefaultServer()
    {
        //
        //    Changing this so that it can be used on both
        //    linux and windows. localhost should always work.
        //
        //    return ResourceManager.getEnvVar( "HOSTNAME" );
        //
        return LOCALHOST;
    }


    protected String getDefaultDatabase()
    {
        return OsUtils.getEnvVar( "PGDATABASE" );
    }

    
    public void execute( final String[]    args )
    {
        addOptions();

        try
        {
            if ( logger.isInfoEnabled() )
            {
                logger.info( "options: " + _options );
            }

            _commandLine = new PosixParser().parse( _options, args );

            checkOptions();
        }
        catch ( Throwable    ex )
        {
            showUsage( ex.getMessage() );

            //
            //    Show NPE stack traces and such
            //
            if( ! ( ex instanceof ParseException
                    || ex instanceof IllegalArgumentException ) )
            {
                ex.printStackTrace();
            }
        }
    }


    public static void initializeApplication( final String    logfilePrefix )
    {
        ToStringBuilder.setDefaultStyle( new S6ToStringStyle() );

        LoggerControl.redirectLoggers( logfilePrefix );
    }


    protected String getCustomer()
    {
        if ( _commandLine == null )
        {
            return OsUtils.getDefaultCust();
        }

        return getOptionValue( "c", OsUtils.getDefaultCust() );
    }


    protected void addOptions()
    {
        addFlagOption( "h",
                       "help",
                       "print out command line args" );
        addFlagOption( "version", "version", "basic version information" );

        addFlagOption( "versionfull", "versionfull", "extended version information" );

        addOption( "L",
                   "logger",
                   "Logger name(s) to set logging on -- separate with spaces or specify multiple times" );

        addOption( "configfile", "configfile", "property FILE to use to boot app", getDefaultPropFile() );

        addOption( "c", "customer", "CUST for application", OsUtils.getDefaultCust() );

        addFlagOption( "debug", "debug", "Add debug flag for debugging options" );

        addFlagOption( "dryrun", "dryrun", "Add dryrun flag for doing a dryrun" );
    }


    protected void addOption( final Option    option )
    {
        _options.addOption( option );
    }


    protected void addOption( final String    optionName,
                              final String    longOptionName,
                              final String    description )
    {
        addOption( optionName, longOptionName, true, description );
    }


    protected void addRequiredOption( final String    optionName,
                                      final String    description )
    {
        addRequiredOption( optionName, optionName, description );
    }


    protected void addRequiredOption( final String    optionName,
                                      final String    longOptionName,
                                      final String    description )
    {
        Option    option = new Option( optionName,
                                       longOptionName,
                                       true,
                                       description );
        option.setRequired( true );

        addOption( option );
    }


    protected void addOption( final String    optionName,
                              final String    longOptionName,
                              final String    description,
                              final String    defaultValue )
    {
        addOption( optionName,
                   longOptionName,
                   description + ", defaults to [" + defaultValue + "]" );
    }


    protected void addOption( final String     optionName,
                              final String     longOptionName,
                              final boolean    hasArg,
                              final String     description )
    {
        addOption( new Option( optionName,
                               longOptionName,
                               hasArg,
                               description ) );
    }


    protected void addFlagOption( final String    optionName,
                                  final String    description )
    {
        addOption( optionName, optionName, false, description );
    }


    protected void addFlagOption( final String    optionName,
                                  final String    longOptionName,
                                  final String    description )
    {
        addOption( optionName, longOptionName, false, description );
    }


    protected void checkOptions()
    {
        if ( hasOption( "h" ) )
        {
            showUsage();
        }

        if ( hasOption( "version" ) )
        {
            System.out.println( getVersionInfo().getVersion() );

            exit();
        }

        if ( hasOption( "versionfull" ) )
        {
            VersionInfo    info = getVersionInfo();
            System.out.println( info.getVersion() );
            System.out.println( StringUtilities.mapToString( info.getVersionMap(), "\n" ) );

            exit();
        }

        if ( hasOption( "L" ) )
        {
            for ( String logset : getOptionValues( "L" ) )
            {
                for ( String loggy : StringUtilities.split( logset, " " ) )
                {
                    LoggerControl.setLoggingLevel( loggy, Level.DEBUG );
                }
            }
        }

        _configFile = getOptionValue( "configfile", getDefaultPropFile() );

        _custCode   = getCustomer();

        if ( hasOption( "debug" ) )
        {
            _debug = true;
        }

        if ( hasOption( "dryrun" ) )
        {
            _dryrun = true;
        }
    }


    protected boolean hasOption( final String    optionName )
    {
        return _commandLine.hasOption( optionName );
    }


    protected String getOptionValue( final String    optionName )
    {
        return _commandLine.getOptionValue( optionName );
    }


    protected String[] getOptionValues( final String    optionName )
    {
        return _commandLine.getOptionValues( optionName );
    }


    protected String getOptionValue( final String    optionName,
                                     final String    defaultValue )
    {
        if ( hasOption( optionName ) )
        {
            return _commandLine.getOptionValue( optionName );
        }

        return defaultValue;
    }


    private void showUsage()
    {
        HelpFormatter    formatter = new HelpFormatter();
        formatter.printHelp( "java " + this.getClass().getName(),
                             getVersionInfo().getVersion(),
                             _options,
                             "" );
        exit();
    }


    /**
     *     Override if you want to specify more information about your application in particular.
     *
     * @return VersionInfo
     */
    public VersionInfo getVersionInfo()
    {
        return new BasicVersionInfo( "com.samsix.util" );
    }


    protected void showUsage( final String    msg )
    {
        System.out.println( msg + "\n" );
        showUsage();
    }


    protected String getDefaultPropFile()
    {
        //
        //    Leave as null, some apps (like RunQuery) need to check
        //    for null.
        //
        return null;
    }


    public void exit()
    {
        exit( 0 );
    }


    /**
     *    Convenience wrapper round core exit method.
     */
    public void exit( final int    exitCode )
    {
        System.exit( exitCode );
    }


    /**
     *    Exit the application after reporting exception that
     *    caused the need to exit.
     */
    public void exit( final Throwable    ex )
    {
        OsUtils.exit( ex );
    }


    /**
     *    Used to kick-off the application
     */
    protected void run()
        throws
            UtilException
    {
        // Do nothing.
    }


    public static void launch( final AbstractApplication    app,
                               final String[]               args )
    {
        try
        {
            app.execute( args );
            app.run();
        }
        catch ( Throwable    ex )
        {
            app.exit( ex );
        }
    }
}

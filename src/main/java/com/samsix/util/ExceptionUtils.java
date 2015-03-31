/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2010 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class ExceptionUtils
{
    /**
     *    Which way should the exception stack be printed out?
     *    The standard java way?  (TRUE)
     *    Or the non-standard, backwards, upside-down way? (FALSE)
     */
    private static boolean    _broadestFirst = false;


    private ExceptionUtils()
    {
        //    Not instantiable
    }


    /**
     *    Checks to see if this exception or any of its causes
     *    is an instance of the given throwable class
     *
     *    @param ex
     *    @param exceptionClass
     *    @return true if it is
     */
    public static boolean causedBy( final Throwable                     ex,
                                    final Class<? extends Throwable>    exceptionClass )
    {
        Throwable    cause = ex;

        while( cause != null && ! exceptionClass.isInstance( cause ) )
        {
            cause = cause.getCause();
        }

        return ( cause == null ) ? false : true;
    }


    /**
     * Checks to see if an exception or any of its causes are of a certain type. Returns the first
     * type in the chain if it exists, or null if no causes are of this type.
     * @param ex
     * @param exceptionClass
     * @return null or an exception
     */
    public static <T extends Throwable> T getCause( final Throwable    ex,
                                                    final Class<T>     exceptionClass )
    {
        Throwable    cause = ex;

        while( cause != null && ! exceptionClass.isInstance( cause ) )
        {
            cause = cause.getCause();
        }

        return ( cause == null ) ? null : exceptionClass.cast( cause );
    }


    /**
     *    Access the full stack trace of this object.
     *    <p>
     *    This is equivalent to using the printStackTrace(..)
     *    and sending in a StringBufferOutputStream.
     */
    public static String getTotalStackTrace( final Throwable    exception )
    {
        if( isBroadestFirst() )
        {
            StringWriter    writer = new StringWriter();
            exception.printStackTrace( new PrintWriter( writer, true ) );

            return writer.toString();
        }

        //
        //     Reverse the order of the exceptions
        //
        List<Throwable>    exceps = new ArrayList<Throwable>();
        Throwable    rev = exception;

        while ( rev != null && rev.getCause() != rev )
        {
            exceps.add( 0, rev );

            rev = rev.getCause();
        }

        StringBuffer   buffer = new StringBuffer();

        for ( int ii = 0; ii < exceps.size(); ii++ )
        {
            Throwable    ex = exceps.get( ii );
            Throwable    parent = null;
            if ( ii != 0 )
            {
                buffer.append( "\n\nIn context of:\n" );
                parent = exceps.get( ii -1 );
            }

            buffer.append( ex.toString() );
            StackTraceElement[]    trace = ex.getStackTrace();
            for ( int jj=0; jj < trace.length; jj++)
            {
                StackTraceElement    traceElement = trace[jj];

                boolean    done = false;
                if ( parent != null )
                {
                    StackTraceElement[]    parentTrace = parent.getStackTrace();
                    for ( int kk=0; kk < parentTrace.length; kk++ )
                    {
                        if ( parentTrace[kk].equals( traceElement ) )
                        {
                            done = true;
                            break;
                        }
                    }
                }

                buffer.append("\n\tat " + traceElement );

                if ( done )
                {
                    buffer.append( "\n\t..." );
                    break;
                }
            }
        }

        return buffer.toString();
    }


    public static boolean isUserError( final Throwable    ex )
    {
        Throwable    exception = ex;

        //
        //    Check to see if this exception or any of its causes is
        //    a user error
        //
        while( exception != null )
        {
            if ( exception instanceof SamSixException
                    && ((SamSixException) exception).isUserError() )
            {
                return true;
            }

            exception = exception.getCause();
        }

        return false;
    }


    /**
     *    Method to get the full message followed by the
     *    full Stack Trace if applicable.  That is, if the
     *    the exception is not a user error.  If the exception
     *    is a user error, this will return the same thing
     *    as the static getMessage( Throwable    ex ) method.
     */
    public static String getMessageAndStackTrace( final Throwable    ex )
    {
        return  getMessageAndStackTrace( ex, "\n\nCaused by:\n" );
    }


    public static String getMessageAndStackTrace( final Throwable    ex,
                                                  final String       delimiter )
    {
        String    msg = ExceptionUtils.getMessage( ex );

        if ( ! ExceptionUtils.isUserError( ex ) )
        {
            msg = msg + delimiter + ExceptionUtils.getTotalStackTrace( ex );
        }

        return msg;
    }


    /**
     *    Static method to recursively get the message text from the
     *    entire stack.  Messages are concatenated with the linefeed
     *    character.  This allows for a more informational
     *    message to be displayed to the user.
     */
    public static String getMessage( final Throwable    ex )
    {
        String      message = ex.getMessage();

        //
        //    It *appears* as though the SQLException hasn't been
        //    converted to nest?  It seems like it has it's own
        //    method of nesting, not sure why.  I don't know
        //    why Sun wouldn't have converted it.  Maybe they
        //    did, but just left these getNextException methods
        //    on for compatibility?   In my java source code, they
        //    aren't deprecated, though.
        //
        if ( ex instanceof SQLException )
        {
            String    sqlMessage = getSqlExceptionMessage( (SQLException) ex );
            if ( ! StringUtils.isBlank( sqlMessage ) )
            {
                if ( ! StringUtils.isBlank( message ) )
                {
                    message  += "\n" + sqlMessage;
                }
                else
                {
                    message = sqlMessage;
                }
            }
        }


        Throwable    cause = ex.getCause();

        if ( ex instanceof SamSixException
             && ((SamSixException) ex).isShowThisCauseOnly() )
        {
            return message;
        }

        if ( cause != null )
        {
            String causeMessage = ExceptionUtils.getMessage( cause );

            if ( ! StringUtils.isBlank( causeMessage ) )
            {
                if ( ! StringUtils.isBlank( message ) )
                {
                    //
                    //    ALWAYS use "broadest first" when showing error messages.
                    //    Otherwise, error messages end up with some non-human readable thing at the top,
                    //    confusing users with deeply technical details. This is especially important for user errors.
                    //
                    //    broadest/non-broadest should be used for stack traces only.
                    //

                    message = message + "\n" + causeMessage;
                }
                else
                {
                    message = causeMessage;
                }
            }
        }


        if ( ! StringUtils.isBlank( message ) )
        {
            return message;
        }

        return ex.getClass().getName() + ":  An error has been detected.";
    }


    private static String getSqlExceptionMessage( final SQLException    sqlEx )
    {
        StringBuilder    message = new StringBuilder();

        SQLException    tmpEx = sqlEx;

        while ( tmpEx.getNextException() != null)
        {
            tmpEx = tmpEx.getNextException();

            if ( tmpEx == null )
            {
                break;
            }

            message.append( "\n" ).append( tmpEx.getMessage() );
        }

        return message.toString();
    }


    public static void setBroadestFirst( final boolean     flag )
    {
        _broadestFirst = flag;
    }


    private static boolean isBroadestFirst()
    {
        //
        //    Ken:   Commenting out due to big refactor of removing the customer
        //           on the home object.  Not going to refactor this just to keep
        //           this in.  Use the setBroadestFirst method to set this at
        //           startup if you want to change the default.
        //
//        boolean broadestFirst = Home.get().getCustomer()
//            .getBoolean( "Exception.ShowBroadestFirst", false );
//
//        _broadestFirst = Boolean.valueOf( broadestFirst );
//
//        return _broadestFirst.booleanValue();

        return _broadestFirst;
    }
}

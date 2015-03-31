/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
/*
 ***************************************************************************
 *
 * Copyright (c) 2001-2011 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util;


import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.ExecutionException;




public abstract class SamSixException
    extends
        Exception
{
    static final long serialVersionUID = -6918170814349977279L;


    /**
     *    I want to know <em>when</em> this happens, so I can tell
     *    in our exception email when these happen.  I'm curious to see
     *    if some are being reported twice.
     */
    private final Date              _when;

    /**
     *     This is the message that will be printed when this
     *     exception is printed.
     */
    private String                  _msg;


    /**
     *    Is this just a user error, or a serious error.  This
     *    variable will be true if it is only a user error.
     */
    private boolean                 _userError;


    private boolean                 _showThisCauseOnly  = false;


    protected SamSixException()
    {
        _when = new Date();

        //
        //    I think we can't do this because of the way we do all those...
        //    new SomeException().getBlahException() in which we call init.  Bleh.
        //
//        this( null, null, false );
    }


    public SamSixException( final String    msg )
    {
        //
        //    Assuming isUserError is true since we have no cause of
        //    our exception.  If you don't want this behavior use the other
        //    constructor.
        //
        this( msg, null, true );
    }


    public SamSixException( final Throwable    cause )
    {
        this( null, cause, false );
    }


    public SamSixException( final String     msg,
                            final boolean    isUserError )
    {
        this( msg, null, isUserError );
    }


    public SamSixException( final String       msg,
                            final Throwable    cause )
    {
        this( msg, cause, false );
    }


    public SamSixException( final String       msg,
                            final Throwable    cause,
                            final boolean      isUserError )
    {
        _when = new Date();
        init( msg, cause );
        setUserError( isUserError );
    }


    public Date getWhen()
    {
        return _when;
    }


    protected void init( final String       msg,
                         final Throwable    cause )
    {
        init( msg );

        //
        //    ExecutionExceptions are uninteresting; they mean something
        //    happened in a SwingWorker that made the SwingWorker fail.
        //
        if( cause instanceof ExecutionException && cause.getCause() != null )
        {
            initCause( cause.getCause() );
        }
        else
        {
            initCause( cause );
        }
    }


    protected void init( final String    msg )
    {
        _msg = msg;
    }


    /**
     *     Test to see if this exception was caused by (either
     *     immediately, or somewhere in the layer hierarchy)
     *     an instance of the given class of exception.
     *     <p>
     *     This is to overcome the weakness of layer, which is
     *     that layering obscures the original reason for trouble.
     *     <p>
     *     To use this, code might be written thusly:
     *     <pre>
     *         catch ( Exception ex )
     *         {
     *             if ( ex.causedBy( IOException.class ) )
     *             {
     *                 //    do something
     *             }
     *             else
     *             {
     *                 //    do something else, like layer the exception
     *             }
     *         }
     *     </pre>
     *    @param exceptionClass   TBD TBD TBD
     */
    public boolean causedBy( final Class<? extends Throwable>    exceptionClass )
    {
        return ExceptionUtils.causedBy( this, exceptionClass );
    }


    /**
     *    Access the full stack trace of this object.
     *    <p>
     *    This is equivalent to using the printStackTrace(..)
     *    and sending in a StringBufferOutputStream.
     */
    public String getTotalStackTrace()
    {
        return ExceptionUtils.getTotalStackTrace( this );
    }


    public void forceNonUserError()
    {
        //
        //    First set the userError flag on this
        //    class and then recursively go through all the causes
        //    and turn the user error off on them.  This,
        //    thus forces the exception back to a non-user
        //    exception even it had been previously set as one.
        //
        setUserError( false );

        final Throwable cause = getCause();
        if ( ! ( cause instanceof SamSixException ) )
        {
            return;
        }

        //
        //    Now recursively check the cause for a User Error.
        //
        ((SamSixException) cause).forceNonUserError();
    }


    @Override
    public String getMessage()
    {
        return _msg;
    }


    public void setUserError()
    {
        setUserError( true );
    }


    public void setUserError( final boolean    flag )
    {
        _userError = flag;
    }


    public boolean isUserError()
    {
        return _userError;
    }


    public void setShowThisCauseOnly()
    {
        setShowThisCauseOnly( true );
    }


    public void setShowThisCauseOnly( final boolean    flag )
    {
        _showThisCauseOnly = flag;
    }


    public boolean isShowThisCauseOnly()
    {
        Throwable   ex = getCause();
        if ( ex instanceof SamSixException
             && ((SamSixException) ex).isShowThisCauseOnly() )
        {
            return true;
        }

        return _showThisCauseOnly;
    }


    /**
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    @Override
    public void printStackTrace( final PrintStream   stream )
    {
        synchronized ( stream )
        {
            stream.println( "\n" + getWhen() + "\n" );
        }

        super.printStackTrace( stream );
    }


    /**
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    @Override
    public void printStackTrace( final PrintWriter   writer )
    {
        synchronized ( writer )
        {
            writer.println( "\n" + getWhen() + "\n" );
        }

        super.printStackTrace( writer );
    }
}

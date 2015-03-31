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

/**
 *    This exists so we are able to have every exception in this
 *    package be a subclass of this exception so that we can
 *    distinquish between exceptions from this package and other
 *    packages, and so we can say "throws UtilException".
 */
public class UtilException
    extends
        SamSixException
{
    static final long serialVersionUID = 1804325795437032523L;


    public UtilException()
    {
        //    Do nothing
    }


    public UtilException( final String    msg )
    {
        init( msg );
    }


    public UtilException( final String       msg,
                          final Throwable    ex )
    {
        init( msg, ex );
    }


    public UtilException( final Throwable    ex )
    {
        super( ex );
    }


    public UtilException gotException( final Throwable    ex,
                                       final String       msg )
    {
        init( msg, ex );
        return this;
    }


    public UtilException gotUserError( final String    msg )
    {
        init( msg );
        setUserError();
        return this;
    }
}

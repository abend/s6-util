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

/**
 *    Sometimes, a method throws a checked exception and under normal
 *    circumstances it should never throw that exception. We don't
 *    want to bubble that exception up, so throw a Runtime exception
 *    if it actually does happen (as a result of programmer error or
 *    bad application state)
 */
public class UnexpectedCheckedException
    extends
        RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public UnexpectedCheckedException()
    {
        // do nothing
    }


    public UnexpectedCheckedException( final String message )
    {
        super( message );
    }


    public UnexpectedCheckedException( final Throwable cause )
    {
        super( cause );
    }


    public UnexpectedCheckedException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}

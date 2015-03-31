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
 * Copyright (c) 2007 Coned.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.image;




/**
 *    Incidates that we weren't able to find the specified icon.
 */
public class CantFindIconException
    extends
        RuntimeException
{
    private String      _path;
    
    static final long serialVersionUID = 1420855219459552853L;
    
    
    /**
     *    Creates an exception message indicating that we weren't able to load resources
     *
     * @param login
     *            The users's login.
     * @param cause
     *            The reason the resource was not found.
     */
    public CantFindIconException( final String      path,
                                  final Throwable   cause )
    {
        super( cause );
        
        _path = path;
    }
    
    
    @Override
    public String getMessage()
    {
        return "Unable to load the specified icon ["
                + _path
                + "].\n"
                + getCause().toString();
    }
    
    
    @Override
    public String toString()
    {
        return getMessage();
    }
}

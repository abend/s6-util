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
package com.samsix.util.string;

import org.apache.commons.lang3.builder.ToStringStyle;


public class UserToStringStyle
    extends
        ToStringStyle
{
//    private static Logger
//    logger = Logger.getLogger( UserToStringStyle.class );

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserToStringStyle()
    {
        setUseIdentityHashCode( false );
        setUseClassName( false );
        setContentStart( "" );
        setFieldNameValueSeparator( ": [" );
        setFieldSeparator( "]\n" );
        setContentEnd( "]" );
    }
}

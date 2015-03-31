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
 * Copyright (c) 2001-2008 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util;




public class JvmVersionUtils
{
    public boolean isVersion5()
    {
        if ( System.getProperty( "java.version", "" ).startsWith( "1.5" ) )
        {
            return true;
        }

        return false;
    }


    public boolean isVersion6()
    {
        if ( System.getProperty( "java.version", "" ).startsWith( "1.6" ) )
        {
            return true;
        }

        return false;
    }
}

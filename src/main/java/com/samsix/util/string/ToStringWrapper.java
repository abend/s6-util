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
package com.samsix.util.string;



/**
 *    Provides a way of getting an nice, neat, informative
 *    toString method for debugging/logging purposes for
 *    a class which has not got an informative toString
 *    method, such as those from Sun or other library/jar
 *    providers.
 */
public interface ToStringWrapper<T>
{
    /**
     *    Provide an informative toString output for a class which does
     *    not have.
     */
    String toString( T     source );
}

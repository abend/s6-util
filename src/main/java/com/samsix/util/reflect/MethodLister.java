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
 * Copyright (c) 2001,2002 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.reflect;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



/**
 *    List the methods of any class using reflection.
 */
final class MethodLister
{
    public MethodLister()
    {
        //    Do nothing.
    }


    /**
     *    Given an object and a method name, list the methods
     *    that match the method name on the class..
     */
    public List<Method> listMethods( final Class<?>    classObj,
                                     final String      methodName )
    {
        //
        //    Get the array of methods for my classname.
        //
        Method[] methods = classObj.getMethods();

        List<Method> methodSignatures = new ArrayList<Method>();

        //
        //    Loop round all the methods and print them out.
        //
        for ( int ii = 0; ii < methods.length; ++ii )
        {
            if ( methods[ii].getName().equals( methodName ) )
            {
                methodSignatures.add( methods[ii] );
            }
        }

        return methodSignatures;
    }
}

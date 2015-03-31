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
 * Copyright (c) 2001 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.reflect;


import java.lang.reflect.Method;
import java.util.List;

import com.samsix.util.UtilException;
import com.samsix.util.string.StringUtilities;



public final class InvokerException
    extends
        UtilException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    //
    //    Empty constructor
    //
    public InvokerException()
    {
        //    Do nothing
    }


    public UtilException
    cantConstruct( Class<?>     desiredClass,
                   Throwable    ex )
    {
        init( "Unable to construct an instance of class " +
        		"[" + desiredClass.getName() + "].",
              ex );

        return this;
    }


    public UtilException
    cantConstruct( String           className,
                   Throwable        ex )
    {
        init( "Unable to find class [" + className + "].", ex );

        return this;
    }


    public UtilException
    failedToInvokeMethod( Class<?>      objClass,
                          String        methodName,
                          Class<?>[]    parameterTypes,
                          Throwable     cause )
    {
        String paramString;

        try
        {
            paramString = StringUtilities.arrayToString( parameterTypes );
        }
        catch ( Exception ex )
        {
            //
            //    We are basically assuming that this will never
            //    happen because we are testing, and really, we can't
            //    do anything anyway.
            //
            paramString = "arrayToString freaked.";
        }

        String msg = "Failed to invoke method: [" + methodName + "], " +
        		     "on object of type [" + objClass + "] " +
        		     "with parameter types [" + paramString + "]";


        if ( cause instanceof NoSuchMethodException )
        {
            String methodSignaturesString;
            MethodLister lister = new MethodLister();

            //
            //    Try and get the method names and signatures that
            //    match the one we tried to invoke.
            //
            List<Method> methodSignatures = lister.listMethods( objClass, methodName );

            try
            {
                methodSignaturesString =
                    StringUtilities.collectionToString( methodSignatures,
                                                  "\n\t" );
            }
            catch ( Exception ex )
            {
                //
                //    We are basically assuming that this will never
                //    happen because we are testing, and really, we can't
                //    do anything anyway.
                //
                methodSignaturesString = "collectionToString freaked.";
            }

            //
            //    List any available matching methods.
            //
            if ( methodSignatures != null )
            {
                init( msg + ", The available methods that match " +
                		"[" + methodName + "] are:\n" + methodSignaturesString,
                      cause );

                return this;
            }

            //
            //    We really screwed up.
            //
            //    There didn't seem to be any methods of the name
            //    we tried to invoke on this object.
            //
        }

        init( msg, cause );

        return this;
    }


    public UtilException
    failedToInvokeConstructor( Class<?>      desiredClass,
                               Class<?>[]    parameterTypes,
                               Throwable     cause )
    {
        String paramString;

        try
        {
            paramString = StringUtilities.arrayToString( parameterTypes );
        }
        catch ( Exception ex )
        {
            //
            //    We are basically assuming that this will never
            //    happen because we are testing, and really, we can't
            //    do anything anyway.
            //
            paramString = "arrayToString freaked.";
        }

        String msg = "Failed to invoke constructor"
                     + " on object of type [" + desiredClass + "] " +
                       "with parameter types [" + paramString + "]";

        init( msg, cause );

        return this;
    }


    public UtilException failedToGetStaticFieldValue( String      className,
                                                      String      fieldName,
                                                      Throwable   ex )
    {
        String msg = "Failed to get static field value [" + fieldName + "] " +
        		     "for class [" + className + "]";
        init( msg, ex );

        return this;
    }
}

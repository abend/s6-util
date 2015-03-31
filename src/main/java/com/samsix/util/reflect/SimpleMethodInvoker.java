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
 * Copyright (c) 2001-2009 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.reflect;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.samsix.util.UtilException;


/**
 *    This class is a utility to invoke an arbitrary method on
 *    an arbitrary object.
 *    <p>
 *    We overload the 'invoke' method to allow us to have
 *    three options:
 *    <p>
 *    <ol>
 *    <li>call a method with NO arguments on an object.</li>
 *    <li>call a method with ONE argument on an object.</li>
 *    <li>call a method with ONE argument on an object AND its type.</li>
 *    </ol>
 *    <p>
 *    These situations should account for most of the coding
 *    situations that should arise.
 *    <p>
 *    By using this kind of wrapper class, we can add more
 *    checks to/information on the method invocation.
 *    <p>
 *    We can:
 *    <pre>
 *        Check the method is valid for the object and
 *        output better error messaging if its not, perhaps
 *        listing those methods that *are* available?
 *    </pre>
 *    <p>
 *    Currently, Sun's exceptions are rather terse, so we can put
 *    out nicer ones, as well as making the effort to invoke these
 *    methods <strong>much</strong> easier.
 *    <p>
 *    There is also a fairly common error programmers make when
 *    invoking methods this way. We'll avoid that for them.
 *    <p>
 */
public final class SimpleMethodInvoker
{
    private SimpleMethodInvoker()
    {
        //    prevent instantiation
    }


    /**
     *    A whole set of 'construct' methods to enable
     *    us to run particular constructors.
     */
    public static Object construct( final String    className )
        throws
            UtilException
    {
        try
        {
            return Class.forName( className ).newInstance();
        }
        catch ( Throwable    ex )
        {
            throw new InvokerException().cantConstruct( className, ex );
        }
    }


    public static <T> T construct( final String      className,
                                   final Class<T>    desiredClass )
        throws
            UtilException
    {
        Object    instance = construct( className );

        try
        {
            return desiredClass.cast( instance );
        }
        catch ( Throwable    ex )
        {
            throw new InvokerException().cantConstruct( className, ex );
        }
    }


    public static <T> T construct( final Class<T>    desiredClass )
        throws
            UtilException
    {
        try
        {
            return desiredClass.newInstance();
        }
        catch ( Throwable    ex )
        {
            throw new InvokerException().cantConstruct( desiredClass, ex );
        }
    }


    public static <T> T construct( final String      className,
                                   final Class<T>    desiredClass,
                                   final Object      param,
                                   final Class<?>    paramType )
        throws
            UtilException
    {
        Object    instance = construct( className, param, paramType );

        try
        {
            return desiredClass.cast( instance );
        }
        catch ( Throwable    ex )
        {
            throw new InvokerException().cantConstruct( className, ex );
        }
    }


    public static Object construct( final String      className,
                                    final Object      param,
                                    final Class<?>    paramType )
        throws
            UtilException
    {
        Class<?>        desiredClass;

        try
        {
            desiredClass = Class.forName( className );
        }
        catch ( Throwable    ex )
        {
            throw new InvokerException().cantConstruct( className, ex );
        }

        return construct( desiredClass, param, paramType );
    }


    public static <T> T construct( final Class<T>     desiredClass,
                                   final Object       param,
                                   final Class<?>     paramType )
        throws
            UtilException
    {
        Object[]       args       = { param };
        Class<?>[]     paramTypes = { paramType };

        return invokeConstructor( desiredClass, args, paramTypes );
    }


    /**
     *    Invoke an arbitrary 'no args' method on an arbitrary object.
     *
     *    i.e, we only specify the object and the method we wish
     *    to run.
     */
    public static Object invoke( final Object    obj,
                                 final String    methodName )
        throws
            UtilException
    {
        //
        //    We pass in empty (NOT null) arrays for args
        //    and parameterTypes.
        //
        //    The ultimate call to the invoke of the method
        //    can deal with this because, of course, we *do*
        //    have to deal with methods with no arguments.
        //
        Object[]        args = {};
        Class<?>[]      parameterTypes = {};

        return invokeMethod( obj,
                             methodName,
                             args,
                             parameterTypes );
    }


    /**
     *    Invoke an arbitrary one argument method on an arbitrary
     *    object and let the method work out the parameter's type.
     *    The user of this method had better be sure that the param
     *    isn't null.  If there's a chance it is null, then the
     *    invoke method with a paramType argument must be used.
     */
    public static Object invoke( final Object    obj,
                                 final String    methodName,
                                 final Object    param )
        throws
            UtilException
    {
        //
        //    This time we have a parameter passed in.
        //
        //    We can therefore work out it's class (type) and pass
        //    that through to our invokeOneArgMethod 'wrapper' method.
        //
        return invokeOneArgMethod( obj,
                                   methodName,
                                   param,
                                   param.getClass() );
    }


    /**
     *    Invoke an arbitrary one argument method on an arbitrary
     *    object, but also include the parameter's type.
     */
    public static Object invoke( final Object      obj,
                                 final String      methodName,
                                 final Object      param,
                                 final Class<?>    parameterType )
        throws
            UtilException
    {
        //
        //    For this call, we have all the information passed in to
        //    this method for us to use.
        //
        //    It may turn out to have the wrong contents etc. but the
        //    final method to actually invoke the 'methodName' stated
        //    here on the 'obj'(ect) stated here, will deal with that.
        //
        return invokeOneArgMethod( obj,
                                   methodName,
                                   param,
                                   parameterType );
    }


    //
    //    This is private because we are using it as a helper method
    //    to deal with the fact we have two variations on the 'One
    //    Arg' invoke; one with just the parameter and one with the
    //    parameter and its type.
    //
    //    The outside world doesn't need to know we use this, but just
    //    have access to the public static 'invoke()' methods for this class.
    //
    private static Object invokeOneArgMethod( final Object      obj,
                                              final String      methodName,
                                              final Object      param,
                                              final Class<?>    parameterType )
        throws
            UtilException
    {
        //
        //    We create an array of our single parameter and its type,
        //    because, ultimately to invoke a method, the args
        //    parameter need to be an array, not a single value.
        //
        Object[]     args           = { param };
        Class<?>[]    parameterTypes = { parameterType };

        //
        //    We now have the request method's arguument(s) and its
        //    type(s).
        //
        return invokeMethod( obj,
                             methodName,
                             args,
                             parameterTypes );
    }


    /**
     *    We have an object and a method name, but in order to invoke
     *    a method on an object, we need the object's Class (type).
     *
     *    Given this Class we can then check that such a method exists
     *    and throw an exception if it doesn't.
     */
    private static Object invokeMethod( final Object        obj,
                                        final String        methodName,
                                        final Object[]      args,
                                        final Class<?>[]    parameterTypes )
        throws
            UtilException
    {
        return invokeMethod( obj, obj.getClass(), methodName, args, parameterTypes );
    }


    /**
     *    Invoke an arbitrary one argument method on an arbitrary
     *    class and let the method work out the parameter's type.
     *    The user of this method had better be sure that the param
     *    isn't null.  If there's a chance it is null, then the
     *    invoke method with a paramType argument must be used.
     */
    public static Object invokeStaticMethod( final Class<?>    objClass,
                                             final String      methodName,
                                             final Object      param )
        throws
            UtilException
    {
        //
        //    This time we have a parameter passed in.
        //
        //    We can therefore work out it's class (type) and pass
        //    that through to our invokeOneArgStaticMethod 'wrapper' method.
        //
        return invokeOneArgStaticMethod( objClass,
                                         methodName,
                                         param,
                                         param.getClass() );
    }


    //
    //    This is private because we are using it as a helper method
    //    to deal with the fact we have two variations on the 'One
    //    Arg' invoke; one with just the parameter and one with the
    //    parameter and its type.
    //
    //    The outside world doesn't need to know we use this, but just
    //    have access to the public 'invoke()' methods for this class.
    //
    private static Object invokeOneArgStaticMethod( final Class<?>    objClass,
                                                    final String      methodName,
                                                    final Object      param,
                                                    final Class<?>    parameterType )
        throws
            UtilException
    {
        //
        //    We create an array of our single parameter and its type,
        //    because, ultimately to invoke a method, the args
        //    parameter need to be an array, not a single value.
        //
        Object[]      args           = { param };
        Class<?>[]    parameterTypes = { parameterType };

        //
        //    We now have the request method's arguument(s) and its
        //    type(s).
        //
        return invokeMethod( null,
                             objClass,
                             methodName,
                             args,
                             parameterTypes );
    }


    public static Object invokeStaticMethod( final String      objClass,
                                             final String      methodName,
                                             final Object[]    args,
                                             final String[]    parameterTypes )
        throws
            UtilException,
            ClassNotFoundException
    {
        Class<?>[]    classes = null;
        if ( parameterTypes != null )
        {
            classes = new Class[ parameterTypes.length ];
            for ( int ii = 0; ii < parameterTypes.length; ii ++ )
            {
                classes[ii] = Class.forName( parameterTypes[ii] );
            }
        }

        return invokeMethod( null,
                             Class.forName( objClass ),
                             methodName,
                             args,
                             classes );
    }


    private static Object invokeMethod( final Object         obj,
                                        final Class<?>       objClass,
                                        final String         methodName,
                                        final Object[]       args,
                                        final Class<?>[]     parameterTypes )
        throws
            UtilException
    {
        try
        {
            //
            //    We need to instantiate our Method object, given the
            //    methodName requested and its parameterTypes, that
            //    is, it's method signature.
            //
            Method    method;
            method = objClass.getMethod( methodName, parameterTypes );

            //
            //    Finally invoke the method we want on the object with
            //    the appropriate arguments.
            //
            return method.invoke( obj, args );
        }
        catch ( Throwable    ex )
        {
            //
            //    We could fail to invoke a method for many reasons.
            //
            //    The method we request may not even exist.  The
            //    parameter types we asked for may be wrong (i.e, we
            //    requested the wrong method signature.)
            //
            throw new InvokerException().failedToInvokeMethod( objClass,
                                                               methodName,
                                                               parameterTypes,
                                                               ex );
        }
    }


    /**
     *    We have an object and a method name, but in order to invoke
     *    a method on an object, we need the object's Class (type).
     *
     *    Given this Class we can then check that such a method exists
     *    and throw an exception if it doesn't.
     */
    private static <T> T invokeConstructor( final Class<T>     desiredClass,
                                            final Object[]     args,
                                            final Class<?>[]   parameterTypes )
        throws
            UtilException
    {
        try
        {
            //
            //    We need to instantiate our constructor object, given
            //    the parameter types, that is, it's method signature.
            //
            Constructor<T>    constructor;
            constructor = desiredClass.getConstructor( parameterTypes );


            //
            //    Finally invoke the method we want on the object with
            //    the appropriate arguments.
            //
            return constructor.newInstance( args );
        }
        catch ( Throwable    ex )
        {
            //
            //    We could fail to invoke a method for many reasons.
            //
            //    The constructor we request may not even exist.  The
            //    parameter types we asked for may be wrong (i.e, we
            //    requested the wrong method signature.)
            //
            throw new InvokerException()
                        .failedToInvokeConstructor( desiredClass,
                                                    parameterTypes,
                                                    ex );
        }
    }


    public static Object getStaticFieldValue( final String    className,
                                              final String    fieldName )
        throws
            UtilException
    {
        try
        {
            Class<?>    objClass = Class.forName( className );
            Field       field = objClass.getField( fieldName );

            return field.get( null );
        }
        catch ( Throwable    ex )
        {
            throw new InvokerException()
                      .failedToGetStaticFieldValue( className,
                                                    fieldName,
                                                    ex );
        }
    }
}

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
package com.samsix.util;


import org.apache.commons.lang3.builder.EqualsBuilder;


/**
 *    This is used when turning a dictionary to a list.
 *    <p>
 *    A tuple will contain a key and it's matching value in the
 *    dictionary that produced it.
 */
public final class Tuple<K,V>
{
    private final K    _key;
    private V          _value;


    public Tuple( final K    aKey,
                  final V    aValue )
    {
        _key   = aKey;
        _value = aValue;
    }


    //==========================================
    //
    //      Utility methods.
    //
    //==========================================


    public K getKey()
    {
        return _key;
    }


    public V getValue()
    {
        return _value;
    }


    public void setValue( final V  aValue )
    {
        _value = aValue;
    }


    //==========================================
    //
    //      Object i/f
    //
    //==========================================

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals( final Object obj )
    {
        if ( ! (obj instanceof Tuple) )
        {
            return false;
        }

        Tuple<K,V>    rhs = (Tuple<K,V>) obj;

        //
        //    Now use the equals() method for the String class.
        //
        return new EqualsBuilder()
                .append( _key, rhs._key )
                .append( _value, rhs._value )
                .isEquals();
    }


    @Override
    public int hashCode()
    {
        int      result = 211;

        if ( _key != null )
        {
            result = 37 * result + _key.hashCode();
        }

        if ( _value != null )
        {
            result = 37 * result + _value.hashCode();
        }

        return result;
    }


    @Override
    public String toString()
    {
        return "(" + getKey() + ", " + getValue() + ")";
    }
}

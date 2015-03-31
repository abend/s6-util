/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.io;


import java.awt.Color;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

import com.samsix.util.UtilException;


public interface ResourceReader
{
    public boolean isResourceDefined( final String    key );

    /**
     *    Returns an Object from a resource file given a key. We pass
     *    in the type so that the ResourceManager can confirm it can
     *    deal with such a type.
     *    <p>
     *    @param  key a key in the resource bundle
     *    <p>
     *    @return a <code>Object</code> value
     *    <p>
     *    @throws UtilException
     *            if key is not the name of a resource.
     */
    public Object getResource( final String    key,
                               final String    type )
        throws UtilException;


    public Object getObject( final String    key )
        throws UtilException;


    public Object getObject( final String    key,
                             final String    defaultClassName )
        throws UtilException;


    public Object getObject( final String         key,
                             final Object[]       args,
                             final Class<?>...    parameterTypes )
        throws
            UtilException;


    public Object getObject( final String         key,
                             final String         defaultClassName,
                             final Object[]       args,
                             final Class<?>...    parameterTypes )
        throws UtilException;

    /**
     *    Returns the string that is mapped with the given key
     *    <p>
     *    @param  key a key in the resource bundle
     *    <p>
     *    @return a <code>String</code> value
     *    <p>
     *    @throws UtilException
     *            if key is not the name of a resource.
     */
    public String getString( final String    key )
        throws UtilException;


    /**
     *    Returns the string that is mapped with the given key
     *    <p>
     *    @param  key a key in the resource bundle
     *    <p>
     *    @return a <code>String</code> value
     */
    public String getString( final String    key,
                             final String    defaultValue );


    public String getString( final String    key,
                             final String    overridingPrefix,
                             final String    defaultValue );

    public String getString( final String                key,
                             final Collection<String>    overridingPrefixes,
                             final String                defaultValue );

    /**
     *    Returns the tokens that compose the string mapped
     *    with the given key. Delimiters (" \t\n\r\f") are not returned.
     *    <p>
     *    @param  key          a key of the resource bundle
     *    <p>
     *    @return a <code>List</code> value
     *    <p>
     *    @throws UtilException
     *            if key is not the name of a resource
     */
    public List<String> getStringList( final String    key );


    /**
     *    Returns the tokens that compose the string mapped
     *    with the given key. Delimiters (" \t\n\r\f,") are not returned.
     *    <p>
     *    @param  key          a key of the resource bundle
     *    <p>
     *    @return a <code>List</code> value
     */
    public List<String> getStringList( final String          key,
                                       final List<String>    defaultValue );


    /**
     *    Returns the tokens that compose the string mapped
     *    with the given key.
     *    <p>
     *    Delimiters are not returned.
     *    <p>
     *    @param  key          a key of the resource bundle.
     *    @param  delim        the delimiters of the tokens.
     *    <p>
     *    @throws UtilException
     *            if key is not the name of a resource.
     */
    public List<String> getStringList( final String    key,
                                       final String    delim );


    /**
     *    Returns the tokens that compose the string mapped
     *    with the given key.
     *    <p>
     *    Delimiters are not returned.
     *    <p>
     *    @param  key          a key of the resource bundle.
     *    @param  delim        the delimiters of the tokens.
     */
    public List<String> getStringList( final String          key,
                                       final String          delim,
                                       final List<String>    defaultValue );


    public List<String> getStringListWithOverride( final String          key,
                                                   final String          overridingPrefix,
                                                   final List<String>    defaultValue );


    /**
     *    Returns the boolean mapped with the given key.
     *    <p>
     *    @param  key a key of the resource bundle.
     *    <p>
     *            if key is not the name of a resource.
     *    <p>
     *    @throws ResourceException if the resource is malformed.
     */
    public boolean getBoolean( final String    key )
        throws
            UtilException;


    public boolean getBoolean( final String     key,
                               final boolean    defaultValue );


    /**
     *    Returns the color mapped with the given key.
     *    <p>
     *    @param  key a key of the resource bundle.
     *    <p>
     *    @throws ResourceException if the resource is malformed.
     */
    public Color getColor( final String    key )
        throws
            UtilException;


    /**
     *    Returns the color mapped with the given key.
     *    <p>
     *    @param  key a key of the resource bundle.
     */
    public Color getColor( final String    key,
                           final Color     defaultValue );


    /**
     *    Returns the integer mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     *    <p>
     *            if key is not the name of a resource.
     *    @throws ResourceException if the resource is malformed.
     */
    public int getInt( final String    key )
        throws
            UtilException;


    /**
     *    Returns the integer mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     */
    public int getInt( final String    key,
                       final int       defaultValue );


    /**
     *    Returns the integer mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     *    <p>
     *            if key is not the name of a resource.
     *    @throws ResourceException if the resource is malformed.
     */
    public Integer getInteger( final String    key )
        throws
            UtilException;


    /**
     *    Returns the integer mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     */
    public Integer getInteger( final String     key,
                               final Integer    defaultValue );


    /**
     *    Returns the long mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     *    <p>
     *            if key is not the name of a resource.
     *    @throws ResourceException if the resource is malformed.
     */
    public long getLongValue( final String    key )
        throws
            UtilException;


    /**
     *    Returns the double mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     *    <p>
     *            if key is not the name of a resource.
     *    @throws ResourceException if the resource is malformed.
     */
    public double getDoubleValue( final String    key )
        throws
            UtilException;


    /**
     *    Returns the double mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     *    <p>
     *            if key is not the name of a resource.
     *    @throws ResourceException if the resource is malformed.
     */
    public Double getDouble( final String     key )
        throws
            UtilException;


    /**
     *    Returns the double mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     */
    public Double getDouble( final String    key,
                             final Double    defaultValue );


    public Double getDouble( final String    key,
                             final String    overridingPrefix,
                             final Double    defaultValue );

    public Double getDouble( final String                key,
                             final Collection<String>    overridingPrefixes,
                             final Double                defaultValue );

    /**
     *    Returns the double mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     */
    public double getDoubleValue( final String    key,
                                  final double    defaultValue );


    public double getDoubleValue( final String    key,
                                  final String    overridingPrefix,
                                  final double    defaultValue );


    public double getDoubleValue( final String                key,
                                  final Collection<String>    overridingPrefixes,
                                  final double                defaultValue );

    /**
     *    Returns the float mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     *    <p>
     *            if key is not the name of a resource.
     *    @throws ResourceException if the resource is malformed.
     */
    public float getFloatValue( final String    key )
        throws
            UtilException;


    /**
     *    Returns the float mapped with the given string.
     *    <p>
     *    @param key a key of the resource bundle.
     */
    public float getFloatValue( final String    key,
                                final float     defaultValue );


    public URL getResource( final String    path );


    public ImageIcon getImageIconResource( final String    key )
        throws
            UtilException;


    public ImageIcon getImageIconResource( final String    key,
                                           final String    defaultPath )
        throws
            UtilException;


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getImageIconResource(java.lang.String, javax.swing.ImageIcon)
     */
    public ImageIcon getImageIconResource( final String       key,
                                           final ImageIcon    defaultValue )
        throws
            UtilException;

    /**
     * Performs a case insensitive comparison of the property value given
     * by key to all enums in the given class, returning a matching enum.
     *
     * @param <T>
     * @param key
     * @param enumClass
     * @param defaultValue
     * @return an enum that matches, or defaultValue if no match was found.
     */
    public <T extends Enum<T>> T getEnum( final String      key,
                                          final Class<T>    enumClass,
                                          final T           defaultValue );

    /**
     *    Computes an attrstring string based on the values contained within
     *
     *    @param value
     *    @param blankOutNull
     *    @return
     */
    public String computeAttrString( final String     value,
                                     final boolean    blankOutNull );
}

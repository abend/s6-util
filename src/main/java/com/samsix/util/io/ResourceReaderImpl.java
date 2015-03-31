/*
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
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.samsix.util.Tuple;
import com.samsix.util.UtilException;
import com.samsix.util.image.CantFindIconException;
import com.samsix.util.image.IconFactory;
import com.samsix.util.io.ResourceReader;
import com.samsix.util.string.AttrString;
import com.samsix.util.string.StringUtilities;


/**
 *    A class responsible for handling access to Properties.
 */
public class ResourceReaderImpl
    implements
        ResourceReader
{
    /**
     *    We have to lazily initialize the logger, as it depends on
     *    top.log which can be set here in our properties.
     */
    private static Logger    logger;

    public static final String    DEFAULT_LIST_DELIMITER = " \t\n\r\f,";

    /**
     *    The list of override prefixes, checked in order from beginning to end.
     *    Usually looks something like this, when we have an
     *    application context:
     *        App.<app>.Env.<env>
     *        Env.<env>
     *        App.<app>
     */
    private final List<String> _overrides;

    /**
     *    The list of property sources to be checked in order.
     */
    private final List<Properties> _sources;

    private final AttrString _attrString;


    public ResourceReaderImpl()
    {
        _overrides  = new LinkedList<String>();
        _sources    = new LinkedList<Properties>();
        _attrString = new AttrString();
    }


    public ResourceReaderImpl( final Properties    props )
    {
        this( null, props, null );
    }


    public ResourceReaderImpl( final Map<String, String>    variables )
    {
        this( null, null, variables );
    }


    public ResourceReaderImpl( final String    propfile )
    {
        this( propfile, null, null );
    }


    public ResourceReaderImpl( final String                 propfile,
                               final Map<String, String>    variables )
    {
        this( propfile, null, variables );
    }


    public ResourceReaderImpl( final String                 propfile,
                               final Properties             initialValues,
                               final Map<String, String>    variables )
    {
        _attrString = new AttrString();
        _attrString.add( variables );

        _overrides = new LinkedList<String>();
        _sources = new LinkedList<Properties>();

        //
        //    Add any file properties, if they are specified.
        //
        if ( ! StringUtils.isBlank( propfile ) )
        {
            try
            {
                Properties    properties = new Properties();

                ResourceBundle    bundle = ResourceBundle.getBundle( propfile );

                for( String    key : bundle.keySet() )
                {
                    properties.setProperty( key, bundle.getString( key ) );
                }

                _sources.add( 0, properties );
            }
            catch ( Throwable    ex )
            {
                getLogger().warn( ex );
            }
        }

        if ( initialValues != null )
        {
            //
            //    NOTE:  I'm pretty sure we want to do this after
            //    loading from the file because I thing when nrgWeb is
            //    loaded it wants to override some values that are in the
            //    file sometimes.  I don't understand this but that's what
            //    seems to be happening.
            //
            _sources.add( 0, initialValues );
        }
    }


    public AttrString getAttrString()
    {
        return _attrString;
    }


    //=================================================================
    //
    //    Internal methods for key overrides
    //    and obtaining property values.
    //
    //=================================================================

    private String getPropertyValue( final String    key )
    {
        String    value = null;
        for ( Properties    source : _sources )
        {
            value = source.getProperty( key );

            if ( value != null )
            {
                break;
            }
        }

        return value;
    }


    /**
     *    Looks for each instance of "key" in all of the overrides, in order.
     *    Does not look for a non-overridden version.
     *
     *    @param key
     *    @return the first override found, or null if no overrides existed
     */
    private Tuple<String,String> getOverrideEntry( final String    key )
    {
        for ( String    prefix : _overrides )
        {
            String    override = prefix + "." + key;
            String    value    = getPropertyValue( override );

            if ( value != null )
            {
                return new Tuple<String,String>( override, value );
            }
        }

        return null;
    }


    /**
     *    Searches for a property of "key" using all overrides
     *
     *    @param key
     *    @return the value, or null if nothing was found
     */
    private Tuple<String,String> getEntry( final String    key )
    {
        Tuple<String,String>    override = getOverrideEntry( key );

        if ( override == null )
        {
            String    value = getPropertyValue( key );

            if ( value != null )
            {
                return new Tuple<String,String>( key, value );
            }
        }

        return override;
    }


    /**
     *    Searches for a property of "key" with all overrides, using the
     *    specified prefixes
     *
     *    @param key
     *    @param prefixes
     *    @return the value, or null if nothing was found
     */
    private Tuple<String,String> getEntry( final String                key,
                                           final Collection<String>    prefixes )
    {
        if ( CollectionUtils.isEmpty( prefixes ) )
        {
            return getEntry( key );
        }

        for( String    prefix : prefixes )
        {
            String    prefixedKey = prefix + "." + key;
            Tuple<String,String>    override = getOverrideEntry( prefixedKey );

            if ( override != null )
            {
                return override;
            }

            //
            //    Above we were checking overrides of the override. Here,
            //    just check for the first override. If that doesn't work,
            //    then we need to just pass it on and ignore the specified override.
            //
            String    value = getPropertyValue( prefixedKey );

            if ( value != null )
            {
                return new Tuple<String,String>( prefixedKey, value );
            }
        }

        //
        //    No prefixed overrides were found, so drop back to using
        //    the standard, non-prefixed version
        //
        return getEntry( key );
    }


    private String getFormattedPropValue( final String    key )
    {
        Tuple<String,String>    value = getEntry( key );

        if ( value == null )
        {
            return null;
        }

        return _attrString.computeString( value.getValue() );
    }


    private String getFormattedPropValue( final String                key,
                                          final Collection<String>    prefixes )
    {
        Tuple<String,String>    value = getEntry( key, prefixes );

        return ( value != null ) ? _attrString.computeString( value.getValue() )
                                 : null;
    }


    private String getRequiredPropValue( final String    key )
        throws
            UtilException
    {
        String    value = getFormattedPropValue( key );

        if ( value == null )
        {
            throw new ResourceException().missingResource( key );
        }
        else
        {
            return value;
        }
    }


    //===============================================
    //
    //      Formatting routines
    //
    //===============================================

    private Object formatObject( final String    key,
                                 final String    className )
        throws
            UtilException
    {
        try
        {
            return Class.forName( className ).newInstance();
        }
        catch ( Throwable    ex )
        {
            throw new ResourceException().cantGetObject( key, className, ex );
        }
    }


    private Object formatObject( final String         key,
                                 final String         className,
                                 final Object[]       args,
                                 final Class<?>...    parameterTypes )
        throws
            UtilException
    {
        try
        {
            Constructor<?>    constructor;
            constructor = Class.forName( className ).getConstructor( parameterTypes );
            return constructor.newInstance( args );
        }
        catch ( Throwable    ex )
        {
            throw new ResourceException().cantGetObject( key, className, ex );
        }
    }


    private List<String> formatStringList( final String    key,
                                           final String    value,
                                           final String    delim )
    {
        String    delimiter = getString( key + ".Delimiter", delim );
        return StringUtilities.createStringList( value, delimiter );
    }


    private List<String> formatStringList( final String          key,
                                           final String          value,
                                           final String          delim,
                                           final List<String>    defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        return formatStringList( key, value, delim );
    }


    private boolean formatBooleanValue( final String    value )
    {
        return Boolean.valueOf( value ).booleanValue();
    }


    private boolean formatBooleanValue( final String     value,
                                        final boolean    defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        return formatBooleanValue( value );
    }


    private double formatDoubleValue( final String    key,
                                      final String    value )
        throws
            UtilException
    {
        try
        {
            return Double.parseDouble( value );
        }
        catch ( NumberFormatException    ex )
        {
            throw new ResourceException().badFormatting( key,
                                                         double.class,
                                                         value,
                                                         ex );
        }
    }


    private double formatDoubleValue( final String    key,
                                      final String    value,
                                      final double    defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        try
        {
            return formatDoubleValue( key, value );
        }
        catch ( UtilException    ex )
        {
            getLogger().error( "Using default value.", ex );

            return defaultValue;
        }
    }


    private Double formatDouble( final String    key,
                                 final String    value )
        throws
            UtilException
    {
        try
        {
            return Double.valueOf( value );
        }
        catch ( NumberFormatException    ex )
        {
            throw new ResourceException().badFormatting( key,
                                                         Double.class,
                                                         value,
                                                         ex );
        }
    }


    private Double formatDouble( final String    key,
                                 final String    value,
                                 final Double    defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        try
        {
            return formatDouble( key, value );
        }
        catch ( UtilException    ex )
        {
            getLogger().error( "Using default value.", ex );

            return defaultValue;
        }
    }


    private float formatFloatValue( final String    key,
                                    final String    value )
        throws
            UtilException
    {
        try
        {
            return Float.parseFloat( value );
        }
        catch ( NumberFormatException    ex )
        {
            throw new ResourceException().badFormatting( key,
                                                         float.class,
                                                         value,
                                                         ex );
        }
    }


    private float formatFloatValue( final String    key,
                                    final String    value,
                                    final float     defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        try
        {
            return formatFloatValue( key, value );
        }
        catch ( UtilException    ex )
        {
            getLogger().error( "Using default value.", ex );

            return defaultValue;
        }
    }


    private long formatLongValue( final String    key,
                                  final String    value )
        throws
            UtilException
    {
        try
        {
            return Long.parseLong( value );
        }
        catch ( NumberFormatException    ex )
        {
            throw new ResourceException().badFormatting( key,
                                                         long.class,
                                                         value,
                                                         ex );
        }
    }


    private Integer formatInteger( final String    key,
                                   final String    value )
        throws
            UtilException
    {
        try
        {
            return Integer.valueOf( value );
        }
        catch ( NumberFormatException ex )
        {
            throw new ResourceException().badFormatting( key,
                                                         Integer.class,
                                                         value,
                                                         ex );
        }
    }


    private Integer formatInteger( final String     key,
                                   final String     value,
                                   final Integer    defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        try
        {
            return formatInteger( key, value );
        }
        catch ( UtilException    ex )
        {
            getLogger().error( "Using default value.", ex );

            return defaultValue;
        }
    }


    private int formatInt( final String    key,
                           final String    value )
        throws
            UtilException
    {
        try
        {
            return Integer.parseInt( value );
        }
        catch ( NumberFormatException    ex )
        {
            throw new ResourceException().badFormatting( key,
                                                         int.class,
                                                         value,
                                                         ex );
        }
    }


    private int formatInt( final String    key,
                           final String    value,
                           final int       defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        try
        {
            return formatInt( key, value );
        }
        catch ( UtilException    ex )
        {
            getLogger().error( "Using default value.", ex );

            return defaultValue;
        }
    }


    private Color formatColor( final String    key,
                               final String    value )
        throws
            UtilException
    {
        try {
            int h = Integer.parseInt( value.substring( 1 ), 16 );
            return new Color( ( h >>> 16 ) & 0xff,
                              ( h >>> 8 ) & 0xff,
                              h & 0xff );
        }
        catch ( Exception ex )
        {
            throw new ResourceException().badFormatting( key,
                                                         Color.class,
                                                         value,
                                                         ex );
        }
    }


    private Color formatColor( final String    key,
                               final String    value,
                               final Color     defaultValue )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return defaultValue;
        }

        try
        {
            return formatColor( key, value );
        }
        catch ( UtilException    ex )
        {
            getLogger().error( "Using default value.", ex );

            return defaultValue;
        }
    }


    //
    //    Given an image path try and return an ImageIcon.
    //
    private static ImageIcon resourceToImageIcon( final String     imagePath )
        throws
            UtilException
    {
        try
        {
            return IconFactory.getIcon( imagePath );
        }
        catch ( CantFindIconException    ex )
        {
            throw new ResourceException().cantFindImageLocation( imagePath, ex );
        }
    }


    // =======================================
    //
    //        ResourceReader i/f
    //
    // =======================================

    @Override
    public boolean isResourceDefined( final String    key )
    {
        return ( getEntry( key ) != null );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getResource(java.lang.String, java.lang.String)
     */
    @Override
    public Object getResource( final String    key,
                               final String    type )
    {
        throw new UnsupportedOperationException();
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getObject(java.lang.String)
     */
    @Override
    public Object getObject( final String    key )
        throws
            UtilException
    {
        return formatObject( key, getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getObject(java.lang.String, java.lang.String)
     */
    @Override
    public Object getObject( final String    key,
                             final String    defaultClassName )
        throws
            UtilException
    {
        return formatObject( key, getString( key, defaultClassName ) );
    }


    @Override
    public Object getObject( final String         key,
                             final Object[]       args,
                             final Class<?>...    parameterTypes )
        throws
            UtilException
    {
        String    className = getRequiredPropValue( key );

        return formatObject( key, className, args, parameterTypes );
    }


    @Override
    public Object getObject( final String         key,
                             final String         defaultClassName,
                             final Object[]       args,
                             final Class<?>...    parameterTypes )
        throws
            UtilException
    {
        String    className = getString( key, defaultClassName );

        return formatObject( key, className, args, parameterTypes );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getString(java.lang.String)
     */
    @Override
    public String getString( final String    key )
        throws
            UtilException
    {
        return getRequiredPropValue( key );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getString(java.lang.String, java.lang.String)
     */
    @Override
    public String getString( final String      key,
                             final String      defaultValue )
    {
        String  candidate;
        candidate = getFormattedPropValue( key );

        if ( candidate == null )
        {
            return defaultValue;
        }

        return candidate;
    }


    @Override
    public String getString( final String    key,
                             final String    overridingPrefix,
                             final String    defaultValue )
    {
        String  candidate;
        candidate = getFormattedPropValue( key, Collections.singleton( overridingPrefix ) );

        if ( candidate == null )
        {
            return defaultValue;
        }

        return candidate;
    }


    @Override
    public String getString( final String                key,
                             final Collection<String>    overridingPrefixes,
                             final String                defaultValue )
    {
        String  candidate;
        candidate = getFormattedPropValue( key, overridingPrefixes );

        if ( candidate == null )
        {
            return defaultValue;
        }

        return candidate;
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getStringList(java.lang.String)
     */
    @Override
    public List<String> getStringList( final String    key )
    {
        return getStringList( key, DEFAULT_LIST_DELIMITER );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getStringList(java.lang.String, java.util.List)
     */
    @Override
    public List<String> getStringList( final String             key,
                                       final List<String>       defaultValue )
    {
        return getStringList( key, DEFAULT_LIST_DELIMITER, defaultValue );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getStringList(java.lang.String, java.lang.String)
     */
    @Override
    public List<String> getStringList( final String    key,
                                       final String    delim )
    {
        return formatStringList( key, getFormattedPropValue( key ), delim );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getStringList(java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public List<String> getStringList( final String             key,
                                       final String             delim,
                                       final List<String>       defaultValue )
    {
        return formatStringList( key, getFormattedPropValue( key ), delim, defaultValue );
    }


    @Override
    public List<String> getStringListWithOverride( final String         key,
                                                   final String         overridingPrefix,
                                                   final List<String>   defaultValue )
    {
        return formatStringList( key,
                                 getFormattedPropValue( key, Collections.singleton( overridingPrefix ) ),
                                 DEFAULT_LIST_DELIMITER,
                                 defaultValue );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean( final String    key )
        throws
            UtilException
    {
        return formatBooleanValue( getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getBoolean(java.lang.String, boolean)
     */
    @Override
    public boolean getBoolean( final String     key,
                               final boolean    defaultValue )
    {
        return formatBooleanValue( getFormattedPropValue( key ), defaultValue );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getColor(java.lang.String)
     */
    @Override
    public Color getColor( final String     key )
        throws
            UtilException
    {
        return formatColor( key, getRequiredPropValue( key ) );
    }



    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getColor(java.lang.String, java.awt.Color)
     */
    @Override
    public Color getColor( final String     key,
                           final Color      defaultValue )
    {
        return formatColor( key, getFormattedPropValue( key ), defaultValue );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getInt(java.lang.String)
     */
    @Override
    public int getInt( final String    key )
        throws
            UtilException
    {
        return formatInt( key, getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getInt(java.lang.String, int)
     */
    @Override
    public int getInt( final String    key,
                       final int       defaultValue )
    {
        return formatInt( key, getFormattedPropValue( key ), defaultValue );
    }



    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getInteger(java.lang.String)
     */
    @Override
    public Integer getInteger( final String    key )
        throws
            UtilException
    {
        return formatInteger( key, getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getInteger(java.lang.String, java.lang.Integer)
     */
    @Override
    public Integer getInteger( final String     key,
                               final Integer    defaultValue )
    {
        return formatInteger( key, getFormattedPropValue( key ), defaultValue );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getLongValue(java.lang.String)
     */
    @Override
    public long getLongValue( final String    key )
        throws
            UtilException
    {
        return formatLongValue( key, getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getDouble(java.lang.String)
     */
    @Override
    public Double getDouble( final String    key )
        throws
            UtilException
    {
        return formatDouble( key, getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getDouble(java.lang.String, java.lang.Double)
     */
    @Override
    public Double getDouble( final String     key,
                             final Double     defaultValue )
    {
        return formatDouble( key, getFormattedPropValue( key ), defaultValue );
    }


    @Override
    public Double getDouble( final String                key,
                             final Collection<String>    overridingPrefixes,
                             final Double                defaultValue )
    {
        return formatDouble( key,
                             getFormattedPropValue( key, overridingPrefixes ),
                             defaultValue );
    }


    @Override
    public Double getDouble( final String    key,
                             final String    overridingPrefix,
                             final Double    defaultValue )
    {
        return formatDouble( key,
                             getFormattedPropValue( key, Collections.singleton( overridingPrefix ) ),
                             defaultValue );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getDoubleValue(java.lang.String)
     */
    @Override
    public double getDoubleValue( final String    key )
        throws
            UtilException
    {
        return formatDoubleValue( key, getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getDoubleValue(java.lang.String, double)
     */
    @Override
    public double getDoubleValue( final String       key,
                                  final double       defaultValue )
    {
        return formatDoubleValue( key, getFormattedPropValue( key ), defaultValue );
    }


    @Override
    public double getDoubleValue( final String    key,
                                  final String    overridingPrefix,
                                  final double    defaultValue )
    {
        return formatDoubleValue( key,
                                  getFormattedPropValue( key, Collections.singleton( overridingPrefix ) ),
                                  defaultValue );
    }


    @Override
    public double getDoubleValue( final String                key,
                                  final Collection<String>    overridingPrefixes,
                                  final double                defaultValue )
    {
        return formatDoubleValue( key,
                                  getFormattedPropValue( key, overridingPrefixes ),
                                  defaultValue );
    }

    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getFloatValue(java.lang.String)
     */
    @Override
    public float getFloatValue( final String    key )
        throws
            UtilException
    {
        return formatFloatValue( key, getRequiredPropValue( key ) );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getFloatValue(java.lang.String, float)
     */
    @Override
    public float getFloatValue( final String    key,
                                final float     defaultValue )
    {
        return formatFloatValue( key, getFormattedPropValue( key ), defaultValue );
    }


    @Override
    public <T extends Enum<T>> T getEnum( final String      key,
                                          final Class<T>    enumClass,
                                          final T           defaultValue )
    {
        String    strValue = getString( key, null );

        if( strValue != null )
        {
            for( T    value : enumClass.getEnumConstants() )
            {
                if( strValue.equalsIgnoreCase( value.name() ) )
                {
                    return value;
                }
            }
        }

        return defaultValue;
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getImageIconResource(java.lang.String)
     */
    @Override
    public ImageIcon getImageIconResource( final String    key )
        throws
            UtilException
    {
        return resourceToImageIcon( getRequiredPropValue( key ) );
    }


    @Override
    public ImageIcon getImageIconResource( final String    key,
                                           final String    defaultPath )
        throws
            UtilException
    {
        ImageIcon    icon = getImageIconResource( key, (ImageIcon)null );

        if ( icon != null )
        {
            return icon;
        }

        if ( defaultPath == null )
        {
            return null;
        }

        return resourceToImageIcon( defaultPath );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getImageIconResource(java.lang.String, javax.swing.ImageIcon)
     */
    @Override
    public ImageIcon getImageIconResource( final String       key,
                                           final ImageIcon    defaultValue )
        throws
            UtilException
    {
        String    imagePath = getFormattedPropValue( key );

        if ( imagePath == null )
        {
            return defaultValue;
        }

        return resourceToImageIcon( imagePath );
    }


    /* (non-Javadoc)
     * @see com.samsix.util.ResourceReader#getResource(java.lang.String)
     */
    @Override
    public URL getResource( final String    path )
    {
        return ResourceReaderImpl.class.getResource( path );
    }


    //===============================================
    //
    //    Helpers
    //
    //===============================================

    private Logger getLogger()
    {
        if ( logger != null )
        {
            return logger;
        }

        logger = Logger.getLogger( ResourceReaderImpl.class );

        return logger;
    }


    /**
     *    Adds a new Properties source to this set of Resources.
     *    The given source is added to the beginning of the list, making
     *    it the first source to be checked.
     */
    public void addSource( final Properties    source )
    {
        _sources.add( 0, source );
    }


    /**
     *    Adds a new override prefix to this set of resources.
     *    The given prefix is added to the beginning of the list.
     */
    public void addOverridePrefix( final String    prefix )
    {
        _overrides.add( 0, prefix );
    }


    public void addOverridePrefixes( final String[]    prefixes )
    {
        _overrides.addAll( 0, Arrays.asList( prefixes ) );
    }


    public void addAttributeMapping( final String    key,
                                     final Object    value )
    {
        _attrString.add( key, value );
    }


    @Override
    public String computeAttrString( final String     value,
                                     final boolean    blankOutNull )
    {
        return _attrString.computeString( value, blankOutNull );
    }
}

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
 *
 ***************************************************************************
 *
 * Copyright (c) 2001-2011 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *    A wrapper around reading from version.properties.
 */
public class BasicVersionInfo
    implements
        VersionInfo
{
    private final static String    UNKNOWN = "[unknown]";

    private ResourceBundle    _bundle;
    private final String      _versionPath;


    public BasicVersionInfo( final String    versionPath )
    {
        _versionPath = versionPath;
    }


    private ResourceBundle getResourceBundle()
    {
        if ( _bundle == null )
        {
            try
            {
                _bundle = ResourceBundle.getBundle( _versionPath + ".Version" );
            }
            catch ( Throwable    ex )
            {
                //
                //    Annoying because this happens in development mode.
                //
//                Logger.getLogger( BasicVersionInfo.class )
//                      .error( "Can't read version info.", ex );

                _bundle = new NullResourceBundle();
            }
        }

        return _bundle;
    }


    @Override
    public String getVersion()
    {
        return ResourceBundleUtils.getString( getResourceBundle(), "version.full", UNKNOWN );
    }


    @Override
    public Map<String, String> getVersionMap()
    {
//        if ( _bundle == null )
//        {
//            return Collections.singletonMap( _versionPath, UNKNOWN );
//        }
//
        return ResourceBundleUtils.bundleToStringMap( getResourceBundle(), _versionPath );
    }


    //================================
    //    NullResourceBundle class
    //================================

    static class NullResourceBundle
        extends
            ResourceBundle
    {
        @Override
        protected Object handleGetObject( final String key )
        {
            return null;
        }


        @Override
        public Enumeration<String> getKeys()
        {
            return Collections.enumeration( Collections.<String>emptyList() );
        }
    }
}

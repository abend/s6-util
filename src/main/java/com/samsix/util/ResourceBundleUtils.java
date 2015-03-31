/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2010 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

public class ResourceBundleUtils
{
    private ResourceBundleUtils()
    {
        //    Do nothing
    }
    
    
    /**
     *    Converts a resource bundle to a map after prepending suffix + "." to each property
     *    
     *    Things that aren't strings in the map (if there are any) are converted
     *    via .toString()
     *    
     *    @param bundle
     *    @return a key,value map of string,string
     */    
    public static Map<String,String> bundleToStringMap( final ResourceBundle    bundle,
    													final String            suffix )
    {
    	if ( bundle == null )
    	{
    		return Collections.<String,String>emptyMap();
    	}

    	String    theSuffix;
    	if ( StringUtils.isEmpty( suffix ) )
    	{
    		theSuffix = "";
    	}
    	else
    	{
    		theSuffix = suffix + ".";
    	}
    	
        Map<String,String>    map = new LinkedHashMap<String,String>();
        
        Enumeration<String>    keys = bundle.getKeys();
        while( keys.hasMoreElements() )
        {
            String    key = keys.nextElement();
            Object    value = bundle.getObject( key );
            
            String    strValue = ( value != null ) ? value.toString() : null;
                        
            map.put( theSuffix + key, strValue );
        }
        
        return map;
    }
    
    
    /**
     *    Converts a resource bundle to a map
     *    
     *    Things that aren't strings in the map (if there are any) are converted
     *    via .toString()
     *    
     *    @param bundle
     *    @return a key,value map of string,string
     */
    public static Map<String,String> bundleToStringMap( final ResourceBundle    bundle )
    {
    	return bundleToStringMap( bundle, null );
    }
    
    
    public static final String getString( final ResourceBundle    bundle,
                                          final String            key,
                                          final String            defaultValue )
    {
        if ( bundle == null )
        {
            return defaultValue;
        }
        
        try
        {
            return bundle.getString( key );
        }
        catch ( MissingResourceException    ex )
        {
            return defaultValue;
        }
    }    
}

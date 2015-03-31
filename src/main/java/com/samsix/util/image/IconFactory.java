/*
 ***************************************************************************
 *
 * Copyright (c) 2001-2011 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.image;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;



/**
 *    Just make it easy to find/load icons.
 *    Uses spring's classpath resources, so you
 *    specify icons this way:
 *    <p>
 *      "com/samsix/nrg/images/paw.png"
 *    <p>
 *    or
 *    <p>
 *      "incorsIcons/ix_op_all/16x16/shadow/user1_message.png"
 */
public class IconFactory
{
    protected static final Logger logger = Logger.getLogger( IconFactory.class );

    public static final String          UNABLE_TO_LOAD_ICON         = "incorsIcons/ix_ap_all/24x24/shadow/error.png";

    public static final String          NRG_ICON_16                 = "com/samsix/nrg/images/nrg.png";
    public static final String          NRG_ICON_24                 = "com/samsix/nrg/images/nrg_icon_24.png";
    public static final String          NRG_ICON_32                 = "com/samsix/nrg/images/nrg_icon_32.png";
    public static final String          ERROR_SMALL_ICON            = "incorsIcons/ix_sd_all/16x16/shadow/application_error.png";
    public static final String          ERROR_LARGE_ICON            = "incorsIcons/ix_sd_all/24x24/shadow/application_error.png";
    public static final String          HELP_SMALL_ICON             = "incorsIcons/ix_ap_all/16x16/shadow/help2.png";
    public static final String          HELP_LARGE_ICON             = "incorsIcons/ix_ap_all/24x24/shadow/stop.png";
    public static final String          CLEAR_TEXT_FIELD_SMALL      = "com/samsix/nrg/images/clearText_16x16.png";

    public static final String          SMALL_USER_MESSAGE_ICON     = "incorsIcons/ix_op_all/16x16/shadow/user1_message.png";
    public static final String          LARGE_USER_MESSAGE_ICON     = "incorsIcons/ix_op_all/24x24/shadow/user1_message.png";
    public static final String          BROADCAST_MESSAGE_ICON      = "incorsIcons/ix_bd_all/16x16/shadow/message.png";


    public static final String          SMALL_USER_ICON             = "incorsIcons/ix_op_all/16x16/shadow/user3.png";
    public static final String          ADD_USER_ICON               = "incorsIcons/ix_op_all/16x16/shadow/user1_add.png";
    public static final String          LARGE_USER_ICON             = "incorsIcons/ix_op_all/24x24/shadow/user3.png";

    public static final String          SMALL_JOIN_ICON             = "incorsIcons/ix_op_all/16x16/shadow/users_into.png";
    public static final String          LARGE_JOIN_ICON             = "incorsIcons/ix_op_all/24x24/shadow/users_into.png";

    public static final String          SMALL_BROWSER_ICON          = "incorsIcons/ix_ns_all/16x16/shadow/earth_network.png";
    public static final String          LARGE_BROWSER_ICON          = "incorsIcons/ix_ns_all/24x24/shadow/earth_network.png";

    private static Map<String,ImageIcon>    _iconCache;


    private IconFactory()
    {
        //    do nothing
    }


    public static Image getIconImage( final String    path )
    {
        return getIconSafe( path ).getImage();
    }


    public static ImageIcon getIcon( final String    path )
    {
        assert path != null;

        ImageIcon   icon = getIconCache().get( path );
        if ( icon != null )
        {
            return icon;
        }

        try
        {
            return getIconAndCache( path );
        }
        catch ( Throwable   ex )
        {
            throw new CantFindIconException( path, ex );
        }
    }


    public static ImageIcon getIconSafe( final String    path )
    {
        if ( StringUtils.isBlank( path ) )
        {
            return null;
        }

        ImageIcon   icon = getIconCache().get( path );
        if ( icon != null )
        {
            return icon;
        }

        try
        {
            icon = getIconAndCache( path );

            if ( icon == null )
            {
                logger.warn( "IconFactory.getIconSafe: Can't find icon [" + path + "]" );

                return getMissingIcon();
            }

            return icon;
        }
        catch ( Throwable   ex )
        {
            logger.error( "IconFactory.getIconSafe: Can't retrieve icon [" + path + "]", ex );

            return getMissingIcon();
        }
    }


    private static ImageIcon getMissingIcon()
    {
        try
        {
            ImageIcon    icon = getIconAndCache( UNABLE_TO_LOAD_ICON );
            if ( icon == null )
            {
            	throw new IllegalStateException( "No missing icon!" );
            }

            return icon;
        }
        catch ( Throwable    ignore )
        {
            BufferedImage    image = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_RGB );

            Graphics    graphics = image.getGraphics();
            graphics.setColor( Color.red );
            graphics.fillRect( 0, 0, 16, 16 );
            graphics.dispose();

            return new ImageIcon( image );
        }
    }


    private static ImageIcon getIconAndCache( final String    path )
        throws
            IOException
    {
        if ( StringUtils.isBlank( path ) )
        {
            return null;
        }

        ClassPathResource    resource = new ClassPathResource( path );

        ImageIcon    icon;

        if ( resource.exists() )
        {
            icon = new ImageIcon( resource.getURL() );
        }
        else
        {
            //
            //      look for the icon outside of the classpath
            //
            try
            {
                icon = new ImageIcon( new URL( path ) );
            }
            catch( MalformedURLException    ex )
            {
                logger.warn( "IconFactory.getIconAndCache: Bad icon path [" + path + "], file not found "
                             + "and not a proper URL" );
                return null;
            }
        }

        getIconCache().put( path, icon );

        return icon;
    }


    @SuppressWarnings("unchecked")
    private static Map<String,ImageIcon> getIconCache()
    {
        if ( _iconCache == null )
        {
            _iconCache = new ReferenceMap( ReferenceMap.SOFT, ReferenceMap.SOFT );
        }

        return _iconCache;
    }
}

/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.image;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

public enum ImageType
{
    PNG,
    JPEG,
    GIF;


    public static ImageType valueOfExtension( final String    extension )
    {
        String    upper = extension.toUpperCase();

        //
        //    special case for .jpg and .jpeg being the same
        //
        if( "JPG".equals( upper ) )
        {
            return JPEG;
        }

        return valueOf( upper );
    }


    public String getMimeType()
    {
        return "image/" + name().toLowerCase();
    }


    public String getFileExtension()
    {
        switch( this )
        {
        //
        //    special case for .jpg and .jpeg being the same
        //
        case JPEG:
            return "jpg";

        default:
            return name().toLowerCase();
        }
    }


    public ImageWriter getImageWriter()
    {
        Iterator<ImageWriter>    writers;
        writers = ImageIO.getImageWritersByFormatName( name().toLowerCase() );

        if( ! writers.hasNext() )
        {
            throw new IllegalArgumentException( "Unable to encode images of type ["
                                                + this + "]" );
        }

        return writers.next();
    }
}

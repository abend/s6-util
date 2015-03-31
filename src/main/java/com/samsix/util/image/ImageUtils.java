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
 *
 */
package com.samsix.util.image;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 *    Utilities for dealing with images
 */
public final class ImageUtils
{
    public static String IMAGE_QUALITY_KEY = "imageQuality";
    public static float DEFAULT_IMAGE_QUALITY = 0.75f;

    private ImageUtils()
    {
        //    Not instantiable
    }


    /**
     *    Writes the image to the given stream
     *
     *    @param image
     *    @param imageType
     *    @param stream
     *    @throws IOException
     */
    public static void writeImageToStream( final RenderedImage    image,
                                           final ImageType        imageType,
                                           final OutputStream     stream )
        throws
            IOException
    {
        writeImageToStream( image, imageType, stream, null );
    }


    /**
     *    Writes the image to the given stream, using any specified parameters.
     *
     *    @param image
     *    @param imageType
     *    @param stream
     *    @param parameters
     *    @throws IOException
     */
    public static void writeImageToStream( final RenderedImage    image,
                                           final ImageType        imageType,
                                           final OutputStream     stream,
                                           final Map<String,?>    parameters )
        throws
            IOException
    {
        ImageWriter    writer = imageType.getImageWriter();

        ImageOutputStream       imageOutputStream;
        imageOutputStream = new MemoryCacheImageOutputStream( stream );

        IIOImage   outputImage = new IIOImage( image, null, null );

        ImageWriteParam    params = null;

        //
        //    JPEG has special considerations for quality
        //
        if( imageType == ImageType.JPEG )
        {
            params = writer.getDefaultWriteParam();
            params.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );

            Float    quality = null;
            if( parameters != null )
            {
                 quality = (Float) parameters.get( IMAGE_QUALITY_KEY );
            }

            params.setCompressionQuality( quality != null ? quality : DEFAULT_IMAGE_QUALITY );
        }

        writer.setOutput( imageOutputStream );

        try
        {
            writer.write( null, outputImage, params );
        }
        finally
        {
            try
            {
                imageOutputStream.close();
            }
            catch( IOException    ioe )
            {
                //    ignore...
            }

            try
            {
                writer.dispose();
            }
            catch( Throwable    ex )
            {
                //    Ignore...
            }
        }
    }

}

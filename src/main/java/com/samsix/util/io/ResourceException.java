package com.samsix.util.io;

import com.samsix.util.UtilException;


public final class ResourceException
    extends
        UtilException
{
    public ResourceException()
    {
        //    Do nothing.
    }


    public ResourceException cantFindImageLocation( final String       imagePath,
                                                    final Throwable    ex )
    {
        init( "The image path ["
              + imagePath
              + "] could not be loaded.",
              ex );

        return this;
    }



    public ResourceException missingResource( final String     key )
    {
        init( "The REQUIRED key ["
            + key
            + "] could not be found." );

        return this;
    }



    public ResourceException doNotRecognizeType( final String     key,
                                                 final String     type,
                                                 final String     candidate )
    {
        init( "Do not recognize type ["
              + type
              + "] when looking up value ["
              + candidate
              + "] with key ["
              + key
              + "]" );

        return this;
    }


    public ResourceException cantTranslateValue( final String     key,
                                                 final String     type,
                                                 final String     candidate )
    {
        init( "Could not translate the value ["
              + candidate
              + "] to the resource type ["
              + type
              + "] for the key ["
              + key
              + "]" );

        return this;
    }


    public ResourceException badFormatting( final String       key,
                                            final Class<?>     desiredType,
                                            final String       candidate,
                                            final Throwable    ex )
    {
        String    msg;
        msg = "Could not translate the value ["
            + candidate
            + "] of property ["
            + key
            + "] into the desired type ["
            + desiredType.getName()
            + "]";

        if ( ex == null )
        {
            init( msg );
        }
        else
        {
            init( msg, ex );
        }

        return this;
    }


    public ResourceException cannotInstallUserProperties( final Throwable    ex )
    {
        init( "Unable to install User properties.", ex );

        return this;
    }


    public UtilException cantGetObject( final String       key,
                                        final String       className,
                                        final Throwable    ex )
    {
        init( "Could not instantiate an object of class ["
              + className
              + "] associated with key ["
              + key
              + "].",
              ex );

        return this;
    }
}

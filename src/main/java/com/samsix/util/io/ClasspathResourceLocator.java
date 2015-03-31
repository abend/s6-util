package com.samsix.util.io;

import java.net.URL;

public class ClasspathResourceLocator
    implements
        ResourceLocator
{
    private final ClassLoader _classLoader;

    public ClasspathResourceLocator()
    {
        _classLoader = getClass().getClassLoader();
    }


    public ClasspathResourceLocator( final ClassLoader classLoader )
    {
        _classLoader = classLoader;
    }


    @Override
    public URL locateResource( final String name )
    {
        return _classLoader.getResource( name );
    }
}

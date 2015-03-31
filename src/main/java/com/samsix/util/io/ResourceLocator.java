package com.samsix.util.io;

import java.net.URL;

public interface ResourceLocator
{
    public URL locateResource( final String name );
}

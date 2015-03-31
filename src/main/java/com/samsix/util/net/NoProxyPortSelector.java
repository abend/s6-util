/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.net;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *    Forces java to not use a proxy for the given list of ports
 *    when installed. 
 */
public class NoProxyPortSelector
    extends
        ProxySelector
{
    private static final Integer        DEFAULT_NO_PROXY_PORTS[] = 
    {
         5432,  // PostgreSQL
         61616  // JMS
    };
    
    private static final List<Proxy>    NO_PROXY = Collections.singletonList( Proxy.NO_PROXY );
    
    private static NoProxyPortSelector    _instance;
    
    /** The default system proxy selector. */
    private ProxySelector         _delegate;
    
    /** The list of ports to force not to proxy. */
    private final Set<Integer>    _ports;
    
    
    private NoProxyPortSelector()
    {
        _ports = new HashSet<Integer>( Arrays.asList( DEFAULT_NO_PROXY_PORTS ) );
    }
        
    /**
     *    Retrieve (and create, if necessary) the singleton instance. 
     */
    public static NoProxyPortSelector getInstance()
    {
        if( _instance == null )
        {
            synchronized( NoProxyPortSelector.class )
            {
                if( _instance == null )
                {
                    _instance = new NoProxyPortSelector();
                }
            }
        }
        
        return _instance;
    }
    
    
    /**
     *    Installs this proxy selector, if it hasn't been already.
     */
    public void install()
    {
        if( _delegate != null )
        {
            throw new IllegalStateException( "Proxy delegate already installed!" );
        }
        
        _delegate = ProxySelector.getDefault();
        ProxySelector.setDefault( this );
    }
    
    
    public void addNoProxyPort( int    port )
    {
        synchronized( _ports )
        {
            if( ! _ports.contains( port ) )
            {
                _ports.add( port );
            }
        }
    }
    

    @Override
    public void connectFailed( URI              uri, 
                               SocketAddress    sa, 
                               IOException      ioe )
    {
        if( _delegate != null )
        {
            _delegate.connectFailed( uri, sa, ioe );
        }
    }
    

    @Override
    public List<Proxy> select( URI    uri )
    {
        if ( uri == null ) 
        {
            throw new IllegalArgumentException( "URI can't be null." );
        }

        if( "socket".equals( uri.getScheme() ) )
        {
            synchronized( _ports ) 
            {
                if( _ports.contains( Integer.valueOf( uri.getPort() ) ) )
                {
                    return NO_PROXY;
                }
            }
        }
        
        if( _delegate != null )
        {
            return _delegate.select( uri );
        }
        
        return NO_PROXY;
    }    
}

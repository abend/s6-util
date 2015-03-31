package com.samsix.util.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.samsix.util.string.StringUtilities;

/**
 *    Simple class to handle matching an internet address against a collection of network blocks
 */
public class SimpleInetAddressMatcher
{
    private final List<InetAddressBlock>    _blocks;


    private SimpleInetAddressMatcher( final List<InetAddressBlock>    blocks )
    {
        _blocks = blocks;
    }


    public boolean matchesAny( final InetAddress    address )
    {
        for( InetAddressBlock    block : _blocks )
        {
            if( block.matches( address ) )
            {
                return true;
            }
        }

        return false;
    }


    public boolean matchesAll( final InetAddress    address )
    {
        boolean    matches = true;

        for( InetAddressBlock    block : _blocks )
        {
            matches &= block.matches( address );
        }

        return matches;
    }


    public boolean matchesNone( final InetAddress    address )
    {
        return ! matchesAny( address );
    }


    /**
     * @param txtBlocks
     * @return null if txtBlocks is null or empty
     */
    public static SimpleInetAddressMatcher valueOf( final List<String>    txtBlocks )
    {
        if( txtBlocks == null || txtBlocks.isEmpty() )
        {
            return null;
        }

        List<InetAddressBlock>    addrBlocks = new ArrayList<InetAddressBlock>( txtBlocks.size() );

        for( String    txtBlock : txtBlocks )
        {
            addrBlocks.add( InetAddressBlock.valueOf( txtBlock ) );
        }

        return new SimpleInetAddressMatcher( addrBlocks );
    }


    @Override
    public String toString()
    {
        return "{" + StringUtilities.collectionToString( _blocks, "|" ) + "}";
    }
}

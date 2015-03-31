package com.samsix.util.net;

import java.net.Inet4Address;

public class NetUtils
{
    private NetUtils()
    {
        // Not instantiable
    }


    /**
     * Turns an Inet4Address into a 32-bit integer representation
     * @param addr
     * @return
     */
    public static int ipv4ToInt( final Inet4Address    addr )
    {
        int    value = 0;

        for( byte    chunk : addr.getAddress() )
        {
            value <<= 8;
            value |= chunk & 0xff;
        }

        return value;
    }


    /**
     * Given the number of bits in a netmask (the "prefix length"), return a suitable netmask as a 32-bit
     * integer representation. e.g., if "24" is given, return 0xFFFFFF00.
     * @param length
     * @return
     */
    public static int ipv4LengthToMask( final int    length )
    {
        return ~ ( ( 1 << 32 - length ) - 1 );
    }
}

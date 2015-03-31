package com.samsix.util.net;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 *    An implementation of CIDR netmasks for IPv4 and IPv6
 */
public abstract class InetAddressBlock
{
    protected final static Logger logger = Logger.getLogger( InetAddressBlock.class );

    private final InetAddress    _baseAddress;
    private final int            _maskLength;


    protected InetAddressBlock( final InetAddress    address,
                                final int            maskLength )
    {
        _baseAddress = address;
        _maskLength = maskLength;
    }


    @Override
    public String toString()
    {
        return _baseAddress.getHostAddress() + "/" + _maskLength;
    }


    public abstract boolean matches( final InetAddress    address );

    public abstract Class<? extends InetAddress> getMatchedType();


    /**
     * @param cidr An IPv4 or IPv6 CIDR block specified in one of the following forms:
     * <ul><li>192.168.2.0/24 (ipv4)</li>
     *     <li>192.168.2.0/255.255.255.0 (ipv4 only)</li>
     *     <li>2620:0:2d0:200::7/32 (ipv6)</li>
     * @return
     */
    public static InetAddressBlock valueOf( final String    cidr )
    {
        int    slashPos = cidr.indexOf( '/' );
        if( slashPos < 0 )
        {
            throw new IllegalArgumentException( "Invalid CIDR notation: " + cidr );
        }

        String    addrText = cidr.substring( 0, slashPos );
        String    maskText = cidr.substring( slashPos + 1 );

        InetAddress baseAddr;
        try
        {
            // Triggers a dns lookup if addrText isn't an ip address. yuck?
            baseAddr = InetAddress.getByName( addrText );
        }
        catch ( UnknownHostException ex )
        {
            throw new IllegalArgumentException( "Invalid CIDR base address [" + addrText + "]", ex );
        }

        int maskLength;

        if( baseAddr instanceof Inet4Address && maskText.indexOf( '.' ) > 0 )
        {
            //
            //    Counts the number of bits in a netmask (e.g. 255.255.240.0)
            //    Simple code; invalid netmasks can be specified and will have unexpected results.
            //
            StringTokenizer    tokenizer = new StringTokenizer( maskText, "." );

            maskLength = 0;
            while( tokenizer.hasMoreTokens() )
            {
                maskLength += Integer.bitCount( Integer.parseInt( tokenizer.nextToken() ) );
            }
        }
        else
        {
            maskLength = Integer.parseInt( maskText );
        }

        if( maskLength < 0 )
        {
            throw new IllegalArgumentException( "Invalid netmask: " + maskLength );
        }

        if( baseAddr instanceof Inet4Address )
        {
            if( maskLength > 32 )
            {
                throw new IllegalArgumentException( "Invalid netmask: " + maskLength );
            }

            return new CIDR4( (Inet4Address) baseAddr, maskLength );
        }
        else if( baseAddr instanceof Inet6Address )
        {
            if( maskLength > 128 )
            {
                throw new IllegalArgumentException( "Invalid netmask: " + maskLength );
            }

            return new CIDR6( (Inet6Address) baseAddr, maskLength );
        }
        else
        {
            throw new IllegalStateException( "Unknown address type " + baseAddr.getClass() );
        }
    }


    static class CIDR4 extends InetAddressBlock
    {
        private final int    _network;
        private final int    _mask;

        CIDR4( final Inet4Address    addr,
               final int             masklength )
        {
            super( addr, masklength );

            int network = NetUtils.ipv4ToInt( addr );
            _mask = NetUtils.ipv4LengthToMask( masklength );

            //
            //    Clean up the address if it's specified as something like 192.168.2.6/24.
            //
            _network = network & _mask;
        }


        @Override
        public boolean matches( final InetAddress    addr )
        {
            if( ! ( addr instanceof Inet4Address ) )
            {
                return false;
            }

            boolean match = ( NetUtils.ipv4ToInt( (Inet4Address) addr ) & _mask ) == _network;

            if( logger.isDebugEnabled() )
            {
                logger.debug( "Test [" + addr.getHostAddress() + "] in [" + this + "]: " + match );
            }

            return match;
        }


        @Override
        public Class<? extends InetAddress> getMatchedType()
        {
            return Inet4Address.class;
        }
    }


    static class CIDR6 extends InetAddressBlock
    {
        private final BigInteger    _network;
        private final BigInteger    _mask;


        CIDR6( final Inet6Address    addr,
               final int             masklength )
        {
            super( addr, masklength );

            BigInteger    network = new BigInteger( 1, addr.getAddress() );
            _mask = BigInteger.ONE.shiftLeft( 128 - masklength ).subtract( BigInteger.ONE ).not();
            _network = network.and( _mask );
        }


        @Override
        public boolean matches( final InetAddress    addr )
        {
            if( ! ( addr instanceof Inet6Address ) )
            {
                return false;
            }

            boolean match = new BigInteger( 1, addr.getAddress() ).and( _mask ).equals( _network );

            if( logger.isDebugEnabled() )
            {
                logger.debug( "Test [" + addr.getHostAddress() + "] in [" + this + "]: " + match );
            }

            return match;
        }


        @Override
        public Class<? extends InetAddress> getMatchedType()
        {
            return Inet6Address.class;
        }
    }


//    public static void main( final String[] args ) throws UnknownHostException
//    {
//        ConsoleAppender    appender = new ConsoleAppender( new PatternLayout( "[%C{1}] %m%n" ) );
//        appender.activateOptions();
//        Logger.getRootLogger().addAppender( appender );
//        logger.setLevel( Level.DEBUG );
//
//        InetAddressBlock   addrBlock;
//        addrBlock = InetAddressBlock.valueOf( "2620:0:2d0:200::7/32" );
//
//        addrBlock.matches( InetAddress.getByName( "2620:0:2d0:200::7" ) );
//        addrBlock.matches( InetAddress.getByName( "2621:0:2d0:200::7" ) );
//        addrBlock.matches( InetAddress.getByName( "ffff:0:2d0:200::7" ) );
//
//        addrBlock = InetAddressBlock.valueOf( "192.168.121.4/32" );
//        addrBlock.matches( InetAddress.getByName( "192.168.121.4" ) );
//        addrBlock.matches( InetAddress.getByName( "192.168.121.5" ) );
//
//        addrBlock = InetAddressBlock.valueOf( "192.168.121.4/24" );
//        addrBlock.matches( InetAddress.getByName( "192.168.121.4" ) );
//        addrBlock.matches( InetAddress.getByName( "192.168.121.5" ) );
//        addrBlock.matches( InetAddress.getByName( "2621:0:2d0:200::7" ) );
//    }
}

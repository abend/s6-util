package com.samsix.util.bits;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *    Wraps an AtomicInteger with operations to set and unset bit flags.
 *    This allows the use a set of bitflags without using synchronization.
 *
 *    Prevents the necessity of synchronizing over a set of booleans.
 */
public class AtomicBitflags
{
    private final AtomicInteger    _flags = new AtomicInteger();

    /**
     *    Atomically add the given flags to the current set
     *
     *    @param flags
     *    @return the previous value
     */
    public int set( final int    flags )
    {
        for (;;)
        {
            int    current = _flags.get();
            int    newValue = current | flags;

            if ( _flags.compareAndSet( current, newValue ) )
            {
                return current;
            }
        }
    }


    /**
     *    Atomically remove the given flags from the current set
     *
     *    @param flags
     *    @return the previous value
     */
    public int unset( final int    flags )
    {
        for (;;)
        {
            int    current = _flags.get();
            int    newValue = current & ~flags;

            if ( _flags.compareAndSet( current, newValue ) )
            {
                return current;
            }
        }
    }


    /**
     *    Atomically add and remove the given flags from the current set
     *
     *    @param add the flags to add
     *    @param remove the flags to remove
     *    @return the previous value
     */
    public int change( final int    add,
                       final int    remove )
    {
        for (;;)
        {
            int    current = _flags.get();
            int    newValue = ( current | add ) & ~remove;

            if ( _flags.compareAndSet( current, newValue ) )
            {
                return current;
            }
        }
    }


    /**
     *    Removes all flags
     *    @return the previous value
     */
    public int clear()
    {
        return _flags.getAndSet( 0 );
    }


    /**
     *    Tests if this set contains all of the given flags
     *
     *    @param flags
     *    @return
     */
    public boolean containsAll( final int    flags )
    {
        return containsAll( _flags.get(), flags );
    }


    public static boolean containsAll( final int    value,
                                       final int    has )
    {
        return ( value & has ) == has;
    }


    /**
     *    Atomically checks to see if the flags contains all of 'has' but does not contain 'not'
     *
     *    @param has
     *    @param not
     *    @return
     */
    public boolean containsAllButNot( final int    has,
                                      final int    not )
    {
        return containsAllButNot( _flags.get(), has, not );
    }


    public static boolean containsAllButNot( final int    value,
                                             final int    has,
                                             final int    not )
    {
        return ( value & ( has | not ) ) == has;
    }


    /**
     *    Atomically checks to see if the flags contains any of 'has' but does not contain 'not'
     *
     *    @param has
     *    @param not
     *    @return
     */
    public boolean containsAnyButNot( final int    has,
                                      final int    not )
    {
        return containsAnyButNot( _flags.get(), has, not );
    }


    public static boolean containsAnyButNot( final int    value,
                                             final int    has,
                                             final int    not )
    {
        return ( ( value & has ) != 0 ) && ( ( value & not ) == 0 );
    }



    /**
     *    Tests if this set contains any of the given flags
     *
     *    @param flags
     *    @return
     */
    public boolean containsAny( final int    flags )
    {
        return containsAny( _flags.get(), flags );
    }


    public static boolean containsAny( final int    value,
                                       final int    has )
    {
        return ( value & has ) != 0;
    }


    /**
     *    @return the underlying flags
     */
    public int get()
    {
        return _flags.get();
    }
}

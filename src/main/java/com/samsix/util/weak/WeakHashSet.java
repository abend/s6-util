/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.weak;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *   Acts just like a HashSet, but keys are weakly referenced.
 *
 *   This implementation does NOT support null values, and will throw
 *   exceptions if you try to use them. HOWEVER, it may return null vaules
 *   during an iteration, so you must check for nulls.
 *
 *   @param <E>
 */
public class WeakHashSet<E>
    extends
        AbstractSet<E>
{
    private final ReferenceQueue<E>      _queue = new ReferenceQueue<E>();
    private final Set<WeakElement<E>>    _set   = new HashSet<WeakElement<E>>();


    @Override
    public Iterator<E> iterator()
    {
        //
        //    Remove gc'd elements. Doesn't guarantee that they won't be
        //    gc'd in this loop, though.
        //
        purge();

        final Iterator<WeakElement<E>>    delegate = _set.iterator();

        return new Iterator<E>() {

            @Override
            public boolean hasNext()
            {
                return delegate.hasNext();
            }


            @Override
            public E next()
            {
                return delegate.next().get();
            }


            @Override
            public void remove()
            {
                delegate.remove();
            }
        };
    }


    @Override
    public int size()
    {
        purge();

        return _set.size();
    }


    @Override
    public boolean add( final E e )
    {
        purge();

        return _set.add( new WeakElement<E>( e, _queue ) );
    }


    @Override
    public void clear()
    {
        _set.clear();
    }


    @Override
    public boolean contains( final Object    o )
    {
        return _set.contains( new WeakElement<Object>( o ) );
    }


    @Override
    public boolean remove( final Object    o )
    {
        boolean    removed = _set.remove( new WeakElement<Object>( o ) );

        purge();

        return removed;
    }


    /**
     *    Removes all garbage-collected elements from this set
     */
    public void purge()
    {
        WeakElement<?>    element;

        while( ( element = (WeakElement<?>) _queue.poll() ) != null )
        {
            _set.remove( element );
        }
    }
}


class WeakElement<E>
    extends
        WeakReference<E>
{
    /**
     *    We have to store the hash code of the element because it might
     *    no longer be accessible when we need it.
     */
    private final int    _hashCode;


    public WeakElement( final E                            referent,
                        final ReferenceQueue<? super E>    q )
    {
        super( referent, q );

        _hashCode = referent.hashCode();
    }


    public WeakElement( final E    referent )
    {
        super( referent );

        _hashCode = referent.hashCode();
    }


    @Override
    public int hashCode()
    {
        return _hashCode;
    }


    @Override
    public boolean equals( final Object    o )
    {
        if( o == this )
        {
            return true;
        }

        if( ! ( o instanceof WeakElement<?> ) )
        {
            return false;
        }

        Object    them = ( (WeakElement<?>) o ).get();
        Object    us   = get();

        if( us == null || them == null )
        {
            return false;
        }

        return us.equals( them );
    }
}

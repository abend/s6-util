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
 ***************************************************************************
 *
 * Copyright (c) 2001-2009 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.weak;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;


/**
 * Sometimes we want to make sure that property change managers don't hold on to
 * the references to a property change listener, making them not GC-able. This
 * class holds a weak reference to a PropertyChangeListener and removes it if
 * it's not there.
 */
public class WeakPropertyChangeListener implements PropertyChangeListener
{
    private final WeakReference<PropertyChangeListener>    _delegate;
    private final PropertyChangeSupport                    _source;
    private final String                                   _propertyName;



    public WeakPropertyChangeListener( final PropertyChangeListener    listener,
                                       final PropertyChangeSupport     source )
    {
        this( listener, source, null );
    }



    public WeakPropertyChangeListener( final PropertyChangeListener    listener,
                                       final PropertyChangeSupport     source,
                                       final String                    propertyName )
    {
        _delegate     = new WeakReference<PropertyChangeListener>( listener );
        _source       = source;
        _propertyName = propertyName;
    }



    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange( final PropertyChangeEvent evt )
    {
        PropertyChangeListener listener = _delegate.get();

        if ( listener == null )
        {
            //
            //    The listener has been gc'd!
            //
            remove();

            return;
        }

        listener.propertyChange( evt );
    }
    
    
    public void remove()
    {
        if ( _propertyName == null )
        {
            _source.removePropertyChangeListener( this );
        }
        else
        {
            _source.removePropertyChangeListener( _propertyName, this );
        }        
    }
}

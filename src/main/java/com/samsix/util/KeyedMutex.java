package com.samsix.util;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Lets you do things line synchronized(stringMutex.get("moo")) { ... } so you can synchronize on a word.
 * Useful on any *IMMUTABLE* object that overrides equals and hashcode.
 */
public class KeyedMutex<T> {
    private final WeakHashMap<Mutex,WeakReference<Mutex>> mutexes = new WeakHashMap<Mutex,WeakReference<Mutex>>();

    public static interface Mutex {
        // nothing available on this interface; it's to be used in synchronized() blocks
    }

    public Mutex get(final T name) {
        if(name == null) {
            throw new NullPointerException();
        }

        Mutex newMutex = new MutexContainer<T>(name);

        synchronized(mutexes) {
            WeakReference<Mutex> mutexRef = mutexes.get(newMutex);
            Mutex mutex;
            if(mutexRef == null || (mutex = mutexRef.get()) == null) {
                mutexes.put(newMutex, new WeakReference<Mutex>(newMutex));
                return newMutex;
            }
            return mutex;
        }
    }


    static class MutexContainer<T> implements Mutex {
        private final T name;

        public MutexContainer(final T name) {
            this.name = name;
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj == this) {
                return true;
            }

            if(obj instanceof MutexContainer) {
                return name.equals(((MutexContainer<?>) obj).name);
            }
            else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}

package com.samsix.util.string;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Decorates a map such that all VALUES (not keys) are Strings.
 * Does not support any put operations.
 * containsValue() is not efficient, O(n)
 * entrySet() returned Map.Entry does not properly implement equals/hashCode
 * @param <K>
 */
public class StringValueMapDecorator<K> implements Map<K,String>
{
    private final Map<K,?> delegate;

    public StringValueMapDecorator(final Map<K,?> delegate) {
        this.delegate = delegate;
    }


    private final static String val(final Object o) {
        return o==null?null:String.valueOf(o);
    }


    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return values().contains(value);
    }

    @Override
    public String get(final Object key) {
        return val(delegate.get(key));
    }

    @Override
    public String remove(final Object key) {
        return val(delegate.remove(key));
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<String> values() {
        return new AbstractCollection<String>() {

            @Override
            public Iterator<String> iterator() {
                final Iterator<?> delegateIterator = delegate.values().iterator();

                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return delegateIterator.hasNext();
                    }

                    @Override
                    public String next() {
                        return val(delegateIterator.next());
                    }

                    @Override
                    public void remove() {
                        delegateIterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return delegate.size();
            }
        };
    }

    @Override
    public Set<Entry<K,String>> entrySet() {
        return new AbstractSet<Entry<K,String>>() {

            @Override
            public Iterator<Entry<K,String>> iterator() {
                final Iterator<?> delegateIterator = delegate.entrySet().iterator();

                return new Iterator<Entry<K,String>>() {

                    @Override
                    public boolean hasNext() {
                        return delegateIterator.hasNext();
                    }

                    @Override
                    public Entry<K,String> next() {
                        @SuppressWarnings("unchecked")
                        final Entry<K,?> entry = (Entry<K,?>) delegateIterator.next();
                        return new Entry<K,String>() {

                            @Override
                            public K getKey() {
                                return entry.getKey();
                            }

                            @Override
                            public String getValue() {
                                return val(entry.getValue());
                            }

                            @Override
                            public String setValue(final String value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        delegateIterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return delegate.size();
            }

        };
    }


    @Override
    public String put(final K key, final String value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void putAll(final Map<? extends K,? extends String> m) {
        throw new UnsupportedOperationException();
    }
}

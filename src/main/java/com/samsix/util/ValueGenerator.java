package com.samsix.util;

/**
 * Simple interface for generating values such as attribute defaults.
 *
 * @param <T>
 */
public interface ValueGenerator<T>
{
    public T generate();
}

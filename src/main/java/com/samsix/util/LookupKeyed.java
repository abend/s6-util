package com.samsix.util;

/**
 * Intended so that exceptions can have a machine-readable lookup key
 */
public interface LookupKeyed
{
    public String getLookupKeyType();
    public String getLookupKey();
}

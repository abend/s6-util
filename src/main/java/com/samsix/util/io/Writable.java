package com.samsix.util.io;

import java.io.IOException;
import java.io.Writer;

// Mirrors groovy.lang.Writable so classes can implement both with the same signature;
// Also allows us to not have a dependence on Groovy if we don't need to.
public interface Writable
{
    public Writer writeTo( Writer writer ) throws IOException;
}

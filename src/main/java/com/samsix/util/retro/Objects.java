package com.samsix.util.retro;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This class is a placeholder for Java 7 compatibility.
 * It replaces the java.util.Objects class, which is only available in Java 7+
 * All methods in here should be binary compatible with signatures in that class.
 */
public final class Objects
{
    private Objects() {}

    public static boolean equals(final Object a, final Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static int hashCode(final Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static int hash(final Object... values) {
        return Arrays.hashCode(values);
    }

    public static String toString(final Object o) {
        return String.valueOf(o);
    }

    public static String toString(final Object o, final String nullDefault) {
        return (o != null) ? o.toString() : nullDefault;
    }

    public static <T> int compare(final T a, final T b, final Comparator<? super T> c) {
        return (a == b) ? 0 :  c.compare(a, b);
    }
}
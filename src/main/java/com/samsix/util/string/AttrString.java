/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2012 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
/*
 ***************************************************************************
 *
 * Copyright (c) 2001-2009 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.string;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.samsix.util.retro.Objects;



/**
 *    This class provides tools for manipulating strings that have
 *    variables in them.
 *    <p>
 *    The premise of the this tool is that given a parameterised
 *    string of say the form:
 *    <p>
 *    <pre>
 *
 *          String      str     = "State ${state} has a population of ${pop}";
 *
 *          AttrString  attrStr = new AttrString( str );
 *
 *    </pre>
 *    <p>
 *    we can use a variety of add() methods to build up an internal
 *    <code>Map</code> of key/value pairs.
 *    <p>
 *    We then run through the string, looking for candidate variables
 *    and for any we find, we look up that variable as a key in our
 *    <code>Map</code>.  If we find the key, we replace the
 *    'variable' in the string with the <code>Map</code>
 *    'value'.
 *    <p>
 *    The string will be searched for a variable 'signature';
 *    "${variable}"
 *    <p>
 *    That is, the variable substitution delimiters are <b>${</b> and
 *    <b>}</b>.
 *    <p>
 *    Here are two examples:
 *    <p>
 *    This first one uses a few ways of adding key/values to our
 *    <code>AttrString</code> dictionary and also demonstrates we can
 *    change our mind as we add key/values, before we finally do the
 *    substitution using the computeString() method.
 *    <p>
 *    <pre>
 *
 *          String      str     = "State ${state} has a population of ${pop}";
 *
 *          AttrString  attrStr = new AttrString( str );
 *
 *          Map  map    = new HashMap();
 *
 *          map.put( "state", "CA" );
 *          map.put( "pop", new Integer( 39000000 ) );
 *
 *          //
 *          //    Nice!
 *          //
 *          attrStr.add( "state", "AK" )
 *                 .add( "pop", new Integer( 3 ) )
 *                 .add( map )
 *                 .add( "state", "NY" )
 *                 .add( "pop", new Integer( 17000000 ) );
 *
 *          //
 *          //    Lets fire it up.
 *          //
 *          attrStr.computeString();
 *
 *    </pre>
 *    <p>
 *    This second example uses an array of <code>Object</code>.
 *    <p>
 *    <pre>
 *
 *          //
 *          //    This one deals with a hard-coded array of *things*
 *          //
 *          String      str     = "${0} "
 *                                + "was "
 *                                + "${1} "
 *                                + "years old, but still knew "
 *                                + "the square root of 2 was roughly "
 *                                + "${2}";
 *
 *          //
 *          //    An arbitrary array of objects.
 *          //
 *          Object[]      values =
 *          {
 *              "Ginny",
 *              new Integer( 55 ),
 *              new Double( 1.414 )
 *          };
 *
 *          AttrString  attrStr = new AttrString( str );
 *
 *          attrStr.add( values );
 *
 *          attrStr.computeString();
 *
 *    </pre>
 *    <p>
 *    At every iteration of the loop any variables are replaced
 *    immediately, so at any point we quit the loop, we know that is
 *    the return string.
 *    <p>
 *    If it doesn't find the variable, it just leaves the string as it
 *    was.
 *    <p>
 */
public final class AttrString
    implements
        Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<String, Object>    _variables = new HashMap<String, Object>();


    /**
     *    Create an AttrString object by supplying a String.
     *    <p>
     *    We supply a String, but convert to a StringBuffer so we can
     *    manipulate the string.
     *    <p>
     *    @param attrString   our initial parameterised string.
     */
    public AttrString()
    {
        //    Do nothing
    }


    /**
     *    @param varKey   the variable key we want to add to our Map
     *    @param value    an object (value) we want to add.
     */
    public AttrString add( final String    varKey,
                           final Object    value )
    {
        _variables.put( varKey, value );

        return this;
    }


    /**
     *    Add the values in the map.
     *    <p>
     *    @param map
     */
    public AttrString add( final Map<String, ? extends Object>    map )
    {
        if ( map != null )
        {
            _variables.putAll( map );
        }

        return this;
    }


    /**
     *    Return a variable substituted string.
     */
    public String computeString( final String    attrString )
    {
        return StringUtilities.computeAttrString( attrString, _variables, false, null );
    }


    /**
     *    Return a variable substituted string,
     *    but if a value hasn't been specified,
     *    then leave take out the ${variable} part
     *    and leave it blank/empty/null.
     */
    public String computeString( final String     attrString,
                                 final boolean    blankOutNullValues )
    {
        return computeString( attrString, blankOutNullValues, null );
    }


    public String computeString( final String                attrString,
                                 final boolean               blankOutNullValues,
                                 final Collection<String>    blankOutNullKeyExceptions )
    {
        return StringUtilities.computeAttrString( attrString,
                                                  _variables,
                                                  blankOutNullValues,
                                                  blankOutNullKeyExceptions );
    }


    //========================================
    //
    //    static methods for built-in functions
    //
    //========================================

    public static String substring( final String    str,
                                    final String    beginIndex )
    {
        return str.substring( Integer.valueOf( beginIndex ).intValue() );
    }


    public static String substring( final String    str,
                                    final String    beginIndex,
                                    final String    endIndex )
    {
        if ( StringUtils.isBlank( str ) )
        {
            return "";
        }

        int    begin = Integer.valueOf( beginIndex ).intValue();

        //
        //    The plus one fixes in my opinion a weirdness in the java
        //    substring in which the end index passed in is actually
        //    one greater than the index used of the substring.
        //    weird.
        //
        int    end = Integer.valueOf( endIndex ).intValue() + 1;

        if ( begin < 0 )
        {
            begin = 0;
        }

        if ( end > str.length() )
        {
            end = str.length();
        }

        if ( begin > end )
        {
            begin = end;
        }

        return str.substring( begin, end );
    }


    public static String cr()
    {
        return "\n";
    }


    public static String join( final String    str1,
                               final String    str2,
                               final String    delim )
    {
        if ( StringUtils.isEmpty( str1 ) )
        {
            return str2;
        }

        if ( StringUtils.isEmpty( str2 ) )
        {
            return str1;
        }

        return str1 + delim + str2;
    }


    public static String toUpperCase( final String   str )
    {
        if( StringUtils.isBlank( str ) )
        {
            return "";
        }

        return str.toUpperCase();
    }


    public static String addSuffix( final String    value,
                                    final String    suffix )
    {
        if( StringUtils.isBlank( value ) )
        {
            return "";
        }

        return value + suffix;
    }


    public static String doubleToString( final String    number )
    {
        return doubleToString( number, null );
    }


    public static String doubleToString( final String    number,
                                         final String    suffix )
    {
        if( StringUtils.isBlank( number ) )
        {
            return "";
        }

        String    result;
        result = StringUtilities.doubleToString( Double.parseDouble( number ) );

        if( suffix != null )
        {
            result += suffix;
        }

        return result;
    }


    public static String roundToWhole( final String    strNumber )
    {
        double    number;

        try
        {
            number = Double.parseDouble( strNumber );
        }
        catch( Throwable    ex )
        {
            return "<invalid>";
        }

        return String.valueOf( (int) Math.round( number ) );
    }


    /**
     *    Used for rounding a number to the nearest larger number
     *    (ie, when nearest > 1).
     *
     *    @param strNumber
     *    @param strNearest
     *    @return
     */
    public static String roundToWhole( final String    strNumber,
                                       final String    strNearest )
    {
        double    number;
        double    nearest;

        try
        {
            number = Double.parseDouble( strNumber );
            nearest = Double.parseDouble( strNearest );
        }
        catch( Throwable    ex )
        {
            return "<invalid>";
        }

        double    value;
        value = Math.round( number / nearest ) * nearest;

        return String.valueOf( (int) value );
    }


    public static String formatFraction( final String    number )
    {
        return formatFraction( number, null );
    }


    public static String formatFraction( final String    number,
                                         final String    suffix )
    {
        if( StringUtils.isBlank( number ) )
        {
            return "";
        }

        String    result;
        result = StringUtilities.formatFraction( Double.parseDouble( number ) );

        if( suffix != null )
        {
            result += suffix;
        }

        return result;
    }


    public static String coalesce( final String    str1,
                                   final String    str2 )
    {
        return StringUtils.isBlank( str1 ) ? str2 : str1;
    }


    public static String coalesce( final String    str1,
                                   final String    str2,
                                   final String    str3 )
    {
        String    coalesced = coalesce( str1, str2 );

        return StringUtils.isBlank( coalesced ) ? str3 : coalesced;
    }


    public static String coalesce( final String    str1,
                                   final String    str2,
                                   final String    str3,
                                   final String    str4 )
    {
        String    coalesced = coalesce( str1, str2, str3 );

        return StringUtils.isBlank( coalesced ) ? str4 : coalesced;
    }

    //========================================
    //
    //    END OF static methods for built-in functions
    //
    //========================================


    @Override
    public String toString()
    {
        return _variables.toString();
    }


    @Override
    public int hashCode()
    {
        return _variables.hashCode();
    }


    /**
     *    An AttrString is defined by its _variables map. Therefore, it can
     *    be equal to both another AttrString, or just a raw map.
     */
    @Override
    public boolean equals( final Object    o )
    {
        if( o instanceof AttrString )
        {
            return _variables.equals( ( (AttrString) o )._variables );
        }

        if( o instanceof Map<?,?> )
        {
            return _variables.equals( o );
        }

        return false;
    }


    /**
     *    returns true if all given attrs are found with the same value in
     *    our internal variables
     */
    public boolean containsAll( final Map<String, ?>    attrs )
    {
        for( Map.Entry<String, ?>    entry : attrs.entrySet() )
        {
            Object    value = _variables.get( entry.getKey() );

            //
            //    Can store nulls in a map.... ugh.
            //
            if( value == null && ! _variables.containsKey( entry.getKey() ) )
            {
                return false;
            }

            if( ! Objects.equals( value, entry.getValue() ) )
            {
                return false;
            }
        }

        return true;
    }


    public AttrString copy()
    {
        AttrString    copy = new AttrString();
        copy.add( _variables );

        return copy;
    }
}

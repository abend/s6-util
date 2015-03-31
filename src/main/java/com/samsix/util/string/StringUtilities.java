/**
 ***************************************************************************
 *
 * Copyright (c) 2001-2013 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.string;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;

import com.samsix.util.UnexpectedCheckedException;
import com.samsix.util.UtilException;
import com.samsix.util.reflect.SimpleMethodInvoker;


/**
 *      A set of String manipulation utilities <b>not</b> in the String class.
 *      <p>
 *      Be <b>very careful/good</b> when adding to this class.
 *      <p>
 *      Think about what you are doing.
 *      <p>
 *      Are you presenting a good, consistent API?
 */
public final class StringUtilities
{
    //
    //    Do not create a static logger here
    //

    public static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";

    private static final String     VARIABLE_START_DELIM  = "${";
    private static final String     VARIABLE_END_DELIM    = "}";
    private static final String     FUNCTION_START_DELIM  = "$[";
    private static final String     FUNCTION_END_DELIM    = "]";
    public  static final String     PARAM_DELIM           = "||";
    private static final String     ESC_SEQ               = "$$";

    public final static String      SPACE                 = " ";
    public final static String      COMMA_DELIMITER       = ",";
    public final static String      COMMA_SPACE_DELIMITER = COMMA_DELIMITER + SPACE;

    private static NumberFormat     _userNumberFormat     = null;

    private static ToStringStyle    _userToStringStyle    = null;

    private static FastDateFormat _displayDateFormatter = null;
    private static FastDateFormat _dbDateFormatter      = null;
    private static FastDateFormat _dbDateFormatterNoTZ  = null;

    private static final char[]     HEX_CHARS             = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f' };


    /**
     * Empty (private!) constructor
     */
    private StringUtilities()
    {
        // To make it non-instantiable
    }


    public static String userFormat( final double value )
    {
        return getUserNumberFormat().format( value );
    }


    private static NumberFormat getUserNumberFormat()
    {
        if ( _userNumberFormat != null )
        {
            return _userNumberFormat;
        }

        _userNumberFormat = NumberFormat.getInstance();
        _userNumberFormat.setMinimumFractionDigits( 1 );
        _userNumberFormat.setMaximumFractionDigits( 1 );
        _userNumberFormat.setGroupingUsed( false );

        return _userNumberFormat;
    }


    /**
     * Counts the number of instances of characters in a string.
     * <p>
     *
     * @param string
     *            String we are checking.
     * @param ch
     *            Character we are looking for.
     *            <p>
     * @return The number of occurances found.
     *         <p>
     * @throws UtilException
     */
    public static int countInstancesOf( final String string, final int ch )
    {
        int index = -1;
        int count = 0;

        while ( ( index = string.indexOf( ch, ++index ) ) > -1 )
        {
            ++count;
        }

        return count;
    }


    /**
     * Check if the string is null or whether it is empty; that is if it
     * equals("").
     * <p>
     *
     * @param string
     *            String we are checking.
     *            <p>
     * @return true if this is a null or empty string.
     */
    public static boolean isNullOrEmpty( final String string )
    {
        return StringUtils.isEmpty( string );
    }


    /**
     * Check if the string is null, empty or blank. (in that order)
     * <p>
     *
     * @param string
     *            String we are checking.
     *            <p>
     * @return true if this string is null, empty, or blank.
     */
    public static boolean isNullOrEmptyOrBlank( final String string )
    {
        return StringUtils.isBlank( string );
    }



    /**
     * Replaces all occurences of <tt>find</tt> with <tt>replacement</tt> in the
     * source String (original).
     * <p>
     * This implementation is copied from the python implementation, fairly
     * literally. It is, unfortunately, sloooooow. It needs to be rewritten.
     * <p>
     *
     * @param original
     *            the original string buffer to modify.
     * @param find
     *            the string to be replaced.
     * @param replacement
     *            the replacement string for <tt>find</tt> matches.
     *            <p>
     * @return a String will all instances of <tt>find</tt> replaced by
     *         <tt>replacement</tt>.
     *         <p>
     */
    public static String replace( final String original,
                                  final String find,
                                  final String replacement )
    {
        return collectionToString( split( original, find ), replacement );
    }


    /**
     * Indent our string a certain number of spaces. This will put white space
     * on the left hand side.
     * <p>
     *
     * @param string
     *            String we are indenting.
     * @param count
     *            The number of spaces to insert.
     *            <p>
     *            The number supplied must be >= 0.
     *            <p>
     * @return a string with spaces prepended.
     */
    public static String indent( final String string, final int count )
    {
        //
        // To hold the spaces we create and the original string
        // passed in.
        //
        StringBuffer buffer = new StringBuffer( ( 2 * string.length() ) + 1 );

        //
        // Build up the buffer of spaces.
        //
        buffer.append( repeat( " ", count ) );

        //
        // Now tack on our original string.
        //
        buffer.append( string );

        return buffer.toString();
    }


    /**
     *
     * @return  The result of A<delim>B unless A is null, then return B.  If B is null then return A.
     */
    public static String concatenate( final String    a,
                                      final String    b,
                                      final String    delim )
    {
        if ( StringUtils.isEmpty( a ) )
        {
            return b;
        }

        if ( StringUtils.isEmpty( b ) )
        {
            return a;
        }

        return a + delim + b;
    }



    /**
     * Given a string and a count, produce a string full of count instances of
     * string.
     * <p>
     *
     * @param string
     *            String we are duplicating.
     * @param count
     *            The number of time to append it.
     *            <p>
     *            The number supplied must be >= 0.
     *            <p>
     * @return a string repeated 'count' times.
     */
    public static String repeat( final String string, final int count )
    {
        //
        // To hold the string we create.
        //
        StringBuffer buffer = new StringBuffer( ( 2 * string.length() ) + 1 );

        //
        // Build up the buffer of 'string X count'.
        //
        for ( int ii = 0; ii < count; ++ii )
        {
            buffer.append( string );
        }

        return buffer.toString();
    }



    /**
     * Create a string array from a string separated by <tt>delim</tt>.
     * <p>
     * The split command needs to deal with the following accurately:
     * <p>
     * Suppose that you are a programmer working with a database.
     * <p>
     * Lets say we output files from the database in this format:
     * <p>
     *
     * <pre>
     *
     *    ||stl|north|10 Moon St.|Culver City|CA||||red
     *
     * </pre>
     *
     * Now, lets say we want to delete the second column and make sure what was
     * the third column is now all caps.
     * <p>
     * Here is the output from the Python split:
     * <p>
     *
     * <pre>
     *    sa&gt; python
     *    Python 1.5.2 (#1, Mar  3 2001, 01:35:43) \
     *                 [GCC 2.96 20000731 (Red Hat Linux 7.1 2 on linux-i386
     *    Copyright 1991-1995 Stichting Mathematisch Centrum, Amsterdam
     *    &gt;&gt;&gt; import string
     *    &gt;&gt;&gt; foo = &quot;andy the aardvark&quot;
     *    &gt;&gt;&gt; string.split( foo, &quot;a&quot; )
     *    ['', 'ndy the ', '', 'rdv', 'rk']
     *    &gt;&gt;&gt; foo = &quot;||stl|north|10 Moon St.|Culver City|CA||||red|&quot;
     *    &gt;&gt;&gt; string.split( foo, &quot;|&quot; )
     *    ['', '', 'stl', 'north', '10 Moon St.', 'Culver City', 'CA', '', '', '', 'red', '']
     * </pre>
     * <p>
     * So, out split, needs to deal with that:
     * <p>
     *
     * @param src
     *            a <code>String</code> value
     * @param delim
     *            the delimiter to split by.
     *            <p>
     * @param trim
     *            a <code>boolean</code> value
     * @return a string array of the split fields. Return an empty array if this
     *         string is null.
     *         <p>
     */
    public static List<String> split( final String     src,
                                      final String     delim,
                                      final boolean    trim )
    {
        if ( delim == null )
        {
            throw new IllegalArgumentException( "delim cannot be null" );
        }

        //
        // Return an empty array if the string is null
        //
        if ( StringUtils.isBlank( src ) )
        {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<String>();

        //
        // Need to allow splitting strings by spaces
        // so we don't want to check for Blank strings
        // as well.
        //
        if ( delim.isEmpty() )
        {
            list.add( src );
            return list;
        }

        int delimLength = delim.length();
        int index = 0;
        int start = 0;
        String piece;

        while ( index < src.length() )
        {
            index = src.indexOf( delim, index );

            if ( index == -1 )
            {
                break;
            }

            //
            // Add the substring to the list.
            //
            piece = src.substring( start, index );

            if ( trim )
            {
                list.add( piece.trim() );
            }
            else
            {
                list.add( piece );
            }

            //
            // Skip further along the buffer, past what we
            // replaced, so we don't end up possibly in an infinite
            // loop.
            //
            start = index + delimLength;

            index += delimLength;
        }

        //
        // Make sure the end of the string gets added.
        //
        if ( trim )
        {
            list.add( src.substring( start ).trim() );
        }
        else
        {
            list.add( src.substring( start ) );
        }

        //
        // Output a list, given our list contents.
        //
        return list;
    }



    /**
     * Describe <code>split</code> method here.
     *
     * @param src
     *            a <code>String</code> value
     * @param delim
     *            a <code>String</code> value
     * @return a <code>List</code> value
     */
    public static List<String> split( final String    src,
                                      final String    delim )
    {
        return split( src, delim, false );
    }



    /**
     * Different from split in that it can handle several single character
     * delimiters.
     * <p>
     *
     * @param source
     *            a <code>String</code> to be turned into a list.
     * @param delim
     *            the potential delimiters of the tokens.
     * @param returnDelims
     *            if true, the delimiters are returned in the list.
     */
    public static List<String> toList( final String source,
                                       final String delim,
                                       final boolean returnDelims )
    {
        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer( source, delim,
                returnDelims );

        while ( tokenizer.hasMoreTokens() )
        {
            result.add( tokenizer.nextToken().trim() );
        }

        return result;
    }



    /**
     * Different from split in that it can handle several single character
     * delimiters.
     * <p>
     *
     * @param source
     *            a <code>String</code> to be turned into a list.
     * @param delim
     *            the potential delimiters of the tokens.
     * @param returnDelims
     *            if true, the delimiters are returned in the list.
     */
    public static String[] toArray( final String source,
                                    final String delim,
                                    final boolean returnDelims )
    {
        List<String> tmp = toList( source, delim, returnDelims );
        String[] result = new String[tmp.size()];

        int index = 0;
        for ( String str : tmp )
        {
            result[index++] = str;
        }

        return result;
    }



    /**
     * Tests to see if a string contains more than one line.
     * <p>
     * This relies on a '\n' defining a 'line'.
     * <p>
     *
     * @param string
     *            String we are checking.
     *            <p>
     * @return true if this string crosses multiple lines
     *         <p>
     */
    public static boolean isMultiLine( final String string )
    {
        //
        // This relies on a '\n' defining a 'line'.
        //
        if ( lines( string ) > 1 )
        {
            return true;
        }

        return false;
    }



    /**
     * Counts the number of lines in a 'string'.
     * <p>
     * An empty string ("") has no lines in it, by our definition.
     * <p>
     *
     * @param string
     *            String we are checking.
     *            <p>
     * @return The number of lines found.
     *         <p>
     * @throws UtilException
     */
    public static int lines( final String string )
    {
        //
        // Need to specifically return 0 here for empty string,
        // rather than let countInstancesOf return 0, since we have
        // to add one to the result.
        //
        if ( StringUtils.isEmpty( string ) )
        {
            return 0;
        }

        return countInstancesOf( string, '\n' ) + 1;
    }



    /**
     * Takes the first letter and capitalizes it.
     * <p>
     *
     * @param string
     *            String we are capitalizing
     *            <p>
     * @return Capitalized string
     */
    public static String toCapCase( final String string )
    {
        if ( string == null )
        {
            return null;
        }

        if( string.length() == 1 )
        {
            return string.toUpperCase();
        }

        return Character.toUpperCase( string.charAt( 0 ) ) + string.substring( 1 ).toLowerCase();
    }


    /**
     * Appends a string to a buffer, prepending with a delimiter string. For
     * instance, this may be used to create a string of comma-delimited values.
     * This does not ignore empty strings.
     * <p>
     *
     * @param buffer
     *            Our initial buffer string.
     * @param string
     *            String we are adding to the buffer
     * @param delimiter
     *            Delimiter that gets appended to the buffer before the string
     *            to be appended
     */
    public static void appendToBuffer( final StringBuffer    buffer,
                                       final String          string,
                                       final String          delimiter )
    {
        if ( string == null )
        {
            return;
        }

        //
        // Only append the delimiter in front if the buffer isn't empty.
        //
        if ( buffer.length() == 0 || delimiter == null )
        {
            buffer.append( string );
        }
        else
        {
            buffer.append( delimiter ).append( string );
        }
    }


    public static void appendToBuilder( final StringBuilder    builder,
                                        final String           string,
                                        final String           delimiter )
    {
        if ( string == null )
        {
            return;
        }

        //
        // Only append the delimiter in front if the buffer isn't empty.
        //
        if ( builder.length() == 0 || delimiter == null )
        {
            builder.append( string );
        }
        else
        {
            builder.append( delimiter ).append( string );
        }
    }


    /**
     * Given a list, output the list with a comma and space between the values.
     */
    public static String collectionToString( final Collection<?>    list )
    {
        return collectionToString( list, ", " );
    }



    /**
     * Given a list, output the list with a comma and space between the values.
     */
    public static <T> String collectionToString( final Collection<T>         list,
                                                 final ToStringWrapper<T>    wrapper )
    {
        return collectionToString( list, ", ", wrapper );
    }



    /**
     * Given a list, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param list
     *            List we are outputting.
     * @param delimiter
     *            Delimiter that separates the list entries.
     *            <p>
     * @return The string representing the list contents..
     *         <p>
     */
    public static <T> String collectionToString( final Collection<T>    list,
                                                 final String           delimiter )
    {
        return collectionToString( list, delimiter, null );
    }



    /**
     * Given a list, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param list
     *            List we are outputting.
     * @param delimiter
     *            Delimiter that separates the list entries.
     *            <p>
     * @return The string representing the list contents..
     *         <p>
     */
    public static <T> String collectionToString( final Collection<T>         list,
                                                 final String                delimiter,
                                                 final ToStringWrapper<T>    wrapper )
    {
        if ( list == null )
        {
            return "null";
        }

        String    delimit;
        if ( delimiter == null )
        {
            //
            // Treat a null delimiter like an empty string.
            //
            delimit = "";
        }
        else
        {
            delimit = delimiter;
        }

        //
        // First time through our delimiter should be empty.
        //
        String          delim = "";
        StringBuffer    results = new StringBuffer( 64 );
        String          name;

        //
        // For each element in the list, separate it by the
        // specified delimiter.
        //
        for ( T tmp : list )
        {
            if ( tmp == null )
            {
                name = "";
            }
            else if ( wrapper != null )
            {
                name = wrapper.toString( tmp );
            }
            else
            {
                name = tmp.toString();
            }

            results.append( delim ).append( name );

            //
            // Now change the delimiter to what we passed in.
            //
            delim = delimit;
        }

        return results.toString();
    }


    public static void appendToBuffer( final StringBuffer          buffer,
                                       final Collection<String>    values,
                                       final String                delim )
    {
        boolean    isFirst = true;

        for ( String    value : values )
        {
            if ( ! isFirst )
            {
                buffer.append( delim );
            }

            buffer.append( value );

            isFirst = false;
        }
    }


    /**
     * Given an array, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param array
     *            Object array to convert to a string.
     *            <p>
     * @return a string representing the array contents.
     */
    public static String arrayToString( final Object[] array )
    {
        if ( array == null )
        {
            return "null";
        }

        String tmp = new ToStringBuilder( array, ToStringStyle.SIMPLE_STYLE )
                .append( array ).toString();

        return tmp.substring( 1, tmp.length() - 1 );

        // return collectionToString( Arrays.asList( array ) );
    }


    public static String arrayToBriefString( final Object[]    inArray,
                                             final String      delimiter,
                                             final int         maxDisplay )
    {
        if ( inArray == null )
        {
            return "null";
        }

        Object[]    array = inArray;
        int         more = 0;
        if ( inArray.length > maxDisplay )
        {
            array = new Object[ maxDisplay ];
            System.arraycopy( inArray, 0, array, 0, maxDisplay );

            more = inArray.length - maxDisplay;
        }

        StringBuilder    string = new StringBuilder();
        boolean          first = true;

        for( Object    obj : array )
        {
            if ( first )
            {
                first = false;
            }
            else
            {
                string.append( delimiter );
            }

            string.append( obj );
        }

        if ( more > 0 )
        {
            string.append( " ... +" ).append( more );
        }

        return string.toString();
    }

    /**
     * Given an array, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param array
     *            Object array to convert to a string.
     *            <p>
     * @return a string representing the array contents.
     */
    public static String arrayToString( final String[] array, final String delimiter )
    {
        if ( array == null )
        {
            return "null";
        }

        StringBuffer buffer = new StringBuffer();
        String separator = "";

        for ( int ii = 0; ii < array.length; ++ii )
        {
            buffer.append( separator );
            buffer.append( array[ii] );

            separator = delimiter;
        }

        return buffer.toString();
    }



    /**
     * Given an array, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param array
     *            byte array to convert to a string.
     *            <p>
     * @return a string representing the array contents.
     *         <p>
     * @throws UtilException
     */
    public static String arrayToString( final byte[] array )
    {
        if ( array == null )
        {
            return "null";
        }

        String tmp = new ToStringBuilder( array, ToStringStyle.SIMPLE_STYLE )
                .append( array ).toString();

        return tmp.substring( 1, tmp.length() - 1 );
    }



    /**
     * Given an array, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param array
     *            float array to convert to a string.
     *            <p>
     * @return a string representing the array contents.
     *         <p>
     * @throws UtilException
     */
    public static String arrayToString( final float[] array )
    {
        if ( array == null )
        {
            return "null";
        }

        String tmp = new ToStringBuilder( array, ToStringStyle.SIMPLE_STYLE )
                .append( array ).toString();

        return tmp.substring( 1, tmp.length() - 1 );
    }



    /**
     * Given an array, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param array
     *            array of Point2D to convert to a string.
     *            <p>
     * @return a string representing the array contents.
     */
    public static String arrayToString( final Point2D[] array )
    {
        if ( array == null )
        {
            return "null";
        }

        String tmp = new ToStringBuilder( array, ToStringStyle.SIMPLE_STYLE )
                .append( array ).toString();

        return tmp.substring( 1, tmp.length() - 1 );
        // List tmp = new ArrayList( array.length );
        //
        // for ( int ii = 0; ii < array.length; ++ii )
        // {
        // tmp.add( array[ii] );
        // }
        //
        // return collectionToString( tmp );
    }



    /**
     * Given an array, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param array
     *            int array to convert to a string.
     *            <p>
     * @return a string representing the array contents.
     */
    public static String arrayToString( final int[] array )
    {
        if ( array == null )
        {
            return "null";
        }

        String tmp = new ToStringBuilder( array, ToStringStyle.SIMPLE_STYLE )
                .append( array ).toString();

        return tmp.substring( 1, tmp.length() - 1 );
    }



    /**
     * Given an array, output the list with a specified delimiter between the
     * values.
     * <p>
     *
     * @param array
     *            int array to convert to a string.
     *            <p>
     * @return a string representing the array contents.
     *         <p>
     * @throws UtilException
     */
    public static String arrayToString( final long[] array )
    {
        if ( array == null )
        {
            return "null";
        }

        String tmp = new ToStringBuilder( array, ToStringStyle.SIMPLE_STYLE )
                .append( array ).toString();

        return tmp.substring( 1, tmp.length() - 1 );
    }



    /**
     *      Given an array, output the list with a specified delimiter between the
     *      values.
     *      <p>
     *
     *      @param array
     *            double array to convert to a string.
     *            <p>
     *      @return a string representing the array contents.
     *         <p>
     *      @throws UtilException
     */
    public static String arrayToString( final double[] array )
    {
        if ( array == null )
        {
            return "null";
        }

        String tmp = new ToStringBuilder( array, ToStringStyle.SIMPLE_STYLE )
                .append( array ).toString();

        return tmp.substring( 1, tmp.length() - 1 );
    }



    public static <K, V> String mapToString( final Map<K, V>    values )
    {
        return mapToString( values, "\n" );
    }


    public static <K, V> String mapToString( final Map<K, V>    keyValues,
                                             final String       delimiter )
    {
        return mapToString( keyValues, delimiter, false );
    }


    public static <K, V> String mapToString( final Map<K, V>    keyValues,
                                             final String       delimiter,
                                             final boolean      treatSingleValueStringAsString )
    {
        List<String> mapList = new ArrayList<String>( keyValues.size() );

        for ( Entry<K, V> entry : keyValues.entrySet() )
        {
            //
            // Extract the value(s) and add the hidden control(s).
            //
            Object    value = entry.getValue();

            if ( value instanceof String[] )
            {
                String[]    strArray = (String[]) value;

                if( strArray.length == 1 && treatSingleValueStringAsString )
                {
                    value = strArray[ 0 ];
                }
                else
                {
                    value = "{" + arrayToString( strArray ) + "}";
                }
            }

            mapList.add( entry.getKey() + ": " + value );
        }

        return collectionToString( mapList, delimiter );
    }



    public static Map<String, String> stringToMap( final String string,
                                                   final String pairDelimiter,
                                                   final String keyValueDelimiter )
    {
        if ( string == null )
        {
            return new HashMap<String, String>();
        }

        Map<String, String> map = new HashMap<String, String>();

        String[] pairs = string.split( pairDelimiter );
        String[] pair;

        for ( String pair1 : pairs )
        {
            pair = pair1.split( keyValueDelimiter, 2 );

            if ( pair.length > 1 )
            {
                map.put( pair[0], pair[1] );
            }
        }

        return map;
    }



    /**
     *      Converts all single quotes into double single quotes and then wraps the
     *      whole string in single quotes.
     *      <p>
     *      This is used to prepare strings to be sent to a database.
     *
     *      @param aString
     *      @return the sql safe wrapped string.
     */
    public static String wrapQuotes( final String    value )
    {
    	if ( value == null )
    	{
    		return "NULL";
    	}

        return "'" + replace( value, "'", "''" ) + "'";
    }



    public static String wrapQuotes( final Object    value )
    {
        if ( value == null )
        {
            return "NULL";
        }

        //
        //      This will only work for list and set if they are
        //      single valued items (each item is a single value,
        //      a string, int, whatever), and the code reading
        //      these guys back in, or the code getting ready to
        //      display these things converts back from a string
        //      to a list of {string, int, whatever}.
        //
        if ( value instanceof String || value instanceof Date )
        {
            return wrapQuotes( value.toString() );
        }

        if ( ( value instanceof List<?> || value instanceof Set<?> )
                && !( (Collection<?>) value ).isEmpty() )
        {
            String tmp = collectionToString( (Collection<?>) value );

            return wrapQuotes( tmp );
        }

        return value.toString();
    }


    public static StringBuffer buildInClause( final List<String>    values )
    {
        return buildInClause( values, false );
    }


    public static StringBuffer buildInClause( final List<String>    values,
                                              final boolean         toLower )
    {
        StringBuffer    buffer = new StringBuffer();
        int             numPlots = 0;

        buffer.append( "IN ( " );

        for( String    value : values )
        {
            String    val;
            if ( toLower )
            {
                val = value.toLowerCase();
            }
            else
            {
                val = value;
            }


            if( numPlots++ > 0 )
            {
                buffer.append( ", " );
            }

            buffer.append( StringUtilities.wrapQuotes( val ) )
                  .append( ' ' );
        }

        buffer.append( ')' );

        return buffer;
    }


    /**
     *      This provides a compareTo operation for strings that allows for nulls.
     */
    public static int compareIgnoreCase( String first, String second )
    {
        //
        //      Treat null values like empty strings when comparing.
        //
        if ( first == null )
        {
            first = "";
        }

        if ( second == null )
        {
            second = "";
        }

        return first.compareToIgnoreCase( second );
    }



    /**
     *      Converts a string from camel to title case. e.g. "TheTitleOfThisThing"
     *      becomes "The Title Of This Thing" It also handles lower camel case too so
     *      that "theTitleOfThisThing" will give the same result
     *      <p>
     *
     *      @param camel case String
     *      @return title case String
     */
    public static String toTitleCase( final String camel )
    {
        if (camel==null) {
            return null;
        }
        StringBuffer    buffer = new StringBuffer( camel.length() + 10 );
        char            ch;

        //      TBD: needs refactoring, cleanup, method cohesiveness, SRP.
        for ( int ii = 0; ii < camel.length(); ii++ )
        {
            ch = camel.charAt( ii );

            //
            //      This will handle lower camel and avoid adding a
            //          space at the start
            //
            if ( ii == 0 )
            {
                if ( Character.isLowerCase( ch ) )
                {
                    buffer.append( Character.toUpperCase( ch ) );
                }
                else
                {
                    buffer.append( ch );
                }
            }
            else
            {
                if ( Character.isUpperCase( ch ) )
                {
                    buffer.append( " " ).append( ch );
                }
                else
                {
                    buffer.append( ch );
                }
            }
        }

        return buffer.toString();
    }


    /**
     *    Given a string with spaces, turn it into something camel case.
     *    "The Quick Brown Fox" -> "theQuickBrownFox"
     */
    public static String toCamelCase( final String    str )
    {
        if ( str == null )
        {
            return null;
        }

        StringBuffer    buffer = new StringBuffer( str.length() );

        boolean              lastCharWasSpace = false;
        CharacterIterator    iterator;
        iterator = new StringCharacterIterator( str.toLowerCase() );

        for( char c = iterator.current();
             c != CharacterIterator.DONE;
             c = iterator.next() )
        {
            if ( Character.isWhitespace( c ) )
            {
                lastCharWasSpace = true;
                continue;
            }
            else if ( lastCharWasSpace )
            {
                c = Character.toUpperCase( c );
                lastCharWasSpace = false;
            }

            buffer.append( c );
        }

        return buffer.toString();
    }


    public static String stripWhitespace( final String    str )
    {
        if ( str == null )
        {
            return null;
        }

        StringBuffer    buffer = new StringBuffer( str.length() );

        CharacterIterator    iterator;
        iterator = new StringCharacterIterator( str );

        for( char c = iterator.current(); c != CharacterIterator.DONE; c = iterator.next() )
        {
            if ( ! Character.isWhitespace( c ) )
            {
                buffer.append( c );
            }
        }

        return buffer.toString();
    }


    public static String rightJustify( final String subject,
                                       final int length,
                                       final char pad )
    {
        StringBuffer buffer = new StringBuffer( subject );

        while ( buffer.length() < length )
        {
            buffer.insert( 0, pad );
        }

        return buffer.toString();
    }



    public static String leftJustify( final String subject,
                                      final int length,
                                      final char pad )
    {
        StringBuffer buffer = new StringBuffer( subject );

        while ( buffer.length() < length )
        {
            buffer.append( pad );
        }

        return buffer.toString();
    }



    public static List<String> getAlphaNumericChunks( final String    text )
    {
        List<String>    chunks = new ArrayList<String>( 1 );

        if ( isNullOrEmptyOrBlank( text ) )
        {
            return chunks;
        }


        //      TBD: Clean this up, refactor into cohesive methods.
        int        startIndex = 0;
        boolean    isDigit = Character.isDigit( text.charAt( startIndex ) );

        for ( int ii = startIndex + 1; ii < text.length(); ii++ )
        {
            if ( Character.isDigit( text.charAt( ii ) ) != isDigit )
            {
                chunks.add( text.substring( startIndex, ii ) );
                isDigit = ! isDigit;
                startIndex = ii;
            }
        }

        //
        //      Now add the last chunk.
        //
        chunks.add( text.substring( startIndex, text.length() ) );

        return chunks;
    }

    public static String encodeUrl( final String url )
    {
        return encodeUrl( url, true );
    }


    public static String encodeUrl( final String     url,
                                    final boolean    spacesAsPlus )
    {
        String encodedUrl;

        //
        //      Encode spaces.
        //
        if ( spacesAsPlus )
        {
            encodedUrl = replace( url, " ", "+" );
        }
        else
        {
            encodedUrl = replace( url, " ", "%20" );
        }

        //
        //      Now encode linefeeds if any.
        //
        encodedUrl = replace( encodedUrl, "\n", "%0a" );

        //
        //      Replace any quotes.
        //
        encodedUrl = replace( encodedUrl, "\"", "%22" );

        //
        //      Replace pound signs.
        //
        encodedUrl = replace( encodedUrl, "#", "%23" );

        return encodedUrl;
    }



    public static String addUrlParameter( final String    url,
                                          final String    name,
                                          final String    value )
    {
        //
        //      Fist check to see if the parameter already exists. If not,
        //      we want to tack it on to the end of the url.
        //
        if ( url == null || name == null || StringUtils.isBlank( value ) )
        {
            return url;
        }

        StringBuilder    buffer = new StringBuilder( url );

        if ( url.indexOf( "?" ) < 0 )
        {
            buffer.append( "?" );
        }
        else
        {
            buffer.append( "&" );
        }

        buffer.append( name ).append( "=" ).append( value );

        return buffer.toString();
    }



    /**
     *      Method that takes a text string and breaks its paragraphs (defined by
     *      line feeds) into lines with maximum character count given by
     *      lineCharMaxWidth. Breaks are made by adding line feeds between the words
     *      necessary to keep the line width below the max.
     */
    public static String breakIntoLines( final String text,
                                         final int lineCharMaxWidth )
    {
        return breakIntoLines( text, lineCharMaxWidth, false );
    }



    /**
     *      Method that takes a text string and breaks its paragraphs (defined by
     *      line feeds) into lines with maximum character count given by
     *      lineCharMaxWidth. Breaks are made by adding line feeds between the words
     *      necessary to keep the line width below the max. If forceWrap equals true
     *      then words longer than the lineCharMaxWidth are also broken into lines.
     */
    public static String breakIntoLines( final String text,
                                         final int lineCharMaxWidth,
                                         final boolean forceWrap )
    {
        String[] paragraphs;
        paragraphs = text.split( "\n" );

        String[] words;
        StringBuffer bigBuffer = new StringBuffer( text.length() );
        StringBuffer buffer = null;
        int wordLength;

        for ( int ii = 0; ii < paragraphs.length; ii++ )
        {
            //
            //    Now break the existing lines into... into... into what??
            //
            words = paragraphs[ii].split( " " );
            for ( int jj = 0; jj < words.length; jj++ )
            {
                if ( buffer != null
                        && ( ( buffer.length() + words[jj].length() ) > lineCharMaxWidth ) )
                {
                    appendToBuffer( bigBuffer, buffer.toString(), "\n" );
                    buffer = null;
                }

                wordLength = words[jj].length();
                if ( wordLength >= lineCharMaxWidth )
                {
                    if ( forceWrap )
                    {
                        int kk = 0;
                        int endIndex;
                        while ( kk <= wordLength - 1 )
                        {
                            endIndex = kk + lineCharMaxWidth;
                            if ( endIndex > wordLength )
                            {
                                endIndex = wordLength;
                            }

                            appendToBuffer( bigBuffer, words[jj]
                                    .substring( kk, endIndex ), "\n" );
                            kk += lineCharMaxWidth;
                        }
                    }
                    else
                    {
                        appendToBuffer( bigBuffer, words[jj], "\n" );
                    }
                    continue;
                }

                if ( buffer == null )
                {
                    buffer = new StringBuffer( lineCharMaxWidth );
                }

                appendToBuffer( buffer, words[jj], " " );
            }

            if ( buffer != null )
            {
                appendToBuffer( bigBuffer, buffer.toString(), "\n" );
                buffer = null;
            }
        }

        return bigBuffer.toString();
    }


    public static String formatDate( final Date date )
    {
        if ( date == null )
        {
            return "";
        }

        return getDateFormatter().format( date );
    }


    /**
     * Don't use this, doesn't make sense to return empty string to format for db.
     * @deprecated
     */
    @Deprecated
    public static String formatDateForDb( final Date date )
    {
        if ( date == null )
        {
            return "";
        }

        return getDbDateFormatter().format( date );
    }


    public static String getCurrentFormattedDate()
    {
        return formatDate( new Date() );
    }


    /**
     * Don't use this. Don't parse dates coming back from the db! Ask for them as native types.
     * @param dateString
     * @deprecated
     */
    @Deprecated
    public static Date parseDateFromDb( final String dateString )
        throws ParseException
    {
        throw new UnsupportedOperationException();
    }


    private static FastDateFormat getDateFormatter()
    {
        if( _displayDateFormatter == null )
        {
            _displayDateFormatter = FastDateFormat.getDateTimeInstance( FastDateFormat.MEDIUM,
                                                                        FastDateFormat.LONG );
        }

        return _displayDateFormatter;
    }


    public static FastDateFormat getDbDateFormatter()
    {
        if ( _dbDateFormatter == null )
        {
            // java's DateFormat is NOT threadsafe, and will corrupt both dates if used
            // simultaneously in two threads.
            // Also, Use ISO8601 Date format to match postgresql's default style for
            // timestamp with time zone.
            _dbDateFormatter = FastDateFormat.getInstance( ISO8601_DATE_FORMAT );
        }

        return _dbDateFormatter;
    }


    public static FastDateFormat getDbDateFormatterWithoutTimezone()
    {
        if ( _dbDateFormatterNoTZ == null )
        {
            _dbDateFormatterNoTZ = FastDateFormat.getInstance( "yyyy-MM-dd HH:mm:ss" );
        }

        return _dbDateFormatterNoTZ;
    }


    private static ToStringStyle getUserToStringStyle()
    {
        if ( _userToStringStyle == null )
        {
            _userToStringStyle = new UserToStringStyle();
        }

        return _userToStringStyle;
    }


    public static ToStringBuilder getUserToStringBuilder( final Object object )
    {
        return new ToStringBuilder( object, getUserToStringStyle() );
    }


    public static String md5Hash( final String text )
    {
        try
        {
            byte[]        digestable = text.getBytes( "8859_1" /* encoding */ );
            MessageDigest md         = MessageDigest.getInstance( "MD5" );

            md.update( digestable );

            byte[] digest = md.digest();

            byte          bytes;
            StringBuilder stringBuilder = new StringBuilder();

            for ( int ii = 0; ii < digest.length; ii++ )
            {
                bytes = digest[ii];
                stringBuilder.append( intToHexString( bytes & 0xff ) );
            }

            return stringBuilder.toString();
        }
        catch ( Throwable ex )
        {
            String msg = "Unable to create hash string.";

            throw new UnexpectedCheckedException( msg, ex );
        }
    }


    public static String intToHexString( final int value )
    {
        String hexBit = Integer.toHexString( value );

        if ( hexBit.length() == 1 )
        {
            //
            //    Add a leading zero
            //
            return "0" + hexBit;
        }

        return hexBit;
    }


    public static String inputStreamToString( final InputStream stream )
        throws IOException
    {
        BufferedReader reader;
        reader = new BufferedReader( new InputStreamReader( stream ) );
        StringBuffer result = new StringBuffer( 64 );
        String line;
        while ( ( line = reader.readLine() ) != null )
        {
            result.append( line ).append( "\n" );
        }
        reader.close();

        return result.toString();
    }


    /**
     *      Checks to see if a string contains a variable of type ${variableName}
     *      embedded within it.
     */
    public static boolean hasVariable( final String string,
                                       final String variableName )
    {
        return ( string.indexOf( VARIABLE_START_DELIM + variableName
                + VARIABLE_END_DELIM ) >= 0 );
    }


    /**
     *      Return a variable substituted string, but if a value hasn't been
     *      specified, then leave take out the ${variable} part and leave it
     *      blank/empty/null.
     */
    public static String computeAttrString( final String    attrString,
                                            final Map<String, ? extends Object> attributes )
    {
        return computeAttrString( attrString, attributes, true, null );
    }


    public static String computeAttrString( final String                              attrString,
                                            final Map<String, ? extends Object> attributes,
                                            final boolean                       blankOutNulls,
                                            final Collection<String>            blankOutNullKeyExceptions )
    {
        if ( attrString == null )
        {
            return "";
        }

        int varStart  = attrString.indexOf( VARIABLE_START_DELIM );
        int funcStart = attrString.indexOf( FUNCTION_START_DELIM );
        int escStart  = attrString.indexOf( ESC_SEQ              );

        if ( escStart >= 0
                && ( varStart < 0 || ( varStart >= 0 && escStart < varStart ) )
                && ( funcStart < 0 || ( funcStart >= 0 && escStart < funcStart ) ) )
        {
            String result;
            if ( escStart > 0 )
            {
                result = attrString.substring( 0, escStart );
            }
            else
            {
                result = "";
            }

            result += "$"
                   + computeAttrString( attrString.substring( escStart
                                                              + ESC_SEQ.length() ),
                                        attributes,
                                        blankOutNulls,
                                        blankOutNullKeyExceptions );
            return result;
        }

        if ( varStart < 0 && funcStart < 0 )
        {
            return attrString;
        }

        boolean isFunction = false;
        int     startIndex;
        String  startDelim;
        String  endDelim;

        if ( varStart < 0 || ( funcStart >= 0 && varStart > funcStart ) )
        {
            isFunction = true;
            startIndex = funcStart;
            startDelim = FUNCTION_START_DELIM;
            endDelim   = FUNCTION_END_DELIM;
        }
        else
        {
            startIndex = varStart;
            startDelim = VARIABLE_START_DELIM;
            endDelim   = VARIABLE_END_DELIM;
        }

        int lastIndex = indexOfEndPair( attrString,
                                        startIndex,
                                        startDelim,
                                        endDelim );

        if ( lastIndex == -1 )
        {
            //
            //      No matching delimiter found in the string so just
            //      tack the whole string on to the end.
            //
            return attrString;
        }

        StringBuffer    buffer = new StringBuffer();

        //
        //      Append anything found before the first delimiter to our
        //      string.
        //
        buffer.append( attrString.substring( 0, startIndex ) );

        //
        //      Only look for functions if we are nested inside of a
        //      pair of variable delimiters. Otherwise parentheses are
        //      well just parentheses.
        //
        String    value;
        value = attrString.substring( startIndex + startDelim.length(),
                                      lastIndex );
        Object    result;
        if ( isFunction )
        {
            Collection<String>    params = split( value, PARAM_DELIM );

            String      function       = null;
            String[]    args           = null;
            String[]    parameterTypes = null;

            int    count = 0;
            for ( String    param : params )
            {
                if ( count == 0 )
                {
                    function = param;
                }
                else
                {
                    if ( count == 1 )
                    {
                        args = new String[params.size() - 1];
                        parameterTypes = new String[params.size() - 1];
                    }

                    parameterTypes[count - 1] = "java.lang.String";

                    args[count - 1] = computeAttrString( param,
                                                         attributes,
                                                         blankOutNulls,
                                                         blankOutNullKeyExceptions );
                }

                count++;
            }

            String    funcClass;
            int       lastDot = function.lastIndexOf( "." );
            if ( lastDot < 0 )
            {
                funcClass = "com.samsix.util.string.AttrString";
            }
            else
            {
                funcClass = function.substring( 0, lastDot );
                function = function.substring( lastDot + 1 );
            }

            try
            {
                result = SimpleMethodInvoker.invokeStaticMethod( funcClass,
                                                                 function,
                                                                 args,
                                                                 parameterTypes );
            }
            catch ( Throwable ex )
            {
                Logger.getLogger( StringUtilities.class )
                      .error( "Can't evaluate attribute string function", ex );

                //
                //    Just put it back the way it was in the original string
                //
                if ( ! blankOutNulls )
                {
                    result = startDelim + value + endDelim;
                }
                else
                {
                    result = "";
                }
            }

            buffer.append( result.toString() );
        }
        else
        {
            String    varName;
            varName = computeAttrString( value, attributes, blankOutNulls, blankOutNullKeyExceptions );

            if ( attributes == null )
            {
                result = null;
            }
            else
            {
                result = attributes.get( varName );
            }

            if ( result == null )
            {
                if ( ! blankOutNulls
                     || ( blankOutNullKeyExceptions != null
                          && blankOutNullKeyExceptions.contains( varName ) ) )
                {
                    //
                    //      Just put the variable back in place I guess.
                    //      This is the way it used to work anyway.
                    //
                    buffer.append( VARIABLE_START_DELIM )
                          .append( varName )
                          .append( VARIABLE_END_DELIM );
                }
            }
            else
            {
                buffer.append( result.toString() );
            }
        }

        if ( lastIndex > -1 )
        {
            int leftOverIndex = lastIndex + endDelim.length();

            if ( leftOverIndex != attrString.length() )
            {
                buffer.append( computeAttrString( attrString.substring( leftOverIndex ),
                                                  attributes,
                                                  blankOutNulls,
                                                  blankOutNullKeyExceptions ) );
            }
        }

        return buffer.toString();
    }


    /**
     *    Chop off trailing .0 to make nicely formatted number from a double
     */
    public static String doubleToString( final double    number )
    {
//        Long round = Math.round( number );
//
//        return ( round.doubleValue() == number ) ? round.toString() : number.toString() ;

        //
        //    This is much faster actually and I think does the same thing.
        //
        DecimalFormat df = new DecimalFormat("#.#####");
        return df.format( number );
    }


    public static String formatFraction( final double    number )
    {
        return formatFraction( number, 64 );
    }


    public static String formatFraction( final double    aNumber,
                                         final int       denom )
    {
        boolean isNegative = false;
        if ( aNumber < 0 )
        {
            isNegative = true;
        }

        double    number;
        number = Math.abs( aNumber );
        int whole = (int) number;
        double value = ( number - whole ) * denom;

        double diff = value - ( (int) value );

        //
        //      Could check for diff being zero, but this being doubles
        //      we should probably give it a bit of tolerance to allow
        //      for roundoff errors.
        //
        String result;
        if ( Math.abs( diff ) < 0.000000001 )
        {
            int numerator = Math.round( (float) value );

            int factor = 1;

            if ( numerator > 0 )
            {
                while ( ( numerator % factor ) == 0 )
                {
                    factor *= 2;
                }

                factor = factor / 2;
            }

            result = "";
            if ( whole == 0 && numerator == 0 )
            {
                return "0";
            }
            else if ( whole != 0 )
            {
                result = String.valueOf( whole );
            }

            if ( numerator > 0 )
            {
                if ( result.length() > 0 )
                {
                    result += " ";
                }

                result += ( numerator / factor ) + "/" + ( denom / factor );
            }
        }
        else
        {
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMinimumFractionDigits( 3 );
            formatter.setMaximumFractionDigits( 3 );
            formatter.setGroupingUsed( false );

            result = formatter.format( number );
        }

        if ( isNegative )
        {
            return "-" + result;
        }

        return result;
    }


    private static int indexOfEndPair( final String    text,
                                       final int       startIndex,
                                       final String    startDelim,
                                       final String    endDelim )
    {
        int endPos = -1;
        int newStartPos;

        int index = startIndex + startDelim.length();
        int endCount = 0;
        int startCount = 1;

        while ( endCount < startCount && index >= 0 )
        {
            endPos = text.indexOf( endDelim, index );
            newStartPos = text.indexOf( startDelim, index );

            if ( newStartPos < endPos && newStartPos > -1 )
            {
                startCount++;
                if ( newStartPos == -1 )
                {
                    index = -1;
                }
                else
                {
                    index = newStartPos + startDelim.length();
                }
            }
            else
            {
                endCount++;
                if ( endCount < startCount )
                {
                    if ( endPos == -1 )
                    {
                        index = -1;
                    }
                    else
                    {
                        index = endPos + endDelim.length();
                    }
                }
            }
        }

        return endPos;
    }


    public static List<String> createStringList( String value, String delim )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return Collections.emptyList();
        }

        if ( delim == null )
        {
            delim = COMMA_DELIMITER;
        }

        //
        //      If the value starts with a delimiter, we want
        //      that to be an empty item in the list. So we check
        //      to see if the value starts with a delim, and
        //      if it does, add a space to the front and it should
        //      all work out.
        //
        char firstChar = value.charAt( 0 );
        if ( delim.indexOf( firstChar ) >= 0 )
        {
            value = " " + value;
        }

        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer( value, delim, false );

        while ( tokenizer.hasMoreTokens() )
        {
            result.add( tokenizer.nextToken().trim() );
        }

        return result;
    }


    /**
     *    Escape special XML characters (&lt; &gt; &amp; &quot; &#39;)
     *
     *    @param str the string to escape;
     *    @return a properly escaped string, or null if str was null.
     */
    public static String encodeXML( final String    str )
    {
        if ( str == null )
        {
            return null;
        }

        StringCharacterIterator    iterator = new StringCharacterIterator( str );
        StringBuilder              result   = new StringBuilder();
        char                       c;

        for( c  = iterator.current();
             c != CharacterIterator.DONE;
             c  = iterator.next() )
        {
            if ( c == '>' )
            {
                result.append( "&gt;" );
            }
            else if ( c == '<' )
            {
                result.append( "&lt;" );
            }
            else if ( c == '&' )
            {
                result.append( "&amp;" );
            }
            else if ( c == '"' )
            {
                result.append( "&quot;" );
            }
            else if ( c == '\'' )
            {
                result.append( "&#39;" );
            }
            else
            {
                result.append( c );
            }
        }

        return result.toString();
    }


    public static char decodeEscapedXMLEntity( final String    entity )
    {
        if ( entity == null )
        {
            throw new IllegalArgumentException( "null XML entity" );
        }

        int    len = entity.length();

        //
        //    must have at least three chars: a start ampersand, something,
        //    and an end semicolon
        //
        if ( len < 3 || entity.charAt( 0 ) != '&'
                    || entity.charAt( len - 1 ) != ';' )
        {
            throw new IllegalArgumentException( "not an XML entity ["
                                                + entity + "]" );
        }

        String    value = entity.substring( 2, len - 2 );

        if ( entity.charAt( 1 ) == '#' )
        {
            return (char) Integer.parseInt( value, 10 );
        }

        if ( "amp".equals( value ) )
        {
            return '&';
        }
        else if ( "gt".equals( value ) )
        {
            return '>';
        }
        else if ( "lt".equals( value ) )
        {
            return '<';
        }
        else if ( "quot".equals( value ) )
        {
            return '"';
        }
        else
        {
            throw new IllegalArgumentException( "Invalid XML entity [" + value + "]" );
        }
    }


    /**
     *    Compares two strings, dealing with null values.
     */
    public static int compareStrings( final String     s1,
                                      final String     s2,
                                      final boolean    ignoreCase )
    {
        if ( s1 == s2 )
        {
            return 0;
        }
        else if ( s1 == null )
        {
            return -1;
        }
        else if ( s2 == null )
        {
            return 1;
        }
        else
        {
            if ( ignoreCase )
            {
                return s1.compareToIgnoreCase( s2 );
            }
            else
            {
                return s1.compareTo( s2 );
            }
        }
    }


    /**
     *    Replaces all non-alphanumeric characters in the source
     *    string with underscores.
     */
    public static String toAlphanumeric( final String    source )
    {
        if ( source == null )
        {
            return null;
        }

        return source.replaceAll( "[^a-zA-Z0-9]", "_" );
    }


    public static String exceptionToString( final Throwable    ex )
    {
        StringWriter    traceWriter = new StringWriter();
        PrintWriter     writer = new PrintWriter( traceWriter );

        ex.printStackTrace( writer );

        return traceWriter.toString();
    }


    //
    //    The following are used to format description and lookup strings
    //
    public static String formatDescr( final String    arg1,
                                      final String    arg2)
    {
        if ( isNullOrEmptyOrBlank( arg1 ) )
        {
            return arg2;
        }
        else if ( isNullOrEmptyOrBlank( arg2 ) )
        {
            return arg1;
        }
        else
        {
            return arg1 + " - " + arg2;
        }
    }


    public static String formatDescr( final String    arg1,
                                      final String    arg2,
                                      final String    arg3 )
    {
        if ( isNullOrEmptyOrBlank( arg1 ) )
        {
            return formatDescr( arg2, arg3 );
        }
        else if ( isNullOrEmptyOrBlank( arg2 ) )
        {
            return formatDescr( arg1, arg3 );
        }
        else if ( isNullOrEmptyOrBlank( arg3 ) )
        {
            return formatDescr( arg1, arg2 );
        }
        else
        {
            return arg1 + " - " + arg2 + " - " + arg3;
        }
    }


    /**
     *    Checks if a String contains only numbers
     */
    public static boolean containsOnlyNumbers( final String    str )
    {
        if( str == null )
        {
            return false;
        }

        return str.matches( "^\\d+$" );
    }

    /**
     *    This method checks if a String contains only numbers
     */
    public static boolean isDigitOnlyString( final String    str )
    {
        //
        //    It can't contain only numbers if it's null or empty...
        //
        if ( StringUtils.isBlank( str ) )
        {
            return false;
        }

        int    length = str.length();
        for ( int ii = 0; ii < length; ii++ )
        {
            //
            //    If we find a non-digit character we return false
            //
            if ( ! Character.isDigit( str.charAt( ii ) ) )
            {
                return false;
            }
        }

        return true;
    }


    /**
     *    Compares two strings (or really, any objects implementing Comparable)
     *    and safely handles nulls.
     *
     *    @param a
     *    @param b
     *    @param nullsFirst
     *    @return
     */
    public static <T extends Comparable<T>> int nullsafeCompare( final T          a,
                                                                 final T          b,
                                                                 final boolean    nullsFirst )
    {
        if( a == b )
        {
            return 0;
        }

        if( a == null )
        {
            return nullsFirst ? -1 : 1;
        }

        if( b == null )
        {
            return nullsFirst ? 1 : -1;
        }

        return a.compareTo( b );
    }



    /**
     *    Similar to SQL coalesce, returns the first non-null argument
     *
     *    @param things
     *    @return
     */
    public static <T> T coalesce( final T ... things )
    {
        if( things == null || things.length == 0 )
        {
            return null;
        }

        for( T    thing : things )
        {
            if( thing != null )
            {
                return thing;
            }
        }

        return null;
    }


    /**
     *    Similar to SQL coalesce, returns the first non-EMPTY argument
     *
     *    @param things
     *    @return
     */
    public static <T> T coalesceNonEmpty( final T ... things )
    {
        if( things == null || things.length == 0 )
        {
            return null;
        }

        for( T    thing : things )
        {
            if( thing instanceof CharSequence )
            {
                if( ! StringUtils.isBlank( (CharSequence) thing ) )
                {
                    return thing;
                }
            }
            else if( thing != null )
            {
                return thing;
            }
        }

        return null;
    }



    public static String toBase64( final String    string )
    {
        if( string == null )
        {
            return null;
        }

        try
        {
            return toBase64( string.getBytes( "UTF-8" ) );
        }
        catch( UnsupportedEncodingException    ex )
        {
            throw new UnexpectedCheckedException( ex );
        }
    }


    public static String toBase64( final byte[]    bytes )
    {
        try
        {
            return new String( Base64.encodeBase64( bytes ), "ISO-8859-1" );
        }
        catch( UnsupportedEncodingException    ex )
        {
            throw new UnexpectedCheckedException( ex );
        }
    }


    public static byte[] fromBase64( final String    string )
    {
        if( string == null )
        {
            return null;
        }

        try
        {
            return Base64.decodeBase64( string.getBytes( "ISO-8859-1" ) );
        }
        catch( UnsupportedEncodingException    ex )
        {
            throw new UnexpectedCheckedException( ex );
        }
    }


    public static String fromBase64ToString( final String    string )
    {
        if( string == null )
        {
            return null;
        }

        try
        {
            return new String( fromBase64( string ), "UTF-8" );
        }
        catch( Throwable    ex )
        {
            throw new UnexpectedCheckedException( ex );
        }
    }


    public static String bytesToHex( final byte[]    hex )
    {
        if( hex == null )
        {
            return null;
        }

        char[]    chars = new char[ hex.length * 2 ];

        for( int    idx = 0; idx < hex.length; idx++ )
        {
            chars[ idx * 2 ]     = HEX_CHARS[ ( hex[idx] & 0xF0 ) >>> 4 ];
            chars[ idx * 2 + 1 ] = HEX_CHARS[ hex[idx] & 0x0F ];
        }

        return new String( chars );
    }


    public static byte[] hexToBytes( final String    hexStr )
    {
        if( hexStr == null )
        {
            return null;
        }

        String    hex = hexStr;

        //
        //     If it has odd length, prefix it with a 0 to make it even.
        //
        if( ( hexStr.length() & 1 ) != 0 )
        {
            hex = "0" + hexStr;
        }

        int       len  = hex.length();
        byte[]    data = new byte[ len / 2 ];

        for ( int idx = 0; idx < len; idx += 2 )
        {
            data[ idx / 2 ] = (byte) ( ( Character.digit( hex.charAt( idx ), 16 ) << 4)
                                       + Character.digit( hex.charAt( idx + 1 ), 16 ) );
        }

        return data;
    }


    public static int computeLevenshteinDistance( final CharSequence    str1,
                                                  final CharSequence    str2 )
    {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for ( int ii = 0; ii <= str1.length(); ii++ )
        {
            distance[ii][0] = ii;
        }

        for ( int jj = 1; jj <= str2.length(); jj++ )
        {
            distance[0][jj] = jj;
        }

        for ( int ii = 1; ii <= str1.length(); ii++ )
        {
            for ( int jj = 1; jj <= str2.length(); jj++ )
            {
                distance[ii][jj] = Math.min( Math.min( distance[ii - 1][jj] + 1,
                                                     distance[ii][jj - 1] + 1 ),
                                           distance[ii - 1][jj - 1]
                                           + ((str1.charAt(ii - 1) == str2.charAt(jj - 1)) ? 0 : 1));
            }
        }

        return distance[str1.length()][str2.length()];
    }
}

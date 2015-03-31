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
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package com.samsix.util.string;


import java.util.Comparator;

/**
 * This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle
 *
 * To convert to use Templates (Java 1.5+):
 *   - Change "implements Comparator" to "implements Comparator<String>"
 *   - Change "compare(Object o1, Object o2)" to "compare(String s1, String s2)"
 *   - Remove the type checking and casting in compare().
 *
 * To use this class:
 *   Use the static "sort" method from the java.util.Collections class:
 *   Collections.sort(your list, new AlphanumComparator());
 *    
 * k-n Sep 19, 2008:
 *    - Modified to allow case-insensitive comparisons.
 *    - Added the following description...
 *    This class is a comparison method following the comparable
 *    interface but will sort strings by their alpha and numeric
 *    sections separately.  So that for example "S-11" comes before
 *    "S-100".  Likewise, a simple numeric string will also sort
 *    numerically such that "11" will come before "100".  As a counter
 *    example, "A-100" will sort before "S-11" because the alpha part
 *    is sorted on before the numeric part.
 */
public class AlphanumComparator
    implements
        Comparator<String>
{
    private static AlphanumComparator    _caseSensitiveInstance;
    private static AlphanumComparator    _caseInsensitiveInstance;
    
    private final boolean    _ignoreCase;
    
    
    public static AlphanumComparator caseInsensitiveInstance()
    {
        if( _caseInsensitiveInstance == null )
        {
            synchronized( AlphanumComparator.class )
            {
                _caseInsensitiveInstance = new AlphanumComparator( true );
            }
        }
        
        return _caseInsensitiveInstance;
    }
    

    public static AlphanumComparator caseSensitiveInstance()
    {
        if( _caseSensitiveInstance == null )
        {
            synchronized( AlphanumComparator.class )
            {
                _caseSensitiveInstance = new AlphanumComparator( false );
            }
        }
        
        return _caseSensitiveInstance;
    }
    
    
    public AlphanumComparator()
    {
        this( false );
    }



    public AlphanumComparator( boolean    ignoreCase )
    {
        _ignoreCase = ignoreCase;
    }



    private final boolean isDigit(char ch)
    {
        return ch >= 48 && ch <= 57;
    }



    /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
    private final String getChunk(String s, int slength, int marker)
    {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (isDigit(c))
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (!isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        } else
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }



    public int compare( String    s1,
                        String    s2 )
    {
        if(s1 == null)
        {
            s1 = "";
        }
        
        if(s2 == null)
        {
            s2 = "";
        }
        
        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length)
        {
            String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();

            String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();

            //    If both chunks contain numeric characters, sort them numerically
            int result = 0;
            if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0)))
            {
                //    Simple chunk comparison by length.
                int thisChunkLength = thisChunk.length();
                result = thisChunkLength - thatChunk.length();
                //    If equal, the first different number counts
                if (result == 0)
                {
                    for (int i = 0; i < thisChunkLength; i++)
                    {
                        result = thisChunk.charAt(i) - thatChunk.charAt(i);

                        if (result != 0)
                        {
                            return result;
                        }
                    }
                }
            } else
            {
                if ( _ignoreCase )
                {
                    result = thisChunk.compareToIgnoreCase( thatChunk );
                }
                else
                {
                    result = thisChunk.compareTo(thatChunk);
                }
            }

            if (result != 0)
                return result;
        }

        return s1Length - s2Length;
    }
}

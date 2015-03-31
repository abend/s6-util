package com.samsix.util.io;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * A Reader that escapes every instance of a given character, requiring that character to appear twice
 * to be unescaped.
 *
 * For instance, an EscapingReader with ( '$', '\\' ) will make the following:
 *     $('#test').text('$${VALUE}')
 * to:
 *     \$('#test').text('${VALUE}')
 */
public class EscapingReader
    extends
        PushbackReader
{
    private final char _escapeChar;
    private final char _charToEscape;

    private boolean _inEscaped = false;


    public EscapingReader( final Reader in,
                           final char charToEscape,
                           final char escapeChar )
    {
        super( in, 2 );

        _charToEscape = charToEscape;
        _escapeChar = escapeChar;
    }


    @Override
    public long skip( final long n )
        throws IOException
    {
        _inEscaped = false;
        return super.skip( n );
    }


    @Override
    public int read()
        throws
            IOException
    {
        int intch = super.read();

        if( intch == -1 )
        {
            return -1;
        }

        final char ch = (char) intch;

        if( ch == _charToEscape && ! _inEscaped )
        {
            int nextintch = super.read();

            // double dollar sign = single unescaped $
            if( ( (char) nextintch ) == _charToEscape )
            {
                return ch;
            }
            else
            {
                _inEscaped = true;

                if( nextintch != -1 )
                {
                    unread( nextintch );
                }

                unread( _charToEscape );

                return _escapeChar;
            }
        }
        else
        {
            _inEscaped = false;
            return ch;
        }
    }


    @Override
    public int read( final char[] cbuf,
                     final int off,
                     final int len )
        throws IOException
    {
        //
        // Just calls read() a whole bunch. Not super efficient.
        //

        if ( len <= 0 )
        {
            if ( len < 0 )
            {
                throw new IndexOutOfBoundsException();
            }

            return 0;
        }

        if ( ( off < 0 ) || ( off > cbuf.length ) )
        {
            throw new IndexOutOfBoundsException();
        }

        int count;
        for( count = 0; count < len; count++ )
        {
            int ch = read();

            // EOF, count is at the position that caused EOF
            if( ch == -1 )
            {
                break;
            }

            cbuf[ count + off ] = (char) ch;
        }

        return count;
    }
}
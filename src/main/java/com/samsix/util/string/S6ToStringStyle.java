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
 * Copyright (c) 2001-2008 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.string;

import org.apache.commons.lang3.builder.ToStringStyle;


public class S6ToStringStyle
    extends
        ToStringStyle
{
	private static final long serialVersionUID = 1L;

    private boolean    _concise;


    /**
     * <p>Constructor.</p>
     *
     * <p>Use the static constant rather than instantiating.</p>
     */
    public S6ToStringStyle()
    {
        setContentStart( "\n{" );
        setDefaultFullDetail( true );
        setFieldSeparator( "\n\t- " );

        //    This method is either newer than the version
        //    we have, or obsolete.
//        setFieldSeparatorAtStart( true );

        setContentEnd( "\n}" );
        setFieldNameValueSeparator( ": " );
    }


    public boolean isConcise()
    {
        return _concise;
    }


    public void setConcise( final boolean    concise )
    {
        _concise = concise;
    }


    /**
     * <p>Append the field start to the buffer.</p>
     *
     * @param buffer  the <code>StringBuffer</code> to populate
     * @param fieldName  the field name
     */
    @Override
    protected void appendFieldStart( final StringBuffer    buffer,
                                     final String          fieldName )
    {
        appendFieldSeparator( buffer );

        if ( isUseFieldNames() && fieldName != null )
        {
            buffer.append( fieldName );

            for ( int ii = 0; ii < ( 18 - fieldName.length()); ii++ )
            {
                buffer.append( " " );
            }

            buffer.append( getFieldNameValueSeparator() );
        }
    }


    @Override
    protected void appendFieldEnd( final StringBuffer    buffer,
                                   final String          fieldName )
    {
        //    do nothing.
    }


	@Override
    protected void appendInternal( final StringBuffer    buffer,
                                   final String          fieldName,
                                   final Object          value,
                                   final boolean         detail )
    {
	    appendSummary( buffer, fieldName, value );

	    if ( detail )
	    {
	        appendDetail( buffer, fieldName, value );
	    }
    }
}

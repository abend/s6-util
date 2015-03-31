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
 * Copyright (c) 2001-2011 Sam Six.  All rights reserved.
 *
 * Company:      http://www.samsix.com
 *
 ***************************************************************************
 */
package com.samsix.util.string;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.samsix.util.annotation.JsonMapper;
import com.samsix.util.annotation.JsonMapperType;


@XmlAccessorType( XmlAccessType.FIELD )
@JsonMapper( JsonMapperType.JACKSON )
public final class StringPair
    implements
        Comparable<StringPair>
{
    @XmlAttribute( name = "key" )
    @JsonProperty("key")
    private String    _key;
    @XmlAttribute( name = "value" )
    @JsonProperty("value")
    private String    _value;


    public StringPair()
    {
        // for JSON deserialization
    }
    
    
    public StringPair( String    key,
                       String    value )
    {
        _key   = key;
        _value = value;
    }
    
    
//    public static Collection<ComboBoxItem<String>> asComboBoxItems( final Collection<StringPair>    pairs )
//    {
//        Collection<ComboBoxItem<String>>    items;
//        items = new ArrayList<ComboBoxItem<String>>( pairs.size() );
//        
//        for ( StringPair    pair : pairs )
//        {
//            items.add(  new SimpleComboBoxItem<String>( pair.getKey(), pair.getValue() ) );
//        }
//        
//        return items;
//    }


    public String getKey()
    {
        return _key;
    }


    public String getValue()
    {
        return _value;
    }
    

    /**
     *    Override equals() so we can compare StringPair objects.
     */
    @Override
    public boolean equals( Object   obj )
    {
        if ( ! ( obj instanceof StringPair ) )
        {
            return false;
        }

        StringPair    rhs = (StringPair) obj;

        //
        //    Now use the equals() method for the String class.
        //
        return _key.equals( rhs.getKey() )
        	&& _value.equals( rhs.getValue() );
    }


    @Override
    public int hashCode()
    {
        int      result = 211;

        if ( _key != null )
        {
            result = 37 * result + _key.hashCode();
        }

        if ( _value != null )
        {
            result = 37 * result + _value.hashCode();
        }

        return result;
    }


    @Override
    public int compareTo( StringPair    pair )
    {
        return _value.compareToIgnoreCase( pair.getValue() );
    }


    @Override
    public String toString()
    {
        return "(" + getKey() + ", " + getValue() + ")";
    }
}

package com.samsix.util.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import com.samsix.util.UtilException;

public class ConnectionHelper
{
    private final Map<String,String> _params = new HashMap<String,String>();
    
    
    public void addParameter( final String name,
                              final String value )
    {
        _params.put( name, value );
    }
    
    
    public String getParamString() throws UnsupportedEncodingException
    {
        StringBuilder builder = new StringBuilder();
        
        for ( Entry<String,String> entry : _params.entrySet() ) {
            if ( builder.length() > 0 ) {
                builder.append( "&" );
            }
            
            builder.append(  entry.getKey() )
                   .append( "=" )
                   .append( URLEncoder.encode( entry.getValue(), "UTF-8") );
        }
        
        return builder.toString();
    }
    
    
    public void sendPost(final HttpsURLConnection conn ) throws IOException
    {
        conn.setRequestMethod( "POST" );
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String params = getParamString();
        conn.setFixedLengthStreamingMode(params.getBytes().length);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params);
        wr.flush();
        wr.close();
    }
    
    
    public InputStreamReader callGet(final String urlPath)
        throws IOException, UtilException
    {
        URL url = new URL(urlPath);
        
        HttpURLConnection conn;
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod( "GET" );
        conn.setDoOutput(true);
        
        String params = getParamString();
        conn.setFixedLengthStreamingMode(params.getBytes().length);

        //Send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params);
        wr.flush ();
        wr.close ();
 
        int code = conn.getResponseCode();

//        java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
//        System.out.println( s.hasNext() ? s.next() : "" );
        
        if ( code != 200 ) {
            throw new UtilException("Got response [" + code + "] from [" + url + "]");
        } else {
            return new InputStreamReader( conn.getInputStream(), "UTF-8" );
        }
    }
}

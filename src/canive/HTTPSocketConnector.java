package canive;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.SocketConnectionEnhanced;

public class HTTPSocketConnector
{

    static public String getHtml( String url, long timeout )
    {

        String response = "";
        try
        {
            String host = getHostUrl( url );
            String page = getPageUrl( url );
            SocketConnectionEnhanced hc = ( SocketConnectionEnhanced )Connector.open( "socket://" + host + ":80" );
            hc.setSocketOptionEx( SocketConnectionEnhanced.READ_TIMEOUT, timeout );
            DataOutputStream dout = new DataOutputStream(( ( SocketConnection )hc ).openOutputStream() );
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            String request = "GET /" + page + " HTTP/1.1\r\n" + "Host: " + host + ":80\r\n" + "User-Agent: MIDP2.0\r\n" + "Content-Type: text/html\r\n\r\n";
            bos.write( request.getBytes() );
            dout.write( bos.toByteArray() );
            dout.flush();
            dout.close();            
            InputStream is = ( ( SocketConnection )hc ).openInputStream();
            byte[] bytes = null;            
            bytes = IOUtilities.streamToBytes( is );
            is.close();
            response = new String( bytes, "UTF-8" );
        }
        catch( Exception e )
        {
            response = e.getMessage();
        }
        return response;
    }

    private static String getPageUrl( String url )
    {
        String result = url;
        if( result.indexOf( "//" ) != -1 )
        {
            result = result.substring( result.indexOf( "//" ) + "//".length(), result.length() );
        }

        if( result.indexOf( "/" ) != -1 )
        {
            result = result.substring( result.indexOf( "/" ) + "/".length(), result.length() );
        }
        return result;
    }

    private static String getHostUrl( String url )
    {
        String result = url;

        if( result.indexOf( "//" ) != -1 )
        {
            result = result.substring( result.indexOf( "//" ) + "//".length(), result.length() );
        }

        if( result.indexOf( "/" ) != -1 )
        {
            result = result.substring( 0, result.indexOf( "/" ) );
        }
        return result;
    }
}
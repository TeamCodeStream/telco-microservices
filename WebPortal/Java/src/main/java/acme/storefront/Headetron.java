package acme.storefront;

import org.apache.http.client.fluent.Request;

import java.util.HashMap;

/**
 * Created by gabe on 3/30/17.
 */
public class Headetron {
    private static HashMap<String,String> headers = new HashMap<String,String>();

    public static void setHeader(String key, String value){
        headers.put(key,value);
    }

    public static String getHeader(String key){
       return (String) headers.get(key);
    }

    public static void addAllHeaders(Request request) {
        headers.forEach((k,v) -> request.addHeader(k,v));
    }
}

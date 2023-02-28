package acme.storefront;

import org.apache.http.client.fluent.Request;

/**
 * Created by gabe on 6/9/17.
 */
public class HttpRequest {

    private static int requestTimeoutSeconds = 10;

    public static Request createGetRequest(String url){
        Request request = Request.Get(url);
        addRequestDetails(request);
        return request;
    }

    public static Request createPostRequest(String url){
        Request request = Request.Post(url);
        addRequestDetails(request);
        return request;
    }

    private static void addRequestDetails(Request request){
        connectionTimeout(request);
        socketTimeout(request);
        Headetron.addAllHeaders(request);
    }

    private static void connectionTimeout(Request request){
        request.connectTimeout(1000*requestTimeoutSeconds);
    }

    private static void socketTimeout(Request request){
        request.socketTimeout(1000*requestTimeoutSeconds);
    }

}

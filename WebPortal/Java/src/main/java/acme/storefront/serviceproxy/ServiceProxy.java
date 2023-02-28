package acme.storefront.serviceproxy;

import acme.storefront.HttpRequest;

import com.newrelic.api.agent.Trace;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class ServiceProxy
{
    @Trace
    public String getResponse(String url) throws IOException
    {
        Request request = HttpRequest.createGetRequest(url);
        String responseJson = request.execute().returnContent().asString();
        return responseJson;
    }
}
package acme.storefront.serviceproxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.newrelic.api.agent.Trace;
import jodd.json.JsonParser;

public class InventoryServiceProxy extends ServiceProxy
{
    private String _root;
    private static final String _route = "/api/v1/phones";

    public InventoryServiceProxy(String root)
    {
        _root = root;
    }

    public ArrayList getPhones() throws IOException
    {
        return getPhones( _root +_route);
    }
    
    public HashMap<String,String> getPhone(String identity) throws IOException
    {
        ArrayList phones = getPhones( _root +_route +"/" +identity);
        return (HashMap) phones.get(0);
    }

    @Trace
    private ArrayList getPhones(String url) throws IOException
    {
        String response = getResponse(url);
        Map json = new JsonParser().parse(response);
        ArrayList phones = (ArrayList) json.get("phones");
        return phones;
    }
}

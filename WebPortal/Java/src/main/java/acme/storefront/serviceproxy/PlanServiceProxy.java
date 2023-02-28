package acme.storefront.serviceproxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.newrelic.api.agent.Trace;
import jodd.json.JsonParser;

public class PlanServiceProxy extends ServiceProxy
{
    private String _root;
    private static final String _route = "/api/v1/plans";

    public PlanServiceProxy(String root)
    {
        _root = root;
    }

    public ArrayList getPlans() throws IOException
    {
        return getPlans( _root +_route);
    }
    
    public HashMap<String,String> getPlan(String identity) throws IOException
    {
        ArrayList plans = getPlans( _root +_route +"/" +identity);
        return (HashMap) plans.get(0);
    }

    @Trace
    private ArrayList getPlans(String url) throws IOException
    {
        String response = getResponse(url);
        Map json = new JsonParser().parse(response);
        ArrayList plans = (ArrayList) json.get("plans");
        return plans;
    }
}

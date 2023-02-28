package acme.storefront.serviceproxy;

import acme.storefront.instrumentation.JavaAgentFacade;
import java.io.IOException;
import java.util.Map;

import com.newrelic.api.agent.Trace;
import jodd.json.*;

public class CouponServiceProxy extends ServiceProxy
{
    private String _root;
    private static final String _route = "/api/v1/coupons";

    public CouponServiceProxy(String root)
    {
        _root = root;
    }

    @Trace
    public String getCoupons() throws IOException
    {
        String url = getServiceUri() +"/list";
        String responseJson = getResponse(url);
        return responseJson;
    }

    @Trace
    public String getCoupon(String product_id) throws IOException
    {
        JsonParser jsonParser = new JsonParser();
        String url = getServiceUri() + "/" + product_id + "/getCoupon";
        String coupon = "";

        String responseJson = getResponse(url);
        try{
            Map map = jsonParser.parse(responseJson);
            coupon =  (String) map.get("id");
            JavaAgentFacade.addCustomParameter("couponCode", coupon);

            String amount =  (String) map.get("id");
            JavaAgentFacade.addCustomParameter("couponAmount", amount);
        }catch(Exception e){
            JavaAgentFacade.addCustomParameter("coupon", "FAIL!!!");
        }

        return coupon;
    }

    @Trace
    public String isValid(String coupon_guid) throws IOException
    {
        String url = getServiceUri() + "/" + coupon_guid + "/isvalid";
        String responseJson = getResponse(url);
        return responseJson;
    }

    public String getServiceUri()
    {
        return _root +_route;
    }
}

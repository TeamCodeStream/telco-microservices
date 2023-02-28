package acme.storefront.action;

import acme.storefront.Configutron;
import acme.storefront.Headetron;
import acme.storefront.instrumentation.JavaAgentFacade;
import acme.storefront.serviceproxy.CouponServiceProxy;
import acme.storefront.serviceproxy.InventoryServiceProxy;

import com.newrelic.api.agent.Trace;
import jodd.madvoc.meta.In;
import jodd.madvoc.meta.MadvocAction;
import jodd.madvoc.meta.Out;
import jodd.madvoc.meta.RestAction;
import jodd.madvoc.ScopeType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by alarson on 5/6/17.
 */
@MadvocAction
public class BrowsePhoneAction {
    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    @In
    String id;

    @Out
    HashMap<String,String> phone;

    private static final Logger logger = LogManager.getLogger(BrowsePhoneAction.class);

    @RestAction("/browse/phones/${id}")
    @Trace(dispatcher = true)
    public String browsePhone() throws IOException {
        String productId = id.toString();

        long start = System.currentTimeMillis();

        logger.debug("Starting Browse Phone ("+ productId +") transaction");

        String username = (String) sessionMap.get("username");
        if(username != null && !username.equals("")) {
            Headetron.setHeader("X-TELCO-USERNAME", username);
        }
        String userid = (String) sessionMap.get("userid");
        if(userid != null && !userid.equals("")) {
            logger.debug("Adding X-TELCO-USERID: ("+ userid +")");
            Headetron.setHeader("X-TELCO-USERID", userid);
            logger.trace("User " + userid + " browsing phone");
        }

        String coretronRoot = Configutron.getValue("coretron_URL");
        logger.debug("coretron: "+ coretronRoot);
        CouponServiceProxy couponProxy = new CouponServiceProxy(coretronRoot);
        couponProxy.getCoupon(productId);

        String inventoryRoot = Configutron.getValue("inventory_URL");
        logger.debug("inventory: "+ inventoryRoot);
        InventoryServiceProxy phoneProxy = new InventoryServiceProxy(inventoryRoot);
        phone = phoneProxy.getPhone(productId);

        JavaAgentFacade.addCustomParameter("BrowsePhoneResult", "success");

        long end = System.currentTimeMillis() - start;

        logger.debug("Finished Browse Phone ("+ productId +") transaction in " + String.valueOf(end) + "ms");

        return "/browse/phone.jsp";
    }
}

package acme.storefront.action;

import acme.storefront.AcmeService;
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
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by gabe on 2/28/17.
 */

@MadvocAction
public class BrowsePhonesAction {
    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;
    @Out
    ArrayList phones;

    private static final Logger logger = LogManager.getLogger(BrowsePhonesAction.class);

    @RestAction("/browse/phones")
    @Trace(dispatcher = true)
    public void browsePhones() throws IOException {
        logger.debug("Starting Browse Phones transaction");

        AcmeService.checkTimingHeaders();

        String username = (String) sessionMap.get("username");
        if(username != null && !username.equals("")) {
            Headetron.setHeader("X-TELCO-USERNAME", username);
        }
        String userid = (String) sessionMap.get("userid");
        if(userid != null && !userid.equals("")) {
            logger.debug("Adding X-TELCO-USERID: ("+ userid +")");
            Headetron.setHeader("X-TELCO-USERID", userid);
            logger.trace("User " + userid + " browsing phones");
        }

        String coretronRoot = Configutron.getValue("coretron_URL");
        logger.debug("coretron: "+ coretronRoot);
        CouponServiceProxy couponProxy = new CouponServiceProxy(coretronRoot);
        couponProxy.getCoupons();

        String inventoryRoot = Configutron.getValue("inventory_URL");
        logger.debug("inventory: "+ inventoryRoot);
        InventoryServiceProxy phoneProxy = new InventoryServiceProxy(inventoryRoot);
        phones = phoneProxy.getPhones();

        logger.debug("Finished Browse Phones transaction");

        JavaAgentFacade.addCustomParameter("BrowsePhonesResult","success");
    }
}

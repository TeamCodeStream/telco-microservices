package acme.storefront.action;

import acme.storefront.AcmeService;
import acme.storefront.Configutron;
import acme.storefront.Headetron;
import acme.storefront.instrumentation.JavaAgentFacade;
import acme.storefront.serviceproxy.CouponServiceProxy;
import acme.storefront.serviceproxy.PlanServiceProxy;

import com.newrelic.api.agent.Trace;
import jodd.madvoc.meta.In;
import jodd.madvoc.meta.MadvocAction;
import jodd.madvoc.meta.Out;
import jodd.madvoc.meta.RestAction;
import jodd.petite.meta.PetiteBean;
import jodd.madvoc.ScopeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


/**
 * Created by gabe on 2/28/17.
 */

@MadvocAction
@PetiteBean
public class BrowsePlansAction {
    private static final Logger logger = LogManager.getLogger(BrowsePlansAction.class);
    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    @Out ArrayList plans;

    @RestAction("/browse/plans")
    @Trace(dispatcher = true)
    public void browsePlans() throws IOException {
        logger.debug("Starting Browse plans transaction");
        AcmeService.checkTimingHeaders();

        String username = (String) sessionMap.get("username");
        if(username != null && !username.equals("")) {
            Headetron.setHeader("X-TELCO-USERNAME", username);
        }
        String userid = (String) sessionMap.get("userid");
        if(userid != null && !userid.equals("")) {
            logger.debug("Adding X-TELCO-USERID: ("+ userid +")");
            Headetron.setHeader("X-TELCO-USERID", userid);
        }

        String coretronRoot = Configutron.getValue("coretron_URL");
        logger.debug("coretron: "+ coretronRoot);
        CouponServiceProxy couponProxy = new CouponServiceProxy(coretronRoot);
        couponProxy.getCoupons();

        String planRoot = Configutron.getValue("plan_URL");
        logger.debug("plan: "+ planRoot);
        PlanServiceProxy planProxy = new PlanServiceProxy(planRoot);
        plans = planProxy.getPlans();

        JavaAgentFacade.addCustomParameter("BrowsePlansResult","success");
        logger.debug("Finished Browse plans transaction");
    }
}

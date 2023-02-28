package acme.storefront.action;

import acme.storefront.AcmeService;
import acme.storefront.Configutron;
import acme.storefront.Headetron;
import acme.storefront.instrumentation.JavaAgentFacade;
import acme.storefront.serviceproxy.CouponServiceProxy;
import acme.storefront.serviceproxy.PlanServiceProxy;

import com.newrelic.api.agent.Trace;
import jodd.madvoc.meta.MadvocAction;
import jodd.madvoc.meta.Out;
import jodd.madvoc.meta.In;
import jodd.madvoc.meta.RestAction;
import jodd.petite.meta.PetiteBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jodd.madvoc.ScopeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by alarson on 5/7/17.
 */

@MadvocAction
@PetiteBean
public class BrowsePlanAction {
    private static final Logger logger = LogManager.getLogger(BrowsePlanAction.class);
    @Out
    HashMap<String,String> plan;

    @In
    String id;

    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    @RestAction("/browse/plans/${id}")
    @Trace(dispatcher = true)
    public String browsePlan() throws IOException {
        logger.debug("Starting Browse Plan transaction");
        String planId = id.toString();
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
        couponProxy.getCoupon(planId);

        String planRoot = Configutron.getValue("plan_URL");
        logger.debug("plan: "+ planRoot);
        PlanServiceProxy planProxy = new PlanServiceProxy(planRoot);
        plan = planProxy.getPlan(planId);

        JavaAgentFacade.addCustomParameter("BrowsePlanResult","success");
        logger.debug("Finished Browse Plan transaction");
        return "/browse/plan.jsp";
    }
}

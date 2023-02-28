package acme.storefront.action;

import acme.storefront.AcmeService;
import acme.storefront.Configutron;
import acme.storefront.Headetron;
import acme.storefront.instrumentation.JavaAgentFacade;
import acme.storefront.serviceproxy.CouponServiceProxy;

import com.newrelic.api.agent.Trace;
import jodd.madvoc.meta.MadvocAction;
import jodd.madvoc.meta.Out;
import jodd.madvoc.meta.In;
import jodd.madvoc.meta.RestAction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jodd.madvoc.ScopeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MadvocAction
public class ValidateCouponAction {
    private static final Logger logger = LogManager.getLogger(CouponServiceProxy.class);
    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    @In
    String id;

    @Out String result;

    @RestAction("/coupons/${id}/isvalid")
    @Trace(dispatcher = true)
    public String validateCoupon() throws IOException {
        try {
            logger.debug("ValidateCouponAction.validateCoupon: Validating coupon...");

            AcmeService.checkTimingHeaders();

            logger.debug("Checking headers...");
            String username = (String) sessionMap.get("username");
            if(username != null && !username.equals("")) {
                Headetron.setHeader("X-TELCO-USERNAME", username);
            }
            String userid = (String) sessionMap.get("userid");
            if(userid != null && !userid.equals("")) {
                logger.debug("ValidateCouponAction.validateCoupon: Adding X-TELCO-USERID: ("+ userid +")");
                Headetron.setHeader("X-TELCO-USERID", userid);
            }

            String coretronRoot = Configutron.getValue("coretron_URL");
            CouponServiceProxy couponProxy = new CouponServiceProxy(coretronRoot);

            logger.debug("ValidateCouponAction.validateCoupon: Sending reqeuest to " + coretronRoot);
            result = couponProxy.isValid(id.toString());

            logger.debug("ValidateCouponAction.validateCoupon: Validation Complete. Result - success.");

            return "/coupons/isValid.jsp";
        }
        catch(IOException e) {
            logger.error("Error validating coupon: " +e.getMessage());
            logger.debug("ValidateCouponAction.validateCoupon: Validation Complete. Result - failure.");
            logger.info("Returning HTTP 500 -- Internal Server Error");
            throw e;
        }
    }
}

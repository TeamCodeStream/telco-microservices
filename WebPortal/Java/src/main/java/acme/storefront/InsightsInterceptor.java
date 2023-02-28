package acme.storefront;

import acme.storefront.instrumentation.JavaAgentFacade;
import jodd.madvoc.ActionRequest;
import jodd.madvoc.ScopeType;
import jodd.madvoc.interceptor.BaseActionInterceptor;
import jodd.madvoc.meta.In;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by gabe on 5/24/17.
 */


public class InsightsInterceptor extends BaseActionInterceptor {
    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    private ActionRequest actionRequest;

    public Object intercept(ActionRequest actionRequest) throws Exception {
        this.actionRequest = actionRequest;

        HttpServletRequest servletRequest = actionRequest.getHttpServletRequest();
        HttpSession session = servletRequest.getSession();

        String username = (String) session.getAttribute("username");
        if(username != null && !username.equals("")) {
            JavaAgentFacade.addCustomParameter("username", username);
        }
        String userid = (String) session.getAttribute("userid");
        if(userid != null && !userid.equals("")) {
            JavaAgentFacade.addCustomParameter("userid", userid);
        }

        return invokeRequest();
    }

    private Object invokeRequest() throws Exception {
        return actionRequest.invoke();
    }
}



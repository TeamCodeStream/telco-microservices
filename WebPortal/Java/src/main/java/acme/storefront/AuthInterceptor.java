package acme.storefront;

import jodd.joy.auth.AuthUtil;
import jodd.madvoc.ActionRequest;
import jodd.madvoc.interceptor.BaseActionInterceptor;
import jodd.servlet.DispatcherUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by alarson on 5/8/17.
 */

//TODO: Incomplete
public class AuthInterceptor extends BaseActionInterceptor {
    private ActionRequest actionRequest;

    public Object intercept(ActionRequest actionRequest) throws Exception {
        this.actionRequest = actionRequest;

        HttpServletRequest servletRequest = actionRequest.getHttpServletRequest();
        HttpSession session = servletRequest.getSession();
        if (AuthUtil.getUserSession(session) != null) {
            return actionRequest.invoke();
        }

        servletRequest.setAttribute("path", DispatcherUtil.getUrl(servletRequest));

        return "/login";
    }

    private Object invokeRequest() throws Exception {
        return actionRequest.invoke();
    }
}

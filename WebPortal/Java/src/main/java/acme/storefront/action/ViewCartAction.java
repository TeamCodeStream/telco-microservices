package acme.storefront.action;

import acme.storefront.AcmeService;
import acme.storefront.instrumentation.JavaAgentFacade;
import com.newrelic.api.agent.Trace;
import jodd.madvoc.ScopeType;
import jodd.madvoc.meta.*;

import java.io.IOException;
import java.util.Map;


/**
 * Created by alarson on 5/8/17.
 */

@MadvocAction
public class ViewCartAction {

    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    @RestAction("/shoppingcart")
    @Trace(dispatcher = true)
    public String view() throws IOException {
        AcmeService.checkTimingHeaders();
        String username = (String) sessionMap.get("username");
        String userid = (String) sessionMap.get("userid");

            if(username != null && !username.equals("")) {
                JavaAgentFacade.addCustomParameter("username", username);
                if(userid != null && !userid.equals("")) {
                    JavaAgentFacade.addCustomParameter("userid", userid);
                }
                JavaAgentFacade.addCustomParameter("ViewedCart","success");
                return "/purchase/cart.jsp";
            }else{
                sessionMap.put("redirect_after_login","/purchase/cart.jsp");
                JavaAgentFacade.addCustomParameter("ViewedCart","redirected to login");
                return "redirect:/login.jsp";
            }


    }


}

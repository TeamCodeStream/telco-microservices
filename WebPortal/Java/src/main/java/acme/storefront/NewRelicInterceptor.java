package acme.storefront;
import jodd.madvoc.ScopeType;
import jodd.madvoc.ActionConfig;
import jodd.madvoc.ActionRequest;
import jodd.madvoc.interceptor.BaseActionInterceptor;

import java.util.Enumeration;

public class NewRelicInterceptor extends BaseActionInterceptor {
    private ActionRequest actionRequest;

    public Object intercept(ActionRequest actionRequest) throws Exception {
        this.actionRequest = actionRequest;
        // NewRelicLogger.setTransactionName("web", txName);

        // Add all
        Enumeration<String> iter = actionRequest.getHttpServletRequest().getHeaderNames();
        while(iter.hasMoreElements()){
            String name = iter.nextElement().toUpperCase();
            if(name.contains("X-DEMOTRON")){
                String value = actionRequest.getHttpServletRequest().getHeader(name);
                Headetron.setHeader(name,value);
            }
        }

        return invokeRequest();
    }


    private String getTxName() {
        return getActionPath(getActionConfig(actionRequest));
    }

    private ActionConfig getActionConfig(ActionRequest actionRequest) {
        return actionRequest.getActionConfig();
    }

    private String getActionPath(ActionConfig actionConfig) {
        return actionConfig.getActionPath();
    }

    private Object invokeRequest() throws Exception {
        return actionRequest.invoke();
    }
}


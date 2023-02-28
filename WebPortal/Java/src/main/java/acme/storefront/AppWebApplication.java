package acme.storefront;

import acme.storefront.action.IndexAction;
import jodd.joy.core.DefaultAppCore;
import jodd.joy.core.DefaultWebApplication;
import jodd.madvoc.component.MadvocConfig;
import jodd.madvoc.interceptor.ServletConfigInterceptor;

public class AppWebApplication extends DefaultWebApplication {
    @Override
    protected DefaultAppCore createAppCore() {
        return new MyAppCore();
    }

    protected void init(MadvocConfig madvocConfig, javax.servlet.ServletContext servletContext) {
        madvocConfig.getRootPackages().addRootPackageOf(IndexAction.class);

        madvocConfig.setDefaultInterceptors(new Class[]{ServletConfigInterceptor.class, NewRelicInterceptor.class, InsightsInterceptor.class});
        super.init(madvocConfig, servletContext);
    }
}
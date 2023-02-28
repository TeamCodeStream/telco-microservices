package acme.storefront.action;

import acme.storefront.AcmeService;
import com.newrelic.api.agent.Trace;
import jodd.madvoc.meta.Action;
import jodd.madvoc.meta.MadvocAction;

import java.io.IOException;

@MadvocAction
public class IndexAction {
    @Action
    @Trace(dispatcher = true)
    public void view() throws IOException {
        AcmeService.checkTimingHeaders();
    }
}
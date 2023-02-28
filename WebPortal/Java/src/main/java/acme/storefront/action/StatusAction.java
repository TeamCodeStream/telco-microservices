package acme.storefront.action;

import com.newrelic.api.agent.Trace;
import jodd.madvoc.meta.MadvocAction;
import jodd.madvoc.meta.RestAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gabe on 1/22/16.
 */

@MadvocAction
public class StatusAction {
    private static final Logger logger = LogManager.getLogger(StatusAction.class);
    @RestAction("/status/check")
    @Trace(dispatcher = true)
    public String check() {
        logger.debug("Status check OK");
        return "text: ok";
    }

}
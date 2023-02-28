package acme.storefront.action;

import acme.storefront.AcmeService;
import acme.storefront.Configutron;
import acme.storefront.Headetron;
import acme.storefront.HttpRequest;
import acme.storefront.instrumentation.JavaAgentFacade;

import com.newrelic.api.agent.Trace;
import jodd.json.JsonParser;
import jodd.madvoc.ScopeType;
import jodd.madvoc.meta.In;
import jodd.madvoc.meta.MadvocAction;
import jodd.madvoc.meta.RestAction;
import jodd.madvoc.meta.Out;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by alarson on 5/8/17.
 */
@MadvocAction
public class PurchaseAction {
    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    private static final JsonParser JSON = new JsonParser();

    @Out String confirmationNumber;

    @In
    Float shipping;

    @In
    Float tax;

    @In
    Float taxRate;

    @In
    Integer itemCount;

    @In
    String invoiceNumber;

    @In
    Float itemsTotal;

    @In
    Float grandTotal;

    private static final Logger logger = LogManager.getLogger(PurchaseAction.class);


    @RestAction(value = "/checkout", method="POST")
    @Trace(dispatcher = true)
    public String checkout() throws IOException {
        logger.debug("Starting Checkout transaction");

        AcmeService.checkTimingHeaders();

        String shippingInfo = "Shipping: " + shipping;
        shippingInfo += " Tax rate: " + taxRate;
        shippingInfo += " Tax: " + tax;
        logger.debug(shippingInfo);
        JavaAgentFacade.addCustomParameter("PurchasedCartShipping",shipping);
        JavaAgentFacade.addCustomParameter("PurchasedCartTax", tax);
        JavaAgentFacade.addCustomParameter("PurchasedCartTaxRate", taxRate);
        JavaAgentFacade.addCustomParameter("PurchasedCartItemCount", itemCount);
        JavaAgentFacade.addCustomParameter("PurchasedCartItemsTotal", itemsTotal);
        JavaAgentFacade.addCustomParameter("PurchasedCartGrandTotal", grandTotal);

        String username = (String) sessionMap.get("username");
        if(username != null && !username.equals("")) {
            JavaAgentFacade.addCustomParameter("username", username);
        }
        String userid = (String) sessionMap.get("userid");
        if(userid != null && !userid.equals("")) {
            JavaAgentFacade.addCustomParameter("userid", userid);
        }

        String postJson = "{ \"shipping\" : \"" + shipping + "\", "
                + "\"tax\" : \""+ tax +"\", "
                + "\"item_count\" : \""+ itemCount +"\", "
                + "\"items_total\" : \""+ itemsTotal +"\", "
                + "\"grand_total\" : \""+ grandTotal +"\" }";

        String url = Configutron.getValue("fulfillment_URL");
        if (url !=null) {
            url += "/purchaseCart";
            if(username != null && !username.equals("")) {
                Headetron.setHeader("X-TELCO-USERNAME", username);
            }
            if(userid != null && !userid.equals("")) {
                logger.debug("Adding X-TELCO-USERID: ("+ userid +")");
                Headetron.setHeader("X-TELCO-USERID", userid);
            }

            Request request = HttpRequest.createPostRequest(url);
            request.bodyString(postJson, ContentType.APPLICATION_JSON);
            String response = request.execute().returnContent().asString();
            String purchase_status = "success";
            JavaAgentFacade.addCustomParameter("Purchase_Status",purchase_status);

            Map parsedJson = JSON.parse(response);
            String SuccessStatus = (String) parsedJson.get("success");
            if (SuccessStatus.toLowerCase().equals("true")) {
                logger.debug("Purchase successful");
                JavaAgentFacade.addCustomParameter("Purchase_Status", "Successful");
            } else {
                logger.debug("Purchase NOT successful");
                JavaAgentFacade.addCustomParameter("Purchase_Status", "Not Successful");
            }
        }

       logger.debug("Finishing Checkout transaction");

        return "/purchase/confirmation.jsp";
    }
}

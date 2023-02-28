package acme.storefront;

import acme.storefront.Configutron;
import acme.storefront.serviceproxy.CouponServiceProxy;

import jodd.json.JsonParser;

import java.util.Map;
import java.util.Random;

/**
 * Created by jmartinez on 4/20/17.
 */
public class AcmeService {
    private static final JsonParser JSON = new JsonParser();

    public static void checkTimingHeaders(){
        // TODO - move to better location?
        if(Headetron.getHeader("X-DEMOTRON-APM-TIMINGS") != null){
            String json = Headetron.getHeader("X-DEMOTRON-APM-TIMINGS");
            Map parsedJson = JSON.parse(json);

            double time = (double) parsedJson.get("WebPortal") * 1000;
            try {
                Thread.sleep((int)time);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
    }

    public static String getPromoServiceUrl(){
        String coretronRoot = Configutron.getValue("coretron_URL");
        CouponServiceProxy couponProxy = new CouponServiceProxy(coretronRoot);
        return couponProxy.getServiceUri();
    }

    public static String randomBrowserErrors(){
        String errorDiv = new String();
        Random randomizer = new Random();
        int probability = randomizer.nextInt(100);

        if (probability < 20) {
            errorDiv = "<div class=\"ERRORS\" data-error-type=\"referenceError\" data-error-message=\"\">";
        } else if (probability >= 20 && probability < 40) {
            errorDiv = "<div class=\"ERRORS\" data-error-type=\"evalError\" data-error-message=\"\">";
        } else if (probability >= 40 && probability < 60) {
            errorDiv = "<div class=\"ERRORS\" data-error-type=\"syntaxError\" data-error-message=\"\">";
        }else if (probability >= 60 && probability < 80) {
            errorDiv = "<div class=\"ERRORS\" data-error-type=\"typeError\" data-error-message=\"\">";
        } else {
            errorDiv = "<div class=\"ERRORS\" data-error-type=\"rangeError\" data-error-message=\"\">";
        }
        return errorDiv;
    }


    public static String jquery(){
        String errorDiv = new String();
        Random randomizer = new Random();
        int probability = randomizer.nextInt(100);
        if (probability < 50 ) {
            errorDiv = "<div class=\"ERRORS\" data-error-type=\"throwJQuery\" data-error-message=\"\">";
        }
        else {
            errorDiv = "<div class=\"ERRORS\" data-error-type=\"throwJQuery2\" data-error-message=\"\">";
        }
        return errorDiv;
    }

    public static String ie6BrowserErrors(){

        return "<div class=\"ERRORS\" data-error-type=\"ie6error\" data-error-message=\"\">";
    }

    public static String couponProcessingError(){
        return "<div class=\"ERRORS\" data-error-type=\"couponError\" data-error-message=\"Timed out loading <script> with source \"./js/couponSpecial.js?brid=2&brver=60.0&bridua=2&bds=1&tstype=128refD=3&dvp_evl=1&ctx=971108&cmp=20554793&sid=4104518&plc=210689871&crt=98458080&btreg=414486375&adsrv=1&advid=8025626\">";
    }

}

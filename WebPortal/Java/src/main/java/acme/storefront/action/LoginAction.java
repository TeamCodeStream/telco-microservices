package acme.storefront.action;

import acme.storefront.AcmeService;
import acme.storefront.Configutron;
import acme.storefront.HttpRequest;
import acme.storefront.InvalidCharacterException;
import acme.storefront.instrumentation.JavaAgentFacade;

import com.newrelic.api.agent.Trace;
import jodd.joy.auth.AuthUtil;
import jodd.joy.madvoc.action.AppAction;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import jodd.madvoc.ScopeType;
import jodd.madvoc.meta.Action;
import jodd.madvoc.meta.In;
import jodd.madvoc.meta.MadvocAction;
import jodd.madvoc.meta.Out;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import jodd.madvoc.meta.InterceptedBy;

@MadvocAction
public class LoginAction extends AppAction {
    private static final Logger logger = LogManager.getLogger(LoginAction.class);

    private static final JsonParser JSON = new JsonParser();

    private int MAX_PASSWORD_LENGTH = 10;
    private int MAX_USERNAME_LENGTH = 30;

    @In(scope = ScopeType.SERVLET)
    Map<String, Object> sessionMap;

    @In
    String username;

    @In
    String password;

    @Out(scope = ScopeType.SESSION, value = AuthUtil.AUTH_SESSION_NAME)
    String userSession;

    @Action(method = "GET")
    @Trace(dispatcher = true)
    public void view()  {
    }

    @Action(value = "/login", method = "POST")
    @Trace(dispatcher = true)
    public String login() throws IOException, InvalidCharacterException {
        logger.debug("LogicAction.login: HTTP Action: login");

        try{
            logger.debug("LoginAction.login: Checking for headers");
            AcmeService.checkTimingHeaders();
            JavaAgentFacade.addCustomParameter("PotentialUserName", username);

            if (validateUser()){
                JavaAgentFacade.addCustomParameter("username", username);
                submitCredentialsToLoginService();

                String redirect = (String) sessionMap.get("redirect_after_login");
                if(redirect != null && redirect.equals("/purchase/cart.jsp")){
                    sessionMap.remove("redirect_after_login");
                    return REDIRECT + "/purchase/cart.jsp";
                }else {
                    return REDIRECT + "/index.html";
                }
            } else {
                return "<PLACEHOLDER FOR ERROR PAGE>";
            }
        }catch(Exception ex){
            logger.error("Caught exception: " + ex.getMessage());
            logger.info("Returning HTTP 500 -- Internal Server Error");
            throw ex;
        }
    }

    private String fetchUserId(String json){
        Map parsedJson = JSON.parse(json);
        String userIdFromLoginService = (String) parsedJson.get("userid");
        String userIdThatWillNotMatchLogFilteringRuleForCreditCards
                = Integer.toString(Math.abs(userIdFromLoginService.hashCode()));
        return userIdThatWillNotMatchLogFilteringRuleForCreditCards;
    }

    private boolean validateUser() throws IOException, InvalidCharacterException {
        logger.debug("LoginAction.validateUser: User validation starting");

        boolean validCredentials = false;

        logger.debug("LoginAction.validateUser: validating username is not empty");
        if (username == null || username.isEmpty()) {
            JavaAgentFacade.addCustomParameter("LoginResult","NullPointerException");
            logger.warn("Empty username received");
            logger.info("User validation result - failed");
            throw new NullPointerException("Username cannot be null");
        } 
        
        logger.debug("LoginAction.validateUser: validating password is not empty");
        if (password == null || password.isEmpty()){
            JavaAgentFacade.addCustomParameter("LoginResult","NullPointerException");
            logger.warn("Empty password received");
            logger.info("User validation result - failed");
            throw new NullPointerException("Password cannot be null");
        } 
        
        logger.debug(String.format("LoginAction.validateUser: validating password length is below maximum (%d characters)", MAX_PASSWORD_LENGTH));
        if (password.length() > MAX_PASSWORD_LENGTH){
            JavaAgentFacade.addCustomParameter("LoginResult","IndexOutOfBoundsException");
            logger.warn(String.format("The provided password exceeded the maximum password length (%d / %d)", password.length(), MAX_PASSWORD_LENGTH));
            logger.info("User validation result - failed");
            throw new IndexOutOfBoundsException("The provided password exceeded the max length of: " + MAX_PASSWORD_LENGTH);
        } 
        
        logger.debug(String.format("LoginAction.validateUser: validating username length is below maximum (%d characters)", MAX_USERNAME_LENGTH));
        if (username.length() > MAX_USERNAME_LENGTH) {
            JavaAgentFacade.addCustomParameter("LoginResult","IndexOutOfBoundsException");
            logger.warn(String.format("The provided username exceeded the maximum username length (%d / %d)", username.length(), MAX_USERNAME_LENGTH));
            logger.info("User validation result - failed");
            throw new IndexOutOfBoundsException("The provided username exceeded the max length of: " + MAX_USERNAME_LENGTH);
        } 
        
        logger.debug("LoginAction.validateUser: validating username contains only valid characters -- ^[a-zA-Z0-9@\\.]*$");
        if (!username.matches("^[a-zA-Z0-9@\\.]*$")){
            JavaAgentFacade.addCustomParameter("LoginResult","InvalidCharacter");
            JavaAgentFacade.addCustomParameter("hostname","frontend-3");
            logger.warn(String.format("Invalid character detected in username login"));
            logger.info("User validation result - failed");
            throw new InvalidCharacterException("Invalid character detected in username");
        }

        logger.info("User validation result - success");
        JavaAgentFacade.addCustomParameter("LoginResult","Success");
        validCredentials = true;

        return validCredentials;
    }

    private String submitCredentialsToLoginService() throws IOException {
        logger.debug("Submitting credentials...");
        String url = Configutron.getValue("login_URL");
        logger.debug("Login URL: " + url);
        if(url != null) {
            Map<String, String> object;
            object = new HashMap<String, String>();
            object.put("username", username);
            object.put("password", password);
            JsonSerializer jsonSerializer = new JsonSerializer();
            String postJson = jsonSerializer.serialize(object);

            url += "/login";
            Request request = HttpRequest.createPostRequest(url);
            request.bodyString(postJson, ContentType.APPLICATION_JSON);
            request.connectTimeout(1000);
            request.socketTimeout(1000);

            logger.debug("Sending request to " + url);
            String response = request.execute().returnContent().asString();
            logger.debug(String.format("Response received. Content: %s", response));

            String userid = fetchUserId(response);
            logger.debug("user ID: " + userid);
            JavaAgentFacade.addCustomParameter("userid", userid);

            sessionMap.put("userid", userid);
            sessionMap.put("username", username);
            userSession = username;

            if (password.equalsIgnoreCase("expired")) {
                JavaAgentFacade.addCustomParameter("LoginResult", "ExpiredCredentials");
                throw new RuntimeException("Credentials have expired");
            }
        }
        return null;
    }
}

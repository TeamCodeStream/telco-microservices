package acme.storefront.instrumentation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JavaAgentFacade {
    private static Class newrelicClass = null;
    private static Object newrelic = null;

    private static boolean init(){
        boolean initialized = false;
        try {
            if (newrelicClass == null || newrelic == null){
                newrelicClass = JavaAgentFacade.class.getClassLoader().loadClass("com.newrelic.api.agent.NewRelic");
                newrelic = newrelicClass.newInstance();
            }
            initialized = true;
        } catch (ClassNotFoundException e) {

        } catch (IllegalAccessException e) {

        } catch (InstantiationException e) {

        }
        return initialized;
    }

    public static void addCustomParameter( String key, Object value){
        if (init()){
            try {
                Method addCustomParameter;
                if (value instanceof Number){
                    addCustomParameter = newrelicClass.getMethod("addCustomParameter",new Class<?>[] { String.class, Number.class });
                }
                else{
                    addCustomParameter = newrelicClass.getMethod("addCustomParameter",new Class<?>[] { String.class, String.class });
                }
                addCustomParameter.invoke(newrelic,new Object[] { key ,value });
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getBrowserTimingHeader(){

        String response = noParamBrowserMethod("getBrowserTimingHeader");

        if (response == null){
            response = "";
        }
        return response;
    }
    public static String getBrowserTimingFooter(){

        String response = noParamBrowserMethod("getBrowserTimingFooter");

        if (response == null){
            response = "";
        }

        return response;
    }

    private static String noParamBrowserMethod(String param){
        Object o = null;
        if (init()){
            try {
                Method getBrowserTiming = newrelicClass.getMethod(param);
                o = getBrowserTiming.invoke(newrelic);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return (String) o;
    }
}

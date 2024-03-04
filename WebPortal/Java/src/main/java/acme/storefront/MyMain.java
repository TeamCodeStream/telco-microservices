package acme.storefront;

import acme.storefront.action.report.UserDataManager;
import com.newrelic.api.agent.Trace;

import java.util.List;

public class MyMain {
    public static void main(String args[]) {
        System.out.println("hello");
        MyMain main = new MyMain();

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                main.runApp();
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                    // Handle interruption here or re-interrupt the thread
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();
    }

    private UserDataManager usrmgr = new UserDataManager();

    @Trace(dispatcher = true)
    public void runApp() {
        UserDataManager.UserView user1 = usrmgr.getUserById("abcd1");
        System.out.println("user 1 " + user1);
        List<UserDataManager.UserView> userViewList = usrmgr.getUserViewByState("MA");
        System.out.println(userViewList);
    }
}

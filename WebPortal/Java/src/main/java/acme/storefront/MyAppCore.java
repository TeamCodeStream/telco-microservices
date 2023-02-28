package acme.storefront;

import jodd.joy.core.DefaultAppCore;

public class MyAppCore extends DefaultAppCore {
    @Override
    protected void resolveAppDir(String classPathFileName) {
        super.resolveAppDir(classPathFileName);
        isWebApplication = true;
    }

    @Override
    protected void startDb(){
        this.useDatabase = false;
        super.startDb();
    }
}

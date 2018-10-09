package com.apporio.demotaxiappdriver;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.apporio.demotaxiappdriver.analytics.MyApplication;
import com.apporio.demotaxiappdriver.manager.RideSession;
import com.apporio.demotaxiappdriver.manager.SessionManager;
import com.onesignal.OneSignal;

/**
 * Created by samirgoel3@gmail.com on 6/5/2017.
 */

public class MainApplication extends MyApplication {


    private static RideSession rideSession = null ;
    private static SessionManager sessionManager = null ;
    private static Context context ;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this ;
        OneSignal.startInit(getApplicationContext())
                .autoPromptLocation(true)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

    }

    public static RideSession getRideSession(){
        if(rideSession == null){
            rideSession = new RideSession(context);
            return rideSession ;
        }else{
            return rideSession ;
        }

    }



    public static SessionManager getSessionManager(){
        if(sessionManager == null){
            sessionManager = new SessionManager(context);
            return sessionManager ;
        }else{
            return sessionManager ;
        }

    }



    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }
}

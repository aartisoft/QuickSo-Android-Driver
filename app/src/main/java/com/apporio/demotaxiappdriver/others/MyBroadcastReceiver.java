package com.apporio.demotaxiappdriver.others;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.apporio.demotaxiappdriver.Config;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadcastReceiver";
    PowerManager pm;

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {

        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        callActivities(context, intent);
    }

    @SuppressLint("LongLogTag")
    private void callActivities(Context context, Intent intent) {
        String ride_status = intent.getExtras().getString("ride_status");
        String ride_id = intent.getExtras().getString("ride_id");
        Log.d("**BROADCAST++RIDE_STATUS", ride_status);


        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wl.acquire(30 * 1000);
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
        lock.disableKeyguard();

        Log.d("" + TAG, "Working ");

        Intent i = new Intent();

        if (ride_status.equals("1")) {  //   ride booked for normal type
            i.setClassName("com.apporio.demotaxiappdriver", "com.apporio.demotaxiappdriver.ReceivePassengerActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("" + Config.IntentKeys.RIDE_ID, ride_id);
            context.startActivity(i);
        } else if (ride_status.equals("10")) {  //  ride booked for rental type
            i.setClassName("com.apporio.demotaxiappdriver", "com.apporio.demotaxiappdriver.ReceiveRentalPassengerActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("" + Config.IntentKeys.RIDE_ID, ride_id);
            context.startActivity(i);
        }


    }


}

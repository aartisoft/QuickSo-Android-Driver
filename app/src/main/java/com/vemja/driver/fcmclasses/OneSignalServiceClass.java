package com.vemja.driver.fcmclasses;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.vemja.driver.AcceptPassActivity;
import com.vemja.driver.ChatActivity;
import com.vemja.driver.Config;
import com.vemja.driver.MainActivity;
import com.vemja.driver.MainApplication;
import com.vemja.driver.NotificationActivity;
import com.vemja.driver.PriceFareActivity;
import com.vemja.driver.ProfileActivity;
import com.vemja.driver.R;
import com.vemja.driver.ReAcceptpassActivity;
import com.vemja.driver.ReceivePassengerActivity;
import com.vemja.driver.ReceiveRentalPassengerActivity;
import com.vemja.driver.RentalTrackRideActivity;
import com.vemja.driver.RideSessionActiveRideEvent;
import com.vemja.driver.RidesActivity;
import com.vemja.driver.SelectedRidesActivity;
import com.vemja.driver.SingletonGson;
import com.vemja.driver.SplashActivity;
import com.vemja.driver.TripHistoryActivity;
import com.vemja.driver.manager.RideSession;
import com.vemja.driver.manager.SessionManager;
import com.vemja.driver.others.Constants;
import com.vemja.driver.trackride.TrackRideActivity;
import com.vemja.driver.urls.Apis;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Locale;


/**
 * Created by samirgoel3@gmail.com on 26-04-2018.
 */

public class OneSignalServiceClass extends NotificationExtenderService {

    private String TAG = "OneSignalServiceClass";
    private int NOTIFICATION_BYPASS_TIME = 60;
    ModelNotification.ModelNewRide modelNewRide = null;
    private int TRACK_RIDE_INTENT = 1, TRIP_HISTORY = 2, LOGOUT = 3, PROMOTIONAL = 4, SPLAHACTIVITY = 5, UPCOMING_TRIP = 6, LATER_REASSIGNED = 7, RECEIPT_EVENT = 8;


    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        Log.d(TAG, "NOTIFICATION RECEIVED --> " + "" + receivedResult.payload.additionalData + "   " + System.currentTimeMillis() / 1000);
        try {
            modelNewRide = SingletonGson.getInstance().fromJson("" + receivedResult.payload.additionalData, ModelNotification.ModelNewRide.class);
            if (modelNewRide.getRide_status().equals("101") || modelNewRide.getRide_status().equals("51") || modelNewRide.getRide_status().equals("104") || modelNewRide.getRide_status().equals("108") || modelNewRide.getRide_status().equals("15") || modelNewRide.getRide_status().equals("19") || modelNewRide.getRide_status().equals("20") || modelNewRide.getRide_status().equals(Config.Status.RIDE_LATER_BOOKING)) {
                // these notification can come any , we should bound these notification to time , so directly call sendNotification method
                EventBus.getDefault().post(new RideSessionActiveRideEvent(modelNewRide.ride_status, modelNewRide.ride_id));

            } else {
                Log.d("" + TAG, "onNotificationProcessing");
                checkNotificationIsValid("" + modelNewRide.timestamp, modelNewRide.ride_id, modelNewRide.ride_status);
            }
        } catch (Exception e) {
            Log.e("" + TAG, "Error in parsing the response of notification " + e.getMessage());
        }
        return showNotification("" + receivedResult.payload.body, "" + modelNewRide.getRide_id(), "" + modelNewRide.getRide_status(), "" + modelNewRide.getTimestamp());
    }

    // the method will by those modification that are older than one minute
    private void checkNotificationIsValid(String notification_timestamp, String ride_id, String ride_status) {
        Log.d("" + TAG, "checkNotificationIsValid");
        if ((System.currentTimeMillis() / 1000) - Long.parseLong("" + notification_timestamp) > NOTIFICATION_BYPASS_TIME) {
            // here we can hold those notification which we need to access even after 60 second delay.
            generateNotification("Please keep you app open in order to get ride request on high priority.", SPLAHACTIVITY);
        } else {
            try {
                handleNotificationResponse(ride_id, ride_status);
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage());
            }
        }
    }


    private void handleNotificationResponse(String ride_id, String ride_status) throws Exception {

        EventBus.getDefault().post(new RideSessionActiveRideEvent(ride_status, ride_id));
        if (ride_status.equals("1") && !Config.ReceiverPassengerActivity && MainApplication.getRideSession().getCurrentRideDetails().get(RideSession.RIDE_ID).equals("")) {


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Config.ReceiverPassengerActivity = true;
                Intent broadcastIntent = new Intent();
                broadcastIntent.putExtra("ride_id", "" + ride_id);
                broadcastIntent.putExtra("ride_status", "" + ride_status);
                broadcastIntent.setAction("com.vemja.driver");
                sendBroadcast(broadcastIntent);
            } else {
                PowerManager pm = (PowerManager) OneSignalServiceClass.this.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                wl.acquire(30 * 1000);
                KeyguardManager keyguardManager = (KeyguardManager) OneSignalServiceClass.this.getSystemService(OneSignalServiceClass.this.KEYGUARD_SERVICE);
                KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
                lock.disableKeyguard();
                OneSignalServiceClass.this.startActivity(new Intent(OneSignalServiceClass.this, ReceivePassengerActivity.class).putExtra("" + Config.IntentKeys.RIDE_ID, ride_id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }


        } else if (ride_status.equals("10") && !Config.RentalReceivepassengerActivity && MainApplication.getRideSession().getCurrentRideDetails().get(RideSession.RIDE_ID).equals("")) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Config.RentalReceivepassengerActivity = true;
                Intent broadcastIntent_rental = new Intent();
                broadcastIntent_rental.putExtra("ride_id", "" + ride_id);
                broadcastIntent_rental.putExtra("ride_status", "" + ride_status);
                broadcastIntent_rental.setAction("com.vemja.driver");
                sendBroadcast(broadcastIntent_rental);
            } else {
                PowerManager pm = (PowerManager) OneSignalServiceClass.this.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                wl.acquire(30 * 1000);
                KeyguardManager keyguardManager = (KeyguardManager) OneSignalServiceClass.this.getSystemService(OneSignalServiceClass.this.KEYGUARD_SERVICE);
                KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
                lock.disableKeyguard();
                OneSignalServiceClass.this.startActivity(new Intent(OneSignalServiceClass.this, ReceiveRentalPassengerActivity.class).putExtra("" + Config.IntentKeys.RIDE_ID, ride_id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        } else if (ride_status.equals("19")) {

        }
    }


    // return false if yo need to show notification
    private boolean showNotification(String message, String ride_id, String ride_status, String notification_timestamp) {
        if (ride_status.equals("101")) { // operator logged in another device
            logoutDriver();
            return true;
        }
        if (ride_status.equals("51")) { // promotional notification
            generateNotification("" + message, PROMOTIONAL);
            return true;
        }
        if (ride_status.equals("20")) { // promotional notification
            generateNotification("" + message, TRACK_RIDE_INTENT);
            return true;
        }
        if (ride_status.equals(Config.Status.RIDE_LATER_BOOKING)) {
            generateNotification("" + message, UPCOMING_TRIP);
            return true;
        }
        if (ride_status.equals(Config.Status.RIDE_LATER_REASSIGNED)) {
            generateNotification("" + message, LATER_REASSIGNED);
            return true;
        }
        if (ride_status.equals("20")) {
            generateNotification("" + message, TRACK_RIDE_INTENT);
            return true;
        }
        if (ride_status.equals("31")) {
            generateNotification("" + message, RECEIPT_EVENT);
            return true;
        }
        if (ride_status.equals("15")) {
            generateNotification("" + message, TRIP_HISTORY);
            return true;
        }
        else {
            if ((System.currentTimeMillis() / 1000) - Long.parseLong("" + notification_timestamp) <= NOTIFICATION_BYPASS_TIME) {
                if (ride_status.equals("" + Config.Status.NORMAL_CANCEL_BY_USER) || ride_status.equals("" + Config.Status.NORMAL_RIDE_CANCEl_BY_ADMIN)) {
                    if (!Constants.is_track_ride_activity_is_open) {
                        MainApplication.getRideSession().clearRideSession();
                        generateNotification("" + message, TRIP_HISTORY);
                    }
                }
            }
            return true;
        }
    }


    private void generateNotification(String message, int IntentType) {

        Intent intent = getIntent(IntentType);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo_100);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.app_logo_100)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setVibrate(pattern)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(0, notificationBuilder.build());
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo_100);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.app_logo_100)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
//                    .setColor(Color.parseColor("#0x008000"))
                    .setSound(alarmSound)
                    .setVibrate(pattern)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private Intent getIntent(int intent_type) {
        if (intent_type == TRACK_RIDE_INTENT) {
            return new Intent(this, TrackRideActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == TRIP_HISTORY) {
            return new Intent(this, TripHistoryActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == LOGOUT) {
            return new Intent(this, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == PROMOTIONAL) {
            return new Intent(this, NotificationActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == TRACK_RIDE_INTENT) {
            return new Intent(this, RentalTrackRideActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == TRIP_HISTORY) {
            return new Intent(this, TripHistoryActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == SPLAHACTIVITY) {
            return null;
        }

        if (intent_type == UPCOMING_TRIP) {
            return new Intent(this, RidesActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == LATER_REASSIGNED) {
            return new Intent(this, ReAcceptpassActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (intent_type == RECEIPT_EVENT) {
            return new Intent(this, PriceFareActivity.class).putExtra("done_ride_id", "" + modelNewRide.ride_id)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            return null;
        }
    }

    private void finishallOtherActivities() {
        try {
            MainActivity.mainActivity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            TrackRideActivity.activity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            TripHistoryActivity.activity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            SelectedRidesActivity.activity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            ProfileActivity.profileActivity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            ReceivePassengerActivity.activity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            AcceptPassActivity.activity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            ChatActivity.activity.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
        try {
            PriceFareActivity.pricefare.finish();
        } catch (Exception E) {
            Log.e("" + TAG, "Exception caught while finish activity " + E.getMessage());
        }
    }


    private void logoutDriver() {

        AndroidNetworking.get(Apis.logout + "?driver_id=" + MainApplication.getSessionManager().getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&driver_token=" + MainApplication.getSessionManager().getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_code=" + Locale.getDefault().getLanguage())
                .setTag(this).setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(final JSONObject jsonObject) {
                        MainApplication.getSessionManager().logoutUser();
                        Intent it = OneSignalServiceClass.this.getApplicationContext().getPackageManager()
                                .getLaunchIntentForPackage(OneSignalServiceClass.this.getApplicationContext().getPackageName());
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        OneSignalServiceClass.this.startActivity(it);
                        finishallOtherActivities();
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }


    class ModelNotification {

        class ModelNewRide {

            /**
             * ride_id : 4039
             * ride_status : 1
             * timestamp : 1524827336
             */

            private String ride_id;
            private String ride_status;
            private int timestamp;

            public String getRide_id() {
                return ride_id;
            }

            public void setRide_id(String ride_id) {
                this.ride_id = ride_id;
            }

            public String getRide_status() {
                return ride_status;
            }

            public void setRide_status(String ride_status) {
                this.ride_status = ride_status;
            }

            public int getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(int timestamp) {
                this.timestamp = timestamp;
            }
        }

    }


}
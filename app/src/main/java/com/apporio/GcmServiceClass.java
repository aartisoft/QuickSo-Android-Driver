package com.apporio;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.apporio.apporiologs.ApporioLog;
import com.apporio.billiondriver.AccuracyEvent;
import com.apporio.billiondriver.Config;
import com.apporio.billiondriver.LocationSession;
import com.apporio.billiondriver.database.DBHelper;
import com.apporio.billiondriver.location.SamLocationRequestService;
import com.apporio.billiondriver.manager.LanguageManager;
import com.apporio.billiondriver.manager.RideSession;
import com.apporio.billiondriver.manager.SessionManager;
import com.apporio.billiondriver.models.restmodels.NewUpdateLatLongModel;
import com.apporio.billiondriver.others.AerialDistance;
import com.apporio.billiondriver.others.FirebaseUtils;
import com.apporio.billiondriver.samwork.ApiManager;
import com.apporio.billiondriver.trackride.EventytrackAccuracy;
import com.apporio.billiondriver.urls.Apis;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;

/**
 * Created by lenovo on 3/11/2018.
 */

public class GcmServiceClass extends GcmTaskService implements ApiManager.APIFETCHER {

    private Handler mHandler = new Handler();
    private static final String TAG = "TimeService";
    SamLocationRequestService sam_location;
    LocationSession app_location_mamanger;
    FirebaseUtils firebaseUtils;
    SessionManager sessionManager;
    LanguageManager languageManager;
    RideSession rideSession;
    DBHelper dbHelper;
    StorageReference storageReference;
    ApiManager apiManager;
    GsonBuilder builder;
    Gson gson;
    private boolean isApiRunnign = false;
    private Timer mTimer = null;

    @Override
    public void onInitializeTasks() {
        // When your package is removed or updated, all of its network tasks are cleared by
        // the GcmNetworkManager. You can override this method to reschedule them in the case of
        // an updated package. This is not called when your application is first installed.
        //
        // This is called on your application's main thread.

        // TODO(developer): In a real app, this should be implemented to re-schedule important tasks.
        Log.e("onInitialize", "Vishal");
        //  Toast.makeText(getApplicationContext(),"Vishal",Toast.LENGTH_SHORT).show();

        app_location_mamanger = new LocationSession(this);
        sam_location = new SamLocationRequestService(this);
        apiManager = new ApiManager(this);
        firebaseUtils = new FirebaseUtils(this);
        rideSession = new RideSession(this);
        sessionManager = new SessionManager(this);
        languageManager = new LanguageManager(this);
        dbHelper = new DBHelper(this);
        builder = new GsonBuilder();
        gson = builder.create();

        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i1) {
        app_location_mamanger = new LocationSession(this);
        sam_location = new SamLocationRequestService(this);
        apiManager = new ApiManager(this);
        firebaseUtils = new FirebaseUtils(this);
        rideSession = new RideSession(this);
        sessionManager = new SessionManager(this);
        languageManager = new LanguageManager(this);
        dbHelper = new DBHelper(this);
        builder = new GsonBuilder();
        gson = builder.create();

        storageReference = FirebaseStorage.getInstance().getReference();

        return super.onStartCommand(intent, i, i1);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                Log.e("onRunTask", "NRUNTASK");

                if (!sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID).equals("")) {
                    updateLocation();
                }
            }

        });

        return 0;
    }

    @Override
    public void onAPIRunningState(int a, String APINAME) {
        if (a == ApiManager.APIFETCHER.KEY_API_IS_STARTED) {
            isApiRunnign = true;
        } else {
            isApiRunnign = false;
        }
    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {
        try{NewUpdateLatLongModel response = gson.fromJson(""+script , NewUpdateLatLongModel.class);
             Toast.makeText(getApplicationContext(), "VishalonRunTask", Toast.LENGTH_SHORT).show();
            sessionManager.setCurrencyCode(""+response.getCurrency_iso_code() , ""+response.getCurrency_unicode());
            sessionManager.setAccuracy(""+response.getApplication_accuracy());


        } catch (Exception e){
            ApporioLog.logE(""+TAG , "Exxception caught while parsing ==>"+e.getMessage());}
    }

    @Override
    public void onFetchResultZero(String script) {

    }

    private void updateLocation() {
        sam_location.executeService(new SamLocationRequestService.SamLocationListener() {
            @Override
            public void onLocationUpdate(Location location) {
                EventBus.getDefault().post(new AccuracyEvent("" + location.getAccuracy()));
                EventBus.getDefault().post(new EventytrackAccuracy("" + location.getAccuracy()));
                try {
                    if (app_location_mamanger.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT).equals("")) {
                        updateLocationToSession(location);
                    } else {
                        if (location.getAccuracy() < Float.parseFloat("" + sessionManager.getUserDetails().get(SessionManager.KEY_accuracy))) {
                            Double distance = Double.parseDouble("" + AerialDistance.aerialDistanceFunctionInMeters(Double.parseDouble(app_location_mamanger.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)), Double.parseDouble(app_location_mamanger.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)), location.getLatitude(), location.getLongitude()));
                            // if distance between two lat long is greater than 100 then only update on firebase and location session
                            if (distance > Double.parseDouble("" + sessionManager.getUserDetails().get(SessionManager.KEY_meter_range))) {
                                updateLocationToSession(location);
                                if (!isApiRunnign) {
                                    apiManager.execution_method_get(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG_BACKGROUND, Apis.BackGroundAppUpdate + "?driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&current_lat=" + location.getLatitude() + "&current_long=" + location.getLongitude() + "&current_location=" + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=" + languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
                                }
                            }

                        }
                    }
                    if (sessionManager.getUserDetails().get(SessionManager.KEY_service_switcher).equals("1")) {
                        firebaseUtils.updateLocation_with_text();
                    }

                } catch (Exception e) {
                    app_location_mamanger.setLocationAddress("" + e.getMessage());
                }
//                    LocationEvent location_event = new LocationEvent();
//                    location_event.setCurrent_speed(location.getSpeed());
//                    location_event.setCurrent_time(location.getTime());
//                    EventBus.getDefault().post(location_event);
            }
        });
    }


    private void updateLocationToSession(Location location) {
        Log.d("****" + TAG, "UpdatingLocationToSession");
        if (location.getBearing() != 0.0) {
            app_location_mamanger.setBearingFactor("" + location.getBearing());
        }
        app_location_mamanger.setLocationLatLong(location);
    }
}

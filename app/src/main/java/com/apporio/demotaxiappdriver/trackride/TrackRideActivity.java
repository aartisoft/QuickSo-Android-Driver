package com.apporio.demotaxiappdriver.trackride;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apporio.apporiologs.ApporioLog;
import com.apporio.demotaxiappdriver.ChatActivity;
import com.apporio.demotaxiappdriver.Config;
import com.apporio.demotaxiappdriver.LocationEvent;
import com.apporio.demotaxiappdriver.LocationSession;
import com.apporio.demotaxiappdriver.PriceFareActivity;
import com.apporio.demotaxiappdriver.R;
import com.apporio.demotaxiappdriver.RideSessionActiveRideEvent;
import com.apporio.demotaxiappdriver.SelectedRidesActivity;
import com.apporio.demotaxiappdriver.SosActivity;
import com.apporio.demotaxiappdriver.SplashActivity;
import com.apporio.demotaxiappdriver.TripHistoryActivity;
import com.apporio.demotaxiappdriver.adapter.ReasonAdapter;
import com.apporio.demotaxiappdriver.database.DBHelper;
import com.apporio.demotaxiappdriver.location.SamLocationRequestService;
import com.apporio.demotaxiappdriver.manager.LanguageManager;
import com.apporio.demotaxiappdriver.manager.RideSession;
import com.apporio.demotaxiappdriver.manager.SessionManager;
import com.apporio.demotaxiappdriver.models.ResultCheck;
import com.apporio.demotaxiappdriver.models.cancelreasoncustomer.CancelReasonCustomer;
import com.apporio.demotaxiappdriver.models.restmodels.NewChangeDropLocationModel;
import com.apporio.demotaxiappdriver.models.restmodels.NewUpdateLatLongModel;
import com.apporio.demotaxiappdriver.models.ridearrived.RideArrived;
import com.apporio.demotaxiappdriver.models.viewrideinfodriver.ViewRideInfoDriver;
import com.apporio.demotaxiappdriver.others.Constants;
import com.apporio.demotaxiappdriver.others.FirebaseChatEvent;
import com.apporio.demotaxiappdriver.others.FirebaseChatUtillistener;
import com.apporio.demotaxiappdriver.others.Maputils;
import com.apporio.demotaxiappdriver.routedrawer.DrawMarker;
import com.apporio.demotaxiappdriver.routedrawer.DrawRouteMaps;
import com.apporio.demotaxiappdriver.samwork.ApiManager;
import com.apporio.demotaxiappdriver.urls.Apis;
import com.apporio.demotaxiappdriver.views.MButton;
import com.apporio.demotaxiappdriver.views.MaterialRippleLayout;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sampermissionutils.AfterPermissionGranted;
import com.sampermissionutils.EasyPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.HashMap;

import morxander.zaman.ZamanUtil;

public class TrackRideActivity extends AppCompatActivity implements OnMapReadyCallback, ApiManager.APIFETCHER {

    private static final String TAG = "TrackRideActivity";
    GoogleMap mGooglemap;
    LocationSession locationSession;
    LanguageManager languageManager;
    RideSession rideSession;
    SessionManager sessionManager;
    TextView customer_info_txt, location_txt, cancel_btn, your_location_txt, meter_txt, customer_phone_txt, connectivity_status, acc_txt, chat_message;
    LinearLayout root, sos, message_layout;
    ImageView dot, pencil_icon;
    ApiManager apiManager;

    MButton trip_status_txt;

    ProgressDialog progressDialog;
    DBHelper dbHelper;
    boolean is_location_updation_running = false;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    float MAIN_BEARING;
    public static Activity activity;
    final Handler mHandeler = new Handler();
    Runnable mRunnable;
    private boolean is_map_loaded = false;
    SamLocationRequestService samLocationRequestService;

    FirebaseChatUtillistener firebaseChatUtillistener;
    private static final int TELEPHONE_PERM = 657;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        apiManager = new ApiManager(this);
        activity = this;
        super.onCreate(savedInstanceState);
        locationSession = new LocationSession(this);
        samLocationRequestService = new SamLocationRequestService(this);
        sessionManager = new SessionManager(this);
        languageManager = new LanguageManager(this);
        rideSession = new RideSession(this);
        progressDialog = new ProgressDialog(this);
        dbHelper = new DBHelper(this);
        progressDialog.setMessage("" + this.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        setContentView(R.layout.activity_track_ride);
        customer_info_txt = (TextView) findViewById(R.id.customer_info_txt);

        trip_status_txt = ((MaterialRippleLayout) findViewById(R.id.trip_status_txt)).getChildView();
        trip_status_txt.setTextSize(15);

        cancel_btn = (TextView) findViewById(R.id.cancel_btn);
        //  cancel_btn.setText(getResources().getString(R.string.TRACK_RIDE_ACTIVITY__cancel));
        //   cancel_btn.setTextSize(15);


        your_location_txt = (TextView) findViewById(R.id.your_location_txt);
        meter_txt = (TextView) findViewById(R.id.meter_txt);
        pencil_icon = (ImageView) findViewById(R.id.pencil_icon);
        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        customer_phone_txt = (TextView) findViewById(R.id.customer_phone_txt);
        connectivity_status = (TextView) findViewById(R.id.connectivity_status);
        root = (LinearLayout) findViewById(R.id.root);
        location_txt = (TextView) findViewById(R.id.location_txt);
        acc_txt = (TextView) findViewById(R.id.acc_txt);
        sos = (LinearLayout) findViewById(R.id.sos);
        dot = (ImageView) findViewById(R.id.dot);
        chat_message = (TextView) findViewById(R.id.chat_message);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        firebaseChatUtillistener = new FirebaseChatUtillistener(rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));
        apiManager.execution_method_get(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG, Apis.updateLatLong + "?driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&current_lat=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT) + "&current_long=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG) + "&current_location=" + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=1");


        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

                if (!Constants.is_main_activity_open) {
                    startActivity(new Intent(TrackRideActivity.this, SplashActivity.class));
                }
            }
        });


        findViewById(R.id.location_changer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("5") || rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")) {
                    openGooglePlaceAPiDialoge();
                }

            }
        });

        findViewById(R.id.call_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    callingTask();
                } catch (Exception e) {
                }
            }
        });


        trip_status_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")) {
                    // run arrived api
                    apiManager.execution_method_get(Config.ApiKeys.KEY_ARRIVED, Apis.arrivedTrip + "?ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) + "&driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=" + languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
                } else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("5")) {
                    // run begin trip api
                    if (location_txt.getText().toString().equals("") || location_txt.getText().toString() == null || location_txt.getText().toString().equals(TrackRideActivity.this.getResources().getString(R.string.TrackRideActivity__set_your_drop_point)) || rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE).equals("0.0")) {
                        Toast.makeText(TrackRideActivity.this, R.string.please_ask_drop_location_from_passenger, Toast.LENGTH_SHORT).show();
                    } else {
                        if (Double.parseDouble("" + locationSession.getLocationDetails().get(LocationSession.KEY_ACCURACY)) > 100.0) {
                            Toast.makeText(TrackRideActivity.this, getString(R.string.ride_started_but_with_low_accuracy) + locationSession.getLocationDetails().get(LocationSession.KEY_ACCURACY), Toast.LENGTH_SHORT).show();
                        } else {
                        }
                        try {
                            String url = "" + Apis.beginTrip + "?ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) + "&driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&begin_lat=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT) + "&begin_long=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG) + "&begin_location=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION) + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=" + languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID);
                            apiManager.execution_method_get(Config.ApiKeys.KEY_BEGIN_TRIP, url.replace(" ", "%20"));
                        } catch (Exception E) {
                        }

                    }
                } else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")) {
                    try {
                        Double distance_travel = Double.parseDouble("" + meter_txt.getText().toString().replace(" km", ""));
                        distance_travel = distance_travel * 1000;
                        if (Double.parseDouble("" + locationSession.getLocationDetails().get(LocationSession.KEY_ACCURACY)) > 100.0) {
                            Toast.makeText(TrackRideActivity.this, getString(R.string.ride_end_but_with_accuracy) + locationSession.getLocationDetails().get(LocationSession.KEY_ACCURACY), Toast.LENGTH_SHORT).show();
                        } else {
                        }

                        HashMap<String, String> data = new HashMap<>();
                        data.put("ride_id", "" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));
                        data.put("driver_id", "" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID));

                        data.put("begin_lat", "" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE));
                        data.put("begin_long", "" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE));
                        data.put("begin_location", "");
                        data.put("end_lat", "" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT));
                        data.put("end_long", "" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG));
                        data.put("end_location", "" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LOCATION_TEXT));
                        data.put("end_time", "" + getArrivalTime());
                        data.put("distance", "" + distance_travel);
                        data.put("driver_token", "" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken));
                        data.put("language_id", "" + languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
                        data.put("lat_long", "" + dbHelper.getRideLocationData("" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)));

                        apiManager.execution_method_post("" + Config.ApiKeys.KEY_END_TRIP, "" + Apis.endTripMeter, data);

                    } catch (Exception e) {
                        Toast.makeText(TrackRideActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        findViewById(R.id.navigation_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isPackageExisted("com.waze")) {
                    showDialogForWazemap();
                } else {
                    if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT) + "," + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG) + "&daddr=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE) + "," + rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE)));
                        startActivity(intent);
                    } else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION) + "&daddr=" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION)));
                        startActivity(intent);
                    } else {
                        Snackbar.make(root, "" + TrackRideActivity.this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__please_start_your_ridr_first), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });


        meter_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDemodialog();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiManager.execution_method_get(Config.ApiKeys.KEY_CANCEL_REASONS, Apis.cancelReason + "?language_id=" + languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
            }
        });

        findViewById(R.id.sos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TrackRideActivity.this, SosActivity.class));
            }
        });


        findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TrackRideActivity.this, ChatActivity.class)
                        .putExtra("ride_id", "" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID))
                        .putExtra("ride_status", "" + trip_status_txt.getText().toString())
                        .putExtra("user_name", "" + rideSession.getCurrentRideDetails().get(RideSession.USER_NAME))
                        .putExtra("user_image", "https://cdn0.iconfinder.com/data/icons/user-pictures/100/matureman1-512.png"));
            }
        });


        message_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TrackRideActivity.this, ChatActivity.class)
                        .putExtra("ride_id", "" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID))
                        .putExtra("ride_status", "" + trip_status_txt.getText().toString())
                        .putExtra("user_name", "" + rideSession.getCurrentRideDetails().get(RideSession.USER_NAME))
                        .putExtra("user_image", "https://cdn0.iconfinder.com/data/icons/user-pictures/100/matureman1-512.png"));
            }
        });


    }

    @AfterPermissionGranted(TELEPHONE_PERM)
    public void callingTask() throws Exception {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CALL_PHONE)) {
            try { // Have permission, do the thing!
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + getIntent().getExtras().getString("customer_phone")));
                if (ActivityCompat.checkSelfPermission(TrackRideActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            } catch (Exception e) {
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.this_app_need_telephony_permission), TELEPHONE_PERM, Manifest.permission.CALL_PHONE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            callingTask();
        } catch (Exception e) {
        }
    }


    private void showDemodialog() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        dialog.setCancelable(true);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_for_meter);

        final EditText demo_meter_edt = (EditText) dialog.findViewById(R.id.demo_meter_edt);
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double km_value = 0.0;

                try {
                    km_value = Double.parseDouble("" + demo_meter_edt.getText().toString());
                    meter_txt.setText("" + (km_value / 1000) + " km");
                } catch (Exception e) {
                    Toast.makeText(TrackRideActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setView() {
        customer_info_txt.setText("" + rideSession.getCurrentRideDetails().get(RideSession.USER_NAME));
        customer_phone_txt.setText("" + rideSession.getCurrentRideDetails().get(RideSession.USER_PHONE));

        if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3") || rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("4")) {
            location_txt.setText("" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION));
            location_txt.setTextColor(Color.parseColor("#2ecc71"));
            dot.setColorFilter(Color.parseColor("#2ecc71"));
            pencil_icon.setVisibility(View.GONE);
        } else {
            location_txt.setText("" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION));
            location_txt.setTextColor(Color.parseColor("#e74c3c"));
            dot.setColorFilter(Color.parseColor("#e74c3c"));
            pencil_icon.setVisibility(View.VISIBLE);
        }
        setviewAccordingToStatus();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FirebaseChatEvent event) {
        try {
            String text = new ZamanUtil(Long.parseLong("" + event.timestamp)).getTime();
            if (text.equals("Just Now") || text.equals("In a few seconds")) {
                showUserMesageWithTimer(event.message);
            }
        } catch (Exception e) {
        }
    }

    private void showUserMesageWithTimer(String message) {
        message_layout.setVisibility(View.VISIBLE);
        chat_message.setText("" + message);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                message_layout.setVisibility(View.GONE);
            }
        }, 5000);

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventytrackAccuracy eventtt) {

        try {
            acc_txt.setText("Acc = " + eventtt.Accuracy);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseChatUtillistener.startChatListening();
        EventBus.getDefault().register(this);
        Constants.is_track_ride_activity_is_open = true;
        try {
            if (is_map_loaded) {
                setView();
            }
            apiManager.execution_method_get(Config.ApiKeys.KEY_VIEW_RIDE_INFO_DRIVER, Apis.viewRideInfoDriver + "?ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=1");

        } catch (Exception e) {
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mHandeler.removeCallbacks(mRunnable);
        firebaseChatUtillistener.stopChatListener();
        Constants.is_track_ride_activity_is_open = false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        is_map_loaded = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGooglemap = googleMap;
        mGooglemap.setBuildingsEnabled(false);


        try {
//            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.midnight_commander_theme));
            googleMap.setMaxZoomPreference(18);
        } catch (Resources.NotFoundException e) {
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGooglemap.setMyLocationEnabled(true);

        mGooglemap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                setView();
                is_map_loaded = true;
            }
        });


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationEvent event) {

        try {
            if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6") && event.getMeter_value() >= Double.parseDouble("" + sessionManager.getUserDetails().get(SessionManager.KEY_TAIL))) { // update route
               // DrawRouteMaps.getInstance(this, 6, R.color.icons_8_muted_green_1).draw(new LatLng(Double.parseDouble(event.getlatitude_string()), Double.parseDouble(event.getLongitude_string())), new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)), Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))), mGooglemap, sessionManager);
            }
        } catch (Exception e) {
            ApporioLog.logE("" + TAG, "(a) Exception caught in onMessage Event ==>" + e.getMessage());
        }



        try {
            double value_in_km = (Double.parseDouble("" + event.getMeter_value()) / 1000);
            value_in_km = Math.round(value_in_km * 100D) / 100D;
            meter_txt.setText("" + value_in_km + " km");
        } catch (Exception e) {
            ApporioLog.logE("" + TAG, "(b) Exception caught in onMessage Event ==>" + e.getMessage());
        }


        try {
            if (event.is_meter_value_cleared()) {
                drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)), Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))), new LatLng(Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)), Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG))), mGooglemap, R.drawable.ic_contact_green, R.drawable.ic_very_small);
            }
        } catch (Exception e) {
            ApporioLog.logE("" + TAG, "(c) Exception caught in onMessage Event ==>" + e.getMessage());
        }
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void ownMessageEvent(MyFirebaseMessagingService.RideEvent event) {
//        if (event.getRideStatus().equals(Config.Status.NORMAL_CANCEL_BY_USER)) {
//            showDialogForCancelation();
//        }
//        if (event.getRideStatus().equals(Config.Status.NORMAL_RIDE_CANCEl_BY_ADMIN)) {
//            showDialogForCancelationViaAdmin();
//        }
//
//    }

    private void showDialogForCancelation() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        dialog.setCancelable(true);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_for_cancel_via_customer);
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rideSession.clearRideSession();
                finaliseAftercancelation();
            }
        });

        dialog.show();
    }


    private void showDialogForCancelationViaAdmin() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        dialog.setCancelable(true);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_for_cancel_via_admin);
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rideSession.clearRideSession();
                finaliseAftercancelation();
            }
        });

        dialog.show();
    }

    public void setviewAccordingToStatus() {

        Log.e("RideSttaus", "" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS));
        try {
            if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("2")) {
                showDialogForCancelation();
            }
            if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")) {
                ////  set view when driver needs to reach over pick up point
                trip_status_txt.setText("" + this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__located));
                drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)), Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))), new LatLng(Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)), Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG))), mGooglemap, R.drawable.ic_contact_green, R.drawable.ic_very_small);
                cancel_btn.setVisibility(View.VISIBLE);
                meter_txt.setVisibility(View.GONE);
                sos.setVisibility(View.GONE);
            }
            if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("5")) {
                trip_status_txt.setText("" + this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__begin));
                cancel_btn.setVisibility(View.VISIBLE);
                meter_txt.setVisibility(View.GONE);
                sos.setVisibility(View.GONE);
                if (rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE).equals("") || rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE) == null || rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE).equals("0.0")) {  // no drop off location
                    mGooglemap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(Double.parseDouble("" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)), Double.parseDouble("" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)))).zoom(17).build()));

                } else {
                    drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)), Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))), new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)), Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))), mGooglemap, R.drawable.dot_green, R.drawable.dot_red);
                }
            }
            if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")) {
                startRunnableProcess();
//            drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))) , new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))),mGooglemap , R.drawable.dot_green , R.drawable.dot_red);
                trip_status_txt.setText("" + this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__end));
                cancel_btn.setVisibility(View.GONE);
                meter_txt.setVisibility(View.VISIBLE);
                sos.setVisibility(View.VISIBLE);
//            startBeginToEndTracking();
            }
        } catch (Exception e) {
            ApporioLog.logE("" + TAG, "Exception caught while setViewAccordingToStatus ==>" + e.getMessage());
        }
    }


    public void drawRoute(LatLng origin, LatLng destination, GoogleMap mMap, int origin_icon, int destination_icon) {
        mGooglemap.clear();
        try {
            // comment draw path

          //  DrawRouteMaps.getInstance(this, 6, R.color.icons_8_muted_green_1).draw(origin, destination, mMap, sessionManager);
            DrawMarker.getInstance(this).draw(mMap, origin, origin_icon, "" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION));
            DrawMarker.getInstance(this).draw(mMap, destination, destination_icon, "" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION));

            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(origin)
                    .include(destination).build();
            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay().getSize(displaySize);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 500, 60));
        } catch (Exception e) {

        }
    }


    public String getArrivalTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        String h = hour + ":" + minutes + ":" + sec;
        String[] h1 = h.split(":");
        int hours = Integer.parseInt(h1[0]);
        int minute = Integer.parseInt(h1[1]);
        int second = Integer.parseInt(h1[2]);
        return "" + (second + (60 * minute) + (3600 * hours));
    }


    @Override
    public void onAPIRunningState(int a, String APINAME) {

        if (a == ApiManager.APIFETCHER.KEY_API_IS_STARTED && !APINAME.equals(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG)) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }


        if (a == ApiManager.APIFETCHER.KEY_API_IS_STARTED) {
            is_location_updation_running = true;
        } else {
            is_location_updation_running = false;
        }

    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {

        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            ResultCheck result_check = gson.fromJson("" + script, ResultCheck.class);

            if (result_check.result.equals("1")) {
                if (APINAME.equals(Config.ApiKeys.KEY_ARRIVED)) {
                    rideSession.setRideStatus("5");
                }
                if (APINAME.equals(Config.ApiKeys.KEY_BEGIN_TRIP)) {
                    rideSession.setRideStatus("6");
                    locationSession.clearMeterValue();
                    apiManager.execution_method_get(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG, Apis.BackGroundAppUpdate + "?driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&current_lat=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT) + "&bearing_factor=" + locationSession.getLocationDetails().get(LocationSession.KEY_BEARING_FACTOR) + "&current_long=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG) + "&current_location=" + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=1");
                }
                if (APINAME.equals(Config.ApiKeys.KEY_END_TRIP)) {
                    rideSession.setRideStatus("7");
                    RideArrived rideArrived = new RideArrived();
                    rideArrived = gson.fromJson("" + script, RideArrived.class);
                    startActivity(new Intent(this, PriceFareActivity.class)
                            .putExtra("amount", rideArrived.getDetails().getAmount())
                            .putExtra("distance", rideArrived.getDetails().getDistance())
                            .putExtra("time", rideArrived.getDetails().getTotTime())
                            .putExtra("customerId", rideSession.getCurrentRideDetails().get(RideSession.USER_ID))
                            .putExtra("ride_id", rideArrived.getDetails().getRideId())
                            .putExtra("done_ride_id", rideArrived.getDetails().getDoneRideId()));
                    finish();
                    rideSession.clearRideSession();
                }
                if (APINAME.equals(Config.ApiKeys.KEY_CANCEL_REASONS)) {
                    CancelReasonCustomer cancelReasonCustomer = gson.fromJson("" + script, CancelReasonCustomer.class);
                    showReasonDialog(cancelReasonCustomer);
                }
                if (APINAME.equals(Config.ApiKeys.KEY_CANCEL_TRIP)) {
                    rideSession.setRideStatus("4");
                    rideSession.clearRideSession();
                    finish();
                    startActivity(new Intent(this, TripHistoryActivity.class));
                }
                if (APINAME.equals("" + Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG)) {
                    NewUpdateLatLongModel response = gson.fromJson("" + script, NewUpdateLatLongModel.class);
                }
                if (APINAME.equals("" + Config.ApiKeys.KEY_CHANGE_DESTINATION)) {
                    NewChangeDropLocationModel drop_change_response = gson.fromJson("" + script, NewChangeDropLocationModel.class);
                    rideSession.setDropLocation("" + drop_change_response.getDetails().getDrop_location(), "" + drop_change_response.getDetails().getDrop_lat(), "" + drop_change_response.getDetails().getDrop_long());
                }
                if (APINAME.equals("" + Config.ApiKeys.KEY_VIEW_RIDE_INFO_DRIVER)) {
                    ViewRideInfoDriver viewRideInfoDriver = gson.fromJson("" + script, ViewRideInfoDriver.class);
                    rideSession.setDropLocation(viewRideInfoDriver.getDetails().getDrop_location(), "" + viewRideInfoDriver.getDetails().getDrop_lat(), "" + viewRideInfoDriver.getDetails().getDrop_long());
                }

                setView();

            } else {
                ApporioLog.logE("" + TAG, "Error While Executing API ");
            }
            Log.d("*****" + APINAME, "" + script);
        } catch (Exception E) {
        }


    }


    @Override
    public void onFetchResultZero(String script) {

    }


    /////////////////// dialog
    public void showReasonDialog(final CancelReasonCustomer cancelReasonCustomer) {

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        dialog.setCancelable(true);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_for_cancel_reason);

        ListView lv_reasons = (ListView) dialog.findViewById(R.id.lv_reasons);
        lv_reasons.setAdapter(new ReasonAdapter(this, cancelReasonCustomer));

        lv_reasons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                apiManager.execution_method_get(Config.ApiKeys.KEY_CANCEL_TRIP, Apis.cancelRide + "?ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) + "&reason_id=" + cancelReasonCustomer.getMsg().get(position).getReasonId());
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void finaliseAftercancelation() {
        try {
            TripHistoryActivity.activity.finish();
        } catch (Exception e) {

        }
        try {
            SelectedRidesActivity.activity.finish();
        } catch (Exception e) {

        }
        rideSession.setRideStatus("18");
        finish();
        startActivity(new Intent(TrackRideActivity.this, TripHistoryActivity.class));
    }


    @Override
    public void onBackPressed() {
        if (!Constants.is_main_activity_open) {
            startActivity(new Intent(TrackRideActivity.this, SplashActivity.class));
        }
        super.onBackPressed();

    }


    private void openGooglePlaceAPiDialoge() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(TrackRideActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                apiManager.execution_method_get("" + Config.ApiKeys.KEY_CHANGE_DESTINATION, "" + Apis.change_drop_location + "drop_lat=" + place.getLatLng().latitude + "&drop_long=" + place.getLatLng().longitude + "&drop_location=" + place.getName() + "&app_id=2" + "&ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("*****", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }


    public void startRunnableProcess() {

        try {
            mHandeler.removeCallbacks(mRunnable);
        } catch (Exception e) {

        }

        try {
            mGooglemap.clear();

            final LatLng POINT_A = new LatLng(Double.parseDouble("" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)), Double.parseDouble("" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE)));
            Maputils.setDestinationmarker(TrackRideActivity.this, mGooglemap, POINT_A, "" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION));

            new SamLocationRequestService(TrackRideActivity.this).executeService(new SamLocationRequestService.SamLocationListener() {
                @Override
                public void onLocationUpdate(Location location) {
                    Double end_lat = 0.0;
                    Double end_lng = 0.0;

                    try {
                        end_lat = location.getLatitude();
                        end_lng = location.getLongitude();

                        // comment draw path
                      //  DrawRouteMaps.getInstance(TrackRideActivity.this, 9, R.color.icons_8_muted_green_1).draw(new LatLng(end_lat, end_lng), new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)), Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))), mGooglemap, sessionManager);


                        //  DrawRouteMaps.getInstance(TrackRideActivity.this , 9 ,R.color.icons_8_muted_green_1).draw(new LatLng(location.getLatitude() , location.getLongitude()), new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))), mGooglemap , sessionManager);

                    } catch (Exception e) {

                    }
                }
            });


            mRunnable = new Runnable() {

                @Override
                public void run() {
                    new SamLocationRequestService(TrackRideActivity.this).executeService(new SamLocationRequestService.SamLocationListener() {
                        @Override
                        public void onLocationUpdate(final Location location) {
                            if (location.getBearing() != 0.0) {
                                MAIN_BEARING = location.getBearing();
                            }
                            Maputils.moverCamera(mGooglemap, new LatLng(location.getLatitude(), location.getLongitude()));
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(),
                                            location.getLongitude())).bearing(MAIN_BEARING)
                                    .tilt(47).zoom(18).build();
                            mGooglemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);
                        }
                    });
                    mHandeler.postDelayed(mRunnable, 3000);
                }
            };
            runOnUiThread(mRunnable);
        } catch (Exception e) {
            ApporioLog.logE("" + TAG, "Caught Exception in start Runnable Process method ==>" + e.getMessage());
        }
    }


    public boolean isPackageExisted(String targetPackage) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


    public void showDialogForWazemap() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        dialog.setCancelable(true);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_for_navigation_types);
        dialog.findViewById(R.id.google_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT) + "," + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG) + "&daddr=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE) + "," + rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE)));
                    startActivity(intent);
                } else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION) + "&daddr=" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION)));
                    startActivity(intent);
                } else {
                    Snackbar.make(root, "" + TrackRideActivity.this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__please_start_your_ridr_first), Snackbar.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.waze_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = "https://waze.com/ul?ll=45.6906304,-120.810983";
                if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")) {
                    s = "https://waze.com/ul?ll=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE) + "," + rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE);
                } else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")) {
                    s = "https://waze.com/ul?ll=" + rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION);
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                startActivity(intent);

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RideSessionActiveRideEvent event) {

        if (event.getRide_status().equals("" + Config.Status.NORMAL_CANCEL_BY_USER)) {
            rideSession.clearRideSession();
            showDialogForCancelation();
        }
        if (event.getRide_status().equals("" + Config.Status.NORMAL_RIDE_CANCEl_BY_ADMIN)) {
            rideSession.clearRideSession();
            showDialogForCancelationViaAdmin();
        }
        if (event.getRide_status().equals("20")) {
            apiManager.execution_method_get(Config.ApiKeys.KEY_VIEW_RIDE_INFO_DRIVER, Apis.viewRideInfoDriver + "?ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=1");
        } else {
            apiManager.execution_method_get(Config.ApiKeys.KEY_VIEW_RIDE_INFO_DRIVER, Apis.viewRideInfoDriver + "?ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=1");

        }
    }


}

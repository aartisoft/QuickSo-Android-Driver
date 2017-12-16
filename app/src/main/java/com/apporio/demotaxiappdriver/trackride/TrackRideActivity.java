package com.apporio.demotaxiappdriver.trackride;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
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

import com.apporio.demotaxiappdriver.ChatActivity;
import com.apporio.demotaxiappdriver.Config;
import com.apporio.demotaxiappdriver.LocationEvent;
import com.apporio.demotaxiappdriver.LocationSession;
import com.apporio.demotaxiappdriver.PriceFareActivity;
import com.apporio.demotaxiappdriver.R;
import com.apporio.demotaxiappdriver.RidesActivity;
import com.apporio.demotaxiappdriver.SelectedRidesActivity;
import com.apporio.demotaxiappdriver.SosActivity;
import com.apporio.demotaxiappdriver.SplashActivity;
import com.apporio.demotaxiappdriver.adapter.ReasonAdapter;
import com.apporio.demotaxiappdriver.database.DBHelper;
import com.apporio.demotaxiappdriver.fcmclasses.MyFirebaseMessagingService;
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
import com.apporio.demotaxiappdriver.others.ChangeLocationEvent;
import com.apporio.demotaxiappdriver.others.ChatModel;
import com.apporio.demotaxiappdriver.others.Constants;
import com.apporio.demotaxiappdriver.others.FirebaseChatEvent;
import com.apporio.demotaxiappdriver.others.FirebaseChatUtillistener;
import com.apporio.demotaxiappdriver.others.Maputils;
import com.apporio.demotaxiappdriver.others.RideSessionEvent;
import com.apporio.demotaxiappdriver.routedrawer.DrawMarker;
import com.apporio.demotaxiappdriver.routedrawer.DrawRouteMaps;
import com.apporio.demotaxiappdriver.samwork.ApiManager;
import com.apporio.demotaxiappdriver.urls.Apis;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.Calendar;
import java.util.List;

import morxander.zaman.ZamanUtil;

public class TrackRideActivity extends AppCompatActivity implements OnMapReadyCallback , ApiManager.APIFETCHER{

    private static final String TAG = "TrackRideActivity";
    GoogleMap mGooglemap;
    LocationSession locationSession;
    LanguageManager languageManager ;
    RideSession rideSession;
    SessionManager sessionManager ;
    TextView customer_info_txt ,location_txt, trip_status_txt, your_location_txt, meter_txt , cancel_btn ,customer_phone_txt , connectivity_status  , acc_txt , chat_message;
    LinearLayout root , sos , message_layout ;
    ImageView dot ;
    ApiManager apiManager ;
    ProgressDialog progressDialog;
    DBHelper dbHelper ;
    boolean  is_location_updation_running   = false  ;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    float MAIN_BEARING  ;
    public static Activity activity ;
    final Handler mHandeler = new Handler();
    Runnable mRunnable ;
    private boolean is_map_loaded = false ;
    SamLocationRequestService samLocationRequestService ;

    FirebaseChatUtillistener firebaseChatUtillistener ;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        apiManager = new ApiManager(this);
        activity = this ;
        super.onCreate(savedInstanceState);
        locationSession = new LocationSession(this);
        samLocationRequestService = new SamLocationRequestService(this);
        sessionManager = new SessionManager(this);
        languageManager = new LanguageManager(this);
        rideSession = new RideSession(this);
        progressDialog = new ProgressDialog(this);
        dbHelper = new DBHelper(this);
        progressDialog.setMessage(""+this.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        setContentView(R.layout.activity_track_ride);
        customer_info_txt = (TextView) findViewById(R.id.customer_info_txt);
        trip_status_txt = (TextView) findViewById(R.id.trip_status_txt);
        your_location_txt = (TextView) findViewById(R.id.your_location_txt);
        cancel_btn = (TextView) findViewById(R.id.cancel_btn);
        meter_txt = (TextView) findViewById(R.id.meter_txt);
        message_layout = (LinearLayout) findViewById(R.id.message_layout);
        customer_phone_txt = (TextView) findViewById(R.id.customer_phone_txt);
        connectivity_status = (TextView) findViewById(R.id.connectivity_status);
        root = (LinearLayout) findViewById(R.id.root);
        location_txt = (TextView) findViewById(R.id.location_txt);
        acc_txt = (TextView) findViewById(R.id.acc_txt);
        sos = (LinearLayout)findViewById(R.id.sos);
        dot = (ImageView) findViewById(R.id.dot);
        chat_message = (TextView) findViewById(R.id.chat_message);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        firebaseChatUtillistener = new FirebaseChatUtillistener(rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));

        apiManager.execution_method_get(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG , Apis.updateLatLong+"?driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&current_lat="+ locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)+"&current_long="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)+"&current_location="+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id=1");





        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

                if(!Constants.is_main_activity_open){
                    startActivity(new Intent(TrackRideActivity.this , SplashActivity.class));
                }
            }
        });



        findViewById(R.id.location_changer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("5") || rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")){
                    openGooglePlaceAPiDialoge();
                }

            }
        });

        findViewById(R.id.call_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+getIntent().getExtras().getString("customer_phone")));
                if (ActivityCompat.checkSelfPermission(TrackRideActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }
        });

        trip_status_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")){
                    // run arrived api
                    apiManager.execution_method_get(Config.ApiKeys.KEY_ARRIVED , Apis.arrivedTrip+"?ride_id="+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)+"&driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
                }else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("5")){
                    // run begin trip api
                    if(location_txt.getText().toString().equals("") || location_txt.getText().toString() == null ||  location_txt.getText().toString().equals(TrackRideActivity.this.getResources().getString(R.string.TrackRideActivity__set_your_drop_point))){
                        Toast.makeText(TrackRideActivity.this, "Please Ask Drop Location From the Passenger.", Toast.LENGTH_SHORT).show();
                    }else{
                        try{samLocationRequestService.executeService(new SamLocationRequestService.SamLocationListener() {
                            @Override
                            public void onLocationUpdate(Location location) {
                                if(location.getAccuracy()>100.0){ Toast.makeText(TrackRideActivity.this, "Ride Started but with Accuracy = "+location.getAccuracy(), Toast.LENGTH_SHORT).show();}else{}
                                try{apiManager.execution_method_get(Config.ApiKeys.KEY_BEGIN_TRIP , Apis.beginTrip+"?ride_id="+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)+"&driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&begin_lat="+location.getLatitude()+"&begin_long="+location.getLongitude()+"&begin_location="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION)+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));}catch (Exception E){}
                            }
                        });}catch (Exception e){}
                    }
                }else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")){
                    // run ride end api
//                    if(meter_txt.getText().equals("0.0")){
//                        apiManager.execution_method_get(Config.ApiKeys.KEY_END_TRIP , Apis.endTripMeter+"?ride_id="+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)+"&driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&begin_lat="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)+"&begin_long="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE)+"&begin_location="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION)+"&end_lat="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)+"&end_long="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)+"&end_location="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LOCATION_TEXT)+"&end_time="+getArrivalTime()+"&distance="+locationSession.getLocationDetails().get(LocationSession.KEY_METER_VALUE)+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
//                    }else {
//                        apiManager.execution_method_get(Config.ApiKeys.KEY_END_TRIP , Apis.endTripMeter+"?ride_id="+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)+"&driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&begin_lat="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)+"&begin_long="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE)+"&begin_location="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION)+"&end_lat="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)+"&end_long="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)+"&end_location="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LOCATION_TEXT)+"&end_time="+getArrivalTime()+"&distance="+meter_txt.getText().toString()+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
//                    }
                    try{
                        samLocationRequestService.executeService(new SamLocationRequestService.SamLocationListener() {@Override public void onLocationUpdate(Location location) {
                            try{Double distance_travel = Double.parseDouble(""+meter_txt.getText().toString().replace(" km" , ""));
                                distance_travel = distance_travel * 1000 ;
                                if(location.getAccuracy()>100.0){ Toast.makeText(TrackRideActivity.this, "Ride End but with Accuracy = "+location.getAccuracy(), Toast.LENGTH_SHORT).show();}else{}
                                apiManager.execution_method_get(Config.ApiKeys.KEY_END_TRIP , Apis.endTripMeter+"?ride_id="+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)+"&driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&begin_lat="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)+"&begin_long="+rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE)+"&begin_location="+"&end_lat="+location.getLatitude()+"&end_long="+location.getLongitude()+"&end_location="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LOCATION_TEXT)+"&end_time="+getArrivalTime()+"&distance="+distance_travel+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID)+"&lat_long="+dbHelper.getRideLocationData(""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)));
                            }catch (Exception e){}
                        }
                        });
                    }catch (Exception e){
                        Toast.makeText(TrackRideActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        findViewById(R.id.navigation_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)+","+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)+"&daddr=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)+","+rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE)));
                    startActivity(intent);
                }else if (rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION)+"&daddr=" +rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION)));
                    startActivity(intent);
                }else {
                    Snackbar.make(root , ""+TrackRideActivity.this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__please_start_your_ridr_first) , Snackbar.LENGTH_SHORT).show();
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
                apiManager.execution_method_get(Config.ApiKeys.KEY_CANCEL_REASONS , Apis.cancelReason+"?language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
            }
        });

        findViewById(R.id.sos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TrackRideActivity.this , SosActivity.class));
            }
        });



        findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TrackRideActivity.this , ChatActivity.class)
                        .putExtra("ride_id" , ""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID))
                        .putExtra("ride_status" , ""+trip_status_txt.getText().toString())
                        .putExtra("user_name" , ""+rideSession.getCurrentRideDetails().get(RideSession.USER_NAME))
                        .putExtra("user_image" , "https://cdn0.iconfinder.com/data/icons/user-pictures/100/matureman1-512.png"));
            }
        });


        message_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TrackRideActivity.this , ChatActivity.class)
                        .putExtra("ride_id" , ""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID))
                        .putExtra("ride_status" , ""+trip_status_txt.getText().toString())
                        .putExtra("user_name" , ""+rideSession.getCurrentRideDetails().get(RideSession.USER_NAME))
                        .putExtra("user_image" , "https://cdn0.iconfinder.com/data/icons/user-pictures/100/matureman1-512.png"));
            }
        });

    }

    private void showDemodialog() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        dialog.setCancelable(true);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_for_meter);

        final EditText demo_meter_edt = (EditText)dialog.findViewById(R.id.demo_meter_edt);
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double km_value = 0.0 ;

                try{
                    km_value  = Double.parseDouble(""+demo_meter_edt.getText().toString());
                    meter_txt.setText(""+(km_value/1000)+" km");
                }catch (Exception e){
                    Toast.makeText(TrackRideActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setView() {
        customer_info_txt.setText("" +rideSession.getCurrentRideDetails().get(RideSession.USER_NAME));
        customer_phone_txt.setText(""+rideSession.getCurrentRideDetails().get(RideSession.USER_PHONE));

        if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3") ||  rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("4") ){
            location_txt.setText(""+rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION));
            location_txt.setTextColor(Color.parseColor("#2ecc71"));
            dot.setColorFilter(Color.parseColor("#2ecc71"));
        }else {
            location_txt.setText(""+rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION));
            location_txt.setTextColor(Color.parseColor("#e74c3c"));
            dot.setColorFilter(Color.parseColor("#e74c3c"));
        }
        setviewAccordingToStatus();
    }


    @Override
    protected void onResume() {
        super.onResume();
        firebaseChatUtillistener.startChatListening();
        EventBus.getDefault().register(this);
        Constants.is_track_ride_activity_is_open = true;
        try{if(is_map_loaded){setView();}}catch (Exception e){}
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChangeLocationEvent event){
        apiManager.execution_method_get(Config.ApiKeys.KEY_VIEW_RIDE_INFO_DRIVER, Apis.viewRideInfoDriver + "?ride_id=" + rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) + "&driver_token=" + sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken) + "&language_id=1" );
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FirebaseChatEvent event){
        try{
            String text = new ZamanUtil(Long.parseLong(""+event.timestamp)).getTime() ;
            if(text.equals("Just Now")|| text.equals("In a few seconds")){
                showUserMesageWithTimer(event.message);
            }
        }catch (Exception e){}
    }

    private void showUserMesageWithTimer(String message) {
        message_layout.setVisibility(View.VISIBLE);
        chat_message.setText(""+message);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                message_layout.setVisibility(View.GONE);
            }
        }, 5000);

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventytrackAccuracy eventtt){

        try{acc_txt.setText("Acc = "+eventtt.Accuracy);}catch (Exception e){}
    }


    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mHandeler.removeCallbacks(mRunnable);
        firebaseChatUtillistener.stopChatListener();
        Constants.is_track_ride_activity_is_open = false ;
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



        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(TrackRideActivity.this, "Present Value of Tail "+sessionManager.getUserDetails().get(SessionManager.KEY_TAIL), Toast.LENGTH_SHORT).show();
                try{
                    Toast.makeText(TrackRideActivity.this, ""+dbHelper.getRideLocationData(""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)), Toast.LENGTH_SHORT).show();
                    Log.d("****"+TAG , ""+dbHelper.getRideLocationData(""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)));
                }catch (Exception e){
                    Toast.makeText(TrackRideActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationEvent event){

        try{
            if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6") && event.getMeter_value() >= Double.parseDouble(""+sessionManager.getUserDetails().get(SessionManager.KEY_TAIL))){ // update route
                DrawRouteMaps.getInstance(this , 6 , R.color.icons_8_muted_green_1).draw(new LatLng(Double.parseDouble(event.getlatitude_string()) , Double.parseDouble(event.getLongitude_string())), new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))), mGooglemap , sessionManager);
            }
        }catch (Exception e){
            Toast.makeText(TrackRideActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }



        if(!is_location_updation_running){
            apiManager.execution_method_get(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG , Apis.updateLatLong+"?driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&current_lat="+ event.getlatitude_string()+"&current_long="+event.getLongitude_string()+"&current_location="+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id=1");
        }



        try{
            double value_in_km  = (Double.parseDouble(""+event.getMeter_value())/1000);
            value_in_km = Math.round(value_in_km * 100D) / 100D;
            meter_txt.setText(""+value_in_km+" km");
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        try{
            if(event.is_meter_value_cleared()){
                drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))) ,  new LatLng(Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)) , Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG))) ,mGooglemap ,R.drawable.ic_contact_green , R.drawable.ic_very_small );
            }
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        is_map_loaded = false ;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ownMessageEvent(MyFirebaseMessagingService.RideEvent event){
        if(event.getRideStatus().equals("2")){
            showDialogForCancelation();
        }if(event.getRideStatus().equals("17")){
            showDialogForCancelationViaAdmin();
        }

    }


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




    public void setviewAccordingToStatus (){

        try{
            if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("2")){
                showDialogForCancelation();
            }
            if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("3")){
                ////  set view when driver needs to reach over pick up point
                trip_status_txt.setText(""+this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__located));
                drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))) ,  new LatLng(Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)) , Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG))) ,mGooglemap ,R.drawable.ic_contact_green , R.drawable.ic_very_small );
                cancel_btn.setVisibility(View.VISIBLE);
                meter_txt.setVisibility(View.GONE);
                sos.setVisibility(View.GONE);
            }
            if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("5")){
                trip_status_txt.setText(""+this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__begin));
                cancel_btn.setVisibility(View.VISIBLE);
                meter_txt.setVisibility(View.GONE);
                sos.setVisibility(View.GONE);
                if(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE).equals("")   ||  rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE)  == null  ){  // no drop off location
                    mGooglemap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(Double.parseDouble(""+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)) , Double.parseDouble(""+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)))).zoom(17).build()));

                }else{
                    drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))) , new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))),mGooglemap , R.drawable.dot_green , R.drawable.dot_red);
                }
            }
            if(rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS).equals("6")){
                startRunnableProcess();
//            drawRoute(new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.PICK_LONGITUDE))) , new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))),mGooglemap , R.drawable.dot_green , R.drawable.dot_red);
                trip_status_txt.setText(""+this.getResources().getString(R.string.TRACK_RIDE_ACTIVITY__end));
                cancel_btn.setVisibility(View.GONE);
                meter_txt.setVisibility(View.VISIBLE);
                sos.setVisibility(View.VISIBLE);
//            startBeginToEndTracking();
            }
        }catch (Exception e){
            Toast.makeText(this, "TrackRideActivity SetViewAccordingToStatus  "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    public void drawRoute (LatLng origin  , LatLng destination , GoogleMap mMap , int origin_icon  , int destination_icon ){
        mGooglemap.clear();
        try {
            DrawRouteMaps.getInstance(this , 6 , R.color.icons_8_muted_green_1).draw(origin, destination, mMap , sessionManager);
            DrawMarker.getInstance(this).draw(mMap, origin, origin_icon, ""+rideSession.getCurrentRideDetails().get(RideSession.PICK_LOCATION));
            DrawMarker.getInstance(this).draw(mMap, destination, destination_icon, ""+rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION));

            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(origin)
                    .include(destination).build();
            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay().getSize(displaySize);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 500, 60));
        }catch (Exception e ){

        }
    }



    public String getArrivalTime (){
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        String h = hour + ":" + minutes + ":" + sec;
        String[] h1 = h.split(":");
        int hours = Integer.parseInt(h1[0]);
        int minute = Integer.parseInt(h1[1]);
        int second = Integer.parseInt(h1[2]);
        return ""+(second + (60 * minute) + (3600 * hours));
    }

    @Override
    public void onAPIRunningState(int a, String APINAME) {

        if(a == ApiManager.APIFETCHER.KEY_API_IS_STARTED && !APINAME.equals(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG)){
            progressDialog.show();
        }else {
            progressDialog.dismiss();
        }



        if (a == ApiManager.APIFETCHER.KEY_API_IS_STARTED) {
            is_location_updation_running = true ;
        }else{
            is_location_updation_running  = false ;
        }

    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {

        try{ GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            ResultCheck result_check = gson.fromJson(""+script, ResultCheck.class);

            if(result_check.result.equals("1")){
                if(APINAME.equals(Config.ApiKeys.KEY_ARRIVED)){
                    rideSession.setRideStatus("5");
                    updateFirebaseEvent(Config.Status.VAL_5 , ""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));
                }
                if(APINAME.equals(Config.ApiKeys.KEY_BEGIN_TRIP)){
                    rideSession.setRideStatus("6");
                    locationSession.clearMeterValue();
                    apiManager.execution_method_get(Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG , Apis.updateLatLong+"?driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&current_lat="+ locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)+"&current_long="+locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)+"&current_location="+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id=1");
                    updateFirebaseEvent(Config.Status.VAL_6 , ""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));
                }
                if(APINAME.equals(Config.ApiKeys.KEY_END_TRIP)){
                    rideSession.setRideStatus("7");
                    RideArrived rideArrived = new RideArrived();
                    rideArrived = gson.fromJson(""+script, RideArrived.class);
                    updateFirebaseEventWithDoneRide(Config.Status.VAL_7  , rideArrived.getDetails().getDoneRideId() , rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));
                    startActivity(new Intent(this, PriceFareActivity.class)
                            .putExtra("amount", rideArrived.getDetails().getAmount())
                            .putExtra("distance", rideArrived.getDetails().getDistance())
                            .putExtra("time", rideArrived.getDetails().getTotTime())
                            .putExtra("customerId", rideSession.getCurrentRideDetails().get(RideSession.USER_ID))
                            .putExtra("ride_id", rideArrived.getDetails().getRideId())
                            .putExtra("done_ride_id" , rideArrived.getDetails().getDoneRideId()));
                    finish();
                    rideSession.clearRideSession();
                }
                if(APINAME.equals(Config.ApiKeys.KEY_CANCEL_REASONS)){
                    CancelReasonCustomer cancelReasonCustomer = gson.fromJson(""+script, CancelReasonCustomer.class);
                    showReasonDialog(cancelReasonCustomer);
                }
                if(APINAME.equals(Config.ApiKeys.KEY_CANCEL_TRIP)){
                    rideSession.setRideStatus("4");
                    updateFirebaseEvent(Config.Status.VAL_9,""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) );
                    rideSession.clearRideSession();
                    finish();
                    startActivity(new Intent(this, RidesActivity.class));
                }
                if(APINAME.equals(""+Config.ApiKeys.KEY_UPDATE_DRIVER_LAT_LONG)){
                    NewUpdateLatLongModel response = gson.fromJson(""+script , NewUpdateLatLongModel.class);
                    your_location_txt.setText(""+response.getDetails());
                }
                if(APINAME.equals(""+Config.ApiKeys.KEY_CHANGE_DESTINATION)){
                    updateFirebaseEventWithDestinationChange();
                    NewChangeDropLocationModel drop_change_response = gson.fromJson("" +script, NewChangeDropLocationModel.class);
                    rideSession.setDropLocation(""+drop_change_response.getDetails().getDrop_location(), ""+drop_change_response.getDetails().getDrop_lat() , ""+drop_change_response.getDetails().getDrop_long());
                }
                if (APINAME.equals("" + Config.ApiKeys.KEY_VIEW_RIDE_INFO_DRIVER)) {
                    ViewRideInfoDriver viewRideInfoDriver = gson.fromJson("" + script, ViewRideInfoDriver.class);
                    rideSession.setDropLocation(viewRideInfoDriver.getDetails().getDrop_location() , ""+viewRideInfoDriver.getDetails().getDrop_lat(), ""+viewRideInfoDriver.getDetails().getDrop_long());
                }

                setView();

            }else {
                Toast.makeText(this, "Server Error while executing API ", Toast.LENGTH_SHORT).show();
            }
            Log.d("*****"+APINAME , ""+script);}catch (Exception E){
            Toast.makeText(activity, ""+E.getMessage(), Toast.LENGTH_SHORT).show();
        }



    }

    private void updateFirebaseEvent(final String status_value  , final String Ride_Id) throws  Exception{

        FirebaseDatabase.getInstance().getReference(Config.RideTableReference).child(""+Ride_Id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<ChatModel>> t = new GenericTypeIndicator<List<ChatModel>>() {};
                List<ChatModel> yourStringArray = dataSnapshot.child("Chat").getValue(t);
                try{FirebaseDatabase.getInstance().getReference(""+Config.RideTableReference).child(""+Ride_Id).setValue(new RideSessionEvent(""+ Ride_Id , ""+status_value , "Not yet generated" , "0"));}catch (Exception e){}
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(""+TAG , "Data Fetched from firebase cancelled "+databaseError.getMessage());
            }
        });
    }

    private void updateFirebaseEventWithDestinationChange( ) throws  Exception{

        FirebaseDatabase.getInstance().getReference(Config.RideTableReference).child(""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<ChatModel>> t = new GenericTypeIndicator<List<ChatModel>>() {};
                List<ChatModel> yourStringArray = dataSnapshot.child("Chat").getValue(t);
                try{FirebaseDatabase.getInstance().getReference(""+Config.RideTableReference).child(""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)).setValue(new RideSessionEvent(""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID) , ""+rideSession.getCurrentRideDetails().get(RideSession.RIDE_STATUS) ,"Not yet generated"  , "1"));}catch (Exception e){}
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(""+TAG , "Data Fetched from firebase cancelled "+databaseError.getMessage());
            }
        });
    }

    private void updateFirebaseEventWithDoneRide(final String status_value , final String done_ride_id , final String ride_id) throws  Exception{

        FirebaseDatabase.getInstance().getReference(Config.RideTableReference).child(""+ride_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<ChatModel>> t = new GenericTypeIndicator<List<ChatModel>>() {};
                List<ChatModel> yourStringArray = dataSnapshot.child("Chat").getValue(t);
                try{FirebaseDatabase.getInstance().getReference(""+Config.RideTableReference).child(""+ride_id).setValue(new RideSessionEvent(""+ride_id , ""+status_value , ""+done_ride_id , "0"));}catch (Exception e){}
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(""+TAG , "Data Fetched from firebase cancelled "+databaseError.getMessage());
            }
        });
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
                apiManager.execution_method_get(Config.ApiKeys.KEY_CANCEL_TRIP , Apis.cancelRide+"?ride_id="+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID)+"&reason_id="+cancelReasonCustomer.getMsg().get(position).getReasonId());
                dialog.dismiss();
            }
        });
        dialog.show();
    }




    private void finaliseAftercancelation() {
        try{
            RidesActivity.activity.finish();
        }catch (Exception e){

        }
        try{
            SelectedRidesActivity.activity.finish();
        }catch (Exception e ){

        }
        rideSession.setRideStatus("18");
        finish();
        startActivity(new Intent(TrackRideActivity.this , RidesActivity.class ));
    }



    @Override
    public void onBackPressed() {
        if(!Constants.is_main_activity_open){
            startActivity(new Intent(TrackRideActivity.this , SplashActivity.class));
        }
        super.onBackPressed();

    }




    private void openGooglePlaceAPiDialoge() {
        try {
            Intent intent =new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(TrackRideActivity.this);
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
                apiManager.execution_method_get(""+Config.ApiKeys.KEY_CHANGE_DESTINATION , ""+Apis.change_drop_location +"drop_lat="+place.getLatLng().latitude+"&drop_long="+place.getLatLng().longitude+"&drop_location="+place.getName()+"&app_id=2"+"&ride_id="+rideSession.getCurrentRideDetails().get(RideSession.RIDE_ID));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("*****", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }



    public void startRunnableProcess(){

        try{
            mHandeler.removeCallbacks(mRunnable);
        }catch (Exception e){

        }

        try{
//            mGooglemap.clear();

            final LatLng POINT_A = new LatLng(Double.parseDouble(""+rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)) , Double.parseDouble(""+rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE)));
            Maputils.setDestinationmarker(TrackRideActivity.this ,mGooglemap,POINT_A , ""+rideSession.getCurrentRideDetails().get(RideSession.DROP_LOCATION));

            new SamLocationRequestService(TrackRideActivity.this).executeService(new SamLocationRequestService.SamLocationListener() {
                @Override
                public void onLocationUpdate(Location location) {
                    DrawRouteMaps.getInstance(TrackRideActivity.this , 9 ,R.color.icons_8_muted_green_1).draw(new LatLng(location.getLatitude() , location.getLongitude()), new LatLng(Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LATITUDE)) , Double.parseDouble(rideSession.getCurrentRideDetails().get(RideSession.DROP_LONGITUDE))), mGooglemap , sessionManager);
                }
            });


            mRunnable = new Runnable() {

                @Override
                public void run() {
                    new SamLocationRequestService(TrackRideActivity.this).executeService(new SamLocationRequestService.SamLocationListener() {
                        @Override
                        public void onLocationUpdate(final Location location) {
                            if(location.getBearing()!= 0.0){
                                MAIN_BEARING = location.getBearing();
                            }
                            Maputils.moverCamera(mGooglemap , new LatLng( location.getLatitude() , location.getLongitude()));
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(),
                                            location.getLongitude())).bearing(MAIN_BEARING)
                                    .tilt(47).zoom(18).build();
                            mGooglemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);
                        }
                    });
                    mHandeler.postDelayed(mRunnable, 3000 );
                }
            };
            runOnUiThread(mRunnable);
        }catch (Exception e){
            Toast.makeText(this, "TrackRideActivity startRunnableProcess  "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}

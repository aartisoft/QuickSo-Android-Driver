package com.apporio.billiondriver;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apporio.billiondriver.manager.RideSession;
import com.apporio.billiondriver.manager.SessionManager;
import com.apporio.billiondriver.models.rideaccept.RideAccept;
import com.apporio.billiondriver.others.FirebaseUtils;
import com.apporio.billiondriver.others.RideSessionEvent;
import com.apporio.billiondriver.samwork.ApiManager;
import com.apporio.billiondriver.trackride.TrackRideActivity;
import com.apporio.billiondriver.urls.Apis;
import com.apporio.billiondriver.views.MButton;
import com.apporio.billiondriver.views.MaterialRippleLayout;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hbb20.CountryCodePicker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ManualUserDetailActivity extends AppCompatActivity implements ApiManager.APIFETCHER {

    LocationSession locationSession;
    SessionManager sessionManager;

    @Bind(R.id.ll_back_rides)
    LinearLayout ll_back_rides;

    @Bind(R.id.textView_dropPoint)
    TextView textView_dropPoint;

    @Bind(R.id.ll_dropLocation)
    LinearLayout ll_dropLocation;

    @Bind(R.id.textView_pickUp)
    TextView textView_pickUp;

    @Bind(R.id.ll_pickUpLocation)
    LinearLayout ll_pickUpLocation;

    @Bind(R.id.edt_cus_number)
    EditText edt_cus_number;

    @Bind(R.id.edt_cus_name)
    EditText edt_cus_name;

    MButton button_startRide;

    CountryCodePicker countryCodePicker;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    String manualDropLat = "", manualDropLng = "", manualPickLat = "", manualPickLng = "", manualPickLocation = "", manualDropLocation = "";
    FirebaseUtils firebaseUtils;
    ApiManager apiManager;
    ProgressDialog pd;
    GsonBuilder builder;
    Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_user_detail);

        ButterKnife.bind(this);
        getSupportActionBar().hide();

        initialization();
    }

    private void initialization() {

        locationSession = new LocationSession(this);
        sessionManager = new SessionManager(this);
        apiManager = new ApiManager(this);
        pd = new ProgressDialog(this);
        firebaseUtils = new FirebaseUtils(this);
        builder = new GsonBuilder();
        gson = builder.create();
        pd.setMessage(ManualUserDetailActivity.this.getResources().getString(R.string.loading));

        button_startRide = ((MaterialRippleLayout) findViewById(R.id.button_startRide)).getChildView();
        countryCodePicker = (CountryCodePicker) findViewById(R.id.otp_ccp);
        button_startRide.setText(getResources().getString(R.string.Manual_Activity_button_start_ride));
        button_startRide.setTextSize(16);


        initialClickListeners();
        setPickUpView();

    }

    private void setPickUpView() {

        try {
            manualPickLat = locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT);
            manualPickLng = locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG);
            apiManager.execution_method_get("" + "reverse_code", "" + Apis.getAddress + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT) + "&longitude=" + locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG));

            //getAddress(getApplicationContext(), Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LAT)), Double.parseDouble(locationSession.getLocationDetails().get(LocationSession.KEY_CURRENT_LONG)));
        } catch (Exception e) {

        }
    }


    private void initialClickListeners() {

        ll_dropLocation.setOnClickListener(new setOnClickList());
        ll_back_rides.setOnClickListener(new setOnClickList());
        button_startRide.setOnClickListener(new setOnClickList());
    }

    //StartRideMethod
    private void startRideMethod() {

        HashMap<String, String> bodyparameters = new HashMap<String, String>();
        bodyparameters.put("user_name", "" + edt_cus_name.getText().toString());
        bodyparameters.put("user_phone", "" + countryCodePicker.getSelectedCountryCodeWithPlus() + edt_cus_number.getText().toString());
        bodyparameters.put("pickup_lat", "" + manualPickLat);
        bodyparameters.put("pickup_long", "" + manualPickLng);
        bodyparameters.put("pickup_location", "" + manualPickLocation);
        bodyparameters.put("drop_lat", "" + manualDropLat);
        bodyparameters.put("drop_long", "" + manualDropLng);
        bodyparameters.put("drop_location", "" + manualDropLocation);
        bodyparameters.put("pem_file", "" + "");
        bodyparameters.put("car_type_id", "" + sessionManager.getUserDetails().get(SessionManager.KEY_Driver_CarTypeId));
        bodyparameters.put("driver_id", "" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID));
        apiManager.execution_method_post(Config.ApiKeys.MANUAL_RIDE, "" + Apis.Manual_Ride, bodyparameters);


    }

    //// GooglePlaceApiDialog
    private void openGooglePlaceAPiDialoge() {

        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(ManualUserDetailActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    Log.d("*#*# getAddress", "" + place.getAddress());
                    Log.d("*#*# getAttributions", "" + place.getAttributions());
                    Log.d("*#*# getLocale", "" + place.getLocale());
                    Log.d("*#*# getname", "" + place.getName());
                    Log.d("*#*# getId", "" + place.getId());
                    Log.d("*#*# geWebsiteURI", "" + place.getWebsiteUri());

                    textView_dropPoint.setText("" + place.getName());

                    manualDropLat = String.valueOf(place.getLatLng().latitude);
                    manualDropLng = String.valueOf(place.getLatLng().longitude);

                    manualDropLocation = place.getName().toString();


                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.i("*****", status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                }
            }
        } catch (Exception e) {
            Log.e("Manual_Exception_Place_AutoComplete", "" + e);
        }
    }


    // Getting Address From GeoCoder
    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {

        //Set Address
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {


                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                Log.d("", "getAddress:  address" + address);
                Log.d("", "getAddress:  city" + city);
                Log.d("", "getAddress:  state" + state);
                Log.d("", "getAddress:  postalCode" + postalCode);
                Log.d("", "getAddress:  knownName" + knownName);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void onAPIRunningState(int a, String APINAME) {

        if (a == ApiManager.APIFETCHER.KEY_API_IS_STARTED) {
            pd.show();
        }
        if (a == ApiManager.APIFETCHER.KEY_API_IS_STOPPED) {
            pd.dismiss();
        }
    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {

        try {
            if (APINAME.equals(Config.ApiKeys.MANUAL_RIDE)) {
                ReceivePassengerActivity.AcceptCheck ac = gson.fromJson("" + script, ReceivePassengerActivity.AcceptCheck.class);
                if (ac.getResult() == 1) {
                    RideAccept rideAccept = gson.fromJson("" + script, RideAccept.class);
                    Log.e("**script--trialreceivepassener", String.valueOf(script));

                    if (rideAccept.getResult() == 1) {
                        new RideSession(this).setRideSesion(rideAccept.getDetails().getRide_id(), rideAccept.getDetails().getUser_id(), rideAccept.getDetails().getUser_name(), rideAccept.getDetails().getUser_phone(), rideAccept.getDetails().getCoupon_code(), rideAccept.getDetails().getPickup_lat(), rideAccept.getDetails().getPickup_long(), rideAccept.getDetails().getPickup_location(), rideAccept.getDetails().getDrop_lat(), rideAccept.getDetails().getDrop_long(), rideAccept.getDetails().getDrop_location(), rideAccept.getDetails().getRide_date(), rideAccept.getDetails().getRide_time(), rideAccept.getDetails().getLater_date(), rideAccept.getDetails().getLater_time(), rideAccept.getDetails().getDriver_id(), rideAccept.getDetails().getRide_type(), rideAccept.getDetails().getRide_status(), rideAccept.getDetails().getStatus());
                        FirebaseDatabase.getInstance().getReference("" + Config.RideTableReference).child("" + rideAccept.getDetails().getRide_id()).setValue(new RideSessionEvent("" + rideAccept.getDetails().getRide_id(), "" + Config.Status.NORMAL_STARTED, "Not yet generated", "0"));
                        startActivity(new Intent(this, TrackRideActivity.class)
                                .putExtra("customer_name", "" + rideAccept.getDetails().getUser_name())
                                .putExtra("customer_phone", "" + rideAccept.getDetails().getUser_phone()));
                        firebaseUtils.createRidePool("" + FirebaseUtils.NO_RIDES, "" + FirebaseUtils.NO_RIDE_STATUS);
                        finish();
                    }
                } else {

                }
            } else if (APINAME.equals("reverse_code")) {

                GetAddressResponse getAddressResponse = gson.fromJson("" + script, GetAddressResponse.class);

                if (getAddressResponse.getResult() == 1) {
                    textView_pickUp.setText("" + getAddressResponse.getDetails().toString());
                    manualPickLocation = getAddressResponse.getDetails().toString();
                } else {

                }
            }

        } catch (Exception e) {

        }

    }

    @Override
    public void onFetchResultZero(String script) {

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == button_startRide.getId()) {
                if (textView_pickUp.getText().toString().equals("")) {
                    Toast.makeText(ManualUserDetailActivity.this, getResources().getString(R.string.Manual_Activity_select_pick_location), Toast.LENGTH_SHORT).show();
                    ManualUserDetailActivity.this.finish();
                } else if (manualDropLat.equals("") && manualDropLng.equals("")) {
                    Toast.makeText(ManualUserDetailActivity.this, getResources().getString(R.string.Manual_Activity_select_drop_location), Toast.LENGTH_SHORT).show();
                } else {
                    startRideMethod();
                }

            } else if (i == ll_dropLocation.getId()) {
                openGooglePlaceAPiDialoge();

            } else if (i == ll_back_rides.getId()) {

                ManualUserDetailActivity.this.finish();

            }
        }
    }
}

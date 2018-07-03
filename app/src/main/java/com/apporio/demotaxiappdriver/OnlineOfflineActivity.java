package com.apporio.demotaxiappdriver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.apporio.demotaxiappdriver.manager.LanguageManager;
import com.apporio.demotaxiappdriver.manager.SessionManager;
import com.apporio.demotaxiappdriver.models.ModelDeviceOnlineIffline;
import com.apporio.demotaxiappdriver.samwork.ApiManager;
import com.apporio.demotaxiappdriver.urls.Apis;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OnlineOfflineActivity extends Activity implements ApiManager.APIFETCHER {

    @Bind(R.id.close_btn) RelativeLayout close_btn;
    @Bind(R.id.status_description_txt) TextView status_description_txt;
    @Bind(R.id.status__txt) TextView status__txt;
    @Bind(R.id.status_image) ImageView status_image;
    @Bind(R.id.status_btn) LinearLayout status_btn;
    @Bind(R.id.btn_txt)
    TextView btn_txt;


    LanguageManager languageManager ;
    SessionManager sessionManager ;
    ApiManager apiManager ;
    GsonBuilder builder ;
    Gson gson ;
    ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder = new GsonBuilder();
        gson = builder.create();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(""+this.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        progressDialog = new ProgressDialog(this);

        apiManager = new ApiManager(this);
        languageManager = new LanguageManager(this);
        setContentView(R.layout.activity_online_offline);
        sessionManager = new SessionManager(this);
        ButterKnife.bind(this);


        setViewAccrodingly ();

        this.setFinishOnTouchOutside(false);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sessionManager.getUserDetails().get(SessionManager.KEY_Driver_Online_Offline_Status).equals("1")){
                    apiManager.execution_method_get(Config.ApiKeys.KEY_ONLINE_OFFLINE , Apis.onlineOffline+"?driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&online_offline=2"+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
                }else{
                    apiManager.execution_method_get(Config.ApiKeys.KEY_ONLINE_OFFLINE , Apis.onlineOffline+"?driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&online_offline=1"+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));

                }

            }
        });
    }

    private void setViewAccrodingly() {
        if(sessionManager.getUserDetails().get(SessionManager.KEY_Driver_Online_Offline_Status).equals("1")){
            status_image.setColorFilter(OnlineOfflineActivity.this.getResources().getColor(R.color.icons_8_muted_green_2_dark));
            status__txt.setText(""+OnlineOfflineActivity.this.getResources().getString(R.string.ONLINE_OFFLINE_ACTIVITY__on_duty));
            status__txt.setTextColor(OnlineOfflineActivity.this.getResources().getColor(R.color.icons_8_muted_green_2_dark));
            status_description_txt.setText(""+OnlineOfflineActivity.this.getResources().getString(R.string.online_status_description_txt));
            btn_txt.setText(""+OnlineOfflineActivity.this.getResources().getString(R.string.ONLINE_OFFLINE_ACTIVITY__go_offline));
        }else{
            status_image.setColorFilter(OnlineOfflineActivity.this.getResources().getColor(R.color.icons_8_muted_red));
            status__txt.setText(""+OnlineOfflineActivity.this.getResources().getString(R.string.ONLINE_OFFLINE_ACTIVITY__off_duty));
            status__txt.setTextColor(OnlineOfflineActivity.this.getResources().getColor(R.color.icons_8_muted_red));
            status_description_txt.setText(""+OnlineOfflineActivity.this.getResources().getString(R.string.ONLINE_OFFLINE_ACTIVITY__offline_status_description_txt));
            btn_txt.setText(""+OnlineOfflineActivity.this.getResources().getString(R.string.ONLINE_OFFLINE_ACTIVITY__go_online));
        }
    }

    @Override
    public void onAPIRunningState(int a, String APINAME) {

        if( a == ApiManager.APIFETCHER.KEY_API_IS_STARTED){
            progressDialog.show();
        }else {
            if(progressDialog != null){
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {
        try{
            //ResultCheck resultCheck = gson.fromJson(""+script , ResultCheck.class);
            ModelDeviceOnlineIffline resultCheck = gson.fromJson(""+script, ModelDeviceOnlineIffline.class);
            if(resultCheck.getResult() == 1){
                switch (APINAME){
                    case Config.ApiKeys.KEY_ONLINE_OFFLINE :
                        ModelDeviceOnlineIffline modelDeviceOnlineIffline = gson.fromJson(""+script, ModelDeviceOnlineIffline.class);
                        if(modelDeviceOnlineIffline.getOffline() == 1){
                            sessionManager.setonline_offline(true);
                        }else {
                            sessionManager.setonline_offline(false);
                        }
                        break;
                }

                finish();
            }
            else {
                Toast.makeText(this, resultCheck.getMsg(), Toast.LENGTH_SHORT).show();
            }}catch (Exception e){}

    }


    @Override
    public void onFetchResultZero(String script) {

    }




//     switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//            if (!isChecked) {
//                switch1 = "0";
//                apiManager_new.execution_method_get(Config.ApiKeys.KEY_ONLINE_OFFLINE , Apis.onlineOffline+"?driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&online_offline=2"+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
//            } else {
//                switch1 = "1";
//                apiManager_new.execution_method_get(Config.ApiKeys.KEY_ONLINE_OFFLINE , Apis.onlineOffline+"?driver_id="+sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID)+"&online_offline=1"+"&driver_token="+sessionManager.getUserDetails().get(SessionManager.KEY_DriverToken)+"&language_id="+languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID));
//            }
//        }
//    });


    @Override
    public void onBackPressed() {

    }
}

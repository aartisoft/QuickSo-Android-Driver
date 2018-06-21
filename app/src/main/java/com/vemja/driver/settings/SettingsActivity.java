package com.vemja.driver.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vemja.driver.AboutActivity;
import com.vemja.driver.Config;
import com.vemja.driver.CustomerSupportActivity;
import com.vemja.driver.R;
import com.vemja.driver.SplashActivity;
import com.vemja.driver.TermsAndCondition;
import com.vemja.driver.manager.LanguageManager;
import com.vemja.driver.models.ModelReportIssue;
import com.vemja.driver.samwork.ApiManager;
import com.vemja.driver.urls.Apis;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, ApiManager.APIFETCHER {

    @Bind(R.id.tv_toolbar_text)
    TextView tv_toolbar_text;
    @Bind(R.id.ll_language_btn)
    LinearLayout ll_language_btn;
    @Bind(R.id.ll_customer_btn)
    LinearLayout ll_customer_btn;
    @Bind(R.id.ll_report_issue_btn)
    LinearLayout ll_report_issue_btn;
    @Bind(R.id.ll_terms_btn)
    LinearLayout ll_terms_btn;
    @Bind(R.id.ll_about_btn)
    LinearLayout ll_about_btn;
    @Bind(R.id.textView_version_name)
    TextView textView_version_name;

    ProgressDialog progressDialog;
    ApiManager apiManager;
    ModelReportIssue modelReportIssue;
    LanguageManager languageManager;
    GsonBuilder builder;
    Gson gson;
    String versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        initialization();
        onClickListeners();
        apiCallingMethod();
    }

    private void apiCallingMethod() {
        apiManager.execution_method_get("" + Config.ApiKeys.KEY_REPORT_ISSUE, "" + Apis.RepostIssueDetails);
    }

    private void onClickListeners() {

        ll_language_btn.setOnClickListener(this);
        ll_customer_btn.setOnClickListener(this);
        ll_report_issue_btn.setOnClickListener(this);
        ll_terms_btn.setOnClickListener(this);
        ll_about_btn.setOnClickListener(this);
    }

    private void initialization() {

        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        apiManager = new ApiManager(this);
        languageManager = new LanguageManager(this);
        builder = new GsonBuilder();
        gson = builder.create();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_text);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_toolbar_text.setText(getResources().getString(R.string.SETTINGS_ACTIVITY_header_text));


        try {
            versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;

            Log.e("VersionNmae",""+ versionName);
            textView_version_name.setText("("+versionName+")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ll_language_btn:
                final String[] languages = new String[]{"English", "Portugues"};
                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(SettingsActivity.this);
                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                builder.setTitle(R.string.select_language);
                builder.setItems(languages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        switch (languages[index]) {
                            case "English":
                                languageManager.setLanguage("en");
                                break;
                            case "Portugues":
                                languageManager.setLanguage("pt");
                                break;
                        }
                        dialogInterface.dismiss();
                        startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                        finish();
                    }
                });
                builder.show();

                break;

            case R.id.ll_customer_btn:
                startActivity(new Intent(SettingsActivity.this, CustomerSupportActivity.class));
                break;

            case R.id.ll_report_issue_btn:

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "" + modelReportIssue.getDeatils(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "" + SettingsActivity.this.getResources().getString(R.string.report_issue));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "" + SettingsActivity.this.getResources().getString(R.string.send_email)));
                emailIntent.setType("text/plain");

                break;

            case R.id.ll_terms_btn:
                startActivity(new Intent(SettingsActivity.this, TermsAndCondition.class));
                break;

            case R.id.ll_about_btn:
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));

                break;
        }
    }

    @Override
    public void onAPIRunningState(int a, String APINAME) {
        if (a == ApiManager.APIFETCHER.KEY_API_IS_STARTED) {
            progressDialog.show();
        } else {
            progressDialog.hide();
        }
    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {

        if (APINAME.equals("" + Config.ApiKeys.KEY_REPORT_ISSUE)) {
            modelReportIssue = gson.fromJson("" + script, ModelReportIssue.class);
        }

    }

    @Override
    public void onFetchResultZero(String script) {

    }
}

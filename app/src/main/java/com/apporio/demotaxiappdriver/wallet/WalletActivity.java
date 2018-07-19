package com.apporio.demotaxiappdriver.wallet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apporio.apporiologs.ApporioLog;
import com.apporio.demotaxiappdriver.BaseActivity;
import com.apporio.demotaxiappdriver.Config;
import com.apporio.demotaxiappdriver.R;
import com.apporio.demotaxiappdriver.manager.SessionManager;
import com.apporio.demotaxiappdriver.models.ModelWalletTransactionsResponse;
import com.apporio.demotaxiappdriver.samwork.ApiManager;
import com.apporio.demotaxiappdriver.urls.Apis;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WalletActivity extends BaseActivity implements
        ApiManager.APIFETCHER {

    @Bind(R.id.tv_toolbar_text)
    TextView tvToolbarText;
    @Bind(R.id.tab_layout_wallet)
    TabLayout tabLayoutWallet;
    @Bind(R.id.view_pager_wallet)
    ViewPager viewPagerWallet;

    SessionManager sessionManager;
    ApiManager apiManager;
    Gson gson;
    GsonBuilder builder;
    @Bind(R.id.tv_wallet_balance_text)
    TextView tvWalletBalanceText;
    @Bind(R.id.tv_wallet_balance)
    TextView tvWalletBalance;
    ProgressDialog pd;

    @Bind(R.id.btn_add_money)
    CardView btn_add_money;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        ButterKnife.bind(this);
        apiManager = new ApiManager(this);
        sessionManager = new SessionManager(this);
        builder = new GsonBuilder();
        gson = builder.create();
        initialization();
        // setUpViewPager();

        btn_add_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddMoneyToWalletActivity.class));
                // Intent intent = new Intent(this,AddMoneyToWalletActivity.class);
            }
        });
    }

    private void setUpViewPager(ModelWalletTransactionsResponse model) {
        viewPagerWallet.setAdapter(new AdapterWalletFragments(getSupportFragmentManager(), model));
        tabLayoutWallet.setupWithViewPager(viewPagerWallet);
        tabLayoutWallet.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    private void initialization() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_text);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvToolbarText.setText(getResources().getString(R.string.WALLET_ACTIVITY_header_text));
        pd = new ProgressDialog(this);
        pd.setMessage(WalletActivity.this.getResources().getString(R.string.loading));

//        btn_add_money.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), AddMoneyToWalletActivity.class));
//               // Intent intent = new Intent(this,AddMoneyToWalletActivity.class);
//            }
//        });


        apiCalling();
    }

    private void apiCalling() {

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("driver_id", "" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID));
        apiManager.execution_method_post("" + Config.ApiKeys.KEY_WALLET_BALANCE, "" + Apis.WALLET_BALANCE_FETCH, data);

    }

    @Override
    protected void onResumeWithConnectionState(boolean connectivityStatus) {

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
    protected void onResume() {
        super.onResume();
        apiCalling();
    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {
        try {

            if (APINAME.equals("" + Config.ApiKeys.KEY_WALLET_BALANCE)) {
                ModelWalletTransactionsResponse model = gson.fromJson("" + script, ModelWalletTransactionsResponse.class);
                ApporioLog.logD("**Wallet", model.toString());
                setWalletBalance(model.getWallet_balance());

                setUpViewPager(model);

            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFetchResultZero(String script) {

    }

    public void setWalletBalance(String str_balance) {
        tvWalletBalance.setText("" + sessionManager.getCurrencyCode() + str_balance);
    }
}

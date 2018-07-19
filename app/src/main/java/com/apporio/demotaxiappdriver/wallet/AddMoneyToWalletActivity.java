package com.apporio.demotaxiappdriver.wallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apporio.demotaxiappdriver.BaseActivity;
import com.apporio.demotaxiappdriver.Config;
import com.apporio.demotaxiappdriver.R;
import com.apporio.demotaxiappdriver.manager.LanguageManager;
import com.apporio.demotaxiappdriver.manager.SessionManager;
import com.apporio.demotaxiappdriver.samwork.ApiManager;
import com.apporio.demotaxiappdriver.urls.Apis;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AddMoneyToWalletActivity extends BaseActivity implements ApiManager.APIFETCHER {

    EditText ed_enter_money;
    TextView amount_first, amount_second, amount_third, Add_Money, done, text_money;
    LinearLayout ll_credit_card, ll_saved_cards;
    EditText edt_card_number, edt_expiry, edt_cvv;
    String a;
    int keyDel;
    SessionManager sessionManager;
    LanguageManager languageManager;
    String language_id;
    public int pos = 0;
    public static ListView lv_cards;
    public static TextView tv_cards;
    ApiManager apiManager;
    String card_id;
    ProgressDialog progress_wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money_to_wallet);

        Log.d("mandir", "Yo");

        progress_wheel = new ProgressDialog(this);
        progress_wheel.setMessage(getResources().getString(R.string.loading));

        apiManager = new ApiManager(AddMoneyToWalletActivity.this);
        ed_enter_money = (EditText) findViewById(R.id.ed_enter_money);
        amount_first = (TextView) findViewById(R.id.am_first_txt);
        amount_second = (TextView) findViewById(R.id.am_second_txt);
        amount_third = (TextView) findViewById(R.id.am_third_txt);
        Add_Money = (TextView) findViewById(R.id.txt_add_money);
        text_money = (TextView) findViewById(R.id.text_money);
        ll_credit_card = (LinearLayout) findViewById(R.id.ll_credit_card);
        edt_card_number = (EditText) findViewById(R.id.edt_card_number);
        edt_expiry = (EditText) findViewById(R.id.edt_expiry);
        edt_cvv = (EditText) findViewById(R.id.edt_cvv);
        done = (TextView) findViewById(R.id.done_money);
        languageManager = new LanguageManager(this);
        sessionManager = new SessionManager(this);
        language_id = languageManager.getLanguageDetail().get(LanguageManager.LANGUAGE_ID);
        lv_cards = (ListView) findViewById(R.id.lv_cards);
        tv_cards = (TextView) findViewById(R.id.tv_card);
        ll_saved_cards = (LinearLayout) findViewById(R.id.ll_saved_cards);

        findViewById(R.id.ll_back_wallet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        amount_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_enter_money.setText("100");
                amount_first.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                amount_first.setTextColor(getResources().getColor(R.color.pure_white));
                amount_second.setBackground(getResources().getDrawable(R.drawable.primary_color_layout_border));
                amount_third.setBackground(getResources().getDrawable(R.drawable.primary_color_layout_border));
                amount_third.setTextColor(getResources().getColor(R.color.colorPrimary));
                amount_second.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });
        amount_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_enter_money.setText("200");
                amount_second.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                amount_second.setTextColor(getResources().getColor(R.color.pure_white));
                amount_first.setBackground(getResources().getDrawable(R.drawable.primary_color_layout_border));
                amount_third.setBackground(getResources().getDrawable(R.drawable.primary_color_layout_border));
                amount_first.setTextColor(getResources().getColor(R.color.colorPrimary));
                amount_third.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });
        amount_third.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_enter_money.setText("300");
                amount_third.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                amount_third.setTextColor(getResources().getColor(R.color.pure_white));
                amount_first.setBackground(getResources().getDrawable(R.drawable.primary_color_layout_border));
                amount_second.setBackground(getResources().getDrawable(R.drawable.primary_color_layout_border));
                amount_first.setTextColor(getResources().getColor(R.color.colorPrimary));
                amount_second.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });


        Add_Money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ed_enter_money.getText().toString().equals("")) {

                    Toast.makeText(AddMoneyToWalletActivity.this, getResources().getString(R.string.please_select_or_enter_money), Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(new Intent(AddMoneyToWalletActivity.this, CreditCardPaymentActivity.class).putExtra("from", "2"), 106);
                }

            }
        });


        edt_expiry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                pos = edt_expiry.getText().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (edt_expiry.getText().length() == 2 && pos != 3) {
                    edt_expiry.setText(edt_expiry.getText().toString() + "/");
                    edt_expiry.setSelection(3);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_card_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                boolean flag = true;
                String eachBlock[] = edt_card_number.getText().toString().split("-");
                for (int i = 0; i < eachBlock.length; i++) {
                    if (eachBlock[i].length() > 4) {
                        flag = false;
                    }
                }
                if (flag) {

                    edt_card_number.setOnKeyListener(new View.OnKeyListener() {

                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {

                            if (keyCode == KeyEvent.KEYCODE_DEL)
                                keyDel = 1;
                            return false;
                        }
                    });

                    if (keyDel == 0) {

                        if (((edt_card_number.getText().length() + 1) % 5) == 0) {

                            if (edt_card_number.getText().toString().split("-").length <= 3) {
                                edt_card_number.setText(edt_card_number.getText() + "-");
                                edt_card_number.setSelection(edt_card_number.getText().length());
                            }
                        }
                        a = edt_card_number.getText().toString();
                    } else {
                        a = edt_card_number.getText().toString();
                        keyDel = 0;
                    }

                } else {
                    edt_card_number.setText(a);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResumeWithConnectionState(boolean connectivityStatus) {

    }


    @Override
    public void onAPIRunningState(int a, String APINAME) {
        if (a == ApiManager.APIFETCHER.KEY_API_IS_STARTED) {
            progress_wheel.show();
        } else {
            progress_wheel.hide();
        }
    }

    @Override
    public void onFetchComplete(Object script, String APINAME) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            if (APINAME.equals("" + Config.ApiKeys.Key_Add_Money_to_Wallet)) {

                AddWalletMoneyResponse walletMoneyResponse = new AddWalletMoneyResponse();
                walletMoneyResponse = gson.fromJson("" + script, AddWalletMoneyResponse.class);
                if (walletMoneyResponse.getResult() == 1) {

                    Toast.makeText(this, "" + walletMoneyResponse.getMsg(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "" + walletMoneyResponse.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }

    }

    @Override
    public void onFetchResultZero(String s) {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CardSessionEvent event) {
        try {
            card_id = event.getCard_id();

            Log.e("EventCardId", "" + card_id);

            addMoneyToWalletMethod();

        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {

        if (res == Activity.RESULT_OK) {
            try {
                if (req == 106) {
                    //  payment_txt.setText("Pay With Card");

                    // PAYMENT_ID = "3";
                    card_id = data.getExtras().getString("CREDIT_ID");

                    Log.e("Card_id", "" + card_id);

                    // addMoneyToWalletMethod();

//                    paystack_authCode = data.getExtras().getString("paystack_authCode");
//                    paystack_last4 = data.getExtras().getString("paystack_last4");
//                    PAYMENT_ID = paystack_authCode;
//                    payment_txt.setText("**** **** **** " + paystack_last4);
                }
            } catch (Exception e) {
                Log.e("res ", e.toString());
            }

        }
        super.onActivityResult(req, res, data);
    }

    private void addMoneyToWalletMethod() {

        apiManager.execution_method_get("" + Config.ApiKeys.Key_Add_Money_to_Wallet, "" + Apis.AddMoneyTOWallet + "driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&amount=" + ed_enter_money.getText().toString() + "&card_id=" + card_id);
    }

}


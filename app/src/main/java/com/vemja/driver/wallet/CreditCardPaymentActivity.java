package com.vemja.driver.wallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vemja.driver.R;
import com.vemja.driver.manager.SessionManager;
import com.vemja.driver.urls.Apis;

import org.greenrobot.eventbus.EventBus;

public class CreditCardPaymentActivity extends AppCompatActivity {

    private static final String TAG = "CreditCardActivity";
    WebView webview;
    SessionManager sessionManager;
    WebView newWebView;

    String finishUrl = "";

    ProgressBar mProgressBar;
    String str_from = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_credit_card_payment);

        mProgressBar = (ProgressBar) findViewById(R.id.pb);

        str_from = getIntent().getStringExtra("from");
        Log.e("From", "" + str_from);

        webview = (WebView) findViewById(R.id.webview);

        //// ///

//        webview.getSettings().setLoadWithOverviewMode(true);
//        webview.getSettings().setUseWideViewPort(true);
//
//        webview.getSettings().setSupportZoom(true);
//        webview.getSettings().setBuiltInZoomControls(true);
//        webview.getSettings().setDisplayZoomControls(false);
//
//        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//        webview.setScrollbarFadingEnabled(false);
//
//        String newUA= "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
//        webview.getSettings().setUserAgentString(newUA);

        ////// ////

        webview.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        sessionManager = new SessionManager(this);
        final WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("" + TAG, "webclint --> " + url);

                finishUrl = url;

                if (url.contains("api/driver_save_card.php")) {
                    newWebView.destroy();

                    //  Toast.makeText(CreditCardPaymentActivity.this, "emter2", Toast.LENGTH_SHORT).show();
                    //webview.destroy();
                    finish();
                    startActivityForResult(new Intent(CreditCardPaymentActivity.this, CreditCardPaymentActivity.class).putExtra("from", "2"), 104);

//
                }


//                if (url.contains("select_card.php")) {
//
//                    passIntentMethod(url);
//                }

                super.onPageStarted(view, url, favicon);
            }


        });


        if (!sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID).equals("")) {
            Log.d("***Url", "" + Apis.paymentUrl + "?driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID));
            renderWebPage(Apis.paymentUrl + "?driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&flag=2");


        } else {
            Toast.makeText(this, "User id not found", Toast.LENGTH_SHORT).show();
        }

    }

    private void passIntentMethod(String url) {

    }

    protected void renderWebPage(final String urlToRender) {
        Log.d("" + TAG, "Loading Url --> " + urlToRender);

        final WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webview.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
            }

            @SuppressLint("NewApi")
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                newWebView = new WebView(CreditCardPaymentActivity.this);
                newWebView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                newWebView.getSettings().setJavaScriptEnabled(true);
                newWebView.getSettings().setSupportZoom(true);
                newWebView.getSettings().setBuiltInZoomControls(true);
                newWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                newWebView.getSettings().setSupportMultipleWindows(true);
                newWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                view.addView(newWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        Log.d("" + TAG, "CreateUrl --> " + url);
                        finishUrl = url;
                        super.onPageStarted(view, url, favicon);
                    }


                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (url.contains("api/driver_save_card.php")) {
                            //  Toast.makeText(CreditCardPaymentActivity.this, "emter", Toast.LENGTH_SHORT).show();
                            view.loadUrl("" + Apis.paymentUrl + "?driver_id=" + sessionManager.getUserDetails().get(SessionManager.KEY_DRIVER_ID) + "&flag=2");
                        }
                        super.onPageFinished(view, url);
                    }
                });

                return true;
            }
        });


        webview.loadUrl(urlToRender);

    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (res == Activity.RESULT_OK) {
            try {
                if (req == 104) {
                    //  payment_txt.setText("Pay With Card");

                    // PAYMENT_ID = "3";
//                    paystack_id = data.getExtras().getString("paystack_id");
//                    paystack_authCode = data.getExtras().getString("paystack_authCode");
//                    paystack_last4 = data.getExtras().getString("paystack_last4");


                    Log.e("******ID==>", "" + data.getStringExtra("CREDIT_ID"));

                    String PAYMENT_ID = "3";
                    String CREDIT_CARD_ID = "" + data.getStringExtra("CREDIT_ID");
                    ;

                    Log.e("Credit_card", "" + CREDIT_CARD_ID);


                }
            } catch (Exception e) {
                Log.e("res ", e.toString());
            }

        }
    }

    public class JavaScriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Log.e("ShowToast", "" + toast);
            // Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();

            EventBus.getDefault().post(new CardSessionEvent(toast));

            if (str_from.equals("0")) {
                Log.e("Card_id_from", "" + toast);
                Intent intent = new Intent();
                intent.putExtra("CREDIT_ID", toast);
                setResult(RESULT_OK, intent);
                finish();
            } else if (str_from.equals("null")) {
                Log.e("Card_id_from", "" + toast);
                Intent intent = new Intent();
                intent.putExtra("CREDIT_ID", toast);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Log.e("Card_id_from", "" + toast);
                Intent intent = new Intent();
                intent.putExtra("CREDIT_ID", toast);
                setResult(RESULT_OK, intent);
                finish();
            }


        }
    }

    @Override
    public void onBackPressed() {

        if (finishUrl.contains("https://checkout.stripe.com/")) {
            finish();

            startActivity(new Intent(this, CreditCardPaymentActivity.class));
        } else {
            finish();
        }

    }
}

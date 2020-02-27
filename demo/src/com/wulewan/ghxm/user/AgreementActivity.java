package com.wulewan.ghxm.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.common.ui.BaseAct;

public class AgreementActivity extends BaseAct {

    private Context context;
    private WebView webView;
    private ProgressBar mProgressBar;

    public static void start(Context context) {
        Intent intent = new Intent(context, AgreementActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        context = this;
        initView();
    }

    private void initView() {
        setToolbar("用户服务协议");
        webView = (WebView) findViewById(R.id.webView_agreement);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        mProgressBar.setMax(100);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 0) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }
        });

        webView.loadUrl("file:////android_asset/agreement.html");//65753843
//        webView.loadUrl("http://www.baidu.com");
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //挂在后台  资源释放
        webView.getSettings().setJavaScriptEnabled(false);
    }

    @Override
    protected void onDestroy() {
        webView.setVisibility(View.GONE);
        webView.destroy();
        super.onDestroy();
    }
}

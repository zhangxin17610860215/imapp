package com.yqbj.ghxm.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.netease.nimlib.jsbridge.util.WebViewConfig;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;

public class MainWebViewActivity extends BaseAct {

    private WebView webView;
    private String url = "";
    private ProgressBar progressBar;
    /**
     * 网页缓存目录
     */
    private static final String cacheDirPath = Environment
            .getExternalStorageDirectory() + "/LoadingWebViewDome/webCache/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadingwebview);
        url = getIntent().getStringExtra("url");
        setToolbar(R.drawable.jrmf_b_top_back, "");
        initWebView();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private void initWebView() {
        webView = findView(R.id.webView);

        progressBar = findView(R.id.webview_loadingr);
        WebSettings settings = webView.getSettings();
        WebViewConfig.setWebSettings(this, settings, cacheDirPath);
        WebViewConfig.removeJavascriptInterfaces(webView);
        WebViewConfig.setWebViewAllowDebug(false);
        WebViewConfig.setAcceptThirdPartyCookies(webView);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(view.GONE);
                } else {
                    if (progressBar.getVisibility() == view.GONE)
                        progressBar.setVisibility(view.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
            public void onReceivedTitle(WebView view, String title) {
                setTitle(title);
            }

        });
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if (TextUtils.equals(uri.getScheme(), "http") || TextUtils.equals(uri.getScheme(), "https")) {
                    view.loadUrl(url);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return true;
            }
        });
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url);
        }
    }
}

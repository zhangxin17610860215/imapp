package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.netease.nimlib.jsbridge.util.WebViewConfig;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.TeamRobotDetatlsBean;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.utils.CrypticUtil;

public class RobotWebViewActivity extends BaseAct {

    private Context context;

    private String teamId;
    private TeamRobotDetatlsBean robotBean;

    /**
     * 网页缓存目录
     */
    private static final String cacheDirPath = Environment
            .getExternalStorageDirectory() + "/LoadingWebViewDome/webCache/";

    private WebView webView;
    private ProgressBar progressBar;

    public static void start(Context context, String teamId, TeamRobotDetatlsBean robotBean) {
        Intent intent = new Intent();
        intent.setClass(context, RobotWebViewActivity.class);
        intent.putExtra("teamId", teamId);
        intent.putExtra("bindBean", robotBean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_assistant_web_layout);
        context = this;

        teamId = getIntent().getStringExtra("teamId");
        robotBean = (TeamRobotDetatlsBean) getIntent().getSerializableExtra("bindBean");

        if (null == robotBean){
            return;
        }
        initView();

        setToolbar(R.drawable.jrmf_b_top_back,robotBean.getNickname());
    }

    private void initView() {
        progressBar = findView(R.id.webview_loadingr);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
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
        autograph();

    }

    /**
     * 参数签名
     * */
    private void autograph() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("group_id").append(teamId).append("owner_id").append(NimUIKit.getAccount()).append("robot_id").append(robotBean.getAccid()).append("QYcuSJ7zoBH4T4Gq");
        String str = stringBuilder.toString();
        Log.e("TAG",">>>>>str>>>>>" + str);
        String md5Str = CrypticUtil.md5(str).toLowerCase();
        Log.e("TAG",">>>>>md5Str>>>>>" + md5Str);

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(robotBean.getBindingUrl())
                .append("?")
                .append("group_id=")
                .append(teamId + "&")
                .append("owner_id=")
                .append(NimUIKit.getAccount() + "&")
                .append("robot_id=")
                .append(robotBean.getAccid() + "&")
                .append("sign=")
                .append(md5Str);
        String url = urlBuilder.toString();
        Log.e("TAG",">>>>>url>>>>>" + url);
        webView.loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != webView){
            webView.destroy();
            webView = null;
        }
    }
}

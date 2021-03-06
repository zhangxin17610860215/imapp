package com.yqbj.ghxm.main.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.mixpush.MixPushService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yqbj.avchatkit.activity.AVChatActivity;
import com.netease.yqbj.avchatkit.constant.AVChatExtras;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.util.log.LogUtil;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.NimApplication;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.CheckVersionBean;
import com.yqbj.ghxm.bean.LoginBean;
import com.yqbj.ghxm.bean.RootListBean;
import com.yqbj.ghxm.cache.MyRootInfoCache;
import com.yqbj.ghxm.common.util.sys.SysInfoUtil;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.config.preference.Preferences;
import com.yqbj.ghxm.login.NewLoginActivity;
import com.yqbj.ghxm.main.SplashActivity;
import com.yqbj.ghxm.mixpush.DemoMixPushMessageHandler;
import com.yqbj.ghxm.requestutils.api.OverallApi;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.share.ShareSchemeActivity;
import com.yqbj.ghxm.utils.Base64;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.view.DialogUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * 欢迎/导航页（app启动Activity）
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class WelcomeActivity extends UI {

    private String TAG = "WelcomeActivity";

    private boolean customSplash = false;

    private static boolean firstEnter = true; // 是否首次进入

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_welcome);

        DemoCache.setMainTaskLaunching(true);

        if (savedInstanceState != null) {
            setIntent(new Intent()); // 从堆栈恢复，不再重复解析之前的intent
        }

        if (!firstEnter) {
            onIntent(); // APP进程还在，Activity被重新调度起来
        } else {
            showSplashView(); // APP进程重新起来
        }
    }

    private void showSplashView() {
        // 首次进入，打开欢迎界面
        getWindow().setBackgroundDrawableResource(R.drawable.splash_bg);
        customSplash = true;
    }

    //检查是否更新
    private void checkVerSion() {
        OverallApi.checkVersion(2, WelcomeActivity.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    final CheckVersionBean bean = (CheckVersionBean) object;
                    if (bean.getUpgrade() == 1 && !StringUtil.getAppVersionName(WelcomeActivity.this).equals(bean.getVersionno())){
                        //需要更新
                        dialog = DialogUtils.showOneButtonDiolag(WelcomeActivity.this, bean, new DialogUtils.OnDialogActionListener() {
                            @Override
                            public void doCancelAction() {
                                dialog.dismiss();
                                init();
                            }

                            @Override
                            public void doOkAction() {
                                dialog.dismiss();
                                Intent intent= new Intent();
                                intent.setAction("android.intent.action.VIEW");
                                Uri content_url = Uri.parse(bean.getDownloadUrl());
                                intent.setData(content_url);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                WelcomeActivity.this.startActivity(intent);
                            }
                        });
                        dialog.show();
                    }else {
                        init();
                    }
                }else {
                    init();
                }
            }

            @Override
            public void onFailed(String errMessage) {
//                toast(errMessage);
                init();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        /*
         * 如果Activity在，不会走到onCreate，而是onNewIntent，这时候需要setIntent
         * 场景：点击通知栏跳转到此，会收到Intent
         */
        setIntent(intent);
        if (!customSplash) {
            onIntent();
        }
    }

    public static void setFirstEnterState(boolean firstEnterState){
        firstEnter = firstEnterState;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getRobotList();
        checkVerSion();
    }

    private void getRobotList() {
        MyRootInfoCache.getInstance().buildCache(WelcomeActivity.this,new MyRootInfoCache.VisitCallback() {
            @Override
            public void onSuccess(int code, RootListBean rootListBean) {
                if (code == Constants.SUCCESS_CODE){
                    checkVerSion();
                }
            }

            @Override
            public void onFailed(String errorMessage) {
//                toast(errorMessage + "请检查网络环境后重试");
            }
        });
    }

    private void init() {
        if (firstEnter) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!NimUIKit.isInitComplete()) {
                        new Handler().postDelayed(this, 100);
                        return;
                    }
                    customSplash = false;
                    if (canAutoLogin()) {
                        login();
                    } else {
                        Intent intent = getIntent();
                        if (null != intent.getData()){
                            Uri uri = intent.getData();
                            String scheme = uri.getScheme();
                            if (!TextUtils.isEmpty(scheme)&&scheme.endsWith("YQIChat")){
                                String data = uri.toString().substring(10,uri.toString().length());
                                byte[] decode = Base64.decode(data);
                                Constants.decodeData = new String(decode);
                            }
                        }
                        NewLoginActivity.start(WelcomeActivity.this);
                        finish();
                    }
                }
            };
            if (customSplash) {
                new Handler().postDelayed(runnable, 1000);
            } else {
                runnable.run();
            }
        }
    }

    /**
     * 再登录一次
     * */
    private void login() {
        OverallApi.getKey(WelcomeActivity.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                String openid = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.OPENID);
                String uuid = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.UUID);
                UserApi.login(null,openid,uuid,WelcomeActivity.this, new requestCallback() {
                    @Override
                    public void onSuccess(int code, Object object) {
                        if (code == Constants.SUCCESS_CODE){
                            LoginBean loginBean = (LoginBean) object;
                            SPUtils.getInstance().put(Constants.USER_TYPE.USERTOKEN, loginBean.getUserToken());
                            SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN, loginBean.getYunxinToken());
                            SPUtils.getInstance().put(Constants.USER_TYPE.ACCID, loginBean.getAccid());
                            onIntent();
                        }
                    }

                    @Override
                    public void onFailed(String errMessage) {

                    }
                });
            }
            @Override
            public void onFailed(String errMessage) {

            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DemoCache.setMainTaskLaunching(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    // 处理收到的Intent
    private void onIntent() {
        if (TextUtils.isEmpty(DemoCache.getAccount())) {
            // 判断当前app是否正在运行
            if (!SysInfoUtil.stackResumed(WelcomeActivity.this)) {
                NewLoginActivity.start(WelcomeActivity.this);
            }
            finish();
        } else {
            MyRootInfoCache.getInstance().buildCache(WelcomeActivity.this,new MyRootInfoCache.VisitCallback() {
                @Override
                public void onSuccess(int code, RootListBean rootListBean) {
                    configInfo();
                }

                @Override
                public void onFailed(String errorMessage) {
                    configInfo();
                }
            });

        }
    }

    /**
     * 已经登录    获取全局配置
     * */
    private void configInfo() {
        OverallApi.configInfo(WelcomeActivity.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    // 已经登录过了，处理过来的请求
                    Intent intent = getIntent();
                    if (intent != null) {
                        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
                            parseNotifyIntent(intent);
                            return;
                        } else if (NIMClient.getService(MixPushService.class).isFCMIntent(intent)) {
                            parseFCMNotifyIntent(NIMClient.getService(MixPushService.class).parseFCMPayload(intent));
                        } else if (intent.hasExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION) || intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT)) {
                            parseNormalIntent(intent);
                        }else {
                            Uri uri = intent.getData();
                            if (uri != null) {
                                String scheme = uri.getScheme();
                                if (!TextUtils.isEmpty(scheme)&&scheme.endsWith("YQIChat")){
                                    String data = uri.toString().substring(10,uri.toString().length());
                                    byte[] decode = Base64.decode(data);
                                    showMainActivity(new Intent().putExtra(ShareSchemeActivity.ACTION_BROWSABLE_LAUNCHER, new String(decode)));
                                }

                            }
                        }
                    }

                    if (!firstEnter && intent == null) {
                        finish();
                    } else {
                        showMainActivity();
                    }
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                toast(errMessage);
            }
        });
    }

    /**
     * 已经登陆过，自动登陆
     */
    private boolean canAutoLogin() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();

        Log.i(TAG, "get local sdk token =" + token);
        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token);
    }

    private void parseNotifyIntent(Intent intent) {
        ArrayList<IMMessage> messages = (ArrayList<IMMessage>) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
        if (messages == null || messages.size() > 1) {
            showMainActivity(null);
        } else {
            showMainActivity(new Intent().putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, messages.get(0)));
        }
    }

    private void parseFCMNotifyIntent(String payloadString) {
        Map<String, String> payload = JSON.parseObject(payloadString, Map.class);
        String sessionId = payload.get(DemoMixPushMessageHandler.PAYLOAD_SESSION_ID);
        String type = payload.get(DemoMixPushMessageHandler.PAYLOAD_SESSION_TYPE);
        if (sessionId != null && type != null) {
            int typeValue = Integer.valueOf(type);
            IMMessage message = MessageBuilder.createEmptyMessage(sessionId, SessionTypeEnum.typeOfValue(typeValue), 0);
            showMainActivity(new Intent().putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, message));
        } else {
            showMainActivity(null);
        }
    }

    private void parseNormalIntent(Intent intent) {
        showMainActivity(intent);
    }

    private void showMainActivity() {
        showMainActivity(null);
    }

    private void showMainActivity(Intent intent) {
        SplashActivity.start(WelcomeActivity.this, intent);
        finish();
    }

}

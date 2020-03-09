package com.yqbj.ghxm.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.support.permission.MPermission;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.NimApplication;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.LoginBean;
import com.yqbj.ghxm.bean.RootListBean;
import com.yqbj.ghxm.bean.WXMesBean;
import com.yqbj.ghxm.cache.MyRootInfoCache;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.config.preference.Preferences;
import com.yqbj.ghxm.config.preference.UserPreferences;
import com.yqbj.ghxm.main.SplashActivity;
import com.yqbj.ghxm.requestutils.RequestHelp;
import com.yqbj.ghxm.requestutils.api.OverallApi;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.share.ShareSchemeActivity;
import com.yqbj.ghxm.user.BindPhoneActivity;
import com.yqbj.ghxm.utils.EventBusUtils;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;

import org.greenrobot.eventbus.Subscribe;

import static com.netease.yqbj.uikit.api.StatisticsConstants.LOGIN_PHONELOGIN;
import static com.netease.yqbj.uikit.api.StatisticsConstants.LOGIN_WCHATLOGIN;

public class NewLoginActivity extends UI implements View.OnKeyListener {

    private static final String TAG = NewLoginActivity.class.getSimpleName();
    private static final String KICK_OUT = "KICK_OUT";
    private final int BASIC_PERMISSION_REQUEST_CODE = 110;
    private RelativeLayout llGoLogin;
    private RelativeLayout llLoginPhone;
    private AbortableFuture<LoginInfo> loginRequest;

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, NewLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_login_activity);
        EventBusUtils.register(this);
        llGoLogin = findView(R.id.ll_login_weixin);
        llLoginPhone = findView(R.id.ll_login_phone);

        llLoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NewLoginActivity.this,LOGIN_PHONELOGIN);
                getKey();
            }
        });

        llGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NewLoginActivity.this,LOGIN_WCHATLOGIN);
                showProgress(NewLoginActivity.this,true);
                goWeiXinLogin();
//                yunXinLogin();
            }
        });

        requestBasicPermission();
        onParseIntent();
    }

    /**
     * 请求权限结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * 请求基本权限
     */
    private void requestBasicPermission() {
        MPermission.with(NewLoginActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    /**
     * 解析意图
     */
    private void onParseIntent() {
        if (!getIntent().getBooleanExtra(KICK_OUT, false)) {
            return;
        }
        int type = NIMClient.getService(AuthService.class).getKickedClientType();
        String client;
        switch (type) {
            case ClientType.Web:
                client = "网页端";
                break;
            case ClientType.Windows:
            case ClientType.MAC:
                client = "电脑端";
                break;
            case ClientType.REST:
                client = "服务端";
                break;
            default:
                client = "移动端";
                break;
        }
        EasyAlertDialogHelper.showOneButtonDiolag(NewLoginActivity.this,
                getString(R.string.kickout_notify),
                String.format(getString(R.string.kickout_content), client),
                getString(R.string.ok),
                true,
                null).show();

    }

    private void getKey() {
        OverallApi.getKey(this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    BindPhoneActivity.start(NewLoginActivity.this, "login");
                }
            }

            @Override
            public void onFailed(String errMessage) {
                toast(errMessage);
            }
        });
    }

    private void goWeiXinLogin() {
        if (NimApplication.api == null) {
            NimApplication.api = WXAPIFactory.createWXAPI(NewLoginActivity.this, NimApplication.APP_ID, true);
        }
        if (!NimApplication.api.isWXAppInstalled()) {
            Toast.makeText(this, "您手机尚未安装微信，请安装后再登录", Toast.LENGTH_LONG).show();
            dismissProgress();
            return;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_xb_live_state";
        // 官方说明：用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击），
        // 建议第三方带上该参数，可设置为简单的随机数加session进行校验
        NimApplication.api.sendReq(req);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RequestHelp.cancelRequest(this);
        EventBusUtils.unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Subscribe
    public void getWXLoginData(EventBusUtils.CommonEvent commonEvent) {
        if (null == commonEvent) {
            return;
        }
        if (commonEvent.id != 101){
            return;
        }
        if (null == commonEvent.data){
            dismissProgress();
            return;
        }
        Bundle bundle = commonEvent.data;
//        showProgress(NewLoginActivity.this, false);
        final WXMesBean bean = (WXMesBean) bundle.getSerializable("WXMesBean");
        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        instance.put(Constants.ALIPAY_USERINFO.ACCESSTOKEN,bean.getAccess_token());
        instance.put(Constants.ALIPAY_USERINFO.OPENID,bean.getOpenid());
        instance.put(Constants.ALIPAY_USERINFO.UUID,bean.getUnionid());
        UserApi.login(bean.getAccess_token(), bean.getOpenid(), bean.getUnionid(), this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.RESPONSE_CODE.CODE_10022) {
                    //用户未注册
                    goSignUp(bean.getAccess_token(), bean.getOpenid(), bean.getUnionid());
                    return;
                }
                LoginBean loginBean = (LoginBean) object;
                SPUtils.getInstance().put(Constants.USER_TYPE.USERTOKEN, loginBean.getUserToken());
                SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN, loginBean.getYunxinToken());
                SPUtils.getInstance().put(Constants.USER_TYPE.ACCID, loginBean.getAccid());
                if (code == Constants.RESPONSE_CODE.CODE_10013) {
                    //手机号未绑定
                    BindPhoneActivity.start(NewLoginActivity.this);
                    return;
                }
                if (code == Constants.SUCCESS_CODE) {
                    getRobotList();
                    return;
                }
                ToastHelper.showToast(NewLoginActivity.this, "登录失败");
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastHelper.showToast(NewLoginActivity.this, errMessage);
            }
        });
    }

    private void goSignUp(String access_token, String openid, String unionid) {
        showProgress(NewLoginActivity.this, false);
        UserApi.signUp(access_token, openid, unionid, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                LoginBean loginBean = (LoginBean) object;
                SPUtils.getInstance().put(Constants.USER_TYPE.USERTOKEN, loginBean.getUserToken());
                SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN, loginBean.getYunxinToken());
                SPUtils.getInstance().put(Constants.USER_TYPE.ACCID, loginBean.getAccid());
                if (code == Constants.RESPONSE_CODE.CODE_10013) {
                    //手机号未绑定
                    BindPhoneActivity.start(NewLoginActivity.this);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastHelper.showToast(NewLoginActivity.this, errMessage);
            }
        });
    }

    private void yunXinLogin() {
        // 云信只提供消息通道，并不包含用户资料逻辑。开发者需要在管理后台或通过服务器接口将用户帐号和token同步到云信服务器。
        // 在这里直接使用同步到云信服务器的帐号和token登录。
        // 如果开发者直接使用这个demo，只更改appkey，然后就登入自己的账户体系的话，需要传入同步到云信服务器的token，而不是用户密码。
        showProgress(NewLoginActivity.this, false);
//        //零钱助手
//        SPUtils.getInstance().put(Constants.USER_TYPE.ACCID,"10000394");
//        SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN,"1cd9b146dc4e07f540ba3f52e8bf1801");
//        //公会小蜜小助手
//        SPUtils.getInstance().put(Constants.USER_TYPE.ACCID,"10000395");
//        SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN,"d3dbaec6e57738071bae73058317a072");
        final String account = SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID);
        final String token = SPUtils.getInstance().getString(Constants.USER_TYPE.YUNXINTOKEN);
        loginRequest = NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
                dismissProgress();
                onLoginDone();
                DemoCache.setAccount(account);
                saveLoginInfo(account, token);
                // 初始化消息提醒配置
                initNotificationConfig();
                // 进入主界面
                if (StringUtil.isNotEmpty(Constants.decodeData)){
                    Intent intent = new Intent();
                    intent.putExtra(ShareSchemeActivity.ACTION_BROWSABLE_LAUNCHER, Constants.decodeData);
                    SplashActivity.start(NewLoginActivity.this,intent);
                }else {
                    SplashActivity.start(NewLoginActivity.this);
                }
                finish();
            }

            @Override
            public void onFailed(int code) {
                dismissProgress();
                onLoginDone();
                if (code == 302 || code == 404) {
                    ToastHelper.showToast(NewLoginActivity.this, R.string.login_failed);
                } else {
                    ToastHelper.showToast(NewLoginActivity.this, "登录失败: " + code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                dismissProgress();
                ToastHelper.showToast(NewLoginActivity.this, R.string.login_exception);
                onLoginDone();
            }
        });
    }

    private void getRobotList() {
        MyRootInfoCache.getInstance().buildCache(this,new MyRootInfoCache.VisitCallback() {
            @Override
            public void onSuccess(int code, RootListBean rootListBean) {
                if (code == Constants.SUCCESS_CODE){
                    OverallApi.configInfo(NewLoginActivity.this, new requestCallback() {
                        @Override
                        public void onSuccess(int code, Object object) {

                        }

                        @Override
                        public void onFailed(String errMessage) {

                        }
                    });
                    yunXinLogin();
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                toast(errorMessage + "请检查网络环境后重试");
            }
        });
    }

    private void onLoginDone() {
        loginRequest = null;
    }

    private void saveLoginInfo(final String account, final String token) {
        Preferences.saveUserAccount(account);
        Preferences.saveUserToken(token);
    }

    private void initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
        // 加载状态栏配置
        StatusBarNotificationConfig statusBarNotificationConfig = UserPreferences.getStatusConfig();
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.getNotificationConfig();
            UserPreferences.setStatusConfig(statusBarNotificationConfig);
        }
        // 更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig);
    }
}

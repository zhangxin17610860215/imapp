package com.yqbj.ghxm.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.yqbj.ghxm.NimApplication;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.BaseBean;
import com.yqbj.ghxm.bean.LoginBean;
import com.yqbj.ghxm.bean.RootListBean;
import com.yqbj.ghxm.main.SplashActivity;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.util.log.LogUtil;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.cache.MyRootInfoCache;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.config.preference.Preferences;
import com.yqbj.ghxm.config.preference.UserPreferences;
import com.yqbj.ghxm.contact.helper.UserUpdateHelper;
import com.yqbj.ghxm.requestutils.api.OverallApi;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.share.ShareSchemeActivity;
import com.yqbj.ghxm.utils.KeyboardHelper;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;

/**
 * 绑定手机号
 */
public class BindPhoneActivity extends BaseAct implements View.OnClickListener {

    public static final String TAG = BindPhoneActivity.class.getSimpleName();
    //    private AbortableFuture<LoginInfo> loginRequest;
    private TextView txtPhoneType;
    private TextView tvSignUp;
    private TextView tvVerCode;
    private TextView tvAgreement;
    private ImageView imgBack;
    private EditText etPhone;
    private EditText etVerCode;

    private String mobile = "";
    private String code = "";

    private Handler mHandler = new Handler();

    private Runnable mRunnable;
    private int mSeconds = 30;

    private boolean isPhoneLogin = false;

    public static final String PHONE_TYPE = "phone_type";

    public static void start(Context context) {
        Intent intent = new Intent(context, BindPhoneActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, String type) {
        Intent intent = new Intent(context, BindPhoneActivity.class);
        intent.putExtra(PHONE_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_phone);
        if (getIntent().getExtras() != null) {
            isPhoneLogin = getIntent().getExtras().get(PHONE_TYPE).equals("login");
        }
        initView();
    }

    private void initData() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mSeconds--;
                if (mSeconds <= 0) {
                    tvVerCode.setText("重新获取验证码");
                    mRunnable = null;
                    mSeconds = 30;
                } else {
                    tvVerCode.setText(mSeconds + "s后重新获取");

                    mHandler.postDelayed(mRunnable, 1000);
                }
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
    }

    private void initView() {
        tvSignUp = findView(R.id.tv_bind);
        tvVerCode = findView(R.id.tv_bind_verCode);
        tvAgreement = findView(R.id.tv_bind_Agreement);
        imgBack = findView(R.id.img_bindPhone_back);
        etPhone = findView(R.id.et_bind_phoneNu);
        etVerCode = findView(R.id.et_bind_verCode);
        txtPhoneType = findView(R.id.txt_phone_type);

        tvSignUp.setOnClickListener(this);
        tvVerCode.setOnClickListener(this);
        imgBack.setOnClickListener(this);

        if (isPhoneLogin) {
            txtPhoneType.setText("验证码登录");
            tvSignUp.setText("登录");
        }

        String str = "点击“"+tvSignUp.getText().toString()+"”即表示已阅读并同意\n《用户服务协议》";
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(str);

        // 单独设置字体颜色
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#859CED"));
        spannableBuilder.setSpan(colorSpan, 15, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                AgreementActivity.start(BindPhoneActivity.this);
            }

            @Override
            public void updateDrawState(TextPaint paint) {
                paint.setColor(Color.parseColor("#859CED"));
                // 设置下划线 true显示、false不显示
                paint.setUnderlineText(false);
                // paint.setStrikeThruText(true);
            }
        };
        spannableBuilder.setSpan(clickableSpan, 15, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 不设置点击不生效
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
        tvAgreement.setText(spannableBuilder);
        // 去掉点击后文字的背景色
        tvAgreement.setHighlightColor(Color.parseColor("#00000000"));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard(true);
            }
        }, 450);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_bind:
                //绑定
                mobile = etPhone.getText().toString().trim();
                code = etVerCode.getText().toString().trim();
                if (TextUtils.isEmpty(mobile)) {
                    ToastHelper.showToast(this, "请输入手机号");
                    return;
                }
                if (TextUtils.isEmpty(code)) {
                    ToastHelper.showToast(this, "请输入验证码");
                    return;
                }

                if (isPhoneLogin) {
                    loginPhone();
                } else {
                    bindPhone();
                }

                break;
            case R.id.tv_bind_verCode:
                //获取短信验证码
                mobile = etPhone.getText().toString().trim();
                if (null == mRunnable) {
                    if (TextUtils.isEmpty(mobile)) {
                        ToastHelper.showToast(this, "请输入手机号");
                        return;
                    }
                    if (!StringUtil.isMobileNO(mobile)) {
                        ToastHelper.showToast(this, "请输入正确的手机号码");
                        return;
                    }
                    if (isPhoneLogin) {
                        getLoginVerCode();
                    } else {
                        getVerCode();
                    }
                    initData();
                }
                break;
            case R.id.img_bindPhone_back:
                //返回
                finish();
                break;
        }
    }

    private void getLoginVerCode() {
        showProgress(this, false);
        UserApi.getLoginVerCode(mobile, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
            }

            @Override
            public void onFailed(String errMessage) {
                toast(errMessage);
                dismissProgress();
            }
        });
    }

    private void loginPhone() {
        showProgress(this, false);
        UserApi.loginPhone(mobile, code, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    LoginBean loginBean = (LoginBean) object;
                    SPUtils.getInstance().put(Constants.USER_TYPE.USERTOKEN, loginBean.getUserToken());
                    SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN, loginBean.getYunxinToken());
                    SPUtils.getInstance().put(Constants.USER_TYPE.ACCID, loginBean.getAccid());
                    SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                    instance.put(Constants.ALIPAY_USERINFO.OPENID,loginBean.getOpenid());
                    instance.put(Constants.ALIPAY_USERINFO.UUID,loginBean.getUuid());
                    //手机号保存成功
                    getRobotList();
                } else {
//                    if (code == Constants.RESPONSE_CODE.CODE_10022) {
//                        //用户未注册
//                        goWeiXinLogin();
//                        finish();
//                    }
                    if (code == Constants.RESPONSE_CODE.CODE_10013) {
                        //未绑定手机号
                        toast("该手机号未绑定，请切换微信登录。");
//                        EasyAlertDialogHelper.showCommonDialog(BindPhoneActivity.this, null, "您的手机号还没有注册过,现在去注册吗~ ?", "去注册", "不了", true, new EasyAlertDialogHelper.OnDialogActionListener() {
//                            @Override
//                            public void doCancelAction() {
//
//                            }
//
//                            @Override
//                            public void doOkAction() {
//                                goWeiXinLogin();
//                            }
//                        }).show();
                        return;
                    }
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastHelper.showToast(BindPhoneActivity.this, errMessage);
            }
        });
    }

    private void goWeiXinLogin() {
        if (NimApplication.api == null) {
            NimApplication.api = WXAPIFactory.createWXAPI(this, NimApplication.APP_ID, true);
        }
        if (!NimApplication.api.isWXAppInstalled()) {
            Toast.makeText(this, "您手机尚未安装微信，请安装后再登录", Toast.LENGTH_LONG).show();
            return;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_xb_live_state";
        // 官方说明：用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击），
        // 建议第三方带上该参数，可设置为简单的随机数加session进行校验
        NimApplication.api.sendReq(req);
        finish();
    }

    private void getVerCode() {
        showProgress(this, false);
        UserApi.getVerCode(mobile, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code != Constants.SUCCESS_CODE){
                    BaseBean bean = (BaseBean) object;
                    toast(bean.getMessage());
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    private void bindPhone() {
        showProgress(this, false);
        UserApi.bindPhone(mobile, code, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE) {
                    UserUpdateHelper.update(UserInfoFieldEnum.MOBILE, mobile, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int i, Void aVoid, Throwable throwable) {
                            //手机号保存成功
                            getRobotList();
                        }
                    });
                } else {
                    dismissProgress();
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastHelper.showToast(BindPhoneActivity.this, errMessage);
            }
        });
    }

    private void yunXinLogin() {
        // 云信只提供消息通道，并不包含用户资料逻辑。开发者需要在管理后台或通过服务器接口将用户帐号和token同步到云信服务器。
        // 在这里直接使用同步到云信服务器的帐号和token登录。
        // 如果开发者直接使用这个demo，只更改appkey，然后就登入自己的账户体系的话，需要传入同步到云信服务器的token，而不是用户密码。
        final String account = SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID);
        final String token = SPUtils.getInstance().getString(Constants.USER_TYPE.YUNXINTOKEN);

        NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
                dismissProgress();
                LogUtil.e(TAG, "yunXin_Login success");
                OverallApi.configInfo(BindPhoneActivity.this);
                onLoginDone();
                DemoCache.setAccount(account);
                saveLoginInfo(account, token);
                // 初始化消息提醒配置
                initNotificationConfig();
                // 进入主界面
                if (StringUtil.isNotEmpty(Constants.decodeData)){
                    Intent intent = new Intent();
                    intent.putExtra(ShareSchemeActivity.ACTION_BROWSABLE_LAUNCHER, Constants.decodeData);
                    SplashActivity.start(BindPhoneActivity.this,intent);
                }else {
                    SplashActivity.start(BindPhoneActivity.this);
                }
                finish();
            }

            @Override
            public void onFailed(int code) {
                dismissProgress();
                onLoginDone();
                if (code == 302 || code == 404) {
                    ToastHelper.showToast(BindPhoneActivity.this, R.string.login_failed);
                } else {
                    ToastHelper.showToast(BindPhoneActivity.this, "登录失败: " + code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                dismissProgress();
                ToastHelper.showToast(BindPhoneActivity.this, R.string.login_exception);
                onLoginDone();
            }
        });
    }

    private void getRobotList() {
        MyRootInfoCache.getInstance().buildCache(this,new MyRootInfoCache.VisitCallback() {
            @Override
            public void onSuccess(int code, RootListBean rootListBean) {
                if (code == Constants.SUCCESS_CODE){
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
//        loginRequest = null;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mHandler) {
            mHandler.removeCallbacks(mRunnable);
        }
        KeyboardHelper.getInstance().hideKeyBoard(this, etPhone);
        KeyboardHelper.getInstance().hideKeyBoard(this, etVerCode);
    }

}

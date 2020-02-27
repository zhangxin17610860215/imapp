package com.wulewan.ghxm.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.AliPayInfoBean;
import com.wulewan.ghxm.bean.SignParamsBean;
import com.wulewan.ghxm.bean.UserInfoBean;
import com.wulewan.ghxm.pay.AliPayResult;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.utils.NoDoubleClickUtils;
import com.umeng.analytics.MobclickAgent;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.contact.helper.UserUpdateHelper;
import com.wulewan.ghxm.login.AliPayLogin;
import com.wulewan.ghxm.login.AuthorizationState;
import com.wulewan.ghxm.redpacket.wallet.SettingPayPasswordActivity;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.utils.Base64;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

import static com.netease.wulewan.uikit.api.StatisticsConstants.COIN_ALIBIND_SUCCESSNUM;

/**
 * 找回支付密码  or 绑定支付宝短信验证
 * */
public class RetrievePayPwdActivity extends BaseAct implements View.OnClickListener {

    private static final String TAG = RetrievePayPwdActivity.class.getSimpleName();

    private TextView tvPhone;
    private TextView tvSendCode;
    private TextView tvDetermine;
    private EditText etCode;

    private String code = "";
    private String phone;
    private Handler mHandler;
    private Runnable mRunnable;
    private int mSeconds = 60;
    private String type;               //type=1   找回支付密码       type=2   绑定支付宝短信验证

    public static void start(Context context, String type) {
        Intent intent = new Intent(context, RetrievePayPwdActivity.class);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retrievepaypwd_activity);
        initView();
    }

    private void initView() {
        type = getIntent().getStringExtra("type");
        if (type.equals("1")){
            setToolbar("找回支付密码");
        }else if (type.equals("2")){
            setToolbar("绑定验证");
        }

        tvPhone = findView(R.id.tv_retrievepaypwd_phone);
        tvSendCode = findView(R.id.tv_retrievepaypwd_sendCode);
        tvDetermine = findView(R.id.tv_retrievepaypwd_Determine);
        etCode = findView(R.id.et_retrievepaypwd);
        tvSendCode.setOnClickListener(this);
        tvDetermine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()){
                case R.id.tv_retrievepaypwd_sendCode:
                    //发送验证码
                    if (null == mRunnable){
                        if (type.equals("1")){
                            getVerCode();
                        }else if (type.equals("2")){
                            getCode();
                        }
                    }
                    break;
                case R.id.tv_retrievepaypwd_Determine:
                    //确定
                    code = etCode.getText().toString();
                    if (StringUtil.isEmpty(code)){
                        ToastUtil.showToast(this,"请输入验证码");
                        return;
                    }
                    if (type.equals("1")){
                        checkCode();
                    }else if (type.equals("2")){
                        checkAliPayCode();
                    }
                    break;
            }
        }
    }

    private void checkAliPayCode() {
        showProgress(this,false);
        UserApi.checkAliPayCode(phone, code, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code != Constants.SUCCESS_CODE){
                    toast((String) object);
                }else {
                    goAliPayLogin();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    private void goAliPayLogin() {
        final AliPayLogin payLogin = new AliPayLogin(this);
        String info = payLogin.getInfo(false);
        String baseInfo = "";
        try {
            baseInfo = Base64.encode(info.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        showProgress(this, false);
        UserApi.singParams("3",baseInfo, 1,null,null,null,null,null,null,null,null,this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    SignParamsBean bean = (SignParamsBean) object;
                    payLogin.goAliPayLogin(RetrievePayPwdActivity.this, payLogin.getInfo(true), bean.getSign(), new AuthorizationState() {
                        @Override
                        public void onSuccess(String code, AliPayResult object) {
                            //授权成功
                            if (code.equals("9000")) {
                                authorizationSuccess(object);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        toast("授权失败");
                                        dismissProgress();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailed(final String errMessage) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (errMessage.equals("6001")) {
                                        toast("用户已取消");
                                    } else {
                                        toast("授权失败");
                                    }
                                    dismissProgress();
                                }
                            });
                        }
                    });
                } else {
                    toast((String) object);
                    dismissProgress();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    private void authorizationSuccess(AliPayResult object) {
        AliPayResult payResult = object;
        if (null == payResult) {
            return;
        }
        if (StringUtil.isEmpty(payResult.getResult())) {
            return;
        }
        String[] strs = payResult.getResult().split("&");
        Map<String, String> map = new HashMap<>();
        for (String s : strs) {
            String[] ms = s.split("=");
            map.put(ms[0], ms[1]);
        }

        getAliPayInfo(map.get("auth_code"));
    }

    private void getAliPayInfo(String authCode) {
//        showProgress(this,false);
        UserApi.getAliPayInfo(authCode, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    MobclickAgent.onEvent(RetrievePayPwdActivity.this,COIN_ALIBIND_SUCCESSNUM);
                    AliPayInfoBean bean = (AliPayInfoBean) object;
                    SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                    instance.put(Constants.ALIPAY_USERINFO.ISBINDALIPAY, true);
                    instance.put(Constants.ALIPAY_USERINFO.AVATAR, bean.getAvatar());
                    instance.put(Constants.ALIPAY_USERINFO.NICKNAME, bean.getNickName());
                    instance.put(Constants.ALIPAY_USERINFO.USERID, bean.getUserId());

                    Map<String, Object> extensionMap = new HashMap<>();
                    extensionMap.put(Constants.ALI_USERNAME,bean.getNickName());
                    extensionMap.put(Constants.ALI_USERID,bean.getUserId());
                    UserUpdateHelper.update(UserInfoFieldEnum.EXTEND, extensionMap, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int i, Void aVoid, Throwable throwable) {
                            //用户信息保存成功
                        }
                    });
                } else {
                    ToastUtil.showToast(RetrievePayPwdActivity.this, (String) object);
                }
                finish();
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(RetrievePayPwdActivity.this, errMessage);
            }
        });
    }

    private void getCode() {
        showProgress(this,false);
        UserApi.getAliPayCode(phone, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code != Constants.SUCCESS_CODE){
                    toast((String) object);
                }else {
                    initData();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    /**
     * 校验验证码
     * */
    private void checkCode() {
        showProgress(this,false);
        UserApi.retrievePaayPwdCheckCode(phone, code, this, new requestCallback() {
            @Override
            public void onSuccess(int errCode, Object object) {
                dismissProgress();
                if (errCode == Constants.SUCCESS_CODE){
                    SettingPayPasswordActivity.start(RetrievePayPwdActivity.this,"retrieve",code);
                    finish();
                }else {
                    ToastUtil.showToast(RetrievePayPwdActivity.this, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(RetrievePayPwdActivity.this,errMessage);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
    }

    private void getUserInfo() {
        showProgress(this,false);
        UserApi.getUserInfo(NimUIKit.getAccount(), this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    UserInfoBean userInfoBean = (UserInfoBean) object;
                    if (StringUtil.isNotEmpty(userInfoBean.mobile)){
                        phone = userInfoBean.mobile;
                        tvPhone.setText(StringUtil.getPwdPhone(phone));
                    }
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                toast(errMessage);
                dismissProgress();
            }
        });
    }

    /**
     * 获取验证码
     * */
    private void getVerCode() {
        showProgress(this,false);
        UserApi.retrievePaayPwdSendCode(phone, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    initData();
                }else {
                    ToastUtil.showToast(RetrievePayPwdActivity.this, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(RetrievePayPwdActivity.this,errMessage);
            }
        });
    }

    private void initData() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mSeconds--;
                if (mSeconds <= 0) {
                    tvSendCode.setText("重新获取验证码");
                    mRunnable = null;
                    mSeconds = 60;
                } else {
                    tvSendCode.setText(mSeconds + "s后重新获取");
                    mHandler.postDelayed(mRunnable, 1000);
                }
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mHandler){
            mHandler.removeCallbacks(mRunnable);
        }
    }
}

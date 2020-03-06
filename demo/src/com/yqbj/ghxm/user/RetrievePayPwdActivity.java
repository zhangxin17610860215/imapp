package com.yqbj.ghxm.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.utils.NoDoubleClickUtils;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.UserInfoBean;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.redpacket.wallet.SettingPayPasswordActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.StringUtil;

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
                        ToastHelper.showToast(this,"请输入验证码");
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

                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
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
                    ToastHelper.showToast(RetrievePayPwdActivity.this, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastHelper.showToast(RetrievePayPwdActivity.this,errMessage);
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
                    ToastHelper.showToast(RetrievePayPwdActivity.this, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastHelper.showToast(RetrievePayPwdActivity.this,errMessage);
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

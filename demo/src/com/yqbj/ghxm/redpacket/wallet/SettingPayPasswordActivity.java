package com.yqbj.ghxm.redpacket.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.view.PayPsdInputView;
import com.netease.yqbj.uikit.common.activity.UI;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;

import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WALLET_EXIST;

/**
 * 设置支付密码
 * */
public class SettingPayPasswordActivity extends UI implements View.OnClickListener {

    private static final String TAG = SettingPayPasswordActivity.class.getSimpleName();

    private ImageView imgBack;
    private PayPsdInputView payEtOne;
    private PayPsdInputView payEtTwo;
    private TextView tvDetermine;
    private String paymentPwd;
    private static String mType = "";           //mType = check更改支付密码    mType = retrieve忘记密码重新设置支付密码
    private static String mValue = "";           //mType=check时mValue=旧密码，  mType=retrieve时mValue=手机验证码

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingPayPasswordActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, String type, String value) {
        Intent intent = new Intent(context, SettingPayPasswordActivity.class);
        mType = type;
        mValue = value;
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingpaypsd_activity);

        initView();
    }

    private void initView() {
        imgBack = findView(R.id.img_settingpaypas_back);
        payEtOne = findView(R.id.et_settingpaypas_one);
        payEtTwo = findView(R.id.et_settingpaypas_two);
        tvDetermine = findView(R.id.tv_settingpaypas_Determine);
        imgBack.setOnClickListener(this);
        tvDetermine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_settingpaypas_back:
                finish();
                break;
            case R.id.tv_settingpaypas_Determine:
                //确定
                paymentPwd = payEtOne.getPasswordString();
                if (StringUtil.isEmpty(paymentPwd) || paymentPwd.length() < 6){
                    ToastUtil.showToast(this,"请输入支付密码");
                    return;
                }
                paymentPwd = payEtTwo.getPasswordString();
                if (StringUtil.isEmpty(paymentPwd) || paymentPwd.length() < 6){
                    ToastUtil.showToast(this,"请再次输入支付密码");
                    return;
                }
                if (!payEtOne.getPasswordString().equals(payEtTwo.getPasswordString())){
                    ToastUtil.showToast(this,"两次输入的密码不相同");
                    return;
                }
                if (StringUtil.isNotEmpty(mType)){
                    if (mType.equals("check")){
                        //修改支付密码
                        modifyPwd();
                    }else {
                        //忘记密码重新设置密码
                        resettingPayPwd();
                    }
                }else {
                    //设置支付密码
                    settingPayPas();
                }
                break;
        }
    }

    private void resettingPayPwd() {
        showProgress(this,false);
        UserApi.resettingPayPwd(mValue, paymentPwd, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    ToastUtil.showToast(SettingPayPasswordActivity.this,"密码设置成功");
                    finish();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(SettingPayPasswordActivity.this,errMessage);
            }
        });
    }

    private void modifyPwd() {
        showProgress(this,false);
        UserApi.modifyPwd(mValue, paymentPwd, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    ToastUtil.showToast(SettingPayPasswordActivity.this,"密码修改成功");
                    finish();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(SettingPayPasswordActivity.this,errMessage);
            }
        });
    }

    private void settingPayPas() {

        showProgress(this,false);
        UserApi.settingPayPas(paymentPwd, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    ToastUtil.showToast(SettingPayPasswordActivity.this,"密码设置成功");
                    SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                    instance.put(WALLET_EXIST,true);
                    finish();
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(SettingPayPasswordActivity.this,errMessage);
            }
        });
    }
}

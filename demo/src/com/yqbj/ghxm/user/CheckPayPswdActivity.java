package com.yqbj.ghxm.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.utils.NoDoubleClickUtils;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.redpacket.wallet.SettingPayPasswordActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.view.PayPsdInputView;

/**
 * 验证支付密码
 * */
public class CheckPayPswdActivity extends BaseAct implements View.OnClickListener {

    private static final String TAG = CheckPayPswdActivity.class.getSimpleName();

    private PayPsdInputView payEt;
    private TextView tvDetermine;
    private String payPwd;

    public static void start(Context context) {
        Intent intent = new Intent(context, CheckPayPswdActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpaypswd_activity);
        initView();
    }

    private void initView() {
        setToolbar("验证支付密码");
        payEt = findView(R.id.et_checkpaypswd);
        tvDetermine = findView(R.id.tv_checkpaypswd_Determine);
        tvDetermine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()){
                case R.id.tv_checkpaypswd_Determine:
                    //确定更改支付密码
                    payPwd = payEt.getPasswordString();
                    if (StringUtil.isEmpty(payPwd)){
                        ToastHelper.showToast(this,"请输入原零钱支付密码");
                        return;
                    }
                    if (payPwd.length() < 6){
                        ToastHelper.showToast(this,"支付密码必须是六位数字");
                        return;
                    }
                    checkPwd();
                    break;
            }
        }
    }

    private void checkPwd() {
        showProgress(this,false);
        UserApi.checkPwd(payPwd, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    //验证成功跳转到设置支付密码页面
                    SettingPayPasswordActivity.start(CheckPayPswdActivity.this,"check",payPwd);
                    finish();
                }else {
                    ToastHelper.showToast(CheckPayPswdActivity.this, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastHelper.showToast(CheckPayPswdActivity.this, errMessage);
            }
        });
    }
}

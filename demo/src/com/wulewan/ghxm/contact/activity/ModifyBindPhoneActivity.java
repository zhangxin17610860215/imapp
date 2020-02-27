package com.wulewan.ghxm.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;

public class ModifyBindPhoneActivity extends BaseAct implements View.OnClickListener {

    private Context context;
    private String mobile = "";

    private TextView tvGetCode;
    private TextView tvModify;
    private EditText etOldModify;
    private EditText etNewModify;
    private EditText etCode;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private int mSeconds = 30;

    public static void start(Context context, String mobile) {
        Intent intent = new Intent();
        intent.setClass(context, ModifyBindPhoneActivity.class);
        intent.putExtra("mobile", mobile);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modifybindpaone_layout);
        context = this;

        mobile = getIntent().getStringExtra("mobile");
        initView();

        setToolbar(R.drawable.jrmf_b_top_back,"更换手机号");
    }

    private void initView() {
        tvGetCode = findView(R.id.tv_bind_verCode);
        tvModify = findView(R.id.tvModify);
        etOldModify = findView(R.id.et_oldModify);
        etNewModify = findView(R.id.et_newModify);
        etCode = findView(R.id.et_bind_verCode);
        tvGetCode.setOnClickListener(this);
        tvModify.setOnClickListener(this);
    }

    private void initData() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mSeconds--;
                if (mSeconds <= 0) {
                    tvGetCode.setText("重新获取验证码");
                    mRunnable = null;
                    mSeconds = 30;
                } else {
                    tvGetCode.setText(mSeconds + "s后重新获取");

                    mHandler.postDelayed(mRunnable, 1000);
                }
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_bind_verCode:
                if (StringUtil.isEmpty(etOldModify.getText().toString())){
                    toast("请输入原手机号");
                    return;
                }
                if (StringUtil.isEmpty(etNewModify.getText().toString())){
                    toast("请输入新手机号");
                    return;
                }
//                if (!mobile.equals(etOldModify.getText().toString())){
//                    toast("原手机号输入不正确");
//                    return;
//                }
                if (null == mRunnable){
                    initData();
                    getCode();
                }
                break;
            case R.id.tvModify:
                if (StringUtil.isEmpty(etOldModify.getText().toString())){
                    toast("请输入原手机号");
                    return;
                }
                if (StringUtil.isEmpty(etNewModify.getText().toString())){
                    toast("请输入新手机号");
                    return;
                }
                if (StringUtil.isEmpty(etCode.getText().toString())){
                    toast("请输入验证码");
                    return;
                }
//                if (!mobile.equals(etOldModify.getText().toString())){
//                    toast("原手机号输入不正确");
//                    return;
//                }
                modifyPhone();
                break;
        }
    }

    private void modifyPhone() {
        showProgress(context,false);
        UserApi.modifyBindPhoneCode(etOldModify.getText().toString(), etNewModify.getText().toString(), etCode.getText().toString(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("更换绑定手机号成功");
                    finish();
                }else {
                    toast((String) object);
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
        showProgress(context,false);
        UserApi.getModifyBindPhoneCode(etNewModify.getText().toString(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("验证码已发送至新手机号");
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mHandler) {
            mHandler.removeCallbacks(mRunnable);
        }
    }
}

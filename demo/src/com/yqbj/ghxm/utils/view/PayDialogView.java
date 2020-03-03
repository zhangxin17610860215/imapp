package com.yqbj.ghxm.utils.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.lxj.xpopup.core.CenterPopupView;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;

public class PayDialogView extends CenterPopupView{

    private Context context;
    private TextView tvBack;
    private PayPsdInputView etCheckpaypswd;
    private TextView tvForgetPwd;
    private TextView tvComplete;

    public PayDialogView(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_pay_layout;
    }

    @Override
    protected void initPopupContent() {
        super.initPopupContent();
        // 实现一些UI的初始和逻辑处理
        initView();
    }

    private void initView() {
        tvBack = findViewById(R.id.tv_dialogPay_back);
        etCheckpaypswd = findViewById(R.id.et_dialogPay_pswd);

        tvForgetPwd = findViewById(R.id.tv_dialogPay_forgetPwd);
        tvComplete = findViewById(R.id.tv_dialogPay_complete);
        etCheckpaypswd.setFocusable(true);
        etCheckpaypswd.setFocusableInTouchMode(true);
        etCheckpaypswd.requestFocus();
        etCheckpaypswd.findFocus();

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etCheckpaypswd, InputMethodManager.SHOW_FORCED);// 显示输入法
        tvBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backListener != null){
                    backListener.onClick(v);
                }
            }
        });

        etCheckpaypswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(etCheckpaypswd.getText().length()==6){
                    if(rigntListener!=null){
                        UserApi.checkPwd(etCheckpaypswd.getText().toString(), context, new requestCallback() {
                            @Override
                            public void onSuccess(int code, Object object) {
                                if (code == Constants.SUCCESS_CODE){
                                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(etCheckpaypswd.getWindowToken(), 0);
                                    rigntListener.onClick(tvComplete);
                                }else {
                                    ToastUtil.showToast(context, (String) object);
                                }
                            }

                            @Override
                            public void onFailed(String errMessage) {
                                ToastUtil.showToast(context, errMessage);
                            }
                        });
                    }
                }

            }
        });
        tvForgetPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forgetPwdListener != null) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etCheckpaypswd.getWindowToken(), 0);
                    forgetPwdListener.onClick(v);
                }
            }
        });
        tvComplete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (StringUtil.isEmpty(getPayeePwd())){
                    ToastUtil.showToast(context,"请输入支付密码");
                    return;
                }
                if (getPayeePwd().length()<6){
                    ToastUtil.showToast(context,"请输入正确的支付密码");
                    return;
                }
                if (rigntListener != null) {
                    UserApi.checkPwd(etCheckpaypswd.getText().toString(), context, new requestCallback() {
                        @Override
                        public void onSuccess(int code, Object object) {
                            if (code == Constants.SUCCESS_CODE){
                                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(etCheckpaypswd.getWindowToken(), 0);
                                rigntListener.onClick(v);
                            }else {
                                ToastUtil.showToast(context, (String) object);
                            }
                        }

                        @Override
                        public void onFailed(String errMessage) {
                            ToastUtil.showToast(context, errMessage);
                        }
                    });
                }
            }
        });
    }

    public String getPayeePwd(){
        return etCheckpaypswd.getPasswordString();
    }

    private View.OnClickListener rigntListener,forgetPwdListener,backListener;
    public void setOnClickListenerOnBack(View.OnClickListener backListener) {
        this.backListener = backListener;
    }public void setOnClickListenerOnRight(View.OnClickListener rightListener) {
        this.rigntListener = rightListener;
    }
    public void setOnClickListenerOnForgetPwd(View.OnClickListener forgetPwdListener) {
        this.forgetPwdListener = forgetPwdListener;
    }

    // 设置最大宽度，看需要而定
    @Override
    protected int getMaxWidth() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }
}

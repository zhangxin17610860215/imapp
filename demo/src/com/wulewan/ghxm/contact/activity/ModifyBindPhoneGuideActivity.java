package com.wulewan.ghxm.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.utils.StringUtil;

public class ModifyBindPhoneGuideActivity extends BaseAct {

    private Context context;
    private String mobile = "";

    private TextView tvPhone;

    public static void start(Context context, String mobile) {
        Intent intent = new Intent();
        intent.setClass(context, ModifyBindPhoneGuideActivity.class);
        intent.putExtra("mobile", mobile);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modifybindpaoneguide_layout);
        context = this;

        mobile = getIntent().getStringExtra("mobile");
        initView();

        setToolbar(R.drawable.jrmf_b_top_back,"更换手机号");
    }

    private void initView() {
        tvPhone = findView(R.id.tv_phone);
        tvPhone.setText("你的手机号: +86 " + StringUtil.getPwdPhone(mobile));
        findView(R.id.tv_modify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyBindPhoneActivity.start(context,mobile);
                finish();
            }
        });
    }

}

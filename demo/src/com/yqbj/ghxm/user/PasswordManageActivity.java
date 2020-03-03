package com.yqbj.ghxm.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.yqbj.ghxm.R;
import com.netease.yqbj.uikit.utils.NoDoubleClickUtils;
import com.yqbj.ghxm.common.ui.BaseAct;

/**
 * 密码管理
 * */
public class PasswordManageActivity extends BaseAct implements View.OnClickListener {

    private static final String TAG = PasswordManageActivity.class.getSimpleName();

    private RelativeLayout rlChange;
    private RelativeLayout rlRetrieve;

    public static void start(Context context) {
        Intent intent = new Intent(context, PasswordManageActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passwordmanage_activity);
        initView();
    }

    private void initView() {
        setToolbar("密码管理");
        rlChange = findView(R.id.rl_passwordmanage_change);
        rlRetrieve = findView(R.id.rl_passwordmanage_Retrieve);
        rlChange.setOnClickListener(this);
        rlRetrieve.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()){
                case R.id.rl_passwordmanage_change:
                    //更改支付密码
                    CheckPayPswdActivity.start(this);
                    break;
                case R.id.rl_passwordmanage_Retrieve:
                    //找回支付密码
                    RetrievePayPwdActivity.start(this,"1");
                    break;
            }
        }
    }
}

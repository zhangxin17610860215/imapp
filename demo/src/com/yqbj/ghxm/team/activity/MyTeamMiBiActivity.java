package com.yqbj.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.netease.yqbj.uikit.api.NimUIKit;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.MyTeamWalletBean;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.redpacket.wallet.DetailsChangeActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;

/**
 * 我的群蜜币
 * */
public class MyTeamMiBiActivity extends BaseAct {

    private Activity mActivity;
    private String teamId = "";

    private TextView tvDetailed;
    private TextView tvMiBiNum;

    public static void start(Context context, String teamId) {
        Intent intent = new Intent();
        intent.setClass(context, MyTeamMiBiActivity.class);
        intent.putExtra("teamId", teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myteammibi_activity_layout);
        mActivity = this;
        teamId = getIntent().getStringExtra("teamId");

        initView();
        initData();
    }

    private void initView() {
        setToolbar(R.drawable.jrmf_b_top_back,"我的群蜜币");
        tvMiBiNum = findView(R.id.tv_mibiNum);
        tvDetailed = findView(R.id.tv_Detailed);
        tvDetailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailsChangeActivity.start(mActivity,teamId);
            }
        });
    }

    private void initData() {
        showProgress(mActivity,false);
        UserApi.getTeamWalletInfo(teamId, NimUIKit.getAccount(), mActivity, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    MyTeamWalletBean walletBean = (MyTeamWalletBean) object;
                    tvMiBiNum.setText(walletBean.getScore()+"");
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

}

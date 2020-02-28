package com.wulewan.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.netease.wulewan.uikit.api.NimUIKit;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.MyTeamWalletBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.redpacket.wallet.DetailsChangeActivity;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;

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

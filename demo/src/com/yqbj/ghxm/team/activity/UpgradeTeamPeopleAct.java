package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.TeamAllocationPriceBean;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

public class UpgradeTeamPeopleAct extends BaseAct {

    private Context context;
    private TextView tvTeamMemberLimit;
    private RecyclerView recyclerView;
    private EasyRVAdapter mAdapter;
    private String tid = "";
    private String teamMemberLimit = "";
    private TeamAllocationPriceBean priceBean;

    public static void start(Context context, String tid, String teamMemberLimit, TeamAllocationPriceBean priceBean) {
        Intent intent = new Intent(context, UpgradeTeamPeopleAct.class);
        intent.putExtra("tid",tid);
        intent.putExtra("teamMemberLimit",teamMemberLimit);
        intent.putExtra("priceBean",priceBean);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgradeteampeople_activity_layout);
        context = this;
        initView();
        initDate();
    }

    private void initView() {
        setToolbar(R.drawable.jrmf_b_top_back,"群升级");
        tvTeamMemberLimit = (TextView) findViewById(R.id.tv_teamMemberLimit);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    private void initDate() {
        tid = getIntent().getStringExtra("tid");
        teamMemberLimit = getIntent().getStringExtra("teamMemberLimit");
        tvTeamMemberLimit.setText("此群是" + teamMemberLimit + "人群");
        priceBean = (TeamAllocationPriceBean) getIntent().getSerializableExtra("priceBean");
        if (null == priceBean){
            toast("数据获取失败");
            return;
        }
        mAdapter = new EasyRVAdapter(context,priceBean.getCfgs(),R.layout.upgradeteampeople_item_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                TextView tvLimit = viewHolder.getView(R.id.tv_teamMemberLimit);
                TextView tvPrive = viewHolder.getView(R.id.tv_prive);
                TextView tvUpgrade = viewHolder.getView(R.id.tv_Upgrade);
                final TeamAllocationPriceBean.CfgsBean cfgsBean = priceBean.getCfgs().get(position);
                tvLimit.setText(cfgsBean.getMaxLimit() + "");
                tvPrive.setText(cfgsBean.getPrice() + "元/年可升级");
                final int type;
                if (teamMemberLimit.equals(cfgsBean.getMaxLimit() + "")){
                    type = 1;
                    tvUpgrade.setText("续费");
                }else {
                    type = 2;
                    tvUpgrade.setText("升级");
                }
                tvUpgrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(type,cfgsBean.getIdentity(),cfgsBean.getPrice(),cfgsBean.getMaxLimit());
                    }
                });
            }
        };
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * 显示弹窗
     * */
    private void showDialog(final int type, final int identity, int price, int Limit){
        EasyAlertDialogHelper.showCommonDialog(context, "升级群", "升级将扣除您工会小蜜账户余额" + price +"元,确定要升级成" + Limit + "人群吗？", "升级", "再想想", true, new EasyAlertDialogHelper.OnDialogActionListener() {
            @Override
            public void doCancelAction() {

            }

            @Override
            public void doOkAction() {
                if (type == 1){
                    renew(identity);
                }else if (type == 2){
                    upgrade(identity);
                }
            }
        }).show();
    }

    /**
     * 升级
     * */
    private void upgrade(int identity) {
        showProgress(context,false);
        UserApi.setTeamUpgrade(tid, identity + "", context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("升级成功");
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

    /**
     * 续费
     * */
    private void renew(int identity) {
        showProgress(context,false);
        UserApi.setTeamRenew(tid, identity + "", context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("续费成功");
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

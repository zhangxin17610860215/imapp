package com.wulewan.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.AutomaticGetRedPackBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.TimeUtils;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 延时领取零钱红包
 * */
public class DelayedCollectionRPAct extends BaseAct {

    private Context context;
    private TextView tvSubstituteCollarNum;
    private TextView tvSubstituteCollarMoney;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView mRecyclerView;
    private TextView tvNodata;
    private LinearLayout llNodata;
    private LinearLayout llTotal;
    private TabLayout tabLayout;

    private String teamId;
    private int page = 1;
    private int rows = 20;
    private int count;
    private List<AutomaticGetRedPackBean.ResultsBean> list = new ArrayList<>();
    private List<AutomaticGetRedPackBean.ResultsBean> data = new ArrayList<>();
    private EasyRVAdapter mAdapter;
    private String selectData = "";

    public static void start(Context context, String teamId) {
        Intent intent = new Intent();
        intent.setClass(context, DelayedCollectionRPAct.class);
        intent.putExtra("teamId", teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_delayedcollectionrp_layout);
        context = this;
        setToolbar(R.drawable.jrmf_b_top_back,"延时红包领取记录");
        teamId = getIntent().getStringExtra("teamId");
        initView();
        selectData(1);
    }

    private void initView() {
        tvSubstituteCollarNum = (TextView) findViewById(R.id.tv_SubstituteCollarNum);
        tvSubstituteCollarMoney = (TextView) findViewById(R.id.tv_SubstituteCollarMoney);
        llTotal = (LinearLayout) findViewById(R.id.ll_Total);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        refreshLayout = findView(R.id.refresh_layout);
        mRecyclerView = findView(R.id.mRecyclerView);
        tvNodata = findView(R.id.tv_noData_content);
        llNodata = findView(R.id.ll_nodata);
        tvNodata.setText("暂无收到的延时红包");

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getText().toString()){
                    case "今天":
                        selectData(1);
                        break;
                    case "昨天":
                        selectData(2);
                        break;
                    case "前天":
                        selectData(3);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                //下拉刷新
                page = 1;
                initData();
            }
        });

        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                //上拉加载更多
                if (count / page > rows && count / page > 0){
                    page++;
                    initData();
                }else {
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }
                }
            }
        });
    }

    private void initData() {
        showProgress(context,false);
        UserApi.automaticGetRedPack(page, rows, teamId, selectData, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }
                    AutomaticGetRedPackBean bean = (AutomaticGetRedPackBean) object;
                    tvSubstituteCollarNum.setText("代领总个数：" + bean.getCount() + "个");
                    tvSubstituteCollarMoney.setText("代领总金额：" + bean.getTotalMoney() + "元");
                    count = bean.getCount();
                    data.clear();
                    data.addAll(bean.getResults());
                    loadData();
                }else {
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
                toast(errMessage);
            }
        });

    }

    /**
     * 选择需要显示哪一天的数据
     * */
    private void selectData(int tag) {
        long currentTime = System.currentTimeMillis();
        switch (tag){
            case 1:
                selectData = TimeUtils.getDateToString(currentTime,TimeUtils.TIME_TYPE_01);
                break;
            case 2:
                long yesterday = currentTime - 60 * 60 * 24 * 1000;
                selectData = TimeUtils.getDateToString(yesterday,TimeUtils.TIME_TYPE_01);
                break;
            case 3:
                long dayBeforeYesterday = (currentTime - 60 * 60 * 24 * 1000) - 60 * 60 * 24 * 1000;
                selectData = TimeUtils.getDateToString(dayBeforeYesterday,TimeUtils.TIME_TYPE_01);
                break;
        }
        if (StringUtil.isNotEmpty(selectData)){
            page = 1;
            initData();
        }
    }

    private void loadData() {
        if (page == 1){
            list.clear();
            list.addAll(data);
            if (list.size() <= 0){
                llNodata.setVisibility(View.VISIBLE);
                llTotal.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
            }else {
                llNodata.setVisibility(View.GONE);
                llTotal.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            initAdapter();
        } else {
            for (AutomaticGetRedPackBean.ResultsBean bean : data) {
                list.add(bean);
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    private void initAdapter(){
        mAdapter = new EasyRVAdapter(this, list, R.layout.item_detailschange_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                if (null == list || list.size() == 0) {
                    return;
                }
                AutomaticGetRedPackBean.ResultsBean bean = list.get(position);
                TextView tvTitle = viewHolder.getView(R.id.tv_item_detailschange_title);
                TextView tvTime = viewHolder.getView(R.id.tv_item_detailschange_time);
                TextView tvAmount = viewHolder.getView(R.id.tv_item_detailschange_amount);
                TextView tvGone = viewHolder.getView(R.id.tv_item_detailschange_isGone);

                tvTitle.setText(bean.getPayerName());
                tvTime.setText(bean.getCreateDate());
                tvAmount.setText("+" + bean.getAmount() + "元");
                if (bean.getTargetType() == 1){
                    tvGone.setText("零钱红包");
                }else if (bean.getTargetType() == 2){
                    tvGone.setText("普通红包");
                }else{
                    tvGone.setText("随机红包");
                }
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }
}

package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.util.GlideUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.AutomaticGetRedPackBean;
import com.yqbj.ghxm.bean.SettlementBean;
import com.yqbj.ghxm.bean.TeamRobotNotifyBean;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.TimeUtils;
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
    private RecyclerView item_RecyclerView;
    private TextView tvNodata;
    private LinearLayout llNodata;
    private LinearLayout llTotal;
    private TabLayout tabLayout;

    private String teamId;
    private int page = 1;
    private int rows = 20;
    private boolean noData = false;
    private List<AutomaticGetRedPackBean.ResultsBean> list = new ArrayList<>();
    private List<AutomaticGetRedPackBean.ResultsBean> data = new ArrayList<>();
    private List<SettlementBean.TeamRobotNotifyBean> settlementList = new ArrayList<>();
    private List<SettlementBean.TeamRobotNotifyBean> settlemenData = new ArrayList<>();
    private EasyRVAdapter mAdapter;
    private EasyRVAdapter mItemAdapter;
    private String selectData = "";
    private String type;  //1:延时红包领取记录 2:战绩未结算记录

    public static void start(Context context, String teamId, String type) {
        Intent intent = new Intent();
        intent.setClass(context, DelayedCollectionRPAct.class);
        intent.putExtra("teamId", teamId);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_delayedcollectionrp_layout);
        context = this;
        teamId = getIntent().getStringExtra("teamId");
        type = getIntent().getStringExtra("type");
        rows = type.equals("1") ? 20 : 10;
        setToolbar(R.drawable.jrmf_b_top_back,type.equals("1")?"延时红包领取记录":"战绩未结算记录");
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
        tvNodata.setText(type.equals("1")?"暂无收到的延时红包":"暂无未结算记录");

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
                if (noData){
                    refreshLayout.finishLoadMoreWithNoMoreData();
                }else {
                    page++;
                    initData();
                }
            }
        });
    }

    private void initData() {
        showProgress(context,false);
        if (type.equals("1")){
            automaticGetRedPack();
        }else {
            settlementFailedList();
        }
    }

    private void settlementFailedList() {
        UserApi.settlementFailedList(page, rows, teamId, selectData, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
                if (code == Constants.SUCCESS_CODE){
                    llTotal.setVisibility(View.GONE);
                    SettlementBean settlementBean = (SettlementBean) object;
                    settlemenData.clear();
                    settlemenData.addAll(settlementBean.getDateBeans());
                    noData = settlemenData.size() < rows;
                    loadData();
                }else {
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

    private void automaticGetRedPack() {
        UserApi.automaticGetRedPack(page, rows, teamId, selectData, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
                if (code == Constants.SUCCESS_CODE){
                    llTotal.setVisibility(View.VISIBLE);
                    AutomaticGetRedPackBean bean = (AutomaticGetRedPackBean) object;
                    tvSubstituteCollarNum.setText("代领总个数：" + bean.getCount() + "个");
                    tvSubstituteCollarMoney.setText("代领总金额：" + bean.getTotalMoney() + "元");
                    data.clear();
                    data.addAll(bean.getResults());
                    noData = data.size() < rows;
                    loadData();
                }else {
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
            if (type.equals("1")){
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
            }else {
                settlementList.clear();
                settlementList.addAll(settlemenData);
                if (settlementList.size() <= 0){
                    llNodata.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }else {
                    llNodata.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
            initAdapter();
        } else {
            if (type.equals("1")){
                for (AutomaticGetRedPackBean.ResultsBean bean : data) {
                    list.add(bean);
                }
            }else {
                for (SettlementBean.TeamRobotNotifyBean bean : settlemenData) {
                    settlementList.add(bean);
                }
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    private void initAdapter(){
        if (type.equals("1")){
            initRpAdapter();
        }else {
            initSettlementAdapter();
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initSettlementAdapter() {
        mAdapter = new EasyRVAdapter(context,settlementList,R.layout.settlement_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, final int position, Object item) {
                final SettlementBean.TeamRobotNotifyBean robotNotifyBean = settlementList.get(position);
                item_RecyclerView = viewHolder.getView(R.id.recyclerView);
                item_RecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

                TextView tv_title = viewHolder.getView(R.id.tv_title);
                TextView tv_time = viewHolder.getView(R.id.tv_time);
                TextView tv_ignore = viewHolder.getView(R.id.tv_ignore);

                TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
                if (teamMember.getType() == TeamMemberType.Owner ||
                        teamMember.getType() == TeamMemberType.Manager){
                    tv_ignore.setVisibility(View.VISIBLE);
                }else {
                    tv_ignore.setVisibility(View.GONE);
                }

                if (StringUtil.isNotEmpty(robotNotifyBean.getTitle())){
                    String[] split = robotNotifyBean.getTitle().split("\n");
                    if (null != split && split.length >= 2){
                        tv_time.setVisibility(View.VISIBLE);
                        tv_title.setText(split[0]);
                        tv_time.setText(split[1]);
                    }else {
                        tv_title.setText(robotNotifyBean.getTitle());
                        tv_time.setVisibility(View.GONE);
                    }
                }

                tv_ignore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
                        if (teamMember.getType() == TeamMemberType.Owner ||
                                teamMember.getType() == TeamMemberType.Manager){
                            ignoreSettlement(robotNotifyBean,position);
                        }else {
                            toast("只有群主和管理员有忽略权");
                        }
                    }
                });

                final List<SettlementBean.TeamRobotNotifyBean.ContentBean> beanList = robotNotifyBean.getContent();
                mItemAdapter = new EasyRVAdapter(context,beanList,R.layout.team_robotnotify_item_layout) {
                    @Override
                    protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                        SettlementBean.TeamRobotNotifyBean.ContentBean bean = beanList.get(position);
                        HeadImageView imgHead = viewHolder.getView(R.id.img_head);
                        TextView tvLind1 = viewHolder.getView(R.id.tv_lind1);
                        TextView tvLind2 = viewHolder.getView(R.id.tv_lind2);
                        TextView tvLind3 = viewHolder.getView(R.id.tv_lind3);
                        View isLast = viewHolder.getView(R.id.isLast);
                        imgHead.setIsRect(true);
                        GlideUtil.loadHavePlaceholderImageView(context,bean.getImageUrl(),R.mipmap.robot_bgicon,imgHead);

                        tvLind1.setText(bean.getLine1());
                        tvLind2.setText(Html.fromHtml(bean.getLine2()));
                        tvLind3.setText(Html.fromHtml(bean.getLine3()));

                        if (position == list.size() -1){
                            isLast.setVisibility(View.GONE);
                        }else {
                            isLast.setVisibility(View.VISIBLE);
                        }
                    }
                };
                item_RecyclerView.setAdapter(mItemAdapter);
            }
        };
    }

    private void ignoreSettlement(SettlementBean.TeamRobotNotifyBean robotNotifyBean, final int position) {
        showProgress(context,false);
        UserApi.ignoreSettlement(robotNotifyBean.getAchievementid(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("忽略成功");
                    if(settlementList.size()>position){
                        settlementList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        mAdapter.notifyDataSetChanged();
                        if (settlementList.size() <= 0){
                            page = 1;
                            initData();
                        }else {
                            llNodata.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
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

    private void initRpAdapter() {
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
                    tvGone.setText("蜜币红包");
                }else if (bean.getTargetType() == 2){
                    tvGone.setText("普通红包");
                }else{
                    tvGone.setText("随机红包");
                }
            }
        };
    }
}

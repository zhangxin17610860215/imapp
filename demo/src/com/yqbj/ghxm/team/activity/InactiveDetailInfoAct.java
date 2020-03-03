package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.TimeUtils;
import com.yqbj.ghxm.bean.TeamInactiveBean;
import com.yqbj.ghxm.bean.TeamLeaveBean;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;

public class InactiveDetailInfoAct extends BaseAct {

    private static final String EXTRA_ID = "EXTRA_ID";
    private String teamId;
    private int timeQuantum;


    private SmartRefreshLayout refreshLayout;
    private RecyclerView mRecyclerView;
    private EasyRVAdapter mAssetsAdapter;
    private View noDataView;

    private int count;              //总数量
    private int page = 1;           //页码
    private int rows = 20;          //每页需要展示的数量

    private TeamLeaveBean.ResultsEntity resultsBean;
    private List<TeamInactiveBean.ResultsEntity> results;
    private List<TeamInactiveBean.ResultsEntity> list = new ArrayList<>();

    public static void start(Context context, String tid,int timeQuantum) {
        Log.e("tid",tid);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.putExtra("timeQuantum",timeQuantum);
        intent.setClass(context, InactiveDetailInfoAct.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inactive_detail_list_act);
        setToolbar(R.drawable.jrmf_b_top_back,"群成员活跃度");
        parseIntentData();
        initView();
        initAdapter();
        initData();
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
        timeQuantum = getIntent().getIntExtra("timeQuantum",1);

    }


    private void initAdapter(){
        mAssetsAdapter = new EasyRVAdapter(this, list, R.layout.list_item_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                if (null == list || list.size() == 0) {
                    return;
                }
                TeamInactiveBean.ResultsEntity resultsBean = list.get(position);
                if (null == resultsBean) {
                    return;
                }

                HeadImageView imageView = viewHolder.getView(R.id.item_img);
                imageView.setIsRect(true);
                imageView.loadBuddyAvatar(resultsBean.getUid());
                TextView item_text = viewHolder.getView(R.id.item_text);
                item_text.setText(resultsBean.getUserName() +"");
                TextView item_tip = viewHolder.getView(R.id.item_tips);
                item_tip.setText(TimeUtils.getDateToString(resultsBean.getLastLoginDate(),TimeUtils.TIME_TYPE_04));



            }
        };
        mRecyclerView.setAdapter(mAssetsAdapter);
        mAssetsAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                //Item点击事件
                AdvancedTeamMemberInfoAct.startActivityForResult(InactiveDetailInfoAct.this, list.get(position).getUid(), teamId);
            }
        });

    }

    public void initView() {
        mRecyclerView = findView(R.id.marginAssets_mRv);
        refreshLayout = findView(R.id.refresh_layout);

        noDataView = findViewById(R.id.ll_nodata);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false);

        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                //上拉加载更多
                if (count / page > rows || count / page > 0){
                    page++;
                    initData();
                }else {
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                //下拉刷新
                page = 1;
                initData();

            }
        });


    }




    private void initData() {
        showProgress(this,false);
        UserApi.teamInactiveQuery(page,rows, teamId, timeQuantum,this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
                TeamInactiveBean bean = (TeamInactiveBean) object;
                count = bean.getCount();
                results = bean.getResults();
                loadData();
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
            }
        });
    }


    private void loadData() {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
        }
        if (page == 1){
            list.clear();
            list.addAll(results);
            mAssetsAdapter.notifyDataSetChanged();

        } else {
            for (int i = 0; i < results.size(); i++) {
                list.add(results.get(i));
            }
            mAssetsAdapter.notifyDataSetChanged();
        }

        if(list==null || list.size()<1){
            noDataView.setVisibility(View.VISIBLE);
            TextView tv_noData = noDataView.findViewById(R.id.tv_noData_content);
            tv_noData.setText("暂无不活跃信息");
        }else{

            noDataView.setVisibility(View.GONE);
        }

    }


}

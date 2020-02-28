package com.wulewan.ghxm.redpacket.wallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.wulewan.uikit.api.NimUIKit;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.DetailsChangeQueryBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.TimeUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 零钱明细
 */
public class DetailsChangeActivity extends BaseAct {

    private static final String TAG = DetailsChangeActivity.class.getSimpleName();

    private Activity mActivity;
    private TextView tvNodata;
    private LinearLayout llNodata;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView mRecyclerView;
    private EasyRVAdapter mAssetsAdapter;
    private List<DetailsChangeQueryBean.ResultsBean> results;
    private List<DetailsChangeQueryBean.ResultsBean> list = new ArrayList<>();
    private int count;              //总数量
    private int page = 1;           //页码
    private int rows = 20;          //每页需要展示的数量
    private String startDate ="";   //开始时间
    private String endDate ="";     //结束时间
    private String teamId = "";

    public static void start(Context context, String teamId) {
        Intent intent = new Intent(context, DetailsChangeActivity.class);
        intent.putExtra("teamId",teamId);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailschange_activity);
        mActivity = this;
        teamId = getIntent().getStringExtra("teamId");
        initView();
        initData();
    }

    private void initData() {
        showProgress(this,false);
//        UserApi.detailsChangeQuery(page, rows, startDate, endDate, this, new requestCallback() {
//            @Override
//            public void onSuccess(int code, Object object) {
//                dismissProgress();
//
//            }
//
//            @Override
//            public void onFailed(String errMessage) {
//                dismissProgress();
//
//            }
//        });
        UserApi.getTeamOrderList(page, teamId, NimUIKit.getAccount(), mActivity, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
                if (code == Constants.SUCCESS_CODE){
                    DetailsChangeQueryBean bean = (DetailsChangeQueryBean) object;
                    count = bean.getCount();
                    results = bean.getResults();
                    loadData();
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
            }
        });
    }

    private void initView() {

        setToolbar("收支明细");
//        setRightImg(R.mipmap.query_icon, new onToolBarRightImgListener() {
//            @Override
//            public void onRight() {
//                startActivityForResult(new Intent(mActivity,DetailedQueryActivity.class),100);
//            }
//        });
        tvNodata = findView(R.id.tv_noData_content);
        llNodata = findView(R.id.ll_nodata);


        mRecyclerView = findView(R.id.mRecyclerView);
        refreshLayout = findView(R.id.refresh_layout);
        tvNodata.setText("暂无零钱明细");
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

    private void loadData() {
        if (page == 1){
            list = results;
            if (list.size() <= 0){
                llNodata.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }else {
                llNodata.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            mAssetsAdapter = new EasyRVAdapter(this, list, R.layout.item_detailschange_new_layout) {
                @Override
                protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                    if (null == list || list.size() == 0) {
                        return;
                    }
                    DetailsChangeQueryBean.ResultsBean resultsBean = list.get(position);
                    if (null == resultsBean) {
                        return;
                    }

                    TextView tvTitle = viewHolder.getView(R.id.tv_item_detailschange_title);
                    TextView tvTime = viewHolder.getView(R.id.tv_item_detailschange_time);
                    TextView tvAmount = viewHolder.getView(R.id.tv_item_detailschange_amount);
                    TextView tvRemainingAmount = viewHolder.getView(R.id.tv_item_detailschange_isGone);
                    tvRemainingAmount.setText("余额:" + resultsBean.getRemainingScore());
                    tvAmount.setText(resultsBean.getScore());
//                    tvTime.setText(TimeUtils.getDateToString(resultsBean.getCreateDate(),TimeUtils.TIME_TYPE_01));
                    tvTime.setText(resultsBean.getCreateDate());
                    tvTitle.setText(resultsBean.getTitle());
                }
            };
            mRecyclerView.setAdapter(mAssetsAdapter);
            mAssetsAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Object item) {
                    //Item点击事件
                }
            });
        } else {
            for (int i = 0; i < results.size(); i++) {
                list.add(results.get(i));
            }
            mAssetsAdapter.notifyDataSetChanged();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100){
            startDate = data.getStringExtra("startDateStr");
            endDate = data.getStringExtra("endDateStr");
            if (StringUtil.isNotEmpty(startDate) && StringUtil.isNotEmpty(endDate)){
                page = 1;
                initData();
            }
        }
    }
}

package com.yqbj.ghxm.redpacket.wallet;

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

import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.wrapper.NimUserInfoProvider;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.DetailsChangeQueryBean;
import com.yqbj.ghxm.bean.RedPackOtherDataBean;
import com.yqbj.ghxm.bean.RedPacketStateBean;
import com.yqbj.ghxm.bean.SenderUserInfoBean;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.redpacket.privateredpacket.RedPackDetailsActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.StringUtil;
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
    private boolean noData = false;
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
                    results = bean.getResults();
                    noData = results.size() < rows;
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
        tvNodata.setText("暂无收支明细");
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
                    tvRemainingAmount.setText("余额:" + resultsBean.getRemainingScore() + "个蜜币");
                    tvAmount.setText(resultsBean.getScore() + "个蜜币");
//                    tvTime.setText(TimeUtils.getDateToString(resultsBean.getCreateDate(),TimeUtils.TIME_TYPE_01));
                    tvTime.setText(resultsBean.getCreateDate());
                    switch (resultsBean.getType()){
                        case 1:
                            tvTitle.setText("你发了一个红包");
                            break;
                        case 2:
                            tvTitle.setText("你领了一个红包");
                            break;
                        case 5:
                            tvTitle.setText("你的红包超时已退还");
                            break;
                        case 6:
                            tvTitle.setText("你的红包已被群主代领");
                            break;
                        case 8:
                            if (StringUtil.isNotEmpty(resultsBean.getOperator())){
                                NimUserInfoProvider userInfoProvider = new NimUserInfoProvider(mActivity);
                                UserInfo userInfo = userInfoProvider.getUserInfo(resultsBean.getOperator());
                                if (resultsBean.getOperator().equals(resultsBean.getUid())){
                                    Team team = NimUIKit.getTeamProvider().getTeamById(teamId);
                                    if (null != team){
                                        if (team.getCreator().equals(NimUIKit.getAccount())){
                                            if (resultsBean.getScore().contains("-")){
                                                tvTitle.setText("你给群员扣除了蜜币");
                                            }else {
                                                tvTitle.setText("你给群员充值了蜜币");
                                            }
                                            return;
                                        }
                                    }
                                    if (resultsBean.getScore().contains("-")){
                                        tvTitle.setText("你给群员充值了蜜币");
                                    }else {
                                        tvTitle.setText("你给群员扣除了蜜币");
                                    }
                                }else {
                                    if (resultsBean.getScore().contains("-")){
                                        tvTitle.setText(userInfo.getName()+"给你扣除了蜜币");
                                    }else {
                                        tvTitle.setText(userInfo.getName()+"给你充值了蜜币");
                                    }
                                }
                            }else {
                                if (resultsBean.getScore().contains("-")){
                                    tvTitle.setText("群主给你扣除了蜜币");
                                }else {
                                    tvTitle.setText("群主给你充值了蜜币");
                                }
                            }
                            break;
                        default:
                            tvTitle.setText(resultsBean.getTitle());
                            break;
                    }

                }
            };
            mRecyclerView.setAdapter(mAssetsAdapter);
            mAssetsAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Object item) {
                    //Item点击事件
                    if (null == list || list.size() < 0){
                        return;
                    }
                    DetailsChangeQueryBean.ResultsBean resultsBean = list.get(position);
                    if (null == resultsBean){
                        return;
                    }

                    if (resultsBean.getType() != 1 && resultsBean.getType() != 2
                            && resultsBean.getType() != 5 && resultsBean.getType() != 6 ){
                        toast("非红包订单无法查看订单详情");
                        return;
                    }
                    showProgress(mActivity,false);
                    UserApi.getRedPackStatisticNew(resultsBean.getId(), mActivity, new requestCallback() {
                        @Override
                        public void onSuccess(int code, Object object) {
                            dismissProgress();
                            if (code == Constants.SUCCESS_CODE){
                                SenderUserInfoBean senderBean = new SenderUserInfoBean();
                                RedPackOtherDataBean bean = new RedPackOtherDataBean();
                                RedPacketStateBean stateBean = (RedPacketStateBean) object;
                                bean.setRedId(stateBean.getId());
                                NimUserInfoProvider userInfoProvider = new NimUserInfoProvider(mActivity);
                                UserInfo userInfo = userInfoProvider.getUserInfo(stateBean.getPayerId());
                                senderBean.setUserID(userInfo.getAccount());
                                senderBean.setAvatar(userInfo.getAvatar());
                                senderBean.setUserName(userInfo.getName());
                                RedPackDetailsActivity.start(mActivity,bean,senderBean);
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

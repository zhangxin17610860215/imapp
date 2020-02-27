package com.wulewan.ghxm.redpacket.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.DetailsRedPacketBean;
import com.wulewan.ghxm.bean.RedPackOtherDataBean;
import com.wulewan.ghxm.bean.SenderUserInfoBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.TimeUtils;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.api.wrapper.NimUserInfoProvider;
import com.netease.wulewan.uikit.business.uinfo.UserInfoHelper;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.umeng.analytics.MobclickAgent;
import com.wulewan.ghxm.redpacket.privateredpacket.RedPackDetailsActivity;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;

import static com.netease.wulewan.uikit.api.StatisticsConstants.COIN_RECEIVEDRP_CHANGEDETAILSCELL;
import static com.netease.wulewan.uikit.api.StatisticsConstants.COIN_SENDRP_CHANGEDETAILSCELL;

public class DetailsRedPacketActivity extends BaseAct {

    private static final String TAG = DetailsRedPacketActivity.class.getSimpleName();

    private TextView tvName;
    private TextView tvAmount;
    private TextView tvNumber;
    private TextView tvNumberText;

    private ImageView imgHead;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView mRecyclerView;
    private TextView tvNodata;
    private LinearLayout llNodata;

    private int receive = 1;  //Receive = 1收到的红包,Receive = 2发出的红包
    private int page = 1;
    private int rows = 20;
    private int count;

    private List<DetailsRedPacketBean.ResultsBean> results;
    private List<DetailsRedPacketBean.ResultsBean> list = new ArrayList<>();

    private EasyRVAdapter mAssetsAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, DetailsRedPacketActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailseredpacket_activity);
        setToolbar(R.drawable.jrmf_b_top_back, "收到的红包",R.color.redpacket_theme);
        setRightImg(R.mipmap.redpacket_switch_icon, new onToolBarRightImgListener() {
            @Override
            public void onRight() {
                switchReceive();
            }
        });

        initView();
        initData();

    }

    private void initAdapter(){

        mAssetsAdapter = new EasyRVAdapter(this, list, R.layout.item_detailschange_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                if (null == list || list.size() == 0) {
                    return;
                }
                DetailsRedPacketBean.ResultsBean resultsBean = list.get(position);
                if (null == resultsBean) {
                    return;
                }

                TextView tvTitle = viewHolder.getView(R.id.tv_item_detailschange_title);
                TextView tvTime = viewHolder.getView(R.id.tv_item_detailschange_time);
                TextView tvAmount = viewHolder.getView(R.id.tv_item_detailschange_amount);
                TextView tvGone = viewHolder.getView(R.id.tv_item_detailschange_isGone);
                if (receive == 1){
                    //收到的红包
                    String alias = NimUIKit.getContactProvider().getAlias(resultsBean.getPayerId());
                    String name = UserInfoHelper.getUserName(resultsBean.getPayerId());

                    if (StringUtil.isNotEmpty(alias)){
                        tvTitle.setText(alias);
                    }else {
                        if (StringUtil.isNotEmpty(name)){
                            tvTitle.setText(name);
                        }else {
                            tvTitle.setText(resultsBean.getPayerName() + "");
                        }
                    }

                    tvAmount.setText("+" + resultsBean.getAmount() + "元");

                    tvGone.setText("工会小蜜");
                }else {
                    //发出的红包
                    tvAmount.setText("-" + resultsBean.getAmount() + "元");
                    tvTitle.setText(resultsBean.getName() + "");
                    tvGone.setText("已领取" + resultsBean.getCount() + "/" + resultsBean.getNumber() + "个");
                    if (resultsBean.getStatus() == 4){
                        tvGone.setText("已领取" + resultsBean.getCount() + "/" + resultsBean.getNumber() + "个,其余已被群主代领");
                    }

                }
                tvTime.setText(TimeUtils.getDateToString(resultsBean.getCreateDate(),TimeUtils.TIME_TYPE_08));
            }
        };
        mRecyclerView.setAdapter(mAssetsAdapter);

        mAssetsAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                if (null == list || list.size() < 0){
                    return;
                }
                DetailsRedPacketBean.ResultsBean resultsBean = list.get(position);
                if (null == resultsBean){
                    return;
                }
                SenderUserInfoBean senderBean = new SenderUserInfoBean();
                RedPackOtherDataBean bean = new RedPackOtherDataBean();

                if (receive == 1) {
                    //收到的红包
                    bean.setRedId(resultsBean.getRid());
                    UserInfo senderUserInfo = NimUIKit.getUserInfoProvider().getUserInfo(resultsBean.getPayerId());
                    senderBean.setAvatar(senderUserInfo.getAvatar());
                    senderBean.setUserID(senderUserInfo.getAccount());
                    senderBean.setUserName(senderUserInfo.getName());
                }else {
                    bean.setRedId(resultsBean.getId());
                    UserInfo senderUserInfo = NimUIKit.getUserInfoProvider().getUserInfo(NimUIKit.getAccount());
                    senderBean.setAvatar(senderUserInfo.getAvatar());
                    senderBean.setUserID(senderUserInfo.getAccount());
                    senderBean.setUserName(senderUserInfo.getName());
                }
                RedPackDetailsActivity.start(DetailsRedPacketActivity.this,bean,senderBean);
            }
        });
    }

    private void initData() {
        showProgress(this,false);
        if (receive == 1){
            MobclickAgent.onEvent(this,COIN_RECEIVEDRP_CHANGEDETAILSCELL);
            setTitle("收到的红包");
            tvNodata.setText("暂无收到的红包数据");
            UserApi.getRedPage(page, rows, this, new requestCallback() {
                @Override
                public void onSuccess(int code, Object object) {
                    dismissProgress();
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }
                    if (code == Constants.SUCCESS_CODE){
                        DetailsRedPacketBean getBean = (DetailsRedPacketBean) object;
                        count = getBean.getCount();
                        tvAmount.setText(getBean.getTotalMoney());
                        tvNumberText.setText("共收到红包");
                        tvNumber.setText(" " + getBean.getCount());
                        results = getBean.getResults();
                        loadData();
                    }

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
        }else {
            MobclickAgent.onEvent(this,COIN_SENDRP_CHANGEDETAILSCELL);
            setTitle("发出的红包");
            tvNodata.setText("暂无发出的红包数据");
            UserApi.sendRedPage(page, rows, this, new requestCallback() {
                @Override
                public void onSuccess(int code, Object object) {
                    dismissProgress();
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }
                    if (code == Constants.SUCCESS_CODE){
                        DetailsRedPacketBean sendBean = (DetailsRedPacketBean) object;
                        count = sendBean.getCount();
                        results = sendBean.getResults();
                        tvAmount.setText(sendBean.getTotalMoney());
                        tvNumberText.setText("共发出红包");
                        tvNumber.setText(" " + sendBean.getCount());
                        loadData();
                    }

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
    }

    private void loadData() {
        if (page == 1){
            list.clear();
            list.addAll(results);
            if (list.size() <= 0){
                llNodata.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }else {
                llNodata.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            initAdapter();
        } else {
            for (int i = 0; i < results.size(); i++) {
                list.add(results.get(i));
            }
            mAssetsAdapter.notifyDataSetChanged();
        }

    }

    private void initView() {
        tvName = findView(R.id.tv_detailseredpacket_name);
        tvAmount = findView(R.id.tv_detailseredpacket_amount);
        tvNumber = findView(R.id.tv_detailseredpacket_number);
        tvNumberText = findView(R.id.tv_detailseredpacket_numberText);
        imgHead = findView(R.id.img_detailseredpacket_head);
        refreshLayout = findView(R.id.refresh_layout);
        mRecyclerView = findView(R.id.mRecyclerView);
        tvNodata = findView(R.id.tv_noData_content);
        llNodata = findView(R.id.ll_nodata);
        tvNodata.setText("暂无收到的红包数据");

        NimUserInfoProvider userInfoProvider = new NimUserInfoProvider(this);
        String uId = SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID);
        UserInfo userInfo = userInfoProvider.getUserInfo(uId);
        if (StringUtil.isNotEmpty(userInfo.getAvatar())){
            GlideUtil.loadCircular(this,userInfo.getAvatar(),imgHead);
        }
        if (StringUtil.isNotEmpty(userInfo.getName())){
            tvName.setText(userInfo.getName());
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false);


        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
//                上拉加载更多
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
                page = 1;
                initData();
            }
        });
    }



    private void switchReceive() {
        page = 1;
        if (receive == 1){
            receive ++;
        }else {
            receive = 1;
        }
        initData();
    }
}

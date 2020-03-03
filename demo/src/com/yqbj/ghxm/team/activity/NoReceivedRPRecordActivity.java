package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lxj.xpopup.XPopup;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.RedPackOtherDataBean;
import com.yqbj.ghxm.bean.RedPacketStateBean;
import com.yqbj.ghxm.bean.SenderUserInfoBean;
import com.yqbj.ghxm.bean.UnclaimedRPDetailsBean;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.wrapper.NimUserInfoProvider;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.redpacket.privateredpacket.AnalyticalRedPackData;
import com.yqbj.ghxm.redpacket.privateredpacket.RedPackDataCallBack;
import com.yqbj.ghxm.redpacket.privateredpacket.RedPackDetailsActivity;
import com.yqbj.ghxm.redpacket.wallet.SettingPayPasswordActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.TimeUtils;
import com.yqbj.ghxm.utils.view.RedPackDialog;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 未领取红包记录
 * */
public class NoReceivedRPRecordActivity extends BaseAct {

    private Context context;
    private TextView tvNodata;
    private LinearLayout llNodata;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView mRecyclerView;
    private EasyRVAdapter mAdapter;
    private List<UnclaimedRPDetailsBean.ResultsBean> list = new ArrayList();
    private List<UnclaimedRPDetailsBean.ResultsBean> data;
    private int count;              //总数量
    private int page = 1;           //页码
    private int rows = 20;          //每页需要展示的数量

    private String teamId = "";

    public static void start(Context context, String teamId) {
        Intent intent = new Intent();
        intent.setClass(context, NoReceivedRPRecordActivity.class);
        intent.putExtra("teamId", teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_noreceivedrprecord_layout);
        context = this;
        setToolbar(R.drawable.jrmf_b_top_back,"未领取零钱红包记录");
        teamId = getIntent().getStringExtra("teamId");
        initView();
        initData();
    }

    private void initView() {
        tvNodata = findView(R.id.tv_noData_content);
        llNodata = findView(R.id.ll_nodata);
        mRecyclerView = findView(R.id.mRecyclerView);
        refreshLayout = findView(R.id.refresh_layout);
        tvNodata.setText("暂无未领取零钱红包");
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
        UserApi.unclaimedRPDetails(page, rows, teamId, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                    }
                    UnclaimedRPDetailsBean bean = (UnclaimedRPDetailsBean) object;
                    count = bean.getCount();
                    data = new ArrayList<>();
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
                toast(errMessage);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
            }
        });
    }

    private void loadData() {
        if (page == 1){
            list = data;
            if (list.size() <= 0){
                llNodata.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }else {
                llNodata.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            mAdapter = new EasyRVAdapter(this, list, R.layout.item_noreceivedrprecord_layout) {
                @Override
                protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                    if (null == list || list.size() == 0) {
                        return;
                    }
                    final UnclaimedRPDetailsBean.ResultsBean bean = list.get(position);
                    NimUserInfoProvider userInfoProvider = new NimUserInfoProvider(context);
                    UserInfo userInfo = userInfoProvider.getUserInfo(bean.getPayerId());
                    HeadImageView imgHead =  viewHolder.getView(R.id.img_Head);
                    TextView tvTime =  viewHolder.getView(R.id.tv_time);
                    TextView tvName =  viewHolder.getView(R.id.tv_name);
                    TextView tvContent =  viewHolder.getView(R.id.tv_bri_target_rev);
                    TextView tvRedPacketContent =  viewHolder.getView(R.id.tv_bri_mess_rev);
                    TextView tvRedPacketType =  viewHolder.getView(R.id.tv_bri_name_rev);
                    final RelativeLayout rlRedPacket =  viewHolder.getView(R.id.bri_rev);
                    imgHead.setIsRect(true);
                    imgHead.loadAvatar(userInfo.getAvatar());
                    tvContent.setText("领取红包");
                    tvTime.setText(TimeUtils.getDateToString(bean.getCreateDate(),TimeUtils.TIME_TYPE_01));
                    tvName.setText(bean.getPayerName());
                    tvRedPacketContent.setText(bean.getName());
                    if (bean.getType() == 1){
                        //单人红包
                        tvRedPacketType.setText("零钱红包");
                    }else if (bean.getType() == 2){
                        //普通红包
                        tvRedPacketType.setText("普通红包");
                    }else if (bean.getType() == 3){
                        //随机红包
                        tvRedPacketType.setText("随机红包");
                    }
                    rlRedPacket.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rlRedPacket.setBackgroundResource(R.drawable.red_packet_rev_press);
                            SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                            if (!instance.getBoolean(Constants.CONFIG_INFO.WALLET_EXIST)){
                                EasyAlertDialogHelper.showCommonDialog(context, "钱包账户未创建", "请先创建钱包账户再领取红包", "确定", "取消", false, new EasyAlertDialogHelper.OnDialogActionListener() {
                                    @Override
                                    public void doCancelAction() {

                                    }

                                    @Override
                                    public void doOkAction() {
                                        SettingPayPasswordActivity.start(context);
                                    }
                                }).show();
                                return;
                            }
                            //检查红包状态
                            redPackStatistic(bean);
                        }
                    });
                }
            };
            mRecyclerView.setAdapter(mAdapter);
        } else {
            for (UnclaimedRPDetailsBean.ResultsBean resultsBean : data) {
                list.add(resultsBean);
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    /**
     * 检查红包状态
     * @param resultsBean
     * */
    private void redPackStatistic(final UnclaimedRPDetailsBean.ResultsBean resultsBean) {
        showProgress(context,false);
        UserApi.getRedPackStatisticNew(resultsBean.getId(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {

                if (code == Constants.SUCCESS_CODE){
                    RedPacketStateBean redPacketStateBean = (RedPacketStateBean) object;
                    if (null == redPacketStateBean){
                        return;
                    }
                    List<String> payeeIdList = redPacketStateBean.getPayeeIdList();
                    Map<String, Object> redPacketMap = new HashMap<>();
                    SenderUserInfoBean senderUserInfoBean = new SenderUserInfoBean();
                    if (StringUtil.isNotEmpty(redPacketStateBean.getPayerId())){
                        UserInfo senderUserInfo = NimUIKit.getUserInfoProvider().getUserInfo(redPacketStateBean.getPayerId());
                        senderUserInfoBean.setAvatar(senderUserInfo.getAvatar());
                        senderUserInfoBean.setUserID(senderUserInfo.getAccount());
                        senderUserInfoBean.setUserName(senderUserInfo.getName());
                    }
                    RedPackOtherDataBean bean = new RedPackOtherDataBean();

                    redPacketMap.put("SenderUserInfoBean",senderUserInfoBean);
                    redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_SENDERID,senderUserInfoBean.getUserID());
                    redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_COUNT,redPacketStateBean.getNumber());
                    redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_ID,redPacketStateBean.getId());

                    bean.setRedContent(redPacketStateBean.getName());
                    bean.setRedId(redPacketStateBean.getId());
                    Map<String,RedPacketStateBean.TargetSignMap> map = new HashMap<>();
                    if (null != redPacketStateBean.targetSignMap){
                        map = redPacketStateBean.targetSignMap;
                    }

                    if (redPacketStateBean.getTargetType() == 1){
                        //单人红包
                        bean.setRedpacketType(2001);
                        bean.setRedTitle("工会小蜜红包");
                    }else if (redPacketStateBean.getTargetType() == 2){
                        if (map.size() > 0){
                            //专属红包
                            bean.setRedpacketType(2005);
                            bean.setRedTitle("专属红包");

                            List<String> receivers = new ArrayList<>();
                            for (RedPacketStateBean.TargetSignMap signMap : map.values()) {
                                receivers.add(signMap.getUid());
                            }
                            redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_RECEIVER, receivers);
                        }else {
                            //普通红包
                            bean.setRedpacketType(2004);
                            bean.setRedTitle("普通红包");
                        }

                    }else if (redPacketStateBean.getTargetType() == 3){
                        //随机红包
                        bean.setRedpacketType(2003);
                        bean.setRedTitle("随机红包");
                    }

                    redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_TYPESTR,bean.getRedTitle());
                    redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_MONEY,redPacketStateBean.getAmount());
                    redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_GREETING,redPacketStateBean.getName());
                    redPacketMap.put(Constants.BUILDREDSTRUCTURE.REDPACKET_TYPE,bean.getRedpacketType());

                    bean.setNumber(redPacketStateBean.getNumber());
                    bean.setCount(payeeIdList.size());
                    bean.setTotalSum(redPacketStateBean.getAmount());
                    bean.setTeamId(redPacketStateBean.getTeamId());
                    if (redPacketStateBean.getStatus() == 1){
                        //待领取
                        if (bean.getRedpacketType() == 2004 || bean.getRedpacketType() == 2005){
                            //普通红包 专属红包 当前用户是红包发送者，不显示弹窗
                            if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderUserInfoBean.getUserID())){
                                RedPackDetailsActivity.start(context,bean,senderUserInfoBean);
                                return;
                            }
                        }

                        for (String payeeId : payeeIdList) {
                            if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(payeeId)){
                                //当前用户已经领取过该红包
                                RedPackDetailsActivity.start(context,bean,senderUserInfoBean);
                                return;
                            }
                        }
                        showRedPackDialog(bean,redPacketMap);

                    }else if (redPacketStateBean.getStatus() == 2){
                        //已领取
                        if (bean.getRedpacketType() == 2004 || bean.getRedpacketType() == 2005){
                            //普通红包 专属红包 当前用户是红包发送者，不显示弹窗
                            if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderUserInfoBean.getUserID())){
                                RedPackDetailsActivity.start(context,bean,senderUserInfoBean);
                                return;
                            }
                        }

                        for (String payeeId : payeeIdList){
                            if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(payeeId)){
                                //当前用户已经领取过该红包
                                RedPackDetailsActivity.start(context,bean,senderUserInfoBean);
                                return;
                            }
                        }
                        showRedPackDialog(bean,redPacketMap);
                    }else if (redPacketStateBean.getStatus() == 3 || redPacketStateBean.getStatus() == 4){
                        //已过期 or 已代领
                        RedPackDetailsActivity.start(context,bean,senderUserInfoBean);
                    }
                }else {
                    dismissProgress();
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

    private void showRedPackDialog(final RedPackOtherDataBean bean, Map<String, Object> redPacketMap) {
        dismissProgress();
        //解析红包数据，并赋值给三个Bean
        AnalyticalRedPackData data = new AnalyticalRedPackData();
        data.analytical(redPacketMap, new RedPackDataCallBack() {
            @Override
            public void getRedPackData(Map<String, Object> map) {
                final RedPackDialog redPackDialog = new RedPackDialog(context,map,bean, null);
                new XPopup.Builder(context)
                        .dismissOnTouchOutside(false)
                        .asCustom(redPackDialog)
                        .show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgress();
    }
}

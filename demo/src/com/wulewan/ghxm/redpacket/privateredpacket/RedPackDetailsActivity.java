package com.wulewan.ghxm.redpacket.privateredpacket;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.RedPackOtherDataBean;
import com.wulewan.ghxm.bean.SenderUserInfoBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.redpacket.wallet.DetailsRedPacketActivity;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.TimeUtils;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.netease.wulewan.uikit.impl.cache.TeamDataCache;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.wulewan.ghxm.utils.TimeUtils.TIME_TYPE_01;

/**
 * 红包领取成功详情页
 * */
public class RedPackDetailsActivity extends BaseAct implements View.OnClickListener {
    private Context context;

    private ImageView imgHead;
    private TextView tvSenderName;
    private TextView tvContent;
    private TextView tvAmount;
    private TextView tvIsShow;
    private TextView tvPrompting;
    private TextView tvCountAndTime;
    private RecyclerView mRecyclerView;
    private TextView tvSeeRecord;
    private LinearLayout llNodata;
    private TextView tvNodata;

    private static String OTHERBEAN = "otherDataBean";
    private static String SENDERBEAN = "senderBean";
    private RedPackOtherDataBean otherDataBean;
    private SenderUserInfoBean senderBean;
    private List<RedPackOtherDataBean.RecordsBean> list = new ArrayList<>();
    private EasyRVAdapter mAssetsAdapter;
    private boolean isOptimum;
    private boolean isMi = false;
    private String money = "";
    private String teamId = "";

    public static void start(Context context, RedPackOtherDataBean otherDataBean, SenderUserInfoBean senderBean) {
        Intent intent = new Intent(context, RedPackDetailsActivity.class);
        intent.putExtra(OTHERBEAN,otherDataBean);
        intent.putExtra(SENDERBEAN,senderBean);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redpackdetails_activity);
        setToolbar(R.drawable.jrmf_b_top_back, "红包",R.color.redpacketdetails_theme);
        context = RedPackDetailsActivity.this;
        otherDataBean = (RedPackOtherDataBean) getIntent().getSerializableExtra(OTHERBEAN);
        senderBean = (SenderUserInfoBean) getIntent().getSerializableExtra(SENDERBEAN);
        initView();
        initData();
    }

    private void initData() {
        showProgress(context,false);
        UserApi.getRedPackStatistic(otherDataBean.getRedId(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    RedPackOtherDataBean bean = (RedPackOtherDataBean) object;
                    teamId = bean.getTeamId();
                    otherDataBean.setTotalSum(bean.getTotalSum());
                    otherDataBean.setTeamId(bean.getTeamId());
                    otherDataBean.setStatus(bean.getStatus());
                    otherDataBean.setCount(bean.getCount());
                    otherDataBean.setNumber(bean.getNumber());
                    otherDataBean.setRedContent(bean.getRedContent());
                    otherDataBean.setExclusive(bean.isExclusive());
                    otherDataBean.setRedTitle(bean.getRedTitle());
                    otherDataBean.setType(bean.getType());

                    switch (otherDataBean.getType()){
                        case 1:
                            otherDataBean.setRedpacketType(2001);
                            break;
                        case 2:
                            if (otherDataBean.isExclusive()){
                                otherDataBean.setRedpacketType(2005);
                            }else {
                                otherDataBean.setRedpacketType(2004);
                            }
                            break;
                        case 3:
                            otherDataBean.setRedpacketType(2003);
                            break;
                    }

                    if (null == otherDataBean.getRecords() || otherDataBean.getRecords().size() < 0){
                        otherDataBean.setRecords(bean.getRecords());
                    }

                    list.addAll(otherDataBean.getRecords());
                    setData();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
            }
        });

    }

    private void setData() {
        for (int i = 0; i < list.size();i++){
            if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(list.get(i).getPayeeId())){
                money = list.get(i).getAmount();
            }
        }
        if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderBean.getUserID())){
            isMi = true;
        }
        switch (otherDataBean.getRedpacketType()){
            case 2001:
            case 2002:
                //单人红包
                mRecyclerView.setVisibility(View.GONE);
                tvCountAndTime.setVisibility(View.GONE);
                if (null != otherDataBean.getRecords() && otherDataBean.getRecords().size() > 0){
                    tvAmount.setText("¥"+otherDataBean.getRecords().get(0).getAmount());
                }
                if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderBean.getUserID())){
                    tvIsShow.setVisibility(View.GONE);
                    tvAmount.setVisibility(View.INVISIBLE);
                    tvPrompting.setVisibility(View.VISIBLE);
                    switch (otherDataBean.getStatus()){
                        case 1:
                            //待领取
                            tvPrompting.setText("红包金额" + otherDataBean.getTotalSum() + "元，等待对方领取");
                            break;
                        case 2:
                            //已领取
                            tvPrompting.setText("红包金额" + otherDataBean.getTotalSum() + "元，对方已领取");
                            break;
                        case 3:
                            //已过期
                            tvPrompting.setText("该红包超过时限未被领取，已退还");
                            break;
                    }

                }else {
                    tvIsShow.setVisibility(View.VISIBLE);
                    tvAmount.setVisibility(View.VISIBLE);
                }
                break;
            case 2003:
                //随机红包
                mRecyclerView.setVisibility(View.VISIBLE);
                tvCountAndTime.setVisibility(View.VISIBLE);
                Drawable drawable = getResources().getDrawable(R.mipmap.pin_icon);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                tvSenderName.setCompoundDrawables(null,null,drawable,null);
                if (StringUtil.isEmpty(money)){
                    tvAmount.setVisibility(View.INVISIBLE);
                    tvIsShow.setVisibility(View.GONE);
                }else {
                    tvAmount.setVisibility(View.VISIBLE);
                    tvIsShow.setVisibility(View.VISIBLE);
                    tvAmount.setText("¥"+money);
                }
                if (otherDataBean.getStatus() == 3){
                    tvPrompting.setVisibility(View.VISIBLE);
                    tvPrompting.setText("该红包超过时限未被领取，已退还");
                }else {
                    tvPrompting.setVisibility(View.GONE);
                }
                if (otherDataBean.getNumber() <= otherDataBean.getCount()){
                    //红包已领完
                    tvCountAndTime.setText(otherDataBean.getNumber()+"个红包，" + TimeUtils.timediff(list.get(0).getCreateDate(),list.get(list.size()-1).getCreateDate()) +"被抢光");
                }else {
                    //红包未领完
                    if (isMi){
                        //是自己发的红包
                        tvCountAndTime.setText("已领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个，共"+otherDataBean.getTotalSum()+"元");
                    }else {
                        //别人的红包
                        tvCountAndTime.setText("领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个");
                    }
                }

                break;
            case 2004:
                //普通红包
                if (isMi){
                    mRecyclerView.setVisibility(View.VISIBLE);
                    tvCountAndTime.setVisibility(View.VISIBLE);

                    if (StringUtil.isEmpty(money)){
                        tvAmount.setVisibility(View.INVISIBLE);
                        tvIsShow.setVisibility(View.GONE);
                    }else {
                        tvAmount.setVisibility(View.VISIBLE);
                        tvIsShow.setVisibility(View.VISIBLE);
                        tvAmount.setText("¥"+money);
                    }
                    if (otherDataBean.getStatus() == 3){
                        tvPrompting.setVisibility(View.VISIBLE);
                        tvPrompting.setText("该红包超过时限未被领取，已退还");
                    }else {
                        tvPrompting.setVisibility(View.GONE);
                    }

                    if (otherDataBean.getNumber() <= otherDataBean.getCount()){
                        //红包已领完
                        tvCountAndTime.setText(otherDataBean.getNumber()+"个红包，" + TimeUtils.timediff(list.get(0).getCreateDate(),list.get(list.size()-1).getCreateDate()) +"被抢光");
                    }else {
                        //红包未领完
                        if (isMi){
                            //是自己发的红包
                            tvCountAndTime.setText("已领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个，共"+otherDataBean.getTotalSum()+"元");
                        }else {
                            //别人的红包
                            tvCountAndTime.setText("领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个");
                        }
                    }
                }else {
                    if (StringUtil.isEmpty(money)){
                        tvAmount.setVisibility(View.INVISIBLE);
                        tvIsShow.setVisibility(View.GONE);

                        mRecyclerView.setVisibility(View.VISIBLE);
                        tvCountAndTime.setVisibility(View.VISIBLE);

                        if (StringUtil.isEmpty(money)){
                            tvAmount.setVisibility(View.INVISIBLE);
                            tvIsShow.setVisibility(View.GONE);
                        }else {
                            tvAmount.setVisibility(View.VISIBLE);
                            tvIsShow.setVisibility(View.VISIBLE);
                            tvAmount.setText("¥"+money);
                        }
                        if (otherDataBean.getStatus() == 3){
                            tvPrompting.setVisibility(View.VISIBLE);
                            tvPrompting.setText("该红包超过时限未被领取，已退还");
                        }else {
                            tvPrompting.setVisibility(View.GONE);
                        }

                        if (otherDataBean.getNumber() <= otherDataBean.getCount()){
                            //红包已领完
                            tvCountAndTime.setText(otherDataBean.getNumber()+"个红包，" + TimeUtils.timediff(list.get(0).getCreateDate(),list.get(list.size()-1).getCreateDate()) +"被抢光");
                        }else {
                            //红包未领完
                            if (isMi){
                                //是自己发的红包
                                tvCountAndTime.setText("已领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个，共"+otherDataBean.getTotalSum()+"元");
                            }else {
                                //别人的红包
                                tvCountAndTime.setText("领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个");
                            }
                        }
                    }else {
                        mRecyclerView.setVisibility(View.GONE);
                        tvCountAndTime.setVisibility(View.GONE);
                        tvAmount.setVisibility(View.VISIBLE);
                        tvIsShow.setVisibility(View.VISIBLE);
                        tvAmount.setText("¥"+money);
                    }

                    if (otherDataBean.getStatus() == 3){
                        tvPrompting.setVisibility(View.VISIBLE);
                        tvPrompting.setText("该红包超过时限未被领取，已退还");
                    }else {
                        tvPrompting.setVisibility(View.GONE);
                    }
                }
                break;
            case 2005:
                //专属红包
                Drawable drawabl = getResources().getDrawable(R.mipmap.zhuan_icon);
                drawabl.setBounds(0, 0, drawabl.getMinimumWidth(), drawabl.getMinimumHeight());
                tvSenderName.setCompoundDrawables(null,null,drawabl,null);
                if (isMi){
                    mRecyclerView.setVisibility(View.VISIBLE);
                    tvCountAndTime.setVisibility(View.VISIBLE);

                    if (StringUtil.isEmpty(money)){
                        tvAmount.setVisibility(View.INVISIBLE);
                        tvIsShow.setVisibility(View.GONE);
                    }else {
                        tvAmount.setVisibility(View.VISIBLE);
                        tvIsShow.setVisibility(View.VISIBLE);
                        tvAmount.setText("¥"+money);
                    }
                    if (otherDataBean.getStatus() == 3){
                        tvPrompting.setVisibility(View.VISIBLE);
                        tvPrompting.setText("该红包超过时限未被领取，已退还");
                    }else {
                        tvPrompting.setVisibility(View.GONE);
                    }

                    if (otherDataBean.getNumber() <= otherDataBean.getCount()){
                        //红包已领完
                        tvCountAndTime.setText(otherDataBean.getNumber()+"个红包，" + TimeUtils.timediff(list.get(0).getCreateDate(),list.get(list.size()-1).getCreateDate()) +"被抢光");
                    }else {
                        //红包未领完
                        if (isMi){
                            //是自己发的红包
                            tvCountAndTime.setText("已领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个，共"+otherDataBean.getTotalSum()+"元");
                        }else {
                            //别人的红包
                            tvCountAndTime.setText("领取" + otherDataBean.getCount()+ "/" + otherDataBean.getNumber() + "个");
                        }
                    }
                }else {
                    mRecyclerView.setVisibility(View.GONE);
                    tvCountAndTime.setVisibility(View.GONE);

                    if (null != otherDataBean.getRecords() && otherDataBean.getRecords().size() > 0){
                        tvAmount.setText("¥"+otherDataBean.getRecords().get(0).getAmount());
                    }
                    if (otherDataBean.getStatus() == 3){
                        tvPrompting.setVisibility(View.VISIBLE);
                        tvPrompting.setText("该红包超过时限未被领取，已退还");
                    }else {
                        tvPrompting.setVisibility(View.GONE);
                    }
                }
                break;
        }
        tvSenderName.setText(senderBean.getUserName(teamId)+"的红包");

        GlideUtil.loadCircular(context,senderBean.getAvatar(),imgHead);
        tvContent.setText(StringUtil.isEmpty(otherDataBean.getRedContent())?"恭喜发财，大吉大利":otherDataBean.getRedContent());
        if (isMi || otherDataBean.getRedpacketType() == 2003){
            if (otherDataBean.getRedpacketType() == 2001 || otherDataBean.getRedpacketType() == 2002){
                return;
            }
            if (list.size() <= 0){
                llNodata.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }else {
                llNodata.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
        if (null != list && list.size() > 0){
            loadData();
        }
    }

    List<Double> amountList = new ArrayList<>();
    int pos;
    private void loadData() {
        for (int i = 0; i < list.size(); i++){
            amountList.add(Double.valueOf(list.get(i).getAmount()));
        }
        pos = amountList.indexOf(Collections.max(amountList));
        mAssetsAdapter = new EasyRVAdapter(context,list,R.layout.redpackdetails_item) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                RedPackOtherDataBean.RecordsBean recordsBean = list.get(position);
                ImageView imgHeadItem = viewHolder.getView(R.id.img_redpackdetailsItem_head);
                TextView tvName = viewHolder.getView(R.id.tv_redpackdetailsItem_name);
                TextView tvTime = viewHolder.getView(R.id.tv_redpackdetailsItem_time);
                TextView tvAmount = viewHolder.getView(R.id.tv_redpackdetailsItem_amount);
                TextView tvIsOptimum = viewHolder.getView(R.id.tv_redpackdetailsItem_isOptimum);
                UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(recordsBean.getPayeeId());
                if (!StringUtil.isEmpty(teamId) && !StringUtil.isEmpty(recordsBean.getPayeeId())){
                    TeamMember teamMember = TeamDataCache.getInstance().getTeamMember(teamId,recordsBean.getPayeeId());
                    if (StringUtil.isNotEmpty(teamId) && StringUtil.isNotEmpty(teamMember.getTeamNick())){
                        tvName.setText(teamMember.getTeamNick());
                    }else {
                        tvName.setText(userInfo.getName());
                    }
                }


                if (null != userInfo && StringUtil.isNotEmpty(userInfo.getAvatar())){
                    GlideUtil.loadCircular(context,userInfo.getAvatar(),imgHeadItem);
                }

                if (recordsBean.getType() == 6){
                    tvName.setText(tvName.getText().toString() + "(代领)");
                }

                tvTime.setText(TimeUtils.getDateToString(recordsBean.getCreateDate(),TIME_TYPE_01));
                tvAmount.setText(recordsBean.getAmount() + "元");
                if (list.size() == 1 || position == pos){
                    tvIsOptimum.setVisibility(View.VISIBLE);
                }
            }
        };
        mRecyclerView.setAdapter(mAssetsAdapter);
    }

    private void initView() {

        imgHead = (ImageView) findViewById(R.id.img_redpackdetails_head);
        tvSenderName = (TextView) findViewById(R.id.tv_redpackdetails_senderName);
        tvContent = (TextView) findViewById(R.id.tv_redpackdetails_content);
        tvAmount = (TextView) findViewById(R.id.tv_redpackdetails_amount);
        tvIsShow = (TextView) findViewById(R.id.tv_redpackdetails_isShow);
        tvPrompting = (TextView) findViewById(R.id.tv_redpackdetails_Prompting);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        tvCountAndTime = (TextView) findViewById(R.id.tv_redpackdetails_countAndTime);
        tvSeeRecord = (TextView) findViewById(R.id.tv_redpackdetails_SeeRecord);
        llNodata = findView(R.id.ll_nodata);
        tvNodata = findView(R.id.tv_noData_content);
        tvNodata.setText("暂未被领取");

        tvSeeRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_redpackdetails_SeeRecord:
                //查看红包记录
                DetailsRedPacketActivity.start(context);
                break;
        }
    }
}

package com.wulewan.ghxm.utils.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.CenterPopupView;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.ReceiverUserInfoBean;
import com.wulewan.ghxm.bean.RedPackOtherDataBean;
import com.wulewan.ghxm.bean.SenderUserInfoBean;
import com.wulewan.ghxm.utils.NumberUtil;
import com.wulewan.ghxm.utils.SPUtils;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.ui.dialog.DialogMaker;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.redpacket.NIMOpenRpCallback;
import com.wulewan.ghxm.redpacket.privateredpacket.RedPackDetailsActivity;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedPackDialog extends CenterPopupView implements View.OnClickListener {

    private Context context;
    private int status;
    private RelativeLayout layout;
    private LinearLayout llExclusive;               //专属红包视图
    private LinearLayout llNoExclusive;               //非专属红包视图
    private LinearLayout llNoRedPackData;               //红包抢完试图
    private ImageView imgBack;
    private ImageView imgHead;
    private ImageView imgNoDataHead;
    private RecyclerView mRecyclerView;
    private TextView tvContent;                  //红包内容
    private TextView tvSenderName;                  //发送者姓名
    private TextView tvSenderNameNo;                  //发送者姓名
    private TextView tvSenderNameNoData;                  //发送者姓名
    private TextView tvSeeData;                  //看看大家手气
    private TextView tvReceiverName;                //接收者姓名
    private TextView tvReceiverNameNo;                //接收者姓名
    private TextView tvOpenRedPack;                 //拆红包
    private EasyRVAdapter receiverAdapter;          //接收者头像
    private List<String> list = new ArrayList<>();
    private Map<String,Object> map;
    private List<ReceiverUserInfoBean> receiverList;
    private SenderUserInfoBean senderUserInfoBean;
    private RedPackOtherDataBean redPackOtherDataBean;
    private ReceiverUserInfoBean receiverUserInfoBean;

    private boolean isHave;                         //接收者中是否有自己
    private Integer redpacketType;                  //红包类型
    private int viewWidth,itemWidth;
    private NIMOpenRpCallback openRpCallback;

    public RedPackDialog(@NonNull Context context, Map<String, Object> map,RedPackOtherDataBean bean, NIMOpenRpCallback cb) {
        super(context);
        this.context = context;
        this.map = map;
        this.redPackOtherDataBean = bean;
        this.openRpCallback = cb;
    }
    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_redpack_layout;
    }

    @Override
    protected void initPopupContent() {
        super.initPopupContent();
        // 实现一些UI的初始和逻辑处理
        initData();
        initView();
    }

    private void initData() {
        senderUserInfoBean = (SenderUserInfoBean) map.get("SenderUserInfoBean");
        receiverList = (List<ReceiverUserInfoBean>) map.get("ReceiverUserInfoBean");

        if (null != senderUserInfoBean){
            list.add(senderUserInfoBean.getAvatar());
        }
        if (null != receiverList&&receiverList.size() > 0){
            for (int i = 0; i < receiverList.size(); i++){
                receiverUserInfoBean = receiverList.get(i);
                if (receiverUserInfoBean.getUserID().equals(SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID))){
                    isHave = true;
                }
                list.add(receiverUserInfoBean.getAvatar());

            }
        }
        redpacketType = redPackOtherDataBean.getRedpacketType();

    }

    private void initView() {
        llExclusive = findViewById(R.id.ll_redDialog_exclusive);
        llNoExclusive = findViewById(R.id.ll_redDialog_NoExclusive);
        llNoRedPackData = findViewById(R.id.ll_redDialog_NoRedPack);
        imgBack = findViewById(R.id.img_redPackDialog_back);
        imgHead = findViewById(R.id.img_redDialog_head);
        imgNoDataHead = findViewById(R.id.img_redDialog_NoRedPack_head);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        tvContent = findViewById(R.id.tv_redPackdialog_No_content);
        tvSenderName = findViewById(R.id.tv_redPack_dialog_senderName);
        tvSenderNameNo = findViewById(R.id.tv_redPackdialog_No_senderName);
        tvSenderNameNoData = findViewById(R.id.tv_redPackdialog_NoRedPack_senderName);
        tvSeeData = findViewById(R.id.tv_redPackdialog_NoRedPack_seeData);
        tvReceiverName = findViewById(R.id.tv_redPack_dialog_receiverName);
        tvReceiverNameNo = findViewById(R.id.tv_redPackdialog_No_receiverName);
        tvOpenRedPack = findViewById(R.id.tv_redPack_dialog_openRedPack);
        layout = findViewById(R.id.rl_redPackDialog_bg);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        loadData();

        imgBack.setOnClickListener(this);
        tvOpenRedPack.setOnClickListener(this);
        tvSeeData.setOnClickListener(this);
    }

    private void loadData() {
        switch (redpacketType){
            case 2001:
            case 2002:
                //单人红包
                tvOpenRedPack.setVisibility(VISIBLE);
                llNoRedPackData.setVisibility(GONE);

                llExclusive.setVisibility(GONE);
                llNoExclusive.setVisibility(VISIBLE);
                tvSenderNameNo.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                tvReceiverNameNo.setText("给你发了一个红包");
                tvContent.setText(redPackOtherDataBean.getRedContent());
                isHave = true;
                tvOpenRedPack.setText("拆红包");
                GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgHead);
                break;
            case 2003:
                //随机红包
                if (redPackOtherDataBean.getNumber() > redPackOtherDataBean.getCount()){
                    //判断是否还有红包可领取    可以领取
                    if (redPackOtherDataBean.getStatus() == 4 || redPackOtherDataBean.getStatus() == 3){
                        //红包已过期 or 红包被群主代领
                        tvOpenRedPack.setVisibility(GONE);
                        llNoRedPackData.setVisibility(VISIBLE);
                        llNoExclusive.setVisibility(GONE);
                        llExclusive.setVisibility(GONE);
                        GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgNoDataHead);
                        tvSenderNameNoData.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                        return;
                    }
                    tvOpenRedPack.setVisibility(VISIBLE);
                    llNoRedPackData.setVisibility(GONE);
                    llExclusive.setVisibility(GONE);
                    llNoExclusive.setVisibility(VISIBLE);
                    tvSenderNameNo.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                    tvReceiverNameNo.setText("发了一个红包金额随机");
                    tvContent.setText(redPackOtherDataBean.getRedContent());
                    isHave = true;
                    tvOpenRedPack.setText("拆红包");
                    GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgHead);
                }else {
                    //没有多余红包
                    tvOpenRedPack.setVisibility(GONE);
                    llNoRedPackData.setVisibility(VISIBLE);
                    llNoExclusive.setVisibility(GONE);
                    llExclusive.setVisibility(GONE);
                    GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgNoDataHead);
                    tvSenderNameNoData.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                }
                break;
            case 2004:
                //普通红包
                if (redPackOtherDataBean.getNumber() > redPackOtherDataBean.getCount()){
                    if (redPackOtherDataBean.getStatus() == 4 || redPackOtherDataBean.getStatus() == 3){
                        //红包已过期 or 红包被群主代领
                        tvSeeData.setVisibility(GONE);
                        tvOpenRedPack.setVisibility(GONE);
                        llNoRedPackData.setVisibility(VISIBLE);
                        llNoExclusive.setVisibility(GONE);
                        llExclusive.setVisibility(GONE);
                        GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgNoDataHead);
                        tvSenderNameNoData.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                        return;
                    }
                    llExclusive.setVisibility(GONE);
                    llNoExclusive.setVisibility(VISIBLE);
                    llNoRedPackData.setVisibility(GONE);
                    tvSenderNameNo.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                    tvReceiverNameNo.setText("给你发了一个红包");
                    tvContent.setText(redPackOtherDataBean.getRedContent());
                    isHave = true;
                    tvOpenRedPack.setText("拆红包");
                    GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgHead);
                }else {
                    //没有多余红包
                    tvSeeData.setVisibility(GONE);
                    tvOpenRedPack.setVisibility(GONE);
                    llNoRedPackData.setVisibility(VISIBLE);
                    llNoExclusive.setVisibility(GONE);
                    llExclusive.setVisibility(GONE);
                    GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgNoDataHead);
                    tvSenderNameNoData.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                }
                break;
            case 2005:
                //专属红包
                tvOpenRedPack.setVisibility(VISIBLE);
                llNoRedPackData.setVisibility(GONE);
                llExclusive.setVisibility(VISIBLE);
                llNoExclusive.setVisibility(GONE);
                tvSenderName.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));

                if(redPackOtherDataBean.getStatus()== 2){
                    tvReceiverName.setText("");
                    tvOpenRedPack.setText("偷偷瞅一眼");
                }else{
                    if (isHave){
                        tvReceiverName.setText("给你发了一个红包");
                        tvOpenRedPack.setText("拆红包");
                    }else {
                        tvReceiverName.setText("给" + receiverList.get(0).getUserName() + "发了一个红包");
                        tvOpenRedPack.setText("默默关掉");
                    }
                }
                if (redPackOtherDataBean.getStatus() == 4 || redPackOtherDataBean.getStatus() == 3){
                    //红包已过期 or 红包被群主代领
                    tvSeeData.setVisibility(GONE);
                    tvOpenRedPack.setVisibility(GONE);
                    llNoRedPackData.setVisibility(VISIBLE);
                    llNoExclusive.setVisibility(GONE);
                    llExclusive.setVisibility(GONE);
                    GlideUtil.loadCircular(context,senderUserInfoBean.getAvatar(),imgNoDataHead);
                    tvSenderNameNoData.setText(senderUserInfoBean.getUserName(redPackOtherDataBean.getTeamId()));
                    return;
                }
                break;
        }

        //获取Dialog的宽度
        viewWidth = NumberUtil.px2dip(context,getMaxWidth());
        if (viewWidth / list.size() < 72){
            itemWidth = viewWidth / list.size();
        }else {
            itemWidth = 72;
        }

        receiverAdapter = new EasyRVAdapter(context,list,R.layout.redpack_dialog_head_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, final int position, Object item) {
                ImageView imgHead = viewHolder.getView(R.id.img_redpackDialog_head);
                final RelativeLayout imgHeadBg = viewHolder.getView(R.id.rl_redpackDialog_head_bg);
                GlideUtil.loadCircular(context,list.get(position),imgHead);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) imgHeadBg.getLayoutParams();
                //设置布局的宽度属性
                layoutParams.width = NumberUtil.dip2px(context,itemWidth);
                layoutParams.height = NumberUtil.dip2px(context,itemWidth);
                //设置布局参数
                imgHeadBg.getLayoutParams();
                imgHeadBg.setLayoutParams(layoutParams);
                if (position == 0){
                    layoutParams.setMargins(NumberUtil.dip2px(context,10),0,NumberUtil.dip2px(context,-10),0);
                }
                if (position == list.size()-1){
                    layoutParams.setMargins(0,0,NumberUtil.dip2px(context,10),0);
                }
            }
        };
        mRecyclerView.setAdapter(receiverAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_redPackDialog_back:
                dismiss();
                break;
            case R.id.tv_redPack_dialog_openRedPack:

                if(redPackOtherDataBean.getStatus()== 2){
                    if(tvOpenRedPack.getText().equals("偷偷瞅一眼")){
                        tvReceiverName.setText("¥ "+redPackOtherDataBean.getTotalSum());
                        tvOpenRedPack.setText("默默关掉");
                    }else{
                        dismiss();
                    }

                }else{

                    if (isHave){
                        //拆红包
                        if (redPackOtherDataBean.getStatus() == 2){
                            ToastUtil.showToast(context,"已领取该红包");
                            dismiss();
                            return;
                        }
                        if (redPackOtherDataBean.getStatus() == 3){
                            ToastUtil.showToast(context,"红包已过期");
                            dismiss();
                            return;
                        }
                        if (redPackOtherDataBean.getStatus() == 4){
                            ToastUtil.showToast(context,"红包已被领取完");
                            dismiss();
                            return;
                        }
                        if (redPackOtherDataBean.getCount() >= redPackOtherDataBean.getNumber()){
                            ToastUtil.showToast(context,"红包已被领取完");
                            dismiss();
                            return;
                        }
                        getRedPack();
                    }else {
                        dismiss();
                    }

                }


                break;
            case R.id.tv_redPackdialog_NoRedPack_seeData:
                //看看大家手气
                getRedPackStatistic(1);
                dismiss();
                break;
        }
    }

    /**
     * @param intent 传1去红包详情页面   传2弹出红包已领完Dialog
     * */
    private void getRedPackStatistic(final int intent) {
        DialogMaker.showProgressDialog(context, context.getString(R.string.empty), false);
        UserApi.getRedPackStatistic(redPackOtherDataBean.getRedId(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                DialogMaker.dismissProgressDialog();
                if (code == Constants.SUCCESS_CODE){
                    RedPackOtherDataBean otherDataBean = (RedPackOtherDataBean) object;
                    otherDataBean.setTotalSum(redPackOtherDataBean.getTotalSum());
                    otherDataBean.setRedpacketType(redPackOtherDataBean.getRedpacketType());
                    otherDataBean.setRedTitle(redPackOtherDataBean.getRedTitle());
                    otherDataBean.setRedId(redPackOtherDataBean.getRedId());
                    otherDataBean.setRedContent(redPackOtherDataBean.getRedContent());
                    if (intent == 1){
                        RedPackDetailsActivity.start(context,otherDataBean,senderUserInfoBean);
                    }else if (intent == 2){
                        RedPackDialog redPackDialog = new RedPackDialog(context,map,otherDataBean,openRpCallback);
                        new XPopup.Builder(context)
                                .dismissOnTouchOutside(false)
                                .asCustom(redPackDialog)
                                .show();
                    }
                }else {
                    ToastUtil.showToast(context, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();
                ToastUtil.showToast(context,errMessage);
            }
        });
    }

    private void getRedPack() {
        DialogMaker.showProgressDialog(context, context.getString(R.string.empty), false);
        UserApi.getRedPack(redPackOtherDataBean.getRedId(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                DialogMaker.dismissProgressDialog();
                if (code == Constants.SUCCESS_CODE){
                    RedPackOtherDataBean otherDataBean = (RedPackOtherDataBean) object;
                    otherDataBean.setTotalSum(redPackOtherDataBean.getTotalSum());
                    otherDataBean.setRedpacketType(redPackOtherDataBean.getRedpacketType());
                    otherDataBean.setRedTitle(redPackOtherDataBean.getRedTitle());
                    otherDataBean.setRedId(redPackOtherDataBean.getRedId());
                    otherDataBean.setRedContent(redPackOtherDataBean.getRedContent());
                    RedPackDetailsActivity.start(context,otherDataBean,senderUserInfoBean);
                    if (null != openRpCallback){
                        openRpCallback.sendMessage(NimUIKit.getAccount(), redPackOtherDataBean.getRedId(), otherDataBean.getNumber() < otherDataBean.getCount());
                    }
                }else if (code == Constants.RESPONSE_CODE.CODE_50004){
                    //红包已领完
                    getRedPackStatistic(2);
                }else {
                    ToastUtil.showToast(context, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();
                ToastUtil.showToast(context,errMessage);
            }
        });
        dismiss();
    }
}

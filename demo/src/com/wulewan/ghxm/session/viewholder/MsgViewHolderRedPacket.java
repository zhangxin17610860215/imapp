package com.wulewan.ghxm.session.viewholder;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.netease.wulewan.uikit.business.chatroom.adapter.ChatRoomMsgAdapter;
import com.netease.wulewan.uikit.business.session.module.ModuleProxy;
import com.netease.wulewan.uikit.business.session.module.list.MsgAdapter;
import com.netease.wulewan.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.RedPackOtherDataBean;
import com.wulewan.ghxm.bean.RedPacketStateBean;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.redpacket.NIMOpenRpCallback;
import com.wulewan.ghxm.redpacket.NIMRedPacketClient;
import com.wulewan.ghxm.redpacket.wallet.SettingPayPasswordActivity;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.session.extension.RedPacketAttachment;
import com.wulewan.ghxm.utils.SPUtils;

import java.util.Map;

import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_GREETING;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_ID;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_MONEY;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_TYPE;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_TYPESTR;
import static com.wulewan.ghxm.config.Constants.CONFIG_INFO.WALLET_EXIST;

public class MsgViewHolderRedPacket extends MsgViewHolderBase {

    private RelativeLayout sendView, revView;
    private TextView sendContentText, revContentText;    // 红包描述
    private TextView sendTitleText, revTitleText;    // 红包名称

    public MsgViewHolderRedPacket(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.red_packet_item;
    }

    @Override
    protected void inflateContentView() {
        sendContentText = findViewById(R.id.tv_bri_mess_send);
        sendTitleText = findViewById(R.id.tv_bri_name_send);
        sendView = findViewById(R.id.bri_send);
        revContentText = findViewById(R.id.tv_bri_mess_rev);
        revTitleText = findViewById(R.id.tv_bri_name_rev);
        revView = findViewById(R.id.bri_rev);
    }

    @Override
    protected void bindContentView() {
        RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
//        if (!isReceivedMessage() && message.getStatus() != MsgStatusEnum.read) {
//            revView.setBackgroundResource(R.drawable.red_packet_rev_normal);
//        } else {
//            revView.setBackgroundResource(R.drawable.red_packet_rev_press);
//        }

        String isGray = "No";

        if (!isReceivedMessage()) {// 消息方向，自己发送的
            sendView.setVisibility(View.VISIBLE);
            revView.setVisibility(View.GONE);
            sendContentText.setText(attachment.getRpContent());
            sendTitleText.setText(attachment.getRpTitle());
            if (isGray.equals("Yes")){
                sendView.setBackgroundResource(R.drawable.red_packet_send_press);
            }else {
                sendView.setBackgroundResource(R.drawable.red_packet_send_normal);
            }
        } else {
            sendView.setVisibility(View.GONE);
            revView.setVisibility(View.VISIBLE);
            revContentText.setText(attachment.getRpContent());
            revTitleText.setText(attachment.getRpTitle());
            if (isGray.equals("Yes")){
                revView.setBackgroundResource(R.drawable.red_packet_rev_press);
            }else {
                revView.setBackgroundResource(R.drawable.red_packet_rev_normal);
            }
        }
    }

    @Override
    protected int leftBackground() {
        return R.color.transparent;
    }

    @Override
    protected int rightBackground() {
        return R.color.transparent;
    }

    @Override
    protected void onItemClick() {
        // 拆红包
        if (!isReceivedMessage()){
            //自己发送的
            sendView.setBackgroundResource(R.drawable.red_packet_send_press);
        }else {
            //别人发送的
            revView.setBackgroundResource(R.drawable.red_packet_rev_press);
        }
//        if (isReceivedMessage()) {
//            revView.setBackgroundResource(R.drawable.red_packet_rev_press);
//        }

//        if (message.getStatus() != MsgStatusEnum.read) {
//            // 将未读标识去掉,更新数据库
//            unreadIndicator.setVisibility(View.GONE);
//        }
        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        if (!instance.getBoolean(WALLET_EXIST)){
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
//        redPackStatistic();
        //检查红包状态
        redPackStatisticNew();
    }

    private void redPackStatistic() {
        showProgress(context,false);
        final RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
        final Map<String, Object> remoteExtension = message.getRemoteExtension();
        UserApi.getRedPackStatistic(attachment.getRpId(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    RedPackOtherDataBean bean = (RedPackOtherDataBean) object;
//                    bean.setNumber((int) remoteExtension.get(REDPACKET_COUNT));
                    bean.setRedContent((String) remoteExtension.get(REDPACKET_GREETING));
                    bean.setRedId((String) remoteExtension.get(REDPACKET_ID));
                    bean.setRedpacketType((Integer) remoteExtension.get(REDPACKET_TYPE));
                    bean.setRedTitle((String) remoteExtension.get(REDPACKET_TYPESTR));
                    bean.setTotalSum((String) remoteExtension.get(REDPACKET_MONEY));
                    BaseMultiItemFetchLoadAdapter adapter = getAdapter();
                    ModuleProxy proxy = null;
                    if (adapter instanceof MsgAdapter) {
                        proxy = ((MsgAdapter) adapter).getContainer().proxy;
                    } else if (adapter instanceof ChatRoomMsgAdapter) {
                        proxy = ((ChatRoomMsgAdapter) adapter).getContainer().proxy;
                    }
                    NIMOpenRpCallback cb = new NIMOpenRpCallback(message.getFromAccount(), message.getSessionId(), message.getSessionType(), proxy);
                    NIMRedPacketClient.startOpenRpDialog((Activity) context, message.getSessionType(), attachment.getRpId(),message, cb, bean);
                }else {
                    ToastUtil.showToast(context, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(context,errMessage);
            }
        });
    }

    private void redPackStatisticNew() {
        final RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
        showProgress(context,false);
        UserApi.getRedPackStatisticNew(attachment.getRpId(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (Constants.SUCCESS_CODE == code){
                    RedPacketStateBean bean = (RedPacketStateBean) object;
                    BaseMultiItemFetchLoadAdapter adapter = getAdapter();
                    ModuleProxy proxy = null;
                    if (adapter instanceof MsgAdapter) {
                        proxy = ((MsgAdapter) adapter).getContainer().proxy;
                    } else if (adapter instanceof ChatRoomMsgAdapter) {
                        proxy = ((ChatRoomMsgAdapter) adapter).getContainer().proxy;
                    }
                    NIMOpenRpCallback cb = new NIMOpenRpCallback(message.getFromAccount(), message.getSessionId(), message.getSessionType(), proxy);
                    NIMRedPacketClient.startOpenRpDialog((Activity) context, message.getSessionType(), attachment.getRpId(),message, cb, bean);
                }else {
                    ToastUtil.showToast(context, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(context,errMessage);
            }
        });
    }
}

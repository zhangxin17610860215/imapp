package com.wulewan.ghxm.redpacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.jrmf360.normallib.JrmfClient;
import com.jrmf360.normallib.base.http.OkHttpModelCallBack;
import com.jrmf360.normallib.rp.JrmfRpClient;
import com.jrmf360.normallib.rp.bean.GrabRpBean;
import com.jrmf360.normallib.rp.http.model.BaseModel;
import com.jrmf360.normallib.rp.utils.callback.GrabRpCallBack;
import com.jrmf360.normallib.wallet.JrmfWalletClient;
import com.lxj.xpopup.XPopup;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.redpacket.RedPacketService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.util.log.LogUtil;
import com.wulewan.ghxm.DemoCache;
import com.wulewan.ghxm.bean.RedPackOtherDataBean;
import com.wulewan.ghxm.bean.RedPacketStateBean;
import com.wulewan.ghxm.bean.SenderUserInfoBean;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.redpacket.privateredpacket.AnalyticalRedPackData;
import com.wulewan.ghxm.redpacket.privateredpacket.GroupRedPacketActivity;
import com.wulewan.ghxm.redpacket.privateredpacket.P2PRedPackActivity;
import com.wulewan.ghxm.redpacket.privateredpacket.RedPackDataCallBack;
import com.wulewan.ghxm.redpacket.privateredpacket.RedPackDetailsActivity;
import com.wulewan.ghxm.session.extension.RedPacketAttachment;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.view.RedPackDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_COUNT;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_GREETING;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_ID;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_MONEY;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_RECEIVER;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_SENDER;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_SENDERID;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_TYPE;
import static com.wulewan.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_TYPESTR;

/**
 * 金融魔方红包SDK接口封装，开发者依赖云信SDK 接入金融魔方需要逻辑流程如下：
 * <p>
 * 1.manifest 清单文件配置金融魔方SDK 渠道id（JRMF_PARTNER_ID）以及红包名称，渠道id 选择云信appkey，红包名称开发者自定义
 * 2.在主进程 application 初始化时调用初始化金融魔方SDK
 * 3.在登陆完成之后使用 RedPacketService#getRedPacketAuthToken 获取金融魔方thridToken，这是以后请求金融魔方sdk的身份凭证
 * 4.红包消息、拆红包消息分别使用自定义消息，参考 {@link com.wulewan.ghxm.session.extension.RedPacketAttachment}、
 * {@link com.wulewan.ghxm.session.extension.RedPacketOpenedAttachment},在调用金融魔方接口发送红包成功之后，发送
 * 附件为 RedPacketAttachment 的自定义消息，在拆红包成功之后调用附件为 RedPacketOpenedAttachment 的自定义消息。
 * 5.在自己的个人信息更改之后，更新个人信息到金融魔方，包含昵称、头像等，用于红包界面的展示。
 */

public class NIMRedPacketClient {

    private static boolean init;

    private static NimUserInfo selfInfo;

    private static String thirdToken;
    private static Observer<StatusCode> observer = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode == StatusCode.LOGINED) {
                getThirdToken();
            }
        }
    };

    private static Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> nimUserInfo) {
            for (NimUserInfo userInfo : nimUserInfo) {
                if (userInfo.getAccount().equals(DemoCache.getAccount())) {
                    // 更新 jrmf 用户昵称、头像信息
                    selfInfo = userInfo;
//                    updateMyInfo();
                    return;
                }
            }
        }
    };

    private static void getRpAuthToken() {
        NIMClient.getService(RedPacketService.class).getRedPacketAuthToken().setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    thirdToken = result;
                } else if (code == ResponseCode.RES_RP_INVALID) {
                    // 红包功能不可用
//                    ToastHelper.showToast(DemoCache.getContext(), "红包功能不可用");
                } else if (code == ResponseCode.RES_FORBIDDEN) {
                    // 应用没开通红包功能
//                    ToastHelper.showToast(DemoCache.getContext(), "应用没开通红包功能");
                }
            }
        });
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
//        initJrmfSDK(context);
        init = true;

        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(observer, true);
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, true);

        RpOpenedMessageFilter.startFilter();
    }

    /**
     * 初始化金融魔方SDK
     *
     * @param context
     */
    private static void initJrmfSDK(Context context) {
        //初始化红包sdk
        JrmfClient.isDebug(false);

        JrmfClient.init(context);

        // com.jrmf360.neteaselib.base.utils.LogUtil.init(true);
        // 设置微信appid，如果不使用微信支付可以不调用，此处需要开发者到微信支付申请appid
        // JrmfClient.setWxAppid("xxxxxx");
    }

    public static boolean isEnable() {
        return init;
    }

    private static boolean checkValid() {
        return init && (selfInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount())) != null;
    }

    /**
     * 获取 thirdToken
     *
     * @return thirdToken
     */
    public static String getThirdToken() {
        if (TextUtils.isEmpty(thirdToken)) {
            getRpAuthToken();
        }
        return thirdToken;
    }

    /**
     * 登出之后，清掉token
     */
    public static void clear() {
        thirdToken = null;
    }

    /**
     * 跳转至我的钱包界面
     *
     * @param activity context
     */
    public static void startWalletActivity(Activity activity) {
        if (checkValid()) {
            JrmfWalletClient.intentWallet(activity, DemoCache.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar());
        }
    }

    /**
     * 打开红包发送界面
     *
     * @param activity        context
     * @param sessionTypeEnum 会话类型，支持单聊和群聊
     * @param targetAccount   会话对象目标 account
     * @param requestCode     startActivityForResult requestCode
     */
    public static void startSendRpActivity(final Activity activity, SessionTypeEnum sessionTypeEnum, String targetAccount, int requestCode) {
        if (!checkValid()) {
            return;
        }

        if (sessionTypeEnum == SessionTypeEnum.Team) { // 群聊红包
            // 调用群聊红包接口
            Team team = NimUIKit.getTeamProvider().getTeamById(targetAccount);
            int count = team == null ? 0 : team.getMemberCount();
            //跳转群红包页面
            Intent intent = new Intent(activity,GroupRedPacketActivity.class);
            intent.putExtra("teamId",team.getId());
            activity.startActivityForResult(intent,requestCode);
//            JrmfRpClient.sendGroupEnvelopeForResult(activity, targetAccount, selfInfo.getAccount(), thirdToken, count, selfInfo.getName(), selfInfo.getAvatar(), requestCode);
        } else { // 单聊红包
            Intent intent = new Intent(activity,P2PRedPackActivity.class);
            intent.putExtra("targetAccount",targetAccount);
            activity.startActivityForResult(intent,requestCode);
//            JrmfRpClient.sendSingleEnvelopeForResult(activity, targetAccount, selfInfo.getAccount(), thirdToken, selfInfo.getName(), selfInfo.getAvatar(), requestCode);
        }

    }

    /**
     * 启动拆红包dialog
     *
     * @param activity          context
     * @param sessionTypeEnum   会话类型
     * @param briberyId         红包id
     * @param bean              红包状态信息
     */
    public static void startOpenRpDialog(final Activity activity, final SessionTypeEnum sessionTypeEnum, final String briberyId, IMMessage message, final NIMOpenRpCallback cb, final RedPackOtherDataBean bean) {
        if (!checkValid()) {
            return;
        }
        GrabRpCallBack callBack = new GrabRpCallBack() {
            @Override
            public void grabRpResult(GrabRpBean grabRpBean) {
                if (grabRpBean.isHadGrabRp()) {
                    cb.sendMessage(selfInfo.getAccount(), briberyId, grabRpBean.getHasLeft() == 0);
                }
            }
        };
        Map<String, Object> remoteExtension = message.getRemoteExtension();

//        String senderId = (String) remoteExtension.get(REDPACKET_SENDER);
//        UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(senderId);
//
//        SenderUserInfoBean senderUserInfoBean = new SenderUserInfoBean();
//        senderUserInfoBean.setAvatar(userInfo.getAvatar());
//        senderUserInfoBean.setUserID(userInfo.getAccount());
//        senderUserInfoBean.setUserName(userInfo.getName());

        String senderId = (String) remoteExtension.get(REDPACKET_SENDERID);
        SenderUserInfoBean senderUserInfoBean = new SenderUserInfoBean();
        if (StringUtil.isNotEmpty(senderId)){
            UserInfo senderUserInfo = NimUIKit.getUserInfoProvider().getUserInfo(senderId);
            senderUserInfoBean.setAvatar(senderUserInfo.getAvatar());
            senderUserInfoBean.setUserID(senderUserInfo.getAccount());
            senderUserInfoBean.setUserName(senderUserInfo.getName());
        }else {
            Map<String, String> senderMap = (Map<String, String>) remoteExtension.get(REDPACKET_SENDER);
            senderUserInfoBean.setAvatar(senderMap.get("avatar"));
            senderUserInfoBean.setUserID(senderMap.get("userID"));
            senderUserInfoBean.setUserName(senderMap.get("userName"));
        }

        if (bean.getStatus() == 3 || bean.getStatus() == 4){
            //红包已过期 or 已代领
            RedPackDetailsActivity.start(activity,bean,senderUserInfoBean);
        }else if (bean.getStatus() == 2){
            //红包已领取

            if (bean.getRedpacketType() == 2004 || bean.getRedpacketType() == 2005){
                //普通红包 专属红包 当前用户是红包发送者，不显示弹窗
                if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderUserInfoBean.getUserID())){
                    RedPackDetailsActivity.start(activity,bean,senderUserInfoBean);
                    return;
                }
            }

            if(sessionTypeEnum == SessionTypeEnum.Team){
                List<RedPackOtherDataBean.RecordsBean> records = bean.getRecords();
                for (int i = 0; i < records.size(); i++){
                    if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(records.get(i).getPayeeId())){
                        //当前用户已经领取过该红包
                        RedPackDetailsActivity.start(activity,bean,senderUserInfoBean);
                        return;
                    }
                }
                showRedPackDialog(activity,message,bean,sessionTypeEnum,cb);
            }else{
                RedPackDetailsActivity.start(activity,bean,senderUserInfoBean);
            }



        }else if (bean.getStatus() == 1){
            //红包待领取     显示弹窗

            if (bean.getRedpacketType() == 2004 || bean.getRedpacketType() == 2005){
                //普通红包 专属红包 当前用户是红包发送者，不显示弹窗
                if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderUserInfoBean.getUserID())){
                    RedPackDetailsActivity.start(activity,bean,senderUserInfoBean);
                    return;
                }
            }

            List<RedPackOtherDataBean.RecordsBean> records = bean.getRecords();
            for (int i = 0; i < records.size(); i++){
                if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(records.get(i).getPayeeId())){
                    //当前用户已经领取过该红包
                    RedPackDetailsActivity.start(activity,bean,senderUserInfoBean);
                    return;
                }
            }
            showRedPackDialog(activity,message,bean,sessionTypeEnum,cb);
        }

    }

    /**
     * 启动拆红包dialog
     *
     * @param activity          context
     * @param sessionTypeEnum   会话类型
     * @param briberyId         红包id
     * @param bean              红包状态信息
     */
    public static void startOpenRpDialog(final Activity activity, final SessionTypeEnum sessionTypeEnum, final String briberyId, IMMessage message, final NIMOpenRpCallback cb, final RedPacketStateBean bean) {
        if (!checkValid()) {
            return;
        }
        GrabRpCallBack callBack = new GrabRpCallBack() {
            @Override
            public void grabRpResult(GrabRpBean grabRpBean) {
                if (grabRpBean.isHadGrabRp()) {
                    cb.sendMessage(selfInfo.getAccount(), briberyId, grabRpBean.getHasLeft() == 0);
                }
            }
        };

        String senderId = bean.getPayerId();
        SenderUserInfoBean senderUserInfoBean = new SenderUserInfoBean();
        if (StringUtil.isNotEmpty(senderId)){
            UserInfo senderUserInfo = NimUIKit.getUserInfoProvider().getUserInfo(senderId);
            senderUserInfoBean.setAvatar(senderUserInfo.getAvatar());
            senderUserInfoBean.setUserID(senderUserInfo.getAccount());
            senderUserInfoBean.setUserName(senderUserInfo.getName());
        }
        RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
        Map<String, Object> map = new HashMap<>();
        map.put(REDPACKET_SENDERID,bean.getPayerId());
        map.put("SenderUserInfoBean",senderUserInfoBean);
        map.put(REDPACKET_COUNT,bean.getNumber());
        map.put(REDPACKET_ID,bean.getId());
        map.put(REDPACKET_MONEY,bean.getAmount());
        map.put(REDPACKET_TYPESTR,attachment.getRpTitle());
        map.put(REDPACKET_GREETING,attachment.getRpContent());
        Integer redpacketType = null;
        switch (bean.getTargetType()){
            case 1:
                //单人红包
                redpacketType = 2001;
                break;
            case 2:
                //普通红包
                redpacketType = 2004;
                if (null != bean.targetSignMap && bean.targetSignMap.size() > 0){
                    //专属红包
                    redpacketType = 2005;
                    List<String> recrivers = new ArrayList<>();
                    Map<String,RedPacketStateBean.TargetSignMap> targetSignMap = new HashMap<>();
                    if (null != bean.targetSignMap){
                        targetSignMap = bean.targetSignMap;
                    }
                    for (RedPacketStateBean.TargetSignMap signMap : targetSignMap.values()) {
                        recrivers.add(signMap.getUid());
                    }
                    map.put(REDPACKET_RECEIVER,recrivers);
                }
                break;
            case 3:
                //随机红包
                redpacketType = 2003;
                break;
        }
        map.put(REDPACKET_TYPE,redpacketType);
        final RedPackOtherDataBean otherDataBean = new RedPackOtherDataBean();
        otherDataBean.setRedpacketType(redpacketType);
        otherDataBean.setRedContent(attachment.getRpContent());
        otherDataBean.setNumber(bean.getNumber());
        if (null != bean.getPayeeIdList()){
            otherDataBean.setCount(bean.getPayeeIdList().size());
        }
        otherDataBean.setStatus(bean.getStatus());
        otherDataBean.setTotalSum(bean.getAmount());
        otherDataBean.setRedId(bean.getId());
        otherDataBean.setTeamId(bean.getTeamId());
        if (bean.getStatus() == 3 || bean.getStatus() == 4){
            //红包已过期 or 已代领
            RedPackDetailsActivity.start(activity,otherDataBean,senderUserInfoBean);
        }else if (bean.getStatus() == 2){
            //红包已领取

            if (bean.getTargetType() == 2 && null != bean.targetSignMap || bean.targetSignMap.size() > 0){
                //普通红包 专属红包 当前用户是红包发送者，不显示弹窗
                if (NimUIKit.getAccount().equals(senderUserInfoBean.getUserID())){
                    RedPackDetailsActivity.start(activity,otherDataBean,senderUserInfoBean);
                    return;
                }
            }

            if(sessionTypeEnum == SessionTypeEnum.Team){

                if (null != bean.getPayeeIdList() && bean.getPayeeIdList().size() > 0 && bean.getPayeeIdList().contains(NimUIKit.getAccount())){
                    //当前用户已经领取过该红包
                    RedPackDetailsActivity.start(activity,otherDataBean,senderUserInfoBean);
                    return;
                }
                showRedPackDialog(activity,map,otherDataBean,sessionTypeEnum,cb);

            }else{
                RedPackDetailsActivity.start(activity,otherDataBean,senderUserInfoBean);
            }



        }else if (bean.getStatus() == 1){
            //红包待领取     显示弹窗

            if (bean.getTargetType() == 2 && null != bean.targetSignMap || bean.targetSignMap.size() > 0){
                //普通红包 专属红包 当前用户是红包发送者，不显示弹窗
                if (NimUIKit.getAccount().equals(senderUserInfoBean.getUserID())){
                    RedPackDetailsActivity.start(activity,otherDataBean,senderUserInfoBean);
                    return;
                }
            }

            if (null != bean.getPayeeIdList() && bean.getPayeeIdList().size() > 0 && bean.getPayeeIdList().contains(NimUIKit.getAccount())){
                //当前用户已经领取过该红包
                RedPackDetailsActivity.start(activity,otherDataBean,senderUserInfoBean);
                return;
            }
            showRedPackDialog(activity,map,otherDataBean,sessionTypeEnum,cb);
        }

    }


    private static void showRedPackDialog(final Activity activity, IMMessage message, final RedPackOtherDataBean bean, final SessionTypeEnum sessionTypeEnum, final NIMOpenRpCallback cb) {
        Map<String, Object> remoteExtension = message.getRemoteExtension();
        //解析红包数据，并赋值给三个Bean
        AnalyticalRedPackData data = new AnalyticalRedPackData();
        data.analytical(remoteExtension, new RedPackDataCallBack() {
            @Override
            public void getRedPackData(Map<String, Object> map) {
                if (sessionTypeEnum == SessionTypeEnum.Team) {


                    final RedPackDialog redPackDialog = new RedPackDialog(activity,map,bean, cb);
                    new XPopup.Builder(activity)
                            .dismissOnTouchOutside(false)
                            .asCustom(redPackDialog)
                            .show();
//            JrmfRpClient.openGroupRp(activity, selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), briberyId, callBack);
                } else if (sessionTypeEnum == SessionTypeEnum.P2P) {
                    //先判断即将领取红包的用户是否是红包发送者
                    SenderUserInfoBean senderBean = (SenderUserInfoBean) map.get("SenderUserInfoBean");
                    if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderBean.getUserID())){
                        //当前用户是红包发送者
                        RedPackDetailsActivity.start(activity,bean,senderBean);
                    }else {
                        //当前用户不是红包发送者
                        RedPackDialog redPackDialog = new RedPackDialog(activity,map,bean, cb);
                        new XPopup.Builder(activity)
                                .dismissOnTouchOutside(false)
                                .asCustom(redPackDialog)
                                .show();
                    }
//                    JrmfRpClient.openSingleRp(activity, selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), briberyId, callBack);
                }
            }
        });
    }

    private static void showRedPackDialog(final Activity activity, Map<String, Object> buderMap, final RedPackOtherDataBean otherDataBean, final SessionTypeEnum sessionTypeEnum, final NIMOpenRpCallback cb) {
        //解析红包数据，并赋值给三个Bean


        AnalyticalRedPackData data = new AnalyticalRedPackData();
        data.analytical(buderMap, new RedPackDataCallBack() {
            @Override
            public void getRedPackData(Map<String, Object> map) {
                if (sessionTypeEnum == SessionTypeEnum.Team) {


                    final RedPackDialog redPackDialog = new RedPackDialog(activity,map,otherDataBean, cb);
                    new XPopup.Builder(activity)
                            .dismissOnTouchOutside(false)
                            .asCustom(redPackDialog)
                            .show();
//            JrmfRpClient.openGroupRp(activity, selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), briberyId, callBack);
                } else if (sessionTypeEnum == SessionTypeEnum.P2P) {
                    //先判断即将领取红包的用户是否是红包发送者
                    SenderUserInfoBean senderBean = (SenderUserInfoBean) map.get("SenderUserInfoBean");
                    if (SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID).equals(senderBean.getUserID())){
                        //当前用户是红包发送者
                        RedPackDetailsActivity.start(activity,otherDataBean,senderBean);
                    }else {
                        //当前用户不是红包发送者
                        RedPackDialog redPackDialog = new RedPackDialog(activity,map,otherDataBean, cb);
                        new XPopup.Builder(activity)
                                .dismissOnTouchOutside(false)
                                .asCustom(redPackDialog)
                                .show();
                    }
//                    JrmfRpClient.openSingleRp(activity, selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), briberyId, callBack);
                }
            }
        });
    }

    /**
     * 打开红包详情界面
     *
     * @param activity context
     * @param packetId 红包id
     */
    public static void startRpDetailActivity(Activity activity, String packetId) {
        if (checkValid()) {
            JrmfRpClient.openRpDetail(activity, selfInfo.getAccount(), getThirdToken(), packetId, selfInfo.getName(), selfInfo.getAvatar());
        }
    }


    /**
     * 更新个人信息到jrmf
     */
    public static void updateMyInfo() {
        if (init && selfInfo != null) {
            JrmfRpClient.updateUserInfo(selfInfo.getAccount(), getThirdToken(), selfInfo.getName(), selfInfo.getAvatar(), new OkHttpModelCallBack<BaseModel>() {

                @Override
                public void onSuccess(BaseModel baseModel) {
                    LogUtil.ui("update jrmf userInfo success");
                }

                @Override
                public void onFail(String s) {
                    LogUtil.ui("update jrmf userInfo fail" + s);
                }
            });
        }
    }
}

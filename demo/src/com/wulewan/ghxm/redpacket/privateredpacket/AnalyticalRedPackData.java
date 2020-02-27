package com.wulewan.ghxm.redpacket.privateredpacket;

import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wulewan.ghxm.bean.ReceiverUserInfoBean;
import com.wulewan.ghxm.bean.RedPackOtherDataBean;
import com.wulewan.ghxm.bean.SenderUserInfoBean;
import com.wulewan.ghxm.utils.StringUtil;
import com.netease.wulewan.uikit.api.NimUIKit;

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

public class AnalyticalRedPackData {
    /**
     * @RedPackDataCallBack 回调
     * @redpacketType       红包类型
     * @receiverMap         接收者信息
     * @count               红包数量
     * @redId               红包ID
     * @redTitle            红包标题
     * @totalSum            红包金额
     * @redContent          红包内容
     * */
    private ReceiverUserInfoBean receiverBean;
    public void analytical(Map<String, Object> map, RedPackDataCallBack callBack){
        List<ReceiverUserInfoBean> list = new ArrayList<>();

        String senderId = (String) map.get(REDPACKET_SENDERID);
        SenderUserInfoBean senderBean = new SenderUserInfoBean();
        if (StringUtil.isNotEmpty(senderId)){
            UserInfo senderUserInfo = NimUIKit.getUserInfoProvider().getUserInfo(senderId);
            senderBean.setAvatar(senderUserInfo.getAvatar());
            senderBean.setUserID(senderUserInfo.getAccount());
            senderBean.setUserName(senderUserInfo.getName());
        }else {
            Map<String, String> senderMap = (Map<String, String>) map.get(REDPACKET_SENDER);
            senderBean.setAvatar(senderMap.get("avatar"));
            senderBean.setUserID(senderMap.get("userID"));
            senderBean.setUserName(senderMap.get("userName"));
        }


        int count = (int) map.get(REDPACKET_COUNT);
        String redId = (String) map.get(REDPACKET_ID);
        String redTitle = (String) map.get(REDPACKET_TYPESTR);
        String totalSum = (String) map.get(REDPACKET_MONEY);
        String redContent = (String) map.get(REDPACKET_GREETING);
        Integer redpacketType = (Integer) map.get(REDPACKET_TYPE);
        RedPackOtherDataBean otherDataBean = new RedPackOtherDataBean();
        otherDataBean.setNumber(count);
        otherDataBean.setRedContent(redContent);
        otherDataBean.setRedId(redId);
        otherDataBean.setRedpacketType(redpacketType);
        otherDataBean.setRedTitle(redTitle);
        otherDataBean.setTotalSum(totalSum);

        switch (redpacketType){
            case 2001:
            case 2002:
                //单人红包
                break;
            case 2003:
                //随机红包
                break;
            case 2004:
                //普通红包
                break;
            case 2005:
                //专属红包

                List<String> receivers = (List<String>) map.get(REDPACKET_RECEIVER);
                for (int i = 0; i < receivers.size(); i++){
                    receiverBean = new ReceiverUserInfoBean();
                    UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(receivers.get(i));
                    receiverBean.setUserName(userInfo.getName());
                    receiverBean.setUserID(userInfo.getAccount());
                    receiverBean.setAvatar(userInfo.getAvatar());
                    list.add(receiverBean);
                }
                break;
        }
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("SenderUserInfoBean",senderBean);
        objectMap.put("ReceiverUserInfoBean",list);
        objectMap.put("RedPackOtherDataBean",otherDataBean);
        callBack.getRedPackData(objectMap);
    }
}

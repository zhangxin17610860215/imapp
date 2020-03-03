package com.yqbj.ghxm.redpacket.privateredpacket;

import android.app.Activity;
import android.content.Intent;

import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;
import com.netease.yqbj.uikit.api.NimUIKit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class BuildRedPackStructure {
    /**
     * @param redPackType       红包类型    类型的约定值  用来判断是那种类型
     * @param isExclusive       是否是专属红包
     * @param redId             红包ID
     * @param redContent        红包描述语   恭喜发财。。。。
     * @param count             红包数量
     * @param teamId            群ID
     * @param totalSum          红包总金额
     * @param targetIds         指定的红包接收者id（如果是多个人用,分开）
     * */
    public void build(Activity activity,int redPackType, boolean isExclusive, String redId, String redContent,
                                    int count,String teamId,String totalSum,String targetIds){
        Map<String, Object> map = new HashMap<>();
        List<String> receivers = new ArrayList<>();
        Map<String, String> senderMap = new HashMap<>();
        String redTitle = "";
        Integer redpacketType = 2003;
        if (redPackType == Constants.REDPACK_TYPE.TEAM_ORDINARY && isExclusive){
            if (StringUtil.isNotEmpty(targetIds) && targetIds.contains(",")){
                String[] split = targetIds.split(",");
                for (int i = 0; i < split.length; i++){
                    receivers.add(split[i]);
                }
            }else {
                receivers.add(targetIds);
            }
//            UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(receiverUserAccid);
        }else if (redPackType == Constants.REDPACK_TYPE.P2P){
            receivers.add(targetIds);
//            UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(targetIds);
        }
        switch (redPackType){
            case Constants.REDPACK_TYPE.P2P:
                //单人红包
                redTitle = "公会小蜜红包";
                redpacketType = 2001;
                map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_RECEIVER,receivers);
                break;
            case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                if (isExclusive){
                    redTitle = "专属红包";
                    redpacketType = 2005;
                    map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_RECEIVER,receivers);
                }else {
                    redTitle = "普通红包";
                    redpacketType = 2004;
                }
                break;
            case Constants.REDPACK_TYPE.TEAM_RANDOM:
                redTitle = "随机红包";
                redpacketType = 2003;
                break;
        }
        final UserInfo senderUserInfo = NimUIKit.getUserInfoProvider().getUserInfo(SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID));
        senderMap.put("avatar",senderUserInfo.getAvatar());
        senderMap.put("userID",senderUserInfo.getAccount());
        senderMap.put("userName",senderUserInfo.getName());
        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_COUNT,count);
        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_ID,redId);
        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_TYPE,redpacketType);
        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_TYPESTR,redTitle);
        if (StringUtil.isNotEmpty(teamId)){
            map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_GROUPID,teamId);
        }
        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_MONEY,totalSum);
        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_GREETING,redContent);
//        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_SENDER,senderMap);
        map.put(Constants.BUILDREDSTRUCTURE.REDPACKET_SENDERID,NimUIKit.getAccount());
        Intent intent = new Intent();
        intent.putExtra("redId", redId);
        intent.putExtra("redTitle", redTitle);
        intent.putExtra("redContent", redContent);
        intent.putExtra("redData", (Serializable)map);
        activity.setResult(RESULT_OK, intent);
        activity.finish();
    }
}

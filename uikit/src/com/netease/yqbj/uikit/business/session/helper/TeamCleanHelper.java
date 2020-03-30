package com.netease.yqbj.uikit.business.session.helper;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.netease.yqbj.uikit.utils.SPUtils;

import org.json.JSONObject;

import java.util.List;

import static com.netease.yqbj.uikit.api.StatisticsConstants.DURATION;
import static com.netease.yqbj.uikit.api.StatisticsConstants.ISREGULARCLEANMODE;
import static com.netease.yqbj.uikit.api.StatisticsConstants.REGULARCLEARTIME;

public class TeamCleanHelper {
    public static void implementClean(Team team){
        boolean isRegularCleanMode = false;
        try {
            String extensionJsonStr = team.getExtension();
            JSONObject jsonObject = new JSONObject(extensionJsonStr);
            if (jsonObject.has(ISREGULARCLEANMODE)){
                isRegularCleanMode =  jsonObject.getBoolean(ISREGULARCLEANMODE);
            }
            final long carryTime = System.currentTimeMillis();
            long time = 0;
            if (jsonObject.has(REGULARCLEARTIME)){
                time = jsonObject.getLong(REGULARCLEARTIME) * 1000;
            }
            if (isRegularCleanMode){
                //群主设置了定时清理消息   需要删除36小时以内的消息
                deleteChattingHistory(team,isRegularCleanMode,carryTime);
            }

            long nearlyTime = isRegularCleanMode ? (carryTime - DURATION + 1000) : 0;
            SPUtils instance = SPUtils.getInstance(StatisticsConstants.ACCID);
            long loactionTime = instance.getLong(team.getId());
            long cleanTime = (nearlyTime > loactionTime ? nearlyTime : loactionTime) > time - DURATION ? (nearlyTime > loactionTime ? nearlyTime : loactionTime) : time - DURATION;
            instance.put(team.getId(),cleanTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteChattingHistory(Team team, boolean isRegularCleanMode, final long carryTime) {
        //清除该群聊中超过了24小时的消息
//        long startTime = System.currentTimeMillis() - 10000;
        long startTime = -1;
        if (isRegularCleanMode){
            startTime = System.currentTimeMillis() - DURATION;
        }

        int limit = 2147483647;//要查询的最大消息条数     int类型的最大值
        IMMessage msg = MessageBuilder.createEmptyMessage(team.getId(), SessionTypeEnum.Team, 0);
        NIMClient.getService(MsgService.class).queryMessageListExTime(msg,startTime, QueryDirectionEnum.QUERY_NEW, limit).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(final List<IMMessage> imMessages) {
//                NIMClient.getService(MsgService.class).clearChattingHistory(team.getId(), SessionTypeEnum.Team);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (IMMessage imMessage : imMessages){
                            if (carryTime - imMessage.getTime() >= DURATION){
                                NIMClient.getService(MsgService.class).deleteChattingHistory(imMessage);
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });
    }
}

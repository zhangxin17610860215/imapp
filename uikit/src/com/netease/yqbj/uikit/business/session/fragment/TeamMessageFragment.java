package com.netease.yqbj.uikit.business.session.fragment;
import android.text.TextUtils;

import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.yqbj.uikit.business.team.helper.ScreenShotListenManager;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.netease.yqbj.uikit.common.ToastHelper;

import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;

import org.json.JSONObject;

import static com.netease.yqbj.uikit.api.StatisticsConstants.ISSCREENSHOT;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public class TeamMessageFragment extends MessageFragment {

    private Team team;
    private ScreenShotListenManager manager;
    @Override
    public boolean isAllowSendMessage(IMMessage message) {
        if (team == null) {
            team = NimUIKit.getTeamProvider().getTeamById(sessionId);
        }

        if (team == null || !team.isMyTeam()) {
            ToastHelper.showToast(getActivity(), R.string.team_send_message_not_allow);
            return false;
        }

        return super.isAllowSendMessage(message);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public void onResume() {
        super.onResume();
        manager = ScreenShotListenManager.newInstance(getContext());
        manager.startListen();
        manager.setListener(new ScreenShotListenManager.OnScreenShotListener() {
            @Override
            public void onShot(String imagePath) {

                boolean isScreenshot = false;
                try {
                    String extensionJsonStr = team.getExtension();
                    JSONObject jsonObject = new JSONObject(extensionJsonStr);
                    if (jsonObject.has(ISSCREENSHOT)){
                        isScreenshot =  jsonObject.getBoolean(ISSCREENSHOT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isScreenshot){
                    if (!TextUtils.isEmpty(imagePath) && imagePath.contains("storage") && imagePath.contains("emulated")){
                        IMMessage message = MessageBuilder.createTipMessage(team.getId(), SessionTypeEnum.Team);
                        String fromAccount = message.getFromAccount();
                        String alias = "";
                        if (!TextUtils.isEmpty(fromAccount) && !TextUtils.isEmpty(NimUIKit.getAccount())){
                            alias = UserInfoHelper.getUserDisplayName(NimUIKit.getAccount());
                        }
                        String tips = "“" + alias + "”在聊天中截屏了";
                        message.setContent(tips);
                        message.setStatus(MsgStatusEnum.success);
                        CustomMessageConfig config = new CustomMessageConfig();
                        config.enableUnreadCount = true;
                        message.setConfig(config);
//                    NIMClient.getService(MsgService.class).saveMessageToLocal(message, true);//本地写入一条消息，只有自己可以看到
                        sendMessage(message);
                    }
                }

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.stopListen();
    }
}
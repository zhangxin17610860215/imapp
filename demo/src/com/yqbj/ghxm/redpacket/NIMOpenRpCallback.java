package com.yqbj.ghxm.redpacket;


import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.session.module.ModuleProxy;
import com.netease.yqbj.uikit.impl.cache.TeamDataCache;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.session.extension.RedPacketOpenedAttachment;

import java.util.List;


/**
 * 发送领取了红包的消息
 */
public class NIMOpenRpCallback {
    private String sendUserAccount;
    private String sessionId;
    private SessionTypeEnum sessionType;
    private ModuleProxy proxy;

    public NIMOpenRpCallback(String sendUserAccount, String sessionId, SessionTypeEnum sessionType, ModuleProxy proxy) {
        this.sendUserAccount = sendUserAccount;
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.proxy = proxy;
    }

    public void sendMessage(String openAccount, String envelopeId, boolean getDone) {
        if (proxy == null) {
            return;
        }
        if (isAllowSendMessage(openAccount)){
            //是否可以发送消息
            return;
        }
        IMMessage imMessage;
        final NimUserInfo selfInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount());
        if (selfInfo == null) {
            return;
        }
        RedPacketOpenedAttachment redPacketOpenedMessage;
        if (openAccount.equals(sendUserAccount)) {
            redPacketOpenedMessage = RedPacketOpenedAttachment.obtain(selfInfo.getAccount(), selfInfo.getAccount(), envelopeId, getDone);
        } else {
            redPacketOpenedMessage = RedPacketOpenedAttachment.obtain(sendUserAccount, selfInfo.getAccount(), envelopeId, getDone);
        }

        String content = redPacketOpenedMessage.getDesc(sessionType, sessionId);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = true;
        config.enablePush = false;
        config.enableUnreadCount = false;
        imMessage = MessageBuilder.createCustomMessage(sessionId, sessionType, content, redPacketOpenedMessage, config);
        proxy.sendMessage(imMessage);
    }

    private boolean isAllowSendMessage(String openAccount) {
        if (sessionType == SessionTypeEnum.Team) {
            Team team = NimUIKit.getTeamProvider().getTeamById(sessionId);
            if (team == null || !team.isMyTeam()) {
                //群不存在 or 自己不在该群
                return true;
            }

            List<TeamMember> teamMembers = NIMClient.getService(TeamService.class).queryMutedTeamMembers(openAccount);
            if (teamMembers.size() > 0){
                for (TeamMember teamMember : teamMembers){
                    if (NimUIKit.getAccount().equals(teamMember.getAccount())){
                        //自己被禁言
                        return true;
                    }
                }
            }

            if (team.isAllMute()){
                //全群禁言 针对普通成员有效
                TeamMember teamMember = TeamDataCache.getInstance().getTeamMember(sessionId,NimUIKit.getAccount());
                if (teamMember.getType() == TeamMemberType.Normal){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

}

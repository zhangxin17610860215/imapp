package com.yqbj.ghxm.session.action;

import android.app.Activity;
import android.content.Intent;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.session.actions.BaseAction;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.impl.cache.TeamDataCache;
import com.umeng.analytics.MobclickAgent;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.redpacket.NIMRedPacketClient;
import com.yqbj.ghxm.redpacket.wallet.SettingPayPasswordActivity;
import com.yqbj.ghxm.session.extension.RedPacketAttachment;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;

import java.util.List;
import java.util.Map;

import static com.netease.yqbj.uikit.api.StatisticsConstants.PERSONAL_RP_SEND_SUCCESSNUM;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_AVERAGE_RP_SEND_SUCCESSNUM;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_EXCLUSIVE_RP_SEND_SUCCESSNUM;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_RANDOM_RP_SEND_SUCCESSNUM;
import static com.yqbj.ghxm.config.Constants.BUILDREDSTRUCTURE.REDPACKET_TYPE;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WALLET_EXIST;

public class RedPacketAction extends BaseAction {

    public RedPacketAction() {
        super(R.drawable.message_plus_rp_selector, R.string.red_packet);
    }

    private static final int CREATE_GROUP_RED_PACKET = 51;
    private static final int CREATE_SINGLE_RED_PACKET = 10;

    @Override
    public void onClick() {
        final int requestCode;
        if (getContainer().sessionType == SessionTypeEnum.Team) {
            requestCode = makeRequestCode(CREATE_GROUP_RED_PACKET);

            if (isAllowSendRedPack()){
                return;
            }

        } else if (getContainer().sessionType == SessionTypeEnum.P2P) {
            requestCode = makeRequestCode(CREATE_SINGLE_RED_PACKET);

            if (!NIMClient.getService(FriendService.class).isMyFriend(getAccount())){
                ToastHelper.showToast(getActivity(),"对方不是你的好友，无法发红包");
                return;
            }
        } else {
            return;
        }
        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        if (instance.getBoolean(WALLET_EXIST)){
            NIMRedPacketClient.startSendRpActivity(getActivity(), getContainer().sessionType, getAccount(), requestCode);
        }else{
            EasyAlertDialogHelper.showCommonDialog(getActivity(), "钱包账户未创建", "请先创建钱包账户再进行发红包", "确定", "取消", false, new EasyAlertDialogHelper.OnDialogActionListener() {
                @Override
                public void doCancelAction() {

                }

                @Override
                public void doOkAction() {
                    SettingPayPasswordActivity.start(getActivity());
                }
            }).show();
        }

    }

    private boolean isAllowSendRedPack() {
        Team team = NimUIKit.getTeamProvider().getTeamById(getAccount());
        if (team == null || !team.isMyTeam()) {
            ToastHelper.showToast(getActivity(), "您已不在该群，不能发送红包消息");
            return true;
        }

        List<TeamMember> teamMembers = NIMClient.getService(TeamService.class).queryMutedTeamMembers(getAccount());
        if (teamMembers.size() > 0){
            for (TeamMember teamMember : teamMembers){
                if (NimUIKit.getAccount().equals(teamMember.getAccount())){
                    ToastHelper.showToast(getActivity(),"您已被禁言，无法发送红包");
                    return true;
                }
            }
        }

        if (team.isAllMute() && StringUtil.isNotEmpty(getAccount()) && StringUtil.isNotEmpty(NimUIKit.getAccount())){
            TeamMember teamMember = TeamDataCache.getInstance().getTeamMember(getAccount(),NimUIKit.getAccount());
            if (teamMember.getType() == TeamMemberType.Normal){
                ToastHelper.showToast(getActivity(),"全员禁言中，无法发送红包");
                return true;
            }
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        sendRpMessage(data);
    }

    private void sendRpMessage(Intent data) {
//        EnvelopeBean groupRpBean = JrmfRpClient.getEnvelopeInfo(data);
//        if (groupRpBean == null) {
//            return;
//        }
        if (null == data){
            return;
        }
        if (StringUtil.isEmpty(data.getStringExtra("redId"))){
            return;
        }
        if (StringUtil.isEmpty(data.getStringExtra("redTitle"))){
            return;
        }
        if (StringUtil.isEmpty(data.getStringExtra("redContent"))){
            return;
        }
        String redId = data.getStringExtra("redId");
        String redTitle = data.getStringExtra("redTitle");
        String redContent = data.getStringExtra("redContent");
        Map<String,Object> map = (Map<String, Object>) data.getSerializableExtra("redData");
        RedPacketAttachment attachment = new RedPacketAttachment();
        // 红包id，红包信息，红包名称
        attachment.setRpId(redId);
        attachment.setRpContent(redContent);
        attachment.setRpTitle(redTitle);

        String content = getActivity().getString(R.string.rp_push_content);
        // 不存云消息历史记录
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = true;
        map.get(REDPACKET_TYPE);

        //统计各个类型红包发送成功次数
        switch ((Integer) map.get(REDPACKET_TYPE)){
            case 2001:
            case 2002:
                //单人红包
                MobclickAgent.onEvent(getActivity(),PERSONAL_RP_SEND_SUCCESSNUM);
                break;
            case 2003:
                //随机红包
                MobclickAgent.onEvent(getActivity(),TEAM_RANDOM_RP_SEND_SUCCESSNUM);
                break;
            case 2004:
                //普通红包
                MobclickAgent.onEvent(getActivity(),TEAM_AVERAGE_RP_SEND_SUCCESSNUM);
                break;
            case 2005:
                //专属红包
                MobclickAgent.onEvent(getActivity(),TEAM_EXCLUSIVE_RP_SEND_SUCCESSNUM);
                break;
        }

//        IMMessage message = MessageBuilder.createCustomMessage(getAccount(), getSessionType(), content, attachment, config);
//        message.setRemoteExtension(map);
//        sendMessage(message);
    }
}

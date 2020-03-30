package com.yqbj.ghxm.team;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.netease.yqbj.uikit.bean.TeamConfigBean;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.netease.yqbj.uikit.common.CommonUtil;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.main.SplashActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.session.SessionHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2015/9/25.
 */
public class TeamCreateHelper {
    private static final String TAG = TeamCreateHelper.class.getSimpleName();
    private static final int DEFAULT_TEAM_CAPACITY = 200;

    /**
     * 创建讨论组
     */
    public static void createNormalTeam(final Context context, List<String> memberAccounts, final boolean isNeedBack, final RequestCallback<CreateTeamResult> callback) {

        String teamName = "讨论组";

        DialogMaker.showProgressDialog(context, context.getString(com.netease.yqbj.uikit.R.string.empty), true);
        // 创建群
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
        fields.put(TeamFieldEnum.Name, teamName);
        NIMClient.getService(TeamService.class).createTeam(fields, TeamTypeEnum.Normal, "",
                memberAccounts).setCallback(
                new RequestCallback<CreateTeamResult>() {
                    @Override
                    public void onSuccess(CreateTeamResult result) {
                        DialogMaker.dismissProgressDialog();

                        ArrayList<String> failedAccounts = result.getFailedInviteAccounts();
                        if (failedAccounts != null && !failedAccounts.isEmpty()) {
                            TeamHelper.onMemberTeamNumOverrun(failedAccounts, context);
                        } else {
                            ToastHelper.showToast(DemoCache.getContext(), com.netease.yqbj.uikit.R.string.create_team_success);
                        }

                        if (isNeedBack) {
//                            SessionHelper.startTeamSession(context, result.getTeam().getId(), MainActivity.class, null); // 进入创建的群
                            SessionHelper.startTeamSession(context, result.getTeam().getId(), SplashActivity.class, null); // 进入创建的群
                        } else {
                            SessionHelper.startTeamSession(context, result.getTeam().getId());
                        }
                        if (callback != null) {
                            callback.onSuccess(result);
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == ResponseCode.RES_TEAM_ECOUNT_LIMIT) {
                            String tip = context.getString(com.netease.yqbj.uikit.R.string.over_team_member_capacity, DEFAULT_TEAM_CAPACITY);
                            ToastHelper.showToast(DemoCache.getContext(), tip);
                        } else {
                            ToastHelper.showToast(DemoCache.getContext(), com.netease.yqbj.uikit.R.string.create_team_failed);
                        }

                        Log.e(TAG, "create team error: " + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                }
        );
    }


    /**
     * 创建高级群
     */
    public static void createAdvancedTeam(final Context context, List<String> memberAccounts) {

        createAdvancedTeam(context,memberAccounts,null);
    }

    /**
     * 创建高级群
     */
    public static void createAdvancedTeam(final Context context, List<String> memberAccounts,RequestCallback requestCallback) {
        Log.e("创建高级群",memberAccounts.size()+"");

        String teamName = "";
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < memberAccounts.size(); i ++){
            buffer.append(UserInfoHelper.getUserDisplayName(memberAccounts.get(i)));
            if (i < memberAccounts.size()){
                buffer.append(",");
            }
        }
        teamName = buffer.toString();
        if (requestCallback == null)
        {
            requestCallback =  new RequestCallback<CreateTeamResult>() {
                @Override
                public void onSuccess(CreateTeamResult result) {
                    Log.e(TAG, "create team success, team id =" + result.getTeam().getId() + ", now begin to update property...");
                    onCreateSuccess(context, result);
                }

                @Override
                public void onFailed(int code) {
                    DialogMaker.dismissProgressDialog();
                    String tip;
                    if (code == ResponseCode.RES_TEAM_ECOUNT_LIMIT) {
                        tip = context.getString(com.netease.yqbj.uikit.R.string.over_team_member_capacity,
                                DEFAULT_TEAM_CAPACITY);
                    } else if (code == ResponseCode.RES_TEAM_LIMIT) {
                        tip = context.getString(com.netease.yqbj.uikit.R.string.over_team_capacity);
                    } else {
                        tip = context.getString(com.netease.yqbj.uikit.R.string.create_team_failed) + ", code=" + code;
                    }

                    ToastHelper.showToast(context, tip);

                    Log.e(TAG, "create team error: " + code);
                }

                @Override
                public void onException(Throwable exception) {
                    Log.e("创群失败",exception.getMessage());
                    DialogMaker.dismissProgressDialog();
                }
            };
        }

        DialogMaker.showProgressDialog(context, context.getString(com.netease.yqbj.uikit.R.string.empty), true);
        // 创建群
        TeamTypeEnum type = TeamTypeEnum.Advanced;
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<>();
        fields.put(TeamFieldEnum.Name, teamName);
        fields.put(TeamFieldEnum.BeInviteMode,TeamBeInviteModeEnum.NoAuth);
        fields.put(TeamFieldEnum.InviteMode,TeamInviteModeEnum.All);
        fields.put(TeamFieldEnum.VerifyType,VerifyTypeEnum.Free);

        NIMClient.getService(TeamService.class).createTeam(fields, type, "",
                memberAccounts).setCallback(requestCallback);
    }

    /**
     * 群创建成功回调
     */
    private static void onCreateSuccess(final Context context, CreateTeamResult result) {
        if (result == null) {
            Log.e(TAG, "onCreateSuccess exception: team is null");
            return;
        }
        final Team team = result.getTeam();
        if (team == null) {
            Log.e(TAG, "onCreateSuccess exception: team is null");
            return;
        }

        getTeamConfig(context,team.getId());

        CommonUtil.uploadTeamIcon(team.getId(),context);

        Log.i(TAG, "create and update team success");

        DialogMaker.dismissProgressDialog();
        // 检查有没有邀请失败的成员
        ArrayList<String> failedAccounts = result.getFailedInviteAccounts();
        if (failedAccounts != null && !failedAccounts.isEmpty()) {
            TeamHelper.onMemberTeamNumOverrun(failedAccounts, context);
        } else {
            ToastHelper.showToast(DemoCache.getContext(), com.netease.yqbj.uikit.R.string.create_team_success);
        }





        // 演示：向群里插入一条Tip消息，使得该群能立即出现在最近联系人列表（会话列表）中，满足部分开发者需求
        Map<String, Object> content = new HashMap<>(1);
        content.put("content", "成功创建高级群");
        IMMessage msg = MessageBuilder.createTipMessage(team.getId(), SessionTypeEnum.Team);
        msg.setRemoteExtension(content);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false;
        msg.setConfig(config);
        msg.setStatus(MsgStatusEnum.success);
        NIMClient.getService(MsgService.class).saveMessageToLocal(msg, true);

        // 发送后，稍作延时后跳转
        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                SessionHelper.startTeamSession(context, team.getId()); // 进入创建的群
            }
        }, 50);
    }

    /**
     * 获取群配置
     * */
    private static void getTeamConfig(final Context context, String teamId) {
        UserApi.teamConfigGet(teamId, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    StatisticsConstants.TEAMCONFIGBEAN = (TeamConfigBean) object;
                }
            }

            @Override
            public void onFailed(String errMessage) {

            }
        });
    }

}

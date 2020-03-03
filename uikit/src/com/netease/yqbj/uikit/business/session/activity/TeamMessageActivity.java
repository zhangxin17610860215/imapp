package com.netease.yqbj.uikit.business.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.api.model.contact.ContactChangedObserver;
import com.netease.yqbj.uikit.api.model.session.SessionCustomization;
import com.netease.yqbj.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.yqbj.uikit.api.model.team.TeamMemberDataChangedObserver;
import com.netease.yqbj.uikit.bean.TeamConfigBean;
import com.netease.yqbj.uikit.business.session.constant.Extras;
import com.netease.yqbj.uikit.business.session.fragment.MessageFragment;
import com.netease.yqbj.uikit.business.session.fragment.TeamMessageFragment;
import com.netease.yqbj.uikit.common.ModuleUIComFn;
import com.netease.yqbj.uikit.common.ToastHelper;

import java.util.List;

import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAMCONFIGBEAN;

/**
 * 群聊界面
 * <p/>
 * Created by huangjun on 2015/3/5.
 */
public class TeamMessageActivity extends BaseMessageActivity {

    // model
    private Team team;

    private View invalidTeamTipView;

    private TextView invalidTeamTipText;

    private TeamMessageFragment fragment;

    private Class<? extends Activity> backToClass;

    public static void start(Context context, String tid, SessionCustomization customization,
                             Class<? extends Activity> backToClass, IMMessage anchor) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, tid);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        intent.putExtra(Extras.EXTRA_BACK_TO_CLASS, backToClass);
        if (anchor != null) {
            intent.putExtra(Extras.EXTRA_ANCHOR, anchor);
        }
        intent.setClass(context, TeamMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    protected void findViews() {
        invalidTeamTipView = findView(R.id.invalid_team_tip);
        invalidTeamTipText = findView(R.id.invalid_team_text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backToClass = (Class<? extends Activity>) getIntent().getSerializableExtra(Extras.EXTRA_BACK_TO_CLASS);
        findViews();

        registerTeamUpdateObserver(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TEAMCONFIGBEAN = null;
        registerTeamUpdateObserver(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestTeamInfo();
//        getTeamConfig();
    }

    /**
     * 获取群配置
     * */
    private void getTeamConfig() {
        ModuleUIComFn.getInstance().getTeamConfig(TeamMessageActivity.this, sessionId, new ModuleUIComFn.GetTeamConfigCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == 200){
                    StatisticsConstants.TEAMCONFIGBEAN = (TeamConfigBean) object;
                }else {
                    StatisticsConstants.TEAMCONFIGBEAN = new TeamConfigBean();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                StatisticsConstants.TEAMCONFIGBEAN = new TeamConfigBean();
            }
        });
    }

    /**
     * 请求群基本信息
     */
    private void requestTeamInfo() {
        // 请求群基本信息
        Team t = NimUIKit.getTeamProvider().getTeamById(sessionId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(sessionId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    } else {
                        onRequestTeamInfoFailed();
                    }
                }
            });
        }
    }

    private void onRequestTeamInfoFailed() {
        ToastHelper.showToast(TeamMessageActivity.this, "获取群组信息失败!");
        finish();
    }

    /**
     * R
     * 更新群信息
     *
     * @param d
     */
    private void updateTeamInfo(final Team d) {
        if (d == null) {
            return;
        }

        team = d;
        fragment.setTeam(team);
        onInitSetTitle(this, team == null ? sessionId : team.getName() + "(" + team.getMemberCount() + "人)");
//        setTitle(team == null ? sessionId : team.getName() + "(" + team.getMemberCount() + "人)");

        invalidTeamTipText.setText(team.getType() == TeamTypeEnum.Normal ? R.string.normal_team_invalid_tip : R.string.team_invalid_tip);
        invalidTeamTipView.setVisibility(team.isMyTeam() ? View.GONE : View.VISIBLE);
    }

    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerTeamUpdateObserver(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataChangedObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberDataChangedObserver, register);
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register);
    }

    /**
     * 群资料变动通知和移除群的通知（包括自己退群和群被解散）
     */
    TeamDataChangedObserver teamDataChangedObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            if (team == null) {
                return;
            }
            for (Team t : teams) {
                if (t.getId().equals(team.getId())) {
                    updateTeamInfo(t);
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team == null) {
                return;
            }
            if (team.getId().equals(TeamMessageActivity.this.team.getId())) {
                updateTeamInfo(team);
            }
        }
    };

    /**
     * 群成员资料变动通知和移除群成员通知
     */
    TeamMemberDataChangedObserver teamMemberDataChangedObserver = new TeamMemberDataChangedObserver() {

        @Override
        public void onUpdateTeamMember(List<TeamMember> members) {
            fragment.refreshMessageList();
        }

        @Override
        public void onRemoveTeamMember(List<TeamMember> member) {
        }
    };

    ContactChangedObserver friendDataChangedObserver = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
            fragment.refreshMessageList();
        }

        @Override
        public void onDeletedFriends(List<String> accounts) {
            fragment.refreshMessageList();
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            fragment.refreshMessageList();
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            fragment.refreshMessageList();
        }
    };

    @Override
    protected MessageFragment fragment() {
        // 添加fragment
        Bundle arguments = getIntent().getExtras();
        arguments.putSerializable(Extras.EXTRA_TYPE, SessionTypeEnum.Team);
        fragment = new TeamMessageFragment();
        fragment.setArguments(arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        return fragment;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.nim_team_message_activity;
    }

    @Override
    protected void initToolBar() {
//        ToolBarOptions options = new NimToolBarOptions();
//        options.titleString = "群聊";
//        setToolBar(R.id.toolbar, options);
        onInitSetBack(this);
        onInitSetTitle(this, "群聊");
        onInitRightSure(this, R.drawable.chat_more_icon, "", 0);
    }

    @Override
    public void setRightSureClick() {
        comRightSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModuleUIComFn.getInstance().toGroupDetailsClick(TeamMessageActivity.this, sessionId);
            }
        });
    }

    @Override
    protected boolean enableSensor() {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (backToClass != null) {
            Intent intent = new Intent();
            intent.setClass(this, backToClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }
}

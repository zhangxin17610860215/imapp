package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.session.SessionHelper;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;

/**
 * 申请加入群组界面
 * Created by hzxuwen on 2015/3/20.
 */
public class AdvancedTeamJoinActivity extends UI implements View.OnClickListener {

    private static final String EXTRA_ID = "EXTRA_ID";

    private final Context teamContext = AdvancedTeamJoinActivity.this;

    private String teamId;
    private Team team;

    private TextView teamNameText;
    private TextView memberCountText;
    private TextView teamTypeText;
    private Button applyJoinButton;

    public static void start(Context context, String teamId) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, teamId);
        intent.setClass(context, AdvancedTeamJoinActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nim_advanced_team_join_activity);

        onInitSetBack(teamContext);
        onInitSetTitle(teamContext, getString(R.string.team_join));

        findViews();
        parseIntentData();
        requestTeamInfo();
    }

    private void findViews() {
        teamNameText = (TextView) findViewById(R.id.team_name);
        memberCountText = (TextView) findViewById(R.id.member_count);
        applyJoinButton = (Button) findViewById(R.id.apply_join);
        teamTypeText = (TextView) findViewById(R.id.team_type);
        applyJoinButton.setOnClickListener(this);
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void requestTeamInfo() {
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);

        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    }
                }
            });
        }
    }

    /**
     * 更新群信息
     *
     * @param t 群
     */
    private void updateTeamInfo(final Team t) {
        if (t == null) {
            ToastHelper.showToast(AdvancedTeamJoinActivity.this, R.string.team_not_exist);
            finish();
        } else {
            team = t;
            teamNameText.setText(team.getName());
            memberCountText.setText(team.getMemberCount() + "人");
            if (team.getType() == TeamTypeEnum.Advanced) {
                teamTypeText.setText(R.string.advanced_team);
            } else {
                teamTypeText.setText(R.string.normal_team);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (team != null) {
            NIMClient.getService(TeamService.class).applyJoinTeam(team.getId(), null).setCallback(new RequestCallback<Team>() {
                @Override
                public void onSuccess(Team team) {
                    applyJoinButton.setEnabled(false);
                    String toast = getString(R.string.team_join_success, team.getName());
                    ToastHelper.showToast(AdvancedTeamJoinActivity.this, toast);
                    SessionHelper.startTeamSession(AdvancedTeamJoinActivity.this, team.getId()); // 进入群
                    finish();
                }

                @Override
                public void onFailed(int code) {
                    //仅仅是申请成功
                    if (code == ResponseCode.RES_TEAM_APPLY_SUCCESS) {
                        applyJoinButton.setEnabled(false);
                        ToastHelper.showToast(AdvancedTeamJoinActivity.this, R.string.team_apply_to_join_send_success);
                    } else if (code == ResponseCode.RES_TEAM_ALREADY_IN) {
                        applyJoinButton.setEnabled(false);
                        ToastHelper.showToast(AdvancedTeamJoinActivity.this, R.string.has_exist_in_team);
                    } else if (code == ResponseCode.RES_TEAM_LIMIT) {
                        applyJoinButton.setEnabled(false);
                        ToastHelper.showToast(AdvancedTeamJoinActivity.this, R.string.team_num_limit);
                    } else {
                        ToastHelper.showToast(AdvancedTeamJoinActivity.this, "failed, error code =" + code);
                    }
                    finish();
                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }
    }
}

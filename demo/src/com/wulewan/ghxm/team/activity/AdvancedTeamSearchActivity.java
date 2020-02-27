package com.wulewan.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.netease.wulewan.uikit.common.ToastHelper;

import com.netease.wulewan.uikit.common.activity.ToolBarOptions;
import com.netease.wulewan.uikit.common.activity.UIDragLess;
import com.netease.wulewan.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.wulewan.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.wulewan.ghxm.R;

/**
 * 搜索加入群组界面
 * Created by hzxuwen on 2015/3/20.
 */
public class AdvancedTeamSearchActivity extends UIDragLess {

    private ClearableEditTextWithIcon searchEditText;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, AdvancedTeamSearchActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_search_activity);
        setTitle(R.string.search_join_team);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.search_join_team;
        setToolBar(R.id.toolbar, options);

        findViews();
        initActionbar();
    }

    private void findViews() {
        searchEditText = findViewById(R.id.team_search_edittext);
        searchEditText.setDeleteImage(R.drawable.nim_grey_delete_icon);
    }

    private void initActionbar() {
        TextView toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setText(R.string.search);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(searchEditText.getText().toString())) {
                    ToastHelper.showToast(AdvancedTeamSearchActivity.this, R.string.not_allow_empty);
                } else {
                    queryTeamById();
                }
            }
        });
    }

    private void queryTeamById() {
        NIMClient.getService(TeamService.class).searchTeam(searchEditText.getText().toString()).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team team) {
                updateTeamInfo(team);
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_TEAM_ENOTEXIST) {
                    ToastHelper.showToast(AdvancedTeamSearchActivity.this, R.string.team_number_not_exist);
                } else {
                    ToastHelper.showToast(AdvancedTeamSearchActivity.this, "search team failed: " + code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                ToastHelper.showToast(AdvancedTeamSearchActivity.this, "search team exception：" + exception.getMessage());
            }
        });
    }

    /**
     * 搜索群组成功的回调
     *
     * @param team 群
     */
    private void updateTeamInfo(Team team) {
        if (team.getId().equals(searchEditText.getText().toString())) {
            AdvancedTeamJoinActivity.start(AdvancedTeamSearchActivity.this, team.getId());
        }

    }
}

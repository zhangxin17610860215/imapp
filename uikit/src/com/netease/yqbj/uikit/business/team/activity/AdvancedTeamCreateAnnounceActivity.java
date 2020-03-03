package com.netease.yqbj.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.LinearLayout;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.api.model.user.IUserInfoProvider;
import com.netease.yqbj.uikit.business.team.helper.AnnouncementHelper;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.yqbj.uikit.common.util.sys.NetworkUtil;
import com.umeng.analytics.MobclickAgent;

import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_TEAMANNOUNCEMENTSUCCESS;

/**
 * 创建群公告界面
 * Created by hzxuwen on 2015/3/18.
 */
public class AdvancedTeamCreateAnnounceActivity extends UI {

    // constant
    private final static String EXTRA_TID = "EXTRA_TID";
    private final static String EXTRA_CONTENT = "EXTRA_CONTENT";

    // data
    private String teamId;
    private String announce;

    // view
//    private EditText teamAnnounceTitle;
    private ClearableEditTextWithIcon teamAnnounceContent;

    private LinearLayout llEditContent;

    public static void startActivityForResult(Activity activity, String teamId, int requestCode, String beforContent) {
        Intent intent = new Intent();
        intent.setClass(activity, AdvancedTeamCreateAnnounceActivity.class);
        intent.putExtra(EXTRA_TID, teamId);
        intent.putExtra(EXTRA_CONTENT, beforContent);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_create_announce);

//        ToolBarOptions options = new NimToolBarOptions();
//        options.titleId = R.string.team_annourcement;
//        setToolBar(R.id.toolbar, options);
        onInitSetBack(this);
        onInitSetTitle(this, getString(R.string.team_annourcement));
        onInitRightSure(this, 0, "完成", 0);


        parseIntentData();
        findViews();

    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_TID);
    }

    private void findViews() {
//        teamAnnounceTitle = (EditText) findViewById(R.id.team_announce_title);
        teamAnnounceContent = findView(R.id.team_announce_content);
        teamAnnounceContent.setText(getIntent().getStringExtra(EXTRA_CONTENT));
//        teamAnnounceTitle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        teamAnnounceContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1024)});

        llEditContent = findView(R.id.ll_edit_content);
        llEditContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showKeyboard(true);
            }
        });
    }

    @Override
    public void setRightSureClick() {
        comRightSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAnnounceData();
            }
        });
    }

    private void requestAnnounceData() {
        if (!NetworkUtil.isNetAvailable(this)) {
            ToastHelper.showToast(this, R.string.network_is_not_available);
            return;
        }

//        if (TextUtils.isEmpty(teamAnnounceTitle.getText().toString())) {
//            ToastHelper.showToast(AdvancedTeamCreateAnnounceActivity.this, R.string.team_announce_notice);
//            return;
//        }

        comRightSure.setEnabled(false);
        // 请求群信息
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateTeamData(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamData(result);
                    } else {
                        comRightSure.setEnabled(true);
                    }
                }
            });
        }
    }

    /**
     * 获得最新公告内容
     *
     * @param team 群
     */
    private void updateTeamData(Team team) {
        if (team == null) {
            ToastHelper.showToast(this, getString(R.string.team_not_exist));
            showKeyboard(false);
            finish();
        } else {
            announce = team.getAnnouncement();
            updateAnnounce();
        }
    }

    /**
     * 创建公告更新到服务器
     */
    private void updateAnnounce() {
//        String announcement = AnnouncementHelper.makeAnnounceJson(announce, teamAnnounceTitle.getText().toString(),
//                teamAnnounceContent.getText().toString());
        IUserInfoProvider iUserInfoProvider = NimUIKit.getUserInfoProvider();
        UserInfo userInfo = iUserInfoProvider.getUserInfo(NimUIKit.getAccount());
        String announcement = AnnouncementHelper.makeAnnounceJson("", userInfo.getName(),
                teamAnnounceContent.getText().toString());
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.Announcement, announcement).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                MobclickAgent.onEvent(AdvancedTeamCreateAnnounceActivity.this,TEAM_MANAGER_TEAMANNOUNCEMENTSUCCESS);
                setResult(Activity.RESULT_OK);
                showKeyboard(false);
                finish();
                ToastHelper.showToast(AdvancedTeamCreateAnnounceActivity.this, R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                comRightSure.setEnabled(true);
                ToastHelper.showToast(AdvancedTeamCreateAnnounceActivity.this, String.format(getString(R.string.update_failed), code));
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
                comRightSure.setEnabled(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        showKeyboard(false);
        super.onBackPressed();
    }

}

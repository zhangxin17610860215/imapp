package com.netease.yqbj.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.yqbj.uikit.business.team.helper.AnnouncementHelper;
import com.netease.yqbj.uikit.business.team.model.Announcement;
import com.netease.yqbj.uikit.business.team.viewholder.TeamAnnounceHolder;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.adapter.TAdapter;
import com.netease.yqbj.uikit.common.adapter.TAdapterDelegate;
import com.netease.yqbj.uikit.common.adapter.TViewHolder;
import com.netease.yqbj.uikit.common.ui.listview.ListViewUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 群公告列表
 * Created by hzxuwen on 2015/3/18.
 */
public class AdvancedTeamAnnounceActivity extends UI implements TAdapterDelegate {
    // constant
    private final static String EXTRA_TID = "EXTRA_TID";
    private final static String EXTRA_AID = "EXTRA_AID";
    private final static int RES_ANNOUNCE_CREATE_CODE = 0x10;
    public final static String RESULT_ANNOUNCE_DATA = "RESULT_ANNOUNCE_DATA";

    // context
    private Handler uiHandler;

    // data
    private String teamId;
    private String announceId;
    private String announce;

    // view
    private LinearLayout announceTips;
    private ListView announceListView;
    private TAdapter mAdapter;
    private List<Announcement> items;

    private boolean isMember = false;
    private boolean isResult = false;

    public static void start(Activity activity, String teamId) {
        start(activity, teamId, null);
    }

    public static void start(Activity activity, String teamId, String announceId) {
        Intent intent = new Intent();
        intent.setClass(activity, AdvancedTeamAnnounceActivity.class);
        intent.putExtra(EXTRA_TID, teamId);
        if (announceId != null) {
            intent.putExtra(EXTRA_AID, announceId);
        }
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_announce);

        onInitSetBack(this);
        onInitSetTitle(this, getString(R.string.team_annourcement));
        onInitRightSure(this, 0, getString(R.string.edit_text), 0);

        uiHandler = new Handler(getMainLooper());

        parseIntentData();
        findViews();
        initActionbar();
        initAdapter();
        requestTeamData();
        requestMemberData();
    }

    /**
     * ************************ TAdapterDelegate **************************
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return TeamAnnounceHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    /**
     * ******************************初始化*******************************
     */

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_TID);
        announceId = getIntent().getStringExtra(EXTRA_AID);
    }

    private void findViews() {
        announceListView = findView(R.id.team_announce_listview);
        announceTips = findView(R.id.ll_nodata);
        TextView nullContent = findView(R.id.tv_noData_content);
        nullContent.setText(getString(R.string.no_announce));
        comRightSure.setVisibility(View.INVISIBLE);
    }

    private void initActionbar() {
        comRightSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sContent = "";
                if (items.size() > 0) sContent = items.get(0).getContent();
                AdvancedTeamCreateAnnounceActivity.startActivityForResult(AdvancedTeamAnnounceActivity.this, teamId, RES_ANNOUNCE_CREATE_CODE, sContent);
            }
        });
    }

    private void initAdapter() {
        items = new ArrayList<>();
        mAdapter = new TAdapter(this, items, this);
        announceListView.setAdapter(mAdapter);
    }

    private void requestTeamData() {
        // 请求群信息
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateAnnounceInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateAnnounceInfo(result);
                    }
                }
            });
        }
    }

    private void requestMemberData() {
        TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
        if (teamMember.getType() == TeamMemberType.Manager || teamMember.getType() == TeamMemberType.Owner) {
            comRightSure.setVisibility(View.VISIBLE);
        } else {
            comRightSure.setVisibility(View.INVISIBLE);
            ToastHelper.showToast(this, "只有群主与管理员才能发布公告");
        }

        if (teamMember != null) {
            updateTeamMember(teamMember);
        } else {
            // 请求群成员
            NimUIKit.getTeamProvider().fetchTeamMember(teamId, NimUIKit.getAccount(), new SimpleCallback<TeamMember>() {
                @Override
                public void onResult(boolean success, TeamMember member, int code) {
                    if (success && member != null) {
                        updateTeamMember(member);
                    }
                }
            });
        }
    }

    /**
     * 更新公告信息
     *
     * @param team 群
     */
    private void updateAnnounceInfo(Team team) {
        if (team == null) {
            ToastHelper.showToast(this, getString(R.string.team_not_exist));
            finish();
        } else {
            announce = team.getAnnouncement();
            setAnnounceItem();
        }
    }

    /**
     * 判断是否是普通成员
     *
     * @param teamMember 群成员
     */
    private void updateTeamMember(TeamMember teamMember) {
        if (teamMember.getType() == TeamMemberType.Normal) {
            isMember = true;
        }
    }

    /**
     * 设置公告
     */
    private void setAnnounceItem() {
        if (TextUtils.isEmpty(announce)) {
            announceTips.setVisibility(View.VISIBLE);
            return;
        } else {
            announceTips.setVisibility(View.GONE);
        }

        List<Announcement> list = AnnouncementHelper.getAnnouncements(teamId, announce, isMember ? 5 : Integer.MAX_VALUE);
        if (list == null || list.isEmpty()) {
            return;
        }
        list = Collections.singletonList(list.get(0));

        items.clear();
        items.addAll(list);
        if (isResult){
            sendMessage(items.get(0));
        }
        mAdapter.notifyDataSetChanged();

        jumpToIndex(list);
    }

    /**
     * 跳转到选中的公告
     *
     * @param list 群公告列表
     */
    private void jumpToIndex(List<Announcement> list) {
        if (TextUtils.isEmpty(announceId)) {
            return;
        }

        int jumpIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(announceId)) {
                jumpIndex = i;
                break;
            }
        }

        if (jumpIndex >= 0) {
            final int position = jumpIndex;
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ListViewUtil.scrollToPosition(announceListView, position, 0);
                }
            }, 200);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RES_ANNOUNCE_CREATE_CODE:
                    announceId = null;
                    items.clear();
                    isResult = true;
                    requestTeamData();
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * 发送@所有人消息
     * @param announcement
     */
    private void sendMessage(Announcement announcement) {
        IMMessage message = MessageBuilder.createTextMessage(teamId, SessionTypeEnum.Team, "@所有人 " + announcement.getContent());
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = true;
        message.setConfig(config);
        message.setStatus(MsgStatusEnum.success);
        MessageListPanelHelper.getInstance().notifyAddMessage(message); // 界面上add一条
        NIMClient.getService(MsgService.class).sendMessage(message, false);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_ANNOUNCE_DATA, announce);
        setResult(Activity.RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }
}

package com.yqbj.ghxm.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.business.recent.RecentContactsFragment;
import com.netease.yqbj.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.netease.yqbj.uikit.common.CommonUtil;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.ui.widget.SwitchButton;
import com.netease.yqbj.uikit.common.util.sys.NetworkUtil;
import com.netease.yqbj.uikit.utils.SPUtils;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.contact.activity.UserProfileActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;

import java.util.ArrayList;

/**
 * Created by hzxuwen on 2015/10/13.
 */
public class MessageInfoActivity extends BaseAct {
    private final static String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
    private static final int REQUEST_CODE_ADVANCE = 1;

    private static final int MSG_TIPS = 1; //消息提醒
    private static final int MSG_TOP = 2; //聊天置顶
//    private static final int MSG_DEL = 3;//清楚聊天记录

    private final Context CONTEXT = MessageInfoActivity.this;

    // data
    private String account;
    // view
    private SwitchButton switchButton;

    private SwitchButton switchMsgTop;

    public static void startActivity(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, MessageInfoActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_info_activity);

//        ToolBarOptions options = new NimToolBarOptions();
//        options.titleId = R.string.message_info;
//        options.navigateId = R.drawable.actionbar_dark_back_icon;
//        setToolBar(R.id.toolbar, options);
        setToolbar(R.drawable.jrmf_b_top_back, "聊天信息");

        account = getIntent().getStringExtra(EXTRA_ACCOUNT);
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSwitchBtn();
    }

    private void findViews() {
        HeadImageView userHead = (HeadImageView) findViewById(R.id.user_layout).findViewById(R.id.imageViewHeader);
        TextView userName = (TextView) findViewById(R.id.user_layout).findViewById(R.id.textViewName);
        userHead.loadBuddyAvatar(account);
        userName.setText(UserInfoHelper.getUserDisplayName(account));
        userHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserProfile();
            }
        });

        ((TextView) findViewById(R.id.create_team_layout).findViewById(R.id.textViewName)).setText(R.string.create_team);
        HeadImageView addImage = (HeadImageView) findViewById(R.id.create_team_layout).findViewById(R.id.imageViewHeader);
        addImage.setBackgroundResource(com.netease.yqbj.uikit.R.drawable.nim_team_member_add_selector);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTeamMsg();
            }
        });

        ((TextView) findViewById(R.id.toggle_layout).findViewById(R.id.user_profile_title)).setText(R.string.msg_notice);
        switchButton = findViewById(R.id.toggle_layout).findViewById(R.id.user_profile_toggle);
        switchButton.setTag(MSG_TIPS);
        switchButton.setOnChangedListener(onChangedListener);

        ((TextView) findViewById(R.id.toggle_layout_chat_top).findViewById(R.id.user_profile_title)).setText(R.string.recent_sticky);
        switchMsgTop = findViewById(R.id.toggle_layout_chat_top).findViewById(R.id.user_profile_toggle);
        switchMsgTop.setTag(MSG_TOP);
        initMsgTop();
        switchMsgTop.setOnChangedListener(onChangedListener);

        findViewById(R.id.toggle_layout_chat_top).findViewById(R.id.line).setVisibility(View.GONE);

        findViewById(R.id.rl_clear_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyAlertDialogHelper.showCommonDialog(CONTEXT, null, "确定要清除吗？", "确定", "取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        SPUtils instance = SPUtils.getInstance(StatisticsConstants.ACCID);
                        instance.put(account,System.currentTimeMillis());
                        NIMClient.getService(MsgService.class).clearChattingHistory(account, SessionTypeEnum.P2P);
                        MessageListPanelHelper.getInstance().notifyClearMessages(account);
                        ToastHelper.showToast(CONTEXT, "清除成功");
                    }
                }).show();

            }
        });
    }

    private void initMsgTop() {
        if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
            RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(account, SessionTypeEnum.P2P);
            boolean isSticky = recentContact != null && CommonUtil.isTagSet(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
            switchMsgTop.setCheck(isSticky);
        }
    }

    private void updateSwitchBtn() {
        boolean notice = NIMClient.getService(FriendService.class).isNeedMessageNotify(account);
        switchButton.setCheck(notice);
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            switch ((int) v.getTag()) {
                case MSG_TIPS:
                    if (!NetworkUtil.isNetAvailable(MessageInfoActivity.this)) {
                        ToastHelper.showToast(MessageInfoActivity.this, R.string.network_is_not_available);
                        switchButton.setCheck(!checkState);
                        return;
                    }

                    NIMClient.getService(FriendService.class).setMessageNotify(account, checkState).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            if (checkState) {
                                ToastHelper.showToast(MessageInfoActivity.this, "开启消息提醒成功");
                            } else {
                                ToastHelper.showToast(MessageInfoActivity.this, "关闭消息提醒成功");
                            }
                        }

                        @Override
                        public void onFailed(int code) {
                            if (code == 408) {
                                ToastHelper.showToast(MessageInfoActivity.this, R.string.network_is_not_available);
                            } else {
                                ToastHelper.showToast(MessageInfoActivity.this, "on failed:" + code);
                            }
                            switchButton.setCheck(!checkState);
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                    break;

                case MSG_TOP:
                    //查询之前是不是存在会话记录
                    RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(account, SessionTypeEnum.P2P);
                    if (checkState) {//置顶
                        //如果之前不存在，创建一条空的会话记录
                        if (recentContact == null) {
                            // RecentContactsFragment 的 MsgServiceObserve#observeRecentContact 观察者会收到通知
                            NIMClient.getService(MsgService.class).createEmptyRecentContact(account,
                                    SessionTypeEnum.P2P,
                                    RecentContactsFragment.RECENT_TAG_STICKY,
                                    System.currentTimeMillis(),
                                    true);
                        }
                        // 之前存在，更新置顶flag
                        else {
                            CommonUtil.addTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                            NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                        }
                    } else { //取消置顶
                        if (recentContact != null) {
                            CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                            NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                        }
                    }
                    ToastHelper.showToast(CONTEXT, "设置成功");
                    break;
//
//                case MSG_DEL:
//                    NIMClient.getService(MsgService.class).clearChattingHistory(account, SessionTypeEnum.P2P);
//                    break;

            }

        }
    };

    private void openUserProfile() {
        UserProfileActivity.start(this, account);
    }

    /**
     * 创建群聊
     */
    private void createTeamMsg() {
        ArrayList<String> memberAccounts = new ArrayList<>();
        memberAccounts.add(account);
        ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(memberAccounts, 50);
        NimUIKit.startContactSelector(this, option, REQUEST_CODE_ADVANCE);// 创建群
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_ADVANCE) {
                final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
//                    TeamCreateHelper.createAdvancedTeam(MessageInfoActivity.this, selected);
                    createTeam(selected);
                } else {
                    ToastHelper.showToast(DemoCache.getContext(), "请选择至少一个联系人！");
                }
            }
        }
    }

    private void createTeam(ArrayList<String> selected) {
        showProgress(this,false);
        String teamName = "";
        for (int i = 0; i < selected.size(); i++){
            if (i <= 4){
                UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(selected.get(i));
                teamName = teamName + userInfo.getName() + ",";
            }
        }
        UserApi.createTeam(teamName.substring(0,teamName.length()-1),JSON.toJSONString(selected), this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("建群成功");
                    finish();
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }
}

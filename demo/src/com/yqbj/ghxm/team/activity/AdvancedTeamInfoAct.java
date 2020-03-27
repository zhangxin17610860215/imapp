package com.yqbj.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.business.session.actions.PickImageAction;
import com.netease.yqbj.uikit.business.session.constant.Extras;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.netease.yqbj.uikit.common.media.picker.PickImageHelper;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.util.log.LogUtil;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.yqbj.uikit.api.model.user.UserInfoObserver;
import com.netease.yqbj.uikit.bean.TeamConfigBean;
import com.netease.yqbj.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.business.recent.RecentContactsFragment;
import com.netease.yqbj.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.yqbj.uikit.business.team.activity.AdvancedTeamAnnounceActivity;
import com.netease.yqbj.uikit.business.team.activity.AdvancedTeamNicknameActivity;
import com.netease.yqbj.uikit.business.team.activity.TeamPropertySettingActivity;
import com.netease.yqbj.uikit.business.team.adapter.TeamMemberAdapter;
import com.netease.yqbj.uikit.business.team.helper.AnnouncementHelper;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.business.team.model.Announcement;
import com.netease.yqbj.uikit.business.team.ui.TeamInfoGridView;
import com.netease.yqbj.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.yqbj.uikit.common.CommonUtil;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.adapter.TAdapterDelegate;
import com.netease.yqbj.uikit.common.adapter.TViewHolder;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.common.ui.widget.SwitchButton;
import com.netease.yqbj.uikit.utils.SPUtils;
import com.umeng.analytics.MobclickAgent;
import com.yqbj.ghxm.bean.BaseBean;
import com.yqbj.ghxm.bean.MyTeamWalletBean;
import com.yqbj.ghxm.busevent.TeamMemberEvent;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.redpacket.privateredpacket.ChooseRecipientsListACT;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.session.search.SearchMessageActivity;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.TimeUtils;
import com.yqbj.ghxm.zxing.ZXingUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.yqbj.uikit.api.StatisticsConstants.DURATION;
import static com.netease.yqbj.uikit.api.StatisticsConstants.ISREGULARCLEANMODE;
import static com.netease.yqbj.uikit.api.StatisticsConstants.ISSCREENSHOT;
import static com.netease.yqbj.uikit.api.StatisticsConstants.REGULARCLEARTIME;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_DATA;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_DISBANDANSUCCESS;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_MSGTOPPING;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_NODISTURB;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_REGULARCLEANING;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_SCREENCASTNOTIFI;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_TEAMQRCODE;

/**
 * 群信息
 * */
public class AdvancedTeamInfoAct extends BaseAct implements TAdapterDelegate, TeamMemberHolder.TeamMemberHolderEventListener, TeamMemberAdapter.AddMemberCallback, TeamMemberAdapter.RemoveMemberCallback {

    private static final String EXTRA_ID = "EXTRA_ID";
    private static final int REQUEST_PICK_ICON = 102;
    private static final int REQUEST_CODE_CONTACT_SELECT = 103;
    private static final int REQUEST_CODE_CONTACT_MANAGER_SELECT = 104;
    private static final int REQUEST_CODE_CONTACT_SELECT_REMOVE  = 105;

    public static final String RESULT_EXTRA_REASON = "RESULT_EXTRA_REASON";
    public static final String RESULT_EXTRA_REASON_QUIT = "RESULT_EXTRA_REASON_QUIT";
    public static final String RESULT_EXTRA_REASON_DISMISS = "RESULT_EXTRA_REASON_DISMISS";

    // constant
    private static final String TAG = "RegularTeamInfoActivity";
    private static final int TEAM_MEMBERS_SHOW_LIMIT = 20;
    private static final int ICON_TIME_OUT = 30000;

    protected String teamId;
    public TeamInfoGridView gridView;
    protected List<String> memberAccounts;
    protected List<TeamMember> members;
    protected List<TeamMemberAdapter.TeamMemberItem> dataSource;
    protected List<String> managerList;
    protected Team team;
    protected TeamMemberAdapter adapter;
    private AbortableFuture<String> uploadFuture;

    protected UserInfoObserver userInfoObserver;

    // state
    protected boolean isSelfAdmin = false;
    protected boolean isSelfManager = false;

    protected String creator;


    private View layoutTeamName;
    private View headerLayout;
    private View layoutCardName;
    private View layoutQrCode;
    private View layoutBanner;
    private View layoutManager;
    private View layoutMyTeamMiBi;
    private TextView tvMiBiNum;
    private View layoutSetMiBi;
    private View layoutSettlementFailed;
    private View layoutChatHistory;
    private View layoutClearHistory;
    private View layoutNoReceivedRPRecord;
    private View layoutLeadTop;
    private View layoutNeedDisturb;
    private View layoutScreenShot;
    private View layoutMsgClear;

    private Button btn_exit;
    private SwitchButton swiBtn_leadTop;
    private SwitchButton swiBtn_NeedDisturb;
    private SwitchButton swiBtn_ScreenShot;
    private SwitchButton swiBtn_MsgClear;
    private TextView tv_show_all;
    private HeadImageView teamHeadImage;

    private static final String SW_KEY_LEAD_TOP = "sw_lead_top";
    private static final String SW_KEY_NEEDDISTURB = "sw_NeedDisturb";
    private static final String SW_KEY_SCREENSHOT = "sw_ScreenShot";
    private static final String SW_KEY_MSGCLEAR = "sw_MsgClear";

    private Runnable runnable;
    private Thread thread;

    private TeamConfigBean teamConfigBean = new TeamConfigBean();

    public static void start(Context context, String tid) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, AdvancedTeamInfoAct.class);
        context.startActivity(intent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onEvent(this,TEAM_MANAGER_DATA);
        setContentView(R.layout.advance_team_info_act);

        setToolbar(R.drawable.jrmf_b_top_back, "群组信息");

        parseIntentData();
        findViews();
        initAdapter();
        loadTeamInfo();
        requestMembers();
        registerObservers(true);
        EventBus.getDefault().register(this);

    }

    /**
     * 获取群拓展字段
     * */
    private void getExtension() {
//        teamConfigBean = StatisticsConstants.TEAMCONFIGBEAN;
//        if (null == teamConfigBean){
//            swiBtn_ScreenShot.setCheck(false);
//            swiBtn_MsgClear.setCheck(false);
//            return;
//        }
        boolean isScreenshot = false;
        try {
            String extensionJsonStr = team.getExtension();
            if (StringUtil.isNotEmpty(extensionJsonStr)){
                JSONObject jsonObject = new JSONObject(extensionJsonStr);
                if (jsonObject.has(ISSCREENSHOT)){
                    isScreenshot =  jsonObject.getBoolean(ISSCREENSHOT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        swiBtn_ScreenShot.setCheck(isScreenshot);

        boolean isRegularCleanMode = false;
        try {
            String extensionJsonStr = team.getExtension();
            if (StringUtil.isNotEmpty(extensionJsonStr)){
                JSONObject jsonObject = new JSONObject(extensionJsonStr);
                if (jsonObject.has(ISREGULARCLEANMODE)){
                    isRegularCleanMode =  jsonObject.getBoolean(ISREGULARCLEANMODE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        swiBtn_MsgClear.setCheck(isRegularCleanMode);
    }

    /**
     * 获取群配置
     * */
    private void getTeamConfig() {
        UserApi.teamConfigGet(teamId, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    StatisticsConstants.TEAMCONFIGBEAN = (TeamConfigBean) object;
                    teamConfigBean = StatisticsConstants.TEAMCONFIGBEAN;
//                    getExtension();
                }else {
//                    toast((String) object);
//                    getExtension();
                }
            }

            @Override
            public void onFailed(String errMessage) {
//                toast(errMessage);
//                getExtension();
            }
        });
    }

    /**
     * 设置群配置
     * */
    private void setTeamConfig(String teamMemberProtect, String expsecond, String regularClear,String screenCapture) {
//        UserApi.teamConfigSet(teamId, teamMemberProtect,expsecond,regularClear,screenCapture,this, new requestCallback() {
//            @Override
//            public void onSuccess(int code, Object object) {
//                if (code == Constants.SUCCESS_CODE){
//                    toast("设置成功");
//                }else {
//                    toast((String) object);
//                }
//            }
//
//            @Override
//            public void onFailed(String errMessage) {
//                toast(errMessage);
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (null != thread){
            thread = null;
            runnable = null;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        String tid = getIntent().getStringExtra(EXTRA_ID);
        Team t = NimUIKit.getTeamProvider().getTeamById(tid);
        if (t == null) {
            ToastHelper.showToast(this, getString(R.string.team_not_exist));
            finish();
            return;
        } else {
            creator = t.getCreator();
            if (creator.equals(NimUIKit.getAccount())) {
                isSelfAdmin = true;
                btn_exit.setText("解散群聊");
            }else {
                btn_exit.setText("退出群聊");
            }
            getExtension();
        }
        getTeamWalletInfo();
    }

    /**
     * 获取我的群蜜币
     * */
    private void getTeamWalletInfo() {
        showProgress(mActivity,false);
        UserApi.getTeamWalletInfo(teamId, NimUIKit.getAccount(), AdvancedTeamInfoAct.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    MyTeamWalletBean walletBean = (MyTeamWalletBean) object;
                    tvMiBiNum.setHint(walletBean.getScore()+"");
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

    /**
     * 初始化群组基本信息
     */
    protected void loadTeamInfo() {
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    } else {
                        onGetTeamInfoFailed();
                    }
                }
            });
        }
    }

    /**
     * *************************** 加载&变更数据源 ********************************
     */
    protected void requestMembers() {
        NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> members, int code) {
                if (success && members != null && !members.isEmpty()) {
                    updateTeamMember(members);
                }
            }
        });
    }

    /**
     * 更新群成员信息
     *
     * @param m
     */
    protected void updateTeamMember(final List<TeamMember> m) {
        if (m != null && m.isEmpty()) {
            return;
        }
        updateTeamBusinessCard(m);
        addTeamMembers(m, true);
    }


    protected void onGetTeamInfoFailed() {
        ToastHelper.showToast(this, getString(R.string.team_not_exist));
        finish();
    }

    /**
     * 更新群信息
     *
     * @param t
     */
    protected void updateTeamInfo(final Team t) {
        this.team = t;

        if (team == null) {
            ToastHelper.showToast(this, getString(R.string.team_not_exist));
            finish();
            return;
        } else {
            creator = team.getCreator();
            if (creator.equals(NimUIKit.getAccount())) {
                isSelfAdmin = true;
                layoutNoReceivedRPRecord.setVisibility(View.VISIBLE);
            }else {
                layoutNoReceivedRPRecord.setVisibility(View.GONE);
            }

            setTitle(team.getName());
            teamHeadImage.loadTeamIconByTeam(team);
            ((TextView) layoutTeamName.findViewById(R.id.item_detail)).setText(team.getName());
            setAnnouncement(team.getAnnouncement());
            getExtension();

        }

    }

    private void upDateTeamConfigInfo() {
        getTeamConfig();
    }


    protected void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }


    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            final String key = (String) v.getTag();
            if (key.equals(SW_KEY_LEAD_TOP)) {
                //消息置顶
                //查询之前是不是存在会话记录
                MobclickAgent.onEvent(AdvancedTeamInfoAct.this,TEAM_MANAGER_MSGTOPPING);
                RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(teamId, SessionTypeEnum.Team);
                //置顶
                if (checkState) {
                    //如果之前不存在，创建一条空的会话记录
                    if (recentContact == null) {
                        // RecentContactsFragment 的 MsgServiceObserve#observeRecentContact 观察者会收到通知
                        NIMClient.getService(MsgService.class).createEmptyRecentContact(teamId,
                                SessionTypeEnum.Team,
                                RecentContactsFragment.RECENT_TAG_STICKY,
                                System.currentTimeMillis(),
                                true);
                    }
                    // 之前存在，更新置顶flag
                    else {
                        CommonUtil.addTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                }
                //取消置顶
                else {
                    if (recentContact != null) {
                        CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                }

            } else if (key.equals(SW_KEY_NEEDDISTURB)){
                //消息免打扰
                MobclickAgent.onEvent(AdvancedTeamInfoAct.this,TEAM_MANAGER_NODISTURB);
                RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(teamId, SessionTypeEnum.Team);
                TeamMessageNotifyTypeEnum type = null;
                if (checkState) {
                    //免打扰
                    if (recentContact != null){
                        type = TeamMessageNotifyTypeEnum.Mute;
                        CommonUtil.addTag(recentContact, RecentContactsFragment.RECENT_TAG_NEEDDISTURB);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                } else {
                    //取消免打扰
                    if (recentContact != null) {
                        type = TeamMessageNotifyTypeEnum.All;
                        CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_NEEDDISTURB);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                }

                NIMClient.getService(TeamService.class).muteTeam(teamId, type).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {

                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 408) {
                            ToastHelper.showToast(AdvancedTeamInfoAct.this, com.netease.yqbj.uikit.R.string.network_is_not_available);
                        } else {
                            ToastHelper.showToast(AdvancedTeamInfoAct.this, "切换失败");
                        }
                        swiBtn_NeedDisturb.setCheck(!checkState);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });

            }else if (key.equals(SW_KEY_SCREENSHOT)){
                //截屏通知
                MobclickAgent.onEvent(AdvancedTeamInfoAct.this,TEAM_MANAGER_SCREENCASTNOTIFI);
                if (isSelfAdmin) {
                    boolean isScreenshot;
                    try {
                        isScreenshot = checkState;
                        String extensionJsonStr = team.getExtension();
                        JSONObject jsonObject = null;
                        if (StringUtil.isNotEmpty(extensionJsonStr)){
                            jsonObject = new JSONObject(extensionJsonStr);
                        }else {
                            jsonObject = new JSONObject();
                        }
                        jsonObject.put(ISSCREENSHOT,isScreenshot);

                        NIMClient.getService(TeamService.class).updateTeam(teamId,TeamFieldEnum.Extension,jsonObject.toString());

//                        setTeamConfig(teamConfigBean.getProtect() + "",teamConfigBean.getExpsecond() + "",teamConfigBean.getRegularClear() + "",isScreenshot);
                    } catch (Exception e) {
                        e.printStackTrace();
                        swiBtn_ScreenShot.setCheck(!checkState);
                    }
                } else {
                    swiBtn_ScreenShot.setCheck(!checkState);
                    toast("只有群主可以切换截屏通知开关");
                }
            }else if (key.equals(SW_KEY_MSGCLEAR)){
                //群消息定时清理
                if (!isSelfAdmin){
                    swiBtn_MsgClear.setCheck(!checkState);
                    toast("只有群主可以切换群消息定时清理开关");
                    return;
                }
                MobclickAgent.onEvent(AdvancedTeamInfoAct.this,TEAM_MANAGER_REGULARCLEANING);
                boolean isRegularCleanMode;
                JSONObject jsonObject = null;
                try {
                    isRegularCleanMode = checkState;
                    String extensionJsonStr = team.getExtension();

                    if (StringUtil.isNotEmpty(extensionJsonStr)){
                        jsonObject = new JSONObject(extensionJsonStr);
                    }else {
                        jsonObject = new JSONObject();
                    }
                    jsonObject.put(ISREGULARCLEANMODE,isRegularCleanMode);
                    if (checkState){
                        //清除该群聊中超过了36小时的消息
                        long currentTime = Long.parseLong(TimeUtils.getCurrentTime());
                        jsonObject.put(REGULARCLEARTIME,currentTime);
//                    long startTime = System.currentTimeMillis() - 10000;
                        long startTime = System.currentTimeMillis() - DURATION;
                        int limit = 2147483647;//要查询的最大消息条数     int类型的最大值
                        IMMessage msg = MessageBuilder.createEmptyMessage(teamId, SessionTypeEnum.Team, startTime);
                        NIMClient.getService(MsgService.class).queryMessageListEx(msg, QueryDirectionEnum.QUERY_OLD, limit,false).setCallback(new RequestCallback<List<IMMessage>>() {
                            @Override
                            public void onSuccess(final List<IMMessage> imMessages) {
                                runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        messageList.clear();
                                        messageList.addAll(imMessages);
                                        for (IMMessage imMessage : imMessages){
                                            NIMClient.getService(MsgService.class).deleteChattingHistory(imMessage);
                                        }
                                        Message message = new Message();
                                        message.what = 100;
                                        handler.sendMessage(message);

                                    }
                                };
                                thread = new Thread(runnable);
                                thread.start();
                            }

                            @Override
                            public void onFailed(int i) {
                                swiBtn_MsgClear.setCheck(!checkState);
                            }

                            @Override
                            public void onException(Throwable throwable) {
                                swiBtn_MsgClear.setCheck(!checkState);
                            }
                        });
                    }
                    NIMClient.getService(TeamService.class).updateTeam(teamId,TeamFieldEnum.Extension,jsonObject.toString());
                } catch (Exception e) {
                    swiBtn_MsgClear.setCheck(!checkState);
                    e.printStackTrace();
                }

            }
        }
    };

    private List<IMMessage> messageList = new ArrayList<>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100){
                if (null != messageList && messageList.size() > 0){
                    NIMClient.getService(MsgService.class).clearChattingHistory(teamId, SessionTypeEnum.Team);
                    MessageListPanelHelper.getInstance().notifyClearMessages(messageList);
                }
            }
        }
    };

    protected void findViews() {

        gridView = (TeamInfoGridView) findViewById(R.id.team_gird_view);
        tv_show_all = findView(R.id.tv_show_all);
        tv_show_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdvanceTeamAllMemAct.start(AdvancedTeamInfoAct.this, teamId);
            }
        });

        layoutLeadTop = findView(R.id.team_lead_top_layout);
        ((TextView) layoutLeadTop.findViewById(R.id.item_title)).setText("置顶聊天");
        swiBtn_leadTop = (SwitchButton) layoutLeadTop.findViewById(R.id.setting_item_toggle);
        swiBtn_leadTop.setTag(SW_KEY_LEAD_TOP);
        swiBtn_leadTop.setOnChangedListener(onChangedListener);
        RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(teamId, SessionTypeEnum.Team);
        boolean isSticky = recentContact != null && CommonUtil.isTagSet(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
        swiBtn_leadTop.setCheck(isSticky);



        layoutNeedDisturb = findView(R.id.team_needDisturb_layout);
        ((TextView) layoutNeedDisturb.findViewById(R.id.item_title)).setText("消息免打扰");
        swiBtn_NeedDisturb = (SwitchButton) layoutNeedDisturb.findViewById(R.id.setting_item_toggle);
        swiBtn_NeedDisturb.setTag(SW_KEY_NEEDDISTURB);
        swiBtn_NeedDisturb.setOnChangedListener(onChangedListener);
        RecentContact recentContactDisturb = NIMClient.getService(MsgService.class).queryRecentContact(teamId, SessionTypeEnum.Team);
        boolean isStickyDisturb = recentContactDisturb != null && CommonUtil.isTagSet(recentContactDisturb, RecentContactsFragment.RECENT_TAG_NEEDDISTURB);
        swiBtn_NeedDisturb.setCheck(isStickyDisturb);



        layoutScreenShot = findView(R.id.team_screenShot_layout);
        ((TextView) layoutScreenShot.findViewById(R.id.item_title)).setText("截屏通知");
        ((TextView) layoutScreenShot.findViewById(R.id.item_content)).setText("开启后，群成员在对话中截屏，所有成员均会收到通知。");
        swiBtn_ScreenShot = (SwitchButton) layoutScreenShot.findViewById(R.id.setting_item_toggle);
        swiBtn_ScreenShot.setTag(SW_KEY_SCREENSHOT);
//        swiBtn_ScreenShot.setInterceptState(true);
        swiBtn_ScreenShot.setOnChangedListener(onChangedListener);



        layoutMsgClear = findView(R.id.team_msg_clear);
        ((TextView) layoutMsgClear.findViewById(R.id.item_title)).setText("群消息定时清理");
        ((TextView) layoutMsgClear.findViewById(R.id.item_content)).setText("开启后，本群超过36小时的历史消息会自动清理，无法恢复。");
        swiBtn_MsgClear = (SwitchButton) layoutMsgClear.findViewById(R.id.setting_item_toggle);
        swiBtn_MsgClear.setTag(SW_KEY_MSGCLEAR);
//        swiBtn_MsgClear.setInterceptState(true);
        swiBtn_MsgClear.setOnChangedListener(onChangedListener);



        layoutTeamName = findViewById(R.id.team_name_layout);
        ((TextView) layoutTeamName.findViewById(R.id.item_title)).setText("群聊名称");
        layoutTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSelfAdmin || isSelfManager) {
                    TeamPropertySettingActivity.start(AdvancedTeamInfoAct.this, teamId, TeamFieldEnum.Name, team.getName());

                } else {

                    toast("只有群主与管理员才能编辑群名称");
                }
            }
        });

        headerLayout = findViewById(R.id.team_header_layout);
        teamHeadImage = (HeadImageView) findViewById(R.id.team_head_image);
        ((TextView) headerLayout.findViewById(R.id.item_detail)).setText("设置群头像");
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelfAdmin || isSelfManager) {
                    showSelector(R.string.set_head_image, REQUEST_PICK_ICON);
                } else {
                    toast("只有群主与管理员才能设置群头像");
                }
            }
        });


        layoutCardName = findViewById(R.id.team_myInfo_layout);
        ((TextView) layoutCardName.findViewById(R.id.item_title)).setText("我的群昵称");
        ((TextView) layoutCardName.findViewById(R.id.item_detail)).setHint("");
        layoutCardName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TeamPropertySettingActivity.start(AdvancedTeamInfoAct.this, teamId, TeamFieldEnum.Name, team.getName());
                AdvancedTeamNicknameActivity.start(AdvancedTeamInfoAct.this, ((TextView) layoutCardName.findViewById(R.id.item_detail)).getText().toString());
            }
        });


        layoutQrCode = findViewById(R.id.team_qrcode_layout);
        ((TextView) layoutQrCode.findViewById(R.id.item_title)).setText(R.string.team_qrcode);
        ((TextView) layoutQrCode.findViewById(R.id.item_detail)).setHint("");
        layoutQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(AdvancedTeamInfoAct.this,TEAM_MANAGER_TEAMQRCODE);
                ZXingUtils.showTeamCode(AdvancedTeamInfoAct.this, teamId);


            }
        });

        layoutBanner = findViewById(R.id.team_banner_layout);
        ((TextView) layoutBanner.findViewById(R.id.item_title)).setText(R.string.team_annourcement);
        layoutBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AdvancedTeamAnnounceActivity.start(AdvancedTeamInfoAct.this, teamId);

            }
        });

        layoutManager = findViewById(R.id.team_manager_layout);
        ((TextView) layoutManager.findViewById(R.id.item_title)).setText(R.string.team_manager);
        ((TextView) layoutManager.findViewById(R.id.item_detail)).setHint("");
        layoutManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TeamManagerActivity.start(AdvancedTeamInfoAct.this, teamId);
            }
        });

        layoutMyTeamMiBi = findViewById(R.id.team_myTeamMiBi_layout);
        ((TextView) layoutMyTeamMiBi.findViewById(R.id.item_title)).setText("我的群蜜币");
        tvMiBiNum = layoutMyTeamMiBi.findViewById(R.id.item_detail);
        layoutMyTeamMiBi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTeamMiBiActivity.start(AdvancedTeamInfoAct.this,teamId);
            }
        });

        layoutSetMiBi = findViewById(R.id.team_setMiBi_layout);
        ((TextView) layoutSetMiBi.findViewById(R.id.item_title)).setText("玩家蜜币设置");
        ((TextView) layoutSetMiBi.findViewById(R.id.item_detail)).setHint("");
        layoutSetMiBi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseRecipientsListACT.start(AdvancedTeamInfoAct.this,teamId);
            }
        });

        layoutSettlementFailed = findViewById(R.id.settlementFailed_layout);
        ((TextView) layoutSettlementFailed.findViewById(R.id.item_title)).setText("战绩未结算记录");
        ((TextView) layoutSettlementFailed.findViewById(R.id.item_detail)).setHint("");
        layoutSettlementFailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DelayedCollectionRPAct.start(AdvancedTeamInfoAct.this,teamId,"2");
            }
        });

        /**
         *  查看历史内容 改为 搜索历史内容
         */
        layoutChatHistory = findViewById(R.id.team_chatHistory_layout);
        ((TextView) layoutChatHistory.findViewById(R.id.item_title)).setText(R.string.team_chat_search);
        ((TextView) layoutChatHistory.findViewById(R.id.item_detail)).setHint("");
        layoutChatHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MessageHistoryActivity.start(AdvancedTeamInfoAct.this, teamId, SessionTypeEnum.Team);
                SearchMessageActivity.start(AdvancedTeamInfoAct.this, teamId, SessionTypeEnum.Team,EditorInfo.TYPE_CLASS_TEXT,"搜索...");
            }
        });

        layoutClearHistory = findViewById(R.id.team_cleanHistory_layout);
        ((TextView) layoutClearHistory.findViewById(R.id.item_title)).setText(R.string.team_clear_history);
        ((TextView) layoutClearHistory.findViewById(R.id.item_detail)).setHint("");
        layoutClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyAlertDialogHelper.showCommonDialog(AdvancedTeamInfoAct.this, null, "确定要清空吗？","确定","取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        SPUtils instance = SPUtils.getInstance(StatisticsConstants.ACCID);
                        instance.put(teamId,System.currentTimeMillis());
                        NIMClient.getService(MsgService.class).clearChattingHistory(teamId, SessionTypeEnum.Team);
                        MessageListPanelHelper.getInstance().notifyClearMessages(teamId);
                    }
                }).show();

            }
        });

        layoutNoReceivedRPRecord = findViewById(R.id.team_NoReceivedRPRecord_layout);
        ((TextView) layoutNoReceivedRPRecord.findViewById(R.id.item_title)).setText("未领取红包记录");
        ((TextView) layoutNoReceivedRPRecord.findViewById(R.id.item_detail)).setHint("");
        layoutNoReceivedRPRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoReceivedRPRecordActivity.start(AdvancedTeamInfoAct.this,teamId);
            }
        });

        btn_exit = findView(R.id.btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String tips = "确定要退出该群吗？";
                if (isSelfAdmin) {
                    tips = "确定要解散该群吗";
                }

                EasyAlertDialogHelper.showCommonDialog(AdvancedTeamInfoAct.this,null,tips,"确定","取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        if (isSelfAdmin) {
                            dismissTeam();
                        } else {
                            quitTeam();
                        }

                    }
                }).show();


            }
        });


    }

    /**
     * 设置群公告
     *
     * @param announcement 群公告
     */
    private void setAnnouncement(String announcement) {
        Announcement a = AnnouncementHelper.getLastAnnouncement(teamId, announcement);
        TextView otherDetail = layoutBanner.findViewById(R.id.item_other_detail);
        if (a == null) {
            otherDetail.setVisibility(View.GONE);
        } else {

            otherDetail.setVisibility(View.VISIBLE);
            if (a.getContent().equals("")) {
                ((TextView) layoutBanner.findViewById(R.id.item_detail)).setText("");
                otherDetail.setText("公告内容为空");
            } else {
                ((TextView) layoutBanner.findViewById(R.id.item_detail)).setText(" ");
                otherDetail.setText(a.getContent());
            }
        }
    }

    protected void initAdapter() {
        memberAccounts = new ArrayList<>();
        members = new ArrayList<>();
        dataSource = new ArrayList<>();
        managerList = new ArrayList<>();
        adapter = new TeamMemberAdapter(teamId,this, dataSource, this, this, this);
        adapter.setEventListener(this);

        gridView.setSelector(R.color.transparent);


        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        gridView.setAdapter(adapter);
    }


    /**
     * 更新成员信息
     */
    protected void updateTeamMemberDataSource() {

        if (!(isSelfAdmin || isSelfManager)) {
            layoutManager.setVisibility(View.GONE);
            layoutSetMiBi.setVisibility(View.GONE);
        }
        if (members.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
            if (members.size() > TEAM_MEMBERS_SHOW_LIMIT) {
                tv_show_all.setVisibility(View.VISIBLE);
            } else {
                tv_show_all.setVisibility(View.GONE);
            }
        } else {
            gridView.setVisibility(View.GONE);
            return;
        }

        dataSource.clear();


        // member item
        int count = 0;
        String identity = null;
        for (String account : memberAccounts) {
            int limit = TEAM_MEMBERS_SHOW_LIMIT;
            if (team.getTeamInviteMode() == TeamInviteModeEnum.All || isSelfAdmin || isSelfManager) {
                limit = TEAM_MEMBERS_SHOW_LIMIT - 1;
            }

            if (count < limit) {
                identity = getIdentity(account);
                dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                        .NORMAL, teamId, account, identity));
            }
            count++;
        }


        // add item
        if (isSelfAdmin || isSelfManager){
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null, null));
        }
//        普通成员不可以拉人
//        else {
//            Team team = NimUIKit.getTeamProvider().getTeamById(teamId);
//            if (team.getVerifyType() != VerifyTypeEnum.Apply){
//                dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null, null));
//            }
//        }

        // remove item
        if (isSelfManager||isSelfAdmin) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.DELETE, null, null,
                    null));
        }

        // refresh
        if (adapter.getMode() != TeamMemberAdapter.Mode.DELETE) {
            adapter.notifyDataSetChanged();
        }

        // refresh
        adapter.notifyDataSetChanged();

    }


    protected String getIdentity(String account) {
        String identity;
        if (creator.equals(account)) {
            identity = TeamMemberHolder.OWNER;
        } else if (managerList.contains(account)) {
            identity = TeamMemberHolder.ADMIN;
        } else {
            identity = null;
        }
        return identity;
    }

    /**
     * 更新我的群名片
     *
     * @param m
     */
    private void updateTeamBusinessCard(List<TeamMember> m) {
        for (TeamMember teamMember : m) {
            if (teamMember != null && teamMember.getAccount().equals(NimUIKit.getAccount())) {
                ((TextView) layoutCardName.findViewById(R.id.item_detail)).setText(teamMember.getTeamNick() != null ? teamMember.getTeamNick() : "");
            }
        }
    }


    /**
     * 设置我的名片
     *
     * @param nickname 群昵称
     */
    private void setBusinessCard(final String nickname) {
        DialogMaker.showProgressDialog(this, getString(com.netease.yqbj.uikit.R.string.empty), true);
        NIMClient.getService(TeamService.class).updateMemberNick(teamId, NimUIKit.getAccount(), nickname).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                ((TextView) layoutCardName.findViewById(R.id.item_detail)).setText(nickname);
                ToastHelper.showToast(AdvancedTeamInfoAct.this, com.netease.yqbj.uikit.R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                ToastHelper.showToast(AdvancedTeamInfoAct.this, String.format(getString(com.netease.yqbj.uikit.R.string.update_failed), code));
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }


    /**
     * 添加群成员到列表
     *
     * @param m     群成员列表
     * @param clear 是否清除
     */
    protected void addTeamMembers(final List<TeamMember> m, boolean clear) {
        if (m == null || m.isEmpty()) {
            return;
        }

        isSelfManager = false;
        isSelfAdmin = false;

        if (clear) {
            this.members.clear();
            this.memberAccounts.clear();
        }

        // add
        if (this.members.isEmpty()) {
            this.members.addAll(m);
        } else {
            for (TeamMember tm : m) {
                if (!this.memberAccounts.contains(tm.getAccount())) {
                    this.members.add(tm);
                }
            }
        }

        // sort
        Collections.sort(this.members, TeamHelper.teamMemberComparator);

        // accounts, manager, creator
        this.memberAccounts.clear();
        this.managerList.clear();
        for (TeamMember tm : members) {
            if (tm == null) {
                continue;
            }
            if (tm.getType() == TeamMemberType.Manager) {
                managerList.add(tm.getAccount());
            }
            if (tm.getAccount().equals(NimUIKit.getAccount())) {
                if (tm.getType() == TeamMemberType.Manager) {
                    isSelfManager = true;
                } else if (tm.getType() == TeamMemberType.Owner) {
                    isSelfAdmin = true;
                    creator = NimUIKit.getAccount();
                }
            }
            this.memberAccounts.add(tm.getAccount());
        }

        updateTeamMemberDataSource();
    }



    TeamDataChangedObserver teamDataObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {

            for (Team team : teams) {
                if (team.getId().equals(teamId)) {
                    updateTeamInfo(team);
                    requestMembers();
//                    upDateTeamConfigInfo();
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team.getId().equals(teamId)) {
                AdvancedTeamInfoAct.this.team = team;
                finish();
            }
        }
    };


    /**
     * ************************** 群信息变更监听 **************************
     */
    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
//        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
        registerUserInfoChangedObserver(register);
    }

    private void registerUserInfoChangedObserver(boolean register) {
        if (register) {
            if (userInfoObserver == null) {
                userInfoObserver = new UserInfoObserver() {
                    @Override
                    public void onUserInfoChanged(List<String> accounts) {
                        adapter.notifyDataSetChanged();
                    }
                };
            }
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true);
        } else {
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false);
        }
    }


    /**
     * 移除群成员成功后，删除列表中的群成员
     *
     * @param account 被删除成员帐号
     */
    private void removeMember(String account) {
        if (TextUtils.isEmpty(account)) {
            return;
        }

        memberAccounts.remove(account);

        for (TeamMember m : members) {
            if (m.getAccount().equals(account)) {
                members.remove(m);
                break;
            }
        }


        for (TeamMemberAdapter.TeamMemberItem item : dataSource) {
            if (item.getAccount() != null && item.getAccount().equals(account)) {
                dataSource.remove(item);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 邀请群成员
     *
     * @param accounts 邀请帐号
     */
    private void inviteMembers(final ArrayList<String> accounts) {
//        JSONObject inviterEx = new JSONObject();
//        try {
//            inviterEx.put(StatisticsConstants.INVITER,NimUIKit.getAccount());
//            inviterEx.put(StatisticsConstants.TEAMID,teamId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        NIMClient.getService(TeamService.class).addMembersEx(teamId, accounts, "邀请附言", inviterEx.toString()).setCallback(new RequestCallback<List<String>>() {
//            @Override
//            public void onSuccess(List<String> failedAccounts) {
//                CommonUtil.uploadTeamIcon(teamId,AdvancedTeamInfoAct.this);
//            }
//
//            @Override
//            public void onFailed(int code) {
//                if (code == ResponseCode.RES_TEAM_INVITE_SUCCESS) {
//                    ToastHelper.showToast(AdvancedTeamInfoAct.this, R.string.team_invite_members_success);
//                } else {
//                    ToastHelper.showToast(AdvancedTeamInfoAct.this, "邀请失败");
//                }
//            }
//
//            @Override
//            public void onException(Throwable exception) {
//
//            }
//        });
        showProgress(this,false);
        UserApi.addMember(teamId, creator, NimUIKit.getAccount(), JSON.toJSONString(accounts), this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("邀请成员成功");
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

    /**
     * 批量踢人入口
     * @param removeList
     */
    private void kickTeamOnce(final List<String> removeList){

        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);


        UserApi.kickTeamByOnce(teamId, JSON.toJSONString(removeList), this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                DialogMaker.dismissProgressDialog();
//                CommonUtil.uploadTeamIcon(teamId,AdvancedTeamInfoAct.this);
                if (code == Constants.SUCCESS_CODE){
                    requestMembers();
                    ToastHelper.showToast(AdvancedTeamInfoAct.this, R.string.remove_member_success);
                }else if (code == Constants.RESPONSE_CODE.CODE_40014){
                    BaseBean bean = (BaseBean) object;
                    EasyAlertDialogHelper.showOneButtonDiolag(AdvancedTeamInfoAct.this, "移出群聊失败", bean.getMessage(), "知道了", true, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();
                toast(errMessage);
            }
        });





    }


    /**
     * 邀请群成员
     *
     * @param accounts 邀请帐号
     */
    private void addManager(ArrayList<String> accounts) {
        // teamId 操作的群id， accountList为待提升为管理员的用户帐号列表
        NIMClient.getService(TeamService.class).addManagers(teamId, accounts).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> managers) {
                // 添加群管理员成功
                requestMembers();

            }

            @Override
            public void onFailed(int code) {
                // 添加群管理员失败
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
            }
        });

    }


    /**
     * 非群主退出群
     */
    private void quitTeam() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        UserApi.leaveTeam(teamId, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                DialogMaker.dismissProgressDialog();
                if (code == Constants.SUCCESS_CODE){
                    ToastHelper.showToast(AdvancedTeamInfoAct.this, R.string.quit_team_success);
                    setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_EXTRA_REASON, RESULT_EXTRA_REASON_QUIT));
                    finish();
                }else if (code == Constants.RESPONSE_CODE.CODE_40014){
                    BaseBean bean = (BaseBean) object;
                    EasyAlertDialogHelper.showOneButtonDiolag(AdvancedTeamInfoAct.this, "不可退群", bean.getMessage(), "知道了", true, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();
                ToastHelper.showToast(AdvancedTeamInfoAct.this, errMessage);
            }
        });




    }

    /**
     * 群主解散群(直接退出)
     */
    private void dismissTeam() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        UserApi.removeTeam(teamId, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                DialogMaker.dismissProgressDialog();
                if (code == Constants.SUCCESS_CODE){
                    MobclickAgent.onEvent(AdvancedTeamInfoAct.this,TEAM_MANAGER_DISBANDANSUCCESS);
                    setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_EXTRA_REASON, RESULT_EXTRA_REASON_DISMISS));
                    ToastHelper.showToast(AdvancedTeamInfoAct.this, R.string.dismiss_team_success);
                    finish();
                }else if (code == Constants.RESPONSE_CODE.CODE_40013){
                    BaseBean bean = (BaseBean) object;
                    EasyAlertDialogHelper.showOneButtonDiolag(AdvancedTeamInfoAct.this, "不可解散", bean.getMessage(), "知道了", true, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }else {
                    toast((String) object);
                }

            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();
                toast(errMessage);
            }
        });

    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return TeamMemberHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public void onHeadImageViewClick(String account) {
        // 打开群成员信息详细页面
        AdvancedTeamMemberInfoAct.startActivityForResult(AdvancedTeamInfoAct.this, account, teamId);
    }


    /**
     * 从联系人选择器发起邀请成员
     */
    @Override
    public void onAddMember() {
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        NimUIKit.startContactSelector(AdvancedTeamInfoAct.this, option, REQUEST_CODE_CONTACT_SELECT);
    }


    @Override
    public void onRemoveMember() {

        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = "选择你要移除的成员";
        option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
        option.allowSelectEmpty = true;
        option.teamId = teamId;
        ArrayList<String> disableAccounts = new ArrayList<>();
//        if(isSelfAdmin){
//
//            disableAccounts.add(NimUIKit.getAccount());
//        }else if(isSelfManager){
//            disableAccounts.add(creator);
//            disableAccounts.addAll(managerList);
//        }
        for (TeamMember teamMember : members){
            Map<String, Object> extension = teamMember.getExtension();
            if (null == extension){
                extension = new HashMap<>();
            }
            String inviter;
            String teamMemberEx = (String) extension.get("ext");
            if (StringUtil.isEmpty(teamMemberEx) || teamMemberEx.equals("null")){
                disableAccounts.add(teamMember.getAccount());
            }else {
                try {
                    JSONObject jsonObject = new JSONObject(teamMemberEx);
                    inviter = (String) jsonObject.get(StatisticsConstants.INVITER);
                    if (StringUtil.isNotEmpty(inviter) && !inviter.equals(NimUIKit.getAccount())){
                        disableAccounts.add(teamMember.getAccount());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        option.itemDisableFilter = new ContactIdFilter(disableAccounts);

        NimUIKit.startContactSelector(AdvancedTeamInfoAct.this, option, REQUEST_CODE_CONTACT_SELECT_REMOVE);



    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getTeamMemberEvent(TeamMemberEvent event){

        requestMembers();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && adapter.switchMode()) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CONTACT_SELECT:
                final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
                    inviteMembers(selected);
                }
                break;
            case REQUEST_CODE_CONTACT_MANAGER_SELECT:
                final ArrayList<String> selectedManager = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selectedManager != null && !selectedManager.isEmpty()) {
                    addManager(selectedManager);
                }
                break;

            case REQUEST_CODE_CONTACT_SELECT_REMOVE:
                final ArrayList<String> selectedRemove = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selectedRemove != null && !selectedRemove.isEmpty()) {
                    // teamId表示群ID，account表示被踢出的成员帐号
                    ArrayList<String> removeList = new ArrayList<>();
                    removeList.addAll(selectedRemove);
                    for (String invitersStr : selectedRemove){
                        for (TeamMember teamMember : members){
                            Map<String, Object> extension = teamMember.getExtension();
                            if (null == extension){
                                extension = new HashMap<>();
                            }
                            String teamMemberEx = (String) extension.get("ext");
                            if (!StringUtil.isEmpty(teamMemberEx) && !teamMemberEx.equals("null")){
                                try {
                                    JSONObject jsonObject = new JSONObject(teamMemberEx);
                                    String teamMemberInviter = (String) jsonObject.get(StatisticsConstants.INVITER);
                                    if (StringUtil.isNotEmpty(teamMemberInviter) && teamMemberInviter.equals(invitersStr)){
                                        removeList.add(teamMember.getAccount());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

//                    NIMClient.getService(TeamService.class).removeMember(teamId, JSON.toJSONString(removeList)).setCallback(new RequestCallback<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.e("removeMember","成功");
//                        }
//
//                        @Override
//                        public void onFailed(int i) {
//                            Log.e("removeMember","失败>>>>>code:" + i);
//                        }
//
//                        @Override
//                        public void onException(Throwable throwable) {
//                            Log.e("removeMember","异常");
//                        }
//                    });

                    kickTeamOnce(removeList);
                }
                break;

            case AdvancedTeamNicknameActivity.REQ_CODE_TEAM_NAME:
                setBusinessCard(data.getStringExtra(AdvancedTeamNicknameActivity.EXTRA_NAME));
                break;
            case REQUEST_PICK_ICON:
                String path = data.getStringExtra(Extras.EXTRA_FILE_PATH);
                updateTeamIcon(path);
                break;
            default:
                break;
        }
    }

    private void updateTeamIcon(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file == null) {
            return;
        }
        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(R.string.team_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);
        new Handler().postDelayed(outimeTask, ICON_TIME_OUT);
        uploadFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    LogUtil.i(TAG, "upload icon success, url =" + url);

                    NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.ICON, url).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            DialogMaker.dismissProgressDialog();
                            ToastHelper.showToast(AdvancedTeamInfoAct.this, com.netease.yqbj.uikit.R.string.update_success);
                            onUpdateDone();
                        }

                        @Override
                        public void onFailed(int code) {
                            DialogMaker.dismissProgressDialog();
                            ToastHelper.showToast(AdvancedTeamInfoAct.this, String.format(getString(com.netease.yqbj.uikit.R.string.update_failed), code));
                        }

                        @Override
                        public void onException(Throwable exception) {
                            DialogMaker.dismissProgressDialog();
                        }
                    }); // 更新资料
                } else {
                    ToastHelper.showToast(AdvancedTeamInfoAct.this, com.netease.yqbj.uikit.R.string.team_update_failed);
                    onUpdateDone();
                }
            }
        });

    }

    private void cancelUpload(int resId) {
        if (uploadFuture != null) {
            uploadFuture.abort();
            ToastHelper.showToast(AdvancedTeamInfoAct.this, resId);
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(R.string.team_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadFuture = null;
        DialogMaker.dismissProgressDialog();
    }

    /**
     * 打开图片选择器
     */
    private void showSelector(int titleId, final int requestCode) {
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = titleId;
        option.multiSelect = false;
        option.crop = true;
        option.cropOutputImageWidth = 720;
        option.cropOutputImageHeight = 720;

        PickImageHelper.pickImage(AdvancedTeamInfoAct.this, requestCode, option);
    }

}

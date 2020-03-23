package com.yqbj.ghxm.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.yqbj.ghxm.R;
import com.netease.yqbj.avchatkit.AVChatProfile;
import com.netease.yqbj.avchatkit.activity.AVChatActivity;
import com.netease.yqbj.avchatkit.constant.AVChatExtras;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.common.ModuleUIComFn;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.ui.drop.DropCover;
import com.netease.yqbj.uikit.common.ui.drop.DropFake;
import com.netease.yqbj.uikit.common.ui.drop.DropManager;
import com.netease.yqbj.uikit.support.permission.MPermission;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.yqbj.ghxm.UIEx.MyNimUIkit;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.common.util.AppDemoUtils;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.config.preference.Preferences;
import com.yqbj.ghxm.contact.activity.AddFriendActivity;
import com.yqbj.ghxm.login.LogoutHelper;
import com.yqbj.ghxm.main.activity.GlobalSearchActivity;
import com.yqbj.ghxm.main.activity.SettingsActivity;
import com.yqbj.ghxm.main.activity.WelcomeActivity;
import com.yqbj.ghxm.main.adapter.MainTabPagerAdapter;
import com.yqbj.ghxm.main.helper.SystemMessageUnreadManager;
import com.yqbj.ghxm.main.model.MainTab;
import com.yqbj.ghxm.main.reminder.ReminderItem;
import com.yqbj.ghxm.main.reminder.ReminderManager;
import com.yqbj.ghxm.main.reminder.ReminderSettings;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.session.SessionHelper;
import com.yqbj.ghxm.session.activity.MessageInfoActivity;
import com.yqbj.ghxm.share.ShareSchemeActivity;
import com.yqbj.ghxm.team.TeamCreateHelper;
import com.yqbj.ghxm.team.activity.AdvancedTeamSearchActivity;
import com.yqbj.ghxm.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 * Created by huangjun on 2015/3/25.
 */
public class SplashActivity extends BaseAct implements ViewPager.OnPageChangeListener, ReminderManager.UnreadNumChangedCallback {

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private static final int REQUEST_CODE_NORMAL = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;
    private static final int BASIC_PERMISSION_REQUEST_CODE = 100;
    private static final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final int CHAT_FRA = 0;
    private static final int CONTACT_FRA = 1;

    //    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private TextView tvMainChat;
    private TextView tvMainContacts;
    private TextView tvMainPersonal;
    private int scrollState;
    private MainTabPagerAdapter adapter;
    private int mTabTextColorSelecdted;
    private int mTabTextColorNormal;
    private int currentPos = 0;
    private BottomBarListener mBottomBarListener;

    private View rl_contacts;
    private View rl_chat;

    DropFake chatRedPoint = null;
    DropFake contactRedPoint = null;

    private float fontSizeScale;


    private boolean isFirstIn;
    private Observer<Integer> sysMsgUnreadCountChangedObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer unreadCount) {

            NIMClient.getService(SystemMessageService.class).querySystemMessageUnread()
                .setCallback(new RequestCallback<List<SystemMessage>>() {
                    @Override
                    public void onSuccess(List<SystemMessage> systemMessages) {
                        for (int i = 0; i < systemMessages.size(); i++){
                            SystemMessage msg = systemMessages.get(i);
                            for (int j = systemMessages.size() - 1 ; j > i; j--){
                                SystemMessage msg2 = systemMessages.get(j);
                                if (msg.getFromAccount().equals(msg2.getFromAccount()) && msg.getType().getValue() == msg2.getType().getValue()){
                                    systemMessages.remove(msg2);
                                }

                            }
                        }
                        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(systemMessages.size());
                        ReminderManager.getInstance().updateContactUnreadNum(systemMessages.size());
                    }

                    @Override
                    public void onFailed(int i) {

                    }

                    @Override
                    public void onException(Throwable throwable) {

                    }
                });
        }
    };


    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSwipeBackLayout().setEnableGesture(false);//禁止右滑退出
        isFirstIn = true;
        WelcomeActivity.setFirstEnterState(false);
        //不保留后台活动，从厂商推送进聊天页面，会无法退出聊天页面
        if (savedInstanceState == null && parseIntent()) {
            return;
        }
        init();
    }

    private void init() {
        fontSizeScale = SPUtils.getInstance().getFloat("app_font_size", 0.0f);
        observerSyncDataComplete();
        findViews();
        setupPager();
        setupTabs();
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        requestSystemMessageUnreadCount();
        initUnreadCover();
        requestBasicPermission();
        initModuleComFn();
        initData();
    }

    private void initData(){

    }

    private void initModuleComFn() {
        ModuleUIComFn.getInstance().ToGroupDetailsInstance = new ModuleUIComFn.ToGroupDetails() {
            @Override
            public void toGroupDetailsClick(Context context, String var1) {
                Team team = NimUIKit.getTeamProvider().getTeamById(var1);
                if (team != null && team.isMyTeam()) {
                    MyNimUIkit.startTeamInfo(context, team.getId());
                } else {
                    ToastHelper.showToast(context, R.string.team_invalid_tip);
                }
            }
        };

        ModuleUIComFn.getInstance().toPersonChatMsgInstance = new ModuleUIComFn.ToPersonChatMsg() {
            @Override
            public void toPersonChatMsgClick(Context context, String var1) {
                MessageInfoActivity.startActivity(context, var1);
            }
        };

        ModuleUIComFn.getInstance().toGlobalSearchInstance = new ModuleUIComFn.ToGlobalSearch() {
            @Override
            public void toGlobalSearch(Context context) {
                GlobalSearchActivity.start(context);
            }
        };

        ModuleUIComFn.getInstance().getTeamConfig = new ModuleUIComFn.GetTeamConfig() {
            @Override
            public void getTeamConfig(Context context, String var1, final ModuleUIComFn.GetTeamConfigCallback callback) {
                UserApi.teamConfigGet(var1, context, new requestCallback() {
                    @Override
                    public void onSuccess(int code, Object object) {
                        callback.onSuccess(code,object);
                    }

                    @Override
                    public void onFailed(String errMessage) {
                        callback.onFailed(errMessage);
                    }
                });
            }
        };

        ModuleUIComFn.getInstance().startRobotChat = new ModuleUIComFn.StartRobotChat() {
            @Override
            public void startRobotChat(Context context, String id, IMMessage anchor) {
                SessionHelper.startP2PSession(context, id, anchor);
            }
        };
    }

    private boolean parseIntent() {

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            intent.removeExtra(EXTRA_APP_QUIT);
            onLogout();
            return true;
        }

        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
            }

            return true;
        }

        if (intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT) && AVChatProfile.getInstance().isAVChatting()) {
            intent.removeExtra(AVChatActivity.INTENT_ACTION_AVCHAT);
            Intent localIntent = new Intent();
            localIntent.setClass(this, AVChatActivity.class);
            startActivity(localIntent);
            return true;
        }

        String account = intent.getStringExtra(AVChatExtras.EXTRA_ACCOUNT);
        if (intent.hasExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION) && !TextUtils.isEmpty(account)) {
            intent.removeExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION);
            SessionHelper.startP2PSession(this, account);
            return true;
        }

        if ( intent.hasExtra(ShareSchemeActivity.ACTION_BROWSABLE_LAUNCHER))
        {
            intent.setClass(this,ShareSchemeActivity.class);
            startActivity(intent);
        }


        return false;
    }

    private void observerSyncDataComplete() {
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(new Observer<Void>() {
            @Override
            public void onEvent(Void v) {
                DialogMaker.dismissProgressDialog();
            }
        });
        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(SplashActivity.this, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false);
        }
    }

    private void findViews() {
//        tabs = findView(R.id.tabs);
        pager = findView(R.id.main_tab_pager);
        tvMainChat = findView(R.id.tv_main_chat);
        tvMainContacts = findView(R.id.tv_main_Contacts);
        tvMainPersonal = findView(R.id.tv_main_personal);

        rl_chat = findView(R.id.rl_chat);
        rl_contacts = findView(R.id.rl_contacts);

        mBottomBarListener = new BottomBarListener();
        rl_chat.setOnClickListener(mBottomBarListener);
        rl_contacts.setOnClickListener(mBottomBarListener);
        tvMainPersonal.setOnClickListener(mBottomBarListener);

        chatRedPoint = findView(R.id.unread_number_chat);
        contactRedPoint = findView(R.id.unread_number_contact);

        mTabTextColorSelecdted = getResources().getColor(R.color.theme_color);
        mTabTextColorNormal = getResources().getColor(R.color.color_9000000);
    }

    private void setupPager() {
        adapter = new MainTabPagerAdapter(getSupportFragmentManager(), this, pager);
        pager.setOffscreenPageLimit(adapter.getCacheCount());
//        pager.setPageTransformer(true, new FadeInOutPageTransformer());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
    }

    private void setupTabs() {
//        tabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {
//            @Override
//            public int getTabLayoutResId(int position) {
//                return R.layout.tab_layout_main;
//            }
//
//            @Override
//            public boolean screenAdaptation() {
//                return true;
//            }
//        });
//        tabs.setViewPager(pager);
//        tabs.setOnTabClickListener(adapter);
//        tabs.setOnTabDoubleTapListener(adapter);


    }

    private void updateTabsNormal() {
        tvMainChat.setTextColor(mTabTextColorNormal);
        tvMainChat.setCompoundDrawablesWithIntrinsicBounds(null,
                getResources().getDrawable(R.mipmap.switch_off), null, null);

        tvMainContacts.setTextColor(mTabTextColorNormal);
        tvMainContacts.setCompoundDrawablesWithIntrinsicBounds(null,
                getResources().getDrawable(R.mipmap.contacts_off), null, null);

        tvMainPersonal.setTextColor(mTabTextColorNormal);
        tvMainPersonal.setCompoundDrawablesWithIntrinsicBounds(null,
                getResources().getDrawable(R.mipmap.personal_off), null, null);

    }

    public void updateTabsSelected(int currentPox) {
        updateTabsNormal();
        switch (currentPox) {
            case 0:
                tvMainChat.setTextColor(mTabTextColorSelecdted);
                tvMainChat.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.mipmap.switch_on), null, null);
                break;
            case 1:
                tvMainContacts.setTextColor(mTabTextColorSelecdted);
                tvMainContacts.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.mipmap.contacts_on), null, null);
                break;
            case 2:
                tvMainPersonal.setTextColor(mTabTextColorSelecdted);
                tvMainPersonal.setCompoundDrawablesWithIntrinsicBounds(null,
                        getResources().getDrawable(R.mipmap.personal_on), null, null);
                break;
            default:
                break;
        }
    }

    private class BottomBarListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_chat:
                    currentPos = 0;
                    break;
                case R.id.rl_contacts:
                    currentPos = 1;
                    break;
                case R.id.tv_main_personal:
                    currentPos = 2;
                    break;
                default:
                    break;
            }
            updateTabsSelected(currentPos);
            pager.setCurrentItem(currentPos, false);
        }
    }

    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(sysMsgUnreadCountChangedObserver, register);
    }

    /**
     * 查询系统消息未读数
     */
    private void requestSystemMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock();
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
        ReminderManager.getInstance().updateContactUnreadNum(unread);
    }

    //初始化未读红点动画
    private void initUnreadCover() {
        DropManager.getInstance().init(this, (DropCover) findView(R.id.unread_cover),
                new DropCover.IDropCompletedListener() {
                    @Override
                    public void onCompleted(Object id, boolean explosive) {
                        if (id == null || !explosive) {
                            return;
                        }

                        if (id instanceof RecentContact) {
                            RecentContact r = (RecentContact) id;
                            NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(), r.getSessionType());
                            return;
                        }

                        if (id instanceof String) {
                            if (((String) id).contentEquals("0")) {
                                NIMClient.getService(MsgService.class).clearAllUnreadCount();
                            } else if (((String) id).contentEquals("1")) {
                                NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
                            }
                        }
                    }
                });
    }

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(SplashActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    private void onLogout() {
        Preferences.saveUserToken("");
        // 清理缓存&注销监听
        LogoutHelper.logout();
        // 启动登录
//        LoginActivity.start(this);

        WelcomeActivity.setFirstEnterState(true);
        AppDemoUtils.simpleToAct(SplashActivity.this, WelcomeActivity.class);
        finish();

    }

    private void selectPage() {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter.onPageSelected(pager.getCurrentItem());
        }
    }

    /**
     * 设置最近联系人的消息为已读
     * <p>
     * account, 聊天对象帐号，或者以下两个值：
     * {@link MsgService#MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     * {@link MsgService#MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
     */
    private void enableMsgNotification(boolean enable) {
        boolean msg = (pager.getCurrentItem() != MainTab.RECENT_CONTACTS.tabIndex);
        if (enable | msg) {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(SplashActivity.this, SettingsActivity.class));
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelector(SplashActivity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelector(SplashActivity.this, advancedOption, REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(SplashActivity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(SplashActivity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(SplashActivity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 第一次 ， 三方通知唤起进会话页面之类的，不会走初始化过程
        boolean temp = isFirstIn;
        isFirstIn = false;
        if (pager == null && temp) {
            return;
        }
        //如果不是第一次进 ， eg: 其他页面back
        if (pager == null) {
            init();
        }
        enableMsgNotification(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pager == null) {
            return;
        }
        enableMsgNotification(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
        DropManager.getInstance().destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_NORMAL) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                TeamCreateHelper.createNormalTeam(SplashActivity.this, selected, false, null);
            } else {
                ToastHelper.showToast(SplashActivity.this, "请选择至少一个联系人！");
            }
        } else if (requestCode == REQUEST_CODE_ADVANCED) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
//                之前客户端通过云信建群      之后改成服务端建群
//            TeamCreateHelper.createAdvancedTeam(SplashActivity.this, selected);
            createTeam(selected);
        }
    }

    private void createTeam(ArrayList<String> selected) {
        showProgress(SplashActivity.this,false);
        UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(NimUIKit.getAccount());
        String teamName = userInfo.getName() + ",";
        for (int i = 0; i < selected.size(); i++){
            if (i <= 4){
                userInfo = NimUIKit.getUserInfoProvider().getUserInfo(selected.get(i));
                teamName = teamName + userInfo.getName() + ",";
            }
        }
        UserApi.createTeam(teamName.substring(0,teamName.length()-1),JSON.toJSONString(selected), SplashActivity.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){

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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        adapter.onPageScrolled(position);
    }

    @Override
    public void onPageSelected(int position) {
//        tabs.onPageSelected(position);
        updateTabsSelected(position);
        selectPage();
        enableMsgNotification(false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
//        tabs.onPageScrollStateChanged(state);
        scrollState = state;
        selectPage();
    }

    //未读消息数量观察者实现
    @Override
    public void onUnreadNumChanged(final ReminderItem item) {
        MainTab tab = MainTab.fromReminderId(item.getId());
        if (tab != null) {
            if (item.getId() == CHAT_FRA) {
                setRedPoint(chatRedPoint, item);
            } else if (item.getId() == CONTACT_FRA) {
                setRedPoint(contactRedPoint, item);
            }
        }
    }

    private void setRedPoint(final DropFake redPointView, final ReminderItem item) {
        if (redPointView != null) {
            redPointView.setTouchListener(new DropFake.ITouchListener() {
                @Override
                public void onDown() {
                    DropManager.getInstance().setCurrentId(String.valueOf(item.getId()));
                    DropManager.getInstance().down(redPointView, redPointView.getText());
                }

                @Override
                public void onMove(float curX, float curY) {
                    DropManager.getInstance().move(curX, curY);
                }

                @Override
                public void onUp() {
                    DropManager.getInstance().up();
                }
            });
        }
        int unread = item.unread();
        redPointView.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
        if (unread > 0) {
            redPointView.setText(String.valueOf(ReminderSettings.unreadMessageShowRule(unread)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {

        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
//        try {
//            ToastHelper.showToast(this, "未全部授权，部分功能可能无法正常运行！");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    //点返回键不销毁
    @Override
    public void onBackPressed() {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(launcherIntent);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        if (fontSizeScale > 0.5) {
            config.fontScale = fontSizeScale - 0.1f;//1 设置正常字体大小的倍数
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}

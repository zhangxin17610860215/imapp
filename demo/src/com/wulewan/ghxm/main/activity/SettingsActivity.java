package com.wulewan.ghxm.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.avchat.AVChatNetDetectCallback;
import com.netease.nimlib.sdk.avchat.AVChatNetDetector;
import com.netease.nimlib.sdk.lucene.LuceneService;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.misc.MiscService;
import com.netease.nimlib.sdk.mixpush.MixPushService;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.settings.SettingsService;
import com.netease.nimlib.sdk.settings.SettingsServiceObserver;
import com.netease.wulewan.avchatkit.AVChatKit;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.ToastHelper;
import com.netease.wulewan.uikit.common.activity.UI;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.wulewan.ghxm.DemoCache;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.config.preference.UserPreferences;
import com.wulewan.ghxm.contact.activity.BlackListActivity;
import com.wulewan.ghxm.contact.activity.SetFontSizeActivity;
import com.wulewan.ghxm.contact.activity.UserProfileSettingActivity;
import com.wulewan.ghxm.jsbridge.JsBridgeActivity;
import com.wulewan.ghxm.main.SplashActivity;
import com.wulewan.ghxm.main.adapter.SettingsAdapter;
import com.wulewan.ghxm.main.model.SettingTemplate;
import com.wulewan.ghxm.main.model.SettingType;
import com.wulewan.ghxm.redpacket.NIMRedPacketClient;
import com.wulewan.ghxm.utils.AppUtils;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2015/6/26.
 */
public class SettingsActivity extends UI implements SettingsAdapter.SwitchChangeListener {
    private static final int TAG_HEAD = 1;
    private static final int TAG_NOTICE = 2;
    private static final int TAG_NO_DISTURBE = 3;
    private static final int TAG_CLEAR = 4;
    private static final int TAG_CUSTOM_NOTIFY = 5;
    private static final int TAG_ABOUT = 6;
    private static final int TAG_SPEAKER = 7;

    private static final int TAG_NRTC_SETTINGS = 8;
    private static final int TAG_NRTC_NET_DETECT = 9;

    private static final int TAG_MSG_IGNORE = 10;
    private static final int TAG_RING = 11;
    private static final int TAG_LED = 12;
    private static final int TAG_NOTICE_CONTENT = 13; // 通知栏提醒配置
    private static final int TAG_CLEAR_INDEX = 18; // 清空全文检索缓存
    private static final int TAG_MULTIPORT_PUSH = 19; // 桌面端登录，是否推送
    private static final int TAG_JS_BRIDGE = 20; // js bridge

    private static final int TAG_NOTIFICATION_STYLE = 21; // 通知栏展开、折叠

    private static final int TAG_JRMFWAllET = 22; // 我的钱包

    private static final int TAG_CLEAR_SDK_CACHE = 23; // 清除 sdk 文件缓存

    private static final int TAG_PUSH_SHOW_NO_DETAIL = 24; // 推送消息不展示详情

    private static final int TAG_VIBRATE = 25; // 推送消息不展示详情

    private static final int TAG_PRIVATE_CONFIG = 26; // 私有化开关
    private static final int TAG_MSG_MIGRATION = 27; // 本地消息迁移

    private static final int TAG_BLACK_LIST = 29; //黑名单
    private static final int TAG_CHECK_UPDATE = 30; //检查更新

    private static final int TAG_TEXT_SIZE = 31;//字体大小设置

    private static final int TAG_OPINION = 32;//意见反馈


    ListView listView;
    SettingsAdapter adapter;
    private List<SettingTemplate> items = new ArrayList<>();
    private String noDisturbTime;
    private SettingTemplate disturbItem;
    private SettingTemplate clearIndexItem;
    private SettingTemplate clearSDKDirCacheItem;
    private SettingTemplate notificationItem;
    private SettingTemplate pushShowNoDetailItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

//        onInitSetBack(R.id.com_back_btn, R.id.com_back_img);
        onInitSetBack(SettingsActivity.this);
        onInitSetTitle(SettingsActivity.this, getString(R.string.uInfo_set));

        initData();
        initUI();

        registerObservers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            setMessageNotify(false);
        } else {
            if (UserPreferences.getNotificationToggle()) {
                setMessageNotify(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }


    private void registerObservers(boolean register) {
        NIMClient.getService(SettingsServiceObserver.class).observeMultiportPushConfigNotify(pushConfigObserver, register);
    }

    Observer<Boolean> pushConfigObserver = new Observer<Boolean>() {
        @Override
        public void onEvent(Boolean aBoolean) {
            ToastHelper.showToast(SettingsActivity.this, "收到multiport push config：" + aBoolean);
        }
    };

    private void initData() {
        if (UserPreferences.getStatusConfig() == null || !UserPreferences.getStatusConfig().downTimeToggle) {
            noDisturbTime = getString(R.string.setting_close);
        } else {
            noDisturbTime = String.format("%s到%s", UserPreferences.getStatusConfig().downTimeBegin,
                    UserPreferences.getStatusConfig().downTimeEnd);
        }
//        getSDKDirCacheSize();
    }

    private void initUI() {
        initItems();
        listView = findView(R.id.settings_listview);
        View footer = LayoutInflater.from(this).inflate(R.layout.settings_logout_footer, null);
        listView.addFooterView(footer);

        initAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingTemplate item = items.get(position);
                onListItemClick(item);
            }
        });

        TextView tvVersionCode = footer.findViewById(R.id.settings_versionCode);
        tvVersionCode.setText("当前版本：" + StringUtil.getAppVersionName(this));

        View logoutBtn = footer.findViewById(R.id.settings_button_logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyAlertDialogHelper.showCommonDialog(SettingsActivity.this, null, "确定要退出吗？", "确定", "取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        logout();
                    }
                }).show();
            }
        });
    }

    private void initAdapter() {
        adapter = new SettingsAdapter(this, this, items);
        listView.setAdapter(adapter);
    }

    private void initItems() {
        items.clear();
        items.add(SettingTemplate.makeSeperator());
//        items.add(new SettingTemplate(TAG_HEAD, SettingType.TYPE_HEAD));
        pushShowNoDetailItem = new SettingTemplate(TAG_PUSH_SHOW_NO_DETAIL, "通知显示详情", SettingType.TYPE_TOGGLE, getIsShowPushNoDetail());
        items.add(pushShowNoDetailItem);
        items.add(SettingTemplate.addLine());
        disturbItem = new SettingTemplate(TAG_NO_DISTURBE, getString(R.string.no_disturb), noDisturbTime);
        items.add(disturbItem);
        items.add(SettingTemplate.addLine());


        items.add(new SettingTemplate(TAG_BLACK_LIST, getString(R.string.black_list)));

        items.add(SettingTemplate.addLine());

        notificationItem = new SettingTemplate(TAG_NOTICE, getString(R.string.msg_notice), SettingType.TYPE_TOGGLE,
                UserPreferences.getNotificationToggle());
        items.add(notificationItem);
        items.add(SettingTemplate.addLine());

        items.add(new SettingTemplate(TAG_RING, getString(R.string.ring), SettingType.TYPE_TOGGLE,
                UserPreferences.getRingToggle()));

        items.add(SettingTemplate.addLine());
//        items.add(new SettingTemplate(TAG_TEXT_SIZE, "字体大小"));

//        items.add(new SettingTemplate(TAG_VIBRATE, getString(R.string.vibrate), SettingType.TYPE_TOGGLE,
//                UserPreferences.getVibrateToggle()));

//        items.add(new SettingTemplate(TAG_LED, getString(R.string.led), SettingType.TYPE_TOGGLE,
//                UserPreferences.getLedToggle()));
//        items.add(SettingTemplate.addLine());
//        items.add(new SettingTemplate(TAG_NOTICE_CONTENT, getString(R.string.notice_content), SettingType.TYPE_TOGGLE,
//                UserPreferences.getNoticeContentToggle()));

//        items.add(new SettingTemplate(TAG_MULTIPORT_PUSH, getString(R.string.multiport_push), SettingType.TYPE_TOGGLE,
//                !NIMClient.getService(SettingsService.class).isMultiportPushOpen()));

//        items.add(SettingTemplate.makeSeperator());

//        items.add(new SettingTemplate(TAG_SPEAKER, getString(R.string.msg_speaker), SettingType.TYPE_TOGGLE,
//                NimUIKit.isEarPhoneModeEnable()));

        items.add(SettingTemplate.makeSeperator());

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            items.add(new SettingTemplate(TAG_NRTC_SETTINGS, getString(R.string.nrtc_settings)));
//            items.add(SettingTemplate.addLine());
//            items.add(new SettingTemplate(TAG_NRTC_NET_DETECT, "音视频通话网络探测"));
//            items.add(SettingTemplate.makeSeperator());
//        }
//
//        items.add(new SettingTemplate(TAG_MSG_IGNORE, "过滤通知",
//                SettingType.TYPE_TOGGLE, UserPreferences.getMsgIgnore()));

//        items.add(SettingTemplate.makeSeperator());

        // items.add(new SettingTemplate(TAG_MSG_MIGRATION, getString(R.string.local_db_migration)));

        items.add(SettingTemplate.addLine());

//        items.add(new SettingTemplate(TAG_OPINION, "意见反馈"));
        items.add(new SettingTemplate(TAG_CLEAR, "清理缓存"));
        items.add(SettingTemplate.addLine());
//        items.add(new SettingTemplate(TAG_CHECK_UPDATE,getString(R.string.check_update)));


//        clearIndexItem = new SettingTemplate(TAG_CLEAR_INDEX, getString(R.string.clear_index), getIndexCacheSize() + " M");
//        items.add(clearIndexItem);
//        items.add(SettingTemplate.addLine());
//        clearSDKDirCacheItem = new SettingTemplate(TAG_CLEAR_SDK_CACHE, getString(R.string.clear_sdk_cache), 0 + " M");
//        items.add(clearSDKDirCacheItem);

//        items.add(new SettingTemplate(TAG_CUSTOM_NOTIFY, getString(R.string.custom_notification)));
//        items.add(SettingTemplate.addLine());
//        items.add(new SettingTemplate(TAG_JS_BRIDGE, getString(R.string.js_bridge_demonstration)));
//        items.add(SettingTemplate.makeSeperator());

//        if (NIMRedPacketClient.isEnable()) {
//            items.add(new SettingTemplate(TAG_JRMFWAllET, "我的钱包"));
//            items.add(SettingTemplate.makeSeperator());
//        }
//        items.add(new SettingTemplate(TAG_PRIVATE_CONFIG, getString(R.string.setting_private_config)));

//        items.add(SettingTemplate.makeSeperator());
//        items.add(SettingTemplate.makeSeperator());
//        items.add(new SettingTemplate(TAG_ABOUT, getString(R.string.setting_about)));
    }

    private void onListItemClick(SettingTemplate item) {
        if (item == null) return;

        switch (item.getId()) {
            case TAG_HEAD:
                UserProfileSettingActivity.start(this, DemoCache.getAccount());
                break;
            case TAG_NO_DISTURBE:
                startNoDisturb();
                break;
            case TAG_CUSTOM_NOTIFY:
                CustomNotificationActivity.start(SettingsActivity.this);
                break;
            case TAG_ABOUT:
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                break;
            case TAG_CLEAR:
//                ToastHelper.showToast(SettingsActivity.this, R.string.clear_msg_success);

//                break;
//            case TAG_CLEAR_INDEX:
//                clearIndex();
//                break;
//            case TAG_CLEAR_SDK_CACHE:
                EasyAlertDialogHelper.showCommonDialog(SettingsActivity.this, null, "确定清理所有聊天记录吗？", "确定", "取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        clearSDKDirCache();
                        NIMClient.getService(MsgService.class).clearMsgDatabase(true);
                        ToastHelper.showToast(SettingsActivity.this, R.string.clear_msg_history_success);
                    }
                }).show();
                break;
            case TAG_NRTC_SETTINGS:
                AVChatKit.startAVChatSettings(SettingsActivity.this);
                break;
            case TAG_NRTC_NET_DETECT:
                netDetectForNrtc();
                break;
            case TAG_JS_BRIDGE:
                startActivity(new Intent(SettingsActivity.this, JsBridgeActivity.class));
                break;
            case TAG_JRMFWAllET:
                NIMRedPacketClient.startWalletActivity(this);
                break;

            case TAG_PRIVATE_CONFIG:
                startActivity(new Intent(this, PrivatizationConfigActivity.class));
                break;

            case TAG_MSG_MIGRATION:
                startActivity(new Intent(this, MsgMigrationActivity.class));
                break;
            case TAG_BLACK_LIST:
                BlackListActivity.start(this);
                break;
            case TAG_CHECK_UPDATE:
                String url = "http://d2.eoemarket.com/app0/68/68162/apk/1984340.apk?channel_id=426";
//                OkHttpUtil.updateApk(url,mActivity);
                break;
            case TAG_TEXT_SIZE:
                SetFontSizeActivity.start(this);
                break;
            case TAG_OPINION:
                //意见反馈
                FeedbackActivity.start(this);
                break;
            default:
                break;
        }
    }

    private void getSDKDirCacheSize() {
        List<DirCacheFileType> types = new ArrayList<>();
        types.add(DirCacheFileType.AUDIO);
        types.add(DirCacheFileType.THUMB);
        types.add(DirCacheFileType.IMAGE);
        types.add(DirCacheFileType.VIDEO);
        types.add(DirCacheFileType.OTHER);
        NIMClient.getService(MiscService.class).getSizeOfDirCache(types, 0, 0).setCallback(new RequestCallbackWrapper<Long>() {
            @Override
            public void onResult(int code, Long result, Throwable exception) {
                clearSDKDirCacheItem.setDetail(String.format("%.2f M", result / (1024.0f * 1024.0f)));
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void clearSDKDirCache() {
        List<DirCacheFileType> types = new ArrayList<>();
        types.add(DirCacheFileType.AUDIO);
        types.add(DirCacheFileType.THUMB);
        types.add(DirCacheFileType.IMAGE);
        types.add(DirCacheFileType.VIDEO);
        types.add(DirCacheFileType.OTHER);

        NIMClient.getService(MiscService.class).clearDirCache(types, 0, 0).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
//                clearSDKDirCacheItem.setDetail("0.00 M");
//                adapter.notifyDataSetChanged();
            }
        });
    }

    private void netDetectForNrtc() {
        AVChatNetDetector.startNetDetect(new AVChatNetDetectCallback() {
            @Override
            public void onDetectResult(String id,
                                       int code,
                                       int loss,
                                       int rttMax,
                                       int rttMin,
                                       int rttAvg,
                                       int mdev,
                                       String info) {
                String msg = code == 200 ?
                        ("loss:" + loss + ", rtt min/avg/max/mdev = " + rttMin + "/" + rttAvg + "/" + rttMax + "/" + mdev + " ms")
                        : ("error:" + code);
                ToastHelper.showToast(SettingsActivity.this, msg);
            }
        });
    }


    private boolean getIsShowPushNoDetail() {
        StatusBarNotificationConfig localConfig = UserPreferences.getStatusConfig();

        // 可能出现服务器和本地不一致，纠正
        boolean remoteShowNoDetail = NIMClient.getService(MixPushService.class).isPushShowNoDetail();
        if (localConfig.hideContent ^ remoteShowNoDetail) {
            updateShowPushNoDetail(!localConfig.hideContent);
        }

        return localConfig.hideContent;
    }

    private void updateShowPushNoDetail(final boolean showNoDetail) {
        NIMClient.getService(MixPushService.class).setPushShowNoDetail(showNoDetail).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                    config.hideContent = showNoDetail;
                    UserPreferences.setStatusConfig(config);
                    NIMClient.updateStatusBarNotificationConfig(config);
                    ToastHelper.showToast(SettingsActivity.this, "设置成功");
                } else {
                    pushShowNoDetailItem.setChecked(!showNoDetail);
                    adapter.notifyDataSetChanged();
                    ToastHelper.showToast(SettingsActivity.this, "设置失败");
                }
            }
        });
    }

    /**
     * 注销
     */
    private void logout() {
        SplashActivity.logout(SettingsActivity.this, false);
        SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).clear();
        finish();
        NIMClient.getService(AuthService.class).logout();
    }

    @Override
    public void onSwitchChange(SettingTemplate item, boolean checkState) {
        switch (item.getId()) {
            case TAG_NOTICE:

                if (checkState) {
                    if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {

                        EasyAlertDialogHelper.showCommonDialog(this, null, "系统中的工会小蜜信息通知设置已关闭，前往打开？", "确定", "取消", false, new EasyAlertDialogHelper.OnDialogActionListener() {
                            @Override
                            public void doCancelAction() {
                                setMessageNotify(false);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void doOkAction() {
                                setNotificationToggle(true);
                                AppUtils.gotoNotificationSetting(SettingsActivity.this);
                            }
                        }).show();

                    } else {
                        setMessageNotify(checkState);
                    }

                } else {
                    setMessageNotify(checkState);
                }


                break;
            case TAG_SPEAKER:
                NimUIKit.setEarPhoneModeEnable(checkState);
                break;
            case TAG_MSG_IGNORE:
                UserPreferences.setMsgIgnore(checkState);
                break;
            case TAG_RING: {
                UserPreferences.setRingToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.ring = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
                break;
            }
            case TAG_VIBRATE: {
                UserPreferences.setVibrateToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.vibrate = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
            }
            break;
            case TAG_LED: {
                UserPreferences.setLedToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                StatusBarNotificationConfig demoConfig = DemoCache.getNotificationConfig();
                if (checkState && demoConfig != null) {
                    config.ledARGB = demoConfig.ledARGB;
                    config.ledOnMs = demoConfig.ledOnMs;
                    config.ledOffMs = demoConfig.ledOffMs;
                } else {
                    config.ledARGB = -1;
                    config.ledOnMs = -1;
                    config.ledOffMs = -1;
                }
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
            }
            break;
            case TAG_NOTICE_CONTENT:
                UserPreferences.setNoticeContentToggle(checkState);
                StatusBarNotificationConfig config2 = UserPreferences.getStatusConfig();
                config2.titleOnlyShowAppName = checkState;
                UserPreferences.setStatusConfig(config2);
                NIMClient.updateStatusBarNotificationConfig(config2);
                break;
            case TAG_MULTIPORT_PUSH:
                updateMultiportPushConfig(!checkState);
                break;
            case TAG_NOTIFICATION_STYLE: {
                UserPreferences.setNotificationFoldedToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.notificationFolded = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
                break;
            }
            case TAG_PUSH_SHOW_NO_DETAIL:
                updateShowPushNoDetail(checkState);
                break;
            default:
                break;
        }
        item.setChecked(checkState);
    }

    private void setMessageNotify(final boolean checkState) {
        // 如果接入第三方推送（小米），则同样应该设置开、关推送提醒
        // 如果关闭消息提醒，则第三方推送消息提醒也应该关闭。
        // 如果打开消息提醒，则同时打开第三方推送消息提醒。

        notificationItem.setChecked(checkState);
        setToggleNotification(checkState);
//        ToastHelper.showToast(SettingsActivity.this, R.string.user_info_update_success);

//        NIMClient.getService(MixPushService.class).enable(checkState).setCallback(new RequestCallback<Void>() {
//            @Override
//            public void onSuccess(Void param) {
//                ToastHelper.showToast(SettingsActivity.this, R.string.user_info_update_success);
//                notificationItem.setChecked(checkState);
//                setToggleNotification(checkState);
//            }
//
//            @Override
//            public void onFailed(int code) {
//                notificationItem.setChecked(!checkState);
//                // 这种情况是客户端不支持第三方推送
//                if (code == ResponseCode.RES_UNSUPPORT) {
//                    notificationItem.setChecked(checkState);
//                    setToggleNotification(checkState);
//                } else if (code == ResponseCode.RES_EFREQUENTLY) {
//                    ToastHelper.showToast(SettingsActivity.this, R.string.operation_too_frequent);
//                } else {
//                    ToastHelper.showToast(SettingsActivity.this, R.string.user_info_update_failed);
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onException(Throwable exception) {
//
//            }
//        });
    }

    private void setToggleNotification(boolean checkState) {
        try {
            setNotificationToggle(checkState);
            NIMClient.toggleNotification(checkState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNotificationToggle(boolean on) {
        UserPreferences.setNotificationToggle(on);
    }

    private void startNoDisturb() {
        NoDisturbActivity.startActivityForResult(this, UserPreferences.getStatusConfig(), noDisturbTime, NoDisturbActivity.NO_DISTURB_REQ);
    }

    private String getIndexCacheSize() {
        long size = NIMClient.getService(LuceneService.class).getCacheSize();
        return String.format("%.2f", size / (1024.0f * 1024.0f));
    }

    private void clearIndex() {
        NIMClient.getService(LuceneService.class).clearCache();
        clearIndexItem.setDetail("0.00 M");
        adapter.notifyDataSetChanged();
    }

    private void updateMultiportPushConfig(final boolean checkState) {
        NIMClient.getService(SettingsService.class).updateMultiportPushConfig(checkState).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                ToastHelper.showToast(SettingsActivity.this, "设置成功");
            }

            @Override
            public void onFailed(int code) {
                ToastHelper.showToast(SettingsActivity.this, "设置失败,code:" + code);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case NoDisturbActivity.NO_DISTURB_REQ:
                    setNoDisturbTime(data);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置免打扰时间
     *
     * @param data
     */
    private void setNoDisturbTime(Intent data) {
        boolean isChecked = data.getBooleanExtra(NoDisturbActivity.EXTRA_ISCHECKED, false);
        noDisturbTime = getString(R.string.setting_close);
        StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
        if (isChecked) {
            config.downTimeBegin = data.getStringExtra(NoDisturbActivity.EXTRA_START_TIME);
            config.downTimeEnd = data.getStringExtra(NoDisturbActivity.EXTRA_END_TIME);
            noDisturbTime = String.format("%s到%s", config.downTimeBegin, config.downTimeEnd);
        } else {
            config.downTimeBegin = null;
            config.downTimeEnd = null;
        }
        disturbItem.setDetail(noDisturbTime);
        adapter.notifyDataSetChanged();
        UserPreferences.setDownTimeToggle(isChecked);
        config.downTimeToggle = isChecked;
        UserPreferences.setStatusConfig(config);
        NIMClient.updateStatusBarNotificationConfig(config);
    }
}

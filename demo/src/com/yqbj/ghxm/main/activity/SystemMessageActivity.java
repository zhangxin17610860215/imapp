package com.yqbj.ghxm.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.adapter.TAdapterDelegate;
import com.netease.yqbj.uikit.common.adapter.TViewHolder;
import com.netease.yqbj.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.yqbj.uikit.common.ui.listview.AutoRefreshListView;
import com.netease.yqbj.uikit.common.ui.listview.ListViewUtil;
import com.netease.yqbj.uikit.common.ui.listview.MessageListView;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.main.adapter.SystemMessageAdapter;
import com.yqbj.ghxm.main.viewholder.SystemMessageViewHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统消息中心界面
 * <p/>
 * Created by huangjun on 2015/3/18.
 */
public class SystemMessageActivity extends UI implements TAdapterDelegate,
        AutoRefreshListView.OnRefreshListener, SystemMessageViewHolder.SystemMessageListener {

    private static final boolean MERGE_ADD_FRIEND_VERIFY = true; // 是否要合并好友申请，同一个用户仅保留最近一条申请内容（默认不合并）

    private static final int LOAD_MESSAGE_COUNT = 10;
    private static final String TAG = "SystemMessageActivity";

    // view
    private MessageListView listView;

    // adapter
    private SystemMessageAdapter adapter;
    private List<SystemMessage> items = new ArrayList<>();
    private Set<Long> itemIds = new HashSet<>();

    // db
    private boolean firstLoad = true;

    private LinearLayout llNodata;

    public static void start(Context context) {
        start(context, null, true);
    }

    public static void start(Context context, Intent extras, boolean clearTop) {
        Intent intent = new Intent();
        intent.setClass(context, SystemMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return SystemMessageViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }


    @Override
    public void onRefreshFromStart() {
    }

    @Override
    public void onRefreshFromEnd() {
        loadMessages(); // load old data
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.system_notification_message_activity);

        onInitSetBack(SystemMessageActivity.this);
        onInitSetTitle(SystemMessageActivity.this, getString(R.string.verify_reminder));
        onInitRightSure(SystemMessageActivity.this, 0, "清空", 16);

        initView();

        initAdapter();
        initListView();

        loadMessages(); // load old data
        registerSystemObserver(true);
    }

    @Override
    public void setRightSureClick() {
        comRightSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllMessages();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }

    @Override
    protected void onStop() {
        super.onStop();

        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registerSystemObserver(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification_menu_btn:
                deleteAllMessages();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        adapter = new SystemMessageAdapter(this, items, this, this);
    }

    private void initListView() {
        listView = (MessageListView) findViewById(R.id.messageListView);
        listView.setMode(AutoRefreshListView.Mode.END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        // adapter
        listView.setAdapter(adapter);

        // listener
        listView.setOnRefreshListener(this);
    }

    private void initView() {
        llNodata = findView(R.id.ll_nodata);
        TextView nullContent = findView(R.id.tv_noData_content);
        nullContent.setText(getString(R.string.no_msg));
    }

    private int loadOffset = 0;
    private Set<String> addFriendVerifyRequestAccounts = new HashSet<>(); // 发送过好友申请的账号（好友申请合并用）
    private Set<String> addTeamVerifyRequestAccounts = new HashSet<>();

    /**
     * 加载历史消息
     */
    public void loadMessages() {
        listView.onRefreshStart(AutoRefreshListView.Mode.END);
        boolean loadCompleted; // 是否已经加载完成，后续没有数据了or已经满足本次请求数量
        int validMessageCount = 0; // 实际加载的数量（排除被过滤被合并的条目）
        boolean isShowLlNodata;//是否显示没有数据的UI
        List<String> messageFromAccounts = new ArrayList<>(LOAD_MESSAGE_COUNT);

        while (true) {
            List<SystemMessage> temps = NIMClient.getService(SystemMessageService.class)
                    .querySystemMessagesBlock(loadOffset, LOAD_MESSAGE_COUNT);
            isShowLlNodata = temps.size() > 0;
            loadOffset += temps.size();
            loadCompleted = temps.size() < LOAD_MESSAGE_COUNT;

            int tempValidCount = 0;

            for (SystemMessage m : temps) {
                // 去重
                if (duplicateFilter(m)) {
                    continue;
                }

                // 同一个账号的好友申请仅保留最近一条
                if (addFriendVerifyFilter(m)) {
                    continue;
                }

                // 同一个账号的好友申请入群
                if (addTeamVerifyFilter(m)) {
                    continue;
                }

                // 保存有效消息
                items.add(m);
                tempValidCount++;
                if (!messageFromAccounts.contains(m.getFromAccount())) {
                    messageFromAccounts.add(m.getFromAccount());
                }

                // 判断是否达到请求数
                if (++validMessageCount >= LOAD_MESSAGE_COUNT) {
                    loadCompleted = true;
                    // 已经满足要求，此时需要修正loadOffset
                    loadOffset -= temps.size();
                    loadOffset += tempValidCount;

                    break;
                }
            }

            if (loadCompleted) {
                break;
            }
        }

        // 更新数据源，刷新界面
        refresh();

        boolean first = firstLoad;
        firstLoad = false;
        if (first) {
            ListViewUtil.scrollToPosition(listView, 0, 0); // 第一次加载后显示顶部
        }

        listView.onRefreshComplete(validMessageCount, LOAD_MESSAGE_COUNT, true);
        boolean isShow = loadOffset > 0;
        comRightSure.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        llNodata.setVisibility(isShowLlNodata ? View.GONE : View.VISIBLE);
        // 收集未知用户资料的账号集合并从远程获取
        collectAndRequestUnknownUserInfo(messageFromAccounts);
    }

    /**
     * 新消息到来
     */
    private void onIncomingMessage(final SystemMessage message) {
        // 同一个账号的好友申请仅保留最近一条
        if (addFriendVerifyFilter(message)) {
            SystemMessage del = null;
            for (SystemMessage m : items) {
                if (m.getFromAccount().equals(message.getFromAccount()) && m.getType() == SystemMessageType.AddFriend) {
                    AddFriendNotify attachData = (AddFriendNotify) m.getAttachObject();
                    if (attachData != null && attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                        del = m;
                        break;
                    }
                }
            }

            if (del != null) {
                items.remove(del);
            }
        }

        if(addTeamVerifyFilter(message))
        {
            SystemMessage del = null;
            for (SystemMessage m : items) {
//                message.getStatus() == SystemMessageStatus.init &&
                if ( m.getType() == SystemMessageType.ApplyJoinTeam) {
                   if (message.getTargetId().equals(m.getTargetId()) && message.getFromAccount().equals(m.getFromAccount()))
                   {
                       del = m;
                       break;
                   }
                }
            }
            if (del != null) {
                items.remove(del);
            }
        }

        loadOffset++;
        items.add(0, message);

        // 只有拉人入群才可以设置自定义字段
        String customInfo = message.getCustomInfo();
        Log.e(TAG, "system message , customInfo = " + customInfo);

        // 获取系统通知的内容。例如：申请附言，拒绝理由
        String content = message.getContent();
        Log.e(TAG, "system message , content = " + content);

        refresh();
        // 收集未知用户资料的账号集合并从远程获取
        List<String> messageFromAccounts = new ArrayList<>(1);
        messageFromAccounts.add(message.getFromAccount());
        collectAndRequestUnknownUserInfo(messageFromAccounts);
    }


    // 去重
    private boolean duplicateFilter(final SystemMessage msg) {
        if (itemIds.contains(msg.getMessageId())) {
            return true;
        }

        itemIds.add(msg.getMessageId());
        return false;
    }

    // 同一个账号的好友申请仅保留最近一条
    private boolean addFriendVerifyFilter(final SystemMessage msg) {
        if (!MERGE_ADD_FRIEND_VERIFY) {
            return false; // 不需要MERGE，不过滤
        }

        if (msg.getType() != SystemMessageType.AddFriend) {
            return false; // 不过滤
        }

        AddFriendNotify attachData = (AddFriendNotify) msg.getAttachObject();
        if (attachData == null) {
            NIMClient.getService(SystemMessageService.class).deleteSystemMessage(msg.getMessageId());
            return true; // 过滤
        }

        if (attachData.getEvent() != AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
            return false; // 不过滤
        }

        if (addFriendVerifyRequestAccounts.contains(msg.getFromAccount())) {
            NIMClient.getService(SystemMessageService.class).deleteSystemMessage(msg.getMessageId());
            return true; // 过滤
        }

        addFriendVerifyRequestAccounts.add(msg.getFromAccount());
        return false; // 不过滤
    }

    // 同一个账号的一个群申请仅保留最近一条
    private boolean addTeamVerifyFilter(final SystemMessage msg) {

        if (msg.getType() != SystemMessageType.ApplyJoinTeam) {
            return false; // 不过滤
        }

//        if(msg.getStatus() != SystemMessageStatus.init)
//        {
//            return false;
//        }

        String team_account = msg.getTargetId() + "_" + msg.getFromAccount();
        if (addTeamVerifyRequestAccounts.contains(team_account)) {
            NIMClient.getService(SystemMessageService.class).deleteSystemMessage(msg.getMessageId());
            return true; // 过滤
        }

        addTeamVerifyRequestAccounts.add(team_account);
        return false; // 不过滤
    }

    // 请求未知的用户资料
    private void collectAndRequestUnknownUserInfo(List<String> messageFromAccounts) {
        List<String> unknownAccounts = new ArrayList<>();
        for (String account : messageFromAccounts) {
            if (NimUIKit.getUserInfoProvider().getUserInfo(account) == null) {
                unknownAccounts.add(account);
            }
        }

        if (!unknownAccounts.isEmpty()) {
            requestUnknownUser(unknownAccounts);
        }
    }

    @Override
    public void onAgree(SystemMessage message) {
        onSystemNotificationDeal(message, true);
    }

    @Override
    public void onReject(SystemMessage message) {
        onSystemNotificationDeal(message, false);
    }

    @Override
    public void onLongPressed(SystemMessage message) {
        showLongClickMenu(message);
    }

    private void onSystemNotificationDeal(final SystemMessage message, final boolean pass) {
        RequestCallback callback = new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                onProcessSuccess(pass, message);
            }

            @Override
            public void onFailed(int code) {
                onProcessFailed(code, message);
            }

            @Override
            public void onException(Throwable exception) {

            }
        };
        if (message.getType() == SystemMessageType.TeamInvite) {
            if (pass) {
                NIMClient.getService(TeamService.class).acceptInvite(message.getTargetId(), message.getFromAccount()).setCallback(callback);
            } else {
                NIMClient.getService(TeamService.class).declineInvite(message.getTargetId(), message.getFromAccount(), "").setCallback(callback);
            }

        } else if (message.getType() == SystemMessageType.ApplyJoinTeam) {
            if (pass) {
                NIMClient.getService(TeamService.class).passApply(message.getTargetId(), message.getFromAccount()).setCallback(callback);
            } else {
                NIMClient.getService(TeamService.class).rejectApply(message.getTargetId(), message.getFromAccount(), "").setCallback(callback);
            }
        } else if (message.getType() == SystemMessageType.AddFriend) {
            NIMClient.getService(FriendService.class).ackAddFriendRequest(message.getFromAccount(), pass).setCallback(callback);
        }
    }

    private void onProcessSuccess(final boolean pass, SystemMessage message) {
        SystemMessageStatus status = pass ? SystemMessageStatus.passed : SystemMessageStatus.declined;
        NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(),
                status);
        message.setStatus(status);
        refreshViewHolder(message);
    }

    private void onProcessFailed(final int code, SystemMessage message) {
        // 809 已过期
        if (code == 809) {
            return;
        }
        ToastHelper.showToastLong(SystemMessageActivity.this, "failed, error code=" + code);
        if (code == 408) {
            return;
        }

        SystemMessageStatus status = SystemMessageStatus.expired;
        NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(),
                status);
        message.setStatus(status);
        refreshViewHolder(message);
    }

    private void refresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isShow = items.size() > 0;
                comRightSure.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
                llNodata.setVisibility(isShow ? View.GONE : View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshViewHolder(final SystemMessage message) {
        final long messageId = message.getMessageId();

        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            SystemMessage item = items.get(i);
            if (messageId == item.getMessageId()) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            return;
        }

        final int m = index;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m < 0) {
                    return;
                }

                Object tag = ListViewUtil.getViewHolderByIndex(listView, m);
                if (tag instanceof SystemMessageViewHolder) {
                    ((SystemMessageViewHolder) tag).refreshDirectly(message);
                }
            }
        });
    }

    private void registerSystemObserver(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(systemMessageObserver, register);
    }

    Observer<SystemMessage> systemMessageObserver = new Observer<SystemMessage>() {
        @Override
        public void onEvent(SystemMessage systemMessage) {
            onIncomingMessage(systemMessage);
        }
    };

    private void requestUnknownUser(List<String> accounts) {
        NimUIKit.getUserInfoProvider().getUserInfoAsync(accounts, new SimpleCallback<List<NimUserInfo>>() {
            @Override
            public void onResult(boolean success, List<NimUserInfo> result, int code) {
                if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && !result.isEmpty()) {
                        refresh();
                    }
                }
            }
        });
    }

    private void deleteAllMessages() {
        NIMClient.getService(SystemMessageService.class).clearSystemMessages();
        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
        items.clear();
        refresh();
        ToastHelper.showToast(SystemMessageActivity.this, R.string.clear_all_success);
    }

    private void showLongClickMenu(final SystemMessage message) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.setTitle(R.string.delete_tip);
        String title = getString(R.string.delete_system_message);
        alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                deleteSystemMessage(message);
            }
        });
        alertDialog.show();
    }

    private void deleteSystemMessage(final SystemMessage message) {
        NIMClient.getService(SystemMessageService.class).deleteSystemMessage(message.getMessageId());
        items.remove(message);
        refresh();
        ToastHelper.showToast(SystemMessageActivity.this, R.string.delete_success);
    }
}

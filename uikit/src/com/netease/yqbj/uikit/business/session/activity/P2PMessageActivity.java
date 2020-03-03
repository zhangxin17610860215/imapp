package com.netease.yqbj.uikit.business.session.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.contact.ContactChangedObserver;
import com.netease.yqbj.uikit.api.model.main.OnlineStateChangeObserver;
import com.netease.yqbj.uikit.api.model.session.SessionCustomization;
import com.netease.yqbj.uikit.api.model.user.UserInfoObserver;
import com.netease.yqbj.uikit.business.session.constant.Extras;
import com.netease.yqbj.uikit.business.session.fragment.MessageFragment;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.netease.yqbj.uikit.common.ModuleUIComFn;
import com.netease.yqbj.uikit.impl.NimUIKitImpl;

import java.util.List;
import java.util.Set;

import static com.netease.yqbj.uikit.api.StatisticsConstants.ROBOT_IDS;


/**
 * 点对点聊天界面
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class P2PMessageActivity extends BaseMessageActivity {

    private boolean isResume = false;

    private SessionCustomization customization;

    public static void start(Context context, String contactId, SessionCustomization customization, IMMessage anchor) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, contactId);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        if (anchor != null) {
            intent.putExtra(Extras.EXTRA_ANCHOR, anchor);
        }
        intent.setClass(context, P2PMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 单聊特例话数据，包括个人信息，
        customization = (SessionCustomization) getIntent().getSerializableExtra(Extras.EXTRA_CUSTOMIZATION);
//        if(customization!=null&&!customization.showRight){
//            comRightSure.setVisibility(View.GONE);
//        }
        requestBuddyInfo();
        displayOnlineState();
        registerObservers(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isResume = false;
    }

    private void requestBuddyInfo() {
//        setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        onInitSetTitle(this, UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));

    }

    private void displayOnlineState() {
        if (!NimUIKitImpl.enableOnlineState()) {
            return;
        }
        String detailContent = NimUIKitImpl.getOnlineStateContentProvider().getDetailDisplay(sessionId);
        setSubTitle(detailContent);
    }


    /**
     * 命令消息接收观察者
     */
    private Observer<CustomNotification> commandObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification message) {
            if (!sessionId.equals(message.getSessionId()) || message.getSessionType() != SessionTypeEnum.P2P) {
                return;
            }
            showCommandMessage(message);
        }
    };


    /**
     * 用户信息变更观察者
     */
    private UserInfoObserver userInfoObserver = new UserInfoObserver() {
        @Override
        public void onUserInfoChanged(List<String> accounts) {
            if (!accounts.contains(sessionId)) {
                return;
            }
            requestBuddyInfo();
        }
    };

    /**
     * 好友资料变更（eg:关系）
     */
    private ContactChangedObserver friendDataChangedObserver = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
//            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
            onInitSetTitle(P2PMessageActivity.this, UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onDeletedFriends(List<String> accounts) {
//            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
            onInitSetTitle(P2PMessageActivity.this, UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
//            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
            onInitSetTitle(P2PMessageActivity.this, UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
//            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
            onInitSetTitle(P2PMessageActivity.this, UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }
    };

    /**
     * 好友在线状态观察者
     */
    private OnlineStateChangeObserver onlineStateChangeObserver = new OnlineStateChangeObserver() {
        @Override
        public void onlineStateChange(Set<String> accounts) {
            if (!accounts.contains(sessionId)) {
                return;
            }
            // 按照交互来展示
            displayOnlineState();
        }
    };

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(commandObserver, register);
        NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, register);
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register);
        if (NimUIKit.enableOnlineState()) {
            NimUIKit.getOnlineStateChangeObservable().registerOnlineStateChangeListeners(onlineStateChangeObserver, register);
        }
    }


    protected void showCommandMessage(CustomNotification message) {
        if (!isResume) {
            return;
        }
        String content = message.getContent();
        try {
            JSONObject json = JSON.parseObject(content);
            int id = json.getIntValue("id");
            if (id == 1) {
                // 正在输入
//                ToastHelper.showToastLong(P2PMessageActivity.this, "对方正在输入...");
            } else {
//                ToastHelper.showToast(P2PMessageActivity.this, "command: " + content);
//                Toast.makeText(P2PMessageActivity.this,"command: " + content);
//                Toast.makeText(P2PMessageActivity.this,"command: "+ content,Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    protected MessageFragment fragment() {
        Bundle arguments = getIntent().getExtras();
        arguments.putSerializable(Extras.EXTRA_TYPE, SessionTypeEnum.P2P);
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        return fragment;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.nim_message_activity;
    }

    @Override
    protected void initToolBar() {
//        ToolBarOptions options = new NimToolBarOptions();
//        setToolBar(R.id.toolbar, options);
        onInitSetBack(this);
        String accontId = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        if (!ROBOT_IDS.contains(accontId)){
            onInitRightSure(this, R.drawable.chat_more_icon, "", 15);
        }
    }

    @Override
    public void setRightSureClick() {
        comRightSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModuleUIComFn.getInstance().toPersonChatMsgClick(P2PMessageActivity.this, sessionId);
            }
        });
    }

    @Override
    protected boolean enableSensor() {
        return true;
    }
}

package com.yqbj.ghxm.session.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.netease.yqbj.uikit.business.session.module.Container;
import com.netease.yqbj.uikit.business.session.module.ModuleProxy;
import com.netease.yqbj.uikit.business.session.module.list.MessageListPanelEx;

/**
 * 消息历史查询界面
 * <p/>
 * Created by huangjun on 2015/4/17.
 */
public class MessageHistoryActivity extends BaseAct implements ModuleProxy {

    private static final String EXTRA_DATA_ACCOUNT = "EXTRA_DATA_ACCOUNT";
    private static final String EXTRA_DATA_SESSION_TYPE = "EXTRA_DATA_SESSION_TYPE";

    // context
    private SessionTypeEnum sessionType;
    private String account; // 对方帐号

    private MessageListPanelEx messageListPanel;

    public static void start(Context context, String account, SessionTypeEnum sessionType) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA_ACCOUNT, account);
        intent.putExtra(EXTRA_DATA_SESSION_TYPE, sessionType);
        intent.setClass(context, MessageHistoryActivity.class);
        context.startActivity(intent);
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = LayoutInflater.from(this).inflate(R.layout.message_history_activity, null);
        setContentView(rootView);

        setToolbar(R.drawable.jrmf_b_top_back,"历史聊天记录");

        onParseIntent();

        Container container = new Container(this, account, sessionType, this);
        messageListPanel = new MessageListPanelEx(container, rootView, true, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        messageListPanel.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        messageListPanel.onDestroy();
    }

    protected void onParseIntent() {
        account = getIntent().getStringExtra(EXTRA_DATA_ACCOUNT);
        sessionType = (SessionTypeEnum) getIntent().getSerializableExtra(EXTRA_DATA_SESSION_TYPE);
    }

    @Override
    public boolean sendMessage(IMMessage msg) {
        return false;
    }

    @Override
    public void onInputPanelExpand() {

    }

    @Override
    public void shouldCollapseInputPanel() {

    }

    @Override
    public void onItemFooterClick(IMMessage message) {

    }

    @Override
    public boolean isLongClickEnabled() {
        return true;
    }

    @Override
    public void lockContentHeight() {

    }

    @Override
    public void unlockContentHeightDelayed() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (messageListPanel != null) {
            messageListPanel.onActivityResult(requestCode, resultCode, data);
        }
    }
}

package com.netease.yqbj.uikit.business.session.module;

import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 会话窗口提供给子模块的代理接口。
 */
public interface ModuleProxy {
    // 发送消息
    boolean sendMessage(IMMessage msg);

    // 消息输入区展开时候的处理
    void onInputPanelExpand();

    // 应当收起输入区
    void shouldCollapseInputPanel();

    // 是否正在录音
    boolean isLongClickEnabled();

    /**
     * 锁定内容高度，防止跳闪
     */
    void lockContentHeight();
    /**
     * 释放被锁定的内容高度
     */
    void unlockContentHeightDelayed();


    void onItemFooterClick(IMMessage message);
}

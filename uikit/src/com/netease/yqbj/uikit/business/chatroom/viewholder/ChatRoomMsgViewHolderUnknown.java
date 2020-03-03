package com.netease.yqbj.uikit.business.chatroom.viewholder;

import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

/**
 * Created by huangjun on 2016/12/27.
 */
public class ChatRoomMsgViewHolderUnknown extends ChatRoomMsgViewHolderBase {

    public ChatRoomMsgViewHolderUnknown(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_unknown;
    }

    @Override
    protected boolean isShowHeadImage() {
        if (message.getSessionType() == SessionTypeEnum.ChatRoom) {
            return false;
        }
        return true;
    }

    @Override
    protected void inflateContentView() {
    }

    @Override
    protected void bindContentView() {
    }
}

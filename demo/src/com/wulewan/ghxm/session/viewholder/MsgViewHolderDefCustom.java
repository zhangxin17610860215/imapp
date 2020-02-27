package com.wulewan.ghxm.session.viewholder;

import com.wulewan.ghxm.session.extension.DefaultCustomAttachment;
import com.netease.wulewan.uikit.business.session.viewholder.MsgViewHolderText;
import com.netease.wulewan.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * Created by zhoujianghua on 2015/8/4.
 */
public class MsgViewHolderDefCustom extends MsgViewHolderText {

    public MsgViewHolderDefCustom(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected String getDisplayText() {
        DefaultCustomAttachment attachment = (DefaultCustomAttachment) message.getAttachment();

        return "type: " + attachment.getType() + ", data: " + attachment.getContent();
    }
}

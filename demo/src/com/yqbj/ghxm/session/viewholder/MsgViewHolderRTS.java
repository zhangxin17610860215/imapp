package com.yqbj.ghxm.session.viewholder;

import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.session.extension.RTSAttachment;
import com.netease.yqbj.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.yqbj.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

public class MsgViewHolderRTS extends MsgViewHolderBase {

    private TextView textView;

    public MsgViewHolderRTS(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_rts;
    }

    @Override
    protected void inflateContentView() {
        textView = (TextView) view.findViewById(R.id.rts_text);
    }

    @Override
    protected void bindContentView() {
        RTSAttachment attachment = (RTSAttachment) message.getAttachment();
        textView.setText(attachment.getContent());
    }
}


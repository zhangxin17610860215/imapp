package com.netease.wulewan.uikit.business.session.viewholder;

import com.netease.wulewan.uikit.R;
import com.netease.wulewan.uikit.business.session.activity.WatchMessagePictureActivity;
import com.netease.wulewan.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * Created by zhoujianghua on 2015/8/4.
 */
public class MsgViewHolderPicture extends MsgViewHolderThumbBase {

    public MsgViewHolderPicture(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_picture;
    }

    @Override
    protected void onItemClick() {
        WatchMessagePictureActivity.start(context, message);
    }

    @Override
    protected String thumbFromSourceFile(String path) {
        return path;
    }

    @Override
    protected boolean showWhiteBG() {
        return false;
    }

    @Override
    protected boolean isShowBubbleBG() {
        return false;
    }
}

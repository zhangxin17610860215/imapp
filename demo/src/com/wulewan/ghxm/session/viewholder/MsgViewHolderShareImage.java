package com.wulewan.ghxm.session.viewholder;


import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.session.extension.ShareImageAttachment;
import com.netease.wulewan.uikit.business.session.activity.WatchMessagePictureActivity;
import com.netease.wulewan.uikit.business.session.viewholder.MsgViewHolderPicture;
import com.netease.wulewan.uikit.common.ui.imageview.MsgThumbImageView;
import com.netease.wulewan.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * 会话中显示分享界面
 */
public class MsgViewHolderShareImage extends MsgViewHolderPicture {

    private TextView appName;
    private MsgThumbImageView appIcon;

    public MsgViewHolderShareImage(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.share_img;
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        appName = view.findViewById(R.id.app_name);
        appIcon = view.findViewById(R.id.app_icon);
    }

    @Override
    protected void bindContentView() {
        super.bindContentView();
        ShareImageAttachment attachment = (ShareImageAttachment) message.getAttachment();
        appName.setText(attachment.getAppName());
        appIcon.loadImage(attachment.getAppIconUrl(), 40, 40, R.drawable.mask_square);
    }


    // 内容区域点击事件响应处理。
    protected void onItemClick() {
        WatchMessagePictureActivity.start(context, message);
    }


    // 是否显示气泡背景，默认为显示
    protected boolean isShowBubbleBG() {
        return false;
    }
}

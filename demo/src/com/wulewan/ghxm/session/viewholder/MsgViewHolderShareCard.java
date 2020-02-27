package com.wulewan.ghxm.session.viewholder;

import android.content.Intent;
import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.session.extension.ShareCardAttachment;
import com.wulewan.ghxm.utils.MainWebViewActivity;
import com.netease.wulewan.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.wulewan.uikit.common.ui.imageview.MsgThumbImageView;
import com.netease.wulewan.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

public class MsgViewHolderShareCard extends MsgViewHolderBase {


    private TextView appName;
    private TextView title;
    private TextView content;


    private MsgThumbImageView appIcon;
    private MsgThumbImageView imgView;

    public MsgViewHolderShareCard(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {

        return R.layout.share_card;
    }

    @Override
    protected void inflateContentView() {
        // 分享链接需要用白色的背景，那左边的背景进行水平翻转了。
        if (!isReceivedMessage()) {
            view.findViewById(R.id.share_card_layout).setScaleX(-1);
        }
        appName = view.findViewById(R.id.app_name);
        title = view.findViewById(R.id.title);
        content = view.findViewById(R.id.content);
        appIcon = view.findViewById(R.id.app_icon);
        imgView = view.findViewById(R.id.message_item_thumb_thumbnail);
    }

    @Override
    protected void bindContentView() {
        ShareCardAttachment attachment = (ShareCardAttachment) message.getAttachment();
        appName.setText(attachment.getAppName());
        title.setText(attachment.getTitle());
        content.setText(attachment.getContent());
        appIcon.loadImage(attachment.getAppIconUrl(), 100, 100, R.drawable.mask_square);
        imgView.loadImage(attachment.getAppIconUrl(), 100, 100, R.drawable.mask_square);
    }

    // 内容区域点击事件响应处理。
    protected void onItemClick() {
        Intent intent = new Intent(context, MainWebViewActivity.class);
        ShareCardAttachment attachment = (ShareCardAttachment) message.getAttachment();
        intent.putExtra("url", attachment.getUrl());
        context.startActivity(intent);
    }

    // 右边显示白色背景
    protected boolean showWhiteBG() {
        return true;
    }
}

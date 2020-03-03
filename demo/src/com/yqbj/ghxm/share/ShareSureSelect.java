package com.yqbj.ghxm.share;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lxj.xpopup.core.CenterPopupView;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.yqbj.ghxm.R;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.ui.imageview.MsgThumbImageView;
import com.yqbj.ghxm.session.extension.ShareCardAttachment;
import com.yqbj.ghxm.session.extension.ShareImageAttachment;



public class ShareSureSelect extends CenterPopupView implements View.OnClickListener {

    private final String headUrl;
    private final String userName;
    private final MsgAttachment shareAttachment;
    private Context context;
    private EditText leave_message;



    public ShareSureSelect(@NonNull Context context,String headUrl,String userName,MsgAttachment shareAttachment) {
        super(context);
        this.context = context;
        this.headUrl = headUrl;
        this.userName = userName;
        this.shareAttachment = shareAttachment;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.share_sure_select;
    }


    @Override
    public void init() {
        super.init();
        initView();
    }

    private void initView() {
        HeadImageView user_icon = findViewById(R.id.user_icon);
        TextView user_name = findViewById(R.id.user_name);
        user_name.setText(this.userName);
        user_icon.loadAvatar(this.headUrl);
        MsgThumbImageView img_icon = findViewById(R.id.img_icon);
        leave_message = findViewById(R.id.leave_message);
        if (shareAttachment instanceof ShareImageAttachment)
        {
            findViewById(R.id.web_content).setVisibility(View.GONE);
            ShareImageAttachment shareimg = ((ShareImageAttachment) shareAttachment);
            int w = shareimg.getWidth();
            int h = shareimg.getHeight();
            int max = 500;
            if (w > h) {
                h = max * h / w;
                w = max;
            } else {
                w = max * w / h;
                h = max;
            }
            img_icon.loadAsPath(shareimg.getPath(),w,h,R.drawable.mask_round_square,"png");
        }else {
            findViewById(R.id.img_conetent).setVisibility(View.GONE);
            TextView web_title = findViewById(R.id.web_title);
            web_title.setText(((ShareCardAttachment) shareAttachment).getDesc());
        }
        TextView close_btn = findViewById(R.id.close_btn);
        close_btn.setOnClickListener(this);

        TextView btn_sure = findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sureListener != null) {
                    sureListener.onClick(v);
                }
            }
        });
    }

    private View.OnClickListener sureListener;
    public void setOnClickListenerOnSure(View.OnClickListener sureListener) {
        this.sureListener = sureListener;
    }

    public String getLeave_message() {
        String msg = leave_message.getText().toString();
        return msg;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sure:
            case R.id.close_btn:
                dismiss();
                break;
        }

    }
}

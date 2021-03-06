package com.yqbj.ghxm.main.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.main.helper.MessageHelper;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.netease.yqbj.uikit.common.adapter.TViewHolder;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.util.sys.TimeUtil;
import com.netease.yqbj.uikit.impl.NimUIKitImpl;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangjun on 2015/3/18.
 */
public class SystemMessageViewHolder extends TViewHolder {

    private SystemMessage message;
    private HeadImageView headImageView;
    private TextView fromAccountText;
    private TextView timeText;
    private TextView contentText;
    private View operatorLayout;
    private Button agreeButton;
    private Button rejectButton;
    private TextView operatorResultText;
    private SystemMessageListener listener;

    public interface SystemMessageListener {
        void onAgree(SystemMessage message);

        void onReject(SystemMessage message);

        void onLongPressed(SystemMessage message);
    }

    @Override
    protected int getResId() {
        return R.layout.message_system_notification_view_item;
    }

    @Override
    protected void inflate() {
        headImageView = (HeadImageView) view.findViewById(R.id.from_account_head_image);
        fromAccountText = (TextView) view.findViewById(R.id.from_account_text);
        contentText = (TextView) view.findViewById(R.id.content_text);
        timeText = (TextView) view.findViewById(R.id.notification_time);
        operatorLayout = view.findViewById(R.id.operator_layout);
        agreeButton = (Button) view.findViewById(R.id.agree);
        rejectButton = (Button) view.findViewById(R.id.reject);
        operatorResultText = (TextView) view.findViewById(R.id.operator_result);
        view.setBackgroundResource(R.drawable.nim_list_item_bg_selecter);
    }

    @Override
    protected void refresh(Object item) {
        message = (SystemMessage) item;
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onLongPressed(message);
                }

                return true;
            }
        });
        UserInfo userInfo;
        if (message.getType() == SystemMessageType.TeamInvite){
            try {
                JSONObject attach = new JSONObject(message.getAttach());
                if (attach.has("attach")){
                    String attachStr = attach.get("attach").toString();
                    JSONObject attachJson = new JSONObject(attachStr);
                    String inviter = attachJson.getString(StatisticsConstants.INVITER);
                    userInfo = NimUIKitImpl.getUserInfoProvider().getUserInfo(inviter);
                }else {
                    userInfo = NimUIKitImpl.getUserInfoProvider().getUserInfo(message.getFromAccount());
                }
            } catch (Exception e) {
                e.printStackTrace();
                userInfo = NimUIKitImpl.getUserInfoProvider().getUserInfo(message.getFromAccount());
            }
        }else {
            userInfo = NimUIKitImpl.getUserInfoProvider().getUserInfo(message.getFromAccount());
        }
        headImageView.loadAvatar(userInfo.getAvatar());
        fromAccountText.setText(UserInfoHelper.getUserDisplayNameEx(userInfo.getAccount(), "我"));
        contentText.setText(MessageHelper.getVerifyNotificationText(message));
        timeText.setText(TimeUtil.getTimeShowString(message.getTime(), false));
        if (!MessageHelper.isVerifyMessageNeedDeal(message)) {
            operatorLayout.setVisibility(View.GONE);
        } else {
            if (message.getStatus() == SystemMessageStatus.init) {
                // 未处理
                operatorResultText.setVisibility(View.GONE);
                operatorLayout.setVisibility(View.VISIBLE);
                agreeButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
            } else {
                // 处理结果
                agreeButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
                operatorResultText.setVisibility(View.VISIBLE);
                operatorResultText.setText(MessageHelper.getVerifyNotificationDealResult(message));
            }
        }
    }

    public void refreshDirectly(final SystemMessage message) {
        if (message != null) {
            refresh(message);
        }
    }

    public void setListener(final SystemMessageListener l) {
        if (l == null) {
            return;
        }

        this.listener = l;
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReplySending();
                listener.onAgree(message);
            }
        });
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReplySending();
                listener.onReject(message);
            }
        });
    }

    /**
     * 等待服务器返回状态设置
     */
    private void setReplySending() {
        agreeButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
        operatorResultText.setVisibility(View.VISIBLE);
        operatorResultText.setText(R.string.team_apply_sending);
    }
}

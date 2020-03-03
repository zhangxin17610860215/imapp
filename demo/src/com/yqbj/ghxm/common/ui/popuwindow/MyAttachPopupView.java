package com.yqbj.ghxm.common.ui.popuwindow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.lxj.xpopup.core.AttachPopupView;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.zxing.ZXingUtils;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.yqbj.ghxm.contact.activity.AddFriendActivity;

/**
 * 自定义会话页面加好Popuwindow背景
 */
public class MyAttachPopupView extends AttachPopupView implements View.OnClickListener {
    private Context context;
    private static final int REQUEST_CODE_NORMAL = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;

    public MyAttachPopupView(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.custom_attach_popup;
    }

    @Override
    public void init() {
        super.init();
        initView();
    }

    private void initView() {
        TextView tv_chat_GroupChat = findViewById(R.id.tv_chat_GroupChat);
        TextView tv_chat_AddFriend = findViewById(R.id.tv_chat_AddFriend);
        TextView tv_chat_QRCode = findViewById(R.id.tv_chat_QRCode);
        TextView tv_chat_Scan = findViewById(R.id.tv_chat_Scan);
        tv_chat_GroupChat.setOnClickListener(this);
        tv_chat_AddFriend.setOnClickListener(this);
        tv_chat_QRCode.setOnClickListener(this);
        tv_chat_Scan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_chat_GroupChat:
                //发起群聊

                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null,50);
//                advancedOption.isMulti = true;

//                if (null != StatisticsConstants.ROBOT_IDS && StatisticsConstants.ROBOT_IDS.size() > 0){
//                    ArrayList<String> disableAccounts = new ArrayList<>();
//                    for (String id : StatisticsConstants.ROBOT_IDS){
//                        disableAccounts.add(id);
//                        advancedOption.itemFilter = new ContactIdFilter(disableAccounts);
//                    }
//                }

                NimUIKit.startContactSelector(getContext(),advancedOption,REQUEST_CODE_ADVANCED);

                break;
            case R.id.tv_chat_AddFriend:
                //添加朋友
                AddFriendActivity.start(getContext());


//                Dialog dialog = EasyAlertDialogHelper.showCommonDialogSelfContent(getContext(), R.layout.share_succ_content_layout , "xxx", "yyy", true, new EasyAlertDialogHelper.OnDialogActionListener() {
//                    @Override
//                    public void doCancelAction() {
//
//                    }
//
//                    @Override
//                    public void doOkAction() {
//
//                    }
//                });
//                TextView textView =  dialog.findViewById(R.id.contentTitle);
//                textView.setText("分享成功");
//                dialog.show();

                break;
            case R.id.tv_chat_QRCode:
                //我的二维码
                ZXingUtils.showMyCode(this.context);
                break;
            case R.id.tv_chat_Scan:
                //扫一扫
                ZXingUtils.scanCode(this.context);
                break;
        }
        dismiss();
    }
}

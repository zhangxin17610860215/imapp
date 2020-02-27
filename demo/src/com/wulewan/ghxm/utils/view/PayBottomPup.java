package com.wulewan.ghxm.utils.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lxj.xpopup.core.BottomPopupView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.user.RetrievePayPwdActivity;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.wulewan.ghxm.config.Constants;

import java.util.Map;

public class PayBottomPup extends BottomPopupView {

    private Context context;
    private ImageView imgClose;
    private RelativeLayout rlAli;
    private RelativeLayout rlWChat;
    private TextView tvAli;
    private boolean isBind = false;

    public PayBottomPup(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.pay_bottompup_layout;
    }

    @Override
    protected void initPopupContent() {
        super.initPopupContent();
        // 实现一些UI的初始和逻辑处理
        initView();
    }

    private void initView() {
        imgClose = findViewById(R.id.img_pay_bottompup_close);
        rlAli = findViewById(R.id.rl_pay_bottompup_ali);
        rlWChat = findViewById(R.id.rl_pay_bottompup_WChat);
        tvAli = findViewById(R.id.tv_ali);
        NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(NimUIKit.getAccount());
        Map<String, Object> extensionMap = user.getExtensionMap();

        if (null == extensionMap){
            tvAli.setText("支付宝（未绑定）");
            isBind = false;
        }else {
            String userName = (String) extensionMap.get(Constants.ALI_USERNAME);
            String userId = (String) extensionMap.get(Constants.ALI_USERID);
            if (StringUtil.isNotEmpty(userId)){
//            tvAli.setText("支付宝（已绑定）");
                tvAli.setText("支付宝（" + userName + ")");
                isBind = true;
            }else {
                tvAli.setText("支付宝（未绑定）");
                isBind = false;
            }
        }

        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        String aLiCarry = instance.getString(Constants.CONFIG_INFO.ALICARRY_ISSHOW);
        String wChatCarry = instance.getString(Constants.CONFIG_INFO.WCHATCARRY_ISSHOW);

        if (!aLiCarry.equals("1")){
            //隐藏支付宝提现
            rlAli.setVisibility(GONE);
        }

        if (!wChatCarry.equals("1")){
            //隐藏微信提现
            rlWChat.setVisibility(GONE);
        }

        if (!aLiCarry.equals("1") && !wChatCarry.equals("1")){
            rlAli.setVisibility(GONE);
            rlWChat.setVisibility(GONE);
        }

        imgClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        rlAli.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != aLiListener){
                    dismiss();
                    if (isBind){
                        aLiListener.onClick(v);
                    }else {
                        //弹框提示先去绑定支付宝
                        showBindAliPayDialog();
                    }

                }
            }
        });

        rlWChat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != WChatListener){
                    dismiss();
                    WChatListener.onClick(v);
                }
            }
        });
    }

    private void showBindAliPayDialog() {
        EasyAlertDialogHelper.showCommonDialog(context, "未绑定支付宝", "请绑定支付宝后再进行提现", "确定", "取消", false, new EasyAlertDialogHelper.OnDialogActionListener() {
            @Override
            public void doCancelAction() {

            }

            @Override
            public void doOkAction() {
                RetrievePayPwdActivity.start(context,"2");
            }
        }).show();
    }

    private View.OnClickListener aLiListener,WChatListener;
    public void setOnALiClickListener(View.OnClickListener aLiListener) {
        this.aLiListener = aLiListener;
    }
    public void setOnWChatClickListener(View.OnClickListener WChatListener) {
        this.WChatListener = WChatListener;
    }
}

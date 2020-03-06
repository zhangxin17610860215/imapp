package com.yqbj.ghxm.chatroom.fragment.tab;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.util.AppDemoUtils;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.contact.activity.UserProfileSettingActivity;
import com.yqbj.ghxm.main.activity.FeedbackActivity;
import com.yqbj.ghxm.main.activity.SettingsActivity;
import com.yqbj.ghxm.main.fragment.MainTabFragment;
import com.yqbj.ghxm.user.PasswordManageActivity;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.zxing.ZXingUtils;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.user.UserInfoObserver;
import com.netease.yqbj.uikit.api.wrapper.NimUserInfoProvider;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.util.GlideUtil;
import com.netease.yqbj.uikit.impl.cache.NimUserInfoCache;
import com.netease.yqbj.uikit.utils.NoDoubleClickUtils;
import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.redpacket.wallet.SettingPayPasswordActivity;

import java.util.List;

import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WALLET_EXIST;

public class UserInfoFragment extends MainTabFragment implements View.OnClickListener {

    private HeadImageView imgUserIcon;
    private TextView txtUserName;
    private TextView txtUserId;

    private RelativeLayout rl_user_msg; //个人信息

    private LinearLayout llScan; //扫一扫
    private LinearLayout llMyQrCode; //我的二维码
    private LinearLayout llCloundMoney; //云零钱
    private LinearLayout llSet; //设置
    private TextView tv_feedback; //意见反馈
    private UserInfo userInfo;

    @Override
    protected void onInit() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_info_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initViewData();
        initClick();
    }

    private void initView() {
        imgUserIcon = findView(R.id.user_icon);
        txtUserName = findView(R.id.user_name);
        txtUserId = findView(R.id.user_id);

        rl_user_msg = findView(R.id.rl_user_msg);

        llScan = findView(R.id.ll_scan);
        llMyQrCode = findView(R.id.ll_my_qrcode);
        llCloundMoney = findView(R.id.ll_clound_money);
        llSet = findView(R.id.ll_set);
        tv_feedback = findView(R.id.tv_feedback);
    }

    @SuppressLint("SetTextI18n")
    private void initViewData() {
        onInitSetTitle(getContext(), getString(R.string.user_center));

        updateUI();
        NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true);


    }

    private void updateUI() {
        NimUserInfoProvider userInfoProvider = new NimUserInfoProvider(getContext());
        userInfo = userInfoProvider.getUserInfo(NimUIKit.getAccount());
        if (userInfo != null) {
            imgUserIcon.setIsRect(true);
            imgUserIcon.loadAvatar(userInfo.getAvatar());
            txtUserName.setText(userInfo.getName());
            txtUserId.setText("公会小蜜号:" + NimUIKit.getAccount());
            imgUserIcon.setOnClickListener(this);
        } else {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(NimUIKit.getAccount(), new RequestCallbackWrapper<NimUserInfo>() {
                @Override
                public void onResult(int code, NimUserInfo result, Throwable exception) {
                    imgUserIcon.setIsRect(true);
                    imgUserIcon.loadAvatar(result.getAvatar());
                    txtUserName.setText(result.getName());
                    txtUserId.setText("公会小蜜号:" + NimUIKit.getAccount());
                }
            });
        }
    }

    private void initClick() {
        rl_user_msg.setOnClickListener(this);
        llScan.setOnClickListener(this);
        llMyQrCode.setOnClickListener(this);
        llCloundMoney.setOnClickListener(this);
        llSet.setOnClickListener(this);
        tv_feedback.setOnClickListener(this);

        rl_user_msg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cmb = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(NimUIKit.getAccount().trim());
                ToastHelper.showToast(getActivity(),"复制成功");
                return true;
            }
        });
    }

    private UserInfoObserver userInfoObserver = new UserInfoObserver() {
        @Override
        public void onUserInfoChanged(List<String> accounts) {
            if (accounts != null && accounts.contains(NimUIKit.getAccount())) {
                updateUI();
            }

        }
    };

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)) {
            switch (v.getId()) {
                case R.id.user_icon:
                    GlideUtil.loadIMGFileToWatch(userInfo.getAvatar(), this.getContext());
                    break;
                case R.id.rl_user_msg:
                    btnUserMsgFn();
                    break;
                case R.id.ll_scan:
                    llScanFn();
                    break;
                case R.id.ll_my_qrcode:
                    llMyQrCodeFn();
                    break;
                case R.id.ll_clound_money:
                    //之前的进入零钱包逻辑
//                    llCloundMoneyFn();
                    PasswordManageActivity.start(getContext());
                    break;
                case R.id.ll_set:
                    llSetFn();
                    break;
                case R.id.tv_feedback:
                    FeedbackActivity.start(getContext());
                    break;
            }
        }
    }

    private void btnUserMsgFn() {
        UserProfileSettingActivity.start(getContext(), DemoCache.getAccount());
    }

    private void llScanFn() {
        ZXingUtils.scanCode(getContext());
    }

    private void llMyQrCodeFn() {
        ZXingUtils.showMyCode(getContext());
    }

    private void llCloundMoneyFn() {
        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        if (!instance.getBoolean(WALLET_EXIST)) {
            //用户钱包账户不存在
            SettingPayPasswordActivity.start(getContext());
        } else {

        }
    }

    private void llSetFn() {
        AppDemoUtils.simpleToAct(getContext(), SettingsActivity.class);
    }

}

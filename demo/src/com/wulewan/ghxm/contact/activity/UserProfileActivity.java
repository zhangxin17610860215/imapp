package com.wulewan.ghxm.contact.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.TeamServiceObserver;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.contact.constant.UserConstant;
import com.wulewan.ghxm.main.model.Extras;
import com.wulewan.ghxm.utils.StringUtil;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.api.model.SimpleCallback;
import com.netease.wulewan.uikit.api.model.contact.ContactChangedObserver;
import com.netease.wulewan.uikit.business.recent.RecentContactsFragment;
import com.netease.wulewan.uikit.business.uinfo.UserInfoHelper;
import com.netease.wulewan.uikit.common.CommonUtil;
import com.netease.wulewan.uikit.common.ToastHelper;
import com.netease.wulewan.uikit.common.activity.UI;
import com.netease.wulewan.uikit.common.ui.dialog.DialogMaker;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.common.ui.imageview.HeadImageView;
import com.netease.wulewan.uikit.common.ui.widget.SwitchButton;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.netease.wulewan.uikit.common.util.log.LogUtil;
import com.netease.wulewan.uikit.common.util.sys.NetworkUtil;
import com.wulewan.ghxm.DemoCache;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.session.SessionHelper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAMCONFIGBEAN;

/**
 * 用户资料页面
 * Created by huangjun on 2015/8/11.
 */
public class UserProfileActivity extends UI {

    private static final String TAG = UserProfileActivity.class.getSimpleName();

    private final boolean FLAG_ADD_FRIEND_DIRECTLY = false; // 是否直接加为好友开关，false为需要好友申请
    private final String KEY_BLACK_LIST = "black_list";
    private final String KEY_MSG_NOTICE = "msg_notice";
    private final String KEY_RECENT_STICKY = "recent_contacts_sticky";

    private String account;

    // 基本信息
    private HeadImageView headImageView;
    private TextView nameText;
    private ImageView genderImage;
    private TextView accountText;
    private TextView birthdayText;
    private TextView mobileText;
    private TextView emailText;
    private TextView signatureText;
    private RelativeLayout birthdayLayout;
    private RelativeLayout phoneLayout;
    private RelativeLayout emailLayout;
    private RelativeLayout signatureLayout;
    private RelativeLayout aliasLayout;
    private RelativeLayout albumLayout;
    private TextView albumText;
    private TextView nickText;

    // 开关
    private ViewGroup toggleLayout;
    private Button addFriendBtn;
    private Button removeFriendBtn;
    private Button chatBtn;
    private SwitchButton blackSwitch;
    private SwitchButton noticeSwitch;
    private SwitchButton stickySwitch;
    private IMMessage message;
    private Team team;

    private String urlData = "";

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void start(Context context, String account, IMMessage message) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.putExtra("IMMessage", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        message = (IMMessage) getIntent().getSerializableExtra("IMMessage");
        if (TextUtils.isEmpty(account)) {
            ToastHelper.showToast(UserProfileActivity.this, "传入的帐号为空");
            finish();
            return;
        }
        onInitSetBack(UserProfileActivity.this);
        onInitSetTitle(UserProfileActivity.this, getString(R.string.user_profile));
        onInitRightSure(UserProfileActivity.this, 0, "查看", 0);

        findViews();
        registerObserver(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUserInfo();
        updateToggleView();
        getUserBusinessCard();
    }

    private void getUserBusinessCard() {
        showProgress(this,false);
        UserApi.getUserBusinessCard(account,this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    urlData = (String) object;
                    if (StringUtil.isNotEmpty(urlData)){
                        albumText.setText("已添加");
                    }else {
                        albumText.setText("未添加");
                    }
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObserver(false);
    }

    private void registerObserver(boolean register) {

        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register);
        NIMClient.getService(FriendServiceObserve.class).observeMuteListChangedNotify(muteListChangedNotifyObserver, register);
        NIMClient.getService(TeamServiceObserver.class).observeTeamUpdate(teamUpdateObserver, true);
    }

    // 创建群组资料变动观察者
    Observer<List<Team>> teamUpdateObserver = new Observer<List<Team>>() {
        @Override
        public void onEvent(List<Team> teams) {
            updateUserOperatorView();
        }
    };

    Observer<MuteListChangedNotify> muteListChangedNotifyObserver = new Observer<MuteListChangedNotify>() {
        @Override
        public void onEvent(MuteListChangedNotify notify) {
            setToggleBtn(noticeSwitch, !notify.isMute());
        }
    };

    ContactChangedObserver friendDataChangedObserver = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onDeletedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            updateUserOperatorView();
        }
    };

    private void findViews() {
        headImageView = findView(R.id.user_head_image);
        nameText = findView(R.id.user_name);
        genderImage = findView(R.id.gender_img);
        accountText = findView(R.id.user_account);
        toggleLayout = findView(R.id.toggle_layout);
        addFriendBtn = findView(R.id.add_buddy);
        chatBtn = findView(R.id.begin_chat);
        removeFriendBtn = findView(R.id.remove_buddy);
        birthdayLayout = findView(R.id.birthday);
        nickText = findView(R.id.user_nick);
        birthdayText = birthdayLayout.findViewById(R.id.value);
        phoneLayout = findView(R.id.phone);
        mobileText = phoneLayout.findViewById(R.id.value);
        emailLayout = findView(R.id.email);
        emailText = emailLayout.findViewById(R.id.value);
        signatureLayout = findView(R.id.signature);
        signatureText = signatureLayout.findViewById(R.id.value);
        aliasLayout = findView(R.id.alias);


        ((TextView) birthdayLayout.findViewById(R.id.attribute)).setText(R.string.birthday);
        ((TextView) phoneLayout.findViewById(R.id.attribute)).setText(R.string.phone);
        ((TextView) emailLayout.findViewById(R.id.attribute)).setText(R.string.email);
        ((TextView) signatureLayout.findViewById(R.id.attribute)).setText(R.string.signature);
        TextView aliasTxt = aliasLayout.findViewById(R.id.attribute);
        aliasTxt.setText(R.string.alias);
        aliasTxt.setTextColor(getResources().getColor(R.color.color_3d3d3d));
        albumLayout = findView(R.id.album_layout);
        albumText = albumLayout.findViewById(R.id.value2);
        albumText.setTextColor(getResources().getColor(R.color.color_3d3d3d));
        albumText.setVisibility(View.VISIBLE);
        albumLayout.findViewById(R.id.arrow_right).setVisibility(View.VISIBLE);
        ((TextView) albumLayout.findViewById(R.id.attribute)).setText("个人相片");
        albumLayout.setOnClickListener(onClickListener);
        headImageView.setOnClickListener(onClickListener);
        addFriendBtn.setOnClickListener(onClickListener);
        chatBtn.setOnClickListener(onClickListener);
        removeFriendBtn.setOnClickListener(onClickListener);
        aliasLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileEditItemActivity.startActivity(UserProfileActivity.this, UserConstant.KEY_ALIAS, account);
            }
        });
    }

    @Override
    public void setRightSureClick() {
        if (!TextUtils.equals(account, DemoCache.getAccount())) {
            comRightSure.setVisibility(View.INVISIBLE);
            return;
        } else {
            comRightSure.setVisibility(View.VISIBLE);
        }
        comRightSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileActivity.start(UserProfileActivity.this, account);
            }
        });
    }

    private void setToggleBtn(SwitchButton btn, boolean isChecked) {
        btn.setCheck(isChecked);
    }

    private void updateUserInfo() {
        if (NimUIKit.getUserInfoProvider().getUserInfo(account) != null) {
            updateUserInfoView();
            return;
        }

        NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {

            @Override
            public void onResult(boolean success, NimUserInfo result, int code) {
                updateUserInfoView();
            }
        });
    }

    private void updateUserInfoView() {
        String accountPsd = StringUtil.getPwdstr(account);

        headImageView.loadBuddyAvatar(account);

        if (TextUtils.equals(account, DemoCache.getAccount())) {
            //是自己
            nameText.setText(UserInfoHelper.getUserName(account));
            accountPsd = account;
        }

        final NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo == null) {
            LogUtil.e(TAG, "userInfo is null when updateUserInfoView");
            return;
        }

        if (userInfo.getGenderEnum() == GenderEnum.MALE) {
            //男
            genderImage.setVisibility(View.VISIBLE);
            genderImage.setBackgroundResource(R.drawable.nim_male);
        } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
            //女
            genderImage.setVisibility(View.VISIBLE);
            genderImage.setBackgroundResource(R.drawable.nim_female);
        } else {
            //未知
            genderImage.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getBirthday())) {
            birthdayLayout.setVisibility(View.VISIBLE);
            birthdayText.setText(userInfo.getBirthday());
        } else {
            birthdayLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getMobile())) {
            phoneLayout.setVisibility(View.VISIBLE);
            mobileText.setText(userInfo.getMobile());
        } else {
            phoneLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getEmail())) {
            emailLayout.setVisibility(View.VISIBLE);
            emailText.setText(userInfo.getEmail());
        } else {
            emailLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getSignature())) {
            signatureLayout.setVisibility(View.VISIBLE);
            signatureText.setText(userInfo.getSignature());
        } else {
            signatureLayout.setVisibility(View.GONE);
        }

        if (NIMClient.getService(FriendService.class).isMyFriend(account)){
            accountPsd = account;
        }
        accountText.setText("帐号：" + accountPsd);
    }

    private void updateUserOperatorView() {

        if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
            removeFriendBtn.setVisibility(View.VISIBLE);
            addFriendBtn.setVisibility(View.GONE);
            updateAlias(true);
            chatBtn.setVisibility(View.VISIBLE);
            toggleLayout.setVisibility(View.VISIBLE);
        } else {
            addFriendBtn.setVisibility(View.VISIBLE);
            removeFriendBtn.setVisibility(View.GONE);
            chatBtn.setVisibility(View.GONE);
            updateAlias(false);
            toggleLayout.setVisibility(View.GONE);
        }

        if (message != null && message.getSessionType() == SessionTypeEnum.Team) {
            Team team = NimUIKit.getTeamProvider().getTeamById(message.getSessionId());
            if (null == TEAMCONFIGBEAN){
                return;
            }
            String isSafeMode = TEAMCONFIGBEAN.getProtect() + "";
            if (!TextUtils.isEmpty(isSafeMode)) {
                if (isSafeMode.equals("1")){
                    addFriendBtn.setVisibility(View.GONE);
                }else if (isSafeMode.equals("0")){
                    if (NIMClient.getService(FriendService.class).isMyFriend(account)){
                        addFriendBtn.setVisibility(View.GONE);
                    }else {
                        addFriendBtn.setVisibility(View.VISIBLE);
                    }
                }

            }
        }

    }

    private void updateToggleView() {

        Log.e("DemoCache.getCount", DemoCache.getAccount());
        if (DemoCache.getAccount() != null && !DemoCache.getAccount().equals(account)) {
            boolean black = NIMClient.getService(FriendService.class).isInBlackList(account);
            boolean notice = NIMClient.getService(FriendService.class).isNeedMessageNotify(account);

            if (blackSwitch == null) {
                blackSwitch = addToggleItemView(KEY_BLACK_LIST, R.string.black_list, black);
            } else {
                setToggleBtn(blackSwitch, black);
            }

            if (noticeSwitch == null) {
                noticeSwitch = addToggleItemView(KEY_MSG_NOTICE, R.string.msg_notice, notice);
            } else {
                setToggleBtn(noticeSwitch, notice);
            }

            if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
                RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(account, SessionTypeEnum.P2P);
                boolean isSticky = recentContact != null && CommonUtil.isTagSet(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                if (stickySwitch == null) {
                    stickySwitch = addToggleItemView(KEY_RECENT_STICKY, R.string.recent_sticky, isSticky);
                } else {
                    setToggleBtn(stickySwitch, isSticky);
                }
            }
            updateUserOperatorView();
        }
    }


    private SwitchButton addToggleItemView(String key, int titleResId, boolean initState) {
        ViewGroup vp = (ViewGroup) getLayoutInflater().inflate(R.layout.nim_user_profile_toggle_item, null);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.isetting_item_height));
        vp.setLayoutParams(vlp);

        TextView titleText = vp.findViewById(R.id.user_profile_title);
        titleText.setText(titleResId);

        SwitchButton switchButton = vp.findViewById(R.id.user_profile_toggle);
        switchButton.setCheck(initState);
        switchButton.setOnChangedListener(onChangedListener);
        switchButton.setTag(key);

        toggleLayout.addView(vp);
        return switchButton;
    }

    private void updateAlias(boolean isFriend) {
        if (isFriend) {
            aliasLayout.setVisibility(View.VISIBLE);
            aliasLayout.findViewById(R.id.arrow_right).setVisibility(View.VISIBLE);
            String alias = NimUIKit.getContactProvider().getAlias(account);
            String name = UserInfoHelper.getUserName(account);
            if (!TextUtils.isEmpty(alias)) {
                nickText.setVisibility(View.VISIBLE);
                nameText.setText(alias);
                nickText.setText("昵称：" + name);
            } else {
                nickText.setVisibility(View.GONE);
                nameText.setText(name);
            }
        } else {
            aliasLayout.setVisibility(View.GONE);
            aliasLayout.findViewById(R.id.arrow_right).setVisibility(View.GONE);
            nickText.setVisibility(View.GONE);
            nameText.setText(UserInfoHelper.getUserName(account));
        }
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            final String key = (String) v.getTag();
            if (KEY_RECENT_STICKY.equals(key)) {
                //查询之前是不是存在会话记录
                RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(account, SessionTypeEnum.P2P);
                //置顶
                if (checkState) {
                    //如果之前不存在，创建一条空的会话记录
                    if (recentContact == null) {
                        // RecentContactsFragment 的 MsgServiceObserve#observeRecentContact 观察者会收到通知
                        NIMClient.getService(MsgService.class).createEmptyRecentContact(account,
                                SessionTypeEnum.P2P,
                                RecentContactsFragment.RECENT_TAG_STICKY,
                                System.currentTimeMillis(),
                                true);
                    }
                    // 之前存在，更新置顶flag
                    else {
                        CommonUtil.addTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                }
                //取消置顶
                else {
                    if (recentContact != null) {
                        CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                }
                return;
            }

            if (!NetworkUtil.isNetAvailable(UserProfileActivity.this)) {
                ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
                if (key.equals(KEY_BLACK_LIST)) {
                    blackSwitch.setCheck(!checkState);
                } else if (key.equals(KEY_MSG_NOTICE)) {
                    noticeSwitch.setCheck(!checkState);
                }
                return;
            }

            if (key.equals(KEY_BLACK_LIST)) {
                if (checkState) {
                    NIMClient.getService(FriendService.class).addToBlackList(account).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            ToastHelper.showToast(UserProfileActivity.this, "加入黑名单成功");
                        }

                        @Override
                        public void onFailed(int code) {
                            if (code == 408) {
                                ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
                            } else {
                                ToastHelper.showToast(UserProfileActivity.this, "on failed：" + code);
                            }
                            blackSwitch.setCheck(!checkState);
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                } else {
                    NIMClient.getService(FriendService.class).removeFromBlackList(account).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            ToastHelper.showToast(UserProfileActivity.this, "移除黑名单成功");
                        }

                        @Override
                        public void onFailed(int code) {
                            if (code == 408) {
                                ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
                            } else {
                                ToastHelper.showToast(UserProfileActivity.this, "on failed:" + code);
                            }
                            blackSwitch.setCheck(!checkState);
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                }
            } else if (key.equals(KEY_MSG_NOTICE)) {
                NIMClient.getService(FriendService.class).setMessageNotify(account, checkState).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        if (checkState) {
                            ToastHelper.showToast(UserProfileActivity.this, "开启消息提醒成功");
                        } else {
                            ToastHelper.showToast(UserProfileActivity.this, "关闭消息提醒成功");
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 408) {
                            ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
                        } else {
                            ToastHelper.showToast(UserProfileActivity.this, "on failed:" + code);
                        }
                        noticeSwitch.setCheck(!checkState);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
            }
        }
    };


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == addFriendBtn) {
                if (FLAG_ADD_FRIEND_DIRECTLY) {
                    doAddFriend(null, true);  // 直接加为好友
                } else {
                    onAddFriendByVerify(); // 发起好友验证请求
                }
            } else if (v == removeFriendBtn) {
                onRemoveFriend();
            } else if (v == chatBtn) {
                onChat();
            } else if (v == headImageView) {
                final UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
                GlideUtil.loadIMGFileToWatch(userInfo.getAvatar(),UserProfileActivity.this);
            } else if (v == albumLayout){
                if (StringUtil.isNotEmpty(urlData)){
                    try {
                        List<String> urlList = new ArrayList<>();
                        JSONArray array = new JSONArray(urlData);
                        for (int i = 0; i < array.length(); i++){
                            urlList.add((String) array.get(i));
                        }
                        if (urlList.size() > 1){
                            AlbumDetailActivity.start(UserProfileActivity.this,0,urlList,account);
                        }else if (urlList.size() == 1){
                            if (account.equals(NimUIKit.getAccount())){
                                //是本人
                                AlbumActivity.start(UserProfileActivity.this,urlData,account);
                            }else {
                                AlbumDetailActivity.start(UserProfileActivity.this,0,urlList,account);
                            }
                        }else {
                            AlbumActivity.start(UserProfileActivity.this,urlData,account);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    urlData = "";
                    AlbumActivity.start(UserProfileActivity.this,urlData,account);
                }
            }
        }
    };

    /**
     * 通过验证方式添加好友
     */
    private void onAddFriendByVerify() {

        Dialog dialog = EasyAlertDialogHelper.showCommonDialogWithEdit(this, R.string.add_friend_verify_tip, "确定", "取消", true, new EasyAlertDialogHelper.OnDialogWithEditActionListener() {
            @Override
            public void doCancelAction() {

            }

            @Override
            public void doOkAction(String text) {
                doAddFriend(text, false);

            }
        });
        dialog.show();

    }

    private void doAddFriend(String msg, boolean addDirectly) {
        if (!NetworkUtil.isNetAvailable(this)) {
            ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
            return;
        }
        if (!TextUtils.isEmpty(account) && account.equals(DemoCache.getAccount())) {
            ToastHelper.showToast(UserProfileActivity.this, "不能加自己为好友");
            return;
        }
        final VerifyType verifyType = addDirectly ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
        DialogMaker.showProgressDialog(this, "", true);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(account, verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        DialogMaker.dismissProgressDialog();
                        updateUserOperatorView();
                        if (VerifyType.DIRECT_ADD == verifyType) {
                            ToastHelper.showToast(UserProfileActivity.this, "添加好友成功");
                        } else {
                            ToastHelper.showToast(UserProfileActivity.this, "添加好友请求发送成功");
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == 408) {
                            ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
                        } else {
                            ToastHelper.showToast(UserProfileActivity.this, "on failed:" + code);
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                });

        Log.i(TAG, "onAddFriendByVerify");
    }

    private void onRemoveFriend() {
        Log.i(TAG, "onRemoveFriend");
        if (!NetworkUtil.isNetAvailable(this)) {
            ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
            return;
        }
        Dialog dialog = EasyAlertDialogHelper.showCommonDialog(this, getString(R.string.remove_friend),
                getString(R.string.remove_friend_tip),"确定","取消", true,
                new EasyAlertDialogHelper.OnDialogActionListener() {

                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        DialogMaker.showProgressDialog(UserProfileActivity.this, "", true);
                        NIMClient.getService(FriendService.class).deleteFriend(account).setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                DialogMaker.dismissProgressDialog();
                                ToastHelper.showToast(UserProfileActivity.this, R.string.remove_friend_success);
                                NIMClient.getService(MsgService.class).deleteRecentContact2(account,SessionTypeEnum.P2P);
                                finish();
                            }

                            @Override
                            public void onFailed(int code) {
                                DialogMaker.dismissProgressDialog();
                                if (code == 408) {
                                    ToastHelper.showToast(UserProfileActivity.this, R.string.network_is_not_available);
                                } else {
                                    ToastHelper.showToast(UserProfileActivity.this, "on failed:" + code);
                                }
                            }

                            @Override
                            public void onException(Throwable exception) {
                                DialogMaker.dismissProgressDialog();
                            }
                        });
                    }
                });
        if (!isFinishing() && !isDestroyedCompatible()) {
            dialog.show();
        }
    }

    private void onChat() {
        Log.i(TAG, "onChat");
        SessionHelper.startP2PSession(this, account);
    }


}

package com.yqbj.ghxm.contact.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.business.session.actions.PickImageAction;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.media.picker.PickImageHelper;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.util.GlideUtil;
import com.netease.yqbj.uikit.common.util.log.LogUtil;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.UserInfoBean;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.contact.constant.UserConstant;
import com.yqbj.ghxm.contact.helper.UserUpdateHelper;
import com.yqbj.ghxm.main.model.Extras;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.zxing.ZXingUtils;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2015/9/14.
 */
public class UserProfileSettingActivity extends UI implements View.OnClickListener {
    private final String TAG = UserProfileSettingActivity.class.getSimpleName();

    // constant
    private static final int PICK_AVATAR_REQUEST = 0x0E;
    private static final int AVATAR_TIME_OUT = 30000;

    private String account;
    private String mobile;

    // view
    private HeadImageView userHead;
    private RelativeLayout nickLayout;
    private RelativeLayout genderLayout;
    //    private RelativeLayout birthLayout;
    private RelativeLayout phoneLayout;
    private RelativeLayout emailLayout;
    private RelativeLayout albumLayout;
//    private RelativeLayout signatureLayout;
    private RelativeLayout accidLayout;
    private RelativeLayout qrCodeLayout;
    private TextView nickText;
    private TextView genderText;
    //    private TextView birthText;
    private TextView phoneText;
    private TextView emailText;
    private TextView albumText;
//    private TextView signatureText;
    private TextView accidText;

    // data
    AbortableFuture<String> uploadAvatarFuture;
    private NimUserInfo userInfo;

    private String urlData = "";

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileSettingActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_set_activity);

        onInitSetBack(UserProfileSettingActivity.this);
        onInitSetTitle(UserProfileSettingActivity.this, getString(R.string.com_title_user_msg));

        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
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

    private void findViews() {
        userHead = findView(R.id.user_head);
        nickLayout = findView(R.id.nick_layout);
        genderLayout = findView(R.id.gender_layout);
//        birthLayout = findView(R.id.birth_layout);
        phoneLayout = findView(R.id.phone_layout);
        emailLayout = findView(R.id.email_layout);
        albumLayout = findView(R.id.album_layout);
        qrCodeLayout = findView(R.id.qrCode_layout);
//        signatureLayout = findView(R.id.signature_layout);
        accidLayout = findView(R.id.accid_layout);

        ((TextView) nickLayout.findViewById(R.id.attribute)).setText(R.string.nickname);
        ((TextView) genderLayout.findViewById(R.id.attribute)).setText(R.string.gender);
//        ((TextView) birthLayout.findViewById(R.id.attribute)).setText(R.string.birthday);
        ((TextView) phoneLayout.findViewById(R.id.attribute)).setText("手机号");
        ((TextView) emailLayout.findViewById(R.id.attribute)).setText(R.string.email);
        ((TextView) albumLayout.findViewById(R.id.attribute)).setText("我的相片");
        ((TextView) qrCodeLayout.findViewById(R.id.attribute)).setText(R.string.uInfo_myqr_code);

//        ((TextView) signatureLayout.findViewById(R.id.attribute)).setText(R.string.signature);
        ((TextView) accidLayout.findViewById(R.id.attribute)).setText(R.string.accid_num);
        ((TextView) accidLayout.findViewById(R.id.attribute_value)).setVisibility(View.VISIBLE);
        ((TextView) accidLayout.findViewById(R.id.attribute_value)).setText("(长按可复制)");

        (accidLayout.findViewById(R.id.arrow_right)).setVisibility(View.INVISIBLE);
//        (nickLayout.findViewById(R.id.arrow_right)).setVisibility(View.INVISIBLE);
//        (genderLayout.findViewById(R.id.arrow_right)).setVisibility(View.INVISIBLE);

        nickText = nickLayout.findViewById(R.id.value);
        nickText.setTextColor(getResources().getColor(R.color.color_3d3d3d));
        genderText = genderLayout.findViewById(R.id.value);
        genderText.setTextColor(getResources().getColor(R.color.color_3d3d3d));
//        birthText = birthLayout.findViewById(R.id.value);
        phoneText = phoneLayout.findViewById(R.id.value);
        emailText = emailLayout.findViewById(R.id.value);
        albumText = albumLayout.findViewById(R.id.value);
        emailText.setTextColor(getResources().getColor(R.color.color_3d3d3d));
        albumText.setTextColor(getResources().getColor(R.color.color_3d3d3d));
//        signatureText = (TextView) signatureLayout.findViewById(R.id.value);
        accidText = accidLayout.findViewById(R.id.value);

        findViewById(R.id.head_layout).setOnClickListener(this);
        nickLayout.setOnClickListener(this);
        genderLayout.setOnClickListener(this);
//        birthLayout.setOnClickListener(this);
        phoneLayout.setOnClickListener(this);
        emailLayout.setOnClickListener(this);
        albumLayout.setOnClickListener(this);
        qrCodeLayout.setOnClickListener(this);
        // userHead.setOnClickListener(this);
//        signatureLayout.setOnClickListener(this);

        accidLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cmb = (ClipboardManager)UserProfileSettingActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(NimUIKit.getAccount().trim());
                ToastHelper.showToast(UserProfileSettingActivity.this,"复制成功");
                return true;
            }
        });
    }

    private void getUserInfo() {
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        userInfo = result;
                        updateUI();
                    } else {
                        ToastHelper.showToast(UserProfileSettingActivity.this, "getUserInfoFromRemote failed:" + code);
                    }
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        userHead.loadBuddyAvatar(account);
        nickText.setText(userInfo.getName());
        if (userInfo.getGenderEnum() != null) {
            if (userInfo.getGenderEnum() == GenderEnum.MALE) {
                genderText.setText("男");
            } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
                genderText.setText("女");
            } else {
                genderText.setText("其他");
            }
        }
//        if (userInfo.getBirthday() != null) {
//            birthText.setText(userInfo.getBirthday());
//        }
        showProgress(this, false);
        UserApi.getUserInfo(account, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    UserInfoBean userInfoBean = (UserInfoBean) object;
                    if (userInfoBean.mobile != null) {
                        mobile = userInfoBean.mobile;
                        phoneText.setText(StringUtil.getPwdPhone(mobile));
                    }
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
            }
        });
//        if (userInfo.getMobile() != null) {
//            phoneText.setText(userInfo.getMobile());
//        }
        if (userInfo.getEmail() != null) {
            emailText.setText(userInfo.getEmail());
        }
//        if (userInfo.getSignature() != null) {
//            signatureText.setText(userInfo.getSignature());
//        }
        if (userInfo.getAccount() != null) {
            accidText.setText(userInfo.getAccount());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_head:
                GlideUtil.loadIMGFileToWatch(userInfo.getAvatar(),this);
                break;
            case R.id.head_layout:
                PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
                option.titleResId = R.string.set_head_image;
                option.crop = true;
                option.multiSelect = false;
                option.cropOutputImageWidth = 720;
                option.cropOutputImageHeight = 720;
                PickImageHelper.pickImage(UserProfileSettingActivity.this, PICK_AVATAR_REQUEST, option);
                break;
            case R.id.nick_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_NICKNAME,
                        nickText.getText().toString());
                break;
            case R.id.gender_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_GENDER,
                        String.valueOf(userInfo.getGenderEnum().getValue()));
                break;
//            case R.id.birth_layout:
//                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_BIRTH,
//                        birthText.getText().toString());
//                break;
            case R.id.phone_layout:
                if (StringUtil.isNotEmpty(mobile)){
                    ModifyBindPhoneGuideActivity.start(UserProfileSettingActivity.this,mobile);
                }
                break;
            case R.id.email_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_EMAIL,
                        emailText.getText().toString());
                break;
            case R.id.album_layout:
                if (StringUtil.isNotEmpty(urlData)){
                    try {
                        List<String> urlList = new ArrayList<>();
                        JSONArray array = new JSONArray(urlData);
                        for (int i = 0; i < array.length(); i++){
                            urlList.add((String) array.get(i));
                        }
                        if (urlList.size() > 1){
                            AlbumDetailActivity.start(UserProfileSettingActivity.this,0,urlList,userInfo.getAccount());
                        }else if (urlList.size() == 1){
                            if (userInfo.getAccount().equals(NimUIKit.getAccount())){
                                //是本人
                                AlbumActivity.start(UserProfileSettingActivity.this,urlData,userInfo.getAccount());
                            }else {
                                AlbumDetailActivity.start(UserProfileSettingActivity.this,0,urlList,userInfo.getAccount());
                            }
                        }else {
                            AlbumActivity.start(UserProfileSettingActivity.this,urlData,userInfo.getAccount());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    urlData = "";
                    AlbumActivity.start(UserProfileSettingActivity.this,urlData,userInfo.getAccount());
                }
                break;
            case R.id.qrCode_layout:
                ZXingUtils.showMyCode(this);
                break;
//            case R.id.signature_layout:
//                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_SIGNATURE,
//                        signatureText.getText().toString());
//                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_AVATAR_REQUEST) {
            String path = data.getStringExtra(com.netease.yqbj.uikit.business.session.constant.Extras.EXTRA_FILE_PATH);
            updateAvatar(path);
        }
    }

    /**
     * 更新头像
     */
    private void updateAvatar(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file == null) {
            return;
        }

        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(R.string.user_info_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);

        LogUtil.i(TAG, "start upload avatar, local file path=" + file.getAbsolutePath());
        new Handler().postDelayed(outimeTask, AVATAR_TIME_OUT);
        uploadAvatarFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadAvatarFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    LogUtil.i(TAG, "upload avatar success, url =" + url);

                    UserUpdateHelper.update(UserInfoFieldEnum.AVATAR, url, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                ToastHelper.showToast(UserProfileSettingActivity.this, R.string.head_update_success);
                                onUpdateDone();
                            } else {
                                ToastHelper.showToast(UserProfileSettingActivity.this, R.string.head_update_failed);
                            }
                        }
                    }); // 更新资料
                } else {
                    ToastHelper.showToast(UserProfileSettingActivity.this, R.string.user_info_update_failed);
                    onUpdateDone();
                }
            }
        });
    }

    private void cancelUpload(int resId) {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture.abort();
            ToastHelper.showToast(UserProfileSettingActivity.this, resId);
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(R.string.user_info_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadAvatarFuture = null;
        DialogMaker.dismissProgressDialog();
        getUserInfo();
    }
}

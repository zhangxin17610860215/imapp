package com.yqbj.ghxm.contact.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.utils.AppUtils;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.WxUtils;
import com.yqbj.ghxm.zxing.QrCodeActivity;
import com.yqbj.ghxm.zxing.ZXingUtils;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.api.wrapper.NimUserInfoProvider;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.support.permission.MPermission;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.yqbj.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.yqbj.ghxm.session.search.SearchMessageActivity;

import java.util.ArrayList;

/**
 * 添加好友页面
 * Created by huangjun on 2015/8/11.
 */
public class AddFriendActivity extends BaseAct {

    private static final int REQUEST_GET_WORD = 100;
    private static final int REQUEST_CODE_MOBILE = 101;
    private RelativeLayout searchLayout;
    private LinearLayout mobileMatchLayout;
    private LinearLayout qrCodeLayout;
    private LinearLayout scanLayout;

    private LinearLayout inviteLayout;
    private LinearLayout wechatLayout;

    private static final int BASIC_PERMISSION_REQUEST_CODE = 100;

    private static final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
    };


    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, AddFriendActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend_activity);
        setToolbar(R.drawable.jrmf_b_top_back,"添加朋友");
        requestBasicPermission();

        findViews();


//        ToolBarOptions options = new NimToolBarOptions();
//        options.titleId = R.string.add_buddy;
//        setToolBar(R.id.toolbar, options);

//        findViews();
//        initActionbar();
    }

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    private void findViews() {

        searchLayout = (RelativeLayout) findViewById(R.id.searchLayout);
        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                SearchMessageActivity.start(AddFriendActivity.this,"",SessionTypeEnum.Team);
                Intent intent = new Intent();
                intent.putExtra(SearchMessageActivity.EDITORINFO,EditorInfo.TYPE_CLASS_NUMBER);
                intent.setClass(AddFriendActivity.this, SearchMessageActivity.class);
                startActivityForResult(intent, REQUEST_GET_WORD);
            }
        });

        qrCodeLayout = (LinearLayout) findViewById(R.id.qrCodeLayout);
        qrCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doClick(5);
            }
        });

        mobileMatchLayout = (LinearLayout) findViewById(R.id.mobileMatchLayout);
        initItem(mobileMatchLayout, R.mipmap.icon_match, "手机通讯录匹配", "匹配手机通讯录中的朋友", 1);
        mobileMatchLayout.setVisibility(View.GONE);


        scanLayout = (LinearLayout) findViewById(R.id.scanLayout);
        initItem(scanLayout, R.mipmap.icon_scan, "扫一扫", "匹配手机通讯录中的朋友", 2);


        inviteLayout = (LinearLayout) findViewById(R.id.inviteLayout);
        initItem(inviteLayout, R.mipmap.icon_contrast, "邀请手机通讯录中的朋友", "匹配手机通讯录中的朋友", 3);


        wechatLayout = (LinearLayout) findViewById(R.id.wechatLayout);
        initItem(wechatLayout, R.mipmap.icon_wechat, "邀请微信好友", "匹配手机通讯录中的朋友", 4);


    }


    private void initItem(View view, int imgId, String title, String tips, final int tag) {

        ImageView itemImg = view.findViewById(R.id.item_img);
        itemImg.setImageResource(imgId);


        TextView itemTitle = (TextView) view.findViewById(R.id.item_text);
        TextView itemTips = (TextView) view.findViewById(R.id.item_tips);
        itemTitle.setText(title);
        itemTips.setText(tips);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doClick(tag);
            }
        });


    }

    public void doClick(int tag) {
        switch (tag) {
            case 1:
                break;
            case 2:
                ZXingUtils.scanCode(this);
                break;
            case 3:
                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);

                advancedOption.type = ContactSelectActivity.ContactSelectType.MOBILE;
                advancedOption.title = "邀请短信通讯录好友";
                NimUIKit.startContactSelector(this, advancedOption, REQUEST_CODE_MOBILE);
                break;
            case 4:
                SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                WxUtils.shareWeb(this, instance.getString(Constants.CONFIG_INFO.DOWNLOADURL), SendMessageToWX.Req.WXSceneSession, "邀请您使用公会小蜜", "我正在使用公会小蜜，一款为有共同兴趣爱好用户打造的聊天交友工具");
                break;
            case 5:
                //我的二维码
                Intent intent = new Intent(AddFriendActivity.this, QrCodeActivity.class);
                intent.putExtra("type", ZXingUtils.TYPE_PERSON);
                intent.putExtra("id", NimUIKit.getAccount());
                NimUserInfoProvider userInfoProvider = new NimUserInfoProvider(AddFriendActivity.this);
                UserInfo userInfo = userInfoProvider.getUserInfo(NimUIKit.getAccount());
                intent.putExtra("name", userInfo.getName());
                intent.putExtra("icon", userInfo.getAvatar());
                startActivity(intent);
                break;
            default:
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_WORD:
                    String keyWord = data.getStringExtra("result");

                    if (!TextUtils.isEmpty(keyWord)) {
                        query(keyWord);
                    }
                    break;
                case REQUEST_CODE_MOBILE:
                    final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                    if (selected != null && selected.size() > 0) {
                        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                        AppUtils.sendSMS(this, selected, "我正在使用公会小蜜，一款为有共同兴趣爱好用户打造的聊天交友工具  " + instance.getString(Constants.CONFIG_INFO.DOWNLOADURL));
                    }
                    break;
            }

        }
    }
    
    private void query(final String account) {
        DialogMaker.showProgressDialog(this, null, false);
        NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {
            @Override
            public void onResult(boolean success, NimUserInfo result, int code) {
                DialogMaker.dismissProgressDialog();
                if (success) {
                    if (result == null) {
                        EasyAlertDialogHelper.showOneButtonDiolag(AddFriendActivity.this, R.string.user_not_exsit,
                                R.string.user_tips, R.string.ok, false, null);
                    } else {
                        UserProfileActivity.start(AddFriendActivity.this, account);
                    }
                } else if (code == 408) {
                    ToastHelper.showToast(AddFriendActivity.this, R.string.network_is_not_available);
                } else if (code == ResponseCode.RES_EXCEPTION) {
                    ToastHelper.showToast(AddFriendActivity.this, "on exception");
                } else {
                    ToastHelper.showToast(AddFriendActivity.this, "on failed:" + code);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            ToastHelper.showToast(this, "未全部授权，部分功能可能无法正常运行！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }
}

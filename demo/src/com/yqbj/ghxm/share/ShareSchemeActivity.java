package com.yqbj.ghxm.share;


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.lxj.xpopup.XPopup;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yqbj.uikit.api.CustomEventManager;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.contact.core.item.ItemTypes;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.common.CommonUtil;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.common.util.AppManager;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.session.extension.ShareCardAttachment;
import com.yqbj.ghxm.session.extension.ShareImageAttachment;
import com.yqbj.ghxm.team.TeamCreateHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import Decoder.BASE64Decoder;


/**
 * scheme 启动app
 */
public class ShareSchemeActivity extends UI {

    private static final int REQUEST_SELECTOR = 1;
    private static final int NEWREQUEST_SELECTOR = 2;
    public static final String ACTION_BROWSABLE_LAUNCHER = "ACTION_BROWSABLE_LAUNCHER";

    MsgAttachment shareAttachment = null;
    String content = null;
    String scheme = null;
    ReturnAppTypeEnum returnAppTypeEnum;
    String appName;
    CustomEventManager.CustomListener clickCustomListener;

    CustomEventManager.CustomListener dataCustomListener;
    private ShareSchemeActivity instance;
    String account;
    SessionTypeEnum sessionType;

    private enum ReturnAppTypeEnum {
        SUCCESSFUL(0),
        DATAERROR(1),
        CANCELED(2);
        private final int value;
        // 构造器默认也只能是private, 从而保证构造函数只能在内部使用
        ReturnAppTypeEnum(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        instance = this;

        String webqueryString = getIntent().getStringExtra(ShareSchemeActivity.ACTION_BROWSABLE_LAUNCHER);

        setIntent(new Intent());
        JSONObject webQueryData = null;
        try {
            webQueryData = (JSONObject) JSONObject.parse(webqueryString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (webQueryData != null) {
            if (!TextUtils.isEmpty(webQueryData.getString("messageType"))) {


                scheme = webQueryData.getString("scheme");
                if (!TextUtils.isEmpty(webQueryData.getString("appName"))) {
                    appName = webQueryData.getString("appName");
                }
                if (TextUtils.equals(webQueryData.getString("messageType"), "0")) {

                    String imgBase64 = webQueryData.getString("image");
                    String path = Environment.getExternalStorageDirectory() + "/shareimg";
                    makeDirs(path);
                    path = path + "/shareimage" + new Date().getTime();
                    GenerateImage(imgBase64, path);
                    Bitmap bitmap = BitmapFactory.decodeFile(path, new BitmapFactory.Options());
                    if (bitmap != null) {
                        webQueryData.put("width", bitmap.getWidth());
                        webQueryData.put("height", bitmap.getHeight());
                    }
                    webQueryData.remove("image");
                    shareAttachment = new ShareImageAttachment(webQueryData);
                    if (((ShareImageAttachment) shareAttachment).isErrorData()) {
                        returnApp(ReturnAppTypeEnum.DATAERROR);
                        return;
                    }
                    File file = new File(path);

                    ((FileAttachment) shareAttachment).setPath(file.getPath());
                    ((FileAttachment) shareAttachment).setSize(file.length());
                    content = getString(R.string.share_img);
                } else if (TextUtils.equals(webQueryData.getString("messageType"), "1")) {
                    shareAttachment = new ShareCardAttachment();
                    content = getString(R.string.share_url);
                    ((ShareCardAttachment) shareAttachment).fromJson(webQueryData);

                    if (((ShareCardAttachment) shareAttachment).isErrorData()) {
                        returnApp(ReturnAppTypeEnum.DATAERROR);
                        return;
                    }
                } else {
                    returnApp(ReturnAppTypeEnum.DATAERROR);
                    return;
                }
                String teamId = webQueryData.getString("teamId");
                if (!TextUtils.isEmpty(teamId) && !TextUtils.equals(teamId, "0")) {


                    Bundle selectedId = new Bundle();
                    selectedId.putString("contactId", teamId);
                    selectedId.putInt("contactType", ItemTypes.TEAM);
                    sendMSG(selectedId, "");
                    return;
                }
                final ContactSelectActivity.Option option = new ContactSelectActivity.Option();

                option.title = getString(R.string.share_select);
                option.type = ContactSelectActivity.ContactSelectType.RECENTLY;
                option.multi = false;
                option.returnType = 2;
                option.showNewContactSelectTypeTip = getString(R.string.share_creatnewsession);
                NimUIKit.startContactSelector(this, option, REQUEST_SELECTOR);
                clickCustomListener = new CustomEventManager.CustomListener(CustomEventManager.CustomListenerName.SHARECREATESESSION) {
                    @Override
                    public void execute(CustomEventManager.CustomEvent event) {
                        if (event.getData() == ContactSelectActivity.ContactSelectType.RECENTLY) {
                            creatNewGroup();
                        } else if (event.getData() == ContactSelectActivity.ContactSelectType.BUDDY) {
                            selectGroup();
                        }

                    }
                };
                CustomEventManager.getInstance().addCustomListener(clickCustomListener);

                dataCustomListener = new CustomEventManager.CustomListener(CustomEventManager.CustomListenerName.SHARESELECTDATABACK) {
                    @Override
                    public void execute(CustomEventManager.CustomEvent event) {

                        Bundle data = (Bundle) event.getData();

                        ArrayList<Bundle> selectedIds = (ArrayList<Bundle>) data.getSerializable("selectedIds");
                        if (selectedIds != null) {
                            if (selectedIds.size() == 1) {

                                showSureView(selectedIds.get(0));
                            } else {
                                ArrayList<String> selectedArray = new ArrayList<>();
                                for (int i = 0; i < selectedIds.size(); i++) {
                                    Bundle bundle = selectedIds.get(i);
                                    String account = bundle.getString("contactId");
                                    selectedArray.add(account);
                                }
                                createAdvancedTeam(selectedArray);

                            }
                        } else {
                            returnApp(ReturnAppTypeEnum.SUCCESSFUL);
                        }


                    }
                };
                CustomEventManager.getInstance().addCustomListener(dataCustomListener);
                return;
            }
        }
        returnApp(ReturnAppTypeEnum.DATAERROR);
    }


    private void createAdvancedTeam(ArrayList<String> selectedArray) {
        RequestCallback requestCallback = new RequestCallback<CreateTeamResult>() {
            @Override
            public void onSuccess(CreateTeamResult result) {

                Team team = result.getTeam();
                uploadTeamIcon(team.getId());

            }

            @Override
            public void onFailed(int code) {
                String tip;
                if (code == ResponseCode.RES_TEAM_ECOUNT_LIMIT) {
                    tip = getString(com.netease.yqbj.uikit.R.string.over_team_member_capacity,
                            200);
                } else if (code == ResponseCode.RES_TEAM_LIMIT) {
                    tip = getString(com.netease.yqbj.uikit.R.string.over_team_capacity);
                } else {
                    tip = getString(com.netease.yqbj.uikit.R.string.create_team_failed) + ", code=" + code;
                }

                ToastHelper.showToast(instance, tip);
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();

            }

        };

        TeamCreateHelper.createAdvancedTeam(this, selectedArray, requestCallback);
        DialogMaker.showProgressDialog(AppManager.getAppManager().currentActivity(), "", true);
    }

    private void uploadTeamIcon(final String teamId) {
        RequestCallback request = new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                Bundle bundle = new Bundle();
                bundle.putString("contactId", teamId);
                bundle.putInt("contactType", ItemTypes.TEAM);
                showSureView(bundle);
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        };
        CommonUtil.uploadTeamIcon(teamId, this, request);

    }


    private void creatNewGroup() {
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = getString(R.string.share_selectcontact);
        option.type = ContactSelectActivity.ContactSelectType.BUDDY;
        option.multi = true;
        option.returnType = 2;
        option.showNewContactSelectTypeTip = getString(R.string.share_selectgroup);
        NimUIKit.startContactSelector(this, option, NEWREQUEST_SELECTOR);
    }

    private void selectGroup() {
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = getString(R.string.share_selectteam);
        option.type = ContactSelectActivity.ContactSelectType.TEAM;
        option.multi = false;
        option.returnType = 2;
        NimUIKit.startContactSelector(this, option, NEWREQUEST_SELECTOR);
    }


    private void returnApp(ReturnAppTypeEnum type) {
        returnAppTypeEnum = type;
        if (!TextUtils.isEmpty(scheme)) {
            if (type == ReturnAppTypeEnum.SUCCESSFUL) {
                String stayin = getString(R.string.share_stayin);
                String back = getString(R.string.qr_back) + appName;
                Activity currentActivity = AppManager.getAppManager().currentActivity();
                Dialog dialog = EasyAlertDialogHelper.showCommonDialogSelfContent(currentActivity, R.layout.share_succ_content_layout, stayin, back, true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {
                        toStart();
                        finish();
                    }

                    @Override
                    public void doOkAction() {
                        if (sessionType == SessionTypeEnum.P2P) {
                            NimUIKit.startP2PSession(instance, account);
                        } else if (sessionType == SessionTypeEnum.Team) {
                            NimUIKit.startTeamSession(instance, account);
                        }
                        finish();
                    }
                });
                TextView textView = dialog.findViewById(R.id.contentTitle);
                textView.setText(getString(R.string.share_successful));
                dialog.show();
            } else {
                toStart();
                finish();
            }
        } else {
            finish();
        }
    }

    private void toStart() {
//        Uri uri = Uri.parse(scheme + "?shareCode=" + returnAppTypeEnum.value);
        Uri uri = Uri.parse(String.format("%s?shareCode=%s",scheme,returnAppTypeEnum.value));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            exception.printStackTrace();
        }
    }


    private void showSureView(final Bundle bundle) {

        String account = bundle.getString("contactId");
        SessionTypeEnum sessionType = SessionTypeEnum.P2P;
        String headUrl = "";
        String userName = "";
        if (bundle.getInt("contactType") == ItemTypes.FRIEND) {
            NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(account);
            headUrl = user.getAvatar();
            userName = user.getName();

        } else if (bundle.getInt("contactType") == ItemTypes.TEAM) {
            Team team = NimUIKit.getTeamProvider().getTeamById(account);
            headUrl = team.getIcon();
            userName = team.getName();
        }
        final ShareSureSelect shareSureSelect = new ShareSureSelect(AppManager.getAppManager().currentActivity(), headUrl, userName, shareAttachment);
        new XPopup.Builder(AppManager.getAppManager().currentActivity())
                .dismissOnTouchOutside(false)
                .asCustom(shareSureSelect)
                .show();
        shareSureSelect.setOnClickListenerOnSure(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMSG(bundle, shareSureSelect.getLeave_message());
                shareSureSelect.dismiss();

            }
        });
    }

    private void sendMSG(Bundle bundle, String leave_message) {

        account = bundle.getString("contactId");
        if (bundle.getInt("contactType") == ItemTypes.FRIEND) {
            sessionType = SessionTypeEnum.P2P;

        } else if (bundle.getInt("contactType") == ItemTypes.TEAM) {
            sessionType = SessionTypeEnum.Team;
        }
        IMMessage customMessage = MessageBuilder.createCustomMessage(account, sessionType, content, shareAttachment);

        NIMClient.getService(MsgService.class).sendMessage(customMessage, false);

        if (!TextUtils.isEmpty(leave_message)) {
            String text = leave_message;
            IMMessage textMessage = MessageBuilder.createTextMessage(account, sessionType, text);
            NIMClient.getService(MsgService.class).sendMessage(textMessage, false);
        }


        returnApp(ReturnAppTypeEnum.SUCCESSFUL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECTOR) {

            returnApp(ReturnAppTypeEnum.CANCELED);
        }
    }


    //base64字符串转化成图片
    public static boolean GenerateImage(String imgStr, String path) {   //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            //生成jpeg图片
            String imgFilePath = path;//新生成的图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void makeDirs(String path) {
        File targetFile = new File(path);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomEventManager.getInstance().removeCustomListener(clickCustomListener);
        CustomEventManager.getInstance().removeCustomListener(dataCustomListener);
        AppManager.getAppManager().finishAllSameActivity(ContactSelectActivity.class);
        Constants.decodeData = null;
    }
}

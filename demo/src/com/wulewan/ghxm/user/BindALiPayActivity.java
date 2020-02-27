package com.wulewan.ghxm.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.AliPayInfoBean;
import com.wulewan.ghxm.bean.SignParamsBean;
import com.wulewan.ghxm.pay.AliPayResult;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.netease.wulewan.uikit.utils.NoDoubleClickUtils;
import com.umeng.analytics.MobclickAgent;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.contact.helper.UserUpdateHelper;
import com.wulewan.ghxm.login.AliPayLogin;
import com.wulewan.ghxm.login.AuthorizationState;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.utils.Base64;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

import static com.netease.wulewan.uikit.api.StatisticsConstants.COIN_ALIBIND_SUCCESSNUM;

/**
 * 绑定支付宝
 */
public class BindALiPayActivity extends BaseAct implements View.OnClickListener {

    private TextView tvIsBind;
    private TextView tvNoBind;
    private TextView tvIsOrNot;
    private ImageView imgAvatar;
    private Context context;

    public static void start(Context context) {
        Intent intent = new Intent(context, BindALiPayActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bindalipay);
        context = this;
        initView();
    }

    private void initView() {
        setToolbar("绑定支付宝");
        tvIsBind = findView(R.id.tv_bindalipay_noBind);
        tvNoBind = findView(R.id.tv_bindalipay_bind);
        tvIsOrNot = findView(R.id.tv_bindalipay_isBind);
        imgAvatar = findView(R.id.img_bindalipay_avatar);

        tvIsBind.setOnClickListener(this);
        tvNoBind.setOnClickListener(this);
        NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(NimUIKit.getAccount());
        Map<String, Object> extensionMap = user.getExtensionMap();
        if (null == extensionMap){
            tvIsBind.setVisibility(View.VISIBLE);
            tvNoBind.setVisibility(View.GONE);
            tvIsOrNot.setText("未绑定支付宝");
            imgAvatar.setImageResource(R.mipmap.bindalipay_icon);
            return;
        }
        String userName = (String) extensionMap.get(Constants.ALI_USERNAME);
        String userId = (String) extensionMap.get(Constants.ALI_USERID);
        if (StringUtil.isNotEmpty(userId)) {
            tvIsBind.setVisibility(View.GONE);
            tvNoBind.setVisibility(View.VISIBLE);
            GlideUtil.loadCircular(context, SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.AVATAR), imgAvatar);
            tvIsOrNot.setText("已绑定支付宝 " + userName);
        } else {
            tvIsBind.setVisibility(View.VISIBLE);
            tvNoBind.setVisibility(View.GONE);
            tvIsOrNot.setText("未绑定支付宝");
            imgAvatar.setImageResource(R.mipmap.bindalipay_icon);
        }
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()) {
                case R.id.tv_bindalipay_noBind:
                    //去绑定
                    goAliPayLogin();
                    break;
                case R.id.tv_bindalipay_bind:
                    //已绑定

                    EasyAlertDialogHelper.showCommonDialog(this, null, "您确定要解除绑定的支付宝账号吗？", "确定", "取消", false, new EasyAlertDialogHelper.OnDialogActionListener() {
                        @Override
                        public void doCancelAction() {

                        }

                        @Override
                        public void doOkAction() {
                            SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                            instance.put(Constants.ALIPAY_USERINFO.ISBINDALIPAY, false);
                            instance.put(Constants.ALIPAY_USERINFO.AVATAR, "");
                            instance.put(Constants.ALIPAY_USERINFO.NICKNAME, "");
                            instance.put(Constants.ALIPAY_USERINFO.USERID, "");

                            Map<String, Object> extensionMap = new HashMap<>();
                            extensionMap.put(Constants.ALI_USERNAME,"");
                            extensionMap.put(Constants.ALI_USERID,"");
                            UserUpdateHelper.update(UserInfoFieldEnum.EXTEND, extensionMap, new RequestCallbackWrapper<Void>() {
                                @Override
                                public void onResult(int i, Void aVoid, Throwable throwable) {
                                    //用户信息保存成功
                                    tvIsBind.setVisibility(View.VISIBLE);
                                    tvNoBind.setVisibility(View.GONE);
                                    tvIsOrNot.setText("未绑定支付宝");
                                    imgAvatar.setImageResource(R.mipmap.bindalipay_icon);
                                    finish();
                                }
                            });

                        }
                    }).show();

                    break;
            }
        }

    }

    private void goAliPayLogin() {
        final AliPayLogin payLogin = new AliPayLogin(context);
        String info = payLogin.getInfo(false);
        String baseInfo = "";
        try {
            baseInfo = Base64.encode(info.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        showProgress(this, false);
        UserApi.singParams("3",baseInfo, 1,null,null,null,null,null,null,null,null,this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
//                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    SignParamsBean bean = (SignParamsBean) object;
                    payLogin.goAliPayLogin(BindALiPayActivity.this, payLogin.getInfo(true), bean.getSign(), new AuthorizationState() {
                        @Override
                        public void onSuccess(String code, AliPayResult object) {
                            //授权成功
                            if (code.equals("9000")) {
                                authorizationSuccess(object);
                            } else {
                                ToastUtil.showToast(BindALiPayActivity.this, "授权失败");
                                dismissProgress();
                            }
                        }

                        @Override
                        public void onFailed(String errMessage) {
                            if (errMessage.equals("6001")) {
                                ToastUtil.showToast(BindALiPayActivity.this, "用户已取消");
                            } else {
                                ToastUtil.showToast(BindALiPayActivity.this, "授权失败");
                            }
                            dismissProgress();
                        }
                    });
                } else {
                    ToastUtil.showToast(BindALiPayActivity.this, (String) object);
                    dismissProgress();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(BindALiPayActivity.this, errMessage);
            }
        });
    }

    private void authorizationSuccess(AliPayResult object) {
        AliPayResult payResult = object;
        if (null == payResult) {
            return;
        }
        if (StringUtil.isEmpty(payResult.getResult())) {
            return;
        }
        String[] strs = payResult.getResult().split("&");
        Map<String, String> map = new HashMap<>();
        for (String s : strs) {
            String[] ms = s.split("=");
            map.put(ms[0], ms[1]);
        }

        getAliPayInfo(map.get("auth_code"));
    }

    private void getAliPayInfo(String authCode) {
//        showProgress(this,false);
        UserApi.getAliPayInfo(authCode, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    MobclickAgent.onEvent(context,COIN_ALIBIND_SUCCESSNUM);
                    AliPayInfoBean bean = (AliPayInfoBean) object;
                    tvIsBind.setVisibility(View.GONE);
                    tvNoBind.setVisibility(View.VISIBLE);
                    tvIsOrNot.setText("已绑定支付宝 " + bean.getNickName());
                    GlideUtil.loadCircular(context,bean.getAvatar(),imgAvatar);
                    SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
                    instance.put(Constants.ALIPAY_USERINFO.ISBINDALIPAY, true);
                    instance.put(Constants.ALIPAY_USERINFO.AVATAR, bean.getAvatar());
                    instance.put(Constants.ALIPAY_USERINFO.NICKNAME, bean.getNickName());
                    instance.put(Constants.ALIPAY_USERINFO.USERID, bean.getUserId());

                    Map<String, Object> extensionMap = new HashMap<>();
                    extensionMap.put(Constants.ALI_USERNAME,bean.getNickName());
                    extensionMap.put(Constants.ALI_USERID,bean.getUserId());
                    UserUpdateHelper.update(UserInfoFieldEnum.EXTEND, extensionMap, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int i, Void aVoid, Throwable throwable) {
                            //用户信息保存成功

                        }
                    });
                } else {
                    ToastUtil.showToast(context, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(context, errMessage);
            }
        });
    }
}

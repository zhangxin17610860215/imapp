package com.yqbj.ghxm.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.yqbj.ghxm.NimApplication;
import com.yqbj.ghxm.bean.WXMesBean;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.OverallApi;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.EventBusUtils;

public class WXEntryActivity extends UI implements IWXAPIEventHandler {
    public static final String TAG = WXEntryActivity.class.getSimpleName();
    public static String code;
    public static BaseResp resp = null;
    private AbortableFuture<LoginInfo> loginRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_wxentry);
        boolean handleIntent = NimApplication.api.handleIntent(getIntent(), this);
        //下面代码是判断微信分享后返回WXEnteryActivity的，如果handleIntent==false,说明没有调用IWXAPIEventHandler，则需要在这里销毁这个透明的Activity;
        if (handleIntent == false) {
            Log.e(TAG, "onCreate: " + handleIntent);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        NimApplication.api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.e(TAG, "onReq: ");
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp != null) {
            resp = baseResp;
            code = ((SendAuth.Resp) baseResp).code; //即为所需的code
        }
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
//                ToastHelper.showToast(this,"登录成功");
                goWX_Login();
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                ToastHelper.showToast(this,"取消登录");
                EventBusUtils.CommonEvent commonEvent = new EventBusUtils.CommonEvent();
                commonEvent.id = 101;
                EventBusUtils.post(commonEvent);
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                ToastHelper.showToast(this,"登录失败，请稍后重试");
                EventBusUtils.CommonEvent commonEvent1 = new EventBusUtils.CommonEvent();
                commonEvent1.id = 101;
                EventBusUtils.post(commonEvent1);
                finish();
                break;
            default:
                EventBusUtils.CommonEvent commonEvent3 = new EventBusUtils.CommonEvent();
                commonEvent3.id = 101;
                EventBusUtils.post(commonEvent3);
                break;
        }
    }

    private void goWX_Login() {
//        DialogMaker.showProgressDialog(this, "", false);
        showProgress(this,false);
        OverallApi.getKey(this, new requestCallback() {
            @Override
            public void onSuccess(int co, Object object) {
                if (co == Constants.SUCCESS_CODE){
                    UserApi.wx_Login(NimApplication.APP_ID, NimApplication.SECRET, code, this, new requestCallback() {
                        @Override
                        public void onSuccess(int code, Object object) {
                            final WXMesBean bean = (WXMesBean) object;
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("WXMesBean",bean);
                            EventBusUtils.CommonEvent commonEvent = new EventBusUtils.CommonEvent();
                            commonEvent.id = 101;
                            commonEvent.data = bundle;
                            EventBusUtils.post(commonEvent);
                        }

                        @Override
                        public void onFailed(final String errMessage) {
                            WXEntryActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastHelper.showToast(WXEntryActivity.this,errMessage);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onFailed(String errMessage) {
                ToastHelper.showToast(WXEntryActivity.this,errMessage);
            }
        });

    }

}
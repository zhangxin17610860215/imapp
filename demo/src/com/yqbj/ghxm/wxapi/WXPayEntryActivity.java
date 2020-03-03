package com.yqbj.ghxm.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.yqbj.ghxm.NimApplication;
import com.yqbj.ghxm.utils.EventBusUtils;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_wechat_pay);

        api = WXAPIFactory.createWXAPI(this, NimApplication.APP_ID);
        api.handleIntent(getIntent(), this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Bundle bundle = new Bundle();
            EventBusUtils.CommonEvent commonEvent = new EventBusUtils.CommonEvent();
            commonEvent.id = 100;
            if (resp.errCode == 0) {
                bundle.putString("payResult","0");
                commonEvent.data = bundle;
            } else {
                ToastUtil.showToast(this,"支付失败");
                bundle.putString("payResult","-1");
                commonEvent.data = bundle;
            }
            EventBusUtils.post(commonEvent);
            finish();
        }
    }
}
package com.wulewan.ghxm.wxapi;

import android.content.Context;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.wulewan.ghxm.NimApplication;
import com.wulewan.ghxm.bean.WChatParamsBean;
import com.netease.wulewan.uikit.common.ui.dialog.DialogMaker;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;

public class WXUtil {
    private static final IWXAPI msgApi = NimApplication.api;

    public static void weiChatPay(String tradeType, String amount, String rid, String money, String targetIds,
                                  String targetType, String name, String paymentPwd, String number,
                                  String targetId, final Context context, final WeiChatPayCallBack payCallBack) {
        DialogMaker.showProgressDialog(context, context.getString(com.netease.wulewan.uikit.R.string.empty), false);
        UserApi.singWChatParams(tradeType, amount, rid, money, targetIds, targetType, name, paymentPwd, number, targetId, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                DialogMaker.dismissProgressDialog();
                WChatParamsBean bean = (WChatParamsBean) object;
                Constants.WXPAY_ORDERID = bean.getTradeNo();
                PayReq req = new PayReq();
                req.appId = NimApplication.APP_ID;          //应用ID
                req.partnerId = bean.getPartnerid();        //商户号
                req.prepayId = bean.getPrepayid();          //微信返回的支付交易会话id
                req.packageValue = bean.getPackageX();      //固定值
                req.nonceStr = bean.getNoncestr();          //随机字符串
                req.timeStamp = bean.getTimestamp();        //时间戳
                req.sign = bean.getSign();                  //签名
                msgApi.sendReq(req);
                payCallBack.onSuccess(code,bean);
            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();
                ToastUtil.showToast(context,errMessage);
                payCallBack.onFailed(errMessage);
            }
        });
    }

    public interface  WeiChatPayCallBack{
        void onSuccess(int code, Object object);
        void onFailed(String errMessage);
    }
}

package com.wulewan.ghxm.login;

import android.app.Activity;
import android.content.Context;
import android.os.Message;

import com.alipay.sdk.app.AuthTask;
import com.wulewan.ghxm.NimApplication;
import com.wulewan.ghxm.pay.AliPayResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliPayLogin {

    private Context context;

    public AliPayLogin(Context context) {
        this.context =  context;
    }

//    @SuppressLint("HandlerLeak")
//    private Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//
//        }
//    };

    public void goAliPayLogin(final Activity context, final String orderParams, final String sign, final AuthorizationState state){
        String encode = "";
        try {
            encode = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String authInfo = orderParams + "&sign=" + encode;

        Runnable authRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造AuthTask 对象
                AuthTask authTask = new AuthTask(context);
                // 调用授权接口，获取授权结果
                Map<String, String> result = authTask.authV2(authInfo, true);

                Message msg = new Message();
                msg.what = 2;
                msg.obj = result;
//                mHandler.sendMessage(msg);

                AliPayResult payResult = new AliPayResult(result);
                if (null == payResult){
                    return;
                }
//                if (StringUtil.isEmpty(payResult.getResult())){
//                    return;
//                }

                if (payResult.getResultStatus().equals("9000")){
                    state.onSuccess(payResult.getResultStatus(),payResult);
                }else {
                    state.onFailed(payResult.getResultStatus());
                }

            }
        };

        // 必须异步调用
        Thread authThread = new Thread(authRunnable);
        authThread.start();
    }

    public String getInfo(boolean isSort){
        Map<String, String> authInfoMap = buildOrderParamMap();
        String info = buildOrderParam(authInfoMap,isSort);
        return info;
    }

    /**
     * 构造支付订单参数列表
     *
     * @param
     * @param
     * @return
     */
    private Map<String, String> buildOrderParamMap() {
        Map<String, String> keyValues = new HashMap<String, String>();

        // 商户签约拿到的app_id，如：2013081700024223
        keyValues.put("app_id", NimApplication.ALIPAY_APPID);

        // 商户签约拿到的pid，如：2088102123816631
        keyValues.put("pid", NimApplication.ALIPAY_PID);

        // 服务接口名称， 固定值
        keyValues.put("apiname", "com.alipay.account.auth");

        // 商户类型标识， 固定值
        keyValues.put("app_name", "mc");

        // 业务类型， 固定值
        keyValues.put("biz_type", "openservice");

        // 产品码， 固定值
        keyValues.put("product_id", "APP_FAST_LOGIN");

        // 授权范围， 固定值
        keyValues.put("scope", "kuaijie");

        // 商户唯一标识，如：kkkkk091125
        keyValues.put("target_id", "yJzdWIiOiJ7XCJUb2");

        // 授权类型， 固定值
        keyValues.put("auth_type", "AUTHACCOUNT");

        // 签名类型
        keyValues.put("sign_type", "RSA2");


        return keyValues;
    }

    /**
     * 构造支付订单参数信息
     *
     * @param map
     * 支付订单参数
     * @return
     */
    private   String buildOrderParam(Map<String, String> map,boolean isSort) {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            sb.append(buildKeyValue(key, value, isSort));
            sb.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        sb.append(buildKeyValue(tailKey, tailValue, isSort));

        return sb.toString();
    }

    /**
     * 拼接键值对
     *
     * @param key
     * @param value
     * @param isEncode
     * @return
     */
    private  String buildKeyValue(String key, String value, boolean isEncode) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("=");
        if (isEncode) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                sb.append(value);
            }
        } else {
            sb.append(value);
        }
        return sb.toString();
    }

}

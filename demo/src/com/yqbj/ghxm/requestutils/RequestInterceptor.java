package com.yqbj.ghxm.requestutils;

import com.yqbj.ghxm.DemoCache;
import com.yqbj.ghxm.bean.BaseBean;
import com.yqbj.ghxm.bean.LoginBean;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.OverallApi;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.utils.GsonHelper;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.ToastUtils;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class RequestInterceptor implements Interceptor {
    private String url = "";
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        url = String.valueOf(request.url());

        Response originalResponse = null;
        try {
            originalResponse = chain.proceed(request);
        } catch (Exception e) {
            throw e;
        }
        if (null != originalResponse) {
            ResponseBody responseBody = originalResponse.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            MediaType contentType = responseBody.contentType();
            String bodyString = buffer.clone().readString(Charset.forName("UTF-8"));
            ResponseBody responseBodyDecode = ResponseBody.create(contentType, bodyString);

            //检查key是否失效
            isKeyInvalid(bodyString);

            return originalResponse.newBuilder().body(responseBodyDecode).build();
        } else {
            return null;
        }
    }

    private void isKeyInvalid(String bodyString) {
        try {
            if (StringUtil.isNotEmpty(bodyString)){
                BaseBean bean = GsonHelper.getSingleton().fromJson(bodyString, BaseBean.class);
                if(null != bean){
                    if (bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_10028){
                        ToastUtils.show(DemoCache.getContext(),bean.getMessage());
                    } else if (bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_10003){
//                        ToastUtils.show(DemoCache.getContext(),"认证过期，请重试");
                        OverallApi.getKey(DemoCache.getContext(), new requestCallback() {
                            @Override
                            public void onSuccess(int code, Object object) {
//                                NewLoginActivity.start(NimApplication.getInstance());
                                String openid = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.OPENID);
                                String uuid = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.UUID);
                                UserApi.login(null,openid,uuid,DemoCache.getContext(), new requestCallback() {
                                    @Override
                                    public void onSuccess(int code, Object object) {
                                        if (code == Constants.SUCCESS_CODE){
                                            LoginBean loginBean = (LoginBean) object;
                                            SPUtils.getInstance().put(Constants.USER_TYPE.USERTOKEN, loginBean.getUserToken());
                                            SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN, loginBean.getYunxinToken());
                                            SPUtils.getInstance().put(Constants.USER_TYPE.ACCID, loginBean.getAccid());
                                        }
                                    }

                                    @Override
                                    public void onFailed(String errMessage) {

                                    }
                                });
                            }
                            @Override
                            public void onFailed(String errMessage) {

                            }
                        });
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

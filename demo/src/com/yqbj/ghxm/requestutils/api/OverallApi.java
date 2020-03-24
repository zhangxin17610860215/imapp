package com.yqbj.ghxm.requestutils.api;


import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.netease.yqbj.rtskit.common.log.LogUtil;
import com.yqbj.ghxm.bean.BaseBean;
import com.yqbj.ghxm.bean.CheckVersionBean;
import com.yqbj.ghxm.bean.ConfigInfoBean;
import com.yqbj.ghxm.bean.GetKeyBean;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.RequestHelp;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.GsonHelper;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.DOWNLOADURL;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.OPENPLATFORMURL;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WALLET_EXIST;
import static com.yqbj.ghxm.config.Constants.ERROR_REQUEST_EXCEPTION_MESSAGE;
import static com.yqbj.ghxm.config.Constants.ERROR_REQUEST_FAILED_MESSAGE;

public class OverallApi {
    private final static String TAG = "OverallApi";

    /**
     * 获取Key接口
     * */
    public static void getKey( Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        RequestHelp.getRequest(ApiUrl.OVERALL_GET_KEY, object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    LogUtil.e(TAG,"getKey--------->onSuccess" + bean.getData());
                    if (Constants.SUCCESS_CODE == bean.getStatusCode()) {
                        GetKeyBean getKeyBean = GsonHelper.getSingleton().fromJson(bean.getData(), GetKeyBean.class);
                        Constants.BASE_URL = getKeyBean.getDomain();
                        SPUtils.getInstance().put(Constants.USER_TYPE.APITOKEN,getKeyBean.getApiToken());
                        SPUtils.getInstance().put(Constants.USER_TYPE.KEY,getKeyBean.getKey());
                        callback.onSuccess(bean.getStatusCode(),getKeyBean);
                    } else {
                        callback.onFailed( bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG,"getKey--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }
    /**
     * 检查客户端版本
     * */
    public static void checkVersion(int deviceType, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("deviceType",deviceType + "");
        RequestHelp.getRequest(ApiUrl.CHECK_VERSION, object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    LogUtil.e(TAG,"checkVersion--------->onSuccess" + bean.getData());
                    if (Constants.SUCCESS_CODE == bean.getStatusCode()) {
                        CheckVersionBean checkVersionBean = GsonHelper.getSingleton().fromJson(bean.getData(), CheckVersionBean.class);
                        callback.onSuccess(bean.getStatusCode(),checkVersionBean);
                    } else {
                        callback.onFailed( bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG,"checkVersion--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 检查全局配置
     * */
    public static void configInfo(Object object, final requestCallback callback) {
        final SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        Map<String, String> map = new HashMap<>();
        RequestHelp.getRequest(StringUtil.stringformat(ApiUrl.CONFIGINFO), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    LogUtil.e(TAG,"configInfo--------->onSuccess" + bean.getData());
                    if (Constants.SUCCESS_CODE == bean.getStatusCode()) {
                        ConfigInfoBean configInfoBean = GsonHelper.getSingleton().fromJson(bean.getData(), ConfigInfoBean.class);
                        instance.put(WALLET_EXIST,configInfoBean.isUserWalletExist());
                        instance.put(DOWNLOADURL,configInfoBean.getDownloadUrl());
                        instance.put(OPENPLATFORMURL,configInfoBean.getOpenPlatformUrl());
                        callback.onSuccess(bean.getStatusCode(),configInfoBean);
                    }else {
                        instance.put(WALLET_EXIST,false);
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    instance.put(WALLET_EXIST,false);
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG,"configInfo--------->onError");

                instance.put(WALLET_EXIST,false);
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }
}

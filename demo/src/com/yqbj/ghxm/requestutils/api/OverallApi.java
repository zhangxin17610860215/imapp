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

import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.ALI_UPPERLIMIT;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WALLET_EXIST;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WX_UPPERLIMIT;
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
                        ApiUrl.BASE_URL = getKeyBean.getDomain();
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
    public static void configInfo(Object object) {
        final SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        Map<String, String> map = new HashMap<>();
        RequestHelp.getRequest(ApiUrl.CONFIGINFO, object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    LogUtil.e(TAG,"configInfo--------->onSuccess" + bean.getData());
                    if (Constants.SUCCESS_CODE == bean.getStatusCode()) {
                        ConfigInfoBean configInfoBean = GsonHelper.getSingleton().fromJson(bean.getData(), ConfigInfoBean.class);
//                        ConfigInfoBean.PayButtonConfigBean payButtonConfig = configInfoBean.getPayButtonConfig();
//                        ConfigInfoBean.WithdrawButtonConfigBean withdrawButtonConfig = configInfoBean.getWithdrawButtonConfig();
                        instance.put(WALLET_EXIST,configInfoBean.isUserWalletExist());
//                        instance.put(ALIPAY_ISSHOW,payButtonConfig.getAli() + "");
//                        instance.put(WCHATPAY_ISSHOW,payButtonConfig.getWechat() + "");
//                        instance.put(ALICARRY_ISSHOW,withdrawButtonConfig.getAli() + "");
//                        instance.put(WCHATCARRY_ISSHOW,withdrawButtonConfig.getWechat() + "");
                        if (StringUtil.isEmpty(configInfoBean.getWxUpperLimit())){
                            configInfoBean.setWxUpperLimit("500");
                        }
                        if (StringUtil.isEmpty(configInfoBean.getAliUpperLimit())){
                            configInfoBean.setAliUpperLimit("500");
                        }
                        instance.put(WX_UPPERLIMIT,configInfoBean.getWxUpperLimit());
                        instance.put(ALI_UPPERLIMIT,configInfoBean.getAliUpperLimit());

                    }else {
                        instance.put(WALLET_EXIST,false);
//                        instance.put(ALIPAY_ISSHOW,"1");
//                        instance.put(WCHATPAY_ISSHOW,"0");
//                        instance.put(ALICARRY_ISSHOW,"1");
//                        instance.put(WCHATCARRY_ISSHOW,"0");
                        instance.put(WX_UPPERLIMIT,"500");
                        instance.put(ALI_UPPERLIMIT,"500");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    instance.put(WALLET_EXIST,false);
//                    instance.put(ALIPAY_ISSHOW,"1");
//                    instance.put(WCHATPAY_ISSHOW,"0");
//                    instance.put(ALICARRY_ISSHOW,"1");
//                    instance.put(WCHATCARRY_ISSHOW,"0");
                    instance.put(WX_UPPERLIMIT,"500");
                    instance.put(ALI_UPPERLIMIT,"500");
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG,"configInfo--------->onError");

                instance.put(WALLET_EXIST,false);
//                instance.put(ALIPAY_ISSHOW,"1");
//                instance.put(WCHATPAY_ISSHOW,"0");
//                instance.put(ALICARRY_ISSHOW,"1");
//                instance.put(WCHATCARRY_ISSHOW,"0");
                instance.put(WX_UPPERLIMIT,"500");
                instance.put(ALI_UPPERLIMIT,"500");
            }
        });
    }
}
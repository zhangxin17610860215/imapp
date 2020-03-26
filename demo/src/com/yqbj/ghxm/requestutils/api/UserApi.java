package com.yqbj.ghxm.requestutils.api;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yqbj.rtskit.common.log.LogUtil;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.bean.TeamConfigBean;
import com.yqbj.ghxm.bean.AutomaticGetRedPackBean;
import com.yqbj.ghxm.bean.BaseBean;
import com.yqbj.ghxm.bean.DetailsChangeQueryBean;
import com.yqbj.ghxm.bean.GetAllMemberWalletBean;
import com.yqbj.ghxm.bean.LoginBean;
import com.yqbj.ghxm.bean.MyTeamWalletBean;
import com.yqbj.ghxm.bean.OrderNumberBean;
import com.yqbj.ghxm.bean.RedPackOtherDataBean;
import com.yqbj.ghxm.bean.RedPacketStateBean;
import com.yqbj.ghxm.bean.RootListBean;
import com.yqbj.ghxm.bean.SettlementBean;
import com.yqbj.ghxm.bean.TeamAllocationPriceBean;
import com.yqbj.ghxm.bean.TeamInactiveBean;
import com.yqbj.ghxm.bean.TeamLeaveBean;
import com.yqbj.ghxm.bean.TeamRobotDataBean;
import com.yqbj.ghxm.bean.TeamRobotDetatlsBean;
import com.yqbj.ghxm.bean.TeamRobotNotifyBean;
import com.yqbj.ghxm.bean.UnclaimedRPDetailsBean;
import com.yqbj.ghxm.bean.UserInfoBean;
import com.yqbj.ghxm.bean.WXMesBean;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.RequestHelp;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.Base64Util;
import com.yqbj.ghxm.utils.GsonHelper;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.yqbj.ghxm.config.Constants.CURRENTTIME;
import static com.yqbj.ghxm.config.Constants.ERROR_REQUEST_EXCEPTION_MESSAGE;
import static com.yqbj.ghxm.config.Constants.ERROR_REQUEST_FAILED_MESSAGE;
import static com.yqbj.ghxm.config.Constants.WX_LOGIN_API;

public class UserApi {
    private final static String TAG = "UserApi";

    /**
     * 微信登录接口
     */
    public static void wx_Login(String appid, String secret, String code, Object object, final requestCallback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder =HttpUrl.parse(WX_LOGIN_API).newBuilder();
        urlBuilder.addQueryParameter("appid", appid);
        urlBuilder.addQueryParameter("secret", secret);
        urlBuilder.addQueryParameter("code", code);
        urlBuilder.addQueryParameter("grant_type", "authorization_code");
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, "wx_Login--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String respBody = response.body().string();
                LogUtil.e(TAG, "wx_Login--------->onSuccess11111" + respBody);
                try {
                    WXMesBean bean = GsonHelper.getSingleton().fromJson(respBody, WXMesBean.class);
                    callback.onSuccess(bean.getExpires_in(), bean);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e(TAG, "wx_Login--------->Exception" + e.getMessage());
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }
        });
    }

    /**
     * 登录
     */
    public static void login(String accessToken, final String openid, final String uuid, final Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        if (StringUtil.isNotEmpty(accessToken)){
            map.put("accessToken", accessToken);
        }
        map.put("openid", openid);
        map.put("uuid", uuid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_LOGIN), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "login--------->onSuccess" + response.body());
                BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                try {
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        SPUtils.getInstance().put(CURRENTTIME,System.currentTimeMillis());
                    }
                    if (bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_10028){
                        callback.onFailed(bean.getMessage());
                    }else if (bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_10022) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        LoginBean loginBean = GsonHelper.getSingleton().fromJson(bean.getData(), LoginBean.class);
                        callback.onSuccess(bean.getStatusCode(), loginBean);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "login--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 注册
     */
    public static void signUp(String accessToken, String openid, String uuid, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("openid", openid);
        map.put("uuid", uuid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_SIGNUP), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "signUp--------->onSuccess" + response.body());
                BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                try {
                    if (bean.getStatusCode()==Constants.SUCCESS_CODE || bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_10013){
                        LoginBean loginBean = GsonHelper.getSingleton().fromJson(bean.getData(), LoginBean.class);
                        callback.onSuccess(bean.getStatusCode(), loginBean);
                    }else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "signUp--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 绑定
     */
    public static void bindPhone(String mobile, String code, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        map.put("code", code);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_BIND_PHONE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "bindPhone--------->onSuccess" + response.body());
                BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                    callback.onSuccess(bean.getStatusCode(), bean);
                } else {
                    callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "bindPhone--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 获取验证码
     */
    public static void getVerCode(String mobile, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_GETVERCODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getVerCode--------->onSuccess" + response.body());
                BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                    callback.onSuccess(bean.getStatusCode(), bean);
                }else {
                    callback.onFailed(bean.getMessage());
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getVerCode--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 获取登录验证码
     */
    public static void getLoginVerCode(String mobile, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_PHONE_LOGIN_CODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getVerCode--------->onSuccess" + response.body());
                BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                    callback.onSuccess(bean.getStatusCode(), bean);
                }else {
                    callback.onFailed(bean.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getVerCode--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 手机验证登录
     */
    public static void loginPhone(String mobile, String code, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        map.put("code", code);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_PHONE_LOGIN), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "loginPhone--------->onSuccess" + response.body());
                BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                    LoginBean loginBean = GsonHelper.getSingleton().fromJson(bean.getData(), LoginBean.class);
                    callback.onSuccess(bean.getStatusCode(), loginBean);
                } else {
                    callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "loginPhone--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 设置支付密码
     */
    public static void settingPayPas(String paymentPwd, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("paymentPwd", paymentPwd);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_SETTINGPAYPAS), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "settingPayPas--------->onSuccess" + response.body());
                BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                    callback.onSuccess(bean.getStatusCode(), bean);
                } else {
                    callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "settingPayPas--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 查询零钱明细
     */
    public static void teamInactiveQuery(int page, int rows, String teamId, int timeQuantum, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("page", page + "");
        map.put("rows", rows + "");
        map.put("tid", teamId);
        map.put("timeQuantum", timeQuantum + "");

        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.TEAM_INACTIVEQUERY), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "sendRedPage--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        TeamInactiveBean detailsRedPacketBean = GsonHelper.getSingleton().fromJson(bean.getData(), TeamInactiveBean.class);
                        callback.onSuccess(bean.getStatusCode(), detailsRedPacketBean);
                    }else {
                        callback.onFailed(bean.getMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }


            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "teamInactiveActive--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }
        });


    }

    /**
     * 查询退群成员
     */
    public static void teamLeaveQuery(String teamId, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("tid", teamId);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.TEAM_EXITINFOQUERY), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "teamLeave--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        TeamLeaveBean detailsChangeQueryBean = GsonHelper.getSingleton().fromJson(bean.getData(), TeamLeaveBean.class);
                        callback.onSuccess(bean.getStatusCode(), detailsChangeQueryBean);
                    }else {
                        callback.onFailed(bean.getMessage());
                    }


                } catch (Exception e) {
                    callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }
        });
    }

    /**
     * 校验支付密码
     */
    public static void checkPwd(String paymentPwd, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("paymentPwd", paymentPwd);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_CHECKPWD), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "checkPwd--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "checkPwd--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 修改支付密码
     */
    public static void modifyPwd(String oldPaymentPwd, String paymentPwd, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("oldPaymentPwd", oldPaymentPwd);
        map.put("paymentPwd", paymentPwd);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_MODIFYPWD), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "modifyPwd--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "modifyPwd--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 找回支付密码——发送验证码
     */
    public static void retrievePaayPwdSendCode(String mobile, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_RETRIEVESEDECODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "retrievePaayPwdSendCode--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "retrievePaayPwdSendCode--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 找回支付密码——校验验证码
     */
    public static void retrievePaayPwdCheckCode(String mobile, String code, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        map.put("code", code);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_RETRIEVECHECKCODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "retrievePaayPwdCheckCode--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "retrievePaayPwdCheckCode--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 找回支付密码——重新设置支付密码
     */
    public static void resettingPayPwd(String code, String paymentPwd, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("paymentPwd", paymentPwd);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_RESETTINGPAYPWD), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "resettingPayPwd--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "resettingPayPwd--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 获取订单号 or 生成红包ID
     */
    public static void getOrderNumber(Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_GETORDERNUMBER), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getOrderNumber--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        OrderNumberBean orderNumberBean = GsonHelper.getSingleton().fromJson(bean.getData(), OrderNumberBean.class);
                        callback.onSuccess(bean.getStatusCode(), orderNumberBean);
                    } else {
                        callback.onSuccess(bean.getStatusCode(), bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getOrderNumber--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 主动退群接口
     */
    public static void leaveTeam(String tid, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("tid", tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.LEAVE_TEAM), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "LEAVE_TEAM--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE || bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_40014) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "LEAVE_TEAM--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }


    /**
     * 解散群接口
     */
    public static void removeTeam(String tid, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("tid", tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.REMOVE_TEAM), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "removeTeam--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE || bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_40013) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "removeTeam--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }


    /**
     * 踢人接口
     */
    public static void kickTeam(String tid, String member, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("tid", tid);
        map.put("member", member);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.KICK_TEAM), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "kickTeam--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE || bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_40014) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                Log.e(TAG, "踢人失败" + response.message());
                super.onError(response);
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 踢人接口,批量踢人
     */
    public static void kickTeamByOnce(String tid, String members, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("tid", tid);
        map.put("members", members);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.KICK_TEAM), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "kickTeamByOnce--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE || bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_40014) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 获取用户信息
     */

    public static void getUserInfo(String account, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("faccid", account);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.USER_INFO), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getUserInfo--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        UserInfoBean userInfo = GsonHelper.getSingleton().fromJson(bean.getData(), UserInfoBean.class);
                        callback.onSuccess(bean.getStatusCode(), userInfo);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getUserInfo--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });

    }

    /**
     * 发送红包
     */
    public static void sendRedPack(String rid, String money, String targetIds,
                                   String targetType, String name, String paymentPwd,
                                   String number,String targetId, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("rid", rid);
        map.put("money", money);
        map.put("targetIds", targetIds);
        map.put("targetType", targetType);
        map.put("name", name);
        map.put("paymentPwd", paymentPwd);
        map.put("number", number);
        map.put("targetId", targetId);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.REDPACK_SENDREDPACK), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "sendRedPack--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "sendRedPack--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 获取红包状态
     */
    public static void getRedPackStatistic(String rid, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("rid", rid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.REDPACK_GETREDPACKSTATISTIC), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getRedPackStatistic--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        RedPackOtherDataBean otherDataBean = GsonHelper.getSingleton().fromJson(bean.getData(), RedPackOtherDataBean.class);
                        callback.onSuccess(bean.getStatusCode(), otherDataBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getRedPackStatistic--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 获取红包状态（新加）
     */
    public static void getRedPackStatisticNew(String rid, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("rid", rid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.REDPACK_GETREDPACKSTATISTICNEW), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getRedPackStatisticNew--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        RedPacketStateBean redPacketStateBean = JSON.parseObject(bean.getData(), new TypeReference<RedPacketStateBean>() {});
                        callback.onSuccess(bean.getStatusCode(), redPacketStateBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getRedPackStatisticNew--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 领取红包
     */
    public static void getRedPack(String rid, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("rid", rid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.REDPACK_GETREDPACK), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getRedPack--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        RedPackOtherDataBean otherDataBean = GsonHelper.getSingleton().fromJson(bean.getData(), RedPackOtherDataBean.class);
                        callback.onSuccess(bean.getStatusCode(), otherDataBean);
                    } else if (bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_50004) {
                        callback.onSuccess(bean.getStatusCode(), bean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getRedPack--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 群主自动领取红包
     */
    public static void automaticGetRedPack(int page, int rows, String tid, String searchDate, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("page", page + "");
        map.put("rows", rows + "");
        map.put("tid", tid);
        map.put("searchDate", searchDate);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.REDPACK_AUTOMATICGETREDPACK), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "automaticGetRedPack--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        AutomaticGetRedPackBean redPackBean = GsonHelper.getSingleton().fromJson(bean.getData(), AutomaticGetRedPackBean.class);
                        callback.onSuccess(bean.getStatusCode(), redPackBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "automaticGetRedPack--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }

    /**
     * 战绩未结算列表
     */
    public static void settlementFailedList(int page, int rows, String tid, String searchDate, Object object, final requestCallback callback) {
        Map<String, String> map = new HashMap<>();
        map.put("page", page + "");
        map.put("rows", rows + "");
        map.put("tid", tid);
        map.put("searchDate", searchDate);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.SETTLEMENTFAILEDLIST), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "settlementFailedList--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE) {
                        SettlementBean settlementBean = GsonHelper.getSingleton().fromJson(bean.getData(), SettlementBean.class);
                        List<SettlementBean.ResultsBean> results = settlementBean.getResults();
                        List<SettlementBean.TeamRobotNotifyBean> list = new ArrayList<>();
                        for (SettlementBean.ResultsBean resultsBean : results){
                            byte[] decode = Base64Util.decodeMessage(resultsBean.getContent().replaceAll(" " , "+"));
                            String dataStr = new String(decode);
                            SettlementBean.TeamRobotNotifyBean robotNotifyBean = GsonHelper.getSingleton().fromJson(dataStr, SettlementBean.TeamRobotNotifyBean.class);
                            robotNotifyBean.setAchievementid(resultsBean.getAchievementid());
                            list.add(robotNotifyBean);
                        }
                        settlementBean.setDateBeans(list);
                        callback.onSuccess(bean.getStatusCode(), settlementBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(ERROR_REQUEST_EXCEPTION_MESSAGE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "settlementFailedList--------->onError");
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);
            }

        });
    }



    /**
     * 获取机器人列表
     * */
    public static void getRootList(Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GET_ROOT_LIST), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getRootList--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        RootListBean otherDataBean = GsonHelper.getSingleton().fromJson(bean.getData(), RootListBean.class);
                        callback.onSuccess(bean.getStatusCode(),otherDataBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getRootList--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取短信验证码（绑定支付宝校验）
     * */
    public static void getAliPayCode(String phone, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("mobile",phone);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETALIPAYBINDCODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getAliPayCode--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getMessage());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getAliPayCode--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }
    /**
     * 校验短信验证码（绑定支付宝校验）
     * */
    public static void checkAliPayCode(String phone, String code, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("mobile",phone);
        map.put("code",code);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.CHECKALIPAYBINDCODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "checkAliPayCode--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getMessage());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "checkAliPayCode--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 搜索群机器人
     * */
    public static void seachTeamRobot(String keyword, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("keyword",keyword);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.SEACHTEAMROBOT), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "seachTeamRobot--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        List<TeamRobotDetatlsBean> list = JSON.parseObject(bean.getData(), new TypeReference<ArrayList<TeamRobotDetatlsBean>>(){});
                        callback.onSuccess(bean.getStatusCode(),list);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "seachTeamRobot--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取群机器人详情
     * */
    public static void getTeamRobotDetatls(String tid, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETTEAMROBOTDETAILS), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getTeamRobotDetatls--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        TeamRobotDetatlsBean otherDataBean = GsonHelper.getSingleton().fromJson(bean.getData(), TeamRobotDetatlsBean.class);
                        callback.onSuccess(bean.getStatusCode(),otherDataBean);
                    } else if (bean.getStatusCode() == Constants.RESPONSE_CODE.CODE_60002){
                        callback.onSuccess(bean.getStatusCode(),bean.getMessage());
                    }else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getTeamRobotDetatls--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 对群助手的增删改
     * @param rid 群助手ID
     * @param tid 群ID
     * @param operatorType      操作码：1:添加；2：更换；3：删除             为2时  rid为新搜索到的群助手ID
     * */
    public static void operateTeamRobot(String rid, String tid, int operatorType, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("rid",rid);
        map.put("tid",tid);
        map.put("operatorType",operatorType + "");
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.OPERATETEAMROBOT), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "operateTeamRobot--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getMessage());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "operateTeamRobot--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 群内未领取红包记录
     * */
    public static void unclaimedRPDetails(int page, int rows, String tid, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("page",page + "");
        map.put("rows",rows + "");
        map.put("tid",tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.UNCLAIMEDREDPACKETDETAILS), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "unclaimedRPDetails--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        UnclaimedRPDetailsBean detailsBean = GsonHelper.getSingleton().fromJson(bean.getData(), UnclaimedRPDetailsBean.class);
                        callback.onSuccess(bean.getStatusCode(),detailsBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "unclaimedRPDetails--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 设置群配置
     * @param settlement 战绩自动结算  1开  0关闭
     * @param expsecond 退还群主红包时指定的失效时间, 传0为关闭
     * */
    public static void teamConfigSet(String tid, String expsecond, String settlement, Object object, final requestCallback callback){
        if (StringUtil.isEmpty(tid)){
            return;
        }
        Map<String,String> map = new HashMap<>();
        Team team = NimUIKit.getTeamProvider().getTeamById(tid);
        if (null == team || StringUtil.isEmpty(team.getCreator())){
            return;
        }
        String ownerId = team.getCreator();
        map.put("tid",tid);
        map.put("ownerId",ownerId);
        map.put("teamMemberProtect","0");
        map.put("expsecond",expsecond);
        map.put("regularClear","0");
        map.put("screenCapture","0");
        map.put("settlement",settlement);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.TEAMCONFIGSET), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "teamConfigSet--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getMessage());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "teamConfigSet--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取群配置
     * */
    public static void teamConfigGet(String tid, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.TEAMCONFIGGET), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "teamConfigGet--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        TeamConfigBean teamConfigBean = GsonHelper.getSingleton().fromJson(bean.getData(), TeamConfigBean.class);
                        callback.onSuccess(bean.getStatusCode(),teamConfigBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "teamConfigGet--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取用户名片信息
     * */
    public static void getUserBusinessCard(String uid, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("uid",uid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETUSERBUSINESSCARD), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getUserBusinessCard--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getUserBusinessCard--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 更新用户名片信息
     * */
    public static void upDateUserBusinessCard(String cardUrlInfo, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("cardUrlInfo",cardUrlInfo);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.UPDATEUSERBUSINESSCARD), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "upDateUserBusinessCard--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "upDateUserBusinessCard--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取修改绑定手机号验证码
     * */
    public static void getModifyBindPhoneCode(String mobile, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("mobile",mobile);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETMODIFYBINDPHONECODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getModifyBindPhoneCode--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getModifyBindPhoneCode--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 修改绑定手机号
     * */
    public static void modifyBindPhoneCode(String bindedMobile, String mobile, String code, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("bindedMobile",bindedMobile);
        map.put("mobile",mobile);
        map.put("code",code);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.MODIFYBINDPHONECODE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "modifyBindPhoneCode--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "modifyBindPhoneCode--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 查询群内被限制所有用户id列表接口
     * */
    public static void getNoCollarList(String tid, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETNOCOLLARLIST), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getNoCollarList--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        List<String> ids = JSON.parseObject(bean.getData(), new TypeReference<List<String>>(){});
                        callback.onSuccess(bean.getStatusCode(),ids);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getNoCollarList--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 限制用户收取群内红包
     * */
    public static void settingNoCollarId(String uids, String tid, String opt, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("uids",uids);
        map.put("tid",tid);
        map.put("opt",opt);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.SETTINGNOCOLLARID), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "settingNoCollarId--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "settingNoCollarId--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取群升级价格列表
     * */
    public static void getTeamAllocationPrice(String tid, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETTEAMALLOCATIONPRICE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getTeamAllocationPrice--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        TeamAllocationPriceBean priceBean = GsonHelper.getSingleton().fromJson(bean.getData(), TeamAllocationPriceBean.class);
                        callback.onSuccess(bean.getStatusCode(),priceBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getTeamAllocationPrice--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 群人数上限--续费
     * */
    public static void setTeamRenew(String tid, String levelCfgId, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        map.put("levelCfgId",levelCfgId);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.TEAMRENEW), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "setTeamRenew--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "setTeamRenew--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 群人数上限--升级
     * */
    public static void setTeamUpgrade(String tid, String levelCfgId, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        map.put("levelCfgId",levelCfgId);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.TEAMUPGRADE), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "setTeamUpgrade--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "setTeamUpgrade--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 建群
     * */
    public static void createTeam(String tname, String members, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tname",tname);
        map.put("members",members);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.CREATETEAM), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "createTeam--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "createTeam--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 拉人进群
     * */
    public static void addMember(String tid, String owner, String inviter, String members, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        map.put("owner",owner);
        map.put("inviter",inviter);
        map.put("members",members);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.ADDMEMBER), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "addMember--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "addMember--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取群钱包零钱
     * */
    public static void getTeamWalletInfo(String tid, String memberId, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        map.put("memberId",memberId);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETTEAMWALLETINFO), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getTeamWalletInfo--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        MyTeamWalletBean walletBean = GsonHelper.getSingleton().fromJson(bean.getData(), MyTeamWalletBean.class);
                        callback.onSuccess(bean.getStatusCode(),walletBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getTeamWalletInfo--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 设置成员钱包
     * */
    public static void setMemberWallet(String tid, String memberId, String score, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("tid",tid);
        map.put("memberId",memberId);
        map.put("score",score);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.SETMEMBERWALLET), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "setMemberWallet--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean.getData());
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "setMemberWallet--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取所有成员钱包余额
     * */
    public static void getAllMemberWallet(String tid, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("page","1");
        map.put("rows","1000");
        map.put("tid",tid);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETALLMEMBERWALLET), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getAllMemberWallet--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        GetAllMemberWalletBean walletBean = GsonHelper.getSingleton().fromJson(bean.getData(), GetAllMemberWalletBean.class);
                        callback.onSuccess(bean.getStatusCode(),walletBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getAllMemberWallet--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 获取收支明细
     * */
    public static void getTeamOrderList(int page, String tid, String memberId, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("page",page+"");
        map.put("rows","20");
        map.put("tid",tid);
        map.put("memberId",memberId);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.GETTEAMORDERLIST), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "getTeamOrderList--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        DetailsChangeQueryBean detailsChangeQueryBean = GsonHelper.getSingleton().fromJson(bean.getData(), DetailsChangeQueryBean.class);
                        callback.onSuccess(bean.getStatusCode(),detailsChangeQueryBean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "getTeamOrderList--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

    /**
     * 忽略未结算战绩
     * */
    public static void ignoreSettlement(String achievementId, Object object, final requestCallback callback){
        Map<String,String> map = new HashMap<>();
        map.put("achievementId",achievementId);
        RequestHelp.postRequest(StringUtil.stringformat(ApiUrl.IGNORESETTLEMNT), object, map, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                LogUtil.e(TAG, "ignoreSettlement--------->onSuccess" + response.body());
                try {
                    BaseBean bean = GsonHelper.getSingleton().fromJson(response.body(), BaseBean.class);
                    if (bean.getStatusCode() == Constants.SUCCESS_CODE){
                        callback.onSuccess(bean.getStatusCode(),bean);
                    } else {
                        callback.onFailed(bean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed(e.getMessage());
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.e(TAG, "ignoreSettlement--------->onError" + response.body());
                callback.onFailed(ERROR_REQUEST_FAILED_MESSAGE);

            }

        });
    }

}

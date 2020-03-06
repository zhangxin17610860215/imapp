package com.yqbj.ghxm.requestutils.api;

import com.yqbj.ghxm.config.Constants;

/**
 * URL
 */
public class ApiUrl {

//    public static String BASE_URL_HEAD = "http://";
//    public static String BASE_URL = "139.196.106.67";                       //测试服务器
//    public static String BASE_URL = "192.168.1.199";                      //服务端本地IP


    public static String BASE_URL_HEAD;
    public static String BASE_URL;
    public static String BASE_URL_DOMAIN = "/IM_new_server";

    public static String CHECK_VERSION;
    public static String OVERALL_GET_KEY;
    static {
        if (Constants.DEBUG){
            BASE_URL_HEAD = "http://";
            BASE_URL = "139.196.106.67";
            OVERALL_GET_KEY = BASE_URL_HEAD + BASE_URL + "/IM_new_key_server/app/im/access";
            CHECK_VERSION = BASE_URL_HEAD + BASE_URL + "/IM_new_key_server/app/version";
        }else {
            BASE_URL_HEAD = "https://";
            BASE_URL = "im.wulewan.cn";
            CHECK_VERSION = BASE_URL_HEAD + "gate.wulewan.cn" + "/app/version";
            OVERALL_GET_KEY = BASE_URL_HEAD + "gate.wulewan.cn" + "/app/im/access";
        }
    }

    public static String USER_LOGIN = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/interactive/user/login";
    public static String USER_SIGNUP = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/interactive/user/signup";
    public static String USER_BIND_PHONE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/interactive/binding/user/mobile";
    public static String USER_GETVERCODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/mobile/send/binding/code";
    public static String USER_PHONE_LOGIN_CODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/mobile/send/login/code";
    public static String USER_PHONE_LOGIN = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/interactive/user/mobile/login";
    public static String USER_SETTINGPAYPAS = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/wallet/create";
    public static String TEAM_INACTIVEQUERY = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/statistics/team/liveness";
    public static String TEAM_EXITINFOQUERY = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/statistics/team/leave";
    public static String USER_CHECKPWD = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/wallet/check/pwd";
    public static String USER_MODIFYPWD = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/wallet/modify/pwd";
    public static String USER_RETRIEVESEDECODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/mobile/send/modify/code";
    public static String USER_RETRIEVECHECKCODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/mobile/check/modify/code";
    public static String USER_RESETTINGPAYPWD = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/wallet/verify/modify/pwd";
    public static String USER_GETORDERNUMBER = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/order/number/generate";
    public static String LEAVE_TEAM = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/member/leave";
    public static String KICK_TEAM = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/kick/member";
    public static String REMOVE_TEAM = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/remove";
    public static String USER_INFO = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/user/info";
    public static String REDPACK_SENDREDPACK = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/redpack/send";
    public static String REDPACK_GETREDPACKSTATISTIC = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/statistics/user/redpack/get";
    public static String REDPACK_GETREDPACKSTATISTICNEW = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/redpack/search";
    public static String REDPACK_AUTOMATICGETREDPACK= BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/order/query/refund/owner/redpack";
    public static String REDPACK_GETREDPACK = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/redpack/get";
    public static String GET_ROOT_LIST = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/user/all/rebots";
    public static String GETALIPAYBINDCODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/mobile/send/alibinding/code";
    public static String CHECKALIPAYBINDCODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/mobile/check/alibinding/code";
    public static String SEACHTEAMROBOT = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/helper/robot/info";
    public static String GETTEAMROBOTDETAILS = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/helper/team/bound/info";
    public static String OPERATETEAMROBOT = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/helper/team/operate";
    public static String CONFIGINFO = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/app/config/info";
    public static String UNCLAIMEDREDPACKETDETAILS = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/order/query/unclaimed/tredpack";
    public static String TEAMCONFIGSET = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/config/set";
    public static String TEAMCONFIGGET = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/config/info";
    public static String GETUSERBUSINESSCARD = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/card/uinfo";
    public static String UPDATEUSERBUSINESSCARD = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/card/update/uinfo";
    public static String GETMODIFYBINDPHONECODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/mobile/send/modify/binding/code";
    public static String MODIFYBINDPHONECODE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/interactive/modify/binding/user/mobile";
    public static String GETNOCOLLARLIST = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/restrict/user/list";
    public static String SETTINGNOCOLLARID = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/restrict/get/redpack";
    public static String GETTEAMALLOCATIONPRICE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/level/list";
    public static String TEAMRENEW = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/level/renew";
    public static String TEAMUPGRADE = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/level/upgrade";
    public static String GETTEAMWALLETINFO = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/member/wallet/info";
    public static String ADDMEMBER = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/add/member";
    public static String CREATETEAM = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/create";
    public static String SETMEMBERWALLET = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/member/wallet/set";
    public static String GETALLMEMBERWALLET = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/member/wallet/all/info";
    public static String GETTEAMORDERLIST = BASE_URL_HEAD + BASE_URL + BASE_URL_DOMAIN + "/team/order/list";

}

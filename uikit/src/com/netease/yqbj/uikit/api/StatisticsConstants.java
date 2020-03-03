package com.netease.yqbj.uikit.api;

import com.netease.yqbj.uikit.bean.TeamConfigBean;

import java.util.ArrayList;

/**
 * 友盟统计用到的eventID统一定义
 * */
public class StatisticsConstants {

    public static final String DOWNLOADADDRESS = "https://fir.im/HCIM";

    public static final String ISREGULARCLEANMODE = "IsOpenRegularClear";                       //36小时定时清理
    public static final String REGULARCLEARTIME = "RegularClearTimestamp";                      //36小时定时清理开启的时间戳
    public static final String ISSCREENSHOT = "IsOpenScreenCapture";                            //截图通知
    public static final String ISSAFEMODE = "IsOpenMemberProtect";                              //群成员保护模式（安全模式）
    public static final String ISHAVEROBOT = "teamRobotUrl";                                    //是否有机器人（群助手）
    public static final String UPDATETEAMCONFIG = "updateTeamConfig";                           //固定修改群配置字符串
    public static final String RPRECEIVEDELAYTIME = "RPReceiveDelaytime";                       //红包延时设置
    public static final ArrayList<String> ROBOT_IDS = new ArrayList<>();                        //所有的机器人ID

    public static TeamConfigBean TEAMCONFIGBEAN;                                                //群配置内存中的数据

    public static final long DURATION = 129600000;                                                  //群消息定时清理的时长   默认36小时(129600000)
//    public static long DELETTIME;

    public static final String ACCID = "accid";

    //登录
    public static final String LOGIN_WCHATLOGIN = "01_01_01_clickWChatLogin";                                           //微信登录
    public static final String LOGIN_PHONELOGIN = "01_01_02_clickPhoneLogin";                                           //手机登录

    //群专属红包
    public static final String TEAM_EXCLUSIVE_RP_SEND_TOTALNUM = "02_01_01_sendExclusiveRedpacket";                     //群专属红包发出总次数
    public static final String TEAM_EXCLUSIVE_RP_SEND_SUCCESSNUM = "02_01_02_sendExclusiveRedpacketSuccess";            //群专属红包发出成功次数
    public static final String TEAM_EXCLUSIVE_RP_SEND_ERROR_CODEERRORNUM = "02_01_03_sendExclusiveSeriveFaild";         //群专属红包发出失败次数_服务器状态失败
    public static final String TEAM_EXCLUSIVE_RP_SEND_ERROR_ALIPAYERRORNUM = "02_01_04_sendExclusiveAlipayFaild";       //群专属红包发出失败次数_支付宝支付失败
    public static final String TEAM_EXCLUSIVE_RP_SEND_ERROR_WCHATERRORNUM = "02_01_05_sendExclusiveWChatFaild";         //群专属红包发出失败次数_微信支付失败
    public static final String TEAM_EXCLUSIVE_RP_SEND_ERROR_PASSWORDERRORNUM = "02_01_06_sendExclusivePasswordError";   //群专属红包发出失败次数_支付密码输入取消
    public static final String TEAM_EXCLUSIVE_RP_SEND_ERROR_OTHERERRORNUM = "02_01_07_sendExclusiveOtherError";         //群专属红包发出失败次数_未知错误

    //群普通红包
    public static final String TEAM_AVERAGE_RP_SEND_TOTALNUM = "02_02_01_sendAverageRedpacket";                         //群普通红包发出总次数
    public static final String TEAM_AVERAGE_RP_SEND_SUCCESSNUM = "02_02_02_sendAverageRedpacketSuccess";                //群普通红包发出成功次数
    public static final String TEAM_AVERAGE_RP_SEND_ERROR_CODEERRORNUM = "02_02_03_sendAverageSeriveFaild";             //群普通红包发出失败次数_服务器状态失败
    public static final String TEAM_AVERAGE_RP_SEND_ERROR_ALIPAYERRORNUM = "02_02_04_sendAverageAlipayFaild";           //群普通红包发出失败次数_支付宝支付失败
    public static final String TEAM_AVERAGE_RP_SEND_ERROR_WCHATERRORNUM = "02_02_05_sendAverageWChatFaild";             //群普通红包发出失败次数_微信支付失败
    public static final String TEAM_AVERAGE_RP_SEND_ERROR_PASSWORDERRORNUM = "02_02_06_sendAveragePasswordError";       //群普通红包发出失败次数_支付密码输入取消
    public static final String TEAM_AVERAGE_RP_SEND_ERROR_OTHERERRORNUM = "02_02_07_sendAverageOtherError";             //群普通红包发出失败次数_未知错误

    //群随机红包
    public static final String TEAM_RANDOM_RP_SEND_TOTALNUM = "02_03_01_sendRandomRedpacket";                           //群随机红包发出总次数
    public static final String TEAM_RANDOM_RP_SEND_SUCCESSNUM = "02_03_02_sendRandomRedpacketSuccess";                  //群随机红包发出成功次数
    public static final String TEAM_RANDOM_RP_SEND_ERROR_CODEERRORNUM = "02_03_03_sendRandomSeriveFaild";               //群随机红包发出失败次数_服务器状态失败
    public static final String TEAM_RANDOM_RP_SEND_ERROR_ALIPAYERRORNUM = "02_03_04_sendRandomAlipayFaild";             //群随机红包发出失败次数_支付宝支付失败
    public static final String TEAM_RANDOM_RP_SEND_ERROR_WCHATERRORNUM = "02_03_05_sendRandomWChatFaild";               //群随机红包发出失败次数_微信支付失败
    public static final String TEAM_RANDOM_RP_SEND_ERROR_PASSWORDERRORNUM = "02_03_06_sendRandomPasswordError";         //群随机红包发出失败次数_支付密码输入取消
    public static final String TEAM_RANDOM_RP_SEND_ERROR_OTHERERRORNUM = "02_03_07_sendRandomOtherError";               //群随机红包发出失败次数_未知错误

    //单人红包
    public static final String PERSONAL_RP_SEND_TOTALNUM = "02_04_01_sendPersonalRedpacket";                            //单人红包发出总次数
    public static final String PERSONAL_RP_SEND_SUCCESSNUM = "02_04_02_sendPersonalRedpacketSuccess";                   //单人红包发出成功次数
    public static final String PERSONAL_RP_SEND_ERROR_CODEERRORNUM = "02_04_03_sendPersonalSeriveFaild";                //单人红包发出失败次数_服务器状态失败
    public static final String PERSONAL_RP_SEND_ERROR_ALIPAYERRORNUM = "02_04_04_sendPersonalAlipayFaild";              //单人红包发出失败次数_支付宝支付失败
    public static final String PERSONAL_RP_SEND_ERROR_WCHATERRORNUM = "02_04_05_sendPersonalWChatFaild";                //单人红包发出失败次数_微信支付失败
    public static final String PERSONAL_RP_SEND_ERROR_PASSWORDERRORNUM = "02_04_06_sendPersonalPasswordError";          //单人红包发出失败次数_支付密码输入取消
    public static final String PERSONAL_RP_SEND_ERROR_OTHERERRORNUM = "02_04_07_sendPersonalOtherError";                //单人红包发出失败次数_未知错误

    public static final String REFUNDREDPACKET = "02_05_01_refundRedpacket";                                            //所有类型红包的退还次数

    public static final String NIMSENDRPMESSAGEERROR = "02_06_01_nimSendRpMessageError";                                //所有类型红包的消息发送失败次数（云信发送失败）

    //零钱包
    public static final String COIN_CARRY_TOTALNUM = "03_01_01_clickWithdraw";                                          //提现按钮点击次数
    public static final String COIN_ALIBIND_TOTALNUM = "03_02_01_clickCheckAlipayStateCell";                            //支付宝绑定按钮点击次数
    public static final String COIN_ALIBIND_SUCCESSNUM = "03_02_02_clickBindAlipayAccount";                             //支付宝绑定成功次数
    public static final String COIN_CHANGEDETAILSCELL = "03_03_01_clickCheckChangeDetailsCell";                         //零钱明细点击次数
    public static final String COIN_SENDRP_CHANGEDETAILSCELL = "03_04_01_clickMySendRedpacketDetail";                   //发出红包明细点击次数
    public static final String COIN_RECEIVEDRP_CHANGEDETAILSCELL = "03_04_02_clickReceivedRedpacketDetail";             //收到红包明细点击次数
    public static final String RECHARGEQUERYSERVICEERROR = "03_05_01_rechArgeQueryServiceError";                        //充值查询服务异常

    //群管理
    public static final String TEAM_MANAGER_DATA = "04_01_01_clickGroupData";                                           //群资料按钮点击次数
    public static final String SHAERGAMERECORD = "51_01_01_shareGameRecord";                                            //战绩分享成功次数
    public static final String TEAM_MANAGER_TEAMQRCODE = "04_02_01_clickGroupQRCode";                                   //群二维码点击次数
    public static final String TEAM_MANAGER_SCANNINGTEAMQRCODE = "04_03_01_scanningGroupQRCode";                        //扫群二维码点击次数
    public static final String TEAM_MANAGER_TEAMANNOUNCEMENTSUCCESS = "04_04_01_saveGroupAnnouncementSuccess";          //成功发出群公告次数
    public static final String TEAM_MANAGER_CREATETEANSUCCESS = "04_05_01_createGroupSuccess";                          //成功创建群数
    public static final String TEAM_MANAGER_DISBANDANSUCCESS = "04_05_02_disbandGroupSuccess";                          //成功解散群数
    public static final String TEAM_MANAGER_COPYNEWTEAM = "04_06_01_copyNewGroup";                                      //一键到新群点击次数
    public static final String TEAM_MANAGER_TRANSFERTEAM = "04_07_01_transferGroup";                                    //成功群主转让次数
    public static final String TEAM_MANAGER_SETTEAMMANAGER = "04_08_01_setGroupManage";                                 //设置群管理点击次数
    public static final String TEAM_MANAGER_MSGTOPPING = "04_09_01_clickMessageTopping";                                //置顶开关按钮点击次数
    public static final String TEAM_MANAGER_NODISTURB = "04_10_01_clickNoDisturb";                                      //群免打扰开关按钮点击次数
    public static final String TEAM_MANAGER_SCREENCASTNOTIFI = "04_11_01_clickScreencastNotifi";                        //截屏通知开关按钮点击次数
    public static final String TEAM_MANAGER_REGULARCLEANING = "04_12_01_clickRegularCleaning";                          //群消息定时清理开关按钮点击次数
    public static final String TEAM_MANAGER_TEAMBANNED = "04_13_01_clickGroupBanned";                                   //全群禁言开关按钮点击次数
    public static final String TEAM_MANAGER_PROTECTMEMBER = "04_14_01_clickProtectMember";                              //群成员保护模式开关按钮点击次数
    public static final String TEAM_MANAGER_TEAMAUTH = "04_15_01_clickGroupAuth";                                       //群认证开关按钮点击次数

}

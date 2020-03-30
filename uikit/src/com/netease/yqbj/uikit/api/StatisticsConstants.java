package com.netease.yqbj.uikit.api;

import com.netease.yqbj.uikit.bean.TeamConfigBean;

import java.util.ArrayList;

/**
 * 友盟统计用到的eventID统一定义
 * */
public class StatisticsConstants {

    public static final String ISREGULARCLEANMODE = "IsOpenRegularClear";                       //24小时定时清理
    public static final String REGULARCLEARTIME = "RegularClearTimestamp";                      //24小时定时清理开启的时间戳
    public static final String ISSCREENSHOT = "IsOpenScreenCapture";                            //截图通知
    public static final String ISSAFEMODE = "IsOpenMemberProtect";                              //群成员保护模式（安全模式）
    public static final String ISSETTLEMENT = "IsSettlement";                                   //战绩自动结算
    public static final String ISHAVEROBOT = "teamRobotUrl";                                    //是否有机器人（群助手）
    public static final String UPDATETEAMCONFIG = "updateTeamConfig";                           //固定修改群配置字符串
    public static final String RPRECEIVEDELAYTIME = "RPReceiveDelaytime";                       //红包延时设置
    public static final String INVITER = "inviter";                                             //邀请者
    public static final String TEAMID = "tid";                                                  //群id
    public static final ArrayList<String> ROBOT_IDS = new ArrayList<>();                        //所有的机器人ID

    public static TeamConfigBean TEAMCONFIGBEAN;                                                //群配置内存中的数据

    public static final long DURATION = 86400000;                                              //群消息定时清理的时长   默认24小时(86400000)
//    public static long DELETTIME;

    public static final String ACCID = "accid";
}

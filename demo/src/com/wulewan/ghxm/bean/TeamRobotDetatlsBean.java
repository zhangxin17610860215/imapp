package com.wulewan.ghxm.bean;

import java.io.Serializable;

public class TeamRobotDetatlsBean implements Serializable {

    /**
     * accid : 937029457
     * bindingUrl : http://192.168.1.174/robot/hcbind
     * createTime : 2019-06-21 19:37:33
     * description : 巴蜀麻将战绩自动分享
     * headUrl : http://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJgFxYzj6DK...uEOec3GADtvJgzo9g/96
     * nickname : 巴蜀麻将机器人
     */

    private String accid;
    private String bindingUrl;
    private String createTime;
    private String description;
    private String headUrl;
    private String nickname;
    private String developer;

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public String getBindingUrl() {
        return bindingUrl;
    }

    public void setBindingUrl(String bindingUrl) {
        this.bindingUrl = bindingUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

package com.yqbj.ghxm.bean;

import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.impl.cache.TeamDataCache;
import com.yqbj.ghxm.utils.StringUtil;

import java.io.Serializable;

public class SenderUserInfoBean implements Serializable {
    private String userName;
    private String userID;
    private String avatar;

    public String getUserName(String teamId) {
        if (StringUtil.isEmpty(teamId)){
            return userName;
        }
        TeamMember teamMember = TeamDataCache.getInstance().getTeamMember(teamId,getUserID());
        if (null != teamMember){
            if (StringUtil.isNotEmpty(teamMember.getTeamNick())){
                return teamMember.getTeamNick();
            }
            return userName;
        }else {
            return userName;
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}

package com.yqbj.ghxm.bean;

import android.text.TextUtils;

public class AliPayInfoBean {

    /**
     * avatar : https://tfs.alipayobjects.com/images/partner/T1xh8tXmFbXXXXXXXX
     * nickName : 换个发型，从头开始
     * userId : 2088902628043873
     */

    private String avatar;
    private String nickName;
    private String userId;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        if (TextUtils.isEmpty(nickName))
        {
            return "";
        }
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

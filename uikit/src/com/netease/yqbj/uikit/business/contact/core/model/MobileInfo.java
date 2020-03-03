package com.netease.yqbj.uikit.business.contact.core.model;

import android.text.TextUtils;

import com.netease.nimlib.sdk.uinfo.model.UserInfo;

public class MobileInfo implements UserInfo {

    private String phoneNum;
    private String displayName;
    private String avatar;

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getAccount() {
        if(TextUtils.isEmpty(this.phoneNum)){
            return "暂无号码";
        }else{
            return this.phoneNum.trim();
        }
    }

    @Override
    public String getName() {

        if (!TextUtils.isEmpty(this.displayName)){
            return this.displayName;
        }
        return "手机联系人";
    }

    @Override
    public String getAvatar() {
        return this.avatar;
    }


    @Override
    public String toString() {
        return "mobileINfo===》" + "[Display=" +getName()+"]" +"[PhoneNum" + getAccount() +"]";
    }
}

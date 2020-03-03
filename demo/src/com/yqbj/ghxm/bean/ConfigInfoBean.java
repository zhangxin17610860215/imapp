package com.yqbj.ghxm.bean;

public class ConfigInfoBean {

    /**
     * aliUpperLimit : 2000
     * userWalletExist : true
     * wxUpperLimit : 300
     */

    private String aliUpperLimit;
    private boolean userWalletExist;
    private String wxUpperLimit;

    public String getAliUpperLimit() {
        return aliUpperLimit;
    }

    public void setAliUpperLimit(String aliUpperLimit) {
        this.aliUpperLimit = aliUpperLimit;
    }

    public boolean isUserWalletExist() {
        return userWalletExist;
    }

    public void setUserWalletExist(boolean userWalletExist) {
        this.userWalletExist = userWalletExist;
    }

    public String getWxUpperLimit() {
        return wxUpperLimit;
    }

    public void setWxUpperLimit(String wxUpperLimit) {
        this.wxUpperLimit = wxUpperLimit;
    }
}

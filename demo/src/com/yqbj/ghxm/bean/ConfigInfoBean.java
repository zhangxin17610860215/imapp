package com.yqbj.ghxm.bean;

public class ConfigInfoBean {

    /**
     * aliUpperLimit : 2000
     * userWalletExist : true
     * wxUpperLimit : 300
     */

    private String downloadUrl;
    private String openPlatformUrl;
    private boolean userWalletExist;

    public void setUserWalletExist(boolean userWalletExist) {
        this.userWalletExist = userWalletExist;
    }

    public boolean isUserWalletExist() {
        return userWalletExist;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getOpenPlatformUrl() {
        return openPlatformUrl;
    }

    public void setOpenPlatformUrl(String openPlatformUrl) {
        this.openPlatformUrl = openPlatformUrl;
    }
}

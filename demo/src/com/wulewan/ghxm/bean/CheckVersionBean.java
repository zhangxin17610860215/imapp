package com.wulewan.ghxm.bean;

import java.io.Serializable;

public class CheckVersionBean implements Serializable {

    /**
     * compel : 1
     * description : 发现新版本啦，请升级到最新版本体验新功能~
     * downloadUrl : https://fir.im/HCIM
     * upgrade : 1
     * versionno : 1.0.5
     */

    private int compel;
    private String description;
    private String downloadUrl;
    private int upgrade;
    private String versionno;

    public int getCompel() {
        return compel;
    }

    public void setCompel(int compel) {
        this.compel = compel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(int upgrade) {
        this.upgrade = upgrade;
    }

    public String getVersionno() {
        return versionno;
    }

    public void setVersionno(String versionno) {
        this.versionno = versionno;
    }
}

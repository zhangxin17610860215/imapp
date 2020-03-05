package com.netease.yqbj.uikit.bean;

import java.io.Serializable;

public class TeamConfigBean implements Serializable {

    /**
     * expsecond : 0
     * id : 2605868126
     * identity : 2605868126
     * protect : 1
     * regularClear : 1
     * rollbackOwner : 0
     * screenCapture : 1
     */

    private int expsecond;
    private String id;
    private String identity;
    private int protect;
    private int regularClear;
    private int rollbackOwner;
    private int screenCapture;
    private int settlement;

    public int getSettlement() {
        return settlement;
    }

    public void setSettlement(int settlement) {
        this.settlement = settlement;
    }

    public int getExpsecond() {
        return expsecond;
    }

    public void setExpsecond(int expsecond) {
        this.expsecond = expsecond;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getProtect() {
        return protect;
    }

    public void setProtect(int protect) {
        this.protect = protect;
    }

    public int getRegularClear() {
        return regularClear;
    }

    public void setRegularClear(int regularClear) {
        this.regularClear = regularClear;
    }

    public int getRollbackOwner() {
        return rollbackOwner;
    }

    public void setRollbackOwner(int rollbackOwner) {
        this.rollbackOwner = rollbackOwner;
    }

    public int getScreenCapture() {
        return screenCapture;
    }

    public void setScreenCapture(int screenCapture) {
        this.screenCapture = screenCapture;
    }
}

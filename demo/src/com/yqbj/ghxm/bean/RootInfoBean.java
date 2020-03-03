package com.yqbj.ghxm.bean;

public class RootInfoBean {

    /**
     * identity : 10000059
     * appid : xialiao_v1
     * name : 零钱助手
     * accid : 10000059
     * id : 10000059
     * type : 1
     * token : 42330448180cd31ab825e647fe92cced
     */
    private String identity;
    private String appid;
    private String name;
    private String accid;
    private String id;
    private int type;
    private String token;

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdentity() {
        return identity;
    }

    public String getAppid() {
        return appid;
    }

    public String getName() {
        return name;
    }

    public String getAccid() {
        return accid;
    }

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getToken() {
        return token;
    }
}

package com.wulewan.ghxm.bean;

import java.io.Serializable;

public class WXMesBean implements Serializable {

    /**
     * access_token : 20_XjMEBpV46LbK_dPmkVNMotoSQsO2AuWEGmrVjTkux6PJ-GvR8-Trh8sQL0oXyD82ej2YK_8Ax7DThS7OFpWd6UBR1o8wYniFjVPdA6Z7H0w
     * expires_in : 7200
     * refresh_token : 20_HuvlwBvBNOwQgYMRs2JsTRUGXEmYFlNvbEQNMmKQqpuIH4SzESK5ATPRAOjc4YDuSENNgVpcxpyytt8sZKjWWEtrcd9AlpMs3_uc6Zv4O48
     * openid : onFnd570qiGgVnF_NUboGjU2C-mo
     * scope : snsapi_userinfo
     * unionid : oay9Hwo2aMJlmHpu9pIGCFa3YXS8
     */

    private String access_token;
    private int expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private String unionid;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }
}

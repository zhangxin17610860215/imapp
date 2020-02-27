package com.wulewan.ghxm.bean;

public class GetKeyBean {

    /**
     * apiToken : eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJUb2tlblR5cGVcIjpcInRva2VuX2FwaVwifSIsIlRva2VuVHlwZSI6InRva2VuX2FwaSIsImlzcyI6ImltX3NlcnZlciIsImV4cCI6MTU1NDcyMTM1MCwiaWF0IjoxNTU0NzIxMjkwLCJqdGkiOiJ4aWFsaWFvX3YxIn0.-sWVVR2nJ62x_SLrUW1rArFJUdr1mOV00MkuGtYTMyI
     * domain : 139.196.106.67
     * key : GMR11fH3t3x84lmvai8BPNBMFqm+sFtRBXS9IqM7j6aYnBSfSN3gvTfCVW3AgiDktcAnkWi6Qj6CdqIZjpO1ggHzbLWTQt1j+HtvCXrv5e9W8wzSg87WDOpv7pSOFQMx/er3txzIDJD564fLSd3vP19blnRpVMJNYlAhxV0Am9g=
     */

    private String apiToken;
    private String domain;
    private String key;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

package com.wulewan.ghxm.bean;

import com.google.gson.annotations.SerializedName;

public class WChatParamsBean {

    /**
     * appid : wxf8fd85aa6f55069a
     * noncestr : 7BRCwqp8ae7fWKwXGOpWP3Ui5DABMCkS
     * package : Sign=WXPay
     * partnerid : 1538235361
     * prepayid : wx051955155545094631fb28a11518089300
     * sign : 8C26145505517EFFC53F6FB189C011E1
     * timestamp : 1559735715
     * trade_no : 201906101244258617510000159
     */

    private String appid;
    private String noncestr;
    @SerializedName("package")
    private String packageX;
    private String partnerid;
    private String prepayid;
    private String sign;
    private String timestamp;
    private String trade_no;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getPackageX() {
        return packageX;
    }

    public void setPackageX(String packageX) {
        this.packageX = packageX;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTradeNo() {
        return trade_no;
    }

    public void settradeNo(String tradeNo) {
        this.trade_no = trade_no;
    }
}

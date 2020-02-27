package com.wulewan.ghxm.bean;

import java.util.List;
import java.util.Map;

public class RedPacketStateBean {

    /**
     * createDate : 2019-07-09 10:08:33
     * id : 2019070911093310533119124241
     * name : ¥ 0.18
     * payerId : 119124241
     * payerName : 😂 😚 😌 xgv
     * status : 1
     * targetType : 2
     */

    private String createDate;
    private String id;
    private String name;
    private String payerId;
    private String payerName;
    private String amount;
    private int number;
    private int status;
    private int targetType;

    public Map<String,TargetSignMap> targetSignMap;
    private List<String> payeeIdList;
    private String teamId;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<String> getPayeeIdList() {
        return payeeIdList;
    }

    public void setPayeeIdList(List<String> payeeIdList) {
        this.payeeIdList = payeeIdList;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public static class TargetSignMap{
        private int sign;
        private String uid;

        public int getSign() {
            return sign;
        }

        public void setSign(int sign) {
            this.sign = sign;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }
}

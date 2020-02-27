package com.wulewan.ghxm.bean;

import java.io.Serializable;
import java.util.List;

public class RedPackOtherDataBean implements Serializable {
    private String redId;
    private String redTitle;
    private String totalSum;
    private String redContent;
    private int count;
    private Integer redpacketType;
    /**
     * number : 2
     * records : [{"amount":"0.02","createDate":1556000200000,"id":"d44e7015bc0c47a8","payeeId":"10000008","payeeName":"vijay","remark":"领取红包","type":2}]
     * status : 2
     */

    private int number;
    private int status;
    private List<RecordsBean> records;
    private String teamId;
    private boolean exclusive;
    private int type;

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getRedId() {
        return redId;
    }

    public void setRedId(String redId) {
        this.redId = redId;
    }

    public String getRedTitle() {
        return redTitle;
    }

    public void setRedTitle(String redTitle) {
        this.redTitle = redTitle;
    }

    public String getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(String totalSum) {
        this.totalSum = totalSum;
    }

    public String getRedContent() {
        return redContent;
    }

    public void setRedContent(String redContent) {
        this.redContent = redContent;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getRedpacketType() {
        return redpacketType;
    }

    public void setRedpacketType(Integer redpacketType) {
        this.redpacketType = redpacketType;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<RecordsBean> getRecords() {
        return records;
    }

    public void setRecords(List<RecordsBean> records) {
        this.records = records;
    }

    public static class RecordsBean implements Serializable{
        /**
         * amount : 0.02
         * createDate : 1556000200000
         * id : d44e7015bc0c47a8
         * payeeId : 10000008
         * payeeName : vijay
         * remark : 领取红包
         * type : 2
         */

        private String amount;
        private long createDate;
        private String id;
        private String payeeId;
        private String payeeName;
        private String remark;
        private int type;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public long getCreateDate() {
            return createDate;
        }

        public void setCreateDate(long createDate) {
            this.createDate = createDate;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPayeeId() {
            return payeeId;
        }

        public void setPayeeId(String payeeId) {
            this.payeeId = payeeId;
        }

        public String getPayeeName() {
            return payeeName;
        }

        public void setPayeeName(String payeeName) {
            this.payeeName = payeeName;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    @Override
    public String toString() {
        return "RedPackOtherDataBean{" +
                "redId='" + redId + '\'' +
                ", redTitle='" + redTitle + '\'' +
                ", totalSum='" + totalSum + '\'' +
                ", redContent='" + redContent + '\'' +
                ", count=" + count +
                ", redpacketType=" + redpacketType +
                ", number=" + number +
                ", status=" + status +
                ", records=" + records +
                '}';
    }
}

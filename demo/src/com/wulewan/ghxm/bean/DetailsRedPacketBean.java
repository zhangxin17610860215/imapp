package com.wulewan.ghxm.bean;

import java.util.List;

public class DetailsRedPacketBean {

    /**
     * count : 1
     * results : [{"amount":6,"count":4,"createDate":1553845482000,"id":"107","name":"开心红包","number":5,"payerName":"0150","remark":"零钱红包","type":1}]
     */

    private int count;
    private String totalMoney;
    private List<ResultsBean> results;

    public int getCount() {
        return count;
    }

    public void setTotalMoney(String totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getTotalMoney() {
        return totalMoney;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ResultsBean> getResults() {
        return results;
    }

    public void setResults(List<ResultsBean> results) {
        this.results = results;
    }

    public static class ResultsBean {
        /**
         * amount : 6
         * count : 4
         * createDate : 1553845482000
         * id : 107
         * name : 开心红包
         * number : 5
         * payerName : 0150
         * remark : 零钱红包
         * type : 1
         */

        private String amount;
        private String count;
        private long createDate;
        private String id;
        private String name;
        private int number;
        private String payerName;
        private String payeeName;
        private String payeeId;
        private String payerId;
        private String remark;
        private int type;
        private int status;
        private String rid;

        public String getRid() {
            return rid;
        }

        public void setRid(String rid) {
            this.rid = rid;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getPayerName() {
            return payerName;
        }

        public void setPayerName(String payerName) {
            this.payerName = payerName;
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

        public String getPayeeId() {
            return payeeId;
        }

        public void setPayeeId(String payeeId) {
            this.payeeId = payeeId;
        }

        public String getPayerId() {
            return payerId;
        }

        public void setPayerId(String payerId) {
            this.payerId = payerId;
        }
    }
}

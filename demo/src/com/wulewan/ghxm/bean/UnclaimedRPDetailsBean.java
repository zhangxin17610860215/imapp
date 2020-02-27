package com.wulewan.ghxm.bean;

import java.util.List;

public class UnclaimedRPDetailsBean {

    /**
     * count : 4
     * results : [{"amount":"0.03","count":0,"createDate":1562577648000,"id":"2019070819404810525955150580","name":"¥ 0.03","number":1,"payerId":"955150580","payerName":"二贝呀！！！","remark":"零钱红包","type":1},{"amount":"0.01","count":0,"createDate":1562582398000,"id":"2019070819395810522955150580","name":"¥ 0.01","number":1,"payerId":"955150580","payerName":"二贝呀！！！","remark":"零钱红包","type":1},{"amount":"0.02","count":0,"createDate":1562584213000,"id":"2019070819401310523955150580","name":"¥ 0.02","number":1,"payerId":"955150580","payerName":"二贝呀！！！","remark":"零钱红包","type":1},{"amount":"0.01","count":0,"createDate":1562584949000,"id":"2019070819402910524955150580","name":"¥ 0.01","number":1,"payerId":"955150580","payerName":"二贝呀！！！","remark":"零钱红包","type":1}]
     */

    private int count;
    private List<ResultsBean> results;

    public int getCount() {
        return count;
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
         * amount : 0.09
         * count : 0
         * createDate : 1562638113000
         * id : 2019070911093310533119124241
         * name : ¥ 0.18
         * number : 2
         * payerId : 119124241
         * payerName : 😂 😚 😌 xgv
         * remark : 零钱红包
         * type : 2
         */

        private String amount;
        private int count;
        private long createDate;
        private String id;
        private String name;
        private int number;
        private String payerId;
        private String payerName;
        private String remark;
        private int type;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
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
}

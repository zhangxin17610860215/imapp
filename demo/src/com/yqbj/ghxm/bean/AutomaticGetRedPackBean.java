package com.yqbj.ghxm.bean;

import java.io.Serializable;
import java.util.List;

public class AutomaticGetRedPackBean implements Serializable {

    /**
     * count : 13
     * results : [{"amount":0.01,"createDate":"2019-07-10 15:50:28","id":"2019070910411410525955150580","identity":"2019070910411410525955150580","name":"¥ 0.01","payerId":"955150580","payerName":"二贝呀！！！","remark":"红包退还群主","rid":"2019070910411410525955150580","type":6},{"amount":0.02,"createDate":"2019-07-10 15:50:28","id":"2019070910412610526955150580","identity":"2019070910412610526955150580","name":"¥ 0.02","payerId":"955150580","payerName":"二贝呀！！！","remark":"红包退还群主","rid":"2019070910412610526955150580","type":6},{"amount":0.03,"createDate":"2019-07-10 15:50:28","id":"2019070910414310527955150580","identity":"2019070910414310527955150580","name":"¥ 0.03","payerId":"955150580","payerName":"二贝呀！！！","remark":"红包退还群主","rid":"2019070910414310527955150580","type":6},{"amount":0.04,"createDate":"2019-07-10 15:50:28","id":"2019070910431210528955150580","identity":"2019070910431210528955150580","name":"¥ 0.04","payerId":"955150580","payerName":"二贝呀！！！","remark":"红包退还群主","rid":"2019070910431210528955150580","type":6},{"amount":0.05,"createDate":"2019-07-10 15:50:28","id":"2019070910432810529955150580","identity":"2019070910432810529955150580","name":"¥ 0.05","payerId":"955150580","payerName":"二贝呀！！！","remark":"红包退还群主","rid":"2019070910432810529955150580","type":6}]
     * totalMoney : 0.51
     */

    private int count;
    private String totalMoney;
    private List<ResultsBean> results;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(String totalMoney) {
        this.totalMoney = totalMoney;
    }

    public List<ResultsBean> getResults() {
        return results;
    }

    public void setResults(List<ResultsBean> results) {
        this.results = results;
    }

    public static class ResultsBean implements Serializable {
        /**
         * amount : 0.01
         * createDate : 2019-07-10 15:50:28
         * id : 2019070910411410525955150580
         * identity : 2019070910411410525955150580
         * name : ¥ 0.01
         * payerId : 955150580
         * payerName : 二贝呀！！！
         * remark : 红包退还群主
         * rid : 2019070910411410525955150580
         * type : 6
         */

        private String amount;
        private String createDate;
        private String id;
        private String identity;
        private String name;
        private String payerId;
        private String payerName;
        private String remark;
        private String rid;
        private int type;
        private int targetType;

        public int getTargetType() {
            return targetType;
        }

        public void setTargetType(int targetType) {
            this.targetType = targetType;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
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

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
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

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getRid() {
            return rid;
        }

        public void setRid(String rid) {
            this.rid = rid;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}

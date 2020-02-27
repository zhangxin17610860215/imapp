package com.wulewan.ghxm.bean;

import java.io.Serializable;
import java.util.List;

public class TeamAllocationPriceBean implements Serializable {

    /**
     * cfgs : [{"id":3,"identity":3,"maxLimit":200,"price":3},{"id":2,"identity":2,"maxLimit":100,"price":2},{"id":1,"identity":1,"maxLimit":50,"price":1}]
     * endTime : 0
     * maxUsers : 30
     */

    private long endTime;
    private int maxUsers;
    private List<CfgsBean> cfgs;

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public List<CfgsBean> getCfgs() {
        return cfgs;
    }

    public void setCfgs(List<CfgsBean> cfgs) {
        this.cfgs = cfgs;
    }

    public static class CfgsBean implements Serializable {
        /**
         * id : 3
         * identity : 3
         * maxLimit : 200
         * price : 3
         */

        private int id;
        private int identity;
        private int maxLimit;
        private int price;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getIdentity() {
            return identity;
        }

        public void setIdentity(int identity) {
            this.identity = identity;
        }

        public int getMaxLimit() {
            return maxLimit;
        }

        public void setMaxLimit(int maxLimit) {
            this.maxLimit = maxLimit;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }
    }
}

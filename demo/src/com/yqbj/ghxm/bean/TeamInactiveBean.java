package com.yqbj.ghxm.bean;

import java.util.List;

public class TeamInactiveBean {

    /**
     * count : 2
     * results : [{"uid":"10000022","lastLoginDate":1555051653000,"userName":"海贼ym"},{"uid":"10000023","lastLoginDate":1554961257000,"userName":"你"}]
     */
    private int count;
    private List<ResultsEntity> results;

    public void setCount(int count) {
        this.count = count;
    }

    public void setResults(List<ResultsEntity> results) {
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public List<ResultsEntity> getResults() {
        return results;
    }

    public class ResultsEntity {
        /**
         * uid : 10000022
         * lastLoginDate : 1555051653000
         * userName : 海贼ym
         */
        private String uid;
        private long lastLoginDate;
        private String userName;

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setLastLoginDate(long lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUid() {
            return uid;
        }

        public long getLastLoginDate() {
            return lastLoginDate;
        }

        public String getUserName() {
            return userName;
        }
    }
}

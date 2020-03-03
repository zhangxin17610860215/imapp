package com.yqbj.ghxm.bean;

import java.util.List;

public class TeamLeaveBean {


    /**
     * count : 2
     * results : [{"leaveTime":1555051653000,"uid":"10000022","uname":"海贼ym"},{"leaveTime":1554961257000,"uid":"10000023","uname":"你"}]
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
         * leaveTime : 1555051653000
         * uid : 10000022
         * uname : 海贼ym
         */
        private long leaveTime;
        private String uid;
        private String uname;

        public void setLeaveTime(long leaveTime) {
            this.leaveTime = leaveTime;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }

        public long getLeaveTime() {
            return leaveTime;
        }

        public String getUid() {
            return uid;
        }

        public String getUname() {
            return uname;
        }
    }
}

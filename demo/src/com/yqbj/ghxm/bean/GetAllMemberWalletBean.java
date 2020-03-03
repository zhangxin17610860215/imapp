package com.yqbj.ghxm.bean;

import java.io.Serializable;
import java.util.List;

public class GetAllMemberWalletBean implements Serializable {

    /**
     * count : 4
     * results : [{"id":"277589409510000395","identity":"277589409510000395","score":0,"tid":"2775894095","uid":"10000395"},{"id":"2775894095156483627","identity":"2775894095156483627","score":0,"tid":"2775894095","uid":"156483627"},{"id":"2775894095605022178","identity":"2775894095605022178","score":0,"tid":"2775894095","uid":"605022178"},{"id":"2775894095636908565","identity":"2775894095636908565","score":0,"tid":"2775894095","uid":"636908565"}]
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

    public static class ResultsBean implements Serializable {
        /**
         * id : 277589409510000395
         * identity : 277589409510000395
         * score : 0
         * tid : 2775894095
         * uid : 10000395
         */

        private String id;
        private String identity;
        private int score;
        private String tid;
        private String uid;

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

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getTid() {
            return tid;
        }

        public void setTid(String tid) {
            this.tid = tid;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }
}

package com.yqbj.ghxm.bean;

import java.util.List;

public class DetailsChangeQueryBean {

    /**
     * count : 13
     * results : [{"createDate":"2020-02-28 17:07:26","id":"2020022817072611991156483627","remainingScore":"19997","score":"-9999","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:07:20","id":"2020022817072011990156483627","remainingScore":"29996","score":"-10000","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:07:14","id":"2020022817071411989156483627","remainingScore":"39996","score":"9999","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:07:09","id":"2020022817070811988156483627","remainingScore":"29997","score":"9999","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:07:03","id":"2020022817070211987156483627","remainingScore":"19998","score":"9999","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:06:58","id":"2020022817065711986156483627","remainingScore":"9999","score":"9999","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:06:51","id":"2020022817065011985156483627","remainingScore":"0","score":"-193","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:06:44","id":"2020022817064411984156483627","remainingScore":"193","score":"-321","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:06:39","id":"2020022817063811983156483627","remainingScore":"514","score":"200","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:06:31","id":"2020022817063011982156483627","remainingScore":"314","score":"100","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:06:16","id":"2020022817061611981156483627","remainingScore":"214","score":"-32","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:06:05","id":"2020022817060411980156483627","remainingScore":"246","score":"123","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"},{"createDate":"2020-02-28 17:05:59","id":"2020022817055811979156483627","remainingScore":"123","score":"123","title":"群主分数操作","type":8,"uid":"156483627","uname":"? ? ? xgv"}]
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
         * createDate : 2020-02-28 17:07:26
         * id : 2020022817072611991156483627
         * remainingScore : 19997
         * score : -9999
         * title : 群主分数操作
         * type : 8
         * uid : 156483627
         * uname : ? ? ? xgv
         */

        private String createDate;
        private String id;
        private String remainingScore;
        private String score;
        private String title;
        private int type;
        private String uid;
        private String operator;
        private String uname;

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
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

        public String getRemainingScore() {
            return remainingScore;
        }

        public void setRemainingScore(String remainingScore) {
            this.remainingScore = remainingScore;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }
    }
}

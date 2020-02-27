package com.wulewan.ghxm.bean;

import java.util.List;

public class DetailsChangeQueryBean {

    /**
     * count : 7
     * results : [{"amount":1,"createDate":1554971459000,"id":"201904111630531372","tile":"提现","type":4},{"amount":1,"createDate":1554970149000,"id":"201904111608391352","tile":"提现","type":4},{"amount":6,"createDate":1554187500000,"id":"e490b80e5c5547f8","tile":"红包退还","type":5},{"amount":6,"createDate":1553845482000,"id":"107","tile":"零钱红包","type":1},{"amount":1.4,"createDate":1553310000000,"id":"e838413ecd774d4d","tile":"红包退还","type":5},{"amount":2.8,"createDate":1552557293000,"id":"2","tile":"领取红包","type":2},{"amount":2,"createDate":1552530031000,"id":"1","tile":"领取红包","type":2}]
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
         * amount : 1
         * createDate : 1554971459000
         * id : 201904111630531372
         * tile : 提现
         * type : 4
         */

        private String amount;
        private long createDate;
        private String id;
        private String tile;
        private String remainingAmount;
        private int type;

        public String getRemainingAmount() {
            return remainingAmount;
        }

        public void setRemainingAmount(String remainingAmount) {
            this.remainingAmount = remainingAmount;
        }

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

        public String getTile() {
            return tile;
        }

        public void setTile(String tile) {
            this.tile = tile;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}

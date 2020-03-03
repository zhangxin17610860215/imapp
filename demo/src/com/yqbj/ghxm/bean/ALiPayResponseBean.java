package com.yqbj.ghxm.bean;

public class ALiPayResponseBean {

    /**
     * alipay_trade_app_pay_response : {"code":"10000","msg":"Success","app_id":"2019032063603734","auth_app_id":"2019032063603734","charset":"utf-8","timestamp":"2019-04-16 14:22:59","out_trade_no":"2019041614225619910000012","total_amount":"0.01","trade_no":"2019041622001443871028921516","seller_id":"2088331211860731"}
     * sign : NFOPhG9KkAsF7AZVZCSJykeqpkMOnw5qGcb3SWbW6aUYZK09nteEyOCoOOSUnrK2DeFFnOG/ciseWca0hMGn5KTWbB6aeCAnZS3ECV2+iU2cCJLboilCG40YFBiq+Dp+oLdvmNl1YwgcgP5g3KFwQ7bzlc2jBJLLCHBEgGx91kUAfbo+tcXdc2VAbJBoIw9rUhIk5DQLb3D84kfgQx6I8h2ldbVM6xNQDc8rGuq9QoBVn1WwtL+njv/L7SPX8w69c5NckHi4cJcY5blhabJwwW7rmaKZLgcqiSy03cSTvoScMQKLR7oO8/HtSRNsr2QvdQUhbtSF0hiaD6STcPbmKw==
     * sign_type : RSA2
     */

    private AlipayTradeAppPayResponseBean alipay_trade_app_pay_response;
    private String sign;
    private String sign_type;

    public AlipayTradeAppPayResponseBean getAlipay_trade_app_pay_response() {
        return alipay_trade_app_pay_response;
    }

    public void setAlipay_trade_app_pay_response(AlipayTradeAppPayResponseBean alipay_trade_app_pay_response) {
        this.alipay_trade_app_pay_response = alipay_trade_app_pay_response;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSign_type() {
        return sign_type;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }

    public static class AlipayTradeAppPayResponseBean {
        /**
         * code : 10000
         * msg : Success
         * app_id : 2019032063603734
         * auth_app_id : 2019032063603734
         * charset : utf-8
         * timestamp : 2019-04-16 14:22:59
         * out_trade_no : 2019041614225619910000012
         * total_amount : 0.01
         * trade_no : 2019041622001443871028921516
         * seller_id : 2088331211860731
         */

        private String code;
        private String msg;
        private String app_id;
        private String auth_app_id;
        private String charset;
        private String timestamp;
        private String out_trade_no;
        private String total_amount;
        private String trade_no;
        private String seller_id;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public String getAuth_app_id() {
            return auth_app_id;
        }

        public void setAuth_app_id(String auth_app_id) {
            this.auth_app_id = auth_app_id;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getOut_trade_no() {
            return out_trade_no;
        }

        public void setOut_trade_no(String out_trade_no) {
            this.out_trade_no = out_trade_no;
        }

        public String getTotal_amount() {
            return total_amount;
        }

        public void setTotal_amount(String total_amount) {
            this.total_amount = total_amount;
        }

        public String getTrade_no() {
            return trade_no;
        }

        public void setTrade_no(String trade_no) {
            this.trade_no = trade_no;
        }

        public String getSeller_id() {
            return seller_id;
        }

        public void setSeller_id(String seller_id) {
            this.seller_id = seller_id;
        }
    }
}

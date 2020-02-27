package com.wulewan.ghxm.bean;

public class ConfigInfoBean {

    /**
     * payButtonConfig : {"ali":1,"wechat":1}
     * userWalletExist : true
     * withdrawButtonConfig : {"ali":1,"wechat":1}
     */

    private PayButtonConfigBean payButtonConfig;
    private boolean userWalletExist;
    private WithdrawButtonConfigBean withdrawButtonConfig;
    private String aliUpperLimit;
    private String wxUpperLimit;

    public String getAliUpperLimit() {
        return aliUpperLimit;
    }

    public void setAliUpperLimit(String aliUpperLimit) {
        this.aliUpperLimit = aliUpperLimit;
    }

    public String getWxUpperLimit() {
        return wxUpperLimit;
    }

    public void setWxUpperLimit(String wxUpperLimit) {
        this.wxUpperLimit = wxUpperLimit;
    }

    public PayButtonConfigBean getPayButtonConfig() {
        return payButtonConfig;
    }

    public void setPayButtonConfig(PayButtonConfigBean payButtonConfig) {
        this.payButtonConfig = payButtonConfig;
    }

    public boolean isUserWalletExist() {
        return userWalletExist;
    }

    public void setUserWalletExist(boolean userWalletExist) {
        this.userWalletExist = userWalletExist;
    }

    public WithdrawButtonConfigBean getWithdrawButtonConfig() {
        return withdrawButtonConfig;
    }

    public void setWithdrawButtonConfig(WithdrawButtonConfigBean withdrawButtonConfig) {
        this.withdrawButtonConfig = withdrawButtonConfig;
    }

    public static class PayButtonConfigBean {
        /**
         * ali : 1
         * wechat : 1
         */

        private int ali;
        private int wechat;

        public int getAli() {
            return ali;
        }

        public void setAli(int ali) {
            this.ali = ali;
        }

        public int getWechat() {
            return wechat;
        }

        public void setWechat(int wechat) {
            this.wechat = wechat;
        }
    }

    public static class WithdrawButtonConfigBean {
        /**
         * ali : 1
         * wechat : 1
         */

        private int ali;
        private int wechat;

        public int getAli() {
            return ali;
        }

        public void setAli(int ali) {
            this.ali = ali;
        }

        public int getWechat() {
            return wechat;
        }

        public void setWechat(int wechat) {
            this.wechat = wechat;
        }
    }
}

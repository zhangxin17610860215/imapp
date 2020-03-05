package com.yqbj.ghxm.bean;

import java.io.Serializable;
import java.util.List;

public class TeamRobotNotifyBean implements Serializable {

    /**
     * shareType : 5
     * userCodeList :
     * title : 血战麻将 房间号:264077 2019-06-26 20:40:00
     */

    private int shareType;
    private String userCodeList;
    private String title;
    private List<ContentBean> content;
    private int settlementFlag;

    public int getSettlementFlag() {
        return settlementFlag;
    }

    public void setSettlementFlag(int settlementFlag) {
        this.settlementFlag = settlementFlag;
    }

    public List<ContentBean> getContent() {
        return content;
    }

    public void setContent(List<ContentBean> content) {
        this.content = content;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public String getUserCodeList() {
        return userCodeList;
    }

    public void setUserCodeList(String userCodeList) {
        this.userCodeList = userCodeList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public class ContentBean implements Serializable{
        private String line1;
        private String line2;
        private String line3;
        private String imageUrl;
        private String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getLine1() {
            return line1;
        }

        public void setLine1(String line1) {
            this.line1 = line1;
        }

        public String getLine2() {
            return line2;
        }

        public void setLine2(String line2) {
            this.line2 = line2;
        }

        public String getLine3() {
            return line3;
        }

        public void setLine3(String line3) {
            this.line3 = line3;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}

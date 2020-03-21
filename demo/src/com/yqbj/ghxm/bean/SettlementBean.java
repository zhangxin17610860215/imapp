package com.yqbj.ghxm.bean;

import java.io.Serializable;
import java.util.List;

public class SettlementBean implements Serializable {

    /**
     * count : 1
     * results : [{"achievementid":"211377","content":"eyJzaGFyZVR5cGUiOjUsInVzZXJDb2RlTGlzdCI6IiIsInRpdGxlIjoi6KGA5oiYN W8oCDmiL/pl7Tlj7c6MjExMzc3XG4yMDIwLTAzLTIwIDE0OjM0OjMyIiwiY29udGVudCI6W3sibGluZTMiOiI8aHRtbD48Ym9keT48Zm9udCBjb2xvcj1cIiNGRjAwMDBcIj7liIbmlbA6MTwvZm9udD48L2JvZHk PC9odG1sPiIsImxpbmUxIjoi5ZCN5a2X5pyA5aSa5LiD5Liq5a2XIiwiaW1hZ2VVcmwiOiJodHRwOi8vdGhpcmR3eC5xbG9nby5jbi9tbW9wZW4vdmlfMzIvUHBYak15Z2RjV1NxV1pMdDVMRHlSSjY0cDVacTlLUDJmdGxmdFdZM3BNeUNuWXBweVdWVDd1SWtjajk0UkQ4cFY3SUQ2VDc4c3dPakQ0cmlhWEtKamNBLzEzMiIsImxpbmUyIjoiPGh0bWw PGJvZHk PGZvbnQgY29sb3I9XCIjMDAwMDAwXCI SUQ6NTUzMTcxPC9mb250PjwvYm9keT48L2h0bWw IiwidXNlcklkIjoiOTQ2NjA3NjcyIn0seyJsaW5lMyI6IjxodG1sPjxib2R5Pjxmb250IGNvbG9yPVwiIzAwMDBGRlwiPuWIhuaVsDotMTwvZm9udD48L2JvZHk PC9odG1sPiIsImxpbmUxIjoia2lraSIsImltYWdlVXJsIjoiaHR0cDovL3RoaXJkd3gucWxvZ28uY24vbW1vcGVuL3ZpXzMyL1ZRaWNxbjNibFdGYm01MWQxWTBlbkNFc1p5VXJvWDVWc2ExYzk1aWJoVU5pYWlha0txS0M4M1NoS1RkaFBsUmZDTzlzZFB4U1o4MjZpYTdla0liZFJZSU9QckEvMTMyIiwibGluZTIiOiI8aHRtbD48Ym9keT48Zm9udCBjb2xvcj1cIiMwMDAwMDBcIj5JRDo1MjI1MjU8L2ZvbnQ PC9ib2R5PjwvaHRtbD4iLCJ1c2VySWQiOiIxMjgwNjkxNTgifV19","createDate":"2020-03-20 14:34:33"}]
     */

    private int count;
    private List<ResultsBean> results;
    private List<TeamRobotNotifyBean> dateBeans;

    public List<TeamRobotNotifyBean> getDateBeans() {
        return dateBeans;
    }

    public void setDateBeans(List<TeamRobotNotifyBean> dateBeans) {
        this.dateBeans = dateBeans;
    }

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
         * achievementid : 211377
         * content : eyJzaGFyZVR5cGUiOjUsInVzZXJDb2RlTGlzdCI6IiIsInRpdGxlIjoi6KGA5oiYN W8oCDmiL/pl7Tlj7c6MjExMzc3XG4yMDIwLTAzLTIwIDE0OjM0OjMyIiwiY29udGVudCI6W3sibGluZTMiOiI8aHRtbD48Ym9keT48Zm9udCBjb2xvcj1cIiNGRjAwMDBcIj7liIbmlbA6MTwvZm9udD48L2JvZHk PC9odG1sPiIsImxpbmUxIjoi5ZCN5a2X5pyA5aSa5LiD5Liq5a2XIiwiaW1hZ2VVcmwiOiJodHRwOi8vdGhpcmR3eC5xbG9nby5jbi9tbW9wZW4vdmlfMzIvUHBYak15Z2RjV1NxV1pMdDVMRHlSSjY0cDVacTlLUDJmdGxmdFdZM3BNeUNuWXBweVdWVDd1SWtjajk0UkQ4cFY3SUQ2VDc4c3dPakQ0cmlhWEtKamNBLzEzMiIsImxpbmUyIjoiPGh0bWw PGJvZHk PGZvbnQgY29sb3I9XCIjMDAwMDAwXCI SUQ6NTUzMTcxPC9mb250PjwvYm9keT48L2h0bWw IiwidXNlcklkIjoiOTQ2NjA3NjcyIn0seyJsaW5lMyI6IjxodG1sPjxib2R5Pjxmb250IGNvbG9yPVwiIzAwMDBGRlwiPuWIhuaVsDotMTwvZm9udD48L2JvZHk PC9odG1sPiIsImxpbmUxIjoia2lraSIsImltYWdlVXJsIjoiaHR0cDovL3RoaXJkd3gucWxvZ28uY24vbW1vcGVuL3ZpXzMyL1ZRaWNxbjNibFdGYm01MWQxWTBlbkNFc1p5VXJvWDVWc2ExYzk1aWJoVU5pYWlha0txS0M4M1NoS1RkaFBsUmZDTzlzZFB4U1o4MjZpYTdla0liZFJZSU9QckEvMTMyIiwibGluZTIiOiI8aHRtbD48Ym9keT48Zm9udCBjb2xvcj1cIiMwMDAwMDBcIj5JRDo1MjI1MjU8L2ZvbnQ PC9ib2R5PjwvaHRtbD4iLCJ1c2VySWQiOiIxMjgwNjkxNTgifV19
         * createDate : 2020-03-20 14:34:33
         */

        private String achievementid;
        private String content;
        private String createDate;

        public String getAchievementid() {
            return achievementid;
        }

        public void setAchievementid(String achievementid) {
            this.achievementid = achievementid;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }
    }

    public static class TeamRobotNotifyBean implements Serializable {

        /**
         * shareType : 5
         * userCodeList :
         * title : 血战麻将 房间号:264077 2019-06-26 20:40:00
         */

        private String achievementid;
        private int shareType;
        private String userCodeList;
        private String title;
        private List<TeamRobotNotifyBean.ContentBean> content;

        public String getAchievementid() {
            return achievementid;
        }

        public void setAchievementid(String achievementid) {
            this.achievementid = achievementid;
        }

        public List<TeamRobotNotifyBean.ContentBean> getContent() {
            return content;
        }

        public void setContent(List<TeamRobotNotifyBean.ContentBean> content) {
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
}

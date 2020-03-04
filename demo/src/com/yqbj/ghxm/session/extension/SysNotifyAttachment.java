package com.yqbj.ghxm.session.extension;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.yqbj.ghxm.utils.StringUtil;

public class SysNotifyAttachment extends CustomAttachment {

    private int msgType;
    private int cardType;
    private String msgTitle;
    private String msgDate;
    private String content;
    private String teamId;

    public SysNotifyAttachment(){
        super(CustomAttachmentType.SystemNotify);
    }

    @Override
    protected void parseData(JSONObject data) {

        msgType = data.getIntValue("msgType");
        cardType = data.getIntValue("msgType");
        JSONObject msgContent = data.getJSONObject("msgContent");
        msgTitle = msgContent.getString("msgTitle");
        msgDate = msgContent.getString("msgDate");
        content = msgContent.getString("content");
        teamId = msgContent.getString("teamId");


    }

    @Override
    protected JSONObject packData() {

        JSONObject json = new JSONObject();
        json.put("msgType",msgType);
        json.put("cardType",cardType);
        JSONObject jsonContent = new JSONObject();
        jsonContent.put("msgTitle",msgTitle);
        jsonContent.put("msgDate",msgDate);
        jsonContent.put("content",content);
        jsonContent.put("teamId",teamId);
        json.put("msgContent",jsonContent);
        return json;

    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = StringUtil.isNotEmpty(teamId) ? teamId : "";
    }

    public int getMsgType() {
        return msgType;
    }

    public int getCardType() {
        return cardType;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public String getMsgDate() {
        return msgDate;
    }

    public String getContent() {
        return content;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

package com.yqbj.ghxm.session.extension;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.yqbj.ghxm.bean.TeamRobotNotifyBean;
import com.yqbj.ghxm.utils.GsonHelper;

public class TeamRobotNotifyAttachment extends CustomAttachment {

    private TeamRobotNotifyBean otherDataBean;
    private int settlementFlag;

    public TeamRobotNotifyAttachment(){
        super(CustomAttachmentType.teamRobot);
    }

    @Override
    protected void parseData(JSONObject data) {
        Log.e("TAG>>>>>>>>>>>>>>>",data.toString());
        settlementFlag = data.getInteger("settlementFlag");
        otherDataBean = GsonHelper.getSingleton().fromJson(data.getString("content"), TeamRobotNotifyBean.class);
        otherDataBean.setSettlementFlag(settlementFlag);
    }

    @Override
    protected JSONObject packData() {

        JSONObject json = new JSONObject();
        json.put("msg",otherDataBean);
        return json;

    }

    public TeamRobotNotifyBean getOtherDataBean() {
        return otherDataBean;
    }

    public void setOtherDataBean(TeamRobotNotifyBean otherDataBean) {
        this.otherDataBean = otherDataBean;
    }
}

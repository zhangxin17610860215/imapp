package com.yqbj.ghxm.session.action;

import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yqbj.uikit.business.session.actions.BaseAction;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.session.extension.SysNotifyAttachment;

public class SysNotifyAction extends BaseAction {


    public SysNotifyAction() {
        super(R.drawable.admin_icon, R.string.add_buddy);
    }

    @Override
    public void onClick() {

        SysNotifyAttachment sysNotifyAttachment = new SysNotifyAttachment();
        sysNotifyAttachment.setCardType(0);
        sysNotifyAttachment.setMsgType(1);
        sysNotifyAttachment.setContent("gdsjhcgsdjmchbdsjchgdscjhcskjc高考倒计时复古宽松风寒咳嗽健身卡大黄峰科技时代科技啊手机的活动");
        sysNotifyAttachment.setMsgTitle("test");
        sysNotifyAttachment.setMsgDate("2019-02-23 13:23:54");

        IMMessage imMessage = MessageBuilder.createCustomMessage(getAccount(),SessionTypeEnum.P2P,"test",sysNotifyAttachment);
        sendMessage(imMessage);
    }
}

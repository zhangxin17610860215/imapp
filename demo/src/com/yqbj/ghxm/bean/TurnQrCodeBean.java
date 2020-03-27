package com.yqbj.ghxm.bean;

import java.io.Serializable;

public class TurnQrCodeBean implements Serializable {
    public String type; //个人还是群 1,个人，2,群
    public String id;   //群还是个人
    public String inviter;  //群邀请人

    @Override
    public String toString() {
        return "{" + "\"type\":\"" + type + "\"," + "\"id\":\"" + id + "\"," + "\"inviter\":\"" + inviter + "\"}";
    }
}

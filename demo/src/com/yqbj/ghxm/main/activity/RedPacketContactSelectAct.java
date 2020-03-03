package com.yqbj.ghxm.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.yqbj.ghxm.R;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;

public class RedPacketContactSelectAct extends ContactSelectActivity {

    public static void startActivityForResult(Context context, Option option, int requestCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA, option);
        intent.setClass(context, RedPacketContactSelectAct.class);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void setBar() {
        super.setBar();
        toolbar.setBackgroundResource(R.color.redpacket_theme);
    }
}

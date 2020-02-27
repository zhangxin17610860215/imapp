package com.wulewan.ghxm.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.wulewan.ghxm.R;
import com.netease.wulewan.uikit.business.contact.selector.activity.ContactSelectActivity;

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

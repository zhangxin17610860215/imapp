package com.netease.wulewan.uikit.business.ait.selector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.netease.wulewan.uikit.R;
import com.netease.wulewan.uikit.business.contact.selector.activity.ContactSelectActivity;

/**
 * Created by hzchenkang on 2017/6/21.
 */

public class AitContactSelectorActivityNew extends ContactSelectActivity {

    public static void startActivityForResult(Context context, Option option, int requestCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA, option);
        intent.putExtra("requestCode", requestCode);
        intent.setClass(context, AitContactSelectorActivityNew.class);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void setBar() {
        super.setBar();
        toolbar.setBackgroundResource(R.color.theme_color);
    }
}

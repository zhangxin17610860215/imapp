package com.netease.yqbj.uikit.common.activity;

import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.netease.yqbj.uikit.R;


public class BaseAct extends UI {

    public TextView tv_right;


    public void setToolbar(int logo, String title) {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        }
        if (logo != 0) {
            toolbar.setNavigationIcon(logo);
        }else {
            toolbar.setNavigationIcon(null);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView tv = (TextView) findViewById(R.id.toolbar_title);
            tv.setText(title);
        }
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            TextView tv = (TextView) findViewById(R.id.toolbar_title);
            tv.setText(title);
        }
        toolbar.setTitle("");
    }

    public void setTitleVisibility(int visibility){
        TextView tv = (TextView) findViewById(R.id.toolbar_title);
        tv.setVisibility(visibility);
    }



    public void setRightText(String text, final onToolBarListner mlistener){
        if(tv_right==null) {
            tv_right = (TextView) findViewById(R.id.tv_right);

        }
        tv_right.setText(text);
        if(mlistener!=null){
            tv_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mlistener.onRight();
                }
            });
        }
    }


    public interface onToolBarListner{

        public void onRight();
    }
}

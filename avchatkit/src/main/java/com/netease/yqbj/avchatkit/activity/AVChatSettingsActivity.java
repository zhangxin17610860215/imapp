package com.netease.yqbj.avchatkit.activity;

import android.os.Bundle;

import com.netease.yqbj.avchatkit.R;
import com.netease.yqbj.avchatkit.common.activity.NimToolBarOptions;
import com.netease.yqbj.avchatkit.common.activity.ToolBarOptions;
import com.netease.yqbj.avchatkit.common.activity.UI;

/**
 * Created by liuqijun on 7/19/16.
 * 注意:全局配置,不区分用户
 */
public class AVChatSettingsActivity extends UI {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.avchat_settings_layout);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.nrtc_settings;
        setToolBar(R.id.toolbar, options);


    }

}

package com.wulewan.ghxm.main.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.wulewan.ghxm.BuildConfig;
import com.wulewan.ghxm.R;
import com.netease.wulewan.uikit.api.wrapper.NimToolBarOptions;
import com.netease.wulewan.uikit.common.activity.ToolBarOptions;
import com.netease.wulewan.uikit.common.activity.UI;

public class AboutActivity extends UI {

    private TextView version;
    private TextView versionGit;
    private TextView versionDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        findViews();
        initViewData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        version = findView(R.id.version_detail);
        versionGit = findView(R.id.version_detail_git);
        versionDate = findView(R.id.version_detail_date);
    }

    private void initViewData() {
        version.setText("Version: " + BuildConfig.VERSION_NAME);
        versionGit.setText("Git Version: " + BuildConfig.GIT_REVISION);
        versionDate.setText("Build Date:" + BuildConfig.BUILD_DATE);
    }
}

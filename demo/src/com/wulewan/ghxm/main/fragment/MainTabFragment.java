package com.wulewan.ghxm.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.main.model.MainTab;
import com.netease.wulewan.uikit.common.fragment.TabFragment;
import com.netease.wulewan.uikit.common.ui.dialog.DialogMaker;


public abstract class MainTabFragment extends TabFragment {

    private boolean loaded = false;

    private MainTab tabData;

    protected abstract void onInit();

    protected boolean inited() {
        return loaded;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_tab_fragment_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void attachTabData(MainTab tabData) {
        this.tabData = tabData;
    }

    protected void showProgress(Context context, boolean isCancel){
        DialogMaker.showProgressDialog(context, context.getString(com.netease.wulewan.uikit.R.string.empty), isCancel);
    }

    protected void dismissProgress(){
        DialogMaker.dismissProgressDialog();
    }

    @Override
    public void onCurrent() {
        super.onCurrent();

        if (!loaded && loadRealLayout()) {
            loaded = true;
            onInit();
        }
    }

    public boolean loadRealLayout() {
        ViewGroup root = (ViewGroup) getView();
        if (root != null && tabData.layoutId != -1) {
            root.removeAllViewsInLayout();
            View.inflate(root.getContext(), tabData.layoutId, root);
        }
        return root != null;
    }
}

package com.yqbj.ghxm.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.main.model.MainTab;
import com.netease.yqbj.uikit.common.fragment.TabFragment;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;


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
        DialogMaker.showProgressDialog(context, context.getString(com.netease.yqbj.uikit.R.string.empty), isCancel);
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

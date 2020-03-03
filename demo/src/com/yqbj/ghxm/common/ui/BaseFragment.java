package com.yqbj.ghxm.common.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yqbj.ghxm.main.fragment.MainTabFragment;
import com.netease.yqbj.uikit.common.ToastHelper;

public abstract class BaseFragment extends MainTabFragment {
    private View rootView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView==null) {
            rootView = initCreateView(inflater, container, savedInstanceState);
        }
        ViewGroup viewGroup= (ViewGroup) rootView.getParent();
        if(viewGroup!=null){
            viewGroup.removeView(rootView);
        }

        return  rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onCurrent() {
    }

    public abstract void initView();

    /**
     * 子类实现初始化View操作
     */
    protected abstract View initCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public void toast(String str){
        ToastHelper.showToast(getActivity(),str);
    }

}

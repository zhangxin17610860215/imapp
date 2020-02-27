package com.wulewan.ghxm.team.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.AutomaticGetRedPackBean;
import com.wulewan.ghxm.common.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class DelayedCollectionRPFragment extends BaseFragment {

    private List<AutomaticGetRedPackBean.ResultsBean> list = new ArrayList<>();
    private List<AutomaticGetRedPackBean.ResultsBean> data = new ArrayList<>();

    @Override
    public void initView() {

    }

    @Override
    protected View initCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(),R.layout.fragment_delayedcollectionrp_layout,null);
        return view;
    }

    @Override
    protected void onInit() {
        Bundle bundle = getArguments();
        data = (List<AutomaticGetRedPackBean.ResultsBean>) bundle.getSerializable("data");
        Log.e("TAG",">>>>>>>>>>>>>>" + data.size());
    }
}

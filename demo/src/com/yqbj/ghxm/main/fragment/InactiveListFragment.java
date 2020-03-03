package com.yqbj.ghxm.main.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.TeamLeaveBean;
import com.yqbj.ghxm.contact.activity.UserProfileActivity;
import com.yqbj.ghxm.main.activity.TeamActiveInfoAct;
import com.yqbj.ghxm.main.model.TeamActiveTab;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.TimeUtils;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;


public class InactiveListFragment extends MainTabFragment {

    private SmartRefreshLayout refreshLayout;
    private RecyclerView mRecyclerView;
    private EasyRVAdapter mAssetsAdapter;

    private View noDataView;

    private int count;              //总数量
    private int page = 1;           //页码
    private int rows = 20;          //每页需要展示的数量

    private TeamLeaveBean.ResultsEntity resultsBean;
    private List<TeamLeaveBean.ResultsEntity> results;
    private List<TeamLeaveBean.ResultsEntity> list = new ArrayList<>();


    @Override
    protected void onInit() {
        initView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_tab_fragment_container, container, false);
    }

    public InactiveListFragment() {
        this.setContainerId(TeamActiveTab.EXITTEAM.fragmentId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }


    @Override
    public boolean loadRealLayout() {
        ViewGroup root = (ViewGroup) getView();
        if (root != null) {
            root.removeAllViewsInLayout();
            View.inflate(root.getContext(),R.layout.common_list_item_layout, root);
        }
        return root != null;
    }


//
    public void initView() {
        mRecyclerView = getView().findViewById(R.id.marginAssets_mRv);
        refreshLayout = getView().findViewById(R.id.refresh_layout);

        noDataView = getView().findViewById(R.id.ll_nodata);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                //下拉刷新
                initData();
            }
        });

        initData();
        initAdapter();

    }


    private void initAdapter(){

        mAssetsAdapter = new EasyRVAdapter(getContext(), list, R.layout.list_item_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                if (null == list || list.size() == 0) {
                    return;
                }
                TeamLeaveBean.ResultsEntity resultsBean = list.get(position);
                if (null == resultsBean) {
                    return;
                }

                HeadImageView imageView = viewHolder.getView(R.id.item_img);
                imageView.setIsRect(true);
                imageView.loadBuddyAvatar(resultsBean.getUid());
                TextView item_text = viewHolder.getView(R.id.item_text);
                item_text.setText(resultsBean.getUname() +"");
                TextView item_tip = viewHolder.getView(R.id.item_tips);
                item_tip.setText(TimeUtils.getDateToString(resultsBean.getLeaveTime(),TimeUtils.TIME_TYPE_04));



            }
        };
        mRecyclerView.setAdapter(mAssetsAdapter);
        mAssetsAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                //Item点击事件

                Log.e("uid====",list.get(position).getUid());

                UserProfileActivity.start(getContext(),list.get(position).getUid());
            }
        });

    }




    private void initData() {
        showProgress(getActivity(),false);
        UserApi.teamLeaveQuery( ((TeamActiveInfoAct)getActivity()).getTeamId(), getActivity(), new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
                TeamLeaveBean bean = (TeamLeaveBean) object;
                count = bean.getCount();
                results = bean.getResults();
                loadData();
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(getContext(),errMessage);
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh();
                }
            }
        });
    }


    private void loadData() {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh();
        }

        list.clear();
        list.addAll(results);
        mAssetsAdapter.notifyDataSetChanged();

        if(list==null || list.size()<1){
            noDataView.setVisibility(View.VISIBLE);
            TextView tv_noData = noDataView.findViewById(R.id.tv_noData_content);
            tv_noData.setText("暂无退群信息");
        }else{

            noDataView.setVisibility(View.GONE);
        }

    }


}

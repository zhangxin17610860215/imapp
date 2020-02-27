package com.wulewan.ghxm.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.main.activity.TeamActiveInfoAct;
import com.wulewan.ghxm.main.model.TeamActiveTab;
import com.wulewan.ghxm.team.activity.InactiveDetailInfoAct;


public class InactiveTeamPersonFragment extends MainTabFragment {

    private static final String TAG = InactiveTeamPersonFragment.class.getSimpleName();

    private View threeDays;
    private View oneWeek;
    private View oneMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_tab_fragment_container, container, false);
    }

    @Override
    protected void onInit() {
        findViews();
    }

    public InactiveTeamPersonFragment() {
        this.setContainerId(TeamActiveTab.INACTIVE.fragmentId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    public void findViews(){

        threeDays = getView().findViewById(R.id.threeDays);
        itemInit(threeDays,"3天不活跃",1);
        oneWeek = getView().findViewById(R.id.oneWeek);
        itemInit(oneWeek,"一周不活跃",2);
        oneMonth = getView().findViewById(R.id.oneMonth);
        itemInit(oneMonth,"一个月不活跃",3);


    }

    private void itemInit(View view, String title, final int tag){

        TextView textTitle = (TextView) view.findViewById(R.id.item_title);
        textTitle.setText(title);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doClick(tag);
            }
        });


    }

    @Override
    public boolean loadRealLayout() {
        ViewGroup root = (ViewGroup) getView();
        if (root != null) {
            root.removeAllViewsInLayout();
            View.inflate(root.getContext(),R.layout.inactive_item_list_frag, root);
        }
        return root != null;
    }

    private void doClick(int tag){
        switch (tag){
            case 1:
                InactiveDetailInfoAct.start(getContext(),((TeamActiveInfoAct)getActivity()).getTeamId(),1);

                break;
            case 2:
                InactiveDetailInfoAct.start(getContext(),((TeamActiveInfoAct)getActivity()).getTeamId(),2);
                break;
            case 3:
                InactiveDetailInfoAct.start(getContext(),((TeamActiveInfoAct)getActivity()).getTeamId(),3);
                break;
            default:
                break;
        }
    }


}

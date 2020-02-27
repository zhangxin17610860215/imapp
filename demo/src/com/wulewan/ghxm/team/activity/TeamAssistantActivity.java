package com.wulewan.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.common.ui.imageview.HeadImageView;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.TeamRobotDetatlsBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.utils.StringUtil;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;

public class TeamAssistantActivity extends BaseAct {

    private Context context;

    private LinearLayout ll_noDataTips;                         //没有机器人时显示
    private TextView tv_noDataTips;                             //是群主：请通过搜索查找你要添加的群助手   不是群主：群主还没有开通群助手功能
    private RecyclerView recyclerView;
    private RelativeLayout rl_seach;                            //是群主时显示
    private TextView tv_seach;                                  //搜索
    private TextView tv_nodata;                                 //搜索无数据时显示
    private EditText et_seach;

    private EasyRVAdapter mAdapter;
    private List<TeamRobotDetatlsBean> list = new ArrayList<>();
    private TeamRobotDetatlsBean robotBean;

    private boolean isGroupOwner = false;                       //是否是群主
    private String teamId = "";

    public static void start(Context context, String teamId, boolean isGroupOwner) {
        Intent intent = new Intent();
        intent.setClass(context, TeamAssistantActivity.class);
        intent.putExtra("teamId", teamId);
        intent.putExtra("isGroupOwner", isGroupOwner);
        context.startActivity(intent);
    }

    public static void start(Context context, String teamId, TeamRobotDetatlsBean robotBean, boolean isGroupOwner) {
        Intent intent = new Intent();
        intent.setClass(context, TeamAssistantActivity.class);
        intent.putExtra("teamId", teamId);
        intent.putExtra("isGroupOwner", isGroupOwner);
        intent.putExtra("bindBean", robotBean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_assistant_layout);
        context = this;

        isGroupOwner = getIntent().getBooleanExtra("isGroupOwner",false);
        teamId = getIntent().getStringExtra("teamId");
        robotBean = (TeamRobotDetatlsBean) getIntent().getSerializableExtra("bindBean");

        initView();

        setToolbar(R.drawable.jrmf_b_top_back,"群助手");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list.size() <= 0){
            showPage();
        }else {
            mAdapter.notifyDataSetChanged();
        }
        if (isGroupOwner){
            queryRobot();
        }
    }

    private void initView() {
        ll_noDataTips = (LinearLayout) findViewById(R.id.ll_noDataTips);
        tv_noDataTips = (TextView) findViewById(R.id.tv_noDataTips);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        rl_seach = (RelativeLayout) findViewById(R.id.rl_seach);
        tv_seach = (TextView) findViewById(R.id.tv_seach);
        tv_nodata = (TextView) findViewById(R.id.tv_nodata);
        et_seach = (EditText) findViewById(R.id.et_seach);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        tv_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtil.isEmpty(et_seach.getText().toString().trim())){
                    toast("请输入要查询的群助手关键字");
                    return;
                }
                seachTeamAssistant();
            }
        });
    }

    private void queryRobot() {
        showProgress(context, false);
        UserApi.getTeamRobotDetatls(teamId, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    robotBean = (TeamRobotDetatlsBean) object;
                }else {
                    robotBean = null;
                }
                if (null != list && list.size() > 0){
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
            }
        });
    }

    /**
     * 搜索群助手
     * */
    private void seachTeamAssistant() {
        showProgress(context,false);
        UserApi.seachTeamRobot(et_seach.getText().toString().trim(), context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                ll_noDataTips.setVisibility(View.GONE);
                if (code == Constants.SUCCESS_CODE){
                    List<TeamRobotDetatlsBean> listBean = (List<TeamRobotDetatlsBean>) object;
                    if (null != listBean && listBean.size() > 0){
                        //搜索有数据
                        recyclerView.setVisibility(View.VISIBLE);
                        tv_nodata.setVisibility(View.GONE);

                        list.clear();
                        list.addAll(listBean);

                        loadData();
                    }else {
                        //搜索无数据
                        recyclerView.setVisibility(View.GONE);
                        tv_nodata.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    /**
     * 根据是否是群主显示不同页面
     * */
    private void showPage(){
        if (isGroupOwner){
            //是群主
            rl_seach.setVisibility(View.VISIBLE);
            tv_noDataTips.setText("请通过搜索查找你要添加的群助手");
        }else {
            //不是群主
            rl_seach.setVisibility(View.GONE);
            tv_noDataTips.setText("群主还没有开通群助手功能");
        }

        ll_noDataTips.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * 显示搜索后的列表页面
     * */
    private void loadData() {
        mAdapter = new EasyRVAdapter(context,list,R.layout.robot_item_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                HeadImageView imgHead = viewHolder.getView(R.id.img_robotHade);
                final TextView tvAdd = viewHolder.getView(R.id.tv_robot_add);
                TextView tvName = viewHolder.getView(R.id.tv_robot_name);
                TextView tvDescribe = viewHolder.getView(R.id.tv_robot_describe);
                imgHead.setIsRect(true);
                final TeamRobotDetatlsBean bean = list.get(position);
                GlideUtil.loadHavePlaceholderImageView(context,bean.getHeadUrl(),R.mipmap.robot_bgicon,imgHead);
                tvName.setText(bean.getNickname());
                tvDescribe.setText(bean.getDescription());
                tvAdd.setEnabled(true);

                if (null != robotBean && StringUtil.isNotEmpty(robotBean.getAccid())){
                    if (robotBean.getAccid().equals(bean.getAccid())){
                        tvAdd.setText("已添加");
                        tvAdd.setTextColor(getResources().getColor(R.color.theme_color));
                        tvAdd.setBackgroundColor(Color.WHITE);
                        tvAdd.setEnabled(false);
                    }else {
                        //更换群助手
                        tvAdd.setText("添加");
                        tvAdd.setTextColor(Color.WHITE);
                        tvAdd.setBackgroundResource(R.drawable.round_theme);
                        tvAdd.setEnabled(true);
                        tvAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EasyAlertDialogHelper.showCommonDialog(context, null, "添加该群助手将更换原有群助手，确定添加吗？", "添加", "取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                                    @Override
                                    public void doCancelAction() {

                                    }

                                    @Override
                                    public void doOkAction() {
                                        operateTeamRobot(bean.getAccid(),2,bean,"2",tvAdd);
                                    }
                                }).show();
                            }
                        });
                    }
                }else {
                    //添加群助手
                    tvAdd.setText("添加");
                    tvAdd.setTextColor(Color.WHITE);
                    tvAdd.setBackgroundResource(R.drawable.round_theme);
                    tvAdd.setEnabled(true);
                    tvAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            operateTeamRobot(bean.getAccid(),1,bean,"2",tvAdd);
                        }
                    });
                }
            }
        };
        mAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                TeamRobotDetatlsBean seachTeamRobotBean = (TeamRobotDetatlsBean) item;
                String type = "1";
                if (null != robotBean && StringUtil.isNotEmpty(robotBean.getAccid()) && robotBean.getAccid().equals(seachTeamRobotBean.getAccid())){
                    type = "2";
                }else {
                    type = "1";
                }
                TeamAssistantDetailsActivity.start(context,teamId,robotBean,seachTeamRobotBean,type);
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * 对机器人的操作
     * operatorType   1(添加)  2（更换）  3（删除）
     * */
    private void operateTeamRobot(String rid, final int operatorType, final TeamRobotDetatlsBean bean, final String type, final TextView tvAdd) {
        showProgress(context,false);
        UserApi.operateTeamRobot(rid, teamId, operatorType, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    if (operatorType == 1){
                        toast("群助手添加成功");
                    }else {
                        toast("群助手更换成功");
                    }
                    robotBean = null;
                    robotBean = bean;
                    tvAdd.setText("已添加");
                    tvAdd.setTextColor(getResources().getColor(R.color.theme_color));
                    tvAdd.setBackgroundColor(Color.WHITE);
                    mAdapter.notifyDataSetChanged();
                    TeamAssistantDetailsActivity.start(context,teamId,bean,null,type);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

}

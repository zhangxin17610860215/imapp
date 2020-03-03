package com.yqbj.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.netease.nimlib.sdk.team.model.TeamMember;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.TeamRobotNotifyBean;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.netease.yqbj.uikit.common.util.GlideUtil;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.utils.StringUtil;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;

public class SeeRecordActivity extends BaseAct {

    private Context context;
    private TeamRobotNotifyBean otherDataBean;
    private TextView tv_title;
    private TextView tv_time;
    private RecyclerView recyclerView;
    private List<TeamRobotNotifyBean.ContentBean> list;
    private EasyRVAdapter mAdapter;
    private String teamId = "";

    public static void start(Context context, TeamRobotNotifyBean otherDataBean, String teamId) {
        Intent intent = new Intent();
        intent.setClass(context, SeeRecordActivity.class);
        intent.putExtra("otherDataBean",otherDataBean);
        intent.putExtra("teamId",teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seerecord_layout);
        context = this;
        setToolbar(R.drawable.jrmf_b_top_back,"战绩详情");

        initView();
        initData();
    }

    private void initData() {
        otherDataBean = (TeamRobotNotifyBean) getIntent().getSerializableExtra("otherDataBean");
        teamId = getIntent().getStringExtra("teamId");
        if (null == otherDataBean){
            return;
        }

        if (StringUtil.isNotEmpty(otherDataBean.getTitle())){
            String[] split = otherDataBean.getTitle().split("\n");
            if (null != split && split.length >= 2){
                tv_time.setVisibility(View.VISIBLE);
                tv_title.setText(split[0]);
                tv_time.setText(split[1]);
            }else {
                tv_title.setText(otherDataBean.getTitle());
                tv_time.setVisibility(View.GONE);
            }
        }
        list = new ArrayList<>();
        list.clear();

        if (null == otherDataBean.getContent()){
            return;
        }
        list.addAll(otherDataBean.getContent());

        mAdapter = new EasyRVAdapter(context,list,R.layout.team_robotnotify_item_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                TeamRobotNotifyBean.ContentBean bean = list.get(position);
                HeadImageView imgHead = viewHolder.getView(R.id.img_head);
                TextView tvLind1 = viewHolder.getView(R.id.tv_lind1);
                TextView tvLind2 = viewHolder.getView(R.id.tv_lind2);
                TextView tvLind3 = viewHolder.getView(R.id.tv_lind3);
                View isLast = viewHolder.getView(R.id.isLast);
                imgHead.setIsRect(true);
                GlideUtil.loadHavePlaceholderImageView(context,bean.getImageUrl(),R.mipmap.robot_bgicon,imgHead);

                tvLind1.setText(bean.getLine1());
                tvLind2.setText(Html.fromHtml(bean.getLine2()));
                tvLind3.setText(Html.fromHtml(bean.getLine3()));

                if (position == list.size() -1){
                    isLast.setVisibility(View.GONE);
                }else {
                    isLast.setVisibility(View.VISIBLE);
                }
            }
        };

        mAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position, Object item) {
                if (null == list || list.isEmpty()){
                    return;
                }
                if (StringUtil.isEmpty(teamId)){
                    return;
                }

                if (list.size() > 0){
                    TeamRobotNotifyBean.ContentBean bean = list.get(position);
                    if (null == bean || StringUtil.isEmpty(bean.getUserId())){
                        toast("该用户暂未绑定公会小蜜号");
                        return;
                    }
                }
                List<TeamMember> members = NimUIKit.getTeamProvider().getTeamMemberList(teamId);
                if (null == members || members.isEmpty() || members.size() <= 0){
                    return;
                }
                boolean isStart = false;
                for (TeamMember member : members){
                    if (member.getAccount().equals(list.get(position).getUserId())){
                        isStart = true;
                    }
                }
                if (isStart){
                    AdvancedTeamMemberInfoAct.startActivityForResult((Activity) context, list.get(position).getUserId(), teamId);
                }else {
                    toast("该群没有此用户无法查看详情");
                }
            }
        });

        recyclerView.setAdapter(mAdapter);
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_time = (TextView) findViewById(R.id.tv_time);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }
}

package com.wulewan.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.TeamRobotDetatlsBean;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.common.ui.imageview.HeadImageView;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.utils.StringUtil;

public class TeamAssistantDetailsActivity extends BaseAct implements View.OnClickListener {

    private Context context;

    private HeadImageView imgHead;
    private TextView tvName;
    private TextView tvReplace;
    private TextView tvDelete;
    private TextView tvAdd;
    private TextView tvTitle;
    private TextView tvUpdateTime;
    private TextView tvDeveloper;
    private TextView tvStatement;

    private TeamRobotDetatlsBean bindBean;              //从服务端返回已绑定的Bean
    private TeamRobotDetatlsBean newBean;               //点击item传过来的Bean
    private TeamRobotDetatlsBean bean;                  //页面需要展示数据的bean
    private String teamId = "";
    private String type = "";
    private String rid = "";

    public static void start(Context context, String teamId, TeamRobotDetatlsBean bindBean, TeamRobotDetatlsBean newBean, String type) {
        Intent intent = new Intent();
        intent.setClass(context, TeamAssistantDetailsActivity.class);
        intent.putExtra("teamId", teamId);
        intent.putExtra("type", type);
        intent.putExtra("bindBean", bindBean);
        intent.putExtra("newBean", newBean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_assistant_details_layout);

        context = this;
        initView();
        initData(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    private void initData(Intent intent) {
        if (null == intent){
            newBean = (TeamRobotDetatlsBean) getIntent().getSerializableExtra("newBean");
            bindBean = (TeamRobotDetatlsBean) getIntent().getSerializableExtra("bindBean");
            teamId = getIntent().getStringExtra("teamId");
            type = getIntent().getStringExtra("type");
        }else {
            newBean = (TeamRobotDetatlsBean) intent.getSerializableExtra("newBean");
            bindBean = (TeamRobotDetatlsBean) intent.getSerializableExtra("bindBean");
            teamId = intent.getStringExtra("teamId");
            type = intent.getStringExtra("type");
        }

        if (null == newBean){
            if (null == bindBean){
                return;
            }else {
                bean = bindBean;
            }
        }else {
            bean = newBean;
        }
        rid = bean.getAccid();
        setToolbar(R.drawable.jrmf_b_top_back,bean.getNickname());
        GlideUtil.loadHavePlaceholderImageView(context,bean.getHeadUrl(),R.mipmap.robot_bgicon,imgHead);
        tvName.setText(bean.getNickname());
        tvTitle.setText(bean.getDescription());
        tvUpdateTime.setText("更新时间：" + bean.getCreateTime());
        tvDeveloper.setText("开发者：" + bean.getDeveloper());
        tvStatement.setText("免责声明：本服务由" + bean.getDeveloper() + "提供。相关服务和责任将由该公司承担。如有问题请咨询该公司客服。");

        tvAdd.setVisibility(View.GONE);
        tvDelete.setVisibility(View.GONE);
        tvReplace.setVisibility(View.GONE);
        if (StringUtil.isNotEmpty(type)){
            if (type.equals("1")){
                //只显示添加按钮
                tvAdd.setVisibility(View.VISIBLE);
            }else {
                //只显示删除更换按钮
                tvDelete.setVisibility(View.VISIBLE);
                tvReplace.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initView() {
        imgHead = (HeadImageView) findViewById(R.id.img_assistantHead);
        tvName = (TextView) findViewById(R.id.tv_assistantName);
        tvReplace = (TextView) findViewById(R.id.tv_assistantReplace);
        tvDelete = (TextView) findViewById(R.id.tv_assistantDelete);
        tvAdd = (TextView) findViewById(R.id.tv_assistantAdd);
        tvTitle = (TextView) findViewById(R.id.tv_assistant_title);
        tvUpdateTime = (TextView) findViewById(R.id.tv_assistant_updateTime);
        tvDeveloper = (TextView) findViewById(R.id.tv_assistant_developer);
        tvStatement = (TextView) findViewById(R.id.tv_assistant_statement);
        imgHead.setIsRect(true);
        tvReplace.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        tvAdd.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_assistantReplace:
                //更换
                TeamAssistantActivity.start(context,teamId, true);
                break;
            case R.id.tv_assistantDelete:
                //删除
                operateTeamRobot(rid,3);
                break;
            case R.id.tv_assistantAdd:
                //添加
                if (null != newBean && null != bindBean && !newBean.getAccid().equals(bindBean.getAccid())){
                    //更换绑定群助手
                    EasyAlertDialogHelper.showCommonDialog(context, null, "添加该群助手将更换原有群助手，确定添加吗？", "添加", "取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                        @Override
                        public void doCancelAction() {

                        }

                        @Override
                        public void doOkAction() {
                            operateTeamRobot(rid,2);
                        }
                    }).show();
                }else {
                    //添加群助手
                    operateTeamRobot(rid,1);
                }
                break;
        }
    }

    /**
     * 对机器人的操作
     * operatorType   1(添加)  2（更换）  3（删除）
     * */
    private void operateTeamRobot(String rid, final int operatorType) {
        showProgress(context,false);
        UserApi.operateTeamRobot(rid, teamId, operatorType, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    if (operatorType == 1){
                        //添加
                        toast("群助手添加成功");
                        tvAdd.setVisibility(View.GONE);
                        tvDelete.setVisibility(View.VISIBLE);
                        tvReplace.setVisibility(View.VISIBLE);
                    } else if (operatorType == 2){
                        //更改
                        toast("群助手更改成功");
                        tvAdd.setVisibility(View.GONE);
                        tvDelete.setVisibility(View.VISIBLE);
                        tvReplace.setVisibility(View.VISIBLE);
                    }else if (operatorType == 3){
                        //删除
                        toast("群助手删除成功");
                        TeamAssistantActivity.start(context,teamId, true);
                        finish();
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
}

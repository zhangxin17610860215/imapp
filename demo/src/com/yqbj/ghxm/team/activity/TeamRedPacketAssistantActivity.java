package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.yqbj.ghxm.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.bean.TeamConfigBean;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.NumberUtil;
import com.yqbj.ghxm.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.netease.yqbj.uikit.api.StatisticsConstants.ISSETTLEMENT;
import static com.netease.yqbj.uikit.api.StatisticsConstants.RPRECEIVEDELAYTIME;

/**
 * 红包助手
 * */
public class TeamRedPacketAssistantActivity extends BaseAct implements View.OnClickListener {

    private Context context;
    private RelativeLayout rlRedPacketAssistant;
    private RelativeLayout rlSettingTime;
    private TextView tvIsOpen;
    private String teamId;
//    private TeamConfigBean teamConfigBean;
    private Team team;

    public static void start(Context context, String teamId) {
        Intent intent = new Intent();
        intent.setClass(context, TeamRedPacketAssistantActivity.class);
        intent.putExtra("teamId", teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_redpacketassistant_layout);
        context = this;
        setToolbar(R.drawable.jrmf_b_top_back,"红包助手");

        initView();
        initData();
    }

    private void initView() {
        rlRedPacketAssistant = (RelativeLayout) findViewById(R.id.rl_redpacketassistant);
        rlSettingTime = (RelativeLayout) findViewById(R.id.rl_SettingTime);
        tvIsOpen = (TextView) findViewById(R.id.tv_isOpen);
        rlRedPacketAssistant.setOnClickListener(this);
        rlSettingTime.setOnClickListener(this);
    }

    private void initData() {
        teamId = getIntent().getStringExtra("teamId");
//        if (null == teamConfigBean){
//            teamConfigBean = StatisticsConstants.TEAMCONFIGBEAN;
//        }
//
//        if (null == teamConfigBean){
//            tvIsOpen.setText("关闭");
//        }else {
//            if (teamConfigBean.getRollbackOwner() == 1){
//                //开启
//                try {
//                    String time = NumberUtil.div_Intercept(teamConfigBean.getExpsecond() + "", "60", 0);
//                    tvIsOpen.setText(time + "分钟");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else if (teamConfigBean.getRollbackOwner() == 0){
//                //关闭
//                tvIsOpen.setText("关闭");
//            }
//        }
        team = NimUIKit.getTeamProvider().getTeamById(teamId);
        String rPReceiveDelaytime = "";
        try {
            String extensionJsonStr = team.getExtension();
            JSONObject jsonObject = new JSONObject(extensionJsonStr);
            if (jsonObject.has(RPRECEIVEDELAYTIME)){
                rPReceiveDelaytime = NumberUtil.div_Intercept(jsonObject.getString(RPRECEIVEDELAYTIME), "60", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(rPReceiveDelaytime)){
            if (rPReceiveDelaytime.equals("0")){
                tvIsOpen.setText("关闭");
            }else {
                try {
                    tvIsOpen.setText(rPReceiveDelaytime + "分钟");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (StringUtil.isNotEmpty(teamId)){
            switch (v.getId()){
                case R.id.rl_redpacketassistant:
                    DelayedCollectionRPAct.start(context,teamId);
                    break;
                case R.id.rl_SettingTime:
                    Intent intent = new Intent(context,TeamTimeSettingActivity.class);
                    intent.putExtra("teamId",teamId);
                    startActivityForResult(intent,101);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 101){
            final String time = data.getStringExtra("time");

            if (StringUtil.isNotEmpty(time)){
                String string = "";
                if (time.equals("0")){
                    tvIsOpen.setText("关闭");
                }else {
                    try {
                        string = NumberUtil.div_Intercept(time, "60", 0);
                        tvIsOpen.setText(string + "分钟");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                String extensionJsonStr = team.getExtension();
                String settlement = "0";
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(extensionJsonStr);
                    if (jsonObject.has(ISSETTLEMENT)){
                        settlement =  jsonObject.getBoolean(ISSETTLEMENT) ? "1" : "0";
                    }
                } catch (Exception e) {
                    settlement = "0";
                }

                final String finalString = String.valueOf(NumberUtil.mul(string,"60"));
                UserApi.teamConfigSet(teamId, null,time,null,null,settlement,this, new requestCallback() {
                    @Override
                    public void onSuccess(int code, Object object) {
                        if (code == Constants.SUCCESS_CODE){
                            try {
                                String extensionJsonStr = team.getExtension();
                                JSONObject jsonObject = null;
                                if (StringUtil.isNotEmpty(extensionJsonStr)){
                                    jsonObject = new JSONObject(extensionJsonStr);
                                }else {
                                    jsonObject = new JSONObject();
                                }
                                jsonObject.put(RPRECEIVEDELAYTIME, finalString);
                                NIMClient.getService(TeamService.class).updateTeam(teamId,TeamFieldEnum.Extension,jsonObject.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            toast("设置成功");
                        }else {
                            toast((String) object);
                        }
                    }

                    @Override
                    public void onFailed(String errMessage) {
                        toast(errMessage);
                    }
                });
            }
        }
    }
}

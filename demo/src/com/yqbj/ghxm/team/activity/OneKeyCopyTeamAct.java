package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.team.TeamCreateHelper;

import java.util.ArrayList;
import java.util.List;


public class OneKeyCopyTeamAct extends BaseAct {

    private static final String EXTRA_ID = "EXTRA_ID";

    private String teamId;
    private Team team;

    private HeadImageView imgTeam;
    private Button btnCopy;
    private TextView txtCount;
    private TextView txtName;

    private List<String> memberAccounts;

    public static void start(Context context, String tid) {
//        ToastHelper.showToast(context,"启动advance");

        Log.e("tid",tid);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, OneKeyCopyTeamAct.class);
        context.startActivity(intent);
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.copy_team_layout);


        setToolbar(R.drawable.jrmf_b_top_back,"一键复制新群");

        parseIntentData();
        findViews();
        memberAccounts = new ArrayList<>();
        getTeamInfo();
        getTeamMember();







    }

    private void findViews(){
       imgTeam = (HeadImageView) findViewById(R.id.img_team);
       txtCount = (TextView) findViewById(R.id.team_count);
       txtName = (TextView) findViewById(R.id.team_name);
       btnCopy = (Button) findViewById(R.id.team_copy);
       btnCopy.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(team!=null){
                   copyNewGroup(team.getName(),"欢迎加入新群",memberAccounts);
               }
           }
       });

       imgTeam.loadTeamIconByTeam(teamId);

    }


    private void getTeamMember(){
        NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> members, int code) {
                if (success && members != null && !members.isEmpty()) {
                    updateTeamMember(members);
                }
            }
        });
    }

    /**
     * 更新群成员信息
     *
     * @param m
     */
    private void updateTeamMember(final List<TeamMember> m) {
        if (m != null && m.isEmpty()) {
            return;
        }
        addTeamMembers(m, true);
    }

    /**
     * 添加群成员到列表
     *
     * @param m     群成员列表
     * @param clear 是否清除
     */
    private void addTeamMembers(final List<TeamMember> m, boolean clear) {
        if (m == null || m.isEmpty()) {
            return;
        }


        if (clear) {
            this.memberAccounts.clear();
        }


        this.memberAccounts.clear();

        for (TeamMember tm : m) {

            this.memberAccounts.add(tm.getAccount());
        }


    }


    private void getTeamInfo(){

        // teamId为想要查询的群组ID
        NIMClient.getService(TeamService.class).searchTeam(teamId).setCallback(new RequestCallback<Team>() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onSuccess(Team team1) {
                team =team1;
//                Log.e("xxxxx",team.getIcon());

                if(!TextUtils.isEmpty(team.getIcon()) && !OneKeyCopyTeamAct.this.isDestroyed()){
                    // 查询成功，获得群组资料
                    Glide.with( OneKeyCopyTeamAct.this )
                            .load( team.getIcon() )
                            .thumbnail( 0.2f )
                            .into(imgTeam);
                }

                txtCount.setText("群人数"+team.getMemberCount()+"人");
                txtName.setText(team.getName());

            }

            @Override
            public void onFailed(int code) {
                // 失败
                ToastHelper.showToast(OneKeyCopyTeamAct.this,"获取群信息失败");
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastHelper.showToast(OneKeyCopyTeamAct.this,"获取群信息失败");
            }
        });

    }


    public void copyNewGroup(String teamName,String teamIntroduce,List<String> accounts){


        TeamCreateHelper.createAdvancedTeam(OneKeyCopyTeamAct.this,accounts);
//        // 群组类型
//        TeamTypeEnum type = TeamTypeEnum.Advanced;
//        // 创建时可以预设群组的一些相关属性，如果是普通群，仅群名有效。
//        // fields 中，key 为数据字段，value 对对应的值，该值类型必须和 field 中定义的 fieldType 一致
//        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
//        fields.put(TeamFieldEnum.Name, teamName);
//        fields.put(TeamFieldEnum.Introduce, teamIntroduce);
//        fields.put(TeamFieldEnum.VerifyType, VerifyTypeEnum.Free);
//        NIMClient.getService(TeamService.class).createTeam(fields,type,"",accounts).setCallback(new RequestCallback<CreateTeamResult>() {
//            @Override
//            public void onSuccess(CreateTeamResult createTeamResult) {
//                ToastHelper.showToast(OneKeyCopyTeamAct.this,"一键创建群组成功");
//                SessionHelper.startTeamSession(OneKeyCopyTeamAct.this,createTeamResult.getTeam().getId());
//            }
//
//            @Override
//            public void onFailed(int i) {
//                ToastHelper.showToast(OneKeyCopyTeamAct.this,"一键创建群组失败" +i);
//            }
//
//            @Override
//            public void onException(Throwable throwable) {
//                ToastHelper.showToast(OneKeyCopyTeamAct.this,"一键创建群组失败" + throwable.getMessage());
//            }
//        });
    }



}

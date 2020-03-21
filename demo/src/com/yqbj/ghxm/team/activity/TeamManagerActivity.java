package com.yqbj.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.yqbj.uikit.api.model.team.TeamMemberDataChangedObserver;
import com.netease.yqbj.uikit.api.wrapper.NimToolBarOptions;
import com.netease.yqbj.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.ToolBarOptions;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.common.ui.widget.SwitchButton;
import com.umeng.analytics.MobclickAgent;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.TeamAllocationPriceBean;
import com.yqbj.ghxm.bean.TeamRobotDetatlsBean;
import com.yqbj.ghxm.busevent.TeamMemberEvent;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.main.activity.TeamActiveInfoAct;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.NumberUtil;
import com.yqbj.ghxm.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.netease.yqbj.uikit.api.StatisticsConstants.ISSAFEMODE;
import static com.netease.yqbj.uikit.api.StatisticsConstants.ISSETTLEMENT;
import static com.netease.yqbj.uikit.api.StatisticsConstants.RPRECEIVEDELAYTIME;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_COPYNEWTEAM;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_PROTECTMEMBER;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_SETTEAMMANAGER;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_TEAMAUTH;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_TEAMBANNED;
import static com.netease.yqbj.uikit.api.StatisticsConstants.TEAM_MANAGER_TRANSFERTEAM;

public class TeamManagerActivity extends BaseAct {

    private static final String EXTRA_ID = "EXTRA_ID";
    private static final int REQUEST_CODE_CONTACT_MANAGER_SELECT = 100;
    private static final int REQUEST_CODE_CONTACT_OWNER_SELECT = 101;
    private static final int REQUEST_CODE_CONTACT_NOCOLLAR_SELECT = 102;

    private static final String SW_KEY_SAFE_MODE = "sw_safe_mode";
    private static final String SW_KEY_CREDIT = "sw_credit";
    private static final String SW_KEY_FORBIT_SPEAK = "sw_forbit_speak";
    private static final String SW_KEY_SETTLEMENT = "sw_settlement";

    private boolean isSelfAdmin = false;

    private boolean isSelfManager = false;

    private List<String> memberAccounts;
    private List<TeamMember> members;
    private List<String> managerList;
    private List<String> noCollarList;


    private String teamId;
    private String teamMemberLimit = Constants.DEBUG ? "200":"500";

    private Team team;
    private String creator;

    private View team_manager_layout;
    private View team_forbit_speak_layout;
    private View team_settlement_layout;
    private SwitchButton swiBtn_forbit;
    private SwitchButton swiBtn_settlement;

    private View team_active_layout;
    private View team_NoCollar_layout;
    private View team_UpgradeTeam_layout;
    private View team_exit_layout;
    private View team_teamRobot_layout;
    private View team_RedPacket_layout;
    private View team_copy_layout;
    private View team_transfer_layout;
    private View team_safe_mode_layout;
    private SwitchButton swiBtn_safeMode;
    private View team_credit_layout;
    private View viewLayout;
    private SwitchButton swiBtn_credit;
    private TextView tvTeamPeople;

    private String teamRobotName = "未配置";
    private TeamRobotDetatlsBean bean;
    private int responseCode;
    private TeamAllocationPriceBean priceBean;

    public static void start(Context context, String tid) {
        Log.e("tid",tid);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, TeamManagerActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.advance_team_manager);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleString = "群管理";
        setToolBar(R.id.toolbar, options);

        parseIntentData();
        findViews();

        memberAccounts = new ArrayList<>();
        members = new ArrayList<>();
        managerList = new ArrayList<>();
        noCollarList = new ArrayList<>();
        loadTeamInfo();
        requestMembers();
        registerObservers(true);

        UserApi.getTeamAllocationPrice(teamId, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    priceBean = (TeamAllocationPriceBean) object;
                    teamMemberLimit = priceBean.getMaxUsers() + "";
                    try {
                        if (priceBean.getEndTime() == 0){
                            tvTeamPeople.setHint("当前上限" + teamMemberLimit + "人");
                            return;
                        }
                        //当前时间戳
                        long currentTime = System.currentTimeMillis();
                        //时间差
                        long diffTime = priceBean.getEndTime() - currentTime;
                        //一天
                        long oneDay = 24 * 60 * 60 * 1000;
                        //剩余时间大于等于30天
                        if (diffTime >= 30 * oneDay){
                            tvTeamPeople.setHint("当前上限" + teamMemberLimit + "人");
                            return;
                        }
                        //剩余时间小于1天
                        if (diffTime < oneDay){
                            tvTeamPeople.setHint("当前上限" + teamMemberLimit + "人,即将到期");
                        }else {
                            String days = NumberUtil.div_Intercept(diffTime + "",oneDay + "",1);
                            if (days.contains(".")){
                                String[] split = days.split("\\.");
                                days = split[0];
                            }
                            tvTeamPeople.setHint("当前上限" + teamMemberLimit + "人,剩余" + days + "天");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);

    }

    private void findViews(){
        viewLayout = findViewById(R.id.view_layout);
        team_manager_layout = findViewById(R.id.team_manager_layout);
        ((TextView) team_manager_layout.findViewById(R.id.item_title)).setText(R.string.set_team_manager);
        team_manager_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(TeamManagerActivity.this,TEAM_MANAGER_SETTEAMMANAGER);
                ContactSelectActivity.Option option = TeamHelper.getContactTeamManagerSelectOption(creator,(ArrayList<String>) managerList);
                option.teamId = teamId;
                NimUIKit.startContactSelector(TeamManagerActivity.this, option, REQUEST_CODE_CONTACT_MANAGER_SELECT);
            }
        });

        team_forbit_speak_layout = findViewById(R.id.team_forbid_speak_layout);
        ((TextView) team_forbit_speak_layout.findViewById(R.id.item_title)).setText(R.string.team_forbit_speak);

        swiBtn_forbit = team_forbit_speak_layout.findViewById(R.id.setting_item_toggle);
        swiBtn_forbit.setTag(SW_KEY_FORBIT_SPEAK);
        swiBtn_forbit.setOnChangedListener(onChangedListener);



        team_active_layout = findViewById(R.id.team_active_layout);
        ((TextView) team_active_layout.findViewById(R.id.item_title)).setText("群成员活跃度");
        ((TextView) team_active_layout.findViewById(R.id.item_detail)).setHint("");
        team_active_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                TeamActiveInfoAct.start(TeamManagerActivity.this,teamId);

            }
        });

        team_NoCollar_layout = findViewById(R.id.team_NoCollar_layout);
        ((TextView) team_NoCollar_layout.findViewById(R.id.item_title)).setText("禁止领取红包");
        ((TextView) team_NoCollar_layout.findViewById(R.id.item_detail)).setHint("");
        team_NoCollar_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoCollarActivity.start(TeamManagerActivity.this,teamId);
            }
        });

        team_UpgradeTeam_layout = findViewById(R.id.team_UpgradeTeam_layout);
        team_UpgradeTeam_layout.setVisibility(View.GONE);
        ((TextView) team_UpgradeTeam_layout.findViewById(R.id.item_title)).setText("升级群人数");
        tvTeamPeople = team_UpgradeTeam_layout.findViewById(R.id.item_detail);
        tvTeamPeople.setHint("当前上限" + teamMemberLimit + "人");
        team_UpgradeTeam_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != priceBean){
                    UpgradeTeamPeopleAct.start(TeamManagerActivity.this,teamId,teamMemberLimit,priceBean);
                }

            }
        });


        team_exit_layout = findViewById(R.id.team_exit_layout);
        ((TextView) team_exit_layout.findViewById(R.id.item_title)).setText("退群成员列表");
        ((TextView) team_exit_layout.findViewById(R.id.item_detail)).setHint("");
        team_exit_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        team_teamRobot_layout = findViewById(R.id.team_teamRobot_layout);
        ((TextView) team_teamRobot_layout.findViewById(R.id.item_title)).setText("群助手");
        ((TextView) team_teamRobot_layout.findViewById(R.id.item_detail)).setHint(teamRobotName);
        team_teamRobot_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (responseCode == Constants.SUCCESS_CODE && null != bean){
                    TeamAssistantDetailsActivity.start(TeamManagerActivity.this,teamId,bean,null,"2");
                }else {
                    TeamAssistantActivity.start(TeamManagerActivity.this,teamId, isSelfAdmin);
                }
            }
        });

        team_RedPacket_layout = findViewById(R.id.team_redPacket_layout);
        ((TextView) team_RedPacket_layout.findViewById(R.id.item_title)).setText("红包助手");
        ((TextView) team_RedPacket_layout.findViewById(R.id.item_detail)).setHint("");
        team_RedPacket_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TeamRedPacketAssistantActivity.start(TeamManagerActivity.this,teamId);
            }
        });

        team_settlement_layout = findViewById(R.id.team_settlement_layout);
        ((TextView) team_settlement_layout.findViewById(R.id.item_title)).setText("战绩自动结算");

        swiBtn_settlement = team_settlement_layout.findViewById(R.id.setting_item_toggle);
        swiBtn_settlement.setTag(SW_KEY_SETTLEMENT);
        swiBtn_settlement.setOnChangedListener(onChangedListener);

        team_copy_layout = findViewById(R.id.team_copy_layout);
        ((TextView) team_copy_layout.findViewById(R.id.item_title)).setText("一键复制新群");
        ((TextView) team_copy_layout.findViewById(R.id.item_detail)).setHint("");
        team_copy_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(TeamManagerActivity.this,TEAM_MANAGER_COPYNEWTEAM);
                OneKeyCopyTeamAct.start(TeamManagerActivity.this,teamId);
            }
        });

        team_transfer_layout = findViewById(R.id.team_transfer_layout);
        ((TextView) team_transfer_layout.findViewById(R.id.item_title)).setText("群主管理权转让");
        ((TextView) team_transfer_layout.findViewById(R.id.item_detail)).setHint("");
        team_transfer_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                ContactSelectActivity.Option option = new ContactSelectActivity.Option();
                option.title = "选择群转移的对象";
                option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
                option.teamId = teamId;
                option.maxSelectNum = 1;
                ArrayList<String> includeAccouts = new ArrayList<>();
                includeAccouts.add(creator);

                option.itemDisableFilter = new ContactIdFilter(includeAccouts);

                NimUIKit.startContactSelector(TeamManagerActivity.this, option, REQUEST_CODE_CONTACT_OWNER_SELECT);

            }
        });

        team_safe_mode_layout = findViewById(R.id.team_safe_mode_layout);
        ((TextView) team_safe_mode_layout.findViewById(R.id.item_title)).setText("群成员保护模式");
        swiBtn_safeMode = team_safe_mode_layout.findViewById(R.id.setting_item_toggle);
        swiBtn_safeMode.setTag(SW_KEY_SAFE_MODE);
        swiBtn_safeMode.setOnChangedListener(onChangedListener);

        team_credit_layout = findViewById(R.id.team_credit_layout);
        ((TextView) team_credit_layout.findViewById(R.id.item_title)).setText("是否开启群认证");
        swiBtn_credit = team_credit_layout.findViewById(R.id.setting_item_toggle);
        swiBtn_credit.setTag(SW_KEY_CREDIT);
        swiBtn_credit.setOnChangedListener(onChangedListener);

    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            final String key = (String) v.getTag();
            if(key.equals(SW_KEY_FORBIT_SPEAK)){
                MobclickAgent.onEvent(TeamManagerActivity.this,TEAM_MANAGER_TEAMBANNED);
                NIMClient.getService(TeamService.class).muteAllTeamMember(teamId, checkState).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(checkState){
                            ToastHelper.showToast(TeamManagerActivity.this,"禁言成功");
                        }else{
                            ToastHelper.showToast(TeamManagerActivity.this,"解除禁言");
                        }
                    }

                    @Override
                    public void onFailed(int i) {

                    }

                    @Override
                    public void onException(Throwable throwable) {

                    }
                });


            }else if (key.equals(SW_KEY_SAFE_MODE)){
                boolean isSafeMode;
                MobclickAgent.onEvent(TeamManagerActivity.this,TEAM_MANAGER_PROTECTMEMBER);
                try {
                    isSafeMode = checkState;
//                    设置群远程字段
                    String extensionJsonStr = team.getExtension();
                    JSONObject jsonObject;
                    if (StringUtil.isNotEmpty(extensionJsonStr)){
                        jsonObject = new JSONObject(extensionJsonStr);
                    }else {
                        jsonObject = new JSONObject();
                    }
                    jsonObject.put(ISSAFEMODE,isSafeMode);
                    NIMClient.getService(TeamService.class).updateTeam(teamId,TeamFieldEnum.Extension,jsonObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }else if(key.equals(SW_KEY_CREDIT)){
                MobclickAgent.onEvent(TeamManagerActivity.this,TEAM_MANAGER_TEAMAUTH);
                NIMClient.getService(TeamService.class).updateTeam(teamId,TeamFieldEnum.VerifyType,checkState?VerifyTypeEnum.Apply:VerifyTypeEnum.Free);

            }else if (key.equals(SW_KEY_SETTLEMENT)){
                //战绩自动结算
                boolean isSettlement;
                try {
                    isSettlement = checkState;
                    double rPReceiveDelaytime = 0;
                    String extensionJsonStr = team.getExtension();
                    JSONObject jsonObject;
                    if (StringUtil.isEmpty(extensionJsonStr)){
                        jsonObject = new JSONObject();
                    }else {
                        jsonObject = new JSONObject(extensionJsonStr);
                    }
                    if (jsonObject.has(RPRECEIVEDELAYTIME)){
                        rPReceiveDelaytime = jsonObject.getDouble(RPRECEIVEDELAYTIME);
                    }

                    setTeamConfig(isSettlement, new Double(rPReceiveDelaytime).intValue()+"");
                } catch (Exception e) {
                    e.printStackTrace();
                    swiBtn_settlement.setCheck(!checkState);
                }
            }

        }
    };

    /**
     * 初始化群组基本信息
     */
    private void loadTeamInfo() {
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);

        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    }
                }
            });
        }
    }


    /**
     * 更新群信息
     *
     * @param t
     */
    private void updateTeamInfo(final Team t) {
        this.team = t;
        if (team == null) {
            ToastHelper.showToast(this, getString(R.string.team_not_exist));
            finish();
            return;
        } else {
            creator = team.getCreator();
            if (creator.equals(NimUIKit.getAccount())) {
                isSelfAdmin = true;
            }
            swiBtn_forbit.setCheck(team.isAllMute());
            swiBtn_credit.setCheck(team.getVerifyType() == VerifyTypeEnum.Apply);
//            updateExtensionInfo();
        }
    }

    private void updateExtensionInfo(){
        String extensionJsonStr = team.getExtension();
        boolean isSafeMode = false;
        boolean isSettlement = false;
        if(!TextUtils.isEmpty(extensionJsonStr)){
            try {
                JSONObject jsonObject = new JSONObject(extensionJsonStr);
                if (jsonObject.has(ISSAFEMODE)){
                    isSafeMode =  jsonObject.getBoolean(ISSAFEMODE);
                }
                if (jsonObject.has(ISSETTLEMENT)){
                    isSettlement =  jsonObject.getBoolean(ISSETTLEMENT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        swiBtn_safeMode.setCheck(isSafeMode);
        swiBtn_settlement.setCheck(isSettlement);
    }

    /**
     * 移除群成员成功后，删除列表中的群成员
     *
     * @param account 被删除成员帐号
     */
    private void removeMember(String account) {
        if (TextUtils.isEmpty(account)) {
            return;
        }
        memberAccounts.remove(account);
        for (TeamMember m : members) {
            if (m.getAccount().equals(account)) {
                members.remove(m);
                break;
            }
        }
    }

    TeamMemberDataChangedObserver teamMemberObserver = new TeamMemberDataChangedObserver() {
        @Override
        public void onUpdateTeamMember(List<TeamMember> m) {
            for (TeamMember mm : m) {
                for (TeamMember member : members) {
                    if (mm.getAccount().equals(member.getAccount())) {
                        members.set(members.indexOf(member), mm);
                        break;
                    }
                }
            }
            addTeamMembers(m, false);
        }

        @Override
        public void onRemoveTeamMember(List<TeamMember> members) {
            for (TeamMember member : members) {
                removeMember(member.getAccount());
            }
        }
    };

    TeamDataChangedObserver teamDataObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            for (Team team : teams) {
                if (team.getId().equals(teamId)) {
                    updateTeamInfo(team);
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team.getId().equals(teamId)) {
                TeamManagerActivity.this.team = team;
                finish();
            }
        }
    };

    /**
     * ************************** 群信息变更监听 **************************
     */
    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
//        registerUserInfoChangedObserver(register);
    }

    /**
     * *************************** 加载&变更数据源 ********************************
     */
    private void requestMembers() {
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

        isSelfManager = false;
        isSelfAdmin = false;

        if (clear) {
            this.members.clear();
            this.memberAccounts.clear();
        }

        // add
        if (this.members.isEmpty()) {
            this.members.addAll(m);
        } else {
            for (TeamMember tm : m) {
                if (!this.memberAccounts.contains(tm.getAccount())) {
                    this.members.add(tm);
                }
            }
        }

        // sort
        Collections.sort(this.members, TeamHelper.teamMemberComparator);

        // accounts, manager, creator
        this.memberAccounts.clear();
        this.managerList.clear();
        for (TeamMember tm : members) {
            if (tm == null) {
                continue;
            }
            if (tm.getType() == TeamMemberType.Manager) {
                managerList.add(tm.getAccount());
            }
            if (tm.getAccount().equals(NimUIKit.getAccount())) {
                if (tm.getType() == TeamMemberType.Manager) {
                    isSelfManager = true;
                } else if (tm.getType() == TeamMemberType.Owner) {
                    isSelfAdmin = true;
                    creator = NimUIKit.getAccount();
                }
            }
            this.memberAccounts.add(tm.getAccount());
        }

        if(!isSelfAdmin){
            team_transfer_layout.setVisibility(View.GONE);
            team_manager_layout.setVisibility(View.GONE);
            team_copy_layout.setVisibility(View.GONE);
            team_teamRobot_layout.setVisibility(View.GONE);
            viewLayout.setVisibility(View.GONE);
            team_RedPacket_layout.setVisibility(View.GONE);
            team_UpgradeTeam_layout.setVisibility(View.GONE);
        }
    }

    /**
     * 设置群配置
     * */
    private void setTeamConfig(final boolean settlement, String expsecond) {
        UserApi.teamConfigSet(teamId, expsecond,settlement?"1":"0",this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
//                  设置群远程字段
                    try {
                        String extensionJsonStr = team.getExtension();
                        JSONObject jsonObject;
                        if (StringUtil.isNotEmpty(extensionJsonStr)){
                            jsonObject = new JSONObject(extensionJsonStr);
                        }else {
                            jsonObject = new JSONObject();
                        }
                        jsonObject.put(ISSETTLEMENT, settlement);
                        NIMClient.getService(TeamService.class).updateTeam(teamId,TeamFieldEnum.Extension,jsonObject.toString());
                    }catch (Exception e){

                    }
                    toast("设置成功");
                }else {
                    toast((String) object);
                    swiBtn_settlement.setCheck(!settlement);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                toast(errMessage);
                swiBtn_settlement.setCheck(!settlement);
            }
        });
    }


    /**
     * 添加群管理员
     *
     * @param accounts 邀请帐号
     */
    private void addManager(ArrayList<String> accounts) {
        // teamId 操作的群id， accountList为待提升为管理员的用户帐号列表
        NIMClient.getService(TeamService.class).addManagers(teamId, accounts).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> managers) {
                // 添加群管理员成功
                ToastHelper.showToast(TeamManagerActivity.this, "添加管理员成功");
                EventBus.getDefault().post(new TeamMemberEvent());
            }

            @Override
            public void onFailed(int code) {
                // 添加群管理员失败
                ToastHelper.showToast(TeamManagerActivity.this, "失败");
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
            }
        });

    }

    private void removeManager(List<String> accounts){

        if(accounts.size()<1){
            return;
        }

        // teamid为群id， accountList为待撤销的管理员的账号列表
        NIMClient.getService(TeamService.class).removeManagers(teamId, accounts).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> teamMembers) {
                EventBus.getDefault().post(new TeamMemberEvent());
            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_CONTACT_MANAGER_SELECT:
                final ArrayList<String> selectedManager = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                final ArrayList<String> removeManager = new ArrayList<>();
                if (selectedManager != null && !selectedManager.isEmpty()) {
                    for (String managerAccount:managerList) {
                        if(!selectedManager.contains(managerAccount)){
                            removeManager.add(managerAccount);
                        }else{
                            selectedManager.remove(managerAccount);
                        }
                    }
                    addManager(selectedManager);
                    removeManager(removeManager);
                }else{
                    removeManager(managerList);
                }
                break;
            case REQUEST_CODE_CONTACT_OWNER_SELECT:
                final ArrayList<String> selectedOwner = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selectedOwner != null && !selectedOwner.isEmpty()) {
                    final String account = selectedOwner.get(0);
                    String tips = String.format("确定要转让群主管理权给 %s 吗？", TeamHelper.getTeamMemberDisplayName(teamId, account));
                    EasyAlertDialogHelper.showCommonDialog(TeamManagerActivity.this, null, tips,"确定","取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                        @Override
                        public void doCancelAction() {

                        }

                        @Override
                        public void doOkAction() {
                            // false表示群主转让后不退群
                            NIMClient.getService(TeamService.class).transferTeam(teamId, account, false)
                                    .setCallback(new RequestCallback<List<TeamMember>>() {
                                        @Override
                                        public void onSuccess(List<TeamMember> members) {
                                            // 群转移成功
                                            MobclickAgent.onEvent(TeamManagerActivity.this,TEAM_MANAGER_TRANSFERTEAM);
                                            ToastHelper.showToast(TeamManagerActivity.this,"群主转让成功");
                                            EventBus.getDefault().post(new TeamMemberEvent());
                                            finish();
                                        }

                                        @Override
                                        public void onFailed(int code) {
                                            // 群转移失败
                                            ToastHelper.showToast(TeamManagerActivity.this,"群主转让失败");
                                        }

                                        @Override
                                        public void onException(Throwable exception) {
                                            // 错误
                                            ToastHelper.showToast(TeamManagerActivity.this,"群主转让失败");
                                        }
                                    });
                        }
                    }).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isSelfAdmin){
            queryRobot();
        }

        updateExtensionInfo();
    }

    private void queryRobot() {
        showProgress(this, false);
        UserApi.getTeamRobotDetatls(teamId, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                responseCode = code;
                if (code == Constants.SUCCESS_CODE){
                    bean = (TeamRobotDetatlsBean) object;
                    teamRobotName = bean.getNickname();
                    ((TextView) team_teamRobot_layout.findViewById(R.id.item_detail)).setHint(teamRobotName);
                }else {
                    teamRobotName = "未配置";
                    ((TextView) team_teamRobot_layout.findViewById(R.id.item_detail)).setHint(teamRobotName);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                teamRobotName = "未配置";
                ((TextView) team_teamRobot_layout.findViewById(R.id.item_detail)).setHint(teamRobotName);
            }
        });
    }

}

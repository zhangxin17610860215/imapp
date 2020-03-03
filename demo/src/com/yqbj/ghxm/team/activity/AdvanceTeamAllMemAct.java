package com.yqbj.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.yqbj.ghxm.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.yqbj.uikit.api.model.team.TeamMemberDataChangedObserver;
import com.netease.yqbj.uikit.api.model.user.UserInfoObserver;
import com.netease.yqbj.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.yqbj.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.yqbj.uikit.business.team.adapter.TeamMemberAdapter;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.business.team.ui.TeamInfoGridView;
import com.netease.yqbj.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.netease.yqbj.uikit.common.CommonUtil;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.adapter.TAdapterDelegate;
import com.netease.yqbj.uikit.common.adapter.TViewHolder;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdvanceTeamAllMemAct extends BaseAct implements TAdapterDelegate,TeamMemberHolder.TeamMemberHolderEventListener,TeamMemberAdapter.AddMemberCallback,TeamMemberAdapter.RemoveMemberCallback {
    private static final String EXTRA_ID = "EXTRA_ID";
    private static final int REQUEST_CODE_CONTACT_SELECT = 103;
    // constant
    private static final String TAG = "RegularTeamInfoActivity";

    private static final int REQUEST_CODE_CONTACT_MANAGER_SELECT = 104;
    private static final int REQUEST_CODE_CONTACT_SELECT_REMOVE  = 105;

    public static final String RESULT_EXTRA_REASON = "RESULT_EXTRA_REASON";
    public static final String RESULT_EXTRA_REASON_QUIT = "RESULT_EXTRA_REASON_QUIT";
    public static final String RESULT_EXTRA_REASON_DISMISS = "RESULT_EXTRA_REASON_DISMISS";
    protected String teamId;
    public TeamInfoGridView gridView;
    private EditText etSearch;
    private TextView tvSearch;
    protected List<String> memberAccounts;
    protected List<TeamMember> members;
    protected List<TeamMemberAdapter.TeamMemberItem> dataSource;
    protected List<TeamMemberAdapter.TeamMemberItem> searchDataSource;
    protected List<String> managerList;
    protected Team team;
    protected TeamMemberAdapter adapter;

    protected UserInfoObserver userInfoObserver;

    // state
    protected boolean isSelfAdmin = false;
    protected boolean isSelfManager = false;

    protected String creator;


    public static void start(Context context, String tid) {
        Log.e("tid",tid);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, AdvanceTeamAllMemAct.class);
        context.startActivity(intent);
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setToolbar(R.drawable.jrmf_b_top_back,"群组信息");
//
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advance_team_member_act);
        setToolbar(R.drawable.jrmf_b_top_back,"群组信息");

        parseIntentData();
        findViews();
        initAdapter();
        loadTeamInfo();
        requestMembers();
        registerObservers(true);
    }

    protected void findViews() {
        gridView = (TeamInfoGridView) findViewById(R.id.team_gird_view);
        etSearch = (EditText) findViewById(R.id.et_Search);
        tvSearch = (TextView) findViewById(R.id.tv_Search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyWord = etSearch.getText().toString().trim();
                if (keyWord.length() <= 0){
                    updateTeamMemberDataSource();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTeamMembers();
            }
        });
    }

    private void searchTeamMembers() {
        String keyWord = etSearch.getText().toString().trim();
        if (StringUtil.isEmpty(teamId)){
            toast("获取群信息失败,请检查网络情况后再次搜索");
            return;
        }

        if (StringUtil.isEmpty(keyWord)){
            toast("请输入需要搜索的关键字");
            return;
        }

        searchDataSource = new ArrayList<>();
        List<TeamMember> teamMemberList = NimUIKit.getTeamProvider().getTeamMemberList(teamId);
        for (TeamMember member : teamMemberList){
            if (StringUtil.isNotEmpty(member.getTeamNick())){
                if (member.getTeamNick().contains(keyWord)){
                    searchDataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                            .NORMAL, teamId, member.getAccount(), getIdentity(member.getAccount())));
                }
            }else if (StringUtil.isNotEmpty(UserInfoHelper.getUserName(member.getAccount()))){
                if (UserInfoHelper.getUserName(member.getAccount()).contains(keyWord)){
                    searchDataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                            .NORMAL, teamId, member.getAccount(), getIdentity(member.getAccount())));
                }
            }

        }
        dataSource.clear();
        dataSource.addAll(searchDataSource);
        if (searchDataSource.size() > 0){
            gridView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }else {
            toast("暂未查到可匹配的群成员");
        }

    }

    protected void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    protected void initAdapter() {
        memberAccounts = new ArrayList<>();
        members = new ArrayList<>();
        dataSource = new ArrayList<>();
        managerList = new ArrayList<>();
        adapter = new TeamMemberAdapter(teamId, this, dataSource, this, this, this);
        adapter.setEventListener(this);

        gridView.setSelector(R.color.transparent);


        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        gridView.setAdapter(adapter);
    }

    /**
     * 初始化群组基本信息
     */
    protected void loadTeamInfo() {
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    } else {
                        onGetTeamInfoFailed();
                    }
                }
            });
        }
    }

    /**
     * *************************** 加载&变更数据源 ********************************
     */
    protected void requestMembers() {
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
     * ************************** 群信息变更监听 **************************
     */
    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
//        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
        registerUserInfoChangedObserver(register);
    }




    private void registerUserInfoChangedObserver(boolean register) {
        if (register) {
            if (userInfoObserver == null) {
                userInfoObserver = new UserInfoObserver() {
                    @Override
                    public void onUserInfoChanged(List<String> accounts) {
                        adapter.notifyDataSetChanged();
                    }
                };
            }
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true);
        } else {
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false);
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
                    updateTeamMemberDataSource();
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team.getId().equals(teamId)) {
                AdvanceTeamAllMemAct.this.team = team;
                finish();
            }
        }
    };


    /**
     * 批量踢人入口
     * @param removeList
     */
    private void kickTeamOnce(final List<String> removeList){

        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);


        UserApi.kickTeamByOnce(teamId, JSON.toJSONString(removeList), this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                DialogMaker.dismissProgressDialog();
                CommonUtil.uploadTeamIcon(teamId,AdvanceTeamAllMemAct.this);
                requestMembers();
                ToastHelper.showToast(AdvanceTeamAllMemAct.this, R.string.remove_member_success);



            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();

            }
        });





    }

    /**
     * 邀请群成员
     *
     * @param accounts 邀请帐号
     */
    private void inviteMembers(ArrayList<String> accounts) {
        NIMClient.getService(TeamService.class).addMembersEx(teamId, accounts, "邀请附言", "邀请扩展字段").setCallback(new RequestCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> failedAccounts) {
                if (failedAccounts == null || failedAccounts.isEmpty()) {
                    ToastHelper.showToast(AdvanceTeamAllMemAct.this, "添加群成员成功");
                } else {
                    TeamHelper.onMemberTeamNumOverrun(failedAccounts, AdvanceTeamAllMemAct.this);
                }
                requestMembers();

            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_TEAM_INVITE_SUCCESS) {
                    ToastHelper.showToast(AdvanceTeamAllMemAct.this, R.string.team_invite_members_success);
                } else {
                    ToastHelper.showToast(AdvanceTeamAllMemAct.this, "invite members failed, code=" + code);
                    Log.e(TAG, "invite members failed, code=" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
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


        for (TeamMemberAdapter.TeamMemberItem item : dataSource) {
            if (item.getAccount() != null && item.getAccount().equals(account)) {
                dataSource.remove(item);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    protected void onGetTeamInfoFailed() {
        ToastHelper.showToast(this, getString(R.string.team_not_exist));
        finish();
    }



    protected void updateTeamInfo(Team t) {
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

            setTitle(team.getName());

        }
    }

    protected void updateTeamMember(List<TeamMember> m) {
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
    protected void addTeamMembers(final List<TeamMember> m, boolean clear) {
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

        updateTeamMemberDataSource();
    }

    /**
     * 更新成员信息
     */
    protected void updateTeamMemberDataSource() {

        if (members.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
        } else {
            gridView.setVisibility(View.GONE);
            return;
        }

        dataSource.clear();

        String identity = null;

        for (String account : memberAccounts) {


            identity = getIdentity(account);
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                        .NORMAL, teamId, account, identity));

        }


        // add item
        if (isSelfAdmin || isSelfManager){
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null, null));
        }else {
            Team team = NimUIKit.getTeamProvider().getTeamById(teamId);
            if (team.getVerifyType() != VerifyTypeEnum.Apply){
                dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null, null));
            }
        }

        // remove item
        if (isSelfManager||isSelfAdmin) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.DELETE, null, null,
                    null));
        }


        // refresh
        adapter.notifyDataSetChanged();

    }

    protected String getIdentity(String account) {
        String identity;
        if (creator.equals(account)) {
            identity = TeamMemberHolder.OWNER;
        } else if (managerList.contains(account)) {
            identity = TeamMemberHolder.ADMIN;
        } else {
            identity = null;
        }
        return identity;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return TeamMemberHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public void onHeadImageViewClick(String account) {
        AdvancedTeamMemberInfoAct.startActivityForResult(AdvanceTeamAllMemAct.this, account, teamId);
    }

    @Override
    public void onAddMember() {
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        NimUIKit.startContactSelector(this, option, REQUEST_CODE_CONTACT_SELECT);
    }

    @Override
    public void onRemoveMember() {
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = "选择你要移除的成员";
        option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
        option.allowSelectEmpty = true;
        option.teamId = teamId;
        ArrayList<String> disableAccounts = new ArrayList<>();
        if(isSelfAdmin){

            disableAccounts.add(NimUIKit.getAccount());
        }else if(isSelfManager){
            disableAccounts.add(creator);
            disableAccounts.addAll(managerList);
        }

        option.itemDisableFilter = new ContactIdFilter(disableAccounts);

        NimUIKit.startContactSelector(AdvanceTeamAllMemAct.this, option, REQUEST_CODE_CONTACT_SELECT_REMOVE);


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CONTACT_SELECT:
                final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
                    inviteMembers(selected);
                }
                break;
            case REQUEST_CODE_CONTACT_SELECT_REMOVE:
                final ArrayList<String> selectedRemove = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selectedRemove != null && !selectedRemove.isEmpty()) {
                    // teamId表示群ID，account表示被踢出的成员帐号
                    kickTeamOnce(selectedRemove);
                }
                break;
            default:
                break;
        }
    }
}

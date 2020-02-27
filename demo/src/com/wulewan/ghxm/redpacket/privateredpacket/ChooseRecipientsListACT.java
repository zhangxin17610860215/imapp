package com.wulewan.ghxm.redpacket.privateredpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gjiazhe.wavesidebar.WaveSideBar;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.utils.LetterComparator;
import com.wulewan.ghxm.utils.SPUtils;
import com.wulewan.ghxm.utils.StringUtil;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.api.model.SimpleCallback;
import com.netease.wulewan.uikit.common.activity.UI;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 选择谁可以领取红包页面
 * */
public class ChooseRecipientsListACT extends UI implements View.OnClickListener {

    private Context context;
    private ImageView imgBack;
    private RecyclerView mRecyclerView;
    private TextView tvDetermine;
    private WaveSideBar mWaveSideBar;
    private EasyRVAdapter mAssetsAdapter;
    private List<TeamMember> list = new ArrayList<>();
    private String mTeamId = "";
    private StringBuilder nameData = new StringBuilder();
    private StringBuilder accountData = new StringBuilder();
    private String replaceName = "";
    private String replaceAccount = "";

    public static void start(Context context) {
        Intent intent = new Intent(context, ChooseRecipientsListACT.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooserecipients);
        context = ChooseRecipientsListACT.this;
        mTeamId = getIntent().getStringExtra("teamId");
        initView();
        initData();
    }

    private void initData() {
        NimUIKit.getTeamProvider().fetchTeamMemberList(mTeamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> result, int code) {
                if (null == result && result.size() <= 0){
                    return;
                }
                for (int i = 0; i < result.size(); i++){
                    TeamMember teamMember = result.get(i);
                    if (StringUtil.isNotEmpty(teamMember.getAccount())&&
                            teamMember.getAccount().equals(SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID))){
                        result.remove(i);
                    }
                }
                list = result;
                loadData();
            }
        });


    }

    private void loadData() {
        mWaveSideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String index) {
                TeamMember teamMember;
                for (int i = 0; i < list.size(); i++) {
                    teamMember = list.get(i);
                    UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(teamMember.getAccount());
                    String str = StringUtil.isNotEmpty(list.get(i).getTeamNick()) ? list.get(i).getTeamNick() : userInfo.getName();
                    if (StringUtil.getFirstSpell(str.substring(0, 1)).toUpperCase().equals(index)) {
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        });
        //对首字母排序
        Collections.sort(list, new LetterComparator());

        mAssetsAdapter = new EasyRVAdapter(this, list, R.layout.item_chooserecipients_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                if (null == list || list.size() == 0) {
                    return;
                }
                TeamMember teamMember = list.get(position);
                if (null == teamMember) {
                    return;
                }
                TextView tv_index = viewHolder.getView(R.id.tv_teamchoose_index);
                ImageView imgHead = viewHolder.getView(R.id.img_teamchoose_head);
                TextView tvName = viewHolder.getView(R.id.tv_teamchoose_name);

                //先获取用户信息
                final UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(teamMember.getAccount());

                Glide.with(context).load(userInfo.getAvatar()).into(imgHead);
                final String name = StringUtil.isNotEmpty(teamMember.getTeamNick()) ? teamMember.getTeamNick() : userInfo.getName();
                String EName = name.substring(0, 1).toUpperCase();
                //获取上一个成员名字
                String  previousFirstLetter = "";
                if (position != 0){
                    UserInfo previousInfo = NimUIKit.getUserInfoProvider().getUserInfo(list.get(position - 1).getAccount());
                    String previousStr = StringUtil.isNotEmpty(teamMember.getTeamNick()) ? teamMember.getTeamNick() : previousInfo.getName();
                    previousFirstLetter = previousStr.substring(0, 1).toUpperCase();
                }
                if (position == 0 || !StringUtil.getFirstSpell(EName).equals(StringUtil.getFirstSpell(previousFirstLetter))) {
                    tv_index.setVisibility(View.VISIBLE);
                    tv_index.setText(StringUtil.getFirstSpell(EName).toUpperCase());
                } else {
                    tv_index.setVisibility(View.GONE);
                }
                tvName.setText(StringUtil.isEmpty(teamMember.getTeamNick()) ? userInfo.getName() : teamMember.getTeamNick());
            }
        };
        mRecyclerView.setAdapter(mAssetsAdapter);
        mAssetsAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                //Item点击事件
                TeamMember teamMember = list.get(position);
                final UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(teamMember.getAccount());
                final String name = StringUtil.isNotEmpty(teamMember.getTeamNick()) ? teamMember.getTeamNick() : userInfo.getName();
                final String account = teamMember.getAccount();
                final CheckBox isSelection = view.findViewById(R.id.cb_teamchoose_isSelection);
                if (isSelection.isChecked()){
                    isSelection.setChecked(false);
                    if (replaceName.contains(name) || replaceAccount.contains(account)){
                        replaceName = replaceName.replace(name + ",", "");
                        replaceAccount = replaceAccount.replace(account + ",", "");
                        nameData.delete(0,nameData.toString().length());
                        accountData.delete(0,accountData.toString().length());
                        nameData.append(replaceName);
                        accountData.append(replaceAccount);
                    }else {
                        if (StringUtil.count(replaceAccount,",") >= 5){
                            toast("最多可以指定5人");
                            return;
                        }
                    }
                }else {
                    if (StringUtil.count(replaceAccount,",") >= 5){
                        toast("最多可以指定5人");
                        return;
                    }
                    isSelection.setChecked(true);
                    if (!replaceName.contains(name) || !replaceAccount.contains(account)){
                        nameData.append(name).append(",");
                        accountData.append(account).append(",");
                        replaceName = nameData.toString();
                        replaceAccount = accountData.toString();
                    }
                }
            }
        });
    }

    private void initView() {
        imgBack = findView(R.id.img_chooserecipients_back);
        mRecyclerView = findView(R.id.mRecyclerView);
        mWaveSideBar = findView(R.id.mWaveSideBar);
        tvDetermine = findView(R.id.tv_chooserecipients_Determine);
        mWaveSideBar.setIndexItems("#","A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        imgBack.setOnClickListener(this);
        tvDetermine.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_chooserecipients_back:
                finish();
                break;
            case R.id.tv_chooserecipients_Determine:
                Intent intent = new Intent();
                intent.putExtra("replaceName", replaceName);
                intent.putExtra("replaceAccount", replaceAccount);
                this.setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

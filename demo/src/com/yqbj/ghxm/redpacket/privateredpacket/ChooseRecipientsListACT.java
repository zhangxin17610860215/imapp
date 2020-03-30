package com.yqbj.ghxm.redpacket.privateredpacket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gjiazhe.wavesidebar.WaveSideBar;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.StatisticsConstants;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.ui.imageview.HeadImageView;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.GetAllMemberWalletBean;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.team.activity.SetMiBiActivity;
import com.yqbj.ghxm.utils.LetterComparator;
import com.yqbj.ghxm.utils.StringUtil;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家蜜币设置页面
 * */
public class ChooseRecipientsListACT extends UI implements View.OnClickListener {

    private Context context;
    private ImageView imgBack;
    private RecyclerView mRecyclerView;
    private WaveSideBar mWaveSideBar;
    private EasyRVAdapter mAssetsAdapter;
    private List<GetAllMemberWalletBean.ResultsBean> list = new ArrayList<>();
    private String mTeamId = "";

    public static void start(Context context,String teamId) {
        Intent intent = new Intent(context, ChooseRecipientsListACT.class);
        intent.putExtra("teamId",teamId);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooserecipients);
        context = ChooseRecipientsListACT.this;
        mTeamId = getIntent().getStringExtra("teamId");
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        showProgress(context,false);
        UserApi.getAllMemberWallet(mTeamId, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    GetAllMemberWalletBean walletBean = (GetAllMemberWalletBean) object;
                    List<GetAllMemberWalletBean.ResultsBean> results = walletBean.getResults();
                    Team team = NimUIKit.getTeamProvider().getTeamById(mTeamId);
                    list.clear();
                    if (team.getCreator().equals(NimUIKit.getAccount())){
                        list.addAll(results);
                    }else {
                        List<TeamMember> teamMemberList = NimUIKit.getTeamProvider().getTeamMemberList(mTeamId);
                        for (TeamMember teamMember : teamMemberList){
                            Map<String, Object> extension = teamMember.getExtension();
                            if (null == extension){
                                extension = new HashMap<>();
                            }
                            String inviter = team.getCreator();
                            String teamMemberEx = (String) extension.get("ext");
                            if (!StringUtil.isEmpty(teamMemberEx) && !teamMemberEx.equals("null")){
                                try {
                                    JSONObject jsonObject = new JSONObject(teamMemberEx);
                                    inviter = (String) jsonObject.get(StatisticsConstants.INVITER);
                                    inviter = TextUtils.isEmpty(inviter) ? team.getCreator() : inviter;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (inviter.equals(NimUIKit.getAccount())){
                                for (GetAllMemberWalletBean.ResultsBean resultsBean : results){
                                    if (resultsBean.getUid().equals(teamMember.getAccount())){
                                        list.add(resultsBean);
                                    }
                                }
                            }
                        }
                    }
                    loadData();
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    private void loadData() {
        mWaveSideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String index) {
                GetAllMemberWalletBean.ResultsBean bean;
                for (int i = 0; i < list.size(); i++) {
                    bean = list.get(i);
                    UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(bean.getUid());
                    String str = userInfo.getName();
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
                GetAllMemberWalletBean.ResultsBean bean = list.get(position);
                if (null == bean) {
                    return;
                }
                TextView tv_index = viewHolder.getView(R.id.tv_teamchoose_index);
                HeadImageView imgHead = viewHolder.getView(R.id.img_teamchoose_head);
                TextView tvName = viewHolder.getView(R.id.tv_teamchoose_name);
                TextView tvBalance = viewHolder.getView(R.id.tv_balance);
                tvBalance.setText("余额：" + bean.getScore());
                //先获取用户信息
                final UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(bean.getUid());

                Glide.with(context).load(userInfo.getAvatar()).into(imgHead);
                final String name = userInfo.getName();
                String EName = name.substring(0, 1).toUpperCase();
                //获取上一个成员名字
                String  previousFirstLetter = "";
                if (position != 0){
                    UserInfo previousInfo = NimUIKit.getUserInfoProvider().getUserInfo(list.get(position - 1).getUid());
                    String previousStr = previousInfo.getName();
                    previousFirstLetter = previousStr.substring(0, 1).toUpperCase();
                }
                if (position == 0 || !StringUtil.getFirstSpell(EName).equals(StringUtil.getFirstSpell(previousFirstLetter))) {
                    tv_index.setVisibility(View.VISIBLE);
                    tv_index.setText(StringUtil.isEmpty(StringUtil.getFirstSpell(EName).toUpperCase()) ? "#" : StringUtil.getFirstSpell(EName).toUpperCase());
                } else {
                    tv_index.setVisibility(View.GONE);
                }
                tvName.setText(userInfo.getName());
            }
        };
        mRecyclerView.setAdapter(mAssetsAdapter);
        mAssetsAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                //Item点击事件
                if (null == list || list.size() <= 0){
                    return;
                }
                Team team = NimUIKit.getTeamProvider().getTeamById(mTeamId);
                GetAllMemberWalletBean.ResultsBean bean = list.get(position);
                if (null != team && !team.getCreator().equals(NimUIKit.getAccount())
                        && bean.getUid().equals(NimUIKit.getAccount())){
                    toast("管理员不能给自己充值");
                }else {
                    SetMiBiActivity.start(context,bean);
                }
            }
        });
    }

    private void initView() {
        imgBack = findView(R.id.img_chooserecipients_back);
        mRecyclerView = findView(R.id.mRecyclerView);
        mWaveSideBar = findView(R.id.mWaveSideBar);
        mWaveSideBar.setIndexItems("#","A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        imgBack.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_chooserecipients_back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

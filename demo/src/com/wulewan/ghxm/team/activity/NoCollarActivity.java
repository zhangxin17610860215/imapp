package com.wulewan.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wulewan.ghxm.R;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.wulewan.uikit.business.team.helper.TeamHelper;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.common.ui.imageview.HeadImageView;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;

/**
 * 禁止领取零钱红包
 * */
public class NoCollarActivity extends BaseAct implements View.OnClickListener {

    private ImageView img_back;
    private TextView tv_rightText;
    private LinearLayout llNodata;
    private TextView tvNodata;
    private RecyclerView rv_nocollar;
    private TextView tv_settingTeamMembers;

    private Context context;
    private String teamId = "";
    private EasyRVAdapter mAdapter;
    private int tag = 1;            //tag 等于1 编辑      等于2 取消
    private ArrayList<String> noCollarList = new ArrayList<>();

    public static void start(Context context, String teamId) {
        Intent intent = new Intent();
        intent.setClass(context, NoCollarActivity.class);
        intent.putExtra("teamId", teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nocollar_activity_layout);
        context = this;
        teamId = getIntent().getStringExtra("teamId");

        initView();
        initData();
    }

    private void initView() {
        tv_rightText = (TextView) findViewById(R.id.tv_rightText);
        img_back = (ImageView) findViewById(R.id.img_back);
        llNodata = findView(R.id.ll_nodata);
        tvNodata = findView(R.id.tv_noData_content);
        tvNodata.setText("暂无被禁止领取红包的群成员");
        rv_nocollar = (RecyclerView) findViewById(R.id.rv_nocollar);
        tv_settingTeamMembers = (TextView) findViewById(R.id.tv_settingTeamMembers);
        rv_nocollar.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        img_back.setOnClickListener(this);
        tv_rightText.setOnClickListener(this);
        tv_settingTeamMembers.setOnClickListener(this);
    }

    private void initData() {
        showProgress(context,false);
        UserApi.getNoCollarList(teamId, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    ArrayList<String> list = (ArrayList<String>) object;
                    noCollarList = list;
                    loadData();
                }else {
                    toast((String) object);
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
        if (noCollarList.size() <= 0){
            llNodata.setVisibility(View.VISIBLE);
            rv_nocollar.setVisibility(View.GONE);
            tv_rightText.setVisibility(View.GONE);
            tv_settingTeamMembers.setVisibility(View.VISIBLE);
        }else {
            llNodata.setVisibility(View.GONE);
            rv_nocollar.setVisibility(View.VISIBLE);
            tv_rightText.setVisibility(View.VISIBLE);
        }
        mAdapter = new EasyRVAdapter(context,noCollarList,R.layout.item_nocollar_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, final int position, Object item) {
                HeadImageView imgHead = viewHolder.getView(R.id.img_head);
                final TextView tvName = viewHolder.getView(R.id.tv_name);
                TextView tvRemove = viewHolder.getView(R.id.tv_remove);
                TextView tvIsMe = viewHolder.getView(R.id.tv_isMe);
                imgHead.setIsRect(true);
                UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(noCollarList.get(position));
                imgHead.loadAvatar(userInfo.getAvatar());
                tvName.setText(userInfo.getName());

                if (noCollarList.get(position).equals(NimUIKit.getAccount())){
                    tvIsMe.setVisibility(View.VISIBLE);
                    tvRemove.setVisibility(View.GONE);
                }else {
                    tvIsMe.setVisibility(View.GONE);
                    if (tag == 2){
                        tvRemove.setVisibility(View.VISIBLE);
                    }else if (tag == 1){
                        tvRemove.setVisibility(View.GONE);
                    }
                }
                tvRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EasyAlertDialogHelper.showCommonDialog(context, null, String.format("确定移除“%s”？",tvName.getText().toString()), "确定", "取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                            @Override
                            public void doCancelAction() {

                            }

                            @Override
                            public void doOkAction() {
                                //移除被限制的成员
                                ArrayList<String> selectedList = new ArrayList<>();
                                selectedList.add(noCollarList.get(position));
                                String uids = JSON.toJSONString(selectedList);
                                settingNoCollarId(uids,"0");
                            }
                        }).show();
                    }
                });
            }
        };
        rv_nocollar.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_rightText:
                if (tag == 1){
                    tv_rightText.setText("取消");
                    tv_settingTeamMembers.setVisibility(View.GONE);
                    tag = 2;
                }else if (tag == 2){
                    tv_rightText.setText("编辑");
                    tv_settingTeamMembers.setVisibility(View.VISIBLE);
                    tag = 1;
                }
                initData();
                break;
            case R.id.tv_settingTeamMembers:
                ContactSelectActivity.Option option = TeamHelper.getContactNoCollarSelectOption(teamId, noCollarList);
                option.teamId = teamId;

                NimUIKit.startContactSelector(context, option, 100);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || requestCode != 100) {
            return;
        }
        final ArrayList<String> selectedList = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
        if (null != selectedList && selectedList.size() > 0){
            String uids = JSON.toJSONString(selectedList);
            settingNoCollarId(uids,"1");
        }
    }

    private void settingNoCollarId(String uids, String opt) {
//        showProgress(context,false);
        UserApi.settingNoCollarId(uids, teamId, opt, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    initData();
                }else {
                    toast((String) object);
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

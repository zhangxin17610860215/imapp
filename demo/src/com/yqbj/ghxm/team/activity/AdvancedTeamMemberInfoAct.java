package com.yqbj.ghxm.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.team.activity.AdvancedTeamMemberInfoActivity;
import com.netease.yqbj.uikit.business.team.helper.IAdvancedTeamMember;
import com.netease.yqbj.uikit.common.CommonUtil;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.contact.activity.AlbumActivity;
import com.yqbj.ghxm.contact.activity.AlbumDetailActivity;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.session.SessionHelper;
import com.yqbj.ghxm.utils.StringUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AdvancedTeamMemberInfoAct extends AdvancedTeamMemberInfoActivity {


    public static void startActivityForResult(Activity activity, String account, String tid) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, account);
        intent.putExtra(EXTRA_TID, tid);
        intent.setClass(activity, AdvancedTeamMemberInfoAct.class);
        activity.startActivityForResult(intent, REQ_CODE_REMOVE_MEMBER);
    }

    public static void startActivity(Activity activity, String account, String tid) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, account);
        intent.putExtra(EXTRA_TID, tid);
        intent.setClass(activity, AdvancedTeamMemberInfoAct.class);
        activity.startActivity(intent);
    }

    @Override
    protected void getUserBusinessCard(final IAdvancedTeamMember iAdvancedTeamMember) {
        showProgress(this,false);
        UserApi.getUserBusinessCard(account,this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    String urlData = (String) object;
                    iAdvancedTeamMember.getUserBusinessCard(urlData);
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

    @Override
    protected void removeMember() {
        DialogMaker.showProgressDialog(this, getString(com.netease.yqbj.uikit.R.string.empty));
        UserApi.kickTeam(teamId, account, this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                Log.e("踢人成功","踢人成功");
                CommonUtil.uploadTeamIcon(teamId,AdvancedTeamMemberInfoAct.this);


                DialogMaker.dismissProgressDialog();
                makeIntent(account, isSetAdmin, true);
                finish();
                ToastHelper.showToastLong(AdvancedTeamMemberInfoAct.this, com.netease.yqbj.uikit.R.string.update_success);

            }

            @Override
            public void onFailed(String errMessage) {
                DialogMaker.dismissProgressDialog();
                Log.e("踢人失败","踢人失败=="+errMessage);
            }
        });


    }

    @Override
    public void onChat(String account) {
        SessionHelper.startP2PSession(this,account);
    }

    @Override
    protected void albumOnClick(String urlData) {
        super.albumOnClick(urlData);
        if (StringUtil.isNotEmpty(urlData)){
            try {
                List<String> urlList = new ArrayList<>();
                JSONArray array = new JSONArray(urlData);
                for (int i = 0; i < array.length(); i++){
                    urlList.add((String) array.get(i));
                }
                if (urlList.size() > 1){
                    AlbumDetailActivity.start(this,0,urlList,account);
                }else if (urlList.size() == 1){
                    if (account.equals(NimUIKit.getAccount())){
                        //是本人
                        AlbumActivity.start(this,urlData,account);
                    }else {
                        AlbumDetailActivity.start(this,0,urlList,account);
                    }
                }else {
                    AlbumActivity.start(this,urlData,account);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            urlData = "";
            AlbumActivity.start(this,urlData,account);
        }
    }

}

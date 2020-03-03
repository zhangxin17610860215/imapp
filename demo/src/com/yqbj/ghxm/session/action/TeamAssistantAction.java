package com.yqbj.ghxm.session.action;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.netease.nimlib.sdk.team.model.Team;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.TeamRobotDetatlsBean;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.team.activity.RobotWebViewActivity;
import com.yqbj.ghxm.team.activity.TeamAssistantActivity;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.session.actions.BaseAction;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;

public class TeamAssistantAction extends BaseAction {

    private boolean isGroupOwner = false;                           //是否是群主

    /**
     * 构造函数
     */
    public TeamAssistantAction() {
        super(R.drawable.robot_action_icon, R.string.teamassistant);
    }

    @Override
    public void onClick() {
        Team team = NimUIKit.getTeamProvider().getTeamById(getAccount());
        if (team != null) {
            if (team.getCreator().equals(NimUIKit.getAccount())) {
                isGroupOwner = true;
            }else {
                isGroupOwner = false;
            }
        }
        if (isGroupOwner){
            queryRobot();
        }else {
            ToastUtil.showToast(getActivity(),"群助手功能仅限群主使用");
        }
    }

    private void queryRobot() {
        UserApi.getTeamRobotDetatls(getAccount(), getActivity(), new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    TeamRobotDetatlsBean robotBean = (TeamRobotDetatlsBean) object;
                    //跳转机器人持有H5URL的页面加载WebView
                    RobotWebViewActivity.start(getActivity(),getAccount(),robotBean);
                }else {
                    //跳转搜索机器人页面
                    TeamAssistantActivity.start(getActivity(),getAccount(),isGroupOwner);
                }
            }

            @Override
            public void onFailed(String errMessage) {

            }
        });
    }
}

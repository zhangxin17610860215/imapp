package com.yqbj.ghxm.main.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.yqbj.uikit.business.recent.RecentContactsCallback;
import com.netease.yqbj.uikit.business.recent.RecentContactsFragment;
import com.netease.yqbj.uikit.common.ToastHelper;
import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.util.NoFastClickUtils;
import com.netease.yqbj.uikit.common.util.log.LogUtil;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.popuwindow.MyAttachPopupView;
import com.yqbj.ghxm.common.util.AppDemoUtils;
import com.yqbj.ghxm.config.preference.Preferences;
import com.yqbj.ghxm.login.LogoutHelper;
import com.yqbj.ghxm.main.activity.GlobalSearchActivity;
import com.yqbj.ghxm.main.activity.MultiportActivity;
import com.yqbj.ghxm.main.activity.WelcomeActivity;
import com.yqbj.ghxm.main.model.MainTab;
import com.yqbj.ghxm.main.reminder.ReminderManager;
import com.yqbj.ghxm.session.SessionHelper;
import com.yqbj.ghxm.session.extension.GuessAttachment;
import com.yqbj.ghxm.session.extension.RTSAttachment;
import com.yqbj.ghxm.session.extension.RedPacketAttachment;
import com.yqbj.ghxm.session.extension.RedPacketOpenedAttachment;
import com.yqbj.ghxm.session.extension.ShareCardAttachment;
import com.yqbj.ghxm.session.extension.ShareImageAttachment;
import com.yqbj.ghxm.session.extension.SnapChatAttachment;
import com.yqbj.ghxm.session.extension.StickerAttachment;
import com.yqbj.ghxm.session.extension.TeamRobotNotifyAttachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoujianghua on 2015/8/17.
 * 会话
 */
public class SessionListFragment extends MainTabFragment {

    private static final String TAG = SessionListFragment.class.getSimpleName();
    private View notifyBar;

    private TextView notifyBarText;

    // 同时在线的其他端的信息
    private List<OnlineClient> onlineClients;

    private View multiportBar;

    private RecentContactsFragment fragment;

    private RelativeLayout rlSeach;

    public SessionListFragment() {
        this.setContainerId(MainTab.RECENT_CONTACTS.fragmentId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    public void onDestroy() {
        registerObservers(false);
        super.onDestroy();
    }

    @Override
    protected void onInit() {
        findViews();
        registerObservers(true);

        addRecentContactsFragment();
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(clientsObserver, register);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    private void findViews() {
        notifyBar = getView().findViewById(R.id.status_notify_bar);
        notifyBarText = getView().findViewById(R.id.status_desc_label);
        notifyBar.setVisibility(View.GONE);

        multiportBar = getView().findViewById(R.id.multiport_notify_bar);
        multiportBar.setVisibility(View.GONE);
        multiportBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiportActivity.startActivity(getActivity(), onlineClients);
            }
        });


        rlSeach = getView().findViewById(R.id.rl_seach);
        rlSeach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalSearchActivity.start(getContext());
            }
        });
        onInitSetTitle(getContext(), getString(R.string.main_tab_session));
        onInitRightSure(getContext(), R.mipmap.img_main_choice, "", 0);
    }

    @Override
    public void setRightSureClick() {
        comRightSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopuWindow(v);
            }
        });
    }

    /**
     * 显示PopuWinDow
     */
    private void showPopuWindow(View v) {
       boolean isFastClick = NoFastClickUtils.isFastClick();

       if(!isFastClick){
           new XPopup.Builder(getContext())
               .atView(v)
               .asCustom((BasePopupView) new MyAttachPopupView(getContext()))
               .show();
       }else{

       }


    }

    /**
     * 用户状态变化
     */
    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                kickOut(code);
            } else {
                if (code == StatusCode.NET_BROKEN) {
                    notifyBar.setVisibility(View.VISIBLE);
                    notifyBarText.setText(R.string.net_broken);
                } else if (code == StatusCode.UNLOGIN) {
                    notifyBar.setVisibility(View.VISIBLE);
                    notifyBarText.setText(R.string.nim_status_unlogin);
                } else if (code == StatusCode.CONNECTING) {
                    notifyBar.setVisibility(View.VISIBLE);
                    notifyBarText.setText(R.string.nim_status_connecting);
                } else if (code == StatusCode.LOGINING) {
                    notifyBar.setVisibility(View.VISIBLE);
                    notifyBarText.setText(R.string.nim_status_logining);
                } else {
                    notifyBar.setVisibility(View.GONE);
                }
            }
        }
    };

    Observer<List<OnlineClient>> clientsObserver = new Observer<List<OnlineClient>>() {
        @Override
        public void onEvent(List<OnlineClient> onlineClients) {
            SessionListFragment.this.onlineClients = onlineClients;
            if (onlineClients == null || onlineClients.size() == 0) {
                multiportBar.setVisibility(View.GONE);
            } else {
                multiportBar.setVisibility(View.VISIBLE);
                TextView status = multiportBar.findViewById(R.id.multiport_desc_label);
                OnlineClient client = onlineClients.get(0);

                for (OnlineClient temp : onlineClients) {

                }

                switch (client.getClientType()) {
                    case ClientType.Windows:
                    case ClientType.MAC:
                        status.setText(getString(R.string.multiport_logging) + getString(R.string.computer_version));
                        break;
                    case ClientType.Web:
                        status.setText(getString(R.string.multiport_logging) + getString(R.string.web_version));
                        break;
                    case ClientType.iOS:
                    case ClientType.Android:
                        status.setText(getString(R.string.multiport_logging) + getString(R.string.mobile_version));
                        break;
                    default:
                        multiportBar.setVisibility(View.GONE);
                        break;
                }
            }
        }
    };

    private void kickOut(StatusCode code) {
        Preferences.saveUserToken("");

        if (code == StatusCode.PWD_ERROR) {
            LogUtil.e("Auth", "user password error");
            ToastHelper.showToast(getActivity(), R.string.login_failed);
        } else {
            LogUtil.i("Auth", "Kicked!");
        }
        onLogout();
    }

    // 注销
    private void onLogout() {
        // 清理缓存&注销监听&清除状态
        LogoutHelper.logout();

//        LoginActivity.start(getActivity(), true);
        WelcomeActivity.setFirstEnterState(true);
        AppDemoUtils.simpleToAct(getContext(), WelcomeActivity.class);
        ToastHelper.showToast(getContext(),"你的帐号被踢出下线，请注意帐号信息安全");
        getActivity().finish();
    }

    // 将最近联系人列表fragment动态集成进来。 开发者也可以使用在xml中配置的方式静态集成。
    private void addRecentContactsFragment() {
        fragment = new RecentContactsFragment();
        fragment.setContainerId(R.id.messages_fragment);

        final UI activity = (UI) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        fragment = (RecentContactsFragment) activity.addFragment(fragment);

        fragment.setCallback(new RecentContactsCallback() {
            @Override
            public void onRecentContactsLoaded() {
                // 最近联系人列表加载完毕
            }

            @Override
            public void onUnreadCountChange(int unreadCount) {
                ReminderManager.getInstance().updateSessionUnreadNum(unreadCount);
            }

            @Override
            public void onItemClick(RecentContact recent) {
                // 回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作
                switch (recent.getSessionType()) {
                    case P2P:
                        SessionHelper.startP2PSession(getActivity(), recent.getContactId());
                        break;
                    case Team:
                        SessionHelper.startTeamSession(getActivity(), recent.getContactId());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public String getDigestOfAttachment(RecentContact recentContact, MsgAttachment attachment) {
                // 设置自定义消息的摘要消息，展示在最近联系人列表的消息缩略栏上
                // 当然，你也可以自定义一些内建消息的缩略语，例如图片，语音，音视频会话等，自定义的缩略语会被优先使用。
                if (attachment instanceof GuessAttachment) {
                    GuessAttachment guess = (GuessAttachment) attachment;
                    return guess.getValue().getDesc();
                } else if (attachment instanceof RTSAttachment) {
                    return "[白板]";
                } else if (attachment instanceof StickerAttachment) {
                    return "[贴图]";
                } else if (attachment instanceof SnapChatAttachment) {
                    return "[阅后即焚]";
                } else if (attachment instanceof RedPacketAttachment) {
                    return "[红包]";
                } else if (attachment instanceof RedPacketOpenedAttachment) {
                    return ((RedPacketOpenedAttachment) attachment).getDesc(recentContact.getSessionType(), recentContact.getContactId());
                } else if (attachment instanceof ShareImageAttachment) {
                    return ((ShareImageAttachment) attachment).getDesc();
                } else if (attachment instanceof ShareCardAttachment) {
                    return ((ShareCardAttachment) attachment).getDesc();
                } else if (attachment instanceof TeamRobotNotifyAttachment) {
                    return "[群助手消息]";
                }

                return null;
            }

            @Override
            public String getDigestOfTipMsg(RecentContact recent) {
                String msgId = recent.getRecentMessageId();
                List<String> uuids = new ArrayList<>(1);
                uuids.add(msgId);
                List<IMMessage> msgs = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
                if (msgs != null && !msgs.isEmpty()) {
                    IMMessage msg = msgs.get(0);
                    Map<String, Object> content = msg.getRemoteExtension();
                    if (content != null && !content.isEmpty()) {
                        return (String) content.get("content");
                    }
                }

                return null;
            }
        });
    }
}

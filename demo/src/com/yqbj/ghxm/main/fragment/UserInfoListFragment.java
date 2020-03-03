package com.yqbj.ghxm.main.fragment;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.main.model.MainTab;
import com.yqbj.ghxm.chatroom.fragment.tab.UserInfoFragment;

/**
 * 个人中心
 * Created by huangjun on 2015/12/11.
 */
public class UserInfoListFragment extends MainTabFragment {
    private UserInfoFragment fragment;

    public UserInfoListFragment() {
        setContainerId(MainTab.CHAT_ROOM.fragmentId);
    }

    @Override
    protected void onInit() {
        // 采用静态集成，这里不需要做什么了
        fragment = (UserInfoFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.chat_rooms_fragment);
    }

    @Override
    public void onCurrent() {
        super.onCurrent();
        if (fragment != null) {
            fragment.onCurrent();
        }
    }

}

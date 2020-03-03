package com.yqbj.ghxm.main.model;


import com.yqbj.ghxm.R;
import com.yqbj.ghxm.main.fragment.InactiveListFragment;
import com.yqbj.ghxm.main.fragment.InactiveTeamPersonFragment;
import com.yqbj.ghxm.main.fragment.MainTabFragment;

public enum TeamActiveTab {

    INACTIVE(0, 1, InactiveTeamPersonFragment.class, R.string.inactive_person, R.layout.team_active_act),
    EXITTEAM(1, 2, InactiveListFragment.class, R.string.exit_team_person, R.layout.contacts_list);


    public final int tabIndex;

    public final int reminderId;

    public final Class<? extends MainTabFragment> clazz;

    public final int resId;

    public final int fragmentId;

    public final int layoutId;

    TeamActiveTab(int index, int reminderId, Class<? extends MainTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.reminderId = reminderId;
        this.clazz = clazz;
        this.resId = resId;
        this.fragmentId = index;
        this.layoutId = layoutId;
    }

    public static final TeamActiveTab fromReminderId(int reminderId) {
        for (TeamActiveTab value : TeamActiveTab.values()) {
            if (value.reminderId == reminderId) {
                return value;
            }
        }

        return null;
    }

    public static final TeamActiveTab fromTabIndex(int tabIndex) {
        for (TeamActiveTab value : TeamActiveTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }

        return null;
    }
}

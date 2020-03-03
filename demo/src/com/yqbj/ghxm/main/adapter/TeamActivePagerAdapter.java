package com.yqbj.ghxm.main.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.yqbj.ghxm.common.ui.viewpager.SlidingTabPagerAdapter;
import com.yqbj.ghxm.main.fragment.MainTabFragment;
import com.yqbj.ghxm.main.model.TeamActiveTab;

import java.util.List;

public class TeamActivePagerAdapter extends SlidingTabPagerAdapter {

    public TeamActivePagerAdapter(FragmentManager fm, Context context, ViewPager pager) {
        super(fm, TeamActiveTab.values().length, context, pager);
        for (TeamActiveTab tab : TeamActiveTab.values()) {
            try {
                MainTabFragment fragment = null;

                List<Fragment> fs = fm.getFragments();
                if (fs != null) {
                    for (Fragment f : fs) {
                        if (f.getClass() == tab.clazz) {
                            fragment = (MainTabFragment) f;
                            break;
                        }
                    }
                }

                if (fragment == null) {
                    fragment = tab.clazz.newInstance();
                }

                fragment.setState(this);

                fragments[tab.tabIndex] = fragment;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCacheCount() {
        return TeamActiveTab.values().length;
    }

    @Override
    public int getCount() {
        return TeamActiveTab.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TeamActiveTab tab = TeamActiveTab.fromTabIndex(position);

        int resId = tab != null ? tab.resId : 0;

        return resId != 0 ? context.getText(resId) : "";
    }
}

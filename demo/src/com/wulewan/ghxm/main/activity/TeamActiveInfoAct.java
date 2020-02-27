package com.wulewan.ghxm.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.main.adapter.TeamActivePagerAdapter;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.common.ui.viewpager.FadeInOutPageTransformer;
import com.wulewan.ghxm.common.ui.viewpager.PagerSlidingTabStrip;

public class TeamActiveInfoAct extends BaseAct implements ViewPager.OnPageChangeListener {


    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private String teamId;
    private static final String EXTRA_ID = "EXTRA_ID";

    private TeamActivePagerAdapter adapter;
    private int scrollState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_active_act);
        setToolbar(R.drawable.jrmf_b_top_back,"群成员活跃度");
        init();

    }

    public String getTeamId() {
        return teamId;
    }

    public static void start(Context context, String tid) {

        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, TeamActiveInfoAct.class);
        context.startActivity(intent);
    }


    private void init(){

        findViews();
        parseIntentData();
        setupPager();
        setupTabs();

    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);

    }

    private void findViews(){
        tabs = findView(R.id.tabs);
        pager = findView(R.id.main_tab_pager);
    }


    private void setupPager() {
        adapter = new TeamActivePagerAdapter(getSupportFragmentManager(), this, pager);
        pager.setOffscreenPageLimit(adapter.getCacheCount());
        pager.setPageTransformer(true, new FadeInOutPageTransformer());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
    }

    private void setupTabs() {
        tabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {
            @Override
            public int getTabLayoutResId(int position) {
                return R.layout.tab_layout_main;
            }

            @Override
            public boolean screenAdaptation() {
                return true;
            }
        });
        tabs.setViewPager(pager);
        tabs.setOnTabClickListener(adapter);
        tabs.setOnTabDoubleTapListener(adapter);
    }

    private void selectPage() {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter.onPageSelected(pager.getCurrentItem());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        adapter.onPageScrolled(position);
    }

    @Override
    public void onPageSelected(int position) {
        tabs.onPageSelected(position);
        selectPage();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        tabs.onPageScrollStateChanged(state);
        scrollState = state;
        selectPage();
    }
}

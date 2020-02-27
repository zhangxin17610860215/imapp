package com.wulewan.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.utils.NumberUtil;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.view.PickerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 红包延时领取时间设置
 * */
public class TeamTimeSettingActivity extends BaseAct {

    private Context context;
    private PickerView time_pv;
    private String time = "关闭";

    public static void start(Context context, String teamId) {
        Intent intent = new Intent();
        intent.setClass(context, TeamTimeSettingActivity.class);
        intent.putExtra("teamId", teamId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_teamtimesetting_layout);
        context = this;
        setToolbar(R.drawable.jrmf_b_top_back,"时间设置");
        setRightText("保存", new onToolBarListner() {
            @Override
            public void onRight() {
                Intent intent = new Intent();

                if (time.equals("关闭")){
                    time = "0";
                }else {
                    String substring = time.substring(0, 2);
                    time = String.valueOf(NumberUtil.mul(substring,"60"));
                }

                intent.putExtra("time", StringUtil.effectiveNum(time));
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        initView();
    }

    private void initView() {
        time_pv = (PickerView) findViewById(R.id.time_pv);
        List<String> times = new ArrayList<>();
        times.add("关闭");
        times.add("20分钟");
        times.add("30分钟");
        times.add("40分钟");
        times.add("50分钟");
        times.add("60分钟");

        time_pv.setData(times);
        time_pv.setSelected(0);
        time_pv.setOnSelectListener(new PickerView.onSelectListener(){

            @Override
            public void onSelect(String text){
                time = text;
            }
        });
    }

}

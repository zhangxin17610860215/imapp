package com.wulewan.ghxm.redpacket.wallet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.jrmf360.normallib.base.utils.ToastUtil;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.utils.NumberUtil;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 明细查询页面
 * */
public class DetailedQueryActivity extends BaseAct implements View.OnClickListener {
    private static final String TAG = DetailedQueryActivity.class.getSimpleName();

    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvDetermine;

    private String startDateStr = "";       //开始时间
    private String endDateStr = "";         //结束时间
    private int markerBit;          //标记位   1=开始时间    2=结束时间

    private String dialogTitle = "";

    public static void start(Context context) {
        Intent intent = new Intent(context, DetailedQueryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailedquery_activity);

        initView();
    }

    private void initView() {
        setToolbar("查询零钱明细");
        tvStartDate = findView(R.id.tv_detailedquery_startDate);
        tvEndDate = findView(R.id.tv_detailedquery_endDate);
        tvDetermine = findView(R.id.tv_detailedquery_Determine);
        tvStartDate.setOnClickListener(this);
        tvEndDate.setOnClickListener(this);
        tvDetermine.setOnClickListener(this);
    }

    private void initPopupWindow() {
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        //正确设置方式 原因：注意事项有说明
        startDate.set(2013,0,1);
        endDate.set(2020,11,31);
        //时间选择器
        TimePickerView pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if (markerBit == 1){
                    startDateStr = TimeUtils.getDateToString(date.getTime(),TimeUtils.TIME_TYPE_02);
                    tvStartDate.setText(startDateStr);
                }else {
                    endDateStr = TimeUtils.getDateToString(date.getTime(),TimeUtils.TIME_TYPE_02);
                    tvEndDate.setText(endDateStr);
                }
            }
        }).setCancelText("取消")//取消按钮文字
                .setSubmitText("确认")//确认按钮文字
                .setTitleSize(18)//标题文字大小
                .setTitleText(dialogTitle)//标题文字
                .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(false)//是否循环滚动
                .setTextColorCenter(Color.BLACK)
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(0xFF3CBEA1)//确定按钮文字颜色
                .setCancelColor(0xFF3CBEA1)//取消按钮文字颜色
                .setTitleBgColor(Color.WHITE)//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setRangDate(startDate,endDate)//起始终止年月日设定
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(true)//是否显示为对话框样式
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间
          .setLabel("年","月","日",null,null,null)//默认设置为年月日时分秒
          .build();
        pvTime.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_detailedquery_startDate:
                //选择开始时间
                markerBit = 1;
                dialogTitle = "选择开始时间";
                initPopupWindow();
                break;
            case R.id.tv_detailedquery_endDate:
                //选择结束时间
                markerBit = 2;
                dialogTitle = "选择结束时间";
                initPopupWindow();
                break;
            case R.id.tv_detailedquery_Determine:
                //确定
                if (StringUtil.isEmpty(startDateStr) || StringUtil.isEmpty(endDateStr)){
                    ToastUtil.showToast(this,"请先选择起止时间");
                    return;
                }
                startDateStr = startDateStr.replace("-","");
                endDateStr = endDateStr.replace("-","");
                if (NumberUtil.compareLess(endDateStr,startDateStr)){
                    ToastUtil.showToast(this,"结束时间不得小于开始时间");
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("startDateStr", tvStartDate.getText());
                intent.putExtra("endDateStr", tvEndDate.getText());
                this.setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}

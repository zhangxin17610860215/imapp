package com.yqbj.ghxm.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.utils.StringUtil;

public class FeedbackActivity extends BaseAct {

    private Context context;
    private TextView tvSubmission;
    private EditText etFeedback;
    private String content = "";
    private Handler handler = new Handler();

    public static void start(Context context) {
        Intent intent = new Intent(context, FeedbackActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_activity_layout);
        context = this;
        initView();
    }

    private void initView() {
        setToolbar("意见反馈");
        tvSubmission = (TextView) findViewById(R.id.tv_Submission);
        etFeedback = (EditText) findViewById(R.id.et_feedback);
        tvSubmission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = etFeedback.getText().toString().trim();
                if (StringUtil.isEmpty(content)){
                    toast("内容不能为空");
                    return;
                }
                showProgress(context,false);
                handler.postDelayed(delayedSubmission, 1500);
            }
        });
    }

    private Runnable delayedSubmission = new Runnable() {
        @Override
        public void run() {
            dismissProgress();
            toast("提交成功");
            etFeedback.setText("");
        }
    };
}

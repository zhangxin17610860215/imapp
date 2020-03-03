package com.yqbj.ghxm.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.netease.yqbj.uikit.api.model.session.SessionCustomization;
import com.netease.yqbj.uikit.business.session.constant.Extras;
import com.netease.yqbj.uikit.common.activity.UI;


public class InactionGroupListAct extends UI {

    private static final String EXTRA_ID = "EXTRA_ID";

    private Button btnScan;

    private static final int OCR_FONT =100;
    private static final int OCR_BACK= 101;

    private static final int REQUEST_CODE_SCAN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.inaction_group_list);
//
//        btnScan = (Button) findViewById(R.id.btnScan);
//
//        btnScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(InactionGroupListAct.this, CaptureActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SCAN);
//            }
//        });



    }

    public static void start(Context context, String tid, SessionCustomization customization) {
//        ToastHelper.showToast(context,"启动advance");

        Log.e("tid",tid);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);

        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        intent.setClass(context, InactionGroupListAct.class);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }


}

package com.yqbj.ghxm.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.main.SplashActivity;
import com.yqbj.ghxm.utils.DensityUtils;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.view.FontSizeView;
import com.netease.yqbj.uikit.common.activity.BaseAct;
import com.netease.yqbj.uikit.common.util.AppManager;

import java.util.ArrayList;

public class SetFontSizeActivity extends BaseAct {

    private ArrayList<String> volume_sections = new ArrayList<String>();

    private FontSizeView fontSizeView;


    private float fontSizeScale;
    private boolean isChange;//用于监听字体大小是否有改动
    private int defaultPos;

    private TextView tv_font_size1, tv_font_size2, tv_font_size3;

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, SetFontSizeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_font_size);
        setToolbar(R.drawable.jrmf_b_top_back, "字体大小");
        setRightText("保存", new onToolBarListner() {
            @Override
            public void onRight() {
                if (isChange) {
                    SPUtils.getInstance().put("app_font_size", fontSizeScale);
                    //重启应用
                    AppManager.getAppManager().finishAllActivity();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(SetFontSizeActivity.this,SplashActivity.class));
                        }
                    });
                } else {
                    finish();
                }
            }
        });
        tv_font_size1 = (TextView) findView(R.id.tv_font_size1);
        tv_font_size2 = (TextView) findView(R.id.tv_font_size2);
        tv_font_size3 = (TextView) findView(R.id.tv_font_size3);

        fontSizeView = (FontSizeView) findView(R.id.fsv_font_size);
        fontSizeView.setChangeCallbackListener(new FontSizeView.OnChangeCallbackListener() {
            @Override
            public void onChangeListener(int position) {
                int dimension = getResources().getDimensionPixelSize(R.dimen.text_size_15);
                //根据position 获取字体倍数
                fontSizeScale = (float) (0.875 + 0.125 * position);
                //放大后的sp单位
                double v = fontSizeScale * (int) DensityUtils.px2sp(SetFontSizeActivity.this, dimension);
                //改变当前页面大小
                changeTextSize((int) v);
                isChange = !(position == defaultPos);
            }
        });
        float scale = SPUtils.getInstance().getFloat("app_font_size", 0.0f);
        if (scale > 0.5) {
            defaultPos = (int) ((scale - 0.875) / 0.125);
        } else {
            defaultPos = 1;
        }
        fontSizeView.setDefaultPosition(defaultPos);

    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        config.fontScale = 1;//1 设置正常字体大小的倍数
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    /**
     * 改变textsize 大小
     */
    private void changeTextSize(int dimension) {
        tv_font_size1.setTextSize(dimension);
        tv_font_size2.setTextSize(dimension);
        tv_font_size3.setTextSize(dimension);
    }

}

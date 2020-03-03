package com.yqbj.ghxm.common.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.LoginBean;
import com.yqbj.ghxm.utils.SPUtils;
import com.netease.yqbj.uikit.common.activity.UI;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.OverallApi;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;

import java.util.List;


public class BaseAct extends UI {

    public TextView tv_right;

    public static boolean isActive; //全局变量  app是否进入后台

    public void setToolbar(int logo, String title) {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        }
        if (logo != 0) {
            toolbar.setNavigationIcon(logo);
        }else {
            toolbar.setNavigationIcon(null);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView tv = (TextView) findViewById(R.id.toolbar_title);
            tv.setText(title);
        }
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }


    public void setToolbar(String title) {
        int logo = R.drawable.jrmf_b_top_back;
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        }
        if (logo != 0) {
            toolbar.setNavigationIcon(logo);
        }else {
            toolbar.setNavigationIcon(null);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView tv = (TextView) findViewById(R.id.toolbar_title);
            tv.setText(title);
        }
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    public void setToolbar(int logo, String title,int toolBarRes) {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.app_toolbar);

            toolbar.setBackgroundResource(toolBarRes);
        }
        if (logo != 0) {
            toolbar.setNavigationIcon(logo);
        }else {
            toolbar.setNavigationIcon(null);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView tv = (TextView) findViewById(R.id.toolbar_title);
            tv.setText(title);
        }
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    public void setRightImg(int imgRes,final onToolBarRightImgListener rightImgListener){
        if(imgRes!=0){
            ImageView img_right =  findView(R.id.toolbar_img_right);
            img_right.setVisibility(View.VISIBLE);
            img_right.setImageResource(imgRes);
            if(rightImgListener!=null){
                img_right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        rightImgListener.onRight();
                    }
                });
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            TextView tv = (TextView) findViewById(R.id.toolbar_title);
            tv.setText(title);
        }
        toolbar.setTitle("");
    }

    public void setRightText(String text, final onToolBarListner mlistener){
        if(tv_right==null) {
            tv_right = (TextView) findViewById(R.id.tv_right);

        }
        tv_right.setText(text);
        if(mlistener!=null){
            tv_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mlistener.onRight();
                }
            });
        }
    }


    public interface onToolBarListner{

        public void onRight();
    }

    public interface onToolBarRightImgListener{
        public void onRight();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isActive) {
            //app 从后台唤醒，进入前台
            isActive = true;
            //执行冷启动处理
            long loginTime = SPUtils.getInstance().getLong(Constants.CURRENTTIME);
            long currentTime = System.currentTimeMillis();
            long betweenTime = currentTime - loginTime;
            if (betweenTime > Constants.APP_BACK_GROUND_LIMIT_SECONDS * 1000) {
                againLogin();
            }
        }
    }

    private void againLogin() {
        OverallApi.getKey(BaseAct.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                String openid = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.OPENID);
                String uuid = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.UUID);
                UserApi.login(null,openid,uuid,BaseAct.this, new requestCallback() {
                    @Override
                    public void onSuccess(int code, Object object) {
                        if (code == Constants.SUCCESS_CODE){
                            LoginBean loginBean = (LoginBean) object;
                            SPUtils.getInstance().put(Constants.USER_TYPE.USERTOKEN, loginBean.getUserToken());
                            SPUtils.getInstance().put(Constants.USER_TYPE.YUNXINTOKEN, loginBean.getYunxinToken());
                            SPUtils.getInstance().put(Constants.USER_TYPE.ACCID, loginBean.getAccid());
                            OverallApi.configInfo(BaseAct.this);
                        }
                    }

                    @Override
                    public void onFailed(String errMessage) {

                    }
                });
            }
            @Override
            public void onFailed(String errMessage) {

            }
        });
    }

    @Override
    protected void onStop() {
        if (!isAppOnForeground()) {
            //app 进入后台
            isActive = false;//记录当前已经进入后台
        }
        super.onStop();
    }

    /**
     * APP是否处于前台唤醒状态
     *
     * @return
     */
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

}

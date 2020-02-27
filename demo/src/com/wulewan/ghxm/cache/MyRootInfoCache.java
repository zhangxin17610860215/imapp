package com.wulewan.ghxm.cache;

import android.content.Context;
import android.text.TextUtils;

import com.wulewan.ghxm.bean.RootInfoBean;
import com.wulewan.ghxm.bean.RootListBean;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.netease.wulewan.uikit.api.StatisticsConstants.ROBOT_IDS;

public class MyRootInfoCache {

    private Context context;

    public static MyRootInfoCache getInstance() {
        return MyRootInfoCache.InstanceHolder.instance;
    }

    /**
     * 数据
     */

    private Map<String, RootInfoBean> robotMap = new ConcurrentHashMap<>();

    /**
     * 初始化&清理
     */

    public void clear() {
        clearRobotCache();
    }

    public void buildCache(Context context, final VisitCallback callback) {
        // 获取所有有效的机器人
        robotMap.clear();
        UserApi.getRootList(context, new requestCallback<RootListBean>() {
            @Override
            public void onSuccess(int code, RootListBean object) {

                for (RootInfoBean r:object.getRobots()) {
                    robotMap.put(r.getAccid(),r);
                    ROBOT_IDS.add(r.getAccid());
                }
                callback.onSuccess(code,object);
            }

            @Override
            public void onFailed(String errMessage) {
                callback.onFailed(errMessage);
            }
        });

    }

    private void clearRobotCache() {
        robotMap.clear();

    }



    public void fetchRobotList() {
        robotMap.clear();
        UserApi.getRootList(this, new requestCallback<RootListBean>() {
            @Override
            public void onSuccess(int code, RootListBean object) {

                for (RootInfoBean r:object.getRobots()) {
                    robotMap.put(r.getAccid(),r);
                }

            }

            @Override
            public void onFailed(String errMessage) {

            }
        });
    }

    /**
     * ****************************** 机器人信息查询接口 ******************************
     */

    public List<RootInfoBean> getAllRobotAccounts() {
        return new ArrayList<>(robotMap.values());
    }

    public RootInfoBean getRobotByAccount(String account) {
        if (TextUtils.isEmpty(account)) {
            return null;
        }

        return robotMap.get(account);
    }



    /**
     * ************************************ 单例 **********************************************
     */

    static class InstanceHolder {
        final static MyRootInfoCache instance = new MyRootInfoCache();
    }

    public interface VisitCallback{
        void onSuccess(int code, RootListBean rootListBean);
        void onFailed(String errorMessage);
    }
}

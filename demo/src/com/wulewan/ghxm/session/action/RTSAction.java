package com.wulewan.ghxm.session.action;


import com.netease.wulewan.uikit.common.ToastHelper;

import com.wulewan.ghxm.R;
import com.netease.wulewan.rtskit.RTSKit;
import com.netease.wulewan.uikit.business.session.actions.BaseAction;
import com.netease.wulewan.uikit.common.util.sys.NetworkUtil;

/**
 * Created by huangjun on 2015/7/7.
 */
public class RTSAction extends BaseAction {

    public RTSAction() {
        super(R.drawable.message_plus_rts_selector, R.string.input_panel_RTS);
    }

    @Override
    public void onClick() {
        if (NetworkUtil.isNetAvailable(getActivity())) {
            RTSKit.startRTSSession(getActivity(), getAccount());
        } else {
            ToastHelper.showToast(getActivity(), R.string.network_is_not_available);
        }

    }
}

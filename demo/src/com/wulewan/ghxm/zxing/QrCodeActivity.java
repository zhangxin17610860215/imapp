package com.wulewan.ghxm.zxing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.TurnQrCodeBean;
import com.wulewan.ghxm.utils.SPUtils;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.activity.UI;
import com.netease.wulewan.uikit.common.ui.imageview.HeadImageView;
import com.netease.wulewan.uikit.common.util.sys.ScreenUtil;
import com.wulewan.ghxm.config.Constants;

public class QrCodeActivity extends UI implements View.OnClickListener {
    private ImageView qrCode;
    private ImageButton back;
    private TextView title;
    private TextView showName;
    private TextView showId;
    private LinearLayout invaviteLL;
    private HeadImageView sIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);
        initView();
        initViewData();
        initClick();
    }

    private void initView() {
        qrCode = findView(R.id.qr_code_pic);
        back = findView(R.id.back);
        title = findView(R.id.qr_title);
        showName = findView(R.id.user_name);
        showId = findView(R.id.user_id);
        invaviteLL = findView(R.id.invavite_group);
        invaviteLL.setVisibility(View.GONE);
        sIcon = findView(R.id.user_head);
    }

    @SuppressLint("SetTextI18n")
    private void initViewData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String type = bundle.getString("type", ZXingUtils.TYPE_PERSON);
        String id = bundle.getString("id", SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID));
        int width = bundle.getInt("width", 800);
        int height = bundle.getInt("height", 800);
        showName.setText(bundle.getString("name")); //用户名称，群名称
        sIcon.setIsRect(true);
        if (type.equals(ZXingUtils.TYPE_PERSON)) {
            showId.setText("工会小蜜号:" + id);
            title.setText("我的二维码");
            sIcon.loadAvatar(bundle.getString("icon"));
        } else if (type.equals(ZXingUtils.TYPE_GROUP)) {
            showId.setText("群ID:" + id);
            title.setText("群二维码");
            sIcon.loadTeamIconByTeam(NimUIKit.getTeamProvider().getTeamById(id));
        }
        try {
            TurnQrCodeBean turnQrCodeBean = new TurnQrCodeBean();
            turnQrCodeBean.type = type;
            turnQrCodeBean.id = id;

            width = ScreenUtil.dip2px(250);
            height = ScreenUtil.dip2px(250);
            ZXingUtils.createQrCode(turnQrCodeBean, qrCode, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initClick() {
        back.setOnClickListener(this);
        invaviteLL.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.invavite_group:
                break;
        }
    }
}

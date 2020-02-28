package com.wulewan.ghxm.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.ui.imageview.HeadImageView;
import com.netease.wulewan.uikit.utils.NoDoubleClickUtils;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.GetAllMemberWalletBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.utils.NumberUtil;
import com.wulewan.ghxm.utils.StringUtil;

/**
 * 设置玩家群蜜币
 * */
public class SetMiBiActivity extends BaseAct implements View.OnClickListener {

    private Activity mActivity;
    private GetAllMemberWalletBean.ResultsBean bean = new GetAllMemberWalletBean.ResultsBean();

    private TextView tvAdd;
    private ImageView imgAdd;
    private TextView tvSubtraction;
    private ImageView imgSubtraction;
    private HeadImageView imgHead;
    private EditText etMibiNum;
    private TextView tvDetermine;
    private String symbol = "";

    public static void start(Context context, GetAllMemberWalletBean.ResultsBean bean) {
        Intent intent = new Intent();
        intent.setClass(context, SetMiBiActivity.class);
        intent.putExtra("bean", bean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setmibi_activity_layout);
        mActivity = this;
        bean = (GetAllMemberWalletBean.ResultsBean) getIntent().getSerializableExtra("bean");

        initView();
        initData();
    }

    private void initView() {
        imgHead = findView(R.id.img_head);
        tvAdd = findView(R.id.tv_add);
        imgAdd = findView(R.id.img_add);
        tvSubtraction = findView(R.id.tv_subtraction);
        imgSubtraction = findView(R.id.img_subtraction);
        etMibiNum = findView(R.id.et_mibiNum);
        tvDetermine = findView(R.id.tv_Determine);
        tvAdd.setOnClickListener(this);
        tvSubtraction.setOnClickListener(this);
        tvDetermine.setOnClickListener(this);
    }

    private void initData() {
        UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(bean.getUid());
        if (null != userInfo){
            String name = userInfo.getName();
            setToolbar(R.drawable.jrmf_b_top_back,name+"余额设定");
            imgHead.loadAvatar(userInfo.getAvatar());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_add:
                symbol = "";
                imgAdd.setVisibility(View.VISIBLE);
                imgSubtraction.setVisibility(View.GONE);
                tvAdd.setTextColor(getResources().getColor(R.color.theme_color));
                tvSubtraction.setTextColor(getResources().getColor(R.color.color_black_333333));
                break;
            case R.id.tv_subtraction:
                symbol = "-";
                imgAdd.setVisibility(View.GONE);
                imgSubtraction.setVisibility(View.VISIBLE);
                tvAdd.setTextColor(getResources().getColor(R.color.color_black_333333));
                tvSubtraction.setTextColor(getResources().getColor(R.color.theme_color));
                break;
            case R.id.tv_Determine:
                if (StringUtil.isEmpty(etMibiNum.getText().toString()) || etMibiNum.getText().toString().equals("0")){
                    toast("请输入需要调整的数额");
                    return;
                }
                if (NumberUtil.compareGreater(etMibiNum.getText().toString(),"10000")){
                    toast("加减数额不得超过10000");
                    return;
                }
                if (symbol.equals("-") && NumberUtil.compareGreater(etMibiNum.getText().toString(),bean.getScore()+"")){
                    toast("该成员余额不足");
                    return;
                }
                if (!NoDoubleClickUtils.isDoubleClick(500)){
                    setMemberWallet(symbol+etMibiNum.getText().toString());
                }
                break;
        }
    }

    private void setMemberWallet(String score) {
        showProgress(mActivity,false);
        UserApi.setMemberWallet(bean.getTid(), bean.getUid(), score, mActivity, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    toast("设置成功");
                    finish();
                }else {
                    toast((String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }
}

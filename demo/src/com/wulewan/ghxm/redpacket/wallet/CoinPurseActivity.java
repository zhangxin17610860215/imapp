package com.wulewan.ghxm.redpacket.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.AmountBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.user.BindALiPayActivity;
import com.wulewan.ghxm.user.PasswordManageActivity;
import com.wulewan.ghxm.user.RetrievePayPwdActivity;
import com.wulewan.ghxm.utils.StringUtil;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.utils.NoDoubleClickUtils;
import com.umeng.analytics.MobclickAgent;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;

import java.util.Map;

import static com.wulewan.ghxm.config.Constants.ALI_USERID;
import static com.netease.wulewan.uikit.api.StatisticsConstants.COIN_ALIBIND_TOTALNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.COIN_CARRY_TOTALNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.COIN_CHANGEDETAILSCELL;

/**
 * 零钱包
 * */
public class CoinPurseActivity extends BaseAct implements View.OnClickListener {

    private static final String TAG = CoinPurseActivity.class.getSimpleName();

    private TextView tvBalance;
    private RelativeLayout rlRecharge;
    private RelativeLayout rlCarry;
    private RelativeLayout rlDetailsChange;
    private RelativeLayout rlDetailsRedPacket;
    private RelativeLayout rlAlipay;
    private RelativeLayout rlPasswordManage;

    private String balance = "";

    public static void start(Context context) {
        Intent intent = new Intent(context, CoinPurseActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coinpurse_activity);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {

        setToolbar("零钱包");

        tvBalance = findView(R.id.tv_coinPurse_balance);
        rlRecharge = findView(R.id.rl_coinPurse_Recharge);
        rlCarry = findView(R.id.rl_coinPurse_Carry);
        rlDetailsChange = findView(R.id.rl_coinPurse_DetailsChange);
        rlDetailsRedPacket = findView(R.id.rl_coinPurse_DetailsRedPacket);
        rlAlipay = findView(R.id.rl_coinPurse_Alipay);
        rlPasswordManage = findView(R.id.rl_coinPurse_PasswordManage);

        rlRecharge.setOnClickListener(this);
        rlCarry.setOnClickListener(this);
        rlDetailsChange.setOnClickListener(this);
        rlDetailsRedPacket.setOnClickListener(this);
        rlAlipay.setOnClickListener(this);
        rlPasswordManage.setOnClickListener(this);
    }

    private void initData() {
//        showProgress(this,false);
        UserApi.getAmount(this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
//                dismissProgress();
                if (code==Constants.RESPONSE_CODE.CODE_50002){
                    //用户钱包账户不存在
                    SettingPayPasswordActivity.start(CoinPurseActivity.this);
                    finish();
                }else {
                    try {
                        AmountBean amountBean = (AmountBean) object;
                        balance = amountBean.getAmount();
                        tvBalance.setText("¥" + balance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailed(String errMessage) {
//                dismissProgress();
                ToastUtil.showToast(CoinPurseActivity.this,errMessage);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()){
                case R.id.rl_coinPurse_Recharge:
                    //充值
                    RechargeActivity.start(this,balance);
                    break;
                case R.id.rl_coinPurse_Carry:
                    //提现
                    MobclickAgent.onEvent(this,COIN_CARRY_TOTALNUM);
                    CarryActivity.start(this,balance);
//                if (SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getBoolean(Constants.ALIPAY_USERINFO.ISBINDALIPAY)){
//                    CarryActivity.start(this);
//                }else {
//                    //弹框提示先去绑定支付宝
//                    showBindAliPayDialog();
//                }
                    break;
                case R.id.rl_coinPurse_DetailsChange:
                    //零钱明细
                    MobclickAgent.onEvent(this,COIN_CHANGEDETAILSCELL);
                    DetailsChangeActivity.start(this);
                    break;
                case R.id.rl_coinPurse_DetailsRedPacket:
                    //红包明细
                    DetailsRedPacketActivity.start(this);
                    break;
                case R.id.rl_coinPurse_Alipay:
                    //支付宝绑定
                    MobclickAgent.onEvent(this,COIN_ALIBIND_TOTALNUM);
                    NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(NimUIKit.getAccount());
                    Map<String, Object> extensionMap = user.getExtensionMap();
                    if (null == extensionMap){
                        RetrievePayPwdActivity.start(this,"2");
                        return;
                    }
                    String userId = (String) extensionMap.get(ALI_USERID);
                    if (StringUtil.isEmpty(userId)){
                        RetrievePayPwdActivity.start(this,"2");
                    }else {
                        BindALiPayActivity.start(this);
                    }
                    break;
                case R.id.rl_coinPurse_PasswordManage:
                    //密码管理
                    PasswordManageActivity.start(this);
                    break;
            }
        }
    }

    private void showBindAliPayDialog() {
        EasyAlertDialogHelper.showCommonDialog(this, "未绑定支付宝", "请绑定支付宝后再进行提现", "确定", "取消", false, new EasyAlertDialogHelper.OnDialogActionListener() {
            @Override
            public void doCancelAction() {

            }

            @Override
            public void doOkAction() {
                RetrievePayPwdActivity.start(CoinPurseActivity.this,"2");
            }
        }).show();
    }
}

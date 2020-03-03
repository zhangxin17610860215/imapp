package com.yqbj.ghxm.redpacket.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.lxj.xpopup.XPopup;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.utils.NoDoubleClickUtils;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.user.RetrievePayPwdActivity;
import com.yqbj.ghxm.utils.NumberUtil;
import com.yqbj.ghxm.utils.RedPacketTextWatcher;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.view.PayBottomPup;
import com.yqbj.ghxm.utils.view.PayDialogView;

import java.util.Map;

import static com.yqbj.ghxm.config.Constants.ALI_USERID;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.ALICARRY_ISSHOW;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.ALI_UPPERLIMIT;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WCHATCARRY_ISSHOW;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WX_UPPERLIMIT;
import static com.yqbj.ghxm.config.Constants.RESPONSE_CODE.CODE_50021;
import static com.yqbj.ghxm.config.Constants.RESPONSE_CODE.CODE_50022;

/**
 * 提现
 * */
public class CarryActivity extends BaseAct implements View.OnClickListener {

    private TextView tvAliName;
    private TextView tvServiceCharge;
    private TextView tvDetermine;
    private TextView tvCarryText;
    private EditText etAmount;
    private RelativeLayout rlChoice;
    private ImageView imgPayIcon;

    private String money;
    private String payType = "";       //默认提现类型   微信 1   支付宝 2
    private String accountId;

    private String aLiCarry = "";
    private String wChatCarry = "";

    private String wChatUpperLimit = "";
    private String aliUpperLimit = "";

    private String balance = "0";

    public static void start(Context context,String balance) {
        Intent intent = new Intent(context, CarryActivity.class);
        intent.putExtra("balance",balance);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_carry);
        balance = getIntent().getStringExtra("balance");
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        aLiCarry = instance.getString(ALICARRY_ISSHOW);
        wChatCarry = instance.getString(WCHATCARRY_ISSHOW);

        if (!aLiCarry.equals("1")){
            tvAliName.setText("微信账户");
            imgPayIcon.setImageResource(R.mipmap.pay_bottompup_wchat);
            payType = "1";
            accountId = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.OPENID);
        }

        if (!wChatCarry.equals("1")){
            tvAliName.setText("支付宝账户");
            imgPayIcon.setImageResource(R.mipmap.pay_bottompup_ali);
            payType = "2";
            NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(NimUIKit.getAccount());
            Map<String, Object> extensionMap = user.getExtensionMap();
            if (null != extensionMap){
                accountId = (String) extensionMap.get(ALI_USERID);
            }
        }

        if (!aLiCarry.equals("1") && !wChatCarry.equals("1")){
            tvAliName.setVisibility(View.GONE);
            imgPayIcon.setVisibility(View.GONE);
            payType = "";
            accountId = "";
        }

        if (aLiCarry.equals("1") && wChatCarry.equals("1")){
            tvAliName.setText("微信账户");
            imgPayIcon.setImageResource(R.mipmap.pay_bottompup_wchat);
            payType = "1";
            accountId = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.OPENID);
        }

        wChatUpperLimit = instance.getString(WX_UPPERLIMIT);
        aliUpperLimit = instance.getString(ALI_UPPERLIMIT);

        tvCarryText.setText(String.format("• 余额提现单日单笔最高金额微信为%s元,支付宝为%s元,最低不得低于10元,微信单日累计总提现额度为500元,支付宝单日累计总提现额度为5000元,单笔提现手续费按照费率0.75%%收取,不足0.1元按照0.1元收取。",wChatUpperLimit,aliUpperLimit));
    }

    private void initView() {
        setToolbar("提现");
        tvAliName = findView(R.id.tv_carry_alname);
        imgPayIcon = findView(R.id.img_carry_payIcon);
        tvServiceCharge = findView(R.id.tv_carry_ServiceCharge);
        tvDetermine = findView(R.id.tv_carry_Determine);
        tvCarryText = findView(R.id.tv_carry_text);
        etAmount = findView(R.id.et_carry_Amount);
        rlChoice = findView(R.id.rl_carry_Choice);
        rlChoice.setOnClickListener(this);
        tvDetermine.setOnClickListener(this);
//        tvAliName.setText(SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.NICKNAME));
        etAmount.addTextChangedListener(new RedPacketTextWatcher(etAmount).addView(2,tvServiceCharge));

    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()){
                case R.id.tv_carry_Determine:
                    //确定提现
                    money = etAmount.getText().toString();
                    if (StringUtil.isEmpty(money)){
                        ToastUtil.showToast(this,"请输入提现金额");
                        return;
                    }
                    if(NumberUtil.compareLess(money,"10")){
                        ToastUtil.showToast(this,"提现金额不得少于10元");
                        return;
                    }
                    if(NumberUtil.compareLess(balance,money)){
                        ToastUtil.showToast(this,"金额已超过可提现余额");
                        return;
                    }
                    if (StringUtil.isEmpty(payType) && StringUtil.isEmpty(accountId)){
                        toast("暂不支持提现，请联系客服");
                        return;
                    }
                    if (payType.equals("1")){
                        if (NumberUtil.compareLess(wChatUpperLimit,money)){
                            ToastUtil.showToast(this,"微信单笔提现金额不得大于" + wChatUpperLimit);
                            return;
                        }
                    }
                    if (payType.equals("2")){
                        if (NumberUtil.compareLess(aliUpperLimit,money)){
                            ToastUtil.showToast(this,"支付宝单笔提现金额不得大于" + aliUpperLimit);
                            return;
                        }
                    }
                    if (payType.equals("2")){
                        NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(NimUIKit.getAccount());
                        Map<String, Object> extensionMap = user.getExtensionMap();
                        if (null == extensionMap || StringUtil.isEmpty((String) extensionMap.get(ALI_USERID))){
                            EasyAlertDialogHelper.showCommonDialog(this, "未绑定支付宝", "请绑定支付宝后再进行提现", "确定", "取消", false, new EasyAlertDialogHelper.OnDialogActionListener() {
                                @Override
                                public void doCancelAction() {

                                }

                                @Override
                                public void doOkAction() {
                                    RetrievePayPwdActivity.start(CarryActivity.this,"2");
                                }
                            }).show();
                            return;
                        }
                    }
                    showDialog();
                    break;
                case R.id.rl_carry_Choice:
                    //选择支付方式
                    showPayBottomPup(v);
                    break;
            }
        }

    }

    private void showPayBottomPup(View v) {
        PayBottomPup payBottomPup = new PayBottomPup(this);
        new XPopup.Builder(this)
                .dismissOnTouchOutside(true)
                .asCustom(payBottomPup)
                .show();
        payBottomPup.setOnALiClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAliName.setText("支付宝账户");
                imgPayIcon.setImageResource(R.mipmap.pay_bottompup_ali);
                payType = "2";
                NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(NimUIKit.getAccount());
                Map<String, Object> extensionMap = user.getExtensionMap();
                if (null != extensionMap){
                    accountId = (String) extensionMap.get(ALI_USERID);
                }
            }
        });
        payBottomPup.setOnWChatClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAliName.setText("微信账户");
                imgPayIcon.setImageResource(R.mipmap.pay_bottompup_wchat);
                payType = "1";
                accountId = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME).getString(Constants.ALIPAY_USERINFO.OPENID);
            }
        });
    }

    private void showDialog() {
        final PayDialogView payDialogView = new PayDialogView(this);
        new XPopup.Builder(this)
                .dismissOnTouchOutside(false)
                .asCustom(payDialogView)
                .show();
        payDialogView.setOnClickListenerOnBack(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payDialogView.dismiss();
            }
        });
        payDialogView.setOnClickListenerOnForgetPwd(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //忘记密码
                payDialogView.dismiss();
                RetrievePayPwdActivity.start(CarryActivity.this,"1");
            }
        });
        payDialogView.setOnClickListenerOnRight(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //完成
                //提现
                carry(payDialogView);
                payDialogView.dismiss();
            }
        });
    }

    private void carry(PayDialogView payDialogView) {
        showProgress(this,false);
        UserApi.carry(money, payDialogView.getPayeePwd(), accountId, payType, CarryActivity.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    ToastUtil.showToast(CarryActivity.this,"提现成功");
                    etAmount.setText("");
                    finish();
                } else if (code == CODE_50021){
                    toast("微信提现失败,请更换其他提现方式");
                }else if (code == CODE_50022){
                    toast("支付宝提现失败，请更换其他提现方式");
                }else {
                    ToastUtil.showToast(CarryActivity.this, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(CarryActivity.this,errMessage);
            }
        });
    }

}

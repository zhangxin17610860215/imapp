package com.yqbj.ghxm.redpacket.wallet;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.utils.NoDoubleClickUtils;
import com.umeng.analytics.MobclickAgent;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.OrderNumberBean;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.pay.MyALipayUtils;
import com.yqbj.ghxm.requestutils.api.ApiUrl;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;
import com.yqbj.ghxm.utils.EventBusUtils;
import com.yqbj.ghxm.utils.NumberUtil;
import com.yqbj.ghxm.utils.SPUtils;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.wxapi.WXUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yqbj.ghxm.NimApplication.ALIPAY_APPID;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.ALIPAY_ISSHOW;
import static com.yqbj.ghxm.config.Constants.CONFIG_INFO.WCHATPAY_ISSHOW;
import static com.netease.yqbj.uikit.api.StatisticsConstants.RECHARGEQUERYSERVICEERROR;

/**
 * 充值页面
 */
public class RechargeActivity extends BaseAct implements View.OnClickListener {

    private TextView tvDetermine;
    private TextView tvAmount;
    private TextView tvAmount50;
    private TextView tvAmount100;
    private TextView tvAmount300;
    private TextView tvAmount500;
    private TextView tvAmount1000;
    private TextView tvAmount2000;
    private RelativeLayout rlWChatPay;
    private RelativeLayout rlALiPay;

    private Context context;
    private String amount = "";
    private String payType = "2";               //支付方式   默认微信支付
    private boolean isQuery = false;        //是否正在查询充值接口
    private ScheduledThreadPoolExecutor exec;
    private int peride = 5000;              //五秒轮询一次
    private int frequency = 0;              //轮询三次停止轮询
    private Dialog dialog;

    public static void start(Context context, String balance) {
        Intent intent = new Intent(context, RechargeActivity.class);
        intent.putExtra("balance",balance);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        context = this;
        EventBusUtils.register(this);
        initView();
    }

    private void initView() {
        setToolbar("充值");
        tvDetermine = findView(R.id.tv_recharge_Determine);
        tvAmount = findView(R.id.tv_recharge_Amount);
        tvAmount50 = findView(R.id.tv_amount_50);
        tvAmount100 = findView(R.id.tv_amount_100);
        tvAmount300 = findView(R.id.tv_amount_300);
        tvAmount500 = findView(R.id.tv_amount_500);
        tvAmount1000 = findView(R.id.tv_amount_1000);
        tvAmount2000 = findView(R.id.tv_amount_2000);
        rlWChatPay = findView(R.id.rl_wChat_Pay);
        rlALiPay = findView(R.id.rl_aLi_Pay);
        tvDetermine.setOnClickListener(this);
        tvAmount50.setOnClickListener(this);
        tvAmount100.setOnClickListener(this);
        tvAmount300.setOnClickListener(this);
        tvAmount500.setOnClickListener(this);
        tvAmount1000.setOnClickListener(this);
        tvAmount2000.setOnClickListener(this);
        rlWChatPay.setOnClickListener(this);
        rlALiPay.setOnClickListener(this);

        String balance = getIntent().getStringExtra("balance");
        if (StringUtil.isNotEmpty(balance)){
            tvAmount.setText(balance);
        }

        setAmount(tvAmount500,"500");
        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        String aLiPay = instance.getString(ALIPAY_ISSHOW);
        String wChatPay = instance.getString(WCHATPAY_ISSHOW);
        if (!aLiPay.equals("1")){
            //隐藏支付宝支付
            rlALiPay.setVisibility(View.GONE);
            rlWChatPay.setBackgroundResource(R.mipmap.pay_mode_selected);
            payType = "1";
        }
        if (!wChatPay.equals("1")){
            //隐藏微信支付
            rlWChatPay.setVisibility(View.GONE);
            rlALiPay.setBackgroundResource(R.mipmap.pay_mode_selected);
            payType = "2";
        }

        if (!aLiPay.equals("1") && !wChatPay.equals("1")){
            //两个都隐藏
            rlWChatPay.setVisibility(View.GONE);
            rlALiPay.setVisibility(View.GONE);
            payType = "";
        }
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()) {
                case R.id.tv_recharge_Determine:
                    //充值
                    showKeyboard(false);
                    if (StringUtil.isEmpty(amount)) {
                        ToastUtil.showToast(this, "请选择要充值的金额");
                        return;
                    }
                    if (NumberUtil.compareLess(amount, "50")) {
                        ToastUtil.showToast(this, "最少充值50元");
                        return;
                    }
                    if (NumberUtil.compareLess("2000", amount)) {
                        ToastUtil.showToast(this, "最大充值2000元");
                        return;
                    }
                    if (!payType.equals("1") && !payType.equals("2")){
                        ToastUtil.showToast(this, "暂不支持充值,请联系客服");
                        return;
                    }
                    getOrderNumber();
                    break;
                case R.id.tv_amount_50:
                    setAmount(tvAmount50,"50");
                    break;
                case R.id.tv_amount_100:
                    setAmount(tvAmount100,"100");
                    break;
                case R.id.tv_amount_300:
                    setAmount(tvAmount300,"300");
                    break;
                case R.id.tv_amount_500:
                    setAmount(tvAmount500,"500");
                    break;
                case R.id.tv_amount_1000:
                    setAmount(tvAmount1000,"1000");
                    break;
                case R.id.tv_amount_2000:
                    setAmount(tvAmount2000,"2000");
                    break;
                case R.id.rl_wChat_Pay:
                    payType = "1";
                    rlWChatPay.setBackgroundResource(R.mipmap.pay_mode_selected);
                    rlALiPay.setBackgroundResource(R.mipmap.pay_mode_unselected);
                    break;
                case R.id.rl_aLi_Pay:
                    payType = "2";
                    rlALiPay.setBackgroundResource(R.mipmap.pay_mode_selected);
                    rlWChatPay.setBackgroundResource(R.mipmap.pay_mode_unselected);
                    break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
        if (null != exec){
            exec.shutdown();
            exec = null;
        }
    }

    /**
     * 设置金额
     * */
    private void setAmount(TextView view, String amount) {
        this.amount = amount;
        tvAmount50.setBackgroundResource(R.mipmap.amount_text_bg);
        tvAmount100.setBackgroundResource(R.mipmap.amount_text_bg);
        tvAmount300.setBackgroundResource(R.mipmap.amount_text_bg);
        tvAmount500.setBackgroundResource(R.mipmap.amount_text_bg);
        tvAmount1000.setBackgroundResource(R.mipmap.amount_text_bg);
        tvAmount2000.setBackgroundResource(R.mipmap.amount_text_bg);
        view.setBackgroundResource(R.mipmap.amount_text_selected_bg);
    }

    /**
     * 获取权限使用的 RequestCode
     */
    private static final int PERMISSIONS_REQUEST_CODE = 1002;

    /**
     * 检查支付宝 SDK 所需的权限，并在必要的时候动态获取。
     * 在 targetSDK = 23 以上，READ_PHONE_STATE 和 WRITE_EXTERNAL_STORAGE 权限需要应用在运行时获取。
     * 如果接入支付宝 SDK 的应用 targetSdk 在 23 以下，可以省略这个步骤。
     * @param orderNO
     */
    private void requestPermission(String orderNO) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSIONS_REQUEST_CODE);

        } else {
            goPay(orderNO);
        }
    }

    /**
     * 权限获取回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {

                // 用户取消了权限弹窗
                if (grantResults.length == 0) {
//                    ToastUtil.showToast(this, getString(R.string.permission_rejected));
                    return;
                }

                // 用户拒绝了某些权限
                for (int x : grantResults) {
                    if (x == PackageManager.PERMISSION_DENIED) {
//                        showToast(this, getString(R.string.permission_rejected));
                        return;
                    }
                }

                // 所需的权限均正常获取
//                showToast(this, getString(R.string.permission_granted));
            }
        }
    }

    private void getOrderNumber() {
        showProgress(this, false);
        UserApi.getOrderNumber(this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    OrderNumberBean bean = (OrderNumberBean) object;
                    String orderNO = bean.getOrderNO();
                    if (payType.equals("2")){
                        //支付宝支付
                        requestPermission(orderNO);
                    }else if (payType.equals("1")){
                        //微信支付
                        WXUtil.weiChatPay("3",amount,orderNO,null,null,null,null,null,null,null, context, new WXUtil.WeiChatPayCallBack() {
                            @Override
                            public void onSuccess(int code, Object object) {
                                if (code == Constants.SUCCESS_CODE){

                                }
                            }

                            @Override
                            public void onFailed(String errMessage) {

                            }
                        });

                    }

                } else {
                    ToastUtil.showToast(RechargeActivity.this, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(RechargeActivity.this, errMessage);
            }
        });
    }

    private void goPay(final String orderNO) {
        final MyALipayUtils.ALiPayBuilder builder = new MyALipayUtils.ALiPayBuilder();
        final MyALipayUtils myALipayUtils = builder.setAppid(ALIPAY_APPID)
                .setMoney(amount)       //设置金额
                .setTitle("华唯环球科技有限公司")     //设置商品信息
                .setBody("华唯环球科技有限公司")       //设置商品信息描述
                .setOrderTradeId(orderNO)   //设置订单ID
                .setNotifyUrl(ApiUrl.BASE_URL_HEAD + ApiUrl.BASE_URL + "/notify/alipay") //服务器异步通知页面路径
                .build();

        myALipayUtils.goAliPay("3",2,orderNO,amount,null,null,null,null,null,null,this, new MyALipayUtils.AlipayListener() {
            @Override
            public void onPaySuccess() {
                showDialog();
            }

            @Override
            public void onPayFailed() {

            }
        });
    }

    /**
     * 微信支付结果回调
     * */
    @Subscribe
    public void getWXPayData(EventBusUtils.CommonEvent commonEvent){
        if (null == commonEvent) {
            return;
        }
        if (commonEvent.id != 100){
            return;
        }
        if (null == commonEvent.data){
            dismissProgress();
            return;
        }
        Bundle bundle = commonEvent.data;
        String result = (String) bundle.get("payResult");
        if (StringUtil.isNotEmpty(result)){
            if (result.equals("0")){
                //支付成功
                showDialog();
            }
        }
    }

    private void showDialog() {
        dialog = EasyAlertDialogHelper.showOneButtonDiolag(context, null, "金额将在两小时内到账,请随时关注您的账户余额", "确定", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    /**
     * 轮询充值查询接口
     * */
    private void polling(final String orderNO) {
        if (null != exec){
            exec.shutdown();
            exec = null;
        }
//        if (isQuery){
//            showProgress(context,false);
//        }
        exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!isQuery){
                    frequency ++;
                    if (frequency > 6){
                        frequency = 0;
                        exec.shutdown();
                        exec = null;
                        dismissProgress();
                    }else {
                        isQuery = true;
                        goRechArge(orderNO,payType);
                    }
                }
            }
        },0, peride, TimeUnit.MILLISECONDS);
    }

    private void goRechArge(String orderNO, String payType) {
        UserApi.rechArgeQuery(orderNO, payType, RechargeActivity.this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE) {
                    dismissProgress();
                    isQuery = true;
                    finish();
                } else {
                    isQuery = false;
                    if (frequency >= 6){
                        ToastUtil.showToast(context, "充值查询失败，请关注零钱余额");
                        MobclickAgent.onEvent(context,RECHARGEQUERYSERVICEERROR);
                    }
                }
            }

            @Override
            public void onFailed(String errMessage) {
                isQuery = false;
                ToastUtil.showToast(RechargeActivity.this, errMessage);
            }
        });
    }
}

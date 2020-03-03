package com.yqbj.ghxm.redpacket.privateredpacket;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.lxj.xpopup.XPopup;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.AmountBean;
import com.yqbj.ghxm.bean.OrderNumberBean;
import com.yqbj.ghxm.common.ui.BaseAct;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.pay.MyALipayUtils;
import com.yqbj.ghxm.user.RetrievePayPwdActivity;
import com.yqbj.ghxm.utils.EventBusUtils;
import com.yqbj.ghxm.utils.NumberUtil;
import com.yqbj.ghxm.utils.PaySelect;
import com.yqbj.ghxm.utils.RedPacketTextWatcher;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.view.PayDialogView;
import com.yqbj.ghxm.wxapi.WXUtil;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.yqbj.uikit.utils.NoDoubleClickUtils;
import com.umeng.analytics.MobclickAgent;
import com.yqbj.ghxm.requestutils.api.ApiUrl;
import com.yqbj.ghxm.requestutils.api.UserApi;
import com.yqbj.ghxm.requestutils.requestCallback;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yqbj.ghxm.NimApplication.ALIPAY_APPID;
import static com.yqbj.ghxm.config.Constants.WXPAY_ORDERID;
import static com.netease.yqbj.uikit.api.StatisticsConstants.PERSONAL_RP_SEND_ERROR_ALIPAYERRORNUM;
import static com.netease.yqbj.uikit.api.StatisticsConstants.PERSONAL_RP_SEND_ERROR_CODEERRORNUM;
import static com.netease.yqbj.uikit.api.StatisticsConstants.PERSONAL_RP_SEND_ERROR_PASSWORDERRORNUM;
import static com.netease.yqbj.uikit.api.StatisticsConstants.PERSONAL_RP_SEND_TOTALNUM;

public class P2PRedPackActivity extends BaseAct implements View.OnClickListener {

    private Context context;
    private EditText etMoney;
    private EditText etContent;
    private TextView tvTotalSum;
    private TextView tvSenderRp;

    private String money;
    private String targetAccount;
    private PayDialogView payDialogView;
    private boolean isWXPay = false;
    private Dialog dialog;
    private boolean isQuery = false;        //是否正在查询充值接口
    private ScheduledThreadPoolExecutor exec;
    private int peride = 5000;              //五秒轮询一次
    private int frequency = 0;              //轮询三次停止轮询

    public static void start(Context context,String targetAccount) {
        Intent intent = new Intent(context, P2PRedPackActivity.class);
        intent.putExtra("targetAccount",targetAccount);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_p2predpack);
        EventBusUtils.register(this);
        setToolbar(R.drawable.jrmf_b_top_back, "发红包",R.color.redpacket_theme);
        context = P2PRedPackActivity.this;
        targetAccount = getIntent().getStringExtra("targetAccount");
        initView();
        initData();
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
    private void initView() {
        etMoney = (EditText) findViewById(R.id.et_p2predpack_money);
        etContent = (EditText) findViewById(R.id.et_p2predpack_content);
        tvTotalSum = (TextView) findViewById(R.id.tv_p2predpack_moneyNum);
        tvSenderRp = (TextView) findViewById(R.id.tv_p2predpack_sendRedPacket);

        tvSenderRp.setOnClickListener(this);
    }

    private void initData() {
        etMoney.addTextChangedListener(new RedPacketTextWatcher(etMoney));
        etMoney.addTextChangedListener(new TextWatcher() {
            private int editStart ;
            private int editEnd ;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                money = etMoney.getText().toString();
                editStart = etMoney.getSelectionStart();
                editEnd = etMoney.getSelectionEnd();
                if (StringUtil.isNotEmpty(money) && NumberUtil.compareLess("200",money)){
                    toast("单次红包金额不得大于200");
                    s.delete(editStart-1, editEnd);
                    int tempSelection = editStart;
                    etMoney.setText(s);
                    etMoney.setSelection(tempSelection);
                }
                if (StringUtil.isEmpty(money)){
                    money = "0.00";
                }
                tvTotalSum.setText("￥ " + money);
                etContent.setText("￥ " + money);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()){
                case R.id.tv_p2predpack_sendRedPacket:
                    //发送红包
                    check(v);
                    break;
            }
        }

    }

    private void check(View v) {
        if (StringUtil.isEmpty(money)){
            toast("请输入红包金额");
            return;
        }
        if (NumberUtil.compareLess(money,"0.01")){
            toast("单个红包金额不得少于0.01");
            return;
        }
        if (NumberUtil.compareLess("200",money)){
            toast("单次红包金额不得大于200");
            return;
        }
        MobclickAgent.onEvent(context,PERSONAL_RP_SEND_TOTALNUM);
        //检验用户是否创建了钱包&&零钱包余额是否足够红包总金额
        checkUserBalance(v);
    }

    private void checkUserBalance(final View v) {
        showProgress(context,false);
        UserApi.getAmount(this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    AmountBean amountBean = (AmountBean) object;
                    String amount = amountBean.getAmount();
                    showPayMode(amount,v);
                }else {
                    toast((String) object);
                    statisticsSendRPError("getBalance",code);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(context,errMessage);
            }
        });
    }

    private void statisticsSendRPError(String key, int code) {
        Map<String,String> map = new HashMap<>();
        map.put(key,code + "");
        MobclickAgent.onEvent(context,PERSONAL_RP_SEND_ERROR_CODEERRORNUM,map);
    }

    /**
     * 微信支付结果回调
     * */
    @Subscribe
    public void getWXPayData(EventBusUtils.CommonEvent commonEvent){
        if (null == commonEvent) {
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
                DialogMaker.showProgressDialog(context, "红包发送中,请勿关闭页面", false);
                polling(WXPAY_ORDERID,"1");
            }
        }
        if (null != dialog){
            dialog.dismiss();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isWXPay){
            showWXPayDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isWXPay = false;
    }

    /**
     * 显示支付方式Dialog
     * */
    private void showPayMode(final String amount, View v) {
        final PaySelect paySelect = new PaySelect(context,money,"红包",amount,1);
        new XPopup.Builder(context)
                .atView(v)
                .asCustom(paySelect)
                .show();
        paySelect.setOnClickListenerOnSure(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //立即支付
                showPayPsdDialog(money,paySelect);
                paySelect.dismiss();
            }
        });
    }

    /**
     * 输入支付密码
     *
     * @param amount
     * @param paySelect
     */
    private void showPayPsdDialog(final String amount, final PaySelect paySelect) {
        payDialogView = new PayDialogView(this);
        new XPopup.Builder(this)
                .dismissOnTouchOutside(false)
                .asCustom(payDialogView)
                .show();
        payDialogView.setOnClickListenerOnBack(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(context,PERSONAL_RP_SEND_ERROR_PASSWORDERRORNUM);
                payDialogView.dismiss();
            }
        });
        payDialogView.setOnClickListenerOnForgetPwd(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //忘记密码
                MobclickAgent.onEvent(context,PERSONAL_RP_SEND_ERROR_PASSWORDERRORNUM);
                payDialogView.dismiss();
                RetrievePayPwdActivity.start(context,"1");
            }
        });

        payDialogView.setOnClickListenerOnRight(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //支付密码校验完成
                PaySelect.SelectPayType type = paySelect.getCurrSeletPayType();
                int payType = 1;
                switch (type) {
                    case ALI:
                        isWXPay = false;
                        payType = 3;
                        break;
                    case WCHAT:
                        isWXPay = true;
                        payType = 2;
                        break;
                    case WALLET:
                        isWXPay = false;
                        payType = 1;
                        break;
                }
                if (!NoDoubleClickUtils.isDoubleClick(2000)){
                    getRedPageId(amount,payType);
                    payDialogView.dismiss();
                }


            }
        });
    }
    private void showWXPayDialog() {
        dialog = EasyAlertDialogHelper.showOneButtonDiolag(context, null, "您的订单尚未完成，请继续。", "继续", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogMaker.showProgressDialog(context, "红包发送中,请勿关闭页面", false);
                polling(WXPAY_ORDERID,"1");
            }
        });
        dialog.show();
    }

    private void goAliPay(final String orderNO, String amount) {
        final MyALipayUtils.ALiPayBuilder builder = new MyALipayUtils.ALiPayBuilder();
        MyALipayUtils myALipayUtils = builder.setAppid(ALIPAY_APPID)
                .setMoney(amount)       //设置金额
                .setTitle("华唯环球科技有限公司")     //设置商品信息
                .setBody("华唯环球科技有限公司")       //设置商品信息描述
                .setOrderTradeId(orderNO)   //设置订单ID
                .setNotifyUrl(ApiUrl.BASE_URL_HEAD + ApiUrl.BASE_URL + "/notify/alipay") //服务器异步通知页面路径
                .build();

        String rid = "";
        if (StringUtil.isNotEmpty(orderNO)){
            rid = orderNO;
        }
        String number = "1";
        String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "￥ " + money :etContent.getText().toString();

        myALipayUtils.goAliPay("1",2, rid, money, targetAccount, Constants.REDPACK_TYPE.P2P + "", redContent, payDialogView.getPayeePwd(), number, targetAccount,this, new MyALipayUtils.AlipayListener() {
            @Override
            public void onPaySuccess() {
//                DialogMaker.showProgressDialog(context, "红包发送中,请勿关闭页面", false);
//                polling(orderNO,"2");
                finish();
            }

            @Override
            public void onPayFailed() {
                MobclickAgent.onEvent(context,PERSONAL_RP_SEND_ERROR_ALIPAYERRORNUM);
                finish();
            }
        });

    }

    /**
     * 轮询充值查询接口
     * */
    private void polling(final String orderNO, final String payType) {
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

    /**
     * 充值查询
     * */
    private void goRechArge(final String orderNO, String payType) {
        UserApi.rechArgeQuery(orderNO, payType, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE){
                    dismissProgress();
                    isQuery = true;
                    setRedPackData(orderNO, payDialogView.getPayeePwd());
                }else {
                    isQuery = false;
                    if (frequency >= 6){
                        ToastUtil.showToast(context, "红包发送失败，请关注零钱余额");
                        statisticsSendRPError("rechArgeQuery",code);
                    }
                }
            }

            @Override
            public void onFailed(String errMessage) {
                isQuery = false;
                ToastUtil.showToast(context,errMessage);
            }
        });
    }
    /**
     * 获取红包Id
     * */
    private void getRedPageId(final String amount, final int payType) {
        showProgress(context,false);
        UserApi.getOrderNumber(this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    OrderNumberBean bean = (OrderNumberBean) object;
                    if (null != bean) {
                        switch (payType){
                            case 1:
                                //钱包余额
                                setRedPackData(bean.getOrderNO(), payDialogView.getPayeePwd());
                                break;
                            case 2:
                                //微信
                                String rid = "";
                                if (StringUtil.isNotEmpty(bean.getOrderNO())){
                                    rid = bean.getOrderNO();
                                }
                                String number = "1";
                                String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "￥ " + money :etContent.getText().toString();
                                WXUtil.weiChatPay("1",amount,rid, money, targetAccount, Constants.REDPACK_TYPE.P2P + "", redContent, payDialogView.getPayeePwd(), number, targetAccount, context, new WXUtil.WeiChatPayCallBack() {
                                    @Override
                                    public void onSuccess(int code, Object object) {
//                                        if (code == Constants.SUCCESS_CODE){
//                                            if (isWXPay){
//                                                showWXPayDialog();
//                                            }
//                                        }
                                        finish();
                                    }

                                    @Override
                                    public void onFailed(String errMessage) {
                                        finish();
                                    }
                                });
                                break;
                            case 3:
                                //支付宝
                                goAliPay(bean.getOrderNO(),amount);
                                break;
                        }

                    }
                } else {
                    ToastUtil.showToast(context, (String) object);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                ToastUtil.showToast(context, errMessage);
            }
        });
    }
    /**
     * 设置红包数据
     * */
    private void setRedPackData(final String redPacketId, String payeePwd) {
        String rid = "";
        if (StringUtil.isNotEmpty(redPacketId)){
            rid = redPacketId;
        }
        String number = "1";
        String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "￥ " + money :etContent.getText().toString();
        showProgress(context,false);
        UserApi.sendRedPack(rid, money, targetAccount, Constants.REDPACK_TYPE.P2P + "", redContent, payeePwd, number, targetAccount, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE){
                    //发送红包
                    pushSendRedPackMessage(redPacketId);
                }else {
                    toast((String) object);
                    statisticsSendRPError("sendRedPacket",code);
                }
            }

            @Override
            public void onFailed(String errMessage) {
                dismissProgress();
                toast(errMessage);
            }
        });
    }

    /**
     * 推送一条红包消息
     * */
    private void pushSendRedPackMessage(String redPacketId) {
        if (StringUtil.isEmpty(redPacketId)){
            return;
        }
        String redId = redPacketId;
        String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "￥ " + money : etContent.getText().toString();

        //构建红包结构Map
        BuildRedPackStructure structure = new BuildRedPackStructure();
        structure.build((Activity) context,Constants.REDPACK_TYPE.P2P,false,redId, redContent,
                1,"",money,targetAccount);
    }
}

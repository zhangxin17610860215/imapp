package com.wulewan.ghxm.redpacket.privateredpacket;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrmf360.normallib.base.utils.ToastUtil;
import com.lxj.xpopup.XPopup;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.wulewan.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.wulewan.uikit.common.ui.dialog.DialogMaker;
import com.netease.wulewan.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.wulewan.uikit.utils.NoDoubleClickUtils;
import com.umeng.analytics.MobclickAgent;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.AmountBean;
import com.wulewan.ghxm.bean.OrderNumberBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.main.activity.RedPacketContactSelectAct;
import com.wulewan.ghxm.pay.MyALipayUtils;
import com.wulewan.ghxm.requestutils.api.ApiUrl;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.user.RetrievePayPwdActivity;
import com.wulewan.ghxm.utils.EventBusUtils;
import com.wulewan.ghxm.utils.NumberUtil;
import com.wulewan.ghxm.utils.PaySelect;
import com.wulewan.ghxm.utils.RedPacketTextWatcher;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.utils.view.PayDialogView;
import com.wulewan.ghxm.wxapi.WXUtil;

import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.wulewan.ghxm.NimApplication.ALIPAY_APPID;
import static com.wulewan.ghxm.config.Constants.WXPAY_ORDERID;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_AVERAGE_RP_SEND_ERROR_ALIPAYERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_AVERAGE_RP_SEND_ERROR_CODEERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_AVERAGE_RP_SEND_ERROR_PASSWORDERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_AVERAGE_RP_SEND_TOTALNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_EXCLUSIVE_RP_SEND_ERROR_ALIPAYERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_EXCLUSIVE_RP_SEND_ERROR_CODEERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_EXCLUSIVE_RP_SEND_ERROR_PASSWORDERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_EXCLUSIVE_RP_SEND_TOTALNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_RANDOM_RP_SEND_ERROR_ALIPAYERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_RANDOM_RP_SEND_ERROR_CODEERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_RANDOM_RP_SEND_ERROR_PASSWORDERRORNUM;
import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_RANDOM_RP_SEND_TOTALNUM;
import static com.netease.wulewan.uikit.business.contact.selector.activity.ContactSelectActivity.RESULT_DATA;
import static com.netease.wulewan.uikit.business.contact.selector.activity.ContactSelectActivity.RESULT_NAME;

/**
 * 群红包
 */
public class GroupRedPacketActivity extends BaseAct implements View.OnClickListener {

    public static final String TAG = GroupRedPacketActivity.class.getSimpleName();

    private Context context;
    private TextView tvMoneyType;           //红包类型  （总金额or单个金额）
    private TextView tvModifyRedType;       //修改红包类型  (改为普通红包or改为拼手气红包)
    private TextView tvRedPacketType;       //红包类型  (当前为拼手气红包or当前为普通红包)
    private TextView tvWho;                 //谁可以领
    private TextView tvPeopleNum;           //群内总共人数
    private TextView tvMoneyNum;            //总共钱数
    private TextView tvSendRedPacket;       //发送红包
    private TextView tvRedPacketNum;        //红包个数
    private String redPacketNum = "";       //红包个数

    private EditText etMoney;               //输入钱数
    private EditText etContent;             //输入红包内容
    private EditText etRedPacketNum;        //输入红包个数

    private int REDPACKET_TYPE;             //红包类型
    private boolean ISEXCLUSIVE = false;    //是否是专属红包

    private String zhiDingPeopleNum = "";   //指定人数
    private String mTeamId = "";
    private String totalSum = "";           //总金额
    private String inputAmount = "";        //输入金额
    private String targetIds = "";          //多个用户id总字符串
    private int count;                      //群内总人数
    private PayDialogView payDialogView;
    private boolean isWXPay = false;
    private Dialog dialog;
    private boolean isQuery = false;        //是否正在查询充值接口
    private ScheduledThreadPoolExecutor exec;
    private int peride = 5000;              //五秒轮询一次
    private int frequency = 0;              //轮询三次停止轮询

    private boolean isGroupOwner = false;                           //是否是群主

    public static void start(Context context) {
        Intent intent = new Intent(context, GroupRedPacketActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_group_redpacket);
        context = GroupRedPacketActivity.this;
        EventBusUtils.register(this);
        REDPACKET_TYPE = Constants.REDPACK_TYPE.TEAM_RANDOM;
        ISEXCLUSIVE = false;
        mTeamId = getIntent().getStringExtra("teamId");
        Team team = NimUIKit.getTeamProvider().getTeamById(mTeamId);
        if (team != null) {
            if (team.getCreator().equals(NimUIKit.getAccount())) {
                isGroupOwner = true;
            }else {
                isGroupOwner = false;
            }
        }
        setToolbar(R.drawable.jrmf_b_top_back, "发红包", R.color.redpacket_theme);
        initView();
        modifyRedType();
        initDate();
//        showKeyboard(true);
    }

    private void initView() {

        tvMoneyType = findView(R.id.tv_groupRed_moneyType);
        tvModifyRedType = findView(R.id.tv_groupRed_modifyRedType);
        tvRedPacketType = findView(R.id.tv_groupRed_redType);
        tvWho = findView(R.id.tv_groupRed_who);
        tvPeopleNum = findView(R.id.tv_groupRed_peopleNum);
        tvMoneyNum = findView(R.id.tv_groupRed_moneyNum);
        tvSendRedPacket = findView(R.id.tv_groupRed_sendRedPacket);
        tvRedPacketNum = findView(R.id.tv_groupRed_redPacketNum);

        LinearLayout llWhoGet = findView(R.id.ll_who_get);

        etMoney = findView(R.id.et_groupRed_money);
        etRedPacketNum = findView(R.id.et_groupRed_redPacketNum);
        etContent = findView(R.id.et_groupRed_content);

        etMoney.setFocusable(true);
        etRedPacketNum.setFocusable(false);
        etContent.setFocusable(false);

        llWhoGet.setOnClickListener(this);

        etMoney.setOnClickListener(this);
        etRedPacketNum.setOnClickListener(this);
        etContent.setOnClickListener(this);

        tvModifyRedType.setOnClickListener(this);
//        tvWho.setOnClickListener(this);
        tvSendRedPacket.setOnClickListener(this);
    }

    private void initDate() {
        Team team = NimUIKit.getTeamProvider().getTeamById(mTeamId);
        count = team == null ? 0 : team.getMemberCount();
        tvPeopleNum.setText("群内人数共" + count + "人");

        etMoney.addTextChangedListener(new RedPacketTextWatcher(etMoney));
        etMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                redPacketNum = tvRedPacketNum.getText().toString();
                inputAmount = etMoney.getText().toString();
                switch (REDPACKET_TYPE) {
                    case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                        if (ISEXCLUSIVE) {
                            //专属红包
                            if (StringUtil.isNotEmpty(inputAmount) && new BigDecimal(inputAmount).compareTo(new BigDecimal("0")) > 0) {
                                if (StringUtil.isNotEmpty(redPacketNum) && new BigDecimal(redPacketNum).compareTo(new BigDecimal("0")) > 0) {
                                    double mul = NumberUtil.mul(inputAmount, redPacketNum);
                                    totalSum = mul + "";
                                } else {
                                    totalSum = "0.00";
                                }
                            } else {
                                totalSum = "0.00";
                            }
                        } else {
                            //普通红包
                            if (StringUtil.isNotEmpty(inputAmount) && new BigDecimal(inputAmount).compareTo(new BigDecimal("0")) > 0) {
                                if (StringUtil.isNotEmpty(redPacketNum) && new BigDecimal(redPacketNum).compareTo(new BigDecimal("0")) > 0) {
                                    double mul = NumberUtil.mul(inputAmount, redPacketNum);
                                    totalSum = mul + "";
                                } else {
                                    totalSum = "0.00";
                                }
                            } else {
                                totalSum = "0.00";
                            }
                        }
                        break;
                    case Constants.REDPACK_TYPE.TEAM_RANDOM:
                        //随机红包
                        if (StringUtil.isNotEmpty(inputAmount) && new BigDecimal(inputAmount).compareTo(new BigDecimal("0")) > 0) {
                            totalSum = inputAmount;
                        } else {
                            totalSum = "0.00";
                        }
                        break;
                }
                tvMoneyNum.setText("￥ " + totalSum);
                if (NumberUtil.compareLess("0",totalSum)){
                    etContent.setText(tvMoneyNum.getText().toString());
                }else {
                    etContent.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etRedPacketNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                redPacketNum = etRedPacketNum.getText().toString();
                tvRedPacketNum.setText(redPacketNum);
                inputAmount = etMoney.getText().toString();
                switch (REDPACKET_TYPE) {
                    case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                        if (ISEXCLUSIVE) {
                            //专属红包
                            if (StringUtil.isNotEmpty(inputAmount) && new BigDecimal(inputAmount).compareTo(new BigDecimal("0")) > 0) {
                                double mul = NumberUtil.mul(inputAmount, redPacketNum);
                                totalSum = mul + "";
                            }
                        } else {
                            //普通红包
                            if (StringUtil.isNotEmpty(inputAmount) && new BigDecimal(inputAmount).compareTo(new BigDecimal("0")) > 0) {
                                double mul = NumberUtil.mul(inputAmount, redPacketNum);
                                totalSum = mul + "";
                            }
                        }
                        break;
                    case Constants.REDPACK_TYPE.TEAM_RANDOM:
                        //随机红包
                        if (StringUtil.isNotEmpty(redPacketNum)) {
                            if (StringUtil.isNotEmpty(inputAmount) && new BigDecimal(inputAmount).compareTo(new BigDecimal("0")) > 0) {
                                totalSum = inputAmount;
                            } else {
                                totalSum = "0.00";
                            }
                        } else {
                            totalSum = "0.00";
                        }
                        break;
                }
                tvMoneyNum.setText("￥ " + totalSum);
                if (NumberUtil.compareLess("0",totalSum)){
                    etContent.setText(tvMoneyNum.getText().toString());
                }else {
                    etContent.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtils.isDoubleClick(500)){
            switch (v.getId()) {
                case R.id.tv_groupRed_modifyRedType:
                    //修改红包类型
                    if (REDPACKET_TYPE == Constants.REDPACK_TYPE.TEAM_RANDOM) {
                        REDPACKET_TYPE = Constants.REDPACK_TYPE.TEAM_ORDINARY;
                    } else {
                        REDPACKET_TYPE = Constants.REDPACK_TYPE.TEAM_RANDOM;
                    }
                    modifyRedType();
                    break;
                case R.id.ll_who_get:
                    showKeyboard(false);
                    //谁可以领
                    ContactSelectActivity.Option option = new ContactSelectActivity.Option();
                    option.maxSelectNum = 5;
                    option.allowSelectEmpty = true;
                    option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
                    option.title = "选择专属成员";
                    option.teamId = mTeamId;

                    ArrayList<String> disableAccounts = new ArrayList<>();
                    disableAccounts.add(NimUIKit.getAccount());
                    option.itemDisableFilter = new ContactIdFilter(disableAccounts);

                    option.withName = true;



                    RedPacketContactSelectAct.startActivityForResult(context, option, 100);

                    break;
                case R.id.tv_groupRed_sendRedPacket:
                    //校验
                    check(v);
                    break;
                case R.id.et_groupRed_money:
                    setEditFocus(etMoney, true);
                    setEditFocus(etRedPacketNum, false);
                    setEditFocus(etContent, false);
                    break;
                case R.id.et_groupRed_redPacketNum:
                    setEditFocus(etRedPacketNum, true);
                    setEditFocus(etMoney, false);
                    setEditFocus(etContent, false);
                    break;
                case R.id.et_groupRed_content:
                    setEditFocus(etContent, true);
                    setEditFocus(etMoney, false);
                    setEditFocus(etRedPacketNum, false);
                    break;
            }
        }
    }


    private void setEditFocus(EditText editText, boolean isFocus) {
        editText.setFocusable(isFocus);
        editText.setFocusableInTouchMode(isFocus);
    }

    private void check(View v) {
        switch (REDPACKET_TYPE) {
            case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                if (ISEXCLUSIVE) {
                    //专属红包校验
                    if (new BigDecimal(zhiDingPeopleNum).compareTo(new BigDecimal("0")) <= 0) {
                        toast("请选择可以领取红包的成员");
                        return;
                    }
                    if (new BigDecimal(zhiDingPeopleNum).compareTo(new BigDecimal("5")) > 0) {
                        toast("专属红包最多可指定5人");
                        return;
                    }
                    if (StringUtil.isEmpty(etMoney.getText().toString())) {
                        toast("请输入红包金额");
                        return;
                    }
                    try {
                        //总金额除以红包=红包的平均值
                        String str = NumberUtil.div_Intercept(totalSum, redPacketNum, 3);
                        if (NumberUtil.compareLess(str, "0.01")) {
                            toast("单个红包金额不得少于0.01");
                            return;
                        }
                        if (NumberUtil.compareLess("200", inputAmount)) {
                            toast("单个红包金额不得大于200");
                            return;
                        }
                        if (NumberUtil.compareLess("20000", totalSum)) {
                            toast("总金额单次不可大于20000");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    MobclickAgent.onEvent(context,TEAM_EXCLUSIVE_RP_SEND_TOTALNUM);
                } else {
                    //普通红包校验
                    if (isGroupOwner){
                        if (StringUtil.isEmpty(etRedPacketNum.getText().toString())) {
                            toast("请输入红包个数");
                            return;
                        }
                        if (NumberUtil.compareLess(etRedPacketNum.getText().toString(), "1")) {
                            toast("红包个数不得少于一个");
                            return;
                        }
                        if (NumberUtil.compareLess("50",etRedPacketNum.getText().toString())) {
                            toast("红包个数不得大于50个");
                            return;
                        }
                    }
                    if (StringUtil.isEmpty(etMoney.getText().toString())) {
                        toast("请输入红包金额");
                        return;
                    }
                    try {
                        //总金额除以红包=红包的平均值
                        String str = NumberUtil.div_Intercept(totalSum, redPacketNum, 3);
                        if (NumberUtil.compareLess(str, "0.01")) {
                            toast("单个红包金额不得少于0.01");
                            return;
                        }
                        if (NumberUtil.compareLess("200", inputAmount)) {
                            toast("单个红包金额不得大于200");
                            return;
                        }
                        if (NumberUtil.compareLess("20000", totalSum)) {
                            toast("总金额单次不可大于20000");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MobclickAgent.onEvent(context,TEAM_AVERAGE_RP_SEND_TOTALNUM);
                }
                break;
            case Constants.REDPACK_TYPE.TEAM_RANDOM:
                //随机红包的校验
                if (isGroupOwner){
                    if (StringUtil.isEmpty(etRedPacketNum.getText().toString())) {
                        toast("请输入红包个数");
                        return;
                    }
                    if (NumberUtil.compareLess(etRedPacketNum.getText().toString(), "1")) {
                        toast("红包个数不得少于一个");
                        return;
                    }
                    if (NumberUtil.compareLess("50",etRedPacketNum.getText().toString())) {
                        toast("红包个数不得大于50个");
                        return;
                    }
                }
                if (StringUtil.isEmpty(etMoney.getText().toString())) {
                    toast("请输入红包金额");
                    return;
                }
                try {
                    //总金额除以红包=红包的平均值
                    String str = NumberUtil.div_Intercept(totalSum, redPacketNum, 3);
                    if (NumberUtil.compareLess(str, "0.01")) {
                        toast("单个红包金额不得少于0.01");
                        return;
                    }
                    if (NumberUtil.compareLess("200", totalSum)) {
                        toast("单个红包金额不得大于200");
                        return;
                    }
                    if (NumberUtil.compareLess("20000", totalSum)) {
                        toast("总金额单次不可大于20000");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MobclickAgent.onEvent(context,TEAM_RANDOM_RP_SEND_TOTALNUM);
                break;
        }
        //检验用户是否创建了钱包&&零钱包余额是否足够红包总金额
        checkUserBalance(v);
    }

    /**
     * 统计各个红包由于服务器状态码出现错误的次数
     * */

    private void statisticsSendRPError(String key, int code){
        Map<String,String> map = new HashMap<>();
        map.put(key,code + "");
        switch (REDPACKET_TYPE) {
            case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                if (ISEXCLUSIVE) {
                    //专属红包
                    MobclickAgent.onEvent(context,TEAM_EXCLUSIVE_RP_SEND_ERROR_CODEERRORNUM,map);
                } else {
                    //普通红包
                    MobclickAgent.onEvent(context,TEAM_AVERAGE_RP_SEND_ERROR_CODEERRORNUM,map);
                }
                break;
            case Constants.REDPACK_TYPE.TEAM_RANDOM:
                //随机红包
                MobclickAgent.onEvent(context,TEAM_RANDOM_RP_SEND_ERROR_CODEERRORNUM,map);
                break;
        }
    }

    private void checkUserBalance(final View v) {
        showProgress(context, false);
        UserApi.getAmount(this, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    AmountBean amountBean = (AmountBean) object;
                    String amount = amountBean.getAmount();
                    showPayMode(amount, v);
                } else {
                    toast((String) object);
                    statisticsSendRPError("getBalance",code);
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
     * 显示支付方式Dialog
     */
    private void showPayMode(final String amount, View v) {
        final PaySelect paySelect = new PaySelect(context, totalSum, "红包", amount, 1);
        new XPopup.Builder(context)
                .atView(v)
                .asCustom(paySelect)
                .show();
        paySelect.setOnClickListenerOnSure(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //立即支付
                showPayPsdDialog(totalSum, paySelect);
                paySelect.dismiss();
            }
        });
    }

//    private void getOrderNu(final String amount) {
//        showProgress(context, false);
//        UserApi.getOrderNumber(this, new requestCallback() {
//            @Override
//            public void onSuccess(int code, Object object) {
//                dismissProgress();
//                if (code == Constants.SUCCESS_CODE) {
//                    OrderNumberBean bean = (OrderNumberBean) object;
//                    String orderNO = bean.getOrderNO();
//                    goAliPay(orderNO, amount);
//                } else {
//                    ToastUtil.showToast(context, (String) object);
//                    statisticsSendRPError("getOrderId",code);
//                }
//            }
//
//            @Override
//            public void onFailed(String errMessage) {
//                dismissProgress();
//                ToastUtil.showToast(context, errMessage);
//            }
//        });
//    }

    private void goAliPay(final String orderNO, String amount) {
        final MyALipayUtils.ALiPayBuilder builder = new MyALipayUtils.ALiPayBuilder();
        MyALipayUtils myALipayUtils = builder.setAppid(ALIPAY_APPID)
                .setMoney(amount)       //设置金额
                .setTitle("杭州吾乐玩网络科技有限公司")     //设置商品信息
                .setBody("杭州吾乐玩网络科技有限公司")       //设置商品信息描述
                .setOrderTradeId(orderNO)   //设置订单ID
                .setNotifyUrl(ApiUrl.BASE_URL_HEAD + ApiUrl.BASE_URL + "/notify/alipay") //服务器异步通知页面路径
                .build();

        String rid = "";
        if (StringUtil.isNotEmpty(orderNO)) {
            rid = orderNO;
        }
        String targetType = REDPACKET_TYPE + "";
        String number = "";
        String money = "";
        switch (REDPACKET_TYPE) {
            case Constants.REDPACK_TYPE.TEAM_RANDOM:
                targetIds = "";
                number = redPacketNum;
                money = totalSum;
                break;
            case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                if (!ISEXCLUSIVE) {
                    targetIds = "";
                    number = redPacketNum;
                    money = etMoney.getText().toString();
                } else {
//                    number = zhiDingPeopleNum;
                    number = redPacketNum;
                    money = etMoney.getText().toString();
                }
                break;
        }
        String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "恭喜发财，大吉大利！" : etContent.getText().toString();
        myALipayUtils.goAliPay("1",2,rid,money,targetIds,targetType,redContent,payDialogView.getPayeePwd(),number,mTeamId,this, new MyALipayUtils.AlipayListener() {
            @Override
            public void onPaySuccess() {
//                DialogMaker.showProgressDialog(context, "红包发送中,请勿关闭页面", false);
//                polling(orderNO,"2");
                finish();
            }

            @Override
            public void onPayFailed() {
                switch (REDPACKET_TYPE) {
                    case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                        if (ISEXCLUSIVE) {
                            //专属红包
                            MobclickAgent.onEvent(context,TEAM_EXCLUSIVE_RP_SEND_ERROR_ALIPAYERRORNUM);
                        } else {
                            //普通红包
                            MobclickAgent.onEvent(context,TEAM_AVERAGE_RP_SEND_ERROR_ALIPAYERRORNUM);
                        }
                        break;
                    case Constants.REDPACK_TYPE.TEAM_RANDOM:
                        //随机红包
                        MobclickAgent.onEvent(context,TEAM_RANDOM_RP_SEND_ERROR_ALIPAYERRORNUM);
                        break;
                }
                finish();
            }
        });

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
     * 充值查询
     */
    private void goRechArge(final String orderNO, String payType) {
        UserApi.rechArgeQuery(orderNO,payType, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                if (code == Constants.SUCCESS_CODE) {
                    dismissProgress();
                    isQuery = true;
                    setRedPackData(orderNO, payDialogView.getPayeePwd());
                } else {
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
                ToastUtil.showToast(context, errMessage);
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
                switch (REDPACKET_TYPE) {
                    case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                        if (ISEXCLUSIVE) {
                            //专属红包
                            MobclickAgent.onEvent(context,TEAM_EXCLUSIVE_RP_SEND_ERROR_PASSWORDERRORNUM);
                        } else {
                            //普通红包
                            MobclickAgent.onEvent(context,TEAM_AVERAGE_RP_SEND_ERROR_PASSWORDERRORNUM);
                        }
                        break;
                    case Constants.REDPACK_TYPE.TEAM_RANDOM:
                        //随机红包
                        MobclickAgent.onEvent(context,TEAM_RANDOM_RP_SEND_ERROR_PASSWORDERRORNUM);
                        break;
                }
                payDialogView.dismiss();
            }
        });
        payDialogView.setOnClickListenerOnForgetPwd(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //忘记密码
                payDialogView.dismiss();
                switch (REDPACKET_TYPE) {
                    case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                        if (ISEXCLUSIVE) {
                            //专属红包
                            MobclickAgent.onEvent(context,TEAM_EXCLUSIVE_RP_SEND_ERROR_PASSWORDERRORNUM);
                        } else {
                            //普通红包
                            MobclickAgent.onEvent(context,TEAM_AVERAGE_RP_SEND_ERROR_PASSWORDERRORNUM);
                        }
                        break;
                    case Constants.REDPACK_TYPE.TEAM_RANDOM:
                        //随机红包
                        MobclickAgent.onEvent(context,TEAM_RANDOM_RP_SEND_ERROR_PASSWORDERRORNUM);
                        break;
                }
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

    /**
     * 获取红包Id
     */
    private void getRedPageId(final String amount, final int payType) {
        showProgress(context, false);
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
                                if (StringUtil.isNotEmpty(bean.getOrderNO())) {
                                    rid = bean.getOrderNO();
                                }
                                String targetType = REDPACKET_TYPE + "";
                                String number = "";
                                String money = "";
                                switch (REDPACKET_TYPE) {
                                    case Constants.REDPACK_TYPE.TEAM_RANDOM:
                                        targetIds = "";
                                        number = redPacketNum;
                                        money = totalSum;
                                        break;
                                    case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                                        if (!ISEXCLUSIVE) {
                                            targetIds = "";
                                            number = redPacketNum;
                                            money = etMoney.getText().toString();
                                        } else {
//                    number = zhiDingPeopleNum;
                                            number = redPacketNum;
                                            money = etMoney.getText().toString();
                                        }
                                        break;
                                }
                                String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "恭喜发财，大吉大利！" : etContent.getText().toString();
                                WXUtil.weiChatPay("1",amount,rid,money,targetIds,targetType, redContent,payDialogView.getPayeePwd(),number,mTeamId,context, new WXUtil.WeiChatPayCallBack() {
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
     */
    private void setRedPackData(final String redPacketId, String payeePwd) {
        String rid = "";
        if (StringUtil.isNotEmpty(redPacketId)) {
            rid = redPacketId;
        }
        String targetType = REDPACKET_TYPE + "";
        String number = "";
        String money = "";
        switch (REDPACKET_TYPE) {
            case Constants.REDPACK_TYPE.TEAM_RANDOM:
                targetIds = "";
                number = redPacketNum;
                money = totalSum;
                break;
            case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                if (!ISEXCLUSIVE) {
                    targetIds = "";
                    number = redPacketNum;
                    money = etMoney.getText().toString();
                } else {
//                    number = zhiDingPeopleNum;
                    number = redPacketNum;
                    money = etMoney.getText().toString();
                }
                break;
        }
        String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "恭喜发财，大吉大利！" : etContent.getText().toString();
        showProgress(context, false);
        UserApi.sendRedPack(rid, money, targetIds, targetType, redContent, payeePwd, number, mTeamId, context, new requestCallback() {
            @Override
            public void onSuccess(int code, Object object) {
                dismissProgress();
                if (code == Constants.SUCCESS_CODE) {
                    //发送红包
                    pushSendRedPackMessage(redPacketId);
                } else {
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
     */
    private void pushSendRedPackMessage(String redPacketId) {
        if (StringUtil.isEmpty(redPacketId)) {
            return;
        }
        String redId = redPacketId;
        String redContent = StringUtil.isEmpty(etContent.getText().toString()) ? "恭喜发财，大吉大利！" : etContent.getText().toString();
        int mCount = count;
        switch (REDPACKET_TYPE) {
            case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                if (ISEXCLUSIVE) {
                    mCount = Integer.parseInt(zhiDingPeopleNum);
                } else {
                    mCount = Integer.parseInt(redPacketNum);
                }
                break;
            case Constants.REDPACK_TYPE.TEAM_RANDOM:
                mCount = Integer.parseInt(redPacketNum);
                break;
        }

        //构建红包结构Map
        BuildRedPackStructure structure = new BuildRedPackStructure();
        structure.build((Activity) context, REDPACKET_TYPE, ISEXCLUSIVE, redId, redContent,
                mCount, mTeamId, totalSum, targetIds);
    }

    /**
     * 切换红包类型
     */
    private void modifyRedType() {
        etMoney.setText("");
        etRedPacketNum.setText("");
        inputAmount = "";
        redPacketNum = "";
        totalSum = "0.00";
        tvMoneyNum.setText("￥ " + totalSum);
        switch (REDPACKET_TYPE) {
            case Constants.REDPACK_TYPE.TEAM_ORDINARY:
                //普通红包
                if (ISEXCLUSIVE) {
                    //专属红包
                    tvModifyRedType.setTextColor(Color.parseColor("#5D5D5D"));
                    tvMoneyType.setText("单个金额");
                    tvRedPacketType.setText("当前为专属红包，");
                    tvModifyRedType.setText("已指定" + zhiDingPeopleNum + "人领取");
                    etRedPacketNum.setVisibility(View.GONE);
                    tvRedPacketNum.setVisibility(View.VISIBLE);
                    tvRedPacketNum.setText(zhiDingPeopleNum);
                    tvModifyRedType.setClickable(false);
                    tvModifyRedType.setEnabled(false);
                } else {
                    //普通红包
                    tvModifyRedType.setTextColor(Color.parseColor("#3778D9"));
                    tvMoneyType.setText("单个金额");
                    tvRedPacketType.setText("当前为普通红包，");
                    tvModifyRedType.setText("改为拼手气红包");
                    if (isGroupOwner){
                        etRedPacketNum.setVisibility(View.VISIBLE);
                        tvRedPacketNum.setVisibility(View.GONE);
                    }else {
                        etRedPacketNum.setVisibility(View.GONE);
                        tvRedPacketNum.setVisibility(View.VISIBLE);
                        tvRedPacketNum.setText("1");
                    }
                    tvModifyRedType.setClickable(true);
                    tvModifyRedType.setEnabled(true);
                }
                break;
            case Constants.REDPACK_TYPE.TEAM_RANDOM:
                //随机红包
                tvModifyRedType.setTextColor(Color.parseColor("#3778D9"));
                tvMoneyType.setText("总金额");
                tvRedPacketType.setText("当前为拼手气红包，");
                tvModifyRedType.setText("改为普通红包");
                if (isGroupOwner){
                    etRedPacketNum.setVisibility(View.VISIBLE);
                    tvRedPacketNum.setVisibility(View.GONE);
                }else {
                    etRedPacketNum.setVisibility(View.GONE);
                    tvRedPacketNum.setVisibility(View.VISIBLE);
                    tvRedPacketNum.setText("1");
                }
                tvModifyRedType.setClickable(true);
                tvModifyRedType.setEnabled(true);
                ISEXCLUSIVE = false;
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            ArrayList<String> selectList = data.getStringArrayListExtra(RESULT_DATA);
            ArrayList<String> selectName = data.getStringArrayListExtra(RESULT_NAME);
            if(selectList!=null&&selectList.size()>0){
                String selectNameStr = selectName.toString();
                ISEXCLUSIVE = true;
                totalSum = String.format("%.2f", NumberUtil.mul(etMoney.getText().toString(), selectList.size() + ""));
                zhiDingPeopleNum = selectList.size() + "";
                targetIds = "";
                for (int i = 0; i < selectList.size() ; i++) {
                    if(i==selectList.size()-1){
                        targetIds = targetIds + selectList.get(i);
                    }else{
                        targetIds = targetIds + selectList.get(i)+",";
                    }
                }

                tvRedPacketNum.setText(zhiDingPeopleNum);
                tvWho.setText(selectNameStr.substring(1,selectNameStr.length()-1));
                REDPACKET_TYPE = Constants.REDPACK_TYPE.TEAM_ORDINARY;
            }else{

                targetIds = "";
                ISEXCLUSIVE = false;
                zhiDingPeopleNum = "";
                tvRedPacketNum.setText(zhiDingPeopleNum);
//                totalSum = "0.00";
                tvWho.setText("群内所有人");

            }

            tvMoneyNum.setText("￥ " + totalSum);
            modifyRedType();




        }
    }

}

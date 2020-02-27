package com.wulewan.ghxm.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lxj.xpopup.core.CenterPopupView;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.config.Constants;


public class PaySelect extends CenterPopupView implements View.OnClickListener {

    private Context context;

    private String payNum;
    private String commodityName;
    private String currWalletNum;
    private ImageView ali_btn_icon;
    private ImageView wchat_btn_icon;

    private ImageView wallet_btn_icon;

    private int type = 0;
    private Boolean isWalletAble = false;

    public static enum SelectPayType {
        ALI, // 支付宝
        WCHAT, // 微信
        WALLET, // 钱包
    }

    private SelectPayType currSeletPayType;

    /**
     * 返回当前选择的支付类型
     *
     * @return
     */
    public SelectPayType getCurrSeletPayType() {
        return currSeletPayType;
    }

    /**
     * 支付方式选择
     *
     * @param context
     * @param payNum        支付的金额
     * @param commodityName 商品名称
     * @param currWalletNum 当前余额
     * @param type          1 （展示 余额 微信 支付宝）2 （展示 微信 支付宝）
     */
    public PaySelect(@NonNull Context context, String payNum, String commodityName, String currWalletNum, int type) {
        super(context);
        this.context = context;
        this.commodityName = commodityName;
        this.payNum = payNum;
        this.currWalletNum = currWalletNum;
        this.type = type;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.attach_pay_select;
    }


    @Override
    public void init() {
        super.init();
        initView();
    }

    private void initView() {
        ImageView close_btn = findViewById(R.id.close_btn);
//        TextView pay_btn = findViewById(R.id.pay_btn);
        LinearLayout ali_btn = findViewById(R.id.ali_btn);
        LinearLayout wchat_btn = findViewById(R.id.wchat_btn);
        LinearLayout wallet_btn = findViewById(R.id.wallet_btn);

        ali_btn_icon = findViewById(R.id.bt3);
        wchat_btn_icon = findViewById(R.id.bt2);
        wallet_btn_icon = findViewById(R.id.bt1);

        close_btn.setOnClickListener(this);
        ali_btn.setOnClickListener(this);
        wchat_btn.setOnClickListener(this);
        TextView pay_btn = findViewById(R.id.pay_btn);
        pay_btn.setOnClickListener(this);
        TextView walletnum = findViewById(R.id.walletnum);
        if (this.type != 1) {
            wallet_btn.setVisibility(View.GONE);
            findViewById(R.id.gap).setVisibility(View.GONE);
        } else {
            if (NumberUtil.compareLess(this.currWalletNum, this.payNum)) {
                findViewById(R.id.little).setVisibility(View.VISIBLE);
                walletnum.setTextColor(getResources().getColor(R.color.color_9000000));
                wallet_btn_icon.setVisibility(View.GONE);
            } else {
                wallet_btn.setOnClickListener(this);
                findViewById(R.id.little).setVisibility(View.GONE);
                isWalletAble = true;
            }
        }

        TextView amount = findViewById(R.id.amount);
        amount.setText("¥ " + this.payNum + " " + this.context.getString(R.string.yuan));
        walletnum.setText(this.context.getString(R.string.wallet_num) + "¥ " + currWalletNum + " " + this.context.getString(R.string.yuan));


        TextView commodityName = findViewById(R.id.commodityName);
        commodityName.setText(this.context.getString(R.string.commodity_name) + this.commodityName);


        SPUtils instance = SPUtils.getInstance(Constants.ALIPAY_USERINFO.FILENAME);
        String aLiPay = instance.getString(Constants.CONFIG_INFO.ALIPAY_ISSHOW);
        String wChatPay = instance.getString(Constants.CONFIG_INFO.WCHATPAY_ISSHOW);
        if (aLiPay.equals("1")) {
            ali_btn.setVisibility(VISIBLE);
            findViewById(R.id.gap2).setVisibility(View.VISIBLE);
            findViewById(R.id.gap3).setVisibility(View.VISIBLE);
        } else if (aLiPay.equals("0")) {
            ali_btn.setVisibility(GONE);
            findViewById(R.id.gap2).setVisibility(View.GONE);
            findViewById(R.id.gap3).setVisibility(View.GONE);
        }
        if (wChatPay.equals("1")) {
            wchat_btn.setVisibility(VISIBLE);
            findViewById(R.id.gap).setVisibility(View.VISIBLE);
        } else if (wChatPay.equals("0")) {
            wchat_btn.setVisibility(GONE);
            findViewById(R.id.gap).setVisibility(View.GONE);
        }

        resetBtnIcon();
        if (isWalletAble){
            //余额充足
            currSeletPayType = SelectPayType.WALLET;
            wallet_btn_icon.setImageResource(R.drawable.myx_radiobtn_select);
        }else {
            //余额不足
            if (wChatPay.equals("1")){
                //可以微信支付
                currSeletPayType = SelectPayType.WCHAT;
                wchat_btn_icon.setImageResource(R.drawable.myx_radiobtn_select);
                return;
            }
            if (aLiPay.equals("1")){
                //可以支付宝支付
                currSeletPayType = SelectPayType.ALI;
                ali_btn_icon.setImageResource(R.drawable.myx_radiobtn_select);
            }
        }

    }

    private OnClickListener sureListener;

    public void setOnClickListenerOnSure(OnClickListener sureListener) {
        this.sureListener = sureListener;
    }

    private void resetBtnIcon() {
        ali_btn_icon.setImageResource(R.drawable.myx_radiobtn_unselect);
        wchat_btn_icon.setImageResource(R.drawable.myx_radiobtn_unselect);
        if (isWalletAble) {
            wallet_btn_icon.setImageResource(R.drawable.myx_radiobtn_unselect);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_btn:
                dismiss();
                break;
            case R.id.pay_btn:
                if (sureListener != null) {
                    sureListener.onClick(v);
                }
                dismiss();
                break;
            case R.id.ali_btn:
                resetBtnIcon();
                currSeletPayType = SelectPayType.ALI;
                ali_btn_icon.setImageResource(R.drawable.myx_radiobtn_select);
                break;
            case R.id.wchat_btn:
//                Toast toast = Toast.makeText(context,
//                        "功能添加中，敬请期待", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
                resetBtnIcon();
                currSeletPayType = SelectPayType.WCHAT;
                wchat_btn_icon.setImageResource(R.drawable.myx_radiobtn_select);
                break;
            case R.id.wallet_btn:
                resetBtnIcon();
                currSeletPayType = SelectPayType.WALLET;
                wallet_btn_icon.setImageResource(R.drawable.myx_radiobtn_select);
                break;
        }

    }
}

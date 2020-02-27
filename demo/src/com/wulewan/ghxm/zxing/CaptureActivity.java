package com.wulewan.ghxm.zxing;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.gson.Gson;
import com.king.zxing.util.CodeUtils;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.TurnQrCodeBean;
import com.wulewan.ghxm.team.activity.AdvancedTeamJoinActivity;
import com.wulewan.ghxm.utils.StringUtil;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.ToastHelper;
import com.umeng.analytics.MobclickAgent;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.contact.activity.UserProfileActivity;
import com.wulewan.ghxm.session.SessionHelper;

import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_MANAGER_SCANNINGTEAMQRCODE;


public class CaptureActivity extends BaseAct implements QRCodeReaderView.OnQRCodeReadListener {

    private static final int REQUEST_IMAGE = 1;

    private QRCodeReaderView qrCodeReaderView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_layout);
        initUI();


    }

    /**
     * 初始化
     */
    public void initUI() {
        setToolbar(R.drawable.jrmf_b_top_back, "新的朋友");

        setRightText("相册", new onToolBarListner() {
            @Override
            public void onRight() {
                openXiangChe();
            }
        });
        qrCodeReaderView = findView(R.id.qrCodeReaderView);

        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setQRDecodingEnabled(true);
        qrCodeReaderView.setTorchEnabled(true);
        qrCodeReaderView.setAutofocusInterval(1000L);

    }


    public void openXiangChe() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }


    private void setScanReslut(String result) {
        Gson gson = new Gson();
        try {

            final TurnQrCodeBean turnQrCodeBean = gson.fromJson(result, TurnQrCodeBean.class);
            if (turnQrCodeBean == null) return;
            switch (turnQrCodeBean.type) {
                case ZXingUtils.TYPE_PERSON:
                    UserProfileActivity.start(CaptureActivity.this, turnQrCodeBean.id);
                    break;
                case ZXingUtils.TYPE_GROUP:
                    MobclickAgent.onEvent(this,TEAM_MANAGER_SCANNINGTEAMQRCODE);
                    TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(turnQrCodeBean.id, NimUIKit.getAccount());
                    if (null != teamMember && teamMember.isInTeam()) {
                        SessionHelper.startTeamSession(this, turnQrCodeBean.id); // 进入群
                    } else {
                        AdvancedTeamJoinActivity.start(this, turnQrCodeBean.id);
                    }
                    break;
                default:
                    ToastHelper.showToast(this, "未识别类型");
                    Intent intent= new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(result);
                    intent.setData(content_url);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } catch (Exception ignored) {
            ToastHelper.showToast(this, "未识别类型");
            Intent intent= new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(result);
            intent.setData(content_url);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
        finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    String result = CodeUtils.parseQRCode(RealPathFromUriUtils.getRealPathFromUri(CaptureActivity.this, uri));
                    if (StringUtil.isNotEmpty(result)){
                        if (result.startsWith("wxp://") || result.startsWith("WXP://")){
                            //字符串以wxp://开头    证明是微信的二维码
                            try {
                                //利用Intent打开微信
                                dismissProgress();
                                Uri wxUri = Uri.parse("weixin://");
                                Intent intent3 = new Intent(Intent.ACTION_VIEW, wxUri);
                                startActivity(intent3);
                            } catch (Exception e) {
                                //若无法正常跳转，在此进行错误处理
                                toast("无法跳转到微信，请检查您是否安装了微信！");
                            }
                        }else if (result.startsWith("https://qr.alipay.com/") || result.startsWith("HTTPS://QR.ALIPAY.COM/")){
                            //字符串以https://qr.alipay.com/开头    证明是支付宝的二维码
                            try {
                                //利用Intent打开支付宝
                                dismissProgress();
                                String[] split = null;
                                if (result.startsWith("https://qr.alipay.com/")){
                                    split = result.split("https://qr.alipay.com/");
                                }else if (result.startsWith("HTTPS://QR.ALIPAY.COM/")){
                                    split = result.split("HTTPS://QR.ALIPAY.COM/");
                                }
                                String urlCode = split[1];
                                String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                                        "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
                                        "%3Dweb-other&_t=1472443966571#Intent;" + "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";

                                Intent intent = Intent.parseUri(intentFullUrl.replace("{urlCode}", urlCode), Intent.URI_INTENT_SCHEME );
                                startActivity(intent);
                            } catch (Exception e) {
                                //若无法正常跳转，在此进行错误处理
                                toast("无法跳转到支付宝，请检查您是否安装了支付宝！");
                            }
                        }else {
                            setScanReslut(result);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        qrCodeReaderView.setOnQRCodeReadListener(null);
        if (StringUtil.isNotEmpty(text)){
            setScanReslut(text);
        }
    }
}

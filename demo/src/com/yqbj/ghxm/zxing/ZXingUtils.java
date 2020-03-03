package com.yqbj.ghxm.zxing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.king.zxing.util.CodeUtils;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.yqbj.ghxm.bean.TurnQrCodeBean;
import com.yqbj.ghxm.utils.SPUtils;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.wrapper.NimUserInfoProvider;
import com.netease.yqbj.uikit.common.util.media.ImageUtil;
import com.netease.yqbj.uikit.common.util.storage.StorageType;
import com.netease.yqbj.uikit.common.util.storage.StorageUtil;
import com.netease.yqbj.uikit.common.util.string.MD5;
import com.yqbj.ghxm.config.Constants;

import java.io.File;
import java.util.Hashtable;

public class ZXingUtils {

    public static final String TYPE_PERSON = "1";
    public static final String TYPE_GROUP = "2";
    public static Bitmap bitmap;


    public static void createQrCode(TurnQrCodeBean turnQrCodeBean, final ImageView qrCodeImg, final int width, final int height) throws Exception {
        if (!(qrCodeImg instanceof ImageView)) throw new Exception("类型不为ImageView");
        final String content = turnQrCodeBean.toString();
        String md5Str= MD5.getStringMD5(content);
        String fileName = md5Str + ".jpeg";

        final String path = StorageUtil.getWritePath(fileName, StorageType.TYPE_FILE);
        File file = new File(path);
        if(file!=null&&file.exists()){
            Log.e("imgExists","imgExists");
            qrCodeImg.setImageBitmap(ImageUtil.getImage(path));
        }else{

            final Handler handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    qrCodeImg.setImageBitmap(bitmap);
                    return false;
                }
            });
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    bitmap = CodeUtils.createQRCode(content,height);
                    ImageUtil.saveBitmap2file(bitmap,path,NimUIKit.getContext());
                    handler.sendEmptyMessage(1);
                }
            });
            thread.start();

        }




    }

    public static void scanCode(Context context) {
        Intent intent = new Intent(context, CaptureActivity.class);
        context.startActivity(intent);
    }

    public static void showMyCode(Context context) {
        Intent intent = new Intent(context, QrCodeActivity.class);
        intent.putExtra("type", TYPE_PERSON);
        intent.putExtra("id", SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID));
        NimUserInfoProvider userInfoProvider = new NimUserInfoProvider(context);
        UserInfo userInfo = userInfoProvider.getUserInfo(SPUtils.getInstance().getString(Constants.USER_TYPE.ACCID));
        intent.putExtra("name", userInfo.getName());
        intent.putExtra("icon", userInfo.getAvatar());
        context.startActivity(intent);
    }

    public static void showTeamCode(Context context, String teamId) {
        Intent intent = new Intent(context, QrCodeActivity.class);
        intent.putExtra("type", TYPE_GROUP);
        intent.putExtra("id", teamId);
        intent.putExtra("name", NimUIKit.getTeamProvider().getTeamById(teamId).getName());
        context.startActivity(intent);
    }


    /**
     * 生成二维码图片
     *
     * @param text
     * @param w
     * @param h
     * @param logo
     * @return
     */
    public Bitmap createImage(String text, int w, int h, Bitmap logo) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        try {
            Bitmap scaleLogo = getScaleLogo(logo, w, h);

            int offsetX = w / 2;
            int offsetY = h / 2;

            int scaleWidth = 0;
            int scaleHeight = 0;
            if (scaleLogo != null) {
                scaleWidth = scaleLogo.getWidth();
                scaleHeight = scaleLogo.getHeight();
                offsetX = (w - scaleWidth) / 2;
                offsetY = (h - scaleHeight) / 2;
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, w, h, hints);
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (x >= offsetX && x < offsetX + scaleWidth && y >= offsetY && y < offsetY + scaleHeight) {
                        int pixel = scaleLogo.getPixel(x - offsetX, y - offsetY);
                        if (pixel == 0) {
                            if (bitMatrix.get(x, y)) {
                                pixel = 0xff000000;
                            } else {
                                pixel = 0xffffffff;
                            }
                        }
                        pixels[y * w + x] = pixel;
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * w + x] = 0xff000000;
                        } else {
                            pixels[y * w + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getScaleLogo(Bitmap logo, int w, int h) {
        if (logo == null) return null;
        Matrix matrix = new Matrix();
        float scaleFactor = Math.min(w * 1.0f / 5 / logo.getWidth(), h * 1.0f / 5 / logo.getHeight());
        matrix.postScale(scaleFactor, scaleFactor);
        Bitmap result = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), matrix, true);
        return result;
    }


}

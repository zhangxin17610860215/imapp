package com.netease.yqbj.uikit.common.util.media;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.text.TextUtils;

import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.business.session.viewholder.MsgViewHolderThumbBase;
import com.netease.yqbj.uikit.common.util.file.AttachmentStore;
import com.netease.yqbj.uikit.common.util.file.FileUtil;
import com.netease.yqbj.uikit.common.util.log.LogUtil;
import com.netease.yqbj.uikit.common.util.storage.StorageType;
import com.netease.yqbj.uikit.common.util.storage.StorageUtil;
import com.netease.yqbj.uikit.common.util.string.StringUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
    public static class ImageSize {
        public int width = 0;
        public int height = 0;

        public ImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public final static float MAX_IMAGE_RATIO = 5f;

    public static Bitmap getDefaultBitmapWhenGetFail() {
        try {
            return getBitmapImmutableCopy(NimUIKit.getContext().getResources(), R.drawable.nim_image_download_failed);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final Bitmap getBitmapImmutableCopy(Resources res, int id) {
        return getBitmap(res.getDrawable(id)).copy(Config.RGB_565, false);
    }

    public static final Bitmap getBitmap(Drawable dr) {
        if (dr == null) {
            return null;
        }

        if (dr instanceof BitmapDrawable) {
            return ((BitmapDrawable) dr).getBitmap();
        }

        return null;
    }

    public static Bitmap rotateBitmapInNeeded(String path, Bitmap srcBitmap) {
        if (TextUtils.isEmpty(path) || srcBitmap == null) {
            return null;
        }

        ExifInterface localExifInterface;
        try {
            localExifInterface = new ExifInterface(path);
            int rotateInt = localExifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            float rotate = getImageRotate(rotateInt);
            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                Bitmap dstBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
                        srcBitmap.getWidth(), srcBitmap.getHeight(), matrix,
                        false);
                if (dstBitmap == null) {
                    return srcBitmap;
                } else {
                    if (srcBitmap != null && !srcBitmap.isRecycled()) {
                        srcBitmap.recycle();
                    }
                    return dstBitmap;
                }
            } else {
                return srcBitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return srcBitmap;
        }
    }

    /**
     * 获得旋转角度
     *
     * @param rotate
     * @return
     */
    public static float getImageRotate(int rotate) {
        float f;
        if (rotate == 6) {
            f = 90.0F;
        } else if (rotate == 3) {
            f = 180.0F;
        } else if (rotate == 8) {
            f = 270.0F;
        } else {
            f = 0.0F;
        }

        return f;
    }

    public static String makeThumbnail(File imageFile) {
        String thumbFilePath = StorageUtil.getWritePath(imageFile.getName(), StorageType.TYPE_THUMB_IMAGE);
        File thumbFile = AttachmentStore.create(thumbFilePath);
        if (thumbFile == null) {
            return null;
        }

        boolean result = scaleThumbnail(
                imageFile,
                thumbFile,
                MsgViewHolderThumbBase.getImageMaxEdge(),
                MsgViewHolderThumbBase.getImageMinEdge(),
                CompressFormat.JPEG,
                60);
        if (!result) {
            AttachmentStore.delete(thumbFilePath);
            return null;
        }

        return thumbFilePath;
    }

    public static Boolean scaleThumbnail(File srcFile, File dstFile, int dstMaxWH, int dstMinWH, CompressFormat compressFormat, int quality) {
        Boolean bRet = false;
        Bitmap srcBitmap = null;
        Bitmap dstBitmap = null;
        BufferedOutputStream bos = null;

        try {
            int[] bound = BitmapDecoder.decodeBound(srcFile);
            ImageSize size = getThumbnailDisplaySize(bound[0], bound[1], dstMaxWH, dstMinWH);
            srcBitmap = BitmapDecoder.decodeSampled(srcFile.getPath(), size.width, size.height);

            // 旋转
            ExifInterface localExifInterface = new ExifInterface(srcFile.getAbsolutePath());
            int rotateInt = localExifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            float rotate = getImageRotate(rotateInt);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);

            float inSampleSize = 1;

            if (srcBitmap.getWidth() >= dstMinWH && srcBitmap.getHeight() <= dstMaxWH
                    && srcBitmap.getWidth() >= dstMinWH && srcBitmap.getHeight() <= dstMaxWH) {
                //如果第一轮拿到的srcBitmap尺寸都符合要求，不需要再做缩放
            } else {
                if (srcBitmap.getWidth() != size.width || srcBitmap.getHeight() != size.height) {
                    float widthScale = (float) size.width / (float) srcBitmap.getWidth();
                    float heightScale = (float) size.height / (float) srcBitmap.getHeight();

                    if (widthScale >= heightScale) {
                        size.width = srcBitmap.getWidth();
                        size.height /= widthScale;//必定小于srcBitmap.getHeight()
                        inSampleSize = widthScale;
                    } else {
                        size.width /= heightScale;//必定小于srcBitmap.getWidth()
                        size.height = srcBitmap.getHeight();
                        inSampleSize = heightScale;
                    }
                }
            }

            matrix.postScale(inSampleSize, inSampleSize);

            if (rotate == 0 && inSampleSize == 1) {
                dstBitmap = srcBitmap;
            } else {
                dstBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, size.width, size.height, matrix, true);
            }

            bos = new BufferedOutputStream(new FileOutputStream(dstFile));
            dstBitmap.compress(compressFormat, quality, bos);
            bos.flush();
            bRet = true;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (srcBitmap != null && !srcBitmap.isRecycled()) {
                srcBitmap.recycle();
                srcBitmap = null;
            }

            if (dstBitmap != null && !dstBitmap.isRecycled()) {
                dstBitmap.recycle();
                dstBitmap = null;
            }
        }
        return bRet;
    }

    public static ImageSize getThumbnailDisplaySize(float srcWidth, float srcHeight, float dstMaxWH, float dstMinWH) {
        if (srcWidth <= 0 || srcHeight <= 0) { // bounds check
            return new ImageSize((int) dstMinWH, (int) dstMinWH);
        }

        float shorter;
        float longer;
        boolean widthIsShorter;

        //store
        if (srcHeight < srcWidth) {
            shorter = srcHeight;
            longer = srcWidth;
            widthIsShorter = false;
        } else {
            shorter = srcWidth;
            longer = srcHeight;
            widthIsShorter = true;
        }

        if (shorter < dstMinWH) {
            float scale = dstMinWH / shorter;
            shorter = dstMinWH;
            if (longer * scale > dstMaxWH) {
                longer = dstMaxWH;
            } else {
                longer *= scale;
            }
        } else if (longer > dstMaxWH) {
            float scale = dstMaxWH / longer;
            longer = dstMaxWH;
            if (shorter * scale < dstMinWH) {
                shorter = dstMinWH;
            } else {
                shorter *= scale;
            }
        }

        //restore
        if (widthIsShorter) {
            srcWidth = shorter;
            srcHeight = longer;
        } else {
            srcWidth = longer;
            srcHeight = shorter;
        }

        return new ImageSize((int) srcWidth, (int) srcHeight);
    }

    public static File getScaledImageFileWithMD5(File imageFile, String mimeType) {
        String filePath = imageFile.getPath();

        if (!isInvalidPictureFile(mimeType)) {
            LogUtil.i("ImageUtil", "is invalid picture file");
            return null;
        }

        String tempFilePath = getTempFilePath(FileUtil.getExtensionName(filePath));
        File tempImageFile = AttachmentStore.create(tempFilePath);
        if (tempImageFile == null) {
            return null;
        }

        CompressFormat compressFormat = CompressFormat.JPEG;
        // 压缩数值由第三方开发者自行决定
        int maxWidth = 720;
        int quality = 60;

        if (ImageUtil.scaleImage(imageFile, tempImageFile, maxWidth, compressFormat, quality)) {
            return tempImageFile;
        } else {
            return null;
        }
    }

    private static String getTempFilePath(String extension) {
        return StorageUtil.getWritePath(
                NimUIKit.getContext(),
                "temp_image_" + StringUtil.get36UUID() + "." + extension,
                StorageType.TYPE_TEMP);
    }

    public static Boolean scaleImage(File srcFile, File dstFile, int dstMaxWH, CompressFormat compressFormat, int quality) {
        Boolean success = false;

        try {
            int inSampleSize = SampleSizeUtil.calculateSampleSize(srcFile.getAbsolutePath(), dstMaxWH * dstMaxWH);
            Bitmap srcBitmap = BitmapDecoder.decodeSampled(srcFile.getPath(), inSampleSize);
            if (srcBitmap == null) {
                return success;
            }

            float rotate;
            String mimeType = com.netease.yqbj.uikit.common.media.picker.util.BitmapUtil.getImageType(srcFile.getAbsolutePath());
            if (!TextUtils.isEmpty(mimeType) && mimeType.equals("image/png")) {
                // png格式不能使用ExifInterface
                rotate = 0;
            } else {
                // 旋转
                ExifInterface localExifInterface = new ExifInterface(srcFile.getAbsolutePath());
                int rotateInt = localExifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                rotate = getImageRotate(rotateInt);
            }

            Bitmap dstBitmap;
            float scale = (float) Math.sqrt(((float) dstMaxWH * (float) dstMaxWH) / ((float) srcBitmap.getWidth() * (float) srcBitmap.getHeight()));
            if (rotate == 0f && scale >= 1) {
                dstBitmap = srcBitmap;
            } else {
                try {
                    Matrix matrix = new Matrix();
                    if (rotate != 0) {
                        matrix.postRotate(rotate);
                    }
                    if (scale < 1) {
                        matrix.postScale(scale, scale);
                    }
                    dstBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
                } catch (OutOfMemoryError e) {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstFile));
                    srcBitmap.compress(compressFormat, quality, bos);
                    bos.flush();
                    bos.close();
                    success = true;

                    if (!srcBitmap.isRecycled())
                        srcBitmap.recycle();
                    srcBitmap = null;

                    return success;
                }
            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstFile));
            dstBitmap.compress(compressFormat, quality, bos);
            bos.flush();
            bos.close();
            success = true;

            if (!srcBitmap.isRecycled())
                srcBitmap.recycle();
            srcBitmap = null;

            if (!dstBitmap.isRecycled())
                dstBitmap.recycle();
            dstBitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return success;
    }

    public static ImageSize getThumbnailDisplaySize(int maxSide, int minSide, String imagePath) {
        int[] bound = BitmapDecoder.decodeBound(imagePath);
        ImageSize imageSize = getThumbnailDisplaySize(bound[0], bound[1], maxSide, minSide);
        return imageSize;
    }

    public static int[] getBoundWithLength(int maxSide, Object imageObject, boolean resizeToDefault) {
        int width = -1;
        int height = -1;

        int[] bound;
        if (String.class.isInstance(imageObject)) {
            bound = BitmapDecoder.decodeBound((String) imageObject);
            width = bound[0];
            height = bound[1];
        } else if (Integer.class.isInstance(imageObject)) {
            bound = BitmapDecoder.decodeBound(NimUIKit.getContext().getResources(), (Integer) imageObject);
            width = bound[0];
            height = bound[1];
        } else if (InputStream.class.isInstance(imageObject)) {
            bound = BitmapDecoder.decodeBound((InputStream) imageObject);
            width = bound[0];
            height = bound[1];
        }

        int defaultWidth = maxSide;
        int defaultHeight = maxSide;
        if (width <= 0 || height <= 0) {
            width = defaultWidth;
            height = defaultHeight;
        } else if (resizeToDefault) {
            if (width > height) {
                height = (int) (defaultWidth * ((float) height / (float) width));
                width = defaultWidth;
            } else {
                width = (int) (defaultHeight * ((float) width / (float) height));
                height = defaultHeight;
            }
        }

        return new int[]{width, height};
    }

    /**
     * 下载失败与获取失败时都统一显示默认下载失败图片
     *
     * @return
     */
    public static Bitmap getBitmapFromDrawableRes(int res) {
        try {
            return getBitmapImmutableCopy(NimUIKit.getContext().getResources(), res);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isInvalidPictureFile(String mimeType) {
        String lowerCaseFilepath = mimeType.toLowerCase();
        return (lowerCaseFilepath.contains("jpg") || lowerCaseFilepath.contains("jpeg")
                || lowerCaseFilepath.toLowerCase().contains("png") || lowerCaseFilepath.toLowerCase().contains("bmp") || lowerCaseFilepath
                .toLowerCase().contains("gif"));
    }

    public static boolean isGif(String extension) {
        return !TextUtils.isEmpty(extension) && extension.toLowerCase().equals("gif");
    }

    public static void saveBitmap2file(Bitmap bmp,String path ,Context context) {



        File filePic = new File(path);
        try {
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            //Toast.makeText(context, "保存成功,位置:" + filePic.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }




    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
//canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
//        Log.e("ImageUtils-->", "原始大小" + baos.toByteArray().length);
        while (baos.toByteArray().length / 1024 > 100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
//        Log.e("ImageUtils-->", "压缩后大小" + baos.toByteArray().length);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static Bitmap getImage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }
}

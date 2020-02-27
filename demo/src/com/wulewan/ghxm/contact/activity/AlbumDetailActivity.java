package com.wulewan.ghxm.contact.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.king.zxing.util.CodeUtils;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.TurnQrCodeBean;
import com.wulewan.ghxm.common.ui.BaseAct;
import com.wulewan.ghxm.config.Constants;
import com.wulewan.ghxm.team.activity.AdvancedTeamJoinActivity;
import com.wulewan.ghxm.utils.DownLoadImageService;
import com.wulewan.ghxm.utils.ImageDownLoadCallBack;
import com.wulewan.ghxm.utils.StringUtil;
import com.wulewan.ghxm.zxing.RealPathFromUriUtils;
import com.wulewan.ghxm.zxing.ZXingUtils;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.business.session.actions.PickImageAction;
import com.netease.wulewan.uikit.common.ToastHelper;
import com.netease.wulewan.uikit.common.media.picker.PickImageHelper;
import com.netease.wulewan.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.wulewan.uikit.common.util.C;
import com.netease.wulewan.uikit.common.util.file.AttachmentStore;
import com.netease.wulewan.uikit.common.util.storage.StorageUtil;
import com.umeng.analytics.MobclickAgent;
import com.wulewan.ghxm.requestutils.api.UserApi;
import com.wulewan.ghxm.requestutils.requestCallback;
import com.wulewan.ghxm.session.SessionHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.netease.wulewan.uikit.api.StatisticsConstants.TEAM_MANAGER_SCANNINGTEAMQRCODE;
import static com.netease.wulewan.uikit.business.session.constant.Extras.EXTRA_FILE_PATH;

public class AlbumDetailActivity extends BaseAct {

    private Context context;
    private Handler handler;
    private int position;
    private List<String> urlList = new ArrayList<>();
    private ViewPager mViewPager;
    private MyPagerAdapter pagerAdapter;

    protected CustomAlertDialog alertDialog;
    private String indexUrl = "";
    private int pos;
    private String accId = "";

    public static void start(Context context, int position, List<String> urlList, String accId) {
        Intent intent = new Intent();
        intent.setClass(context, AlbumDetailActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("urlList", (Serializable) urlList);
        intent.putExtra("accId",accId);
        ((Activity) context).startActivityForResult(intent, 10);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_detail_activity_layout);
        context = this;

        position = getIntent().getIntExtra("position",0);
        urlList = (List<String>) getIntent().getSerializableExtra("urlList");
        accId = getIntent().getStringExtra("accId");
        initView();

    }

    private void initView() {
        alertDialog = new CustomAlertDialog(this);
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        pagerAdapter = new MyPagerAdapter(context,urlList);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(position);

        findView(R.id.img_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pos = mViewPager.getCurrentItem();
                if (null != urlList || urlList.size() > 0){
                    String url = urlList.get(pos);
                    onLongClickAction(url);
                }
            }
        });
    }

    public class MyPagerAdapter extends PagerAdapter {
        private Context mContext;
        private List<String> mData;

        public MyPagerAdapter(Context context ,List<String> list) {
            mContext = context;
            mData = list;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = View.inflate(mContext, R.layout.album_detail_item_layout,null);
            ImageView img = (ImageView) view.findViewById(R.id.img_album_detail_item);
            Glide.with(mContext).load(mData.get(position)).into(img);

            img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    pos = position;
                    onLongClickAction(mData.get(position));
                    return true;
                }
            });

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFinish();
                }
            });

            view.findViewById(R.id.mRl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFinish();
                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // super.destroyItem(container,position,object); 这一句要删除，否则报错
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            //增加此函数为了支持ViewPager的刷新功能
            return POSITION_NONE;
        }
    }

    private void onFinish(){
        Intent intent = new Intent();
        intent.putExtra("urlList", (Serializable) urlList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onLongClickAction(final String url) {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
            return;
        }
        handler = new Handler();
        indexUrl = url;
        alertDialog.clearData();
        if (accId.equals(NimUIKit.getAccount())){
            //是本人
            alertDialog.addItem("更换图片", new CustomAlertDialog.onSeparateItemClickListener() {

                @Override
                public void onClick() {
                    showProgress(context,false);
                    upDatePicture();
                }
            });
        }
        alertDialog.addItem("识别图中二维码", new CustomAlertDialog.onSeparateItemClickListener() {

            @Override
            public void onClick() {
                showProgress(context,false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downLoadImage(url, 2);
                    }
                }).start();
            }
        });
        alertDialog.addItem("保存到手机", new CustomAlertDialog.onSeparateItemClickListener() {

            @Override
            public void onClick() {
                showProgress(context,false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downLoadImage(url, 1);
                    }
                }).start();
            }
        });
        alertDialog.addItem("取消", new CustomAlertDialog.onSeparateItemClickListener() {

            @Override
            public void onClick() {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void savePicture(String url, File file) {
        if (null != file){
            String path = file.getPath();
            if (TextUtils.isEmpty(path)) {
                return;
            }

            String srcFilename = file.getName();
            //默认jpg
            String extension = "jpg";
            srcFilename += ("." + extension);

            String picPath = StorageUtil.getSystemImagePath();
            String dstPath = picPath + srcFilename;
            if (AttachmentStore.copy(path, dstPath) != -1) {
                try {
                    ContentValues values = new ContentValues(2);
                    values.put(MediaStore.Images.Media.MIME_TYPE, C.MimeType.MIME_JPEG);
                    values.put(MediaStore.Images.Media.DATA, dstPath);
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    handler.post(onDownLoadSuccessRunnable);
                } catch (Exception e) {
                    // may be java.lang.UnsupportedOperationException
                    handler.post(onDownLoadFailedRunnable);
                }
            } else {
                handler.post(onDownLoadFailedRunnable);
            }
        }
    }

    private void downLoadImage(final String url, final int type) {
        //下载图片
        new DownLoadImageService(context, url, new ImageDownLoadCallBack() {
            @Override
            public void onDownLoadSuccess(File file) {
                switch (type){
                    case 1:
                        savePicture(url,file);
                        break;
                    case 2:
                        distinguishQRCode(file);
                        break;
                }

            }

            @Override
            public void onDownLoadFailed() {

            }
        }).run();
    }

    private void distinguishQRCode(File file) {
        //识别图中二维码
        if (null == file){
            return;
        }
        try {
            FileInputStream fs = new FileInputStream(file.getPath());

            Bitmap bitmap = BitmapFactory.decodeStream(fs);

            Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),bitmap , null,null));

            String result = CodeUtils.parseQRCode(RealPathFromUriUtils.getRealPathFromUri(context, imageUri));
            if (StringUtil.isNotEmpty(result)){
                if (result.startsWith("wxp://") || result.startsWith("WXP://")){
                    //字符串以wxp://开头    证明是微信的二维码
                    try {
                        //利用Intent打开微信
                        dismissProgress();
                        Uri uri = Uri.parse("weixin://");
                        Intent intent3 = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent3);
                    } catch (Exception e) {
                        //若无法正常跳转，在此进行错误处理
                        handler.post(onStateWeChatRunnable);
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
                        handler.post(onStateAliPayRunnable);
                    }
                }else {
                    dismissProgress();
                    setScanReslut(result);
                }

            }else {
                dismissProgress();
                handler.post(onDistinguishFailedRunnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
            dismissProgress();
        }
    }

    private void setScanReslut(String result) {
        Gson gson = new Gson();
        try {

            final TurnQrCodeBean turnQrCodeBean = gson.fromJson(result, TurnQrCodeBean.class);
            if (turnQrCodeBean == null) return;
            switch (turnQrCodeBean.type) {
                case ZXingUtils.TYPE_PERSON:
                    UserProfileActivity.start(context, turnQrCodeBean.id);
                    break;
                case ZXingUtils.TYPE_GROUP:
                    MobclickAgent.onEvent(context,TEAM_MANAGER_SCANNINGTEAMQRCODE);
                    TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(turnQrCodeBean.id, NimUIKit.getAccount());
                    if (null != teamMember && teamMember.isInTeam()) {
                        SessionHelper.startTeamSession(context, turnQrCodeBean.id); // 进入群
                    } else {
                        AdvancedTeamJoinActivity.start(context, turnQrCodeBean.id);
                    }
                    break;
                default:
                    handler.post(onDistinguishFailedRunnable);
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
            handler.post(onDistinguishFailedRunnable);
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
        onFinish();
    }

    private void upDatePicture() {
        //更换图片
        if (null != alertDialog){
            alertDialog.dismiss();
        }
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = R.string.set_album_image;
        option.crop = true;
        option.multiSelect = false;
        option.cropOutputImageWidth = 720;
        option.cropOutputImageHeight = 720;
        PickImageHelper.pickImage(context, 101, option);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            showProgress(context,false);
            String path = data.getStringExtra(EXTRA_FILE_PATH);
            upDataAlbum(path);
        }
    }

    private void upDataAlbum(String path) {
        if (StringUtil.isEmpty(path)){
            return;
        }
        File file = new File(path);
        if (file == null) {
            return;
        }
        AbortableFuture<String> upload = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        upload.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int i, final String url, Throwable throwable) {
                Log.e("TAG",">>>>>>>>" + url);
                if (StringUtil.isEmpty(url)){
                    return;
                }
                if (urlList.size() == 1){
                    urlList.clear();
                    urlList.add(url);
                }else {
                    if (urlList.contains(indexUrl)){
                        Iterator<String> iterator = urlList.iterator();
                        while (iterator.hasNext()){
                            String next = iterator.next();
                            if (indexUrl.equals(next)){
                                iterator.remove();
                                urlList.add(pos,url);
                                break;
                            }
                        }
                    }
                }
                Gson gson = new Gson();
                String cardUrlInfo = gson.toJson(urlList);
                Log.e("TAG",">>>>>>>" + cardUrlInfo);
                UserApi.upDateUserBusinessCard(cardUrlInfo, context, new requestCallback() {
                    @Override
                    public void onSuccess(int code, Object object) {
                        dismissProgress();
                        if (code == Constants.SUCCESS_CODE){
                            pagerAdapter.notifyDataSetChanged();
                            toast("上传成功");
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
        });
    }

    private Runnable onDownLoadSuccessRunnable = new Runnable() {
        @Override
        public void run() {
            dismissProgress();
            ToastHelper.showToastLong(context, getString(R.string.picture_save_to));
        }
    };

    private Runnable onDownLoadFailedRunnable = new Runnable() {
        @Override
        public void run() {
            dismissProgress();
            ToastHelper.showToastLong(context, getString(R.string.picture_save_fail));
        }
    };

    private Runnable onDistinguishFailedRunnable = new Runnable() {
        @Override
        public void run() {
            dismissProgress();
            ToastHelper.showToastLong(context, "未识别类型");
        }
    };

    private Runnable onStateWeChatRunnable = new Runnable() {
        @Override
        public void run() {
            dismissProgress();
            ToastHelper.showToastLong(context, "无法跳转到微信，请检查您是否安装了微信！");
        }
    };

    private Runnable onStateAliPayRunnable = new Runnable() {
        @Override
        public void run() {
            dismissProgress();
            ToastHelper.showToastLong(context, "无法跳转到支付宝，请检查您是否安装了支付宝！");
        }
    };
}

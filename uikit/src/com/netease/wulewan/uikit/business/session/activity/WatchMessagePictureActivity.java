package com.netease.wulewan.uikit.business.session.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.wulewan.uikit.R;
import com.netease.wulewan.uikit.business.session.viewholder.IMsgImage;
import com.netease.wulewan.uikit.common.ToastHelper;
import com.netease.wulewan.uikit.common.activity.UI;
import com.netease.wulewan.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.wulewan.uikit.common.ui.widget.ViewPagerFixed;
import com.netease.wulewan.uikit.common.util.C;
import com.netease.wulewan.uikit.common.util.file.AttachmentStore;
import com.netease.wulewan.uikit.common.util.media.BitmapDecoder;
import com.netease.wulewan.uikit.common.util.media.ImageUtil;
import com.netease.wulewan.uikit.common.util.storage.StorageUtil;
import com.netease.wulewan.uikit.common.util.sys.TimeUtil;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * 查看聊天消息原图
 * Created by huangjun on 2015/3/6.
 */
public class WatchMessagePictureActivity extends UI {

    private static final String TAG = WatchMessagePictureActivity.class.getSimpleName();
    private static final String INTENT_EXTRA_IMAGE = "INTENT_EXTRA_IMAGE";
    private static final String INTENT_EXTRA_MENU = "INTENT_EXTRA_MENU";

    private static final int MODE_NOMARL = 0;
    private static final int MODE_GIF = 1;

    private Handler handler;
    private IMMessage message;
    private boolean isShowMenu;
    private List<IMMessage> imageMsgList = new ArrayList<>();
    private int firstDisplayImageIndex = 0;

    private boolean newPageSelected = false;

//    private View loadingLayout;

    private ImageView simpleImageView;
    private int mode;
    protected CustomAlertDialog alertDialog;
    private ViewPagerFixed imageViewPager;
    private PagerAdapter adapter;
    private SparseArray<SoftReference<View>> cacheView;
    private ImageView show_list;

    public static void start(Context context, IMMessage message) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE, message);
        intent.setClass(context, WatchMessagePictureActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, IMMessage message, boolean isShowMenu) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE, message);
        intent.putExtra(INTENT_EXTRA_MENU, isShowMenu);
        intent.setClass(context, WatchMessagePictureActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_watch_picture_activity);

        onInitSetBack(this);
        onInitSetTitle(this, "图片");
        onInitRightSure(this, 0, "图片列表", 15);
        handleIntent();

        initActionbar();
        findViews();

        loadMsgAndDisplay();

        handler = new Handler();
    }

    private void handleIntent() {
        this.message = (IMMessage) getIntent().getSerializableExtra(INTENT_EXTRA_IMAGE);
        mode = ImageUtil.isGif(((FileAttachment) message.getAttachment()).getExtension()) ? MODE_GIF : MODE_NOMARL;
        setTitle(message);
        isShowMenu = getIntent().getBooleanExtra(INTENT_EXTRA_MENU, true);
    }

    @Override
    protected void onDestroy() {
        imageViewPager.setAdapter(null);
        super.onDestroy();
    }

    private void setTitle(IMMessage message) {
        if (message == null) {
            return;
        }
        super.setTitle(String.format("图片发送于%s", TimeUtil.getDateString(message.getTime())));
    }

    private void initActionbar() {
//        TextView menuBtn = findView(R.id.actionbar_menu);
        show_list = findView(R.id.show_list);
        if (isShowMenu) {
            show_list.setVisibility(View.VISIBLE);
            show_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WatchPicAndVideoMenuActivity.startActivity(WatchMessagePictureActivity.this, message);

                }
            });
        } else {
            show_list.setVisibility(View.GONE);
        }
    }

    private void findViews() {
        alertDialog = new CustomAlertDialog(this);
//        loadingLayout = findViewById(R.id.loading_layout);

        imageViewPager = (ViewPagerFixed) findViewById(R.id.view_pager_image);
        simpleImageView = (ImageView) findViewById(R.id.simple_image_view);

        if (mode == MODE_GIF) {
            simpleImageView.setVisibility(View.VISIBLE);
            simpleImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    if (isOriginImageHasDownloaded(message)) {
//                        showWatchPictureAction();
//                    }
                    return true;
                }
            });

            imageViewPager.setVisibility(View.GONE);
        } else if (mode == MODE_NOMARL) {
            simpleImageView.setVisibility(View.GONE);
            imageViewPager.setVisibility(View.VISIBLE);
        }
        imageViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("点击");
            }
        });
        imageViewPager.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast("changan");
                return true;
            }
        });
    }

    // 加载并显示
    private void loadMsgAndDisplay() {
        if (mode == MODE_NOMARL) {
            if (message.getSessionId().equals("0"))
            {
                imageMsgList.add(message);
                queryCustomMessages();
            }
            else {
                queryImageMessages();
            }
        } else if (mode == MODE_GIF) {
            displaySimpleImage();
        }
    }

    // 显示单个gif图片
    private void displaySimpleImage() {
        String path = ((FileAttachment) message.getAttachment()).getPath();
        String thumbPath = ((FileAttachment) message.getAttachment()).getThumbPath();
        String url = ((FileAttachment) message.getAttachment()).getUrl();
        if (!TextUtils.isEmpty(path)) {
            Glide.with(this).asGif().load(new File(path)).into(simpleImageView);
            return;
        }
        if (!TextUtils.isEmpty(thumbPath)) {
            Glide.with(this).asGif().load(new File(thumbPath)).into(simpleImageView);
            return;
        }

        Glide.with(this).asGif().load(url).into(simpleImageView);


    }


    // 查询并显示图片，带viewPager
    private void queryImageMessages() {
        IMMessage anchor = MessageBuilder.createEmptyMessage(message.getSessionId(), message.getSessionType(), 0);
        NIMClient.getService(MsgService.class).queryMessageListByType(MsgTypeEnum.image, anchor, Integer.MAX_VALUE).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> param) {
                for (IMMessage imMessage : param) {
                    if (!ImageUtil.isGif(((FileAttachment) imMessage.getAttachment()).getExtension())) {
                        imageMsgList.add(imMessage);
                    }
                }
                queryCustomMessages();
            }

            @Override
            public void onFailed(int code) {
                Log.i(TAG, "query msg by type failed, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }


    private void queryCustomMessages() {
        IMMessage anchor = MessageBuilder.createEmptyMessage(message.getSessionId(), message.getSessionType(), 0);
        NIMClient.getService(MsgService.class).queryMessageListByType(MsgTypeEnum.custom, anchor, Integer.MAX_VALUE).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> param) {
                for (IMMessage imMessage : param) {

                    if (imMessage.getAttachment() instanceof IMsgImage) {
                        imageMsgList.add(imMessage);
                    }
                }
                Collections.reverse(imageMsgList);
                setDisplayIndex();
                setViewPagerAdapter();
            }

            @Override
            public void onFailed(int code) {
                Log.i(TAG, "query msg by type failed, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {

            }
        });

    }

    // 设置第一个选中的图片index
    private void setDisplayIndex() {
        for (int i = 0; i < imageMsgList.size(); i++) {
            IMMessage imageObject = imageMsgList.get(i);
            if (compareObjects(message, imageObject)) {
                firstDisplayImageIndex = i;
                break;
            }
        }
    }

    protected boolean compareObjects(IMMessage t1, IMMessage t2) {
        return (t1.getUuid().equals(t2.getUuid()));
    }

    private void setViewPagerAdapter() {

        cacheView = new SparseArray<>(imageMsgList == null ? 0 : imageMsgList.size());

        adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return imageMsgList == null ? 0 : imageMsgList.size();
            }

            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View view = (View) object;
                container.removeView(view);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return (view == object);
            }



            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                final IMMessage msg = imageMsgList.get(position);
                final FileAttachment fileAttachment = (FileAttachment) msg.getAttachment();
                String thumbPath = fileAttachment.getThumbPath();
                String path = fileAttachment.getPath();
                final String url = fileAttachment.getUrl();

                View view = cacheView.get(position) != null ? cacheView.get(position).get() : null;
                if(view == null){

                    view = LayoutInflater.from(WatchMessagePictureActivity.this).inflate(R.layout.nim_image_layout_multi_touch, null);

                    final ViewHolder viewHolder = new ViewHolder();

                    viewHolder.image = view.findViewById(R.id.watch_image_view);
                    viewHolder.imgeShowOriginal = view.findViewById(R.id.show_original);

                    viewHolder.imgeShowOriginal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            showProgress(WatchMessagePictureActivity.this,false);

                            NIMClient.getService(MsgService.class).downloadAttachment(msg, false).setCallback(new RequestCallback() {
                                @Override
                                public void onSuccess(Object o) {
                                    dismissProgress();
                                    Bitmap bitmap = null;
                                    bitmap = BitmapDecoder.decodeSampledForDisplay(fileAttachment.getPathForSave());
                                    bitmap = ImageUtil.rotateBitmapInNeeded(fileAttachment.getPathForSave(), bitmap);
                                    viewHolder.image.setImageBitmap(bitmap);
                                    viewHolder.photoViewAttacher.update();
                                    fileAttachment.setPath(fileAttachment.getPathForSave());
                                    viewHolder.imgeShowOriginal.setVisibility(View.GONE);

                                }

                                @Override
                                public void onFailed(int i) {
                                    dismissProgress();
                                }

                                @Override
                                public void onException(Throwable throwable) {
                                    dismissProgress();
                                }
                            });

                        }
                    });

                    viewHolder.photoViewAttacher = new PhotoViewAttacher(viewHolder.image);

                    viewHolder.photoViewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {
                            finish();
                        }
                    });

                    viewHolder.photoViewAttacher.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            /*if (isOriginImageHasDownloaded(msg)){
                                //已下载原图
                                showWatchPictureAction(msg);
                            }*/
                            showWatchPictureAction(msg);
                            return true;
                        }
                    });

                    view.setTag(viewHolder);
                    cacheView.put(position,new SoftReference<>(view));

                }

                final ViewHolder viewHolder = (ViewHolder) view.getTag();

                setTitle(imageMsgList.get(position));


                Bitmap bitmap = null;

                if (!TextUtils.isEmpty(path)) {
                    bitmap = BitmapDecoder.decodeSampledForDisplay(path);
                    bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
                    viewHolder.imgeShowOriginal.setVisibility(View.GONE);

                }else{
                    viewHolder.imgeShowOriginal.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(thumbPath)) {
                        bitmap = BitmapDecoder.decodeSampledForDisplay(thumbPath);
                        bitmap = ImageUtil.rotateBitmapInNeeded(thumbPath, bitmap);
                    }
                }

                if (bitmap != null) {
                    viewHolder.image.setImageBitmap(bitmap);
                    viewHolder.photoViewAttacher.update();

                }else{

                    showProgress(WatchMessagePictureActivity.this,false);

                    NIMClient.getService(MsgService.class).downloadAttachment(msg, false).setCallback(new RequestCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            dismissProgress();
                            Bitmap bitmap = null;
                            bitmap = BitmapDecoder.decodeSampledForDisplay(fileAttachment.getPathForSave());
                            bitmap = ImageUtil.rotateBitmapInNeeded(fileAttachment.getPathForSave(), bitmap);
                            viewHolder.image.setImageBitmap(bitmap);
                            viewHolder.photoViewAttacher.update();
                            fileAttachment.setPath(fileAttachment.getPathForSave());
                            viewHolder.imgeShowOriginal.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailed(int i) {
                            dismissProgress();
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            dismissProgress();
                        }
                    });
                }

                container.addView(view);
                return view;
            }


        };

        imageViewPager.setAdapter(adapter);
        imageViewPager.setOffscreenPageLimit(2);
        imageViewPager.setCurrentItem(firstDisplayImageIndex);
        imageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset == 0f && newPageSelected) {
                    newPageSelected = false;

                }
            }

            @Override
            public void onPageSelected(int position) {
                newPageSelected = true;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }




    private boolean isOriginImageHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((ImageAttachment) message.getAttachment()).getPath())) {
            return true;
        }

        return false;
    }

    // 图片长按
    protected void showWatchPictureAction(final IMMessage msg) {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
            return;
        }
        alertDialog.clearData();
        String path = "";
        if (msg.getAttachment() instanceof ImageAttachment){
            path = ((ImageAttachment) msg.getAttachment()).getPath();
        }else {
            path = ((FileAttachment) msg.getAttachment()).getPath();
        }

        if (TextUtils.isEmpty(path)) {
            return;
        }
        String title;
        if (!TextUtils.isEmpty(path)) {
            title = getString(R.string.save_to_device);
            alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {

                @Override
                public void onClick() {
                    savePicture(msg);
                }
            });
        }
        alertDialog.show();
    }




    private int getImageResOnLoading() {
        return R.drawable.nim_image_default;
    }








    // 保存图片
    public void savePicture(IMMessage msg) {
        FileAttachment attachment = (FileAttachment) msg.getAttachment();
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
            return;
        }

        String srcFilename = attachment.getFileName();
        //默认jpg
        String extension = TextUtils.isEmpty(attachment.getExtension()) ? "jpg" : attachment.getExtension();
        srcFilename += ("." + extension);

        String picPath = StorageUtil.getSystemImagePath();
        String dstPath = picPath + srcFilename;
        if (AttachmentStore.copy(path, dstPath) != -1) {
            try {
                ContentValues values = new ContentValues(2);
                values.put(MediaStore.Images.Media.MIME_TYPE, C.MimeType.MIME_JPEG);
                values.put(MediaStore.Images.Media.DATA, dstPath);
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                ToastHelper.showToastLong(WatchMessagePictureActivity.this, getString(R.string.picture_save_to));
            } catch (Exception e) {
                // may be java.lang.UnsupportedOperationException
                ToastHelper.showToastLong(WatchMessagePictureActivity.this, getString(R.string.picture_save_fail));
            }
        } else {
            ToastHelper.showToastLong(WatchMessagePictureActivity.this, getString(R.string.picture_save_fail));
        }
    }


    /**
     * Cache ViewPager ViewHolder
     */
    private class ViewHolder{
        ImageView image;
        TextView imgeShowOriginal;

        PhotoViewAttacher photoViewAttacher;
    }
}

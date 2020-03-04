package com.netease.yqbj.uikit.common.media.picker.loader;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.netease.yqbj.uikit.api.NimUIKit;

public class PickerImageLoader {
    public static void initCache() {
    }

    public static void clearCache() {
    }

    public static void display(final String thumbPath, final String originalPath, final ImageView imageView, final int defResource) {

        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .placeholder(defResource)
                .error(defResource)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new RotateTransformation(NimUIKit.getContext(), originalPath));

        Glide.with(NimUIKit.getContext())
                .asBitmap()
                .load(thumbPath)
                .apply(requestOptions)
                .into(imageView);
    }
}
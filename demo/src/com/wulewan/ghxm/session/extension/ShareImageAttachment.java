package com.wulewan.ghxm.session.extension;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.wulewan.ghxm.R;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.business.session.viewholder.IMsgImage;

public class ShareImageAttachment extends FileAttachment implements IMsgImage {

    private String appIconUrl = "";
    private String appName = "";
    private Integer width = 500;
    private Integer height = 300;
    private static final String KEY_PATH = "path";
    private static final String KEY_SIZE = "size";
    private static final String KEY_MD5 = "md5";
    private static final String KEY_URL = "url";

    public ShareImageAttachment(JSONObject data) {
        parseData(data);
    }

    public Boolean isErrorData(){
        if (appIconUrl == null || appName == null || width == null || height == null)
        {
            return true;
        }
        return false;
    }

    private void parseData(JSONObject data) {
        appIconUrl = data.getString("appIconUrl");
        appName = data.getString("appName");

        path = data.getString(KEY_PATH);
        md5 = data.getString(KEY_MD5);
        url = data.getString(KEY_URL);
        size = data.containsKey(KEY_SIZE) ? data.getLong(KEY_SIZE) : 0;

        width = data.getInteger("width");
        if (width == null)
        {
            width = 500;
        }
        height = data.getInteger("height");
        if (height == null)
        {
            height = 300;
        }
    }

    protected JSONObject packData() {

        JSONObject data = new JSONObject();
        data.put("appIconUrl", appIconUrl);
        data.put("appName", appName);
        data.put("width", width);
        data.put("height", height);
        return data;
    }

    @Override
    public String toJson(boolean send) {
        JSONObject data = packData();
        try {
            if (!send && !TextUtils.isEmpty(path)) {
                data.put(KEY_PATH, path);
            }

            if (!TextUtils.isEmpty(md5)) {
                data.put(KEY_MD5, md5);
            }
            if (!TextUtils.isEmpty(url)) {
                data.put(KEY_URL, url);
            }
            data.put(KEY_SIZE, size);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CustomAttachParser.packData(CustomAttachmentType.ShareImage, data);
    }

    public String getAppName() {
        return appName;
    }

    public String getAppIconUrl() {
        return appIconUrl;
    }

    public int getWidth(){
     return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDesc() {
        return NimUIKit.getContext().getString(R.string.share_img);
    }



}

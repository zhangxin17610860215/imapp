package com.yqbj.ghxm.session.extension;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.yqbj.ghxm.R;
import com.netease.yqbj.uikit.api.NimUIKit;

public class ShareCardAttachment extends CustomAttachment {


    // appicon图片链接
    private String appIconUrl = "";
    private String appName = "";

    private String title = "";
    private String content = "";
    private String url = "";


    public ShareCardAttachment() {
        super(CustomAttachmentType.ShareCard);
    }


    @Override
    protected void parseData(JSONObject data) {
        appIconUrl = data.getString("appIconUrl");
        appName = data.getString("appName");
        title = data.getString("title");
        content = data.getString("content");
        url = data.getString("url");
    }

    @Override
    protected JSONObject packData() {

        JSONObject data = new JSONObject();
        data.put("appIconUrl", appIconUrl);
        data.put("appName", appName);
        data.put("title", title);
        data.put("content", content);
        data.put("url", url);
        return data;
    }

    public Boolean isErrorData() {
        if (appIconUrl == null || appName == null || title == null || content == null || url == null) {
            return true;
        }
        return false;
    }

    public String getAppName() {
        return appName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public String getAppIconUrl() {
        return appIconUrl;
    }

    public String getDesc() {
        if (title == null) {
            title = "";
        }
        return NimUIKit.getContext().getString(R.string.share_url) + " " + title;
    }
}

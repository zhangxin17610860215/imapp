package com.yqbj.ghxm.contact;

import android.content.Context;

import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.contact.ContactEventListener;
import com.yqbj.ghxm.contact.activity.UserProfileActivity;

/**
 * UIKit联系人列表定制展示类
 * <p/>
 * Created by huangjun on 2015/9/11.
 */
public class ContactHelper {

    public static void init() {
        setContactEventListener();
    }

    private static void setContactEventListener() {
        NimUIKit.setContactEventListener(new ContactEventListener() {
            @Override
            public void onItemClick(Context context, String account) {
                UserProfileActivity.start(context, account);
            }

            @Override
            public void onItemLongClick(Context context, String account) {

            }

            @Override
            public void onAvatarClick(Context context, String account) {
                UserProfileActivity.start(context, account);
            }
        });
    }

}

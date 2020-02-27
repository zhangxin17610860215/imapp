package com.netease.wulewan.uikit.business.contact.core.provider;

import android.content.Context;
import android.util.Log;

import com.netease.wulewan.uikit.business.contact.core.item.AbsContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ItemTypes;
import com.netease.wulewan.uikit.business.contact.core.model.MobileInfo;
import com.netease.wulewan.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.wulewan.uikit.business.contact.core.query.TextQuery;
import com.netease.wulewan.uikit.business.contact.core.util.ContactHelper;
import com.netease.wulewan.uikit.business.contact.core.util.ContactUtil;
import com.netease.wulewan.uikit.common.util.log.LogUtil;
import com.netease.wulewan.uikit.impl.cache.UIKitLogTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MobileDataProvider implements IContactDataProvider {
    private Context mContext;

    public MobileDataProvider(Context context){
        this.mContext = context;
    }

    @Override
    public List<AbsContactItem> provide(TextQuery query) {
        List<MobileInfo> sources = query(query);
        List<AbsContactItem> items = new ArrayList<>(sources.size());
        for (MobileInfo u : sources) {
            items.add(new ContactItem(ContactHelper.makeContactFromMobileInfo(u), ItemTypes.MOBILE));
        }

        LogUtil.e(UIKitLogTag.CONTACT, "contact provide data size =" + items.size());
        return items;
    }

    private final List<MobileInfo> query(TextQuery query) {

        ContactUtil contactUtil = new ContactUtil(mContext);
        List<MobileInfo> users = new ArrayList<>();
        try{
            users = contactUtil.getContactInfo();
        }catch (Exception e){

        }


        Log.e("users",users.size()+"");
        if (query == null) {
            return users;
        }

        MobileInfo user;
        for (Iterator<MobileInfo> iter = users.iterator(); iter.hasNext(); ) {
            user = iter.next();
            boolean hit = ContactSearch.hitMobile(user, query);
            if (!hit) {
                iter.remove();
            }
        }

        Log.e("users after",users.size()+"");
        return users;
    }
}

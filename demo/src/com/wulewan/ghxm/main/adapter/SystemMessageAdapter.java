package com.wulewan.ghxm.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.netease.wulewan.uikit.common.adapter.TAdapter;
import com.netease.wulewan.uikit.common.adapter.TAdapterDelegate;
import com.wulewan.ghxm.main.viewholder.SystemMessageViewHolder;

import java.util.List;

public class SystemMessageAdapter extends TAdapter {

    private SystemMessageViewHolder.SystemMessageListener systemMessageListener;

    public SystemMessageAdapter(Context context, List<?> items, TAdapterDelegate delegate,
                                SystemMessageViewHolder.SystemMessageListener listener) {
        super(context, items, delegate);
        this.systemMessageListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (systemMessageListener != null) {
            ((SystemMessageViewHolder) view.getTag()).setListener(systemMessageListener);
        }

        return view;
    }
}

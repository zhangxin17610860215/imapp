package com.netease.wulewan.uikit.business.team.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.netease.wulewan.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.wulewan.uikit.common.adapter.IScrollStateListener;
import com.netease.wulewan.uikit.common.adapter.TAdapter;
import com.netease.wulewan.uikit.common.adapter.TAdapterDelegate;
import com.netease.wulewan.uikit.common.util.log.LogUtil;

import java.util.List;

public class TeamMemberAdapter extends TAdapter {

    /**
     * 当前GridView显示模式：显示讨论组成员，正在移除讨论组成员
     */
    public static enum Mode {
        NORMAL,
        DELETE
    }

    /**
     * 每个Item的类型：讨论组成员，添加成员，移除成员
     */
    public static enum TeamMemberItemTag {
        NORMAL,
        ADD,
        DELETE
    }

    /**
     * GridView数据项
     */
    public static class TeamMemberItem {
        private TeamMemberItemTag tag;
        private String tid;
        private String account;
        private String desc;

        public TeamMemberItem(TeamMemberItemTag tag, String tid, String account, String desc) {
            this.tag = tag;
            this.tid = tid;
            this.account = account;
            this.desc = desc;
        }

        public TeamMemberItemTag getTag() {
            return tag;
        }

        public String getTid() {
            return tid;
        }

        public String getDesc() {
            return desc;
        }

        public String getAccount() {
            return account;
        }
    }

    /**
     * 群成员移除回调函数
     */
    public static interface RemoveMemberCallback {
        public void onRemoveMember();
    }

    public static interface AddMemberCallback {
        public void onAddMember();
    }

    public String teamId;

    private Context context;

    private Mode mode = Mode.NORMAL;

    private RemoveMemberCallback removeMemberCallback;

    private AddMemberCallback addMemberCallback;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean switchMode() {
        if (getMode() == Mode.DELETE) {
            setMode(Mode.NORMAL);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public RemoveMemberCallback getRemoveMemberCallback() {
        return removeMemberCallback;
    }

    public AddMemberCallback getAddMemberCallback() {
        return addMemberCallback;
    }

    public TeamMemberAdapter(String teamId, Context context, List<?> items, TAdapterDelegate delegate,
                             RemoveMemberCallback removeMemberCallback, AddMemberCallback addMemberCallback) {
        super(context, items, delegate);
        this.teamId = teamId;
        this.context = context;
        this.removeMemberCallback = removeMemberCallback;
        this.addMemberCallback = addMemberCallback;
    }

    private TeamMemberHolder.TeamMemberHolderEventListener teamMemberHolderEventListener;

    public void setEventListener(TeamMemberHolder.TeamMemberHolderEventListener eventListener) {
        this.teamMemberHolderEventListener = eventListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (teamMemberHolderEventListener != null) {
            ((TeamMemberHolder) view.getTag()).setEventListener(teamMemberHolderEventListener);
        }

        return view;
    }


    public View getView(final int position, View convertView, ViewGroup parent, boolean needRefresh) {

        convertView = viewAtPosition(position);
        TeamMemberHolder holder = (TeamMemberHolder) convertView.getTag();
        holder.setPosition(position);
        if (needRefresh) {
            try {
                holder.refresh(getItem(position));
            } catch (RuntimeException e) {
                LogUtil.e("TAdapter", "refresh viewholder error. " + e.getMessage());
            }
        }

        if (holder instanceof IScrollStateListener) {
            listeners.add(holder);
        }

        return convertView;
    }
}

package com.yqbj.ghxm.main.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.main.activity.SystemMessageActivity;
import com.yqbj.ghxm.main.activity.TeamListActivity;
import com.yqbj.ghxm.main.helper.SystemMessageUnreadManager;
import com.yqbj.ghxm.main.reminder.ReminderId;
import com.yqbj.ghxm.main.reminder.ReminderItem;
import com.yqbj.ghxm.main.reminder.ReminderManager;
import com.netease.yqbj.uikit.business.contact.core.item.AbsContactItem;
import com.netease.yqbj.uikit.business.contact.core.item.ItemTypes;
import com.netease.yqbj.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.yqbj.uikit.business.contact.core.viewholder.AbsContactViewHolder;
import com.yqbj.ghxm.contact.activity.AddFriendActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FuncViewHolder extends AbsContactViewHolder<FuncViewHolder.FuncItem> implements ReminderManager.UnreadNumChangedCallback {

    private static ArrayList<WeakReference<ReminderManager.UnreadNumChangedCallback>> sUnreadCallbackRefs = new ArrayList<>();

    private ImageView image;
    private TextView funcName;
    private TextView unreadNum;
    private View bottomLine;
    private Set<ReminderManager.UnreadNumChangedCallback> callbacks = new HashSet<>();

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.func_contacts_item, null);
        this.image = view.findViewById(R.id.img_head);
        this.funcName = view.findViewById(R.id.tv_func_name);
        this.unreadNum = view.findViewById(R.id.tab_new_msg_label);
        this.bottomLine = view.findViewById(R.id.bottomLine);
        return view;
    }

    @Override
    public void refresh(ContactDataAdapter contactAdapter, int position, FuncItem item) {
        if (item == FuncItem.VERIFY) {
            funcName.setText(context.getString(R.string.verify_reminder));
            image.setImageResource(R.mipmap.new_friend);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            int unreadCount = SystemMessageUnreadManager.getInstance().getSysMsgUnreadCount();
            updateUnreadNum(unreadCount);
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
            sUnreadCallbackRefs.add(new WeakReference<ReminderManager.UnreadNumChangedCallback>(this));
//        } else if (item == FuncItem.ROBOT) {
//            funcName.setText("智能机器人");
//            image.setImageResource(R.drawable.ic_robot);
//        } else if (item == FuncItem.NORMAL_TEAM) {
//            funcName.setText("讨论组");
//            image.setImageResource(R.drawable.ic_secretary);
        } else if (item == FuncItem.ADD_FRIEND) {
            funcName.setText("添加朋友");
            image.setImageResource(R.mipmap.add_friend);
        } else if (item == FuncItem.ADVANCED_TEAM) {
            funcName.setText(context.getString(R.string.save_group));
            image.setImageResource(R.mipmap.save_group);
            bottomLine.setVisibility(View.INVISIBLE);
//        } else if (item == FuncItem.BLACK_LIST) {
//            funcName.setText("黑名单");
//            image.setImageResource(R.drawable.ic_black_list);
//        } else if (item == FuncItem.MY_COMPUTER) {
//            funcName.setText("我的电脑");
//            image.setImageResource(R.drawable.ic_my_computer);
        }

        if (item != FuncItem.VERIFY) {
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            unreadNum.setVisibility(View.GONE);
        }
    }


    private void updateUnreadNum(int unreadCount) {
        // 2.*版本viewholder复用问题
        if (unreadCount > 0 && funcName.getText().toString().equals(context.getString(R.string.verify_reminder))) {
            unreadNum.setVisibility(View.VISIBLE);
            unreadNum.setText("" + unreadCount);
        } else {
            unreadNum.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        if (item.getId() != ReminderId.CONTACT) {
            return;
        }
        updateUnreadNum(item.getUnread());
    }

    public static void unRegisterUnreadNumChangedCallback() {
        Iterator<WeakReference<ReminderManager.UnreadNumChangedCallback>> iter = sUnreadCallbackRefs.iterator();
        while (iter.hasNext()) {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(iter.next().get());
            iter.remove();
        }
    }


    public final static class FuncItem extends AbsContactItem {
        static final FuncItem VERIFY = new FuncItem();
        //        static final FuncItem ROBOT = new FuncItem();
//        static final FuncItem NORMAL_TEAM = new FuncItem();
        static final FuncItem ADD_FRIEND = new FuncItem();
        static final FuncItem ADVANCED_TEAM = new FuncItem();
//        static final FuncItem BLACK_LIST = new FuncItem();
//        static final FuncItem MY_COMPUTER = new FuncItem();

        @Override
        public int getItemType() {
            return ItemTypes.FUNC;
        }

        @Override
        public String belongsGroup() {
            return null;
        }


        public static List<AbsContactItem> provide() {
            List<AbsContactItem> items = new ArrayList<>();
            items.add(VERIFY);
//            items.add(ROBOT);
            items.add(ADD_FRIEND);
            items.add(ADVANCED_TEAM);
//            items.add(BLACK_LIST);
//            items.add(MY_COMPUTER);

            return items;
        }

        public static void handle(Context context, AbsContactItem item) {
            if (item == VERIFY) {
                SystemMessageActivity.start(context);
//            } else if (item == ROBOT) {
//                RobotListActivity.start(context);
//            } else if (item == NORMAL_TEAM) {
//                TeamListActivity.start(context, ItemTypes.TEAMS.NORMAL_TEAM)
            } else if (item == ADD_FRIEND) {
                AddFriendActivity.start(context);
            } else if (item == ADVANCED_TEAM) {
                TeamListActivity.start(context, ItemTypes.TEAMS.ADVANCED_TEAM);
//            } else if (item == MY_COMPUTER) {
//                SessionHelper.startP2PSession(context, DemoCache.getAccount());
//            } else if (item == BLACK_LIST) {
//                BlackListActivity.start(context);
            }
        }
    }
}

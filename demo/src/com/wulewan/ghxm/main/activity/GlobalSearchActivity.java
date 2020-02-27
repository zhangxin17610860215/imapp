package com.wulewan.ghxm.main.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nimlib.sdk.search.model.MsgIndexRecord;
import com.netease.wulewan.uikit.business.contact.core.item.AbsContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ItemTypes;
import com.netease.wulewan.uikit.business.contact.core.item.MsgItem;
import com.netease.wulewan.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.wulewan.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.wulewan.uikit.business.contact.core.provider.ContactDataProvider;
import com.netease.wulewan.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.wulewan.uikit.business.contact.core.viewholder.ContactHolder;
import com.netease.wulewan.uikit.business.contact.core.viewholder.LabelHolder;
import com.netease.wulewan.uikit.business.contact.core.viewholder.MsgHolder;
import com.netease.wulewan.uikit.common.activity.UIDragLess;
import com.netease.wulewan.uikit.common.util.string.StringUtil;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.session.SessionHelper;
import com.wulewan.ghxm.session.search.DisplayMessageActivity;

/**
 * 全局搜索页面
 * 支持通讯录搜索、消息全文检索
 * <p/>
 * Created by huangjun on 2015/4/13.
 */
public class GlobalSearchActivity extends UIDragLess implements OnItemClickListener {

    private ContactDataAdapter adapter;

    private ListView lvContacts;

    private SearchView searchView;
//    private SearchView searchView2;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, GlobalSearchActivity.class);
        context.startActivity(intent);
    }

//    public boolean onCreateOptionsMenu(android.view.Menu menu) {
//        getMenuInflater().inflate(R.menu.global_search_menu, menu);
//        final MenuItem item = menu.findItem(R.id.action_search);
//
//        getHandler().post(new Runnable() {
//            @Override
//            public void run() {
//                MenuItemCompat.expandActionView(item);
//            }
//        });
//
//        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem menuItem) {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
//                finish();
//
//                return false;
//            }
//        });
//
//        searchView2 = (SearchView) MenuItemCompat.getActionView(item);
//
//        searchView2.setOnQueryTextListener(new OnQueryTextListener() {
//
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                showKeyboard(false);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                if (StringUtil.isEmpty(query)) {
//                    lvContacts.setVisibility(View.GONE);
//                } else {
//                    lvContacts.setVisibility(View.VISIBLE);
//                }
//                adapter.query(query);
//                return true;
//            }
//        });
//        return true;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.global_search_result);
//
//        ToolBarOptions options = new NimToolBarOptions();
//        setToolBar(R.id.toolbar, options);

        lvContacts = findViewById(R.id.searchResultList);
        lvContacts.setVisibility(View.GONE);
        SearchGroupStrategy searchGroupStrategy = new SearchGroupStrategy();
        IContactDataProvider dataProvider = new ContactDataProvider(ItemTypes.FRIEND, ItemTypes.TEAM, ItemTypes.MSG);

        adapter = new ContactDataAdapter(this, searchGroupStrategy, dataProvider);
        adapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        adapter.addViewHolder(ItemTypes.FRIEND, ContactHolder.class);
        adapter.addViewHolder(ItemTypes.TEAM, ContactHolder.class);
        adapter.addViewHolder(ItemTypes.MSG, MsgHolder.class);

        lvContacts.setAdapter(adapter);
        lvContacts.setOnItemClickListener(this);
        lvContacts.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                showKeyboard(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        findViewById(R.id.global_search_root).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    finish();
                    return true;
                }
                return false;
            }
        });

        searchView = findView(R.id.searchView);
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        final SearchView.SearchAutoComplete textView = searchView.findViewById(R.id.search_src_text);
        if (textView != null) textView.setTextColor(Color.parseColor("#959595"));
        ImageView closeBtn = searchView.findViewById(R.id.search_close_btn);
        if (closeBtn != null) closeBtn.setVisibility(View.INVISIBLE);

        final TextView textClearTxt = findView(R.id.text_clear_txt);
        textClearTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });

        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                showKeyboard(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (StringUtil.isEmpty(query)) {
                    lvContacts.setVisibility(View.GONE);
                    textClearTxt.setVisibility(View.INVISIBLE);
                } else {
                    lvContacts.setVisibility(View.VISIBLE);
                    textClearTxt.setVisibility(View.VISIBLE);
                }
                adapter.query(query);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    private static class SearchGroupStrategy extends ContactGroupStrategy {
        public static final String GROUP_FRIEND = "FRIEND";
        public static final String GROUP_TEAM = "TEAM";
        public static final String GROUP_MSG = "MSG";

        SearchGroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, 0, "");
            add(GROUP_TEAM, 1, "群组");
            add(GROUP_FRIEND, 2, "好友");
            add(GROUP_MSG, 3, "聊天记录");
        }

        @Override
        public String belongs(AbsContactItem item) {
            switch (item.getItemType()) {
                case ItemTypes.FRIEND:
                    return GROUP_FRIEND;
                case ItemTypes.TEAM:
                    return GROUP_TEAM;
                case ItemTypes.MSG:
                    return GROUP_MSG;
                default:
                    return null;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final AbsContactItem item = (AbsContactItem) adapter.getItem(position);
        switch (item.getItemType()) {
            case ItemTypes.TEAM: {
                SessionHelper.startTeamSession(GlobalSearchActivity.this, ((ContactItem) item).getContact().getContactId());
                break;
            }

            case ItemTypes.FRIEND: {
                SessionHelper.startP2PSession(this, ((ContactItem) item).getContact().getContactId());
                break;
            }

            case ItemTypes.MSG: {
                MsgIndexRecord msgIndexRecord = ((MsgItem) item).getRecord();
                if (msgIndexRecord.getCount() > 1) {
                    GlobalSearchDetailActivity2.start(this, msgIndexRecord);
                } else {
                    DisplayMessageActivity.start(this, msgIndexRecord.getMessage());
                }
                break;
            }

            default:
                break;
        }
    }

}

package com.wulewan.ghxm.session.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.wulewan.ghxm.R;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.business.uinfo.UserInfoHelper;
import com.netease.wulewan.uikit.common.activity.UIDragLess;
import com.netease.wulewan.uikit.common.ui.listview.AutoRefreshListView;
import com.netease.wulewan.uikit.common.util.string.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoujianghua on 2015/7/7.
 */
public class SearchMessageActivity extends UIDragLess {

    private static final String INTENT_EXTRA_UID = "intent_extra_uid";
    private static final String INTENT_EXTRA_SESSION_TYPE = "intent_extra_session_type";
    public static final String EDITORINFO = "editor_info";
    public static final String QUERYHINT = "Query_hint";

    private static final int SEARCH_COUNT = 20;

    public static final void start(Context context, String sessionId, SessionTypeEnum sessionType, int editorInfo, String queryHint) {
        Intent intent = new Intent();
        intent.setClass(context, SearchMessageActivity.class);
        intent.putExtra(INTENT_EXTRA_UID, sessionId);
        intent.putExtra(INTENT_EXTRA_SESSION_TYPE, sessionType);
        intent.putExtra(EDITORINFO, editorInfo);
        intent.putExtra(QUERYHINT, queryHint);
        context.startActivity(intent);
    }

    private SearchView searchView;
    private AutoRefreshListView searchResultListView;
    private List<IMMessage> searchResultList = new ArrayList<IMMessage>();
    private SearchMessageAdapter adapter;

    private boolean searching;
    private String pendingText;

    private String sessionId;
    private SessionTypeEnum sessionType;

    // 转为群组类型提供
    private List<TeamMember> members;

    private IMMessage emptyMsg;

    private SearchView.OnQueryTextListener mlistener;

    private int editorInfo;

    private String queryHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message_search_activity);
//
//        ToolBarOptions options = new NimToolBarOptions();
//        setToolBar(R.id.toolbar, options);

        findViewById(R.id.global_search_root).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!searching) {
                        finish();
                        return true;
                    }
                }
                return false;
            }
        });

        initSearchListView();
        handleIntent();
        initSearchView();
    }

    private void initSearchView() {
        searchView = findView(R.id.searchView);
        searchView.onActionViewExpanded();
        final SearchView.SearchAutoComplete textView = searchView.findViewById(R.id.search_src_text);
        searchView.setInputType(editorInfo);
        if (!TextUtils.isEmpty(queryHint))
            searchView.setQueryHint(queryHint);
        if (textView != null) textView.setTextColor(Color.parseColor("#959595"));
        final TextView textClearTxt = findView(R.id.text_clear_txt);
        textClearTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {

                if (TextUtils.isEmpty(sessionId) && sessionType == null) {
                    returnWord(text);
                } else {
                    searchByKeyword(text);
                }

                showKeyboard(false);

                if (mlistener != null) {
                    mlistener.onQueryTextSubmit(text);
                }

                return true;

            }

            @Override
            public boolean onQueryTextChange(String text) {
                searchByKeyword(text);
                if (StringUtil.isEmpty(text)) {
                    textClearTxt.setVisibility(View.INVISIBLE);
                } else {
                    textClearTxt.setVisibility(View.VISIBLE);
                }

                if (mlistener != null) {
                    mlistener.onQueryTextChange(text);
                }
                return true;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        sessionId = getIntent().getStringExtra(INTENT_EXTRA_UID);
        sessionType = (SessionTypeEnum) getIntent().getSerializableExtra(INTENT_EXTRA_SESSION_TYPE);
        editorInfo = getIntent().getIntExtra(EDITORINFO, EditorInfo.TYPE_CLASS_NUMBER);
        queryHint = getIntent().getStringExtra(QUERYHINT);
        reset();
    }

    private void initSearchListView() {
        searchResultListView = (AutoRefreshListView) findViewById(R.id.searchResultList);
        searchResultListView.setMode(AutoRefreshListView.Mode.END);
        searchResultListView.setVisibility(View.GONE);
        searchResultListView.setEmptyView(LayoutInflater.from(this).inflate(R.layout.message_search_empty_view, null));
        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IMMessage anchor = (IMMessage) searchResultListView.getAdapter().getItem(position);
                DisplayMessageActivity.start(SearchMessageActivity.this, anchor);

                showKeyboard(false);
            }
        });
        searchResultListView.addOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                showKeyboard(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
        searchResultListView.setOnRefreshListener(new AutoRefreshListView.OnRefreshListener() {
            @Override
            public void onRefreshFromStart() {
            }

            @Override
            public void onRefreshFromEnd() {
                showKeyboard(false);
                loadMoreSearchResult();
            }
        });

        adapter = new SearchMessageAdapter(this, searchResultList);
        searchResultListView.setAdapter(adapter);
    }

//    @Override
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
//                return false;
//            }
//        });
//
//        searchView = (SearchView) MenuItemCompat.getActionView(item);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//
//            @Override
//            public boolean onQueryTextSubmit(String text) {
//
//                if (TextUtils.isEmpty(sessionId) && sessionType==null){
//                    returnWord(text);
//                }else{
//                    searchByKeyword(text);
//                }
//
//                showKeyboard(false);
//
//                if (mlistener!=null){
//                    mlistener.onQueryTextSubmit(text);
//                }
//
//                return true;
//
//            }
//
//            @Override
//            public boolean onQueryTextChange(String text) {
//
//                if (!(TextUtils.isEmpty(sessionId) && sessionType==null)){
//                    searchByKeyword(text);
//                }
//
//                if(mlistener!=null){
//                    mlistener.onQueryTextChange(text);
//                }
//
//                return true;
//            }
//        });
//        return true;
//    }

    public void setSearchViewListener(SearchView.OnQueryTextListener mlistener) {
        this.mlistener = mlistener;
    }

    private void returnWord(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            searchResultListView.setVisibility(View.GONE);
            reset();
        } else if (TextUtils.isEmpty(keyword.trim())) {
            searchResultList.clear();
            adapter.notifyDataSetChanged();
            searchResultListView.setVisibility(View.VISIBLE);
        } else {


            String query = keyword.toLowerCase();

            Log.e("xxxxx", "ixixiixiixi");
            Intent i = new Intent();
            i.putExtra("result", keyword);
            setResult(RESULT_OK, i);
            finish();
            return;

        }
    }

    private void loadMoreSearchResult() {
        doSearch(searchView.getQuery().toString(), searchResultList.size() > 0);
    }

    private void searchByKeyword(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            searchResultListView.setVisibility(View.GONE);
            reset();
        } else if (TextUtils.isEmpty(keyword.trim())) {
            searchResultList.clear();
            adapter.notifyDataSetChanged();
            searchResultListView.setVisibility(View.VISIBLE);
        } else {
            doSearch(keyword, false);
        }
    }

    public void doSearch(final String keyword, final boolean append) {
        if (pend(keyword, append)) {
            return;
        }
        searching = true;
        String query = keyword.toLowerCase();


        IMMessage anchor = (append ? searchResultList.get(searchResultList.size() - 1) : emptyMsg);

        NIMClient.getService(MsgService.class).searchMessageHistory(keyword, filterAccounts(query), anchor, SEARCH_COUNT)
                .setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
                    @Override
                    public void onResult(int code, List<IMMessage> result, Throwable exception) {
                        searching = false;
                        if (result != null) {
                            searchResultListView.onRefreshComplete(result.size(), SEARCH_COUNT, true);

                            if (!onPend()) {
                                if (!append) {
                                    searchResultList.clear();
                                }
                                searchResultList.addAll(result);
                                adapter.setKeyword(keyword);
                                adapter.notifyDataSetChanged();
                                searchResultListView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showKeyboard(false);
    }


    @Override
    public void finish() {
        showKeyboard(false);
        super.finish();

    }

    private boolean pend(String query, boolean append) {
        if (searching && !append) {
            pendingText = query;
        }
        return searching;
    }

    private boolean onPend() {
        boolean reset = false;
        if (pendingText != null) {
            if (pendingText.length() == 0) {
                reset();
                reset = true;
            } else {
                doSearch(pendingText, false);
            }
            pendingText = null;
        }

        return reset;
    }

    private ArrayList<String> filterAccounts(String query) {
        ArrayList<String> filter = new ArrayList<String>();
        if (sessionType == SessionTypeEnum.Team) {
            if (members == null) {
                members = NimUIKit.getTeamProvider().getTeamMemberList(sessionId);
            }

            if (members != null) {
                for (TeamMember member : members) {
                    if (member == null) {
                        continue;
                    }
                    String account = member.getAccount();
                    if (match(member.getTeamNick(), query)) {
                        filter.add(account);
                        continue;
                    }
                    if (match(UserInfoHelper.getUserName(account), query)) {
                        filter.add(account);
                        continue;
                    }
                }
            }
        }
        return filter;
    }

    private boolean match(String source, String query) {
        if (TextUtils.isEmpty(source)) {
            return false;
        }

        return source.toLowerCase().contains(query);
    }

    private void reset() {
        searchResultList.clear();
        adapter.notifyDataSetChanged();

        emptyMsg = MessageBuilder.createEmptyMessage(sessionId, sessionType, 0);
    }
}

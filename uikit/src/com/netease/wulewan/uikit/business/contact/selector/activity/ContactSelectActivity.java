package com.netease.wulewan.uikit.business.contact.selector.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.wulewan.uikit.R;
import com.netease.wulewan.uikit.api.CustomEventManager;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.api.StatisticsConstants;
import com.netease.wulewan.uikit.api.wrapper.NimToolBarOptions;
import com.netease.wulewan.uikit.business.contact.core.item.AbsContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.wulewan.uikit.business.contact.core.item.ContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ContactItemFilter;
import com.netease.wulewan.uikit.business.contact.core.item.ItemTypes;
import com.netease.wulewan.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.wulewan.uikit.business.contact.core.model.IContact;
import com.netease.wulewan.uikit.business.contact.core.provider.ContactDataProvider;
import com.netease.wulewan.uikit.business.contact.core.provider.MobileDataProvider;
import com.netease.wulewan.uikit.business.contact.core.provider.TeamMemberDataProvider;
import com.netease.wulewan.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.wulewan.uikit.business.contact.core.query.TextQuery;
import com.netease.wulewan.uikit.business.contact.core.viewholder.LabelHolder;
import com.netease.wulewan.uikit.business.contact.selector.adapter.ContactSelectAdapter;
import com.netease.wulewan.uikit.business.contact.selector.adapter.ContactSelectAvatarAdapter;
import com.netease.wulewan.uikit.business.contact.selector.viewholder.ContactsMultiSelectHolder;
import com.netease.wulewan.uikit.business.contact.selector.viewholder.ContactsSelectHolder;
import com.netease.wulewan.uikit.common.ToastHelper;
import com.netease.wulewan.uikit.common.activity.ToolBarOptions;
import com.netease.wulewan.uikit.common.activity.UI;
import com.netease.wulewan.uikit.common.ui.liv.LetterIndexView;
import com.netease.wulewan.uikit.common.ui.liv.LivIndex;
import com.netease.wulewan.uikit.common.util.string.StringUtil;
import com.netease.wulewan.uikit.impl.cache.TeamDataCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 联系人选择器
 * <p/>
 * Created by huangjun on 2015/3/3.
 */
public class ContactSelectActivity extends UI implements View.OnClickListener, android.support.v7.widget.SearchView.OnQueryTextListener {

    public static final String EXTRA_DATA = "EXTRA_DATA"; // 请求数据：Option
    public static final String RESULT_DATA = "RESULT_DATA"; // 返回结果
    public static final String RESULT_NAME = "RESULT_NAME";


    // adapter

    private ContactSelectAdapter contactAdapter;

    private ContactSelectAvatarAdapter contactSelectedAdapter;

    // view


    private ListView listView;

    private LivIndex livIndex;

    private RelativeLayout bottomPanel;

    private HorizontalScrollView scrollViewSelected;

    private GridView imageSelectedGridView;

    private Button btnSelect;

    private SearchView searchView;

    // other

    private String queryText;

    private Option option;

    private int requestCode;

    // class

    private static class ContactsSelectGroupStrategy extends ContactGroupStrategy {
        public ContactsSelectGroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, -1, "");
            addABC(0);
        }
    }

    /**
     * 联系人选择器配置可选项
     */
    public enum ContactSelectType {
        BUDDY,
        TEAM_MEMBER,
        TEAM,
        // 最近会话
        RECENTLY,
        MOBILE,

    }


    /**
     * 选择器返回操作结果
     */
    public static enum SelectResultCode {
        // 选择
        SELECTED,
        // 创建新会话
        CREATESESSION,
        // 创建新会话群聊
        CREATETEAM,
        // 单选
        RADIO,
        // 多选
        MULTISELECT
    }


    public static class Option implements Serializable {

        /**
         * 联系人选择器中数据源类型：好友（默认）、群、群成员（需要设置teamId）
         */
        public ContactSelectType type = ContactSelectType.BUDDY;

        /**
         * 联系人选择器数据源类型为群成员时，需要设置群号
         */
        public String teamId = null;

        /**
         * 联系人选择器标题
         */
        public String title = "联系人选择器";

        /**
         * 联系人单选/多选（默认）
         */
        public boolean multi = true;

        /**
         * 至少选择人数
         */
        public int minSelectNum = 1;

        /**
         * 低于最少选择人数的提示
         */
        public String minSelectedTip = null;

        /**
         * 最大可选人数
         */
        public int maxSelectNum = 2000;

        /**
         * 超过最大可选人数的提示
         */
        public String maxSelectedTip = null;

        /**
         * 是否显示已选头像区域
         */
        public boolean showContactSelectArea = true;

        /**
         * 默认勾选（且可操作）的联系人项
         */
        public ArrayList<String> alreadySelectedAccounts = null;

        /**
         * 需要过滤（不显示）的联系人项
         */
        public ContactItemFilter itemFilter = null;

        /**
         * 需要disable(可见但不可操作）的联系人项
         */
        public ContactItemFilter itemDisableFilter = null;

        /**
         * 是否支持搜索
         */
        public boolean searchVisible = true;

        /**
         * 允许不选任何人点击确定
         */
        public boolean allowSelectEmpty = false;

        /**
         * 是否显示最大数目，结合maxSelectNum,与搜索位置相同
         */
        public boolean maxSelectNumVisible = false;

        /**
         * 新的选择器类型显示
         */
        public String showNewContactSelectTypeTip = "";

        /**
         * 数据返回类型 1 ArrayList<String>   2 ArrayList<Bundle> （put (id type)）
         */
        public int returnType = 1;

        public boolean withName = false;

    }

    public static void startActivityForResult(Context context, Option option, int requestCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA, option);
        intent.setClass(context, ContactSelectActivity.class);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    @Override
    public void onBackPressed() {
        if (searchView != null) {
            searchView.setQuery("", true);
            searchView.setIconified(true);
        }
        showKeyboard(false);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // search view
        getMenuInflater().inflate(R.menu.nim_contacts_search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (!option.searchVisible) {
            item.setVisible(false);
            return true;
        }

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                finish();
                return false;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        ImageView searchButton = searchView.findViewById(R.id.search_button);
        ImageView mCloseButton = searchView.findViewById(R.id.search_close_btn);
        if (searchButton != null) {
            searchButton.setImageResource(R.drawable.search_white_icon);
        }
        if (mCloseButton != null) {
            mCloseButton.setImageResource(R.drawable.seach_cancle);
        }

        this.searchView = searchView;
        this.searchView.setVisibility(option.searchVisible ? View.VISIBLE : View.GONE);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_contacts_select);



        parseIntentData();
        setBar();

        initAdapter();
        initListView();
        initContactSelectArea();

        loadData();
    }



    protected void setBar() {
        ToolBarOptions options = new NimToolBarOptions();
//        if (iInitBar != null) {
//            options.titleString = "谁可以领";
//            options.barBackgroundColor = Color.parseColor("#D43F3F");
//        } else {
//            options.titleString = "邀请成员";
////        }
        options.titleString = option.title;
        setToolBar(R.id.toolbar, options);
    }

    private void parseIntentData() {
        requestCode = getIntent().getIntExtra("requestCode",0);
        this.option = (Option) getIntent().getSerializableExtra(EXTRA_DATA);
        if (TextUtils.isEmpty(option.maxSelectedTip)) {
            option.maxSelectedTip = "最多选择" + option.maxSelectNum + "人";
        }
        if (TextUtils.isEmpty(option.minSelectedTip)) {
            option.minSelectedTip = "至少选择" + option.minSelectNum + "人";
        }
        setTitle(option.title);
    }

    private class ContactDataProviderEx extends ContactDataProvider {
        private String teamId;

        private boolean loadedTeamMember = false;

        private Context context;

        public ContactDataProviderEx(String teamId, int... itemTypes) {
            super(itemTypes);
            this.teamId = teamId;
        }

        @Override
        public List<AbsContactItem> provide(TextQuery query) {
            List<AbsContactItem> data = new ArrayList<>();
            // 异步加载
            if (!loadedTeamMember) {
                TeamMemberDataProvider.loadTeamMemberDataAsync(teamId, new TeamMemberDataProvider.LoadTeamMemberCallback() {
                    @Override
                    public void onResult(boolean success) {
                        if (success) {
                            loadedTeamMember = true;
                            // 列表重新加载数据
                            loadData();
                        }
                    }
                });
            } else {
                data = TeamMemberDataProvider.provide(query, teamId);
            }
            return data;
        }
    }

    private void initAdapter() {
        IContactDataProvider dataProvider;
        LinearLayout switchDataView;
        switchDataView = findView(R.id.switchData);
        switchDataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomEventManager.getInstance().notifyListeners(new CustomEventManager.CustomEvent(CustomEventManager.CustomListenerName.SHARECREATESESSION, option.type));
            }
        });
        TextView itemdes = findView(R.id.itemdes);

        if (!TextUtils.isEmpty(option.showNewContactSelectTypeTip)) {
            ((TextView) findView(R.id.switchDatatv)).setText(option.showNewContactSelectTypeTip);
            switchDataView.setVisibility(View.VISIBLE);

        } else {
            switchDataView.setVisibility(View.GONE);
        }
        itemdes.setVisibility(View.GONE);
        if (option.type == ContactSelectType.TEAM_MEMBER && !TextUtils.isEmpty(this.option.teamId)) {
            dataProvider = new ContactDataProviderEx(this.option.teamId, ItemTypes.TEAM_MEMBER);
        } else if (option.type == ContactSelectType.TEAM) {
            option.showContactSelectArea = false;
            dataProvider = new ContactDataProvider(ItemTypes.TEAM);
        } else if (option.type == ContactSelectType.RECENTLY) {
            dataProvider = new ContactDataProvider(ItemTypes.RECENTLY);
            itemdes.setVisibility(View.VISIBLE);
            this.setSwipeBackEnable(false);
        } else if (option.type == ContactSelectType.MOBILE) {
            dataProvider = new MobileDataProvider(ContactSelectActivity.this);

        } else {
            dataProvider = new ContactDataProvider(ItemTypes.FRIEND);
        }

        // contact adapter
        contactAdapter = new ContactSelectAdapter(ContactSelectActivity.this, new ContactsSelectGroupStrategy(),
                dataProvider) {
            boolean isEmptyContacts = false;

            @Override
            protected List<AbsContactItem> onNonDataItems() {
                return null;
            }

            @Override
            protected void onPostLoad(boolean empty, String queryText, boolean all) {
                if (empty) {
                    if (TextUtils.isEmpty(queryText)) {
                        isEmptyContacts = true;
                    }
                    updateEmptyView(queryText);
                } else {
                    setSearchViewVisible(true);
                }
            }

            private void updateEmptyView(String queryText) {
                if (!isEmptyContacts && !TextUtils.isEmpty(queryText)) {
                    setSearchViewVisible(true);
                } else {
                    setSearchViewVisible(false);
                }
            }

            private void setSearchViewVisible(boolean visible) {
                option.searchVisible = visible;
                if (searchView != null) {
                    searchView.setVisibility(option.searchVisible ? View.VISIBLE : View.GONE);
                }
            }
        };


        Class c = option.multi ? ContactsMultiSelectHolder.class : ContactsSelectHolder.class;
        contactAdapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        contactAdapter.addViewHolder(ItemTypes.FRIEND, c);
        contactAdapter.addViewHolder(ItemTypes.TEAM_MEMBER, c);
        contactAdapter.addViewHolder(ItemTypes.TEAM, c);
        contactAdapter.addViewHolder(ItemTypes.MOBILE, c);
        if (null != StatisticsConstants.ROBOT_IDS && StatisticsConstants.ROBOT_IDS.size() > 0){
            ArrayList<String> disableAccounts = new ArrayList<>();
            for (String id : StatisticsConstants.ROBOT_IDS){
                disableAccounts.add(id);
                option.itemFilter = new ContactIdFilter(disableAccounts);
            }
        }
        contactAdapter.setFilter(option.itemFilter);
        contactAdapter.setDisableFilter(option.itemDisableFilter);

        // contact select adapter
        contactSelectedAdapter = new ContactSelectAvatarAdapter(this);
    }

    private void initListView() {
        listView = findView(R.id.contact_list_view);
        listView.setAdapter(contactAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                showKeyboard(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = position - listView.getHeaderViewsCount();
                AbsContactItem item = (AbsContactItem) contactAdapter.getItem(position);

                if (item == null) {
                    return;
                }

                if (option.multi) {
                    if (!contactAdapter.isEnabled(position)) {
                        return;
                    }
                    IContact contact = null;
                    if (item instanceof ContactItem) {
                        contact = ((ContactItem) item).getContact();
                    }
                    if (contactAdapter.isSelected(position)) {
                        contactAdapter.cancelItem(position);
                        if (contact != null) {
                            contactSelectedAdapter.removeContact(contact);
                        }
                    } else {
                        if (contactSelectedAdapter.getCount() <= option.maxSelectNum) {
                            contactAdapter.selectItem(position);
                            if (contact != null) {
                                contactSelectedAdapter.addContact(contact);
                            }
                        } else {
                            ToastHelper.showToast(ContactSelectActivity.this, option.maxSelectedTip);
                        }

                        if (!TextUtils.isEmpty(queryText) && searchView != null) {
                            searchView.setQuery("", true);
                            searchView.setIconified(true);
                            showKeyboard(false);
                        }
                    }
                    arrangeSelected();
                } else {
                    if (item instanceof ContactItem) {
                        if (option.returnType == 2) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("type", SelectResultCode.SELECTED);

                            ArrayList<Bundle> selectedIds = new ArrayList<>();
                            final IContact contact = ((ContactItem) item).getContact();

                            Bundle selectedId = new Bundle();
                            selectedId.putString("contactId", contact.getContactId());
                            selectedId.putInt("contactType", contact.getContactType());

                            selectedIds.add(selectedId);

                            bundle.putSerializable("selectedIds", selectedIds);
                            onSelectedReturnBundle(bundle);
                        } else {
                            final IContact contact = ((ContactItem) item).getContact();
                            ArrayList<String> selectedIds = new ArrayList<>();
                            selectedIds.add(contact.getContactId());
                            onSelected(selectedIds);
                        }

                    }

                    arrangeSelected();
                }
            }
        });

        // 字母导航
        TextView letterHit = (TextView) findViewById(R.id.tv_hit_letter);
        LetterIndexView idxView = (LetterIndexView) findViewById(R.id.liv_index);
        idxView.setLetters(getResources().getStringArray(R.array.letter_list2));
        ImageView imgBackLetter = (ImageView) findViewById(R.id.img_hit_letter);
        if (option.type != ContactSelectType.TEAM && option.type != ContactSelectType.RECENTLY) {
            livIndex = contactAdapter.createLivIndex(listView, idxView, letterHit, imgBackLetter);
            livIndex.show();
        } else {
            idxView.setVisibility(View.GONE);
        }


    }


    private void initContactSelectArea() {
        btnSelect = (Button) findViewById(R.id.btnSelect);
        if (!option.allowSelectEmpty) {
            btnSelect.setEnabled(false);
        } else {
            btnSelect.setEnabled(true);
        }
        btnSelect.setOnClickListener(this);
        bottomPanel = (RelativeLayout) findViewById(R.id.rlCtrl);
        scrollViewSelected = (HorizontalScrollView) findViewById(R.id.contact_select_area);
        if (option.multi) {
            bottomPanel.setVisibility(View.VISIBLE);
            if (option.showContactSelectArea) {
                scrollViewSelected.setVisibility(View.VISIBLE);
                btnSelect.setVisibility(View.VISIBLE);
            } else {
                scrollViewSelected.setVisibility(View.GONE);
                btnSelect.setVisibility(View.GONE);
            }
            // bottomPanel.setVisibility(View.VISIBLE);
            // scrollViewSelected.setVisibility(View.VISIBLE);
            // btnSelect.setVisibility(View.VISIBLE);
            btnSelect.setText(getOKBtnText(0));
        } else {
            bottomPanel.setVisibility(View.GONE);
        }

        // selected contact image banner
        imageSelectedGridView = (GridView) findViewById(R.id.contact_select_area_grid);
        imageSelectedGridView.setAdapter(contactSelectedAdapter);
        notifySelectAreaDataSetChanged();
        imageSelectedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (contactSelectedAdapter.getItem(position) == null) {
                        return;
                    }

                    IContact iContact = contactSelectedAdapter.remove(position);
                    if (iContact != null) {
                        contactAdapter.cancelItem(iContact);
                    }
                    arrangeSelected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // init already selected items
        List<String> selectedUids = option.alreadySelectedAccounts;
        if (selectedUids != null && !selectedUids.isEmpty()) {
            contactAdapter.setAlreadySelectedAccounts(selectedUids);
            List<ContactItem> selectedItems = contactAdapter.getSelectedItem();
            for (ContactItem item : selectedItems) {
                contactSelectedAdapter.addContact(item.getContact());
            }
            arrangeSelected();
        }

        if (requestCode == 111){
            TextView tvAll = (TextView) findViewById(R.id.tv_all);
            Team team = NimUIKit.getTeamProvider().getTeamById(option.teamId);
            if (!StringUtil.isEmpty(team.getId()) && !StringUtil.isEmpty(NimUIKit.getAccount())){
                TeamMember teamMember = TeamDataCache.getInstance().getTeamMember(team.getId(),NimUIKit.getAccount());
                if (teamMember.getType() == TeamMemberType.Owner || teamMember.getType() == TeamMemberType.Manager){
                    tvAll.setVisibility(View.VISIBLE);
                }else {
                    tvAll.setVisibility(View.GONE);
                }

                tvAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("all",true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        }

    }

    private void loadData() {
        contactAdapter.load(true);
    }

    private void arrangeSelected() {
        this.contactAdapter.notifyDataSetChanged();
        if (option.multi) {
            int count = contactSelectedAdapter.getCount();
            if (!option.allowSelectEmpty) {
                btnSelect.setEnabled(count > 1);
            } else {
                btnSelect.setEnabled(true);
            }

            btnSelect.setText(getOKBtnText(count));
            notifySelectAreaDataSetChanged();
        }
    }

    private void notifySelectAreaDataSetChanged() {
        int converViewWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 46, this.getResources()
                .getDisplayMetrics()));
        ViewGroup.LayoutParams layoutParams = imageSelectedGridView.getLayoutParams();
        layoutParams.width = converViewWidth * contactSelectedAdapter.getCount();
        layoutParams.height = converViewWidth;
        imageSelectedGridView.setLayoutParams(layoutParams);
        imageSelectedGridView.setNumColumns(contactSelectedAdapter.getCount());

        try {
            final int x = layoutParams.width;
            final int y = layoutParams.height;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    scrollViewSelected.scrollTo(x, y);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        contactSelectedAdapter.notifyDataSetChanged();
    }


    private String getOKBtnText(int count) {
        String caption = getString(R.string.ok);
        int showCount = (count < 1 ? 0 : (count - 1));
        StringBuilder sb = new StringBuilder(caption);
        sb.append(" (");
        sb.append(showCount);
        if (option.maxSelectNumVisible) {
            sb.append("/");
            sb.append(option.maxSelectNum);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * ************************** select ************************
     */

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSelect) {
            List<IContact> contacts = contactSelectedAdapter
                    .getSelectedContacts();
            if (option.allowSelectEmpty || checkMinMaxSelection(contacts.size())) {

                if (option.returnType == 2) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("type", SelectResultCode.SELECTED);

                    ArrayList<Bundle> selectedIds = new ArrayList<>();
                    for (IContact c : contacts) {
                        Bundle selectedId = new Bundle();
                        selectedId.putString("contactId", c.getContactId());
                        selectedId.putInt("contactType", c.getContactType());
                        selectedIds.add(selectedId);
                    }
                    bundle.putSerializable("selectedIds", selectedIds);
                    onSelectedReturnBundle(bundle);
                } else {
                    ArrayList<String> selectedAccounts = new ArrayList<>();
                    ArrayList<String> selectedName = new ArrayList<>();
                    for (IContact c : contacts) {
                        selectedAccounts.add(c.getContactId());
                        selectedName.add(c.getDisplayName());
                    }
                    onSelected(selectedAccounts, selectedName);
                }
            }

        }
    }


    private boolean checkMinMaxSelection(int selected) {
        if (option.minSelectNum > selected) {
            return showMaxMinSelectTip(true);
        } else if (option.maxSelectNum < selected) {
            return showMaxMinSelectTip(false);
        }
        return true;
    }

    private boolean showMaxMinSelectTip(boolean min) {
        if (min) {
            ToastHelper.showToast(this, option.minSelectedTip);
        } else {
            ToastHelper.showToast(this, option.maxSelectedTip);
        }
        return false;
    }


    public void onSelectedReturnBundle(Bundle selects) {

        CustomEventManager.getInstance().notifyListeners(new CustomEventManager.CustomEvent(CustomEventManager.CustomListenerName.SHARESELECTDATABACK, selects));
//        this.finish();
    }

    public void onSelected(ArrayList<String> selects) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(RESULT_DATA, selects);
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    public void onSelected(ArrayList<String> selects, ArrayList<String> selectName) {
        Intent intent = new Intent();
        if (option.withName ) {
//            if (selects.size() > 0 || selectName.size() > 0) {
//                if (selects.size() > 5 || selectName.size() > 5) {
//                    toast("最多可以指定5人");
//                    return;
//                }
//                HashMap<String, ArrayList<String>> tempMap = new HashMap<>();
//                tempMap.put("uId", selects);
//                tempMap.put("uName", selectName);
//                iChoose.setIntentExtra(this, tempMap);
//            } else {
//                setResult(Activity.RESULT_OK, intent);
//            }


//            HashMap<String, ArrayList<String>> tempMap = new HashMap<>();
//            tempMap.put("uId", selects);
//            tempMap.put("uName", selectName);

            intent.putStringArrayListExtra(RESULT_DATA,selects);
            intent.putStringArrayListExtra(RESULT_NAME,selectName);

            setResult(Activity.RESULT_OK,intent);


        } else {
            intent.putStringArrayListExtra(RESULT_DATA, selects);
            setResult(Activity.RESULT_OK, intent);
        }
        this.finish();
    }

    public interface IChoose {
        void setIntentExtra(Activity activity, Object object);
    }

    public interface IChooseTheme {
        void initTheme(Activity activity);
    }

    public interface IInitBar {
        void initBar(ToolBarOptions options);
    }

    /**
     * ************************* search ******************************
     */

    @Override
    public boolean onQueryTextChange(String query) {
        queryText = query;
        if (TextUtils.isEmpty(query)) {
            this.contactAdapter.load(true);
        } else {
            this.contactAdapter.query(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String arg0) {
        return false;
    }

    @Override
    public void finish() {
        showKeyboard(false);
        super.finish();
    }
}

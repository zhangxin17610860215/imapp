package com.wulewan.ghxm.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.wulewan.ghxm.R;
import com.wulewan.ghxm.session.SessionHelper;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.wulewan.uikit.business.contact.core.item.AbsContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ItemTypes;
import com.netease.wulewan.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.wulewan.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.wulewan.uikit.business.contact.core.provider.ContactDataProvider;
import com.netease.wulewan.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.wulewan.uikit.business.contact.core.viewholder.ContactHolder;
import com.netease.wulewan.uikit.business.contact.core.viewholder.LabelHolder;
import com.netease.wulewan.uikit.common.ToastHelper;
import com.netease.wulewan.uikit.common.activity.UI;

import java.util.List;

/**
 * 群列表(通讯录)
 * <p/>
 * Created by huangjun on 2015/4/21.
 */
public class TeamListActivity extends UI implements AdapterView.OnItemClickListener {

    private static final String EXTRA_DATA_ITEM_TYPES = "EXTRA_DATA_ITEM_TYPES";

    private ContactDataAdapter adapter;

    private ListView lvContacts;

    private LinearLayout llNoData;

    private int itemType;

    public static final void start(Context context, int teamItemTypes) {
        Intent intent = new Intent();
        intent.setClass(context, TeamListActivity.class);
        intent.putExtra(EXTRA_DATA_ITEM_TYPES, teamItemTypes);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemType = getIntent().getIntExtra(EXTRA_DATA_ITEM_TYPES, ItemTypes.TEAMS.ADVANCED_TEAM);

        setContentView(R.layout.group_list_activity);

//        ToolBarOptions options = new NimToolBarOptions();
//        options.titleId = itemType == ItemTypes.TEAMS.ADVANCED_TEAM ? R.string.advanced_team : R.string.normal_team;
//        setToolBar(R.id.toolbar, options);

        onInitSetBack(TeamListActivity.this);
        onInitSetTitle(TeamListActivity.this, getString(R.string.save_group));

        lvContacts = findView(R.id.group_list);
        llNoData = findView(R.id.ll_nodata);
        TextView nullContent = findView(R.id.tv_noData_content);
        nullContent.setText(getString(R.string.no_add_group));
        GroupStrategy groupStrategy = new GroupStrategy();
        IContactDataProvider dataProvider = new ContactDataProvider(itemType);

        adapter = new ContactDataAdapter(this, groupStrategy, dataProvider) {
            @Override
            protected List<AbsContactItem> onNonDataItems() {
                return null;
            }

            @Override
            protected void onPreReady() {
            }

            @Override
            protected void onPostLoad(boolean empty, String queryText, boolean all) {
            }
        };
        adapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        adapter.addViewHolder(ItemTypes.TEAM, ContactHolder.class);

        lvContacts.setAdapter(adapter);
        lvContacts.setOnItemClickListener(this);
        lvContacts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                showKeyboard(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

        // load data
        updateBlock();

        adapter.load(true);

        registerTeamUpdateObserver(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registerTeamUpdateObserver(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final AbsContactItem item = (AbsContactItem) adapter.getItem(position);
        switch (item.getItemType()) {
            case ItemTypes.TEAM:
                SessionHelper.startTeamSession(TeamListActivity.this, ((ContactItem) item).getContact().getContactId());
                break;
        }
    }

    private void registerTeamUpdateObserver(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataChangedObserver, register);
    }

    TeamDataChangedObserver teamDataChangedObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            adapter.load(true);
            updateBlock();
        }

        @Override
        public void onRemoveTeam(Team team) {
            adapter.load(true);
            updateBlock();
        }
    };

    private void updateBlock() {
        int count = NIMClient.getService(TeamService.class).queryTeamCountByTypeBlock(itemType == ItemTypes.TEAMS
                .ADVANCED_TEAM ? TeamTypeEnum.Advanced : TeamTypeEnum.Normal);
        if (count == 0) {
            llNoData.setVisibility(View.VISIBLE);
            if (itemType == ItemTypes.TEAMS.ADVANCED_TEAM) {
                ToastHelper.showToast(TeamListActivity.this, R.string.no_team);
            } else if (itemType == ItemTypes.TEAMS.NORMAL_TEAM) {
                ToastHelper.showToast(TeamListActivity.this, R.string.no_normal_team);
            }
        } else {
            llNoData.setVisibility(View.GONE);
        }
    }

    private static class GroupStrategy extends ContactGroupStrategy {
        GroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, 0, ""); // 默认分组
        }

        @Override
        public String belongs(AbsContactItem item) {
            switch (item.getItemType()) {
                case ItemTypes.TEAM:
                    return GROUP_NULL;
                default:
                    return null;
            }
        }
    }

}

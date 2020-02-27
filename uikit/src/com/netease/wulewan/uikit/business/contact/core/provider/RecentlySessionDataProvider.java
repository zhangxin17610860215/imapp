package com.netease.wulewan.uikit.business.contact.core.provider;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.business.contact.core.item.AbsContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ContactItem;
import com.netease.wulewan.uikit.business.contact.core.item.ItemTypes;
import com.netease.wulewan.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.wulewan.uikit.business.contact.core.model.IContact;
import com.netease.wulewan.uikit.business.contact.core.model.TeamContact;
import com.netease.wulewan.uikit.business.contact.core.query.TextQuery;
import com.netease.wulewan.uikit.business.contact.core.util.ContactHelper;
import com.netease.wulewan.uikit.common.util.log.LogUtil;
import com.netease.wulewan.uikit.impl.cache.UIKitLogTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecentlySessionDataProvider {

    public static List<AbsContactItem> provide(TextQuery query) {
        List<RecentContact> sources = query(query);
        List<AbsContactItem> items = new ArrayList<>(sources.size());
        for (RecentContact u : sources) {
            if (u.getSessionType() == SessionTypeEnum.Team) {
                items.add(createTeamItem(new TeamContact(NimUIKit.getTeamProvider().getTeamById(u.getContactId())), ItemTypes.FRIEND));

            } else if (u.getSessionType() == SessionTypeEnum.P2P) {
                items.add(createTeamItem(ContactHelper.makeContactFromUserInfo(NimUIKit.getUserInfoProvider().getUserInfo(u.getContactId())), ItemTypes.TEAM));
            }
        }

        LogUtil.i(UIKitLogTag.CONTACT, "contact provide data size =" + items.size());
        return items;
    }

    private static AbsContactItem createTeamItem(IContact team,int type) {
        return new ContactItem(team,type) {
            @Override
            public int compareTo(ContactItem item) {
                return 0;
            }

            @Override
            public String belongsGroup() {
                return ContactGroupStrategy.GROUP_RECENTLY;
            }
        };
    }

    private static final List<RecentContact> query(TextQuery query) {
        // 查询最近联系人列表数据
        List<RecentContact> recents = NIMClient.getService(MsgService.class).queryRecentContactsBlock();
        if (query == null) {
            return recents;
        }
        for (Iterator<RecentContact> iter = recents.iterator(); iter.hasNext(); ) {
            RecentContact recent = iter.next();
            if (recent.getSessionType() == SessionTypeEnum.P2P)
            {
                UserInfo user = NimUIKit.getUserInfoProvider().getUserInfo(recent.getContactId());
                if (user != null)
                {
                    boolean hit = ContactSearch.hitUser(user, query) || (ContactSearch.hitFriend(user, query));
                    if (!hit) {
                        iter.remove();
                    }
                }
            }
            else if(recent.getSessionType() == SessionTypeEnum.Team){

                Team team = NimUIKit.getTeamProvider().getTeamById(recent.getContactId());
                if (!ContactSearch.hitTeam(team, query)) {
                    iter.remove();
                }
            }

        }
        return recents;
    }
}

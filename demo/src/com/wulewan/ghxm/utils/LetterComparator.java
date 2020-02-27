package com.wulewan.ghxm.utils;

import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.wulewan.uikit.api.NimUIKit;

import java.util.Comparator;

/**
 * 专用于按首字母排序
 *
 * @author nanchen
 * @fileName WaveSideBarView
 * @packageName com.nanchen.wavesidebarview
 * @date 2016/12/27  16:19
 * @github https://github.com/nanchen2251
 */

public class LetterComparator implements Comparator<TeamMember> {

    @Override
    public int compare(TeamMember teamMember, TeamMember t1) {
        if (teamMember == null || t1 == null) {
            return 0;
        }
        UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(teamMember.getAccount());
        UserInfo userInfo1 = NimUIKit.getUserInfoProvider().getUserInfo(t1.getAccount());
        String name = StringUtil.isEmpty(teamMember.getTeamNick()) ? userInfo.getName() : teamMember.getTeamNick();
        String name1 = StringUtil.isEmpty(t1.getTeamNick()) ? userInfo1.getName() : t1.getTeamNick();
        String lhsSortLetters = StringUtil.getFirstSpell(name.substring(0, 1)).toUpperCase();
        String rhsSortLetters = StringUtil.getFirstSpell(name1.substring(0, 1)).toUpperCase();
        return lhsSortLetters.compareTo(rhsSortLetters);
    }
}

package com.yqbj.ghxm.utils;

import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.yqbj.ghxm.bean.GetAllMemberWalletBean;

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

public class LetterComparator implements Comparator<GetAllMemberWalletBean.ResultsBean> {

    @Override
    public int compare(GetAllMemberWalletBean.ResultsBean teamMember, GetAllMemberWalletBean.ResultsBean bean) {
        if (teamMember == null || bean == null) {
            return 0;
        }
        UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(teamMember.getUid());
        UserInfo userInfo1 = NimUIKit.getUserInfoProvider().getUserInfo(bean.getUid());
        String name = userInfo.getName() ;
        String name1 = userInfo1.getName();
        String lhsSortLetters = StringUtil.getFirstSpell(name.substring(0, 1)).toUpperCase();
        String rhsSortLetters = StringUtil.getFirstSpell(name1.substring(0, 1)).toUpperCase();
        return lhsSortLetters.compareTo(rhsSortLetters);
    }
}

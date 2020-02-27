package com.netease.wulewan.uikit.business.ait;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.common.util.string.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hzchenkang on 2017/7/7.
 *
 * @ 联系人数据
 */

public class AitContactsModel {

    // 已@ 的成员
    private Map<String, AitBlock> aitBlocks = new HashMap<>();

    // 清除所有的@块
    public void reset() {
        aitBlocks.clear();
    }

    public void addAitMember(String tid, final String account, final String name, final int type, final int start) {

        //因为@所有人结构体太大   有可能会造成消息发送失败所以注释掉
//        if (tid.equals(account)){
            //@了所有人
//            NIMClient.getService(TeamService.class).queryMemberList(tid).setCallback(new RequestCallback<List<TeamMember>>() {
//                @Override
//                public void onSuccess(List<TeamMember> list) {
//                    for (TeamMember member : list){
//                        if (!NimUIKit.getAccount().equals(member.getAccount())){
//                            AitBlock aitBlock = aitBlocks.get(member.getAccount());
//                            if (aitBlock == null) {
//                                aitBlock = new AitBlock(member.getTeamNick(), type);
//                                aitBlocks.put(member.getAccount(), aitBlock);
//                            }
//                            aitBlock.addSegment(start);
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailed(int i) {
//
//                }
//
//                @Override
//                public void onException(Throwable throwable) {
//
//                }
//            });
//        }else {
//            AitBlock aitBlock = aitBlocks.get(account);
//            if (aitBlock == null) {
//                aitBlock = new AitBlock(name, type);
//                aitBlocks.put(account, aitBlock);
//            }
//            aitBlock.addSegment(start);
//        }
        if (StringUtil.isEmpty(tid) || StringUtil.isEmpty(account)){
            return;
        }
        if (!tid.equals(account)){
            AitBlock aitBlock = aitBlocks.get(account);
            if (aitBlock == null) {
                aitBlock = new AitBlock(name, type);
                aitBlocks.put(account, aitBlock);
            }
            aitBlock.addSegment(start);
        }
    }

    // 查所有被@的群成员
    public List<String> getAitTeamMember() {
        List<String> teamMembers = new ArrayList<>();
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            if (block.aitType == AitContactType.TEAM_MEMBER && block.valid()) {
                teamMembers.add(account);
            }
        }
        return teamMembers;
    }

    public AitBlock getAitBlock(String account) {
        return aitBlocks.get(account);
    }

    // 查第一个被@ 的机器人
    public String getFirstAitRobot() {
        int start = -1;
        String robotAccount = null;

        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            if (block.valid() && block.aitType == AitContactType.ROBOT) {
                int blockStart = block.getFirstSegmentStart();
                if (blockStart == -1) {
                    continue;
                }
                if (start == -1 || blockStart < start) {
                    start = blockStart;
                    robotAccount = account;
                }
            }
        }
        return robotAccount;
    }

    // 找到 curPos 恰好命中 end 的segment
    public AitBlock.AitSegment findAitSegmentByEndPos(int start) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            AitBlock.AitSegment segment = block.findLastSegmentByEnd(start);
            if (segment != null) {
                return segment;
            }
        }
        return null;
    }

    // 文本插入后更新@块的起止位置
    public void onInsertText(int start, String changeText) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            block.moveRight(start, changeText);
            if (!block.valid()) {
                iterator.remove();
            }
        }
    }

    // 文本删除后更新@块的起止位置
    public void onDeleteText(int start, int length) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            block.moveLeft(start, length);
            if (!block.valid()) {
                iterator.remove();
            }
        }
    }
}

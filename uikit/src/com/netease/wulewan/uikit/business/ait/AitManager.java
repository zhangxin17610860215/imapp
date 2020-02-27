package com.netease.wulewan.uikit.business.ait;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.netease.wulewan.uikit.api.NimUIKit;
import com.netease.wulewan.uikit.business.ait.selector.AitContactSelectorActivity;
import com.netease.wulewan.uikit.business.ait.selector.AitContactSelectorActivityNew;
import com.netease.wulewan.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.wulewan.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.wulewan.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.wulewan.uikit.common.util.string.StringUtil;
import com.netease.wulewan.uikit.impl.cache.TeamDataCache;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by hzchenkang on 2017/7/10.
 */

public class AitManager implements TextWatcher {

    private Context context;

    private String tid;

    private boolean robot;

    private AitContactsModel aitContactsModel;

    private int curPos;

    private boolean ignoreTextChange = false;

    private AitTextChangeListener listener;

    public AitManager(Context context, String tid, boolean robot) {
        this.context = context;
        this.tid = tid;
        this.robot = robot;
        aitContactsModel = new AitContactsModel();
    }

    public void setTextChangeListener(AitTextChangeListener listener) {
        this.listener = listener;
    }

    public List<String> getAitTeamMember() {
        return aitContactsModel.getAitTeamMember();
    }

    public String getAitRobot() {
        return aitContactsModel.getFirstAitRobot();
    }

    public String removeRobotAitString(String text, String robotAccount) {
        AitBlock block = aitContactsModel.getAitBlock(robotAccount);
        if (block != null) {
            return text.replaceAll(block.text, "");
        } else {
            return text;
        }
    }

    public void reset() {
        aitContactsModel.reset();
        ignoreTextChange = false;
        curPos = 0;
    }

    /**
     * ------------------------------ 增加@成员 --------------------------------------
     */

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            boolean isAll = data.getBooleanExtra("all", false);
            if (isAll){
                insertAitMemberInner(tid, "所有人", 2, curPos, false);
            }else {
                int type = data.getIntExtra(AitContactSelectorActivity.RESULT_TYPE, -1);
                String account = "";
                String name = "";
                ArrayList<String> accounts = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                for (String str : accounts){
                    account = str;
                    if (!StringUtil.isEmpty(tid) && !StringUtil.isEmpty(account)){
                        TeamMember teamMember = TeamDataCache.getInstance().getTeamMember(tid,account);
                        name = getAitTeamMemberName(teamMember);
                    }
                }
                String longClicked = data.getStringExtra("LongClicked");
                if (TextUtils.isEmpty(longClicked) || !longClicked.equals("isLongClicked")){
                    insertAitMemberInner(account, name, 2, curPos, false);
                }else {
                    insertAitMemberInner(account, name, 2, curPos, true);
                }

            }
        }
    }

    // 群昵称 > 用户昵称 > 账号
    private static String getAitTeamMemberName(TeamMember member) {
        if (member == null) {
            return "";
        }
        String memberNick = member.getTeamNick();
        if (!TextUtils.isEmpty(memberNick)) {
            return memberNick;
        }
        return UserInfoHelper.getUserName(member.getAccount());
    }

    public void insertAitRobot(String account, String name, int start) {
        insertAitMemberInner(account, name, AitContactType.ROBOT, start, true);
    }

    private void insertAitMemberInner(String account, String name, int type, int start, boolean needInsertAitInText) {
        name = name + " ";
        String content = needInsertAitInText ? "@" + name : name;
        if (listener != null) {
            // 关闭监听
            ignoreTextChange = true;
            // insert 文本到editText
            listener.onTextAdd(content, start, content.length());
            // 开启监听
            ignoreTextChange = false;
        }

        // update 已有的 aitBlock
        aitContactsModel.onInsertText(start, content);

        int index = needInsertAitInText ? start : start - 1;
        // 添加当前到 aitBlock
        aitContactsModel.addAitMember(tid,account, name, type, index);
    }

    /**
     * ------------------------------ editText 监听 --------------------------------------
     */

    // 当删除尾部空格时，删除一整个segment,包含界面上也删除
    private boolean deleteSegment(int start, int count) {
        if (count != 1) {
            return false;
        }
        boolean result = false;
        AitBlock.AitSegment segment = aitContactsModel.findAitSegmentByEndPos(start);
        if (segment != null) {
            int length = start - segment.start;
            if (listener != null) {
                ignoreTextChange = true;
                listener.onTextDelete(segment.start, length);
                ignoreTextChange = false;
            }
            aitContactsModel.onDeleteText(start, length);
            result = true;
        }
        return result;
    }

    /**
     * @param editable 变化后的Editable
     * @param start    text 变化区块的起始index
     * @param count    text 变化区块的大小
     * @param delete   是否是删除
     */
    private void afterTextChanged(Editable editable, int start, int count, boolean delete) {
        curPos = delete ? start : count + start;
        if (ignoreTextChange) {
            return;
        }
        if (delete) {
            int before = start + count;
            if (deleteSegment(before, count)) {
                return;
            }
            aitContactsModel.onDeleteText(before, count);

        } else {
            if (count <= 0 || editable.length() < start + count) {
                return;
            }
            CharSequence s = editable.subSequence(start, start + count);
            if (s == null) {
                return;
            }
            if (s.toString().equals("@")) {
                // 启动@联系人界面
                if (!TextUtils.isEmpty(tid) && robot) {
                    ContactSelectActivity.Option option = new ContactSelectActivity.Option();
                    option.maxSelectNum = 1;
                    option.allowSelectEmpty = true;
                    option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
                    option.title = "选择提醒的人";
                    option.teamId = tid;
                    option.multi = false;

                    ArrayList<String> disableAccounts = new ArrayList<>();
                    disableAccounts.add(NimUIKit.getAccount());
                    option.itemFilter = new ContactIdFilter(disableAccounts);

                    option.withName = true;



                    AitContactSelectorActivityNew.startActivityForResult(context, option, 111);
                }
            }
            aitContactsModel.onInsertText(start, s.toString());
        }
    }

    private int editTextStart;
    private int editTextCount;
    private int editTextBefore;
    private boolean delete;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        delete = count > after;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.editTextStart = start;
        this.editTextCount = count;
        this.editTextBefore = before;
    }

    @Override
    public void afterTextChanged(Editable s) {
        afterTextChanged(s, editTextStart, delete ? editTextBefore : editTextCount, delete);
    }
}

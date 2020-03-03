package com.yqbj.ghxm.session.viewholder;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.team.model.Team;
import com.yqbj.ghxm.R;
import com.yqbj.ghxm.session.extension.SysNotifyAttachment;
import com.yqbj.ghxm.utils.StringUtil;
import com.yqbj.ghxm.utils.TimeUtils;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.team.TeamProvider;
import com.netease.yqbj.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.yqbj.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.yqbj.uikit.common.util.sys.ScreenUtil;

public class MsgViewHolderNotify extends MsgViewHolderBase {

    TextView tv_title;
    TextView tv_time;
    TextView tv_content;
    LinearLayout content_bg;

    SysNotifyAttachment sysNotifyAttachment;

    public MsgViewHolderNotify(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.notify_card_layout;
    }

    @Override
    protected void inflateContentView() {
        content_bg = view.findViewById(R.id.content_bg);
        tv_title = view.findViewById(R.id.notify_title);
        tv_time = view.findViewById(R.id.notify_time);
        tv_content = view.findViewById(R.id.notify_content);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) content_bg.getLayoutParams();
        layoutParams.width = ScreenUtil.getDisplayWidth() - ScreenUtil.dip2px(40);
        content_bg.setLayoutParams(layoutParams);



    }

    @Override
    protected void bindContentView() {
        sysNotifyAttachment = (SysNotifyAttachment) message.getAttachment();

        String teamId = "";
        String content = sysNotifyAttachment.getContent();
        teamId = sysNotifyAttachment.getTeamId();
        if (StringUtil.isNotEmpty(teamId) && content.contains("***")){
            TeamProvider teamProvider = NimUIKit.getTeamProvider();
            Team teamById = teamProvider.getTeamById(teamId);
            content = content.replace("***",String.format("<%s>",teamById.getName()));
        }

        tv_title.setText(sysNotifyAttachment.getMsgTitle());
        tv_time.setText(TimeUtils.getDateToString(Long.parseLong(sysNotifyAttachment.getMsgDate()),TimeUtils.TIME_TYPE_04));
        tv_content.setText(content);

    }

    @Override
    protected boolean shouldDisplayReceipt() {
        return false;
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}

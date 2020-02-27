package com.wulewan.ghxm.session.viewholder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.wulewan.ghxm.R;
import com.wulewan.ghxm.bean.TeamRobotNotifyBean;
import com.wulewan.ghxm.session.extension.TeamRobotNotifyAttachment;
import com.wulewan.ghxm.team.activity.SeeRecordActivity;
import com.wulewan.ghxm.utils.StringUtil;
import com.netease.wulewan.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.wulewan.uikit.common.ui.imageview.HeadImageView;
import com.netease.wulewan.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.wulewan.uikit.common.util.GlideUtil;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import java.util.ArrayList;
import java.util.List;

public class MsgViewHolderTeamRobotNotify extends MsgViewHolderBase {

    private TeamRobotNotifyAttachment attachment;
    private TextView tv_title;
    private TextView tv_time;
    private RecyclerView recyclerView;
    private TeamRobotNotifyBean otherDataBean;
    private List<TeamRobotNotifyBean.ContentBean> list;
    private EasyRVAdapter mAdapter;

    public MsgViewHolderTeamRobotNotify(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.notify_teamrobot_layout;
    }

    @Override
    protected void inflateContentView() {
        if (!isReceivedMessage()) {
            view.findViewById(R.id.team_robot_card_layout).setScaleX(-1);
        }
        tv_title = view.findViewById(R.id.tv_title);
        tv_time = view.findViewById(R.id.tv_time);
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    @Override
    protected void bindContentView() {
        attachment = (TeamRobotNotifyAttachment) message.getAttachment();
        otherDataBean = attachment.getOtherDataBean();
        if (null == otherDataBean){
            return;
        }
        if (StringUtil.isNotEmpty(otherDataBean.getTitle())){
            String[] split = otherDataBean.getTitle().split("\n");
            if (null != split && split.length >= 2){
                tv_time.setVisibility(View.VISIBLE);
                tv_title.setText(split[0]);
                tv_time.setText(split[1]);
            }else {
                tv_title.setText(otherDataBean.getTitle());
                tv_time.setVisibility(View.GONE);
            }
        }

        if (null == otherDataBean.getContent()){
            return;
        }

        list = new ArrayList<>();
        list.clear();
        list.addAll(otherDataBean.getContent());

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mAdapter = new EasyRVAdapter(context,list,R.layout.team_robotnotify_item_layout) {
            @Override
            protected void onBindData(EasyRVHolder viewHolder, int position, Object item) {
                TeamRobotNotifyBean.ContentBean bean = list.get(position);
                HeadImageView imgHead = viewHolder.getView(R.id.img_head);
                TextView tvLind1 = viewHolder.getView(R.id.tv_lind1);
                TextView tvLind2 = viewHolder.getView(R.id.tv_lind2);
                TextView tvLind3 = viewHolder.getView(R.id.tv_lind3);
                View isLast = viewHolder.getView(R.id.isLast);
                imgHead.setIsRect(true);
                GlideUtil.loadHavePlaceholderImageView(context,bean.getImageUrl(),R.mipmap.robot_bgicon,imgHead);

                tvLind1.setText(bean.getLine1());
                tvLind2.setText(Html.fromHtml(bean.getLine2()));
                tvLind3.setText(Html.fromHtml(bean.getLine3()));

                if (position == list.size() -1){
                    isLast.setVisibility(View.GONE);
                }else {
                    isLast.setVisibility(View.VISIBLE);
                }
            }
        };
        mAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object item) {
                if (null == otherDataBean){
                    return;
                }
                if (null == otherDataBean.getContent()){
                    return;
                }
                if (StringUtil.isEmpty(otherDataBean.getTitle())){
                    return;
                }
                SeeRecordActivity.start(context,otherDataBean,message.getSessionId());
            }
        });

        recyclerView.setAdapter(mAdapter);
    }

    // 内容区域点击事件响应处理。
    protected void onItemClick() {
        if (null == otherDataBean){
            return;
        }
        if (null == otherDataBean.getContent()){
            return;
        }
        if (StringUtil.isEmpty(otherDataBean.getTitle())){
            return;
        }
        SeeRecordActivity.start(context,otherDataBean,message.getSessionId());
    }

    // 右边显示白色背景
    protected boolean showWhiteBG() {
        return true;
    }
}

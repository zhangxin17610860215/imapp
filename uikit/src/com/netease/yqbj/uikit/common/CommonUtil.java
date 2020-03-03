package com.netease.yqbj.uikit.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.model.SimpleCallback;
import com.netease.yqbj.uikit.business.session.actions.PickImageAction;
import com.netease.yqbj.uikit.common.util.media.ImageUtil;
import com.netease.yqbj.uikit.common.util.storage.StorageType;
import com.netease.yqbj.uikit.common.util.storage.StorageUtil;
import com.netease.yqbj.uikit.common.util.string.StringUtil;
import com.othershe.combinebitmap.CombineBitmap;
import com.othershe.combinebitmap.layout.WechatLayoutManager;
import com.othershe.combinebitmap.listener.OnProgressListener;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class CommonUtil {


    public static void addTag(RecentContact recent, long tag) {
        tag = recent.getTag() | tag;
        recent.setTag(tag);
    }

    public static void removeTag(RecentContact recent, long tag) {
        tag = recent.getTag() & ~tag;
        recent.setTag(tag);
    }

    public static boolean isTagSet(RecentContact recent, long tag) {
        return (recent.getTag() & tag) == tag;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static void uploadTeamIcon(final String teamId, final Context context){
        uploadTeamIcon(teamId,context,null);
    }
    public static void uploadTeamIcon(final String teamId, final Context context, final RequestCallback requestCallback){
        NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> result, int code) {
                if(success&&result.size()>0){
                    int limitSize = 9;
                    if(result.size()<9){
                        limitSize =result.size();
                    }
                    Log.e("result.size",result.size() +"xxxx");
                    String[] IMG_URL_ARR = new String[limitSize];

                    for (int i = 0; i < limitSize; i++) {

                        String url = NimUIKit.getUserInfoProvider().getUserInfo(result.get(i).getAccount()).getAvatar();


                        if(!TextUtils.isEmpty(url)){
                            IMG_URL_ARR[i] = url;
                        }else{
                            IMG_URL_ARR[i] = "";
                        }

                    }
                    CombineBitmap.init(context).setLayoutManager(new WechatLayoutManager())
                            .setUrls(IMG_URL_ARR)
                            .setSize(50)
                            .setGap(1)
                            .setPlaceholder(R.drawable.nim_avatar_default)
                            .setGapColor(Color.parseColor("#E8E8E8"))
                            .setOnProgressListener(new OnProgressListener() {
                                @Override
                                public void onStart() {

                                }

                                @Override
                                public void onComplete(Bitmap bitmap) {

                                    if(bitmap==null){

                                        return;
                                    }


                                    String filename = StringUtil.get32UUID() + ".jpeg";
                                    String path = StorageUtil.getWritePath(filename, StorageType.TYPE_IMAGE);
                                    ImageUtil.saveBitmap2file(bitmap,path,context);

                                    if (TextUtils.isEmpty(path)) {
                                        return;
                                    }

                                    File file = new File(path);
                                    if (file == null) {
                                        return;
                                    }

                                    AbortableFuture<String> uploadFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
                                    uploadFuture.setCallback(new RequestCallbackWrapper<String>() {
                                        @Override
                                        public void onResult(int code, String url, Throwable exception) {
                                            if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {

                                                NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.ICON, url); // 更新资料
                                            }
                                        }
                                    });




                                }


                            }).build();
                }
            }
        });
    }
}

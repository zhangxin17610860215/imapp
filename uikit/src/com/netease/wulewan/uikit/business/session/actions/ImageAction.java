package com.netease.wulewan.uikit.business.session.actions;

import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.wulewan.uikit.R;

import java.io.File;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class ImageAction extends PickImageAction {


    private int imageActionType;
    private int titleId;

    public ImageAction(int iconResId, int titleId,int imageActionType) {
        super(iconResId,titleId, true);
        this.imageActionType = imageActionType;
        this.titleId = titleId;
    }

    public ImageAction() {
        super(R.drawable.nim_message_plus_photo_selector, R.string.input_panel_photo, true);
    }

//    @Override
//    public void onClick() {
//
//        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
//        option.titleResId = titleId;
//        option.multiSelect = true;
//        option.multiSelectMaxCount = PICK_IMAGE_COUNT;
//        option.crop = crop;
//        option.cropOutputImageWidth = PORTRAIT_IMAGE_WIDTH;
//        option.cropOutputImageHeight = PORTRAIT_IMAGE_WIDTH;
//        option.outputPath = tempFile();
//        int requestCode = makeRequestCode(RequestCode.PICK_IMAGE);
//        if(imageActionType == RequestCode.PREVIEW_IMAGE_FROM_CAMERA){
//            int from = PickImageActivity.FROM_CAMERA;
//            if (!option.crop) {
//                PickImageActivity.start( getActivity(), requestCode, from, option.outputPath, option.multiSelect, 1,
//                        true, false, 0, 0);
//            } else {
//                PickImageActivity.start(getActivity(), requestCode, from, option.outputPath, false, 1,
//                        false, true, option.cropOutputImageWidth, option.cropOutputImageHeight);
//            }
//        }else if(imageActionType == RequestCode.PICK_IMAGE){
//            int from = PickImageActivity.FROM_LOCAL;
//            if (!option.crop) {
//                PickImageActivity.start((Activity) getActivity(), requestCode, from, option.outputPath, option.multiSelect,
//                        option.multiSelectMaxCount, true, false, 0, 0);
//            } else {
//                PickImageActivity.start((Activity) getActivity(), requestCode, from, option.outputPath, false, 1,
//                        false, true, option.cropOutputImageWidth, option.cropOutputImageHeight);
//            }
//        }
//
//
//
//
//    }

    @Override
    protected void onPicked(File file) {
        IMMessage message;
        if (getContainer() != null && getContainer().sessionType == SessionTypeEnum.ChatRoom) {
            message = ChatRoomMessageBuilder.createChatRoomImageMessage(getAccount(), file, file.getName());
        } else {
            message = MessageBuilder.createImageMessage(getAccount(), getSessionType(), file, file.getName());
        }
        sendMessage(message);
    }
}


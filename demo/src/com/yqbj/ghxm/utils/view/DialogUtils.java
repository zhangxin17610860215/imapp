package com.yqbj.ghxm.utils.view;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yqbj.ghxm.R;
import com.yqbj.ghxm.bean.CheckVersionBean;

public class DialogUtils {

    public static Dialog showOneButtonDiolag(final Activity activity, final CheckVersionBean bean, final OnDialogActionListener listener) {
        final Dialog dialog = new Dialog(activity,R.style.custom_dialog);
        dialog.setContentView(R.layout.update_dialog_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        TextView tvTitle = dialog.findViewById(R.id.title);
        TextView tvMessage = dialog.findViewById(R.id.message);
        ImageView imgClose = dialog.findViewById(R.id.img_close);
        TextView tvYes = dialog.findViewById(R.id.yes);
        final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        tvTitle.setText("最新版本：V" + bean.getVersionno());
        tvMessage.setText(bean.getDescription()+"\n\n");
        progressBar.setVisibility(View.GONE);
        if (bean.getCompel() == 1){
            //强制升级
            imgClose.setVisibility(View.GONE);
        }else {
            imgClose.setVisibility(View.VISIBLE);
        }
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (null != listener){
                    listener.doCancelAction();
                }
            }
        });
        tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener){
                    listener.doOkAction();
                }
//                layout.setVisibility(View.GONE);
//                progressBar.setVisibility(View.VISIBLE);
//                OkHttpUtil.updateApk(activity,progressBar,bean.getDownloadUrl(), dialog);
            }
        });
        return dialog;
    }

    public interface OnDialogActionListener {
        void doCancelAction();

        void doOkAction();

    }

}

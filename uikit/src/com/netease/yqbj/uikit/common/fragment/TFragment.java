package com.netease.yqbj.uikit.common.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.yqbj.uikit.common.activity.UI;
import com.netease.yqbj.uikit.common.util.log.LogUtil;

public abstract class TFragment extends Fragment {
    private static final Handler handler = new Handler();

    private int containerId;

    private boolean destroyed;

    protected final boolean isDestroyed() {
        return destroyed;
    }

    public TextView comRightSure;

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LogUtil.ui("fragment: " + getClass().getSimpleName() + " onActivityCreated()");

        destroyed = false;
    }

    public void onDestroy() {
        super.onDestroy();

        LogUtil.ui("fragment: " + getClass().getSimpleName() + " onDestroy()");

        destroyed = true;
    }

    protected final Handler getHandler() {
        return handler;
    }

    protected final void postRunnable(final Runnable runnable) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // validate
                if (!isAdded()) {
                    return;
                }

                // run
                runnable.run();
            }
        });
    }

    protected final void postDelayed(final Runnable runnable, long delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // validate
                if (!isAdded()) {
                    return;
                }

                // run
                runnable.run();
            }
        }, delay);
    }

    protected void showKeyboard(boolean isShow) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        if (isShow) {
            if (activity.getCurrentFocus() == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                imm.showSoftInput(activity.getCurrentFocus(), 0);
            }
        } else {
            if (activity.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }
    }

    protected void hideKeyboard(View view) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        imm.hideSoftInputFromWindow(
                view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected <T extends View> T findView(int resId) {
        return (T) (getView().findViewById(resId));
    }

    public void onInitSetBack(Context context) {
        Resources res = context.getResources();
        String packageName = context.getPackageName();
        Button btnComBack = findView(res.getIdentifier("com_back_btn", "id", packageName)); //新返回
        ImageView imgComBackIcon = findView(res.getIdentifier("com_back_img", "id", packageName));//新返回图标
        if (btnComBack instanceof Button && imgComBackIcon instanceof ImageView) {
            btnComBack.setVisibility(View.VISIBLE);
            imgComBackIcon.setVisibility(View.VISIBLE);
            btnComBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }
    }


    public void onInitSetTitle(Context context, String title) {
        Resources res = context.getResources();
        String packageName = context.getPackageName();
        //新标题
        TextView txtComTitle = findView(res.getIdentifier("com_title_txt", "id", packageName));
        if (txtComTitle instanceof TextView) {
            txtComTitle.setText(title);
        }
    }

    public void onInitRightSure(Context context, int resId, String txt, float txtSize) {
        Resources res = context.getResources();
        String packageName = context.getPackageName();
        comRightSure = findView(res.getIdentifier("com_sure_ibtn", "id", packageName));
        if (!(comRightSure instanceof TextView)) return;
        comRightSure.setVisibility(View.VISIBLE);
        if (!txt.equals("")) comRightSure.setText(txt);
        if (txtSize > 0) comRightSure.setTextSize(txtSize);
        if (resId > 0) {
            Drawable drawable = getResources().getDrawable(resId);
            if (drawable instanceof Drawable) {
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                comRightSure.setCompoundDrawables(null, null, drawable, null);
            }
        }
        setRightSureClick();
    }

    public void setRightSureClick() {

    }

    protected void setToolBar(int toolbarId, int titleId, int logoId) {
        if (getActivity() != null && getActivity() instanceof UI) {
            ((UI) getActivity()).setToolBar(toolbarId, titleId, logoId);
        }
    }

    protected void setTitle(int titleId) {
        if (getActivity() != null && getActivity() instanceof UI) {
            getActivity().setTitle(titleId);
        }
    }

    // Fragment页面onResume函数重载
    public void onResume() {
        super.onResume();
    }
    // Fragment页面onResume函数重载
    public void onPause() {
        super.onPause();
    }
}

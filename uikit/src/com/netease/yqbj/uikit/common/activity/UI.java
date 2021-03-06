package com.netease.yqbj.uikit.common.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swipebacktofinish.SwipeBackActivity;
import com.example.swipebacktofinish.SwipeBackActivityHelper;
import com.netease.yqbj.uikit.R;
import com.netease.yqbj.uikit.common.fragment.TFragment;
import com.netease.yqbj.uikit.common.ui.dialog.DialogMaker;
import com.netease.yqbj.uikit.common.util.AppManager;
import com.netease.yqbj.uikit.common.util.log.LogUtil;
import com.netease.yqbj.uikit.common.util.sys.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;


public abstract class UI extends SwipeBackActivity {

    private boolean destroyed = false;

    private static Handler handler;

    public Toolbar toolbar;

    public TextView comRightSure;

    public TextView titleCenter;


    private SwipeBackActivityHelper mHelper;
    public Activity mActivity;


    protected void showProgress(Context context, boolean isCancel) {
        DialogMaker.showProgressDialog(context, context.getString(R.string.empty), isCancel);
    }

    protected void dismissProgress() {
        DialogMaker.dismissProgressDialog();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        AppManager.getAppManager().addActivity(this);
        LogUtil.ui("activity: " + getClass().getSimpleName() + " onCreate()");
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();


    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        super.startActivity(intent, options);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Override
    public void finish() {
        showKeyboard(false);
        super.finish();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
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
                    showKeyboard(false);
                    finish();
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


    @Override
    public void onBackPressed() {
        invokeFragmentManagerNoteStateNotSaved();
//        // 正在滑动返回的时候取消返回按钮事件
//        if (mSwipeBackHelper.isSliding()) {
//            return;
//        }
//        mSwipeBackHelper.backward();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (DialogMaker.isShowing()) {
            dismissProgress();
        }
        super.onDestroy();
        AppManager.getAppManager().removeActivity(this);
        LogUtil.ui("activity: " + getClass().getSimpleName() + " onDestroy()");
        destroyed = true;
    }

    public void toast(String tips) {
        Toast.makeText(this, tips, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onNavigateUpClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void setToolBar(int toolBarId, ToolBarOptions options) {
        toolbar = findView(toolBarId);
        toolbar.setTitle("");
        if (options.titleId != 0) {
            newCentrenTextView(options.titleId);
        }

        if (!TextUtils.isEmpty(options.titleString)) {
            newCentrenTextView(options.titleString);
        }
        if (options.logoId != 0) {
            toolbar.setLogo(options.logoId);
        }

        if (options.barBackgroundColor != 0) {
            toolbar.setBackgroundColor(options.barBackgroundColor);
        }
        setSupportActionBar(toolbar);

        if (options.isNeedNavigate) {
            toolbar.setNavigationIcon(options.navigateId);
            toolbar.setContentInsetStartWithNavigation(0);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigateUpClicked();
                }
            });
        }
    }

    public void setToolBar(int toolbarId, int titleId, int logoId) {
        toolbar = findView(toolbarId);
        toolbar.setTitle("");
        newCentrenTextView(titleId);
        toolbar.setLogo(logoId);
        setSupportActionBar(toolbar);
    }

    private void newCentrenTextView(String title) {
        if (titleCenter == null) creatorTitleCenter();
        titleCenter.setText(title);
    }

    private void newCentrenTextView(int title) {
        if (titleCenter == null) creatorTitleCenter();
        titleCenter.setText(title);
    }

    private void creatorTitleCenter() {
        titleCenter = new TextView(UI.this);
        titleCenter.setTextColor(ContextCompat.getColor(UI.this, android.R.color.white));
        titleCenter.setTextSize(18);
        titleCenter.setEllipsize(TextUtils.TruncateAt.END);
        titleCenter.setSingleLine(true);
        titleCenter.setMaxEms(15);
        titleCenter.setGravity(Gravity.CENTER);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        titleCenter.setLayoutParams(layoutParams);
        toolbar.addView(titleCenter);
    }

    public Toolbar getToolBar() {
        return toolbar;
    }

    public int getToolBarHeight() {
        if (toolbar != null) {
            return toolbar.getHeight();
        }

        return 0;
    }

    public void onNavigateUpClicked() {
        onBackPressed();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (toolbar != null) {
            toolbar.setTitle("");
            newCentrenTextView(title.toString());
        }
    }

    public void setSubTitle(String subTitle) {
        if (toolbar != null) {
            toolbar.setSubtitle(subTitle);
        }
    }

    protected final Handler getHandler() {
        if (handler == null) {
            handler = new Handler(getMainLooper());
        }
        return handler;
    }

    protected void showKeyboard(boolean isShow) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isShow) {
            if (getCurrentFocus() == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                imm.showSoftInput(getCurrentFocus(), 0);
            }
        } else {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    /**
     * 延时弹出键盘
     *
     * @param focus 键盘的焦点项
     */
    protected void showKeyboardDelayed(View focus) {
        final View viewToFocus = focus;
        if (focus != null) {
            focus.requestFocus();
        }

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (viewToFocus == null || viewToFocus.isFocused()) {
                    showKeyboard(true);
                }
            }
        }, 200);
    }


    public boolean isDestroyedCompatible() {
        if (Build.VERSION.SDK_INT >= 17) {
            return isDestroyedCompatible17();
        } else {
            return destroyed || super.isFinishing();
        }
    }

    @TargetApi(17)
    private boolean isDestroyedCompatible17() {
        return super.isDestroyed();
    }

    /**
     * fragment management
     */
    public TFragment addFragment(TFragment fragment) {
        List<TFragment> fragments = new ArrayList<>(1);
        fragments.add(fragment);

        List<TFragment> fragments2 = addFragments(fragments);
        return fragments2.get(0);
    }

    public List<TFragment> addFragments(List<TFragment> fragments) {
        List<TFragment> fragments2 = new ArrayList<>(fragments.size());

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        boolean commit = false;
        for (int i = 0; i < fragments.size(); i++) {
            // install
            TFragment fragment = fragments.get(i);
            int id = fragment.getContainerId();

            // exists
            TFragment fragment2 = (TFragment) fm.findFragmentById(id);

            if (fragment2 == null) {
                fragment2 = fragment;
                transaction.add(id, fragment);
                commit = true;
            }

            fragments2.add(i, fragment2);
        }

        if (commit) {
            try {
                transaction.commitAllowingStateLoss();
            } catch (Exception e) {

            }
        }

        return fragments2;
    }

    public TFragment switchContent(TFragment fragment) {
        return switchContent(fragment, false);
    }

    protected TFragment switchContent(TFragment fragment, boolean needAddToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(fragment.getContainerId(), fragment);
        if (needAddToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        try {
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {

        }

        return fragment;
    }

    protected boolean displayHomeAsUpEnabled() {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                return onMenuKeyDown();

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    protected boolean onMenuKeyDown() {
        return false;
    }

    private void invokeFragmentManagerNoteStateNotSaved() {
        FragmentManager fm = getSupportFragmentManager();
        ReflectionUtil.invokeMethod(fm, "noteStateNotSaved", null);
    }

    protected void switchFragmentContent(TFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(fragment.getContainerId(), fragment);
        try {
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shake() {

//
//        float currentX = getWindow().getDecorView().getTranslationX();
//        ObjectAnimator animator = ObjectAnimator.ofFloat(getWindow().getDecorView(),"translationX",currentX+50,currentX,currentX+50,currentX);
//        animator.setDuration(800);
//        animator.start();
//
//        Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
//        long[] patter = {200, 200, 200, 200};
//        vibrator.vibrate(patter, -1);


    }

    protected boolean isCompatible(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }


}

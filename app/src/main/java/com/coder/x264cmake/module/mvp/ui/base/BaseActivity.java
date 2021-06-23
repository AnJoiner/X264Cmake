package com.coder.x264cmake.module.mvp.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.coder.mvp.base.BaseMvpActivity;
import com.coder.x264cmake.R;
import com.coder.x264cmake.core.ActivityStackManager;
import com.gyf.immersionbar.ImmersionBar;


import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.ISupportActivity;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportActivityDelegate;
import me.yokeyword.fragmentation.SupportHelper;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public abstract class BaseActivity<T extends ViewBinding> extends BaseMvpActivity<T> implements ISupportActivity {
    protected final SupportActivityDelegate mDelegate = new SupportActivityDelegate(this);
    // 状态栏工具
    protected ImmersionBar mImmersionBar;

    @Override
    public void onCreated(Bundle bundle) {
        // 将当前activity添加进入页面管理
        ActivityStackManager.getInstance().addActivity(this);
        if (isBackBtnListener()) {
            back2PreviousPage();
        }
    }

    @Override
    public void onCreateStart(Bundle bundle) {
        mDelegate.onCreate(bundle);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
//      getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        initImmersionBar();
    }

    @Override
    public void start() {

    }

    @Override
    public void showError(String s) {

    }

    @Override
    public void complete() {

    }

    @Override
    public void showProgressUI(boolean b) {

    }

    @Override
    public void loadMoreStatus(boolean b) {

    }

    @Override
    public void showEmpty(int i) {

    }

    protected void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {

        }
    }

    protected void hideSoftKeyboard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {

        }
    }

    protected void showSoftKeyboard(Context mContext, View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context
                    .INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }


    /**
     * 判断软键盘是否弹出
     */
    protected boolean isShowSoftKeyboard(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (imm.hideSoftInputFromWindow(v.getWindowToken(), 0)) {
                imm.showSoftInput(v, 0);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /****************************************fragmentation start*******************************************/
    @Override
    public SupportActivityDelegate getSupportDelegate() {
        return mDelegate;
    }

    @Override
    public ExtraTransaction extraTransaction() {
        return mDelegate.extraTransaction();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDelegate.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        mDelegate.onDestroy();
        // 将当前页面从页面管理移除
        ActivityStackManager.getInstance().finishActivity(this);
        super.onDestroy();
    }

    /**
     * Note： return mDelegate.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mDelegate.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    /**
     * 不建议复写该方法,请使用 {@link #onBackPressedSupport} 代替
     */
    @Override
    final public void onBackPressed() {
        mDelegate.onBackPressed();
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    @Override
    public void onBackPressedSupport() {
        mDelegate.onBackPressedSupport();
    }

    @Override
    public FragmentAnimator getFragmentAnimator() {
        return mDelegate.getFragmentAnimator();
    }

    @Override
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        mDelegate.setFragmentAnimator(fragmentAnimator);
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return mDelegate.onCreateFragmentAnimator();
    }

    @Override
    public void post(Runnable runnable) {
        mDelegate.post(runnable);
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    public void loadRootFragment(int containerId, @NonNull ISupportFragment toFragment) {
        mDelegate.loadRootFragment(containerId, toFragment);
    }

    public void loadRootFragment(int containerId, ISupportFragment toFragment, boolean addToBackStack, boolean allowAnimation) {
        mDelegate.loadRootFragment(containerId, toFragment, addToBackStack, allowAnimation);
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    public void loadMultipleRootFragment(int containerId, int showPosition, ISupportFragment... toFragments) {
        mDelegate.loadMultipleRootFragment(containerId, showPosition, toFragments);
    }

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     * <p>
     * 建议使用更明确的{@link #showHideFragment(ISupportFragment, ISupportFragment)}
     *
     * @param showFragment 需要show的Fragment
     */
    public void showHideFragment(ISupportFragment showFragment) {
        mDelegate.showHideFragment(showFragment);
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     */
    public void showHideFragment(ISupportFragment showFragment, ISupportFragment hideFragment) {
        mDelegate.showHideFragment(showFragment, hideFragment);
    }

    /**
     * 获取栈内的fragment对象
     */
    public <T extends ISupportFragment> T findFragment(Class<T> fragmentClass) {
        return SupportHelper.findFragment(getSupportFragmentManager(), fragmentClass);
    }

    /**
     * 初始化状态栏工具
     */
    private void initImmersionBar() {
        if (isUseImmersionBar()) {
            mImmersionBar = ImmersionBar.with(this)
                    .fitsSystemWindows(true)
                    .statusBarColor(R.color.black)
                    .statusBarDarkFont(false)
                    .navigationBarColor("#ffffff")
                    .navigationBarDarkIcon(true);

            mImmersionBar.init();
        }
    }

    /**
     * 初始化状态栏工具(白色背景)
     */
    protected void initImmersionBarWhite() {
        mImmersionBar = ImmersionBar.with(this)
                .fitsSystemWindows(false)
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .navigationBarColor("#ffffff")
                .navigationBarDarkIcon(true);

        mImmersionBar.init();
    }

    /**
     * 是否启用状态栏工具
     *
     * @return 默认 true
     */
    protected boolean isUseImmersionBar() {
        return true;
    }

    /**
     * 是否启用返回按钮监听
     *
     * @return true or false
     */
    protected boolean isBackBtnListener() {
        return true;
    }

    /**
     * 是否启用主题背景展示
     *
     * @return 默认true
     */
    protected boolean isUseThemeBackground() {
        return true;
    }

    /**
     * 销毁当前页面并返回上一页
     */
    protected void back2PreviousPage() {
        View backBtn = findViewById(R.id.back_btn);
        if (backBtn != null) {
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }



}

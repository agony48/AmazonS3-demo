package com.mateu.cats.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mateu.cats.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import com.mateu.cats.utils.ActivityStackManager;
import com.mateu.cats.utils.ToastUtils;
import com.mateu.cats.utils.sbar.StatusBarUtil;

public class BaseActivity extends AppCompatActivity {
    private boolean mEnableListenKeyboardState = false;
    private Handler handler = new Handler();
    private long lastClickTime;
    protected Context mContext;
    protected String code = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setStatusBar();
        mContext = this;
        // 添加Activity到堆栈
        ActivityStackManager.getInstance().onCreated(this);
//        code = SharedPreferenceUtil.getString(SharedPreferenceUtil.XCC_DATA_CODE);
//        if (TextUtils.isEmpty(code)) {
//            systemConfig();
//        }
        /*
         * 修复部分 Android 8.0 手机在TargetSDK 大于 26 时，在透明主题时指定 Activity 方向时崩溃的问题
         */
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            fixOrientation();
        }
        super.onCreate(savedInstanceState);
        if (isFullScreen()) {
            // 隐藏Activity顶部的状态栏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        // 监听退出
        if (isObserveLogout()) {
            registerLogoutBoardcast();
        }
        // 清除已存在的 Fragment 防止因没有复用导致叠加显示
        clearAllFragmentExistBeforeCreate();
    }
    /**
     * 清除所有已存在的 Fragment 防止因重建 Activity 时，前 Fragment 没有销毁和重新复用导致界面重复显示
     * 如果有自己实现 Fragment 的复用，请复写此方法并不实现内容
     */
    public void clearAllFragmentExistBeforeCreate() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() == 0) return;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : fragments) {
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commitNow();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isFastClick()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断是否是快速点击
     */
    private long lastClickTimes;

    public boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTimes;
        if (0 < timeD && timeD < 500) {

            return true;
        }
        lastClickTimes = time;
        return false;

    }

    /**
     * 是否隐藏状态栏全屏
     *
     * @return
     */
    protected boolean isFullScreen() {
        return false;
    }

    public void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isUseFullScreenMode()) {
                StatusBarUtil.transparencyBar(this);
            } else {
                StatusBarUtil.setStatusBarColor(this, getStatusBarColor());
            }
            StatusBarUtil.setLightStatusBar(this, isUseBlackFontWithStatusBar(), isUseFullScreenMode());
        }
    }

    /**
     * 是否设置成透明状态栏，即就是全屏模式
     */
    protected boolean isUseFullScreenMode() {
        return false;
    }

    /**
     * 更改状态栏颜色，只有非全屏模式下有效
     */
    protected int getStatusBarColor() {
        return R.color.white;
    }

    /**
     * 是否改变状态栏文字颜色为黑色，默认为黑色
     */
    protected boolean isUseBlackFontWithStatusBar() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEnableListenKeyboardState) {
            addKeyboardStateListener();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeKeyBoardStateListener();
    }

    @Override
    protected void onDestroy() {
        ActivityStackManager.getInstance().onDestroyed(this);
        super.onDestroy();
        // 注销广播
        if (isObserveLogout()) {
            unRegisterLogoutBroadcast();
        }
        //移除所有
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 隐藏键盘
     */
    public void hideInputKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * 设置沉浸式状态栏
     */
    public void setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 隐藏导航键
     */
    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 启动键盘状态监听
     *
     * @param enable
     */
    public void enableKeyboardStateListener(boolean enable) {
        mEnableListenKeyboardState = enable;
    }

    /**
     * 添加键盘显示监听
     */
    private void addKeyboardStateListener() {
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onKeyboardStateChangedListener);
    }

    /**
     * 移除键盘显示监听
     */
    private void removeKeyBoardStateListener() {
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onKeyboardStateChangedListener);
    }

    /**
     * 监听键盘显示状态
     */
    private ViewTreeObserver.OnGlobalLayoutListener onKeyboardStateChangedListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        int mScreenHeight = 0;
        boolean isCurrentActive = false;

        private int getScreenHeight() {
            if (mScreenHeight > 0) {
                return mScreenHeight;
            }
            Point point = new Point();
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
            mScreenHeight = point.y;
            return mScreenHeight;
        }

        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            // 获取当前窗口显示范围
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = getScreenHeight();
            int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
            boolean isActive = false;
            if (Math.abs(keyboardHeight) > screenHeight / 4) {
                isActive = true; // 超过屏幕1/4则表示弹出了输入法
            }

            if (isCurrentActive != isActive) {
                isCurrentActive = isActive;
                onKeyboardStateChanged(isActive, keyboardHeight);
            }
        }
    };

    /**
     * 当软键盘显示时回调
     * 此回调在调用{@link BaseActivity#enableKeyboardStateListener(boolean)}启用监听键盘显示
     *
     * @param isShown
     * @param height
     */
    public void onKeyboardStateChanged(boolean isShown, int height) {

    }

    /**
     * 判断当前主题是否是透明悬浮
     *
     * @return
     */
    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    /**
     * 改变当前的 Activity 的显示方向
     * 解决当前Android 8.0 系统在透明主题时设定显示方向时崩溃的问题
     *
     * @return
     */
    @SuppressLint("WrongConstant")
    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        /*
         * 修复 Android 8.0 手机在TargetSDK 大于 26 时，指定 Activity 方向时崩溃的问题
         */
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }


    public void showToast(String text) {
        //toast
        ToastUtils.showToast(text);
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }


    // 退出应用


    /**
     * 是否监听退出应用操作，默认监听， 如果不像监听， 可复写
     * 此方法并返回 false
     *
     * @return
     */
    public boolean isObserveLogout() {
        return true;
    }

    private void registerLogoutBoardcast() {
        IntentFilter intentFilter = new IntentFilter("com.rong.im.action.logout");
        registerReceiver(logoutRecevier, intentFilter);
    }

    private void unRegisterLogoutBroadcast() {
        unregisterReceiver(logoutRecevier);
    }

    /**
     * 通知通其他注册了登出广播的 Activity 关闭
     */
    public void sendLogoutNotify() {
        //发送广播
        Intent intent = new Intent("com.rong.im.action.logout");
        sendBroadcast(intent);
    }

    private BroadcastReceiver logoutRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    protected static final String TAG = "xuechacha";

    /**
     * 为防止多次重复点击
     *
     * @return
     */
    public synchronized boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    /**
     * 打印调试级别日志
     *
     * @param format
     * @param args
     */
    protected void logDebug(String format, Object... args) {
        logMessage(Log.DEBUG, format, args);
    }

    /**
     * 打印信息级别日志
     *
     * @param format
     * @param args
     */
    protected void logInfo(String format, Object... args) {
        logMessage(Log.INFO, format, args);
    }

    /**
     * 打印错误级别日志
     *
     * @param format
     * @param args
     */
    protected void logError(String format, Object... args) {
        logMessage(Log.ERROR, format, args);
    }

    /**
     * 打印日志
     *
     * @param level
     * @param format
     * @param args
     */
    private void logMessage(int level, String format, Object... args) {
        String formattedString = String.format(format, args);
        switch (level) {
            case Log.DEBUG:
                Log.d(TAG, formattedString);
                break;
            case Log.INFO:
                Log.i(TAG, formattedString);
                break;
            case Log.ERROR:
                Log.e(TAG, formattedString);
                break;
        }
    }

}
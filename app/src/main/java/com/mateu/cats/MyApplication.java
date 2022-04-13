package com.mateu.cats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.multidex.MultiDexApplication;

import com.dds.skywebrtc.SkyEngineKit;
import com.mateu.cats.ui.activity.MainActivity;
import com.mateu.cats.utils.CrashHandler;
import com.mateu.cats.voip.VoipEvent;

public class MyApplication extends MultiDexApplication {
    private static MyApplication appInstance;
    /**
     * 应用是否在后台
     */
    private boolean isAppInForeground;
    private String lastVisibleActivityName;
    private Intent nextOnForegroundIntent;
    private boolean isMainActivityIsCreated;

    /**
     * webRTC 部分内容
     */
    private String username = "";
    private String roomId = "";
    private String otherUserId = "";

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        // 监听 App 前后台变化
        observeAppInBackground();
        //webRTC 内容
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
        // 初始化信令
        SkyEngineKit.init(new VoipEvent());
    }

    public static MyApplication getApplication() {
        return appInstance;
    }

    /**
     * 监听应用是否转为后台
     */
    private void observeAppInBackground() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (activity instanceof MainActivity) {
                    isMainActivityIsCreated = true;
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // 当切换为前台时启动预设的优先显示界面
                if (isMainActivityIsCreated && !isAppInForeground && nextOnForegroundIntent != null) {
                    activity.startActivity(nextOnForegroundIntent);
                    nextOnForegroundIntent = null;
                }

                lastVisibleActivityName = activity.getClass().getSimpleName();
                isAppInForeground = true;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                String pauseActivityName = activity.getClass().getSimpleName();
                /*
                 * 介于 Activity 生命周期在切换画面时现进行要跳转画面的 onResume，
                 * 再进行当前画面 onPause，所以当用户且到后台时肯定会为当前画面直接进行 onPause，
                 * 同过此来判断是否应用在前台
                 */
                if (pauseActivityName.equals(lastVisibleActivityName)) {
                    isAppInForeground = false;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activity instanceof MainActivity) {
                    isMainActivityIsCreated = false;
                }
            }
        });
    }

    /**
     * 当前 App 是否在前台
     *
     * @return
     */
    public boolean isAppInForeground() {
        return isAppInForeground;
    }

    /**
     * 获取最后在前台的 Activity 名称
     *
     * @return
     */
    public String getLastVisibleActivityName() {
        return lastVisibleActivityName;
    }

    /**
     * 设置当 App 切换为前台时启动的 intent，该 intent 在启动后情况
     *
     * @param intent
     */
    public void setOnAppForegroundStartIntent(Intent intent) {
        nextOnForegroundIntent = intent;
    }

    /**
     * 获取最近设置的未触发的启动 intent
     *
     * @return
     */
    public Intent getLastOnAppForegroundStartIntent() {
        return nextOnForegroundIntent;
    }

    /**
     * 判断是否进入到了主界面
     *
     * @return
     */
    public boolean isMainActivityCreated() {
        return isMainActivityIsCreated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }
}

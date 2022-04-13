package com.mateu.cats.utils;

import android.content.Intent;
import android.util.Log;

import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.SkyEngineKit;
import com.mateu.cats.MyApplication;
import com.mateu.cats.ui.activity.SplashActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "MyUncaughtExceptionHand";

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        SkyEngineKit gEngineKit = SkyEngineKit.Instance();
        CallSession session = gEngineKit.getCurrentSession();

        Log.d(TAG, "uncaughtException session = " + session);
        if (session != null) {
            gEngineKit.endCall();
        } else {
            gEngineKit.sendDisconnected(MyApplication.getApplication().getRoomId(), MyApplication.getApplication().getOtherUserId(), true);
        }
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        //如果异常时在AsyncTask里面的后台线程抛出的
        //那么实际的异常仍然可以通过getCause获得
        Throwable cause = ex;
        while (null != cause) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        //stacktraceAsString就是获取的carsh堆栈信息
        final String stacktraceAsString = result.toString();
        printWriter.close();
        restartApp();
    }

    /**
     * 崩溃后 重启应用
     */
    private void restartApp() {
        Intent i = new Intent(MyApplication.getApplication(), SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getApplication().startActivity(i);
    }


}
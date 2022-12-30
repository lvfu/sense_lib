package com.sense.crashapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.StyleRes;


@SuppressLint("StaticFieldLeak")
public class SpiderMan implements Thread.UncaughtExceptionHandler {

    public static final String TAG = "SpiderMan";

    private static Context mContext;
    private static String baseUrl;


    private static int mThemeId = R.style.SpiderManTheme_Dark;

    private static OnCrashListener mOnCrashListener;

    private static boolean isCrash;

    private SpiderMan() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     *
     * @param context
     * @param url    崩溃日志接口完整地址  如 "ip"+ "/api/qms-center-problem/qumanufactureproblemflow/appSaveError"
     *               空字符串则不上传日志到后台
     */
    public static void init(Context context, String url) {
        mContext = context;
        baseUrl = url;
        new SpiderMan();
    }

    @Override
    public void uncaughtException(Thread t, Throwable ex) {

        //处理自己的逻辑
        CrashModel model = SpiderManUtils.parseCrash(mContext, ex);
        if (!isCrash) {
            handleException(model);
        }

        //杀掉App进程
        SpiderManUtils.killApp();
    }

    public static void setTheme(@StyleRes int themeId) {
        mThemeId = themeId;
    }

    protected static int getThemeId() {
        return mThemeId;
    }

    private static void handleException(CrashModel model) {
        isCrash = true;
        model.setBaseUrl(baseUrl);

        Intent intent = new Intent(getContext(), CrashActivity.class);
        intent.putExtra(CrashActivity.CRASH_MODEL, model);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    public static void show(Throwable e) {
        CrashModel model = SpiderManUtils.parseCrash(mContext, e);
        handleException(model);
    }

    public static Context getContext() {
        if (mContext == null) {
            throw new NullPointerException("Please call init method in Application onCreate");
        }
        return mContext;
    }

    public static void setOnCrashListener(OnCrashListener listener) {
        mOnCrashListener = listener;
    }

    public interface OnCrashListener {
        void onCrash(Thread t, Throwable ex);
    }

    private static void callbackCrash(Thread t, Throwable ex) {
        if (mOnCrashListener == null) return;
        mOnCrashListener.onCrash(t, ex);
    }
}
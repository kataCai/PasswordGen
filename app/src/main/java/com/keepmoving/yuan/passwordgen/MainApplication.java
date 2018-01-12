package com.keepmoving.yuan.passwordgen;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.keepmoving.yuan.passwordgen.model.DataCenter;

/**
 * Created by caihanyuan on 2017/11/18.
 */

public class MainApplication extends Application {

    private static Context mContext;

    private static HandlerThread mDataIOThread;
    private static Handler mDataIOHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this.getApplicationContext();

        mDataIOThread = new HandlerThread("DataIOThread");
        mDataIOThread.start();
        mDataIOHandler = new Handler(mDataIOThread.getLooper());
        DataCenter.getInstance();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Handler getDataIOHandler() {
        return mDataIOHandler;
    }
}

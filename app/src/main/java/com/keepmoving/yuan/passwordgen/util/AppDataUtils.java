package com.keepmoving.yuan.passwordgen.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.keepmoving.yuan.passwordgen.MainApplication;

/**
 * Created by wangmiaomiao on 2017/7/10.
 */

public class AppDataUtils {
    static String sAppversion;

    public static String getChannelID() {
        Context context = MainApplication.getContext();
        String channel = "local";
        try {
            channel = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getString("CHANNEL");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return channel;
    }

    public static String getDeviceID() {
        Context context = MainApplication.getContext();
        String deviceID = "";

        String deviceInfo = null;
        if (checkStringNull(deviceInfo)) {
            deviceInfo = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        }

        deviceID = deviceInfo;
        context.getSharedPreferences("share", Context.MODE_PRIVATE).edit().putString("deviceID", deviceID).apply();
        return deviceID;
    }

    public static String getAppVersion() {
        if (!TextUtils.isEmpty(sAppversion)) {
            return sAppversion;
        }
        Context context = MainApplication.getContext();
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            sAppversion = version;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getAppVersionCode() {
        Context context = MainApplication.getContext();
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = info.versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取手机号
     *
     * @return
     */
    public static String getPhoneNumber() {
        String phone = null;
        Context context = MainApplication.getContext();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return phone;
        } else {
            phone = tm.getLine1Number();
        }
        return phone;
    }

    /**
     * 获取运营商名字
     *
     * @return
     */
    public static String getNetworkOperatorName() {
        String networkOperator = null;
        Context context = MainApplication.getContext();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        networkOperator = tm.getNetworkOperatorName();
        return networkOperator;
    }

    /**
     * 获取手机序列号
     *
     * @return
     */
    public static String getSerialNumber() {
        Context context = MainApplication.getContext();
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        } else {
            return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getSimSerialNumber();
        }
    }

    public static boolean checkStringNull(String str) {
        return str == null || str.equals("null") || str.equals("");
    }
}

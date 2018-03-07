package com.keepmoving.yuan.passwordgen.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.keepmoving.yuan.passwordgen.MainApplication;
import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by caihanyuan on 2018/1/9.
 */

public class SharePreferenceData {

    private static final String DATA_NAME = "YUAN_PASSWORD";

    private static final String KEY_USERNAME = "KEY_USERNAME";
    private static final String KEY_LAST_SYNC_TIME = "KEY_LAST_SYNC_TIME"; //上次同步时间
    private static final String KEY_DATA_NEED_SYNC = "KEY_DATA_NEED_SYNC"; //需要同步的数据

    private static final long SYNC_TIMEOUT = 3 * 24 * 60 * 60 * 1000; //同步时间阈值3天

    private static SharedPreferences sSharedPreferences;

    static {
        sSharedPreferences = MainApplication.getContext().getSharedPreferences
                (DATA_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isLogin() {
        String userName = sSharedPreferences.getString(KEY_USERNAME, "");
        return !TextUtils.isEmpty(userName);
    }

    public static String getLoginName() {
        return sSharedPreferences.getString(KEY_USERNAME, "");
    }

    static void logout() {
        Editor editor = sSharedPreferences.edit();
        editor.remove(KEY_USERNAME);
        editor.apply();
    }

    static void login(String userName) {
        Editor editor = sSharedPreferences.edit();
        if (TextUtils.isEmpty(userName)) {
            editor.remove(KEY_USERNAME);
        } else {
            editor.putString(KEY_USERNAME, userName);
        }
        editor.apply();
    }

    /**
     * 是否需要和服务端同步数据
     *
     * @return
     */
    public static boolean needToSync() {
        long lastSynsTime = sSharedPreferences.getLong(KEY_LAST_SYNC_TIME, 0);
        boolean timeout = System.currentTimeMillis() - lastSynsTime >= SYNC_TIMEOUT;
        synchronized (KEY_DATA_NEED_SYNC) {
            String dataCache = sSharedPreferences.getString(KEY_DATA_NEED_SYNC, "");
            return timeout || !TextUtils.isEmpty(dataCache);
        }
    }

    /**
     * 同步成功，清除标志
     */
    public static void endSync() {
        Editor editor = sSharedPreferences.edit();
        editor.putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis());
        synchronized (KEY_DATA_NEED_SYNC) {
            editor.remove(KEY_DATA_NEED_SYNC);
        }
        editor.apply();
    }

    /**
     * 将需要更新的keybean缓存
     *
     * @param keyBean
     * @return
     */
    public static void cacheKeyBean(KeyBean keyBean) {
        synchronized (KEY_DATA_NEED_SYNC) {
            String dataCache = sSharedPreferences.getString(KEY_DATA_NEED_SYNC, "");
            List<KeyBean> keyBeanList = null;
            if (TextUtils.isEmpty(dataCache)) {
                keyBeanList = new ArrayList<>(1);
                keyBeanList.add(keyBean);
            } else {
                Gson gson = new Gson();
                KeyBean[] keyBeans = gson.fromJson(dataCache, KeyBean[].class);
                keyBeanList = new ArrayList<>(Arrays.asList(keyBeans));
                int index = keyBeanList.indexOf(keyBean);
                if (index == -1) {
                    keyBeanList.add(keyBean);
                } else {
                    KeyBean otherBean = keyBeanList.get(index);
                    otherBean.copy(keyBean);
                }
            }

            Gson gson = new Gson();
            dataCache = gson.toJson(keyBeanList);

            Editor editor = sSharedPreferences.edit();
            editor.putString(KEY_DATA_NEED_SYNC, dataCache);
            editor.apply();
        }
    }

    /**
     * keybean同步成功，清除缓存
     *
     * @param keyBean
     */
    public static void clearKeyBean(KeyBean keyBean) {
        synchronized (KEY_DATA_NEED_SYNC) {
            String dataCache = sSharedPreferences.getString(KEY_DATA_NEED_SYNC, "");
            List<KeyBean> keyBeanList = null;
            if (!TextUtils.isEmpty(dataCache)) {
                Gson gson = new Gson();
                KeyBean[] keyBeans = gson.fromJson(dataCache, KeyBean[].class);
                keyBeanList = Arrays.asList(keyBeans);
                keyBeanList.remove(keyBean);
            }

            if (keyBeanList == null || keyBeanList.isEmpty()) {
                Editor editor = sSharedPreferences.edit();
                editor.remove(KEY_DATA_NEED_SYNC);
                editor.apply();
            } else {
                Gson gson = new Gson();
                dataCache = gson.toJson(keyBeanList);

                Editor editor = sSharedPreferences.edit();
                editor.putString(KEY_DATA_NEED_SYNC, dataCache);
                editor.apply();
            }
        }
    }
}

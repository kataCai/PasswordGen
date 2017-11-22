package com.keepmoving.yuan.passwordgen.model;

import android.content.Context;

import com.keepmoving.yuan.passwordgen.MainApplication;
import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;

import java.util.List;

/**
 * Created by caihanyuan on 2017/11/19.
 */

public class DatabaseManager {

    private static DatabaseManager sInstance;
    private final static int DATABASE_VERSION = 1;
    private final static String DATABASE_NAME = "passwords.db";

    private Context mContext;
    private DatabaseHelper mDataHelper;


    public static DatabaseManager getInstance() {
        if (sInstance == null) {
            synchronized (DatabaseManager.class) {
                if (sInstance == null) {
                    sInstance = new DatabaseManager();
                }
            }
        }
        return sInstance;
    }

    private DatabaseManager() {
        mContext = MainApplication.getContext();
        mDataHelper = new DatabaseHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 获取模糊匹配的服务商列表
     *
     * @param support
     * @return
     */
    public List<String> getSupportList(String support) {
        return mDataHelper.getSupportList(support);
    }

    /**
     * 获取模糊匹配的用户名列表
     *
     * @param username
     * @return
     */
    public List<String> getUserNameList(String username) {
        return mDataHelper.getUserNameList(username);
    }

    /**
     * 获取匹配的秘钥信息
     *
     * @param support 服务商名
     * @return
     */
    public KeyBean getMatchKey(String support) {
        return mDataHelper.getMatchKey(support);
    }

    /**
     * 获取匹配的秘钥信息
     *
     * @param support  服务商名
     * @param username 用户名
     * @return
     */
    public KeyBean getMatchKey(String support, String username) {
        return mDataHelper.getMatchKey(support, username);
    }

    /**
     * 新建或者更新秘钥信息
     *
     * @param keyBean
     */
    public void createOrUpdateKey(KeyBean keyBean) {
        mDataHelper.createOrUpdateKey(keyBean);
    }

    /**
     * 查看是否有匹配的秘钥信息
     *
     * @param keyBean
     * @return
     */
    public boolean hasMatchKey(KeyBean keyBean) {
        return mDataHelper.hasMathKey(keyBean);
    }
}

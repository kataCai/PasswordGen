package com.keepmoving.yuan.passwordgen.model;

import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;
import com.keepmoving.yuan.passwordgen.model.bean.UserBean;
import com.keepmoving.yuan.passwordgen.model.iaccess.IKeyAccess;
import com.keepmoving.yuan.passwordgen.model.iaccess.IUserAccess;

import java.util.List;

/**
 * Created by caihanyuan on 2018/1/9.
 * <p>
 * 用户数据中心。
 * <p>
 * 主要功能： 本地数据缓存，网络数据请求，本地和网络数据同步
 */

public class DataCenter implements IKeyAccess, IUserAccess, ServerDataManager.DataCallback {

    private static DataCenter sInstance;

    private DatabaseManager mDatabaseManager;
    private ServerDataManager mServerDataManager;

    public static DataCenter getInstance() {
        if (sInstance == null) {
            synchronized (DataCenter.class) {
                if (sInstance == null) {
                    sInstance = new DataCenter();
                }
            }
        }
        return sInstance;
    }

    private DataCenter() {
        mDatabaseManager = DatabaseManager.getInstance();
        mServerDataManager = ServerDataManager.getInstance();
        mServerDataManager.setDataCallback(this);
    }

    @Override
    public List<String> getSupportList(String support) {
        return mDatabaseManager.getSupportList(support);
    }

    @Override
    public List<String> getUserNameList(String username) {
        return mDatabaseManager.getUserNameList(username);
    }

    @Override
    public KeyBean getMatchKey(String support) {
        return mDatabaseManager.getMatchKey(support);
    }

    @Override
    public KeyBean getMatchKey(String support, String username) {
        return mDatabaseManager.getMatchKey(support, username);
    }

    @Override
    public void createOrUpdateKey(KeyBean keyBean) {
        mDatabaseManager.createOrUpdateKey(keyBean);
        mServerDataManager.createOrUpdateKey(keyBean);
    }

    @Override
    public boolean hasMatchKey(KeyBean keyBean) {
        return mDatabaseManager.hasMatchKey(keyBean);
    }

    @Override
    public boolean isLogin() {
        return mDatabaseManager.isLogin();
    }

    @Override
    public void login(UserBean userBean) {
        mDatabaseManager.login(userBean);
    }

    @Override
    public void logOut() {
        mDatabaseManager.logOut();
    }

    /**
     * 同步本地数据和服务端数据
     */
    public void syncData() {
        if (SharePreferenceData.needToSync()) {
            mServerDataManager.getSupportList();
            mServerDataManager.getUserKeys();
        }
    }

    @Override
    public UserBean getLoginUser() {
        return mDatabaseManager.getLoginUser();
    }

    @Override
    public void onGetSupportList(boolean success, String... suportList) {

    }

    @Override
    public void onGetUserKeys(boolean success, String username, KeyBean... keyBeans) {

    }

    @Override
    public void onKeyUpdate(KeyBean keyBean, boolean success) {

    }
}

package com.keepmoving.yuan.passwordgen.model;

import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;

/**
 * Created by caihanyuan on 2018/1/10.
 * <p>
 * 网路数据中心
 */

public class ServerDataManager {
    private static ServerDataManager sInstance;

    public static ServerDataManager getInstance() {
        if (sInstance == null) {
            synchronized (ServerDataManager.class) {
                if (sInstance == null) {
                    sInstance = new ServerDataManager();
                }
            }
        }
        return sInstance;
    }

    private ServerDataManager() {
    }

    private DataCallback mDataCallback;

    public void setDataCallback(DataCallback mDataCallback) {
        this.mDataCallback = mDataCallback;
    }

    /**
     * 获取所有公司列表
     */
    public void getSupportList() {
    }

    /**
     * 获取当前用户的keys
     */
    public void getUserKeys() {

    }

    /**
     * 增加一个密码键值
     *
     * @param keyBean
     */
    public void createOrUpdateKey(KeyBean keyBean) {
        SharePreferenceData.cacheKeyBean(keyBean);
    }

    /**
     * 数据回调
     */
    public interface DataCallback {

        void onGetSupportList(boolean success, String... suportList);

        void onGetUserKeys(boolean success, String username, KeyBean... keyBeans);

        void onKeyUpdate(KeyBean keyBean, boolean success);
    }
}

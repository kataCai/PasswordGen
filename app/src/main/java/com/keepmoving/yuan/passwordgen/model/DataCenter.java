package com.keepmoving.yuan.passwordgen.model;

import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;
import com.keepmoving.yuan.passwordgen.model.bean.SupportBean;
import com.keepmoving.yuan.passwordgen.model.bean.UserBean;
import com.keepmoving.yuan.passwordgen.model.iaccess.IKeyAccess;
import com.keepmoving.yuan.passwordgen.model.iaccess.IUserAccess;
import com.keepmoving.yuan.passwordgen.util.LogUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by caihanyuan on 2018/1/9.
 * <p>
 * 用户数据中心。
 * <p>
 * 主要功能： 本地数据缓存，网络数据请求，本地和网络数据同步
 */

public class DataCenter implements IKeyAccess, IUserAccess, ServerDataManager.DataCallback, DatabaseHelper.SupportAddListener {
    private static final String TAG = DataCenter.class.getSimpleName();

    private static DataCenter sInstance;

    private DatabaseManager mDatabaseManager;
    private ServerDataManager mServerDataManager;

    private AtomicInteger loadCountAtomic = new AtomicInteger(0);

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
        mDatabaseManager.setAddSupportListener(this);
        if(SharePreferenceData.useServerData()){
            mServerDataManager = ServerDataManager.getInstance();
            mServerDataManager.setDataCallback(this);
        }
    }

    @Override
    public List<String> getWholeSupportList() {
        return mDatabaseManager.getWholeSupportList();
    }

    @Override
    public List<String> getMatchSupportList(String support) {
        return mDatabaseManager.getMatchSupportList(support);
    }

    @Override
    public List<String> getUserNameList(String username) {
        return mDatabaseManager.getUserNameList(username);
    }

    @Override
    public List<KeyBean> getWholeKeyBeanList() {
        return mDatabaseManager.getWholeKeyBeanList();
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
        if(mServerDataManager != null){
            mServerDataManager.createOrUpdateKey(keyBean);
        }
        if(mDatabaseManager != null){
            mDatabaseManager.createOrUpdateKey(keyBean);
        }
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
        if (SharePreferenceData.needToSync() && SharePreferenceData.useServerData()) {
            loadCountAtomic.set(2);
            mServerDataManager.getSupportList();

            List<KeyBean> keyBeanList = SharePreferenceData.getDirtyKeyBeans();
            if (keyBeanList == null || keyBeanList.isEmpty()) {
                mServerDataManager.getUserKeys();
            } else {
                for (KeyBean keyBean : keyBeanList) {
                    createOrUpdateKey(keyBean);
                }
                int count = loadCountAtomic.decrementAndGet();
                if (count == 0) {
                    SharePreferenceData.endSync();
                }
            }

        }
    }

    @Override
    public UserBean getLoginUser() {
        return mDatabaseManager.getLoginUser();
    }

    @Override
    public void onGetSupportList(boolean success, List<SupportBean> suportList) {
        if (success) {
            mDatabaseManager.syncSupportList(suportList);
        }
        int count = loadCountAtomic.decrementAndGet();
        if (count == 0) {
            SharePreferenceData.endSync();
        }
    }

    @Override
    public void onGetUserKeys(boolean success, List<KeyBean> keyBeans) {
        if (success) {
            mDatabaseManager.syncKeyList(keyBeans);
        }
        int count = loadCountAtomic.decrementAndGet();
        if (count == 0) {
            SharePreferenceData.endSync();
        }
    }

    @Override
    public void onKeyUpdate(KeyBean keyBean, boolean success) {
        if (success) {
            mDatabaseManager.createOrUpdateKey(keyBean);
        }
    }

    @Override
    public void needAddSupport(final String support) {
        if (SharePreferenceData.useServerData()) {
            BmobQuery<SupportBean> query = new BmobQuery<>();
            query.addWhereEqualTo("name", support);
            query.findObjects(new FindListener<SupportBean>() {
                @Override
                public void done(List<SupportBean> list, BmobException e) {
                    if (e == null) {
                        if (list == null || list.isEmpty()) {
                            LogUtils.d(TAG, "support:%s is not exist in server", support);

                            final SupportBean supportBean = new SupportBean();
                            supportBean.setName(support);
                            supportBean.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        LogUtils.d(TAG, "add support %s to server success", support);
                                        supportBean.setObjectId(objectId);
                                        mDatabaseManager.addSupport(supportBean);
                                    } else {
                                        LogUtils.e(e);
                                    }
                                }
                            });
                        } else {
                            LogUtils.d(TAG, "support:%s already exist in server", support);
                        }
                    } else {
                        LogUtils.e(e);
                    }
                }
            });
        } else {
            final SupportBean supportBean = new SupportBean();
            supportBean.setName(support);
            mDatabaseManager.addSupport(supportBean);
        }
    }
}

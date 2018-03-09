package com.keepmoving.yuan.passwordgen.model;

import android.text.TextUtils;

import com.keepmoving.yuan.passwordgen.MainApplication;
import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;
import com.keepmoving.yuan.passwordgen.model.bean.SupportBean;
import com.keepmoving.yuan.passwordgen.util.LogUtils;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

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
        Bmob.initialize(MainApplication.getContext(), "ae509c72a9f71ac3b17c7e0d1c77b0cc");
    }

    private DataCallback mDataCallback;

    public void setDataCallback(DataCallback mDataCallback) {
        this.mDataCallback = mDataCallback;
    }

    /**
     * 获取所有公司列表
     */
    public void getSupportList() {
        BmobQuery<SupportBean> query = new BmobQuery<SupportBean>();
        query.setLimit(Integer.MAX_VALUE);
        query.findObjects(new FindListener<SupportBean>() {
            @Override
            public void done(List<SupportBean> list, BmobException e) {
                if (e == null) {
                    LogUtils.d("getSupportList from server success, size:" + list.size());
                    if (mDataCallback != null) {
                        mDataCallback.onGetSupportList(true, list);
                    }
                } else {
                    LogUtils.e(e);
                    if (mDataCallback != null) {
                        mDataCallback.onGetSupportList(false, null);
                    }
                }
            }
        });
    }

    /**
     * 获取当前用户的keys
     */
    public void getUserKeys() {
        BmobQuery<KeyBean> query = new BmobQuery<KeyBean>();
        query.setLimit(Integer.MAX_VALUE);
        query.addWhereEqualTo("accountName", SharePreferenceData.getLoginName());
        query.findObjects(new FindListener<KeyBean>() {
            @Override
            public void done(List<KeyBean> list, BmobException e) {
                if (e == null) {
                    LogUtils.d("getUserKeys from server success, size:" + list.size());
                    if (mDataCallback != null) {
                        mDataCallback.onGetUserKeys(true, list);
                    }
                } else {
                    LogUtils.e(e);
                    if (mDataCallback != null) {
                        mDataCallback.onGetUserKeys(false, null);
                    }
                }
            }
        });
    }

    /**
     * 增加一个密码键值
     *
     * @param keyBean
     */
    public void createOrUpdateKey(final KeyBean keyBean) {
        List<KeyBean> needUpdateList = SharePreferenceData.cacheKeyBean(keyBean);
        for (final KeyBean bean : needUpdateList) {
            final String keyId = bean.getObjectId();

            if (TextUtils.isEmpty(keyId)) {
                bean.save(new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        if (e == null) {
                            LogUtils.i(LogUtils.TAG, "key %s save to server success", objectId);
                            bean.setObjectId(objectId);
                            if (mDataCallback != null) {
                                mDataCallback.onKeyUpdate(bean, true);
                            }
                            SharePreferenceData.clearKeyBean(bean);
                        } else {
                            LogUtils.e(e);
                            if (mDataCallback != null) {
                                mDataCallback.onKeyUpdate(bean, false);
                            }
                        }
                    }
                });
            } else {
                bean.update(keyId, new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            LogUtils.i(LogUtils.TAG, "key %s update to server success", keyId);
                            SharePreferenceData.clearKeyBean(bean);
                            if (mDataCallback != null) {
                                mDataCallback.onKeyUpdate(bean, true);
                            }
                        } else {
                            LogUtils.e(e);
                            if (mDataCallback != null) {
                                mDataCallback.onKeyUpdate(bean, false);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 数据回调
     */
    public interface DataCallback {

        void onGetSupportList(boolean success, List<SupportBean> suportList);

        void onGetUserKeys(boolean success, List<KeyBean> keyBeans);

        void onKeyUpdate(KeyBean keyBean, boolean success);
    }
}

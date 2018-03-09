package com.keepmoving.yuan.passwordgen.model;

import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;
import com.keepmoving.yuan.passwordgen.model.bean.SupportBean;
import com.keepmoving.yuan.passwordgen.model.bean.UserBean;
import com.keepmoving.yuan.passwordgen.model.iaccess.IKeyAccess;
import com.keepmoving.yuan.passwordgen.model.iaccess.IUserAccess;

import java.util.List;

/**
 * Created by caihanyuan on 2017/11/19.
 */

public class DatabaseManager implements IKeyAccess, IUserAccess {

    private static DatabaseManager sInstance;
    private final static int DATABASE_VERSION = 1;

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
        mDataHelper = DatabaseHelper.getInstance(DATABASE_VERSION);
    }

    /**
     * 获取模糊匹配的服务商列表
     *
     * @param support
     * @return
     */
    public List<String> getMatchSupportList(String support) {
        return mDataHelper.getMatchSupportList(support);
    }

    /**
     * 更新供应商列表
     *
     * @param supportBeanList
     */
    public void syncSupportList(List<SupportBean> supportBeanList) {
        mDataHelper.syncSupportList(supportBeanList);
    }

    /**
     * 更新密码键值表
     * @param keyBeanList
     */
    public void syncKeyList(List<KeyBean> keyBeanList){
        mDataHelper.syncKeyList(keyBeanList);
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
     * 添加一个供应商
     *
     * @param supportBean
     */
    public void addSupport(SupportBean supportBean) {
        mDataHelper.addSupport(supportBean);
    }

    /**
     * 查看是否有匹配的秘钥信息
     *
     * @param keyBean
     * @return
     */
    public boolean hasMatchKey(KeyBean keyBean) {
        return mDataHelper.hasMatchKey(keyBean);
    }

    @Override
    public boolean isLogin() {
        return SharePreferenceData.isLogin();
    }

    @Override
    public void login(UserBean userBean) {
        mDataHelper.login(userBean);
    }

    @Override
    public void logOut() {
        mDataHelper.logOut();
    }

    @Override
    public UserBean getLoginUser() {
        return mDataHelper.getLoginUser();
    }

    public void setAddSupportListener(DatabaseHelper.SupportAddListener supportListener) {
        mDataHelper.setSupportAddListener(supportListener);
    }
}

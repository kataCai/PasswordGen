package com.keepmoving.yuan.passwordgen.model.iaccess;

import com.keepmoving.yuan.passwordgen.model.bean.UserBean;

/**
 * Created by caihanyuan on 2018/1/9.
 * <p>
 * 用户数据接口
 */

public interface IUserAccess {

    /**
     * 是否登录
     *
     * @return
     */
    boolean isLogin();

    /**
     * 登录
     */
    void login(UserBean userBean);

    /**
     * 注销
     */
    void logOut();

    /**
     * 获取当前登录的用户信息
     *
     * @return
     */
    UserBean getLoginUser();
}

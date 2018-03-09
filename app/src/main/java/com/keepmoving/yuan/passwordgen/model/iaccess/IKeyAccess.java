package com.keepmoving.yuan.passwordgen.model.iaccess;

import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;

import java.util.List;

/**
 * Created by caihanyuan on 2018/1/9.
 * <p>
 * 密码键值数据接口
 */

public interface IKeyAccess {

    /**
     * 获取所有support数据
     */
    List<String> getWholeSupportList();

    /**
     * 获取模糊匹配的服务商列表
     *
     * @param support
     * @return
     */
    List<String> getMatchSupportList(String support);

    /**
     * 获取模糊匹配的用户名列表
     *
     * @param username
     * @return
     */
    List<String> getUserNameList(String username);

    /**
     * 获取所有密码键值数据
     * @return
     */
    List<KeyBean> getWholeKeyBeanList();

    /**
     * 获取匹配的秘钥信息
     *
     * @param support 服务商名
     * @return
     */
    KeyBean getMatchKey(String support);

    /**
     * 获取匹配的秘钥信息
     *
     * @param support  服务商名
     * @param username 用户名
     * @return
     */
    KeyBean getMatchKey(String support, String username);

    /**
     * 新建或者更新秘钥信息
     *
     * @param keyBean
     */
    void createOrUpdateKey(KeyBean keyBean);

    /**
     * 查看是否有匹配的秘钥信息
     *
     * @param keyBean
     * @return
     */
    boolean hasMatchKey(KeyBean keyBean);
}

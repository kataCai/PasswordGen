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
     * 获取模糊匹配的服务商列表
     *
     * @param support
     * @return
     */
    public List<String> getSupportList(String support);

    /**
     * 获取模糊匹配的用户名列表
     *
     * @param username
     * @return
     */
    public List<String> getUserNameList(String username);

    /**
     * 获取匹配的秘钥信息
     *
     * @param support 服务商名
     * @return
     */
    public KeyBean getMatchKey(String support);

    /**
     * 获取匹配的秘钥信息
     *
     * @param support  服务商名
     * @param username 用户名
     * @return
     */
    public KeyBean getMatchKey(String support, String username);

    /**
     * 新建或者更新秘钥信息
     *
     * @param keyBean
     */
    public void createOrUpdateKey(KeyBean keyBean);

    /**
     * 查看是否有匹配的秘钥信息
     *
     * @param keyBean
     * @return
     */
    public boolean hasMatchKey(KeyBean keyBean);
}

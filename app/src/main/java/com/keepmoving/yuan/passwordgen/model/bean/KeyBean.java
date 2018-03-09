package com.keepmoving.yuan.passwordgen.model.bean;

import android.text.TextUtils;

import com.keepmoving.yuan.passwordgen.model.SharePreferenceData;

import cn.bmob.v3.BmobObject;

/**
 * Created by caihanyuan on 2017/11/19.
 */

public class KeyBean extends BmobObject {
    private String support;
    private String accountName;
    private String username;
    private int version;
    private int passwordLen;
    private int type;
    private String customPassword;


    public KeyBean() {
    }

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getPasswordLen() {
        return passwordLen;
    }

    public void setPasswordLen(int passwordLen) {
        this.passwordLen = passwordLen;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCustomPassword() {
        return customPassword;
    }

    public void setCustomPassword(String customPassword) {
        this.customPassword = customPassword;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof KeyBean)) {
            return false;
        } else {
            KeyBean other = (KeyBean) obj;
            return TextUtils.equals(support, other.support)
                    && TextUtils.equals(accountName, other.accountName)
                    && TextUtils.equals(username, other.username);
        }
    }

    public void copy(KeyBean keyBean) {
        setObjectId(keyBean.getObjectId());
        support = keyBean.support;
        accountName = keyBean.accountName;
        username = keyBean.username;
        version = keyBean.version;
        passwordLen = keyBean.passwordLen;
        type = keyBean.type;
        customPassword = keyBean.customPassword;
    }
}

package com.keepmoving.yuan.passwordgen.model.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.keepmoving.yuan.passwordgen.model.SharePreferenceData;

/**
 * Created by caihanyuan on 2017/11/19.
 */

public class KeyBean implements Parcelable {
    private String support;
    private String accountName;
    private String username;
    private int version;
    private int passwordLen;


    public KeyBean() {
    }

    protected KeyBean(Parcel in) {
        support = in.readString();
        accountName = in.readString();
        username = in.readString();
        version = in.readInt();
        passwordLen = in.readInt();
    }

    public static final Creator<KeyBean> CREATOR = new Creator<KeyBean>() {
        @Override
        public KeyBean createFromParcel(Parcel in) {
            return new KeyBean(in);
        }

        @Override
        public KeyBean[] newArray(int size) {
            return new KeyBean[size];
        }
    };

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public String getAccountName() {
        if (TextUtils.isEmpty(accountName)) {
            accountName = SharePreferenceData.getLoginName();
        }
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

    @Override
    public int describeContents() {
        return 0;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(support);
        dest.writeString(accountName);
        dest.writeString(username);
        dest.writeInt(version);
        dest.writeInt(passwordLen);
    }

    public void copy(KeyBean keyBean) {
        support = keyBean.support;
        accountName = keyBean.accountName;
        username = keyBean.username;
        version = keyBean.version;
        passwordLen = keyBean.passwordLen;
    }
}

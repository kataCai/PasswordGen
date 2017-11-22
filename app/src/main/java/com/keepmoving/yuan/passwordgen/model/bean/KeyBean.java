package com.keepmoving.yuan.passwordgen.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by caihanyuan on 2017/11/19.
 */

public class KeyBean implements Parcelable {
    private String support;
    private String username;
    private int version;
    private int passwordLen;

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
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

    public KeyBean() {
    }

    protected KeyBean(Parcel in) {
        support = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(support);
        dest.writeString(username);
        dest.writeInt(version);
        dest.writeInt(passwordLen);
    }
}

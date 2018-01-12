package com.keepmoving.yuan.passwordgen.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by caihanyuan on 2018/1/9.
 * <p>
 * 用户信息
 */

public class UserBean implements Parcelable {

    private int id;
    private String token;
    private String name;
    private String company;
    private int check;

    public UserBean() {
    }

    protected UserBean(Parcel in) {
        id = in.readInt();
        token = in.readString();
        name = in.readString();
        company = in.readString();
        check = in.readInt();
    }

    public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {
        @Override
        public UserBean createFromParcel(Parcel in) {
            return new UserBean(in);
        }

        @Override
        public UserBean[] newArray(int size) {
            return new UserBean[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(token);
        dest.writeString(name);
        dest.writeString(company);
        dest.writeInt(check);
    }
}

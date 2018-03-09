package com.keepmoving.yuan.passwordgen.model.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by caihanyuan on 2018/1/9.
 * <p>
 * 用户信息
 */

public class UserBean extends BmobObject {

    private String token;
    private String name;
    private String company;
    private int check;

    public UserBean() {
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
}

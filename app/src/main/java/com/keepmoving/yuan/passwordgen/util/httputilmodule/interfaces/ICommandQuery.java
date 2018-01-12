package com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces;

import retrofit2.Call;

/**
 * Created by liuqiang on 2017/11/21.
 */

// I，执行查询的接口；B，返回的javabean类型。本模块类同
public interface ICommandQuery<I, B> {
    Call<B> commandQuery(I interfaceObj);
}

package com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces;

/**
 * Created by liuqiang on 2017/6/28.
 * 通用数据加载和callback接口
 */

public interface DataSourceInterface<T> {
    void loadData(GetDataCallback<T> Callback);

    interface GetDataCallback<T> {
        void onGetDate(T data);
        void onFailure(int statusCode, String description, String errorMsg, Throwable t);
    }
}

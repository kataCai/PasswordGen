package com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces;

import com.keepmoving.yuan.passwordgen.model.bean.BaseBean;

/**
 * Created by caihanyuan on 2017/5/31.
 */

public interface IResponseCallback<B extends BaseBean> {
    void success(B bean);

    void failure(int statusCode, String description, String errorMsg, Throwable t);

    int preSuccess(B bean);
}

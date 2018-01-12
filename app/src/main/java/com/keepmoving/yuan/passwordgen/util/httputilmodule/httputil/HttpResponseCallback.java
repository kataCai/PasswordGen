package com.keepmoving.yuan.passwordgen.util.httputilmodule.httputil;

import com.keepmoving.yuan.passwordgen.model.bean.BaseBean;
import com.keepmoving.yuan.passwordgen.util.LogUtils;
import com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces.IResponseCallback;

/**
 * Created by liuqiang on 2017/11/21.
 */

public abstract class HttpResponseCallback<B extends BaseBean> implements IResponseCallback<B> {
    @Override
    public void failure(int statusCode, String description, String errorMsg, Throwable t) {
        LogUtils.e("HttpUtilModule", t, "Http Request Error! From : %s, Message: %s; Error Code: %d",
                description, errorMsg, statusCode);
    }

    /**
     * 统一处理接口级的错误
     */
    @Override
    public int preSuccess(B bean) {
        switch (bean.getCode()) {
            case 400:
            case 500:
            case 1000:
            case 4000:
            case 4001:
            case 4002:
            case 4003:
                LogUtils.d("preSuccess", "preSuccess: " + bean.getMsg());
                return HttpUtil.PRE_FAIL;
            default:
                return HttpUtil.PRE_SUCCESS;
        }

    }
}

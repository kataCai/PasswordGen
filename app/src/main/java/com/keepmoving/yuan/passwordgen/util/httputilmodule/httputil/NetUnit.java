package com.keepmoving.yuan.passwordgen.util.httputilmodule.httputil;

import com.keepmoving.yuan.passwordgen.model.bean.BaseBean;
import com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces.DataSourceInterface.GetDataCallback;
import com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces.ICommandQuery;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;

/**
 * Created by caihanyuan on 2018/1/10.
 * <p>
 * 网络请求单元.
 * <p>
 * 当用装饰模式包装的时候会进行链式调用，里层的请求先执行，然后将结果传给上层，
 * 上层可用这个结果作为请求参数进行请求，重复这个过程，直到到达最终请求。
 * <p>
 * androidTest/java/com.kingsoft.loan/netunit有单元测试。
 */

public abstract class NetUnit<T, B extends BaseBean> {

    /**
     * 外层包装器
     */
    protected NetUnit mWrapper;

    /**
     * 内部单元
     */
    protected NetUnit mInner;

    /**
     * 父任务
     */
    protected NetUnit mParentUnit;

    /**
     * 任务信号控制器, 由父任务传递进来，子任务完成后减一
     */
    protected CountDownLatch mCountDown;

    /**
     * 服务器返回数据
     */
    protected B mData;

    /**
     * 最大重试次数
     */
    protected int mMaxRetry = 0;

    /**
     * 请求回调接口
     */
    private GetDataCallback<B> mCallback;

    /**
     * 请求接口class
     */
    private Class<T> mRequestClass;

    /**
     * 请求体
     */
    private Call mCall;

    /**
     * 请求构造器
     */
    private HttpUtil.Builder mHttpBuilder;

    public NetUnit() {
    }

    public NetUnit(NetUnit unit) {
        mInner = unit;
        unit.mWrapper = this;
    }

    public NetUnit(GetDataCallback callback) {
        mCallback = callback;
    }

    public NetUnit getWrapper() {
        return mWrapper;
    }

    public void setMaxRetry(int mMaxRetry) {
        this.mMaxRetry = mMaxRetry;
    }

    public void setCallback(GetDataCallback<B> mCallback) {
        this.mCallback = mCallback;
    }

    public void setCountDown(CountDownLatch countDownLatch) {
        mCountDown = countDownLatch;
    }

    /**
     * 服务器返回的数据
     * <p>
     * 其他相关的请求单元可以拿到这个单元的数据
     *
     * @return
     */
    public B getData() {
        return mData;
    }

    /**
     * 执行网络异步请求任务
     */
    public void call() {
        if (mInner != null) {
            mInner.call();
        } else {
            selfCall(null);
        }
    }

    /**
     * 执行网络同步请求任务
     */
    public void execute() {
        if (mInner != null) {
            mInner.execute();
        } else {
            selfExecute(null);
        }
    }

    /**
     * 执行网络异步请求任务
     *
     * @param callback 请求结果回调接口
     */
    public void call(GetDataCallback<B> callback) {
        mCallback = callback;
        call();
    }


    /**
     * 执行网络同步请求任务
     */
    public void execute(GetDataCallback<B> callback) {
        mCallback = callback;
        execute();
    }

    /**
     * 取消任务
     */
    public void cancel() {
        if (mCall != null) {
            mCall.cancel();
        }
        if (mCountDown != null) {
            mCountDown.countDown();
        }
    }

    /**
     * 自己的异步接口请求
     */
    private void selfCall(BaseBean baseBean) {
        if (mCall != null && mHttpBuilder != null) {
            mCall = mHttpBuilder.call(mCall);
        } else {
            createHttpBuilder(baseBean);
            mHttpBuilder.call();
        }
    }

    /**
     * 自己的同步接口请求
     */
    private void selfExecute(BaseBean baseBean) {
        if (mCall != null && mHttpBuilder != null) {
            mCall = mHttpBuilder.execute(mCall);
        } else {
            createHttpBuilder(baseBean);
            mHttpBuilder.execute();
        }
    }

    private void createHttpBuilder(BaseBean baseBean) {
        Class<T> requestClass = buildRequesetClass();
        ICommandQuery<T, B> commandQuery = buildQuery(baseBean);

        if (requestClass == null) {
            throw new RuntimeException("requestClass must set in NetUnit");
        }
        if (commandQuery == null) {
            throw new RuntimeException("commandQuery must set in NetUnit");
        }
        mHttpBuilder = HttpUtil.builder();
        mHttpBuilder.ClassService(requestClass);
        mHttpBuilder.CommandQuery(commandQuery);
        mHttpBuilder.ResponseCallback(buildResponseCallback());
        mHttpBuilder.maxRetry(mMaxRetry);
    }


    /**
     * 生成请求接口Class
     * <p>
     *
     * @return
     */
    private Class<T> buildRequesetClass() {
        if (mRequestClass == null) {
            Type type = getClass().getGenericSuperclass();
            Class<T> requestClass = null;
            if (type instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                if (types.length > 0) {
                    requestClass = (Class<T>) types[0];
                }
            }
            if (requestClass == null) {
                throw new RuntimeException("T in <T, B> not set", null);
            }

            mRequestClass = requestClass;
        }
        return mRequestClass;
    }

    /**
     * 生成请求器
     *
     * @return
     */
    protected abstract Call<B> buildCall(T requestObj, BaseBean baseBean);

    /**
     * @param baseBean 上个节点返回的结果
     * @return
     */
    private ICommandQuery<T, B> buildQuery(BaseBean baseBean) {
        return new CommandQuery(baseBean);
    }

    /**
     * 网络完成回调接口，默认直接将结果抛给上层处理。子类可重写
     *
     * @return
     */
    protected NetUnitResponseCallback buildResponseCallback() {
        return new NetUnitResponseCallback();
    }

    /**
     * 网络请求成功
     *
     * @param bean
     */
    protected void onSuccess(B bean) {
        mData = bean;
        if (mCallback != null) {
            mCallback.onGetDate(bean);
        }
        //链式请求，通知上个节点请求
        if (mWrapper != null) {
            mWrapper.selfCall(bean);
        }
        //通知父任务
        if (mCountDown != null) {
            mCountDown.countDown();
        }
    }

    /**
     * 网络请求失败
     *
     * @param statusCode
     * @param description
     * @param errorMsg
     * @param t
     */
    protected void onFailure(int statusCode, String description, String errorMsg, Throwable t) {
        if (mCallback != null) {
            mCallback.onFailure(statusCode, description, errorMsg, t);
        }
        //链式请求，通知上个节点请求失败
        if (mWrapper != null) {
            mWrapper.cancel();
            mWrapper.onFailure(statusCode, description, errorMsg, t);
        }
        //通知父任务
        if (mParentUnit != null) {
            mParentUnit.onFailure(statusCode, description, errorMsg, t);
            mParentUnit.cancel();
        }
    }

    /**
     * 请求结果回调
     */
    protected class NetUnitResponseCallback extends HttpResponseCallback<B> {
        @Override
        public void success(B bean) {
            onSuccess(bean);
        }

        @Override
        public void failure(int statusCode, String description, String errorMsg, Throwable t) {
            super.failure(statusCode, description, errorMsg, t);
            onFailure(statusCode, description, errorMsg, t);
        }
    }

    /**
     * 请求生成封装
     */
    protected class CommandQuery implements ICommandQuery<T, B> {

        BaseBean mRelayData; //请求依赖的数据

        public CommandQuery(BaseBean relayBean) {
            this.mRelayData = relayBean;
        }

        @Override
        public Call<B> commandQuery(T interfaceObj) {
            return buildCall(interfaceObj, mRelayData);
        }
    }
}

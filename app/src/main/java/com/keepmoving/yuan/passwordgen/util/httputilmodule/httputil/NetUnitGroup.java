package com.keepmoving.yuan.passwordgen.util.httputilmodule.httputil;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.keepmoving.yuan.passwordgen.model.bean.BaseBean;
import com.keepmoving.yuan.passwordgen.util.LogUtils;
import com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces.DataSourceInterface;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;

/**
 * Created by caihanyuan on 2018/1/11.
 * <p>
 * 请求单元容器。
 * <p>
 * 管理多个请求，这些请求结果集作为容器的返回结果
 * <p>
 * androidTest/java/com.kingsoft.loan/netunit有单元测试。
 */

public class NetUnitGroup extends NetUnit {

    private ArrayList<NetUnit> mNetUnitList;
    private AsyncTask mTask;

    protected boolean mFail = false;

    public NetUnitGroup() {
        mNetUnitList = new ArrayList<>();
    }

    public NetUnitGroup(DataSourceInterface.GetDataCallback callback) {
        super(callback);
        mNetUnitList = new ArrayList<>();
    }

    public NetUnitGroup addNetUnit(NetUnit netUnit) {
        synchronized (mNetUnitList) {
            mNetUnitList.add(netUnit);
            netUnit.mParentUnit = this;
        }
        return this;
    }

    public NetUnitGroup removeNetUnit(NetUnit netUnit) {
        synchronized (mNetUnitList) {
            mNetUnitList.remove(netUnit);
            netUnit.mParentUnit = null;
        }
        return this;
    }

    @Override
    protected Call buildCall(Object requestObj, BaseBean baseBean) {
        return null;
    }

    /**
     * 发起多个子任务请求
     */
    @SuppressLint("StaticFieldLeak")
    public void call() {
        if (mTask != null) {
            cancel();
        }
        mTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mFail = false;

                synchronized (mNetUnitList) {
                    mCountDown = new CountDownLatch(mNetUnitList.size());

                    for (NetUnit netUnit : mNetUnitList) {
                        netUnit.setCountDown(mCountDown);
                        netUnit.call();
                    }
                }
                try {
                    mCountDown.await();
                } catch (InterruptedException e) {
                    LogUtils.e(e);
                }

                //子任务结束，如果所有都成功，则将数据集封装回调
                if (!mFail) {
                    GroupData data = null;
                    synchronized (mNetUnitList) {
                        data = new GroupData();
                        ArrayList<BaseBean> dataList = new ArrayList<>(mNetUnitList.size());
                        for (NetUnit netUnit : mNetUnitList) {
                            dataList.add(netUnit.getData());
                        }
                        data.setData(dataList);
                    }
                    onSuccess(data);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * 取消所有子任务
     */
    public void cancel() {
        mFail = true; //取消也标志为失败，不走onSuccess
        synchronized (mNetUnitList) {
            for (NetUnit netUnit : mNetUnitList) {
                netUnit.cancel();
            }
        }
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    @Override
    protected void onFailure(int statusCode, String description, String errorMsg, Throwable t) {
        mFail = true;
        cancel();
        super.onFailure(statusCode, description, errorMsg, t);
    }

    /**
     * 子任务数据集
     */
    public static class GroupData extends BaseBean {
        private ArrayList<BaseBean> data;

        public ArrayList<BaseBean> getData() {
            return data;
        }

        public void setData(ArrayList<BaseBean> data) {
            this.data = data;
        }
    }
}

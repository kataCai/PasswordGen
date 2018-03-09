package com.keepmoving.yuan.passwordgen.util.httputilmodule.httputil;

import android.support.annotation.NonNull;

import com.keepmoving.yuan.passwordgen.BuildConfig;
import com.keepmoving.yuan.passwordgen.R;
import com.keepmoving.yuan.passwordgen.model.bean.BaseBean;
import com.keepmoving.yuan.passwordgen.util.LogUtils;
import com.keepmoving.yuan.passwordgen.util.ToastUtils;
import com.keepmoving.yuan.passwordgen.util.httputilmodule.annotation.Description;
import com.keepmoving.yuan.passwordgen.util.httputilmodule.interfaces.ICommandQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by liuqiang on 2017/11/21.
 * Modify by caidong on 2017/12/21
 */

public class HttpUtil {
    public static int PRE_SUCCESS = 0;
    public static int PRE_FAIL = 1;
    public static int PRE_PROCESSED = 2;

    private static final String TAG = HttpUtil.class.getSimpleName();
    private static final Map<String, Call> CALL_MAP = new HashMap<>();
    private static final String BASE_URL = BuildConfig.HOST;

    private static final Map<String, Retrofit> sDomainMap = new HashMap<>();
    private static OkHttpClient sHttpClient;

    private HttpUtil() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static boolean checkStringNull(String str) {
        return str == null || str.equals("null") || str.equals("");
    }

    // 取消某个界面所有请求，requestName为空即可。取消单个请求，需加入requestName
    public static synchronized void cancel(Object tag, String requestName) {
        if (tag == null) {
            return;
        }

        if (!checkStringNull(requestName)) {
            // 单个取消
            String keyStr = tag.toString() + requestName;
            synchronized (CALL_MAP) {
                Call call = CALL_MAP.get(keyStr);
                if (call != null) {
                    call.cancel();
                    CALL_MAP.remove(keyStr);
                }
            }
        } else {
            synchronized (CALL_MAP) {
                List<String> list = new ArrayList<>();
                for (String key : CALL_MAP.keySet()) {
                    if (key.startsWith(tag.toString())) {
                        CALL_MAP.get(key).cancel();
                        list.add(key);
                    }
                }

                for (String key : list) {
                    CALL_MAP.remove(key);
                }
            }
        }
    }

    private static synchronized void putCall(Object tag, String requestName, Call call) {
        if (tag == null) {
            return;
        }

        synchronized (CALL_MAP) {
            CALL_MAP.put(tag.toString() + requestName, call);
        }
    }

    private static synchronized void removeCall(Object tag, String requestName) {
        synchronized (CALL_MAP) {
            CALL_MAP.remove(tag.toString() + requestName);
        }
    }

    public static class Builder<I, B extends BaseBean> {
        private ICommandQuery mCommandQuery = null;
        private HttpResponseCallback mResponseCallback = null;
        private Class<I> mClassService = null;
        private String mBaseUrl = null;
        private Object mTag = null;
        private String mRequestName = "";     // 用于取消请求
        //请求重试相关
        private int mMaxRetry = 0;
        private int mRetryNum = 0;

        private boolean mSync = false; // 是否同步执行, 外部不可设置

        private Builder() {
            mBaseUrl = BASE_URL;
        }

        // 必须
        public Builder CommandQuery(ICommandQuery iCommandQuery) {
            this.mCommandQuery = iCommandQuery;
            return this;
        }

        // 必须
        public Builder ResponseCallback(HttpResponseCallback responseCallback) {
            this.mResponseCallback = responseCallback;
            return this;
        }

        // 必须
        public Builder ClassService(Class classService) {
            this.mClassService = classService;
            return this;
        }

        // 可选。如果需要取消，则为必须
        public Builder Tag(Object tag, String name) {
            this.mTag = tag;
            this.mRequestName = name;
            return this;
        }

        //可选。切换域名
        public Builder url(String baseUrl) {
            mBaseUrl = baseUrl;
            return this;
        }

        public Builder maxRetry(int maxRetry) {
            mMaxRetry = maxRetry;
            return this;
        }

        private static Retrofit getService(String baseUrl) {
            Retrofit retrofit = sDomainMap.get(baseUrl);
            if (retrofit == null) {
                synchronized (sDomainMap) {
                    retrofit = buildService(baseUrl);
                    sDomainMap.put(baseUrl, retrofit);
                }
            }
            return retrofit;
        }

        private static Retrofit buildService(String baseUrl) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getOkHttpClient())
                    .build();
            return retrofit;
        }

        private static OkHttpClient getOkHttpClient() {
            if (sHttpClient == null) {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.writeTimeout(30, TimeUnit.SECONDS);
                builder.readTimeout(30, TimeUnit.SECONDS);
                if (BuildConfig.IS_DEBUG) {
                    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLogger());
                    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    builder.addInterceptor(httpLoggingInterceptor);
                }
                sHttpClient = builder.build();
            }
            return sHttpClient;
        }

        /**
         * 发起异步请求
         * <p>
         * 如果需要重新发起请求，clone一份出来, 不需要重新配置
         *
         * @param lastCall 重复发起请求的call
         * @return
         */
        public Call call(Call... lastCall) {
            mSync = false;

            Call currentCall = buildCall(lastCall);
            currentCall.enqueue(getCallback());

            return currentCall;
        }

        /**
         * 发起同步请求
         */
        public Call execute(Call... lastCall) {
            mSync = true;

            Call currentCall = buildCall(lastCall);
            try {
                Response<B> response = currentCall.execute();
                handleResponse(currentCall, response);
            } catch (IOException e) {
                handleFailure(currentCall, e);
            }

            return currentCall;
        }

        private Call buildCall(Call[] lastCall) {
            Call currentCall;
            if (lastCall != null && lastCall.length != 0) {
                Call originCall = lastCall[0];
                currentCall = originCall.clone();
            } else {
                checkParameterValid();

                Retrofit service = getService(mBaseUrl);
                I iQuery = service.create(mClassService);

                currentCall = mCommandQuery.commandQuery(iQuery);
            }

            if (mTag != null) {
                putCall(mTag, mRequestName, currentCall);
            }
            return currentCall;
        }

        @NonNull
        private Callback<B> getCallback() {
            return new Callback<B>() {
                @Override
                public void onResponse(Call<B> call, Response<B> response) {
                    handleResponse(call, response);
                }

                @Override
                public void onFailure(Call<B> call, Throwable t) {
                    handleFailure(call, t);
                }
            };
        }

        /**
         * 处理网络结果
         *
         * @param call
         * @param response
         */
        private void handleResponse(Call<B> call, Response<B> response) {
            if (response.code() == 200) {
                // success
                int ret = mResponseCallback.preSuccess(response.body());
                if (ret == PRE_SUCCESS) {
                    mResponseCallback.success(response.body());
                } else if (ret == PRE_FAIL) {
                    // 接口内的系统错误，已经处理，通知具体业务处理过啦
                    mResponseCallback.failure(5000,
                            "", response.body().getMsg(),
                            null);
                }
            } else {
                if (!retryWhenFailure(call)) {
                    mResponseCallback.failure(response.code(), getDescription(),
                            message(response.message()), null);
                    ToastUtils.showToast(R.string.error_network);
                }
            }

            if (mTag != null) {
                removeCall(mTag, mRequestName);
            }
        }

        /**
         * 处理网络错误或者业务接口错误
         *
         * @param call
         * @param t
         */
        private void handleFailure(Call<B> call, Throwable t) {
            if (!retryWhenFailure(call)) {
                mResponseCallback.failure(5000, getDescription(),
                        message(t.getMessage()), t);
            }

            if (mTag != null) {
                removeCall(mTag, mRequestName);
            }
        }

        /**
         * 进行请求重试
         *
         * @return 是否进行重试
         */
        private boolean retryWhenFailure(Call call) {
            boolean retry = false;
            if (mRetryNum < mMaxRetry) {
                if (mTag != null) {
                    removeCall(mTag, mRequestName);
                }
                retry = true;
                mRetryNum++;

                String retryTip = "Url[%s] request failure and retry again";
                retryTip = String.format(retryTip, call.request().url().toString());
                LogUtils.e(TAG, retryTip);

                if (mSync) {
                    execute(call);
                } else {
                    call(call);
                }
            }
            return retry;
        }

        public static String message(String mes) {
            if (checkStringNull(mes)) {
                return "似乎已断开与互联网连接";
            }

            if (mes.equals("timeout") || mes.equals("SSL handshake timed out")) {
                return "网络请求超时";
            } else {
                return mes;
            }
        }

        private String getDescription() {
            if (mClassService != null) {
                boolean bFlag = mClassService.isAnnotationPresent(Description.class);
                if (bFlag) {
                    return mClassService.getAnnotation(Description.class).value();
                }
            }

            return "";
        }

        private void checkParameterValid() {
            String errMsg = null;
            do {
                if (checkStringNull(mBaseUrl)) {
                    errMsg = "Url can not be null";
                    break;
                }

                if (mCommandQuery == null) {
                    errMsg = "ICommandQuery can not be null";
                    break;
                }

                if (mResponseCallback == null) {
                    errMsg = "Response Callback can not be null";
                    break;
                }

                if (mClassService == null) {
                    errMsg = "Class type can not be null";
                    break;
                }

                if (mTag != null && checkStringNull(mRequestName)) {
                    errMsg = "Tag is not null BUT Request Name is null";
                    break;
                }
            } while (false);

            if (errMsg != null) {
                throw new RuntimeException(errMsg, null);
            }
        }
    }

    /**
     * http日志记录用自定义的Logger
     */
    static class HttpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String message) {
            LogUtils.i(TAG, message);
        }
    }
}

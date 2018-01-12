package com.keepmoving.yuan.passwordgen.util.httputilmodule.httputil;

import android.os.Build;

import com.keepmoving.yuan.passwordgen.util.AppDataUtils;

import java.io.IOException;
import java.util.LinkedList;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by caihanyuan on 2018/1/2.
 * <p>
 * 不需要加解密的拦截器，用于内部测试
 */

public class NoEncryptInterceptor implements Interceptor {

    /**
     * 不加密host白名单
     */
    private static LinkedList<String> sNoEncryptHost;

    static {
        sNoEncryptHost = new LinkedList();
        sNoEncryptHost.push("50.2.39.252");
    }

    public static boolean containHost(String host) {
        return sNoEncryptHost.contains(host);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request request = originalRequest.newBuilder()
                .header("DevicePlatform", "Android")
                .header("DeviceName", Build.PRODUCT)
                .header("DeviceID", AppDataUtils.getDeviceID())
                .header("AppName", "ks_loan")
                .header("Channel", AppDataUtils.getChannelID())
                .header("Version", AppDataUtils.getAppVersion())
                .method(originalRequest.method(), originalRequest.body())
                .build();

        Response response = chain.proceed(request);
        return response;
    }
}

package com.keepmoving.yuan.passwordgen.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by caihanyuan on 2017/11/21.
 */

public class AsyncTaskUtil {
    private static ExecutorService sExecutorService = Executors.newFixedThreadPool(8, Executors.defaultThreadFactory());
}

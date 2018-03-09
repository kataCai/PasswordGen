package com.keepmoving.yuan.passwordgen.model;

import android.content.Context;

import com.keepmoving.yuan.passwordgen.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据库导入导出工具
 */
public class DbFileAcceessHelper {
    private static final String TAG = DbFileAcceessHelper.class.getSimpleName();

    private static DbFileAcceessHelper sInstance;
    private List<FileAccessListener> mAccessListeners;

    private ExecutorService mExecutorService;
    private CountDownLatch mCountDownLatch;

    private List<Runnable> mExportRunnables;

    public static DbFileAcceessHelper getInstance() {
        if (sInstance == null) {
            synchronized (DbFileAcceessHelper.class) {
                if (sInstance == null) {
                    sInstance = new DbFileAcceessHelper();
                }
            }
        }
        return sInstance;
    }

    public DbFileAcceessHelper() {
        mAccessListeners = new LinkedList<>();
        mExecutorService = Executors.newFixedThreadPool(3);
        mExportRunnables = Collections.synchronizedList(new LinkedList<Runnable>());
    }

    public void addAccessListener(FileAccessListener fileAccessListener) {
        mAccessListeners.add(fileAccessListener);
    }

    public void removeAccessListener(FileAccessListener fileAccessListener) {
        mAccessListeners.remove(fileAccessListener);
    }

    public void exportDb(Context context, final String dbName, final String outputDir) {
        LogUtils.d(TAG, "exportDb");
        final File inputFile = context.getDatabasePath(dbName);
        if (inputFile == null || !inputFile.exists()) {
            notifyExportFinish(false, "db:" + dbName + " not exits");
            return;
        } else {
            final String inputFileName = inputFile.getName();
            LogUtils.d(TAG, "db file path:" + inputFile.getAbsolutePath());

            if(mCountDownLatch == null){
                mCountDownLatch = new CountDownLatch(4);
            }
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    File dbOutputFile = new File(outputDir, inputFileName);
                    String dbInputFilePath = inputFile.getAbsolutePath();
                    String dbOutputFilePath = dbOutputFile.getAbsolutePath();

                    // 1. 将db导出
                    doExportDb(dbInputFilePath, dbOutputFilePath);

                    try {
                        mCountDownLatch.await();
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                        notifyExportFinish(false, "CountDownLatch error");
                        return;
                    }

                    if (mExportRunnables.isEmpty()) {
                        notifyExportFinish(true, null);
                    } else {
                        notifyExportFinish(false, "some export part error, see detail log");
                    }
                }
            });
        }
    }

    public void importDb() {

    }

    /**
     * 将DB文件导出到指定目录
     *
     * @param dbInputPath
     * @param dbOutputPath
     */
    private void doExportDb(final String dbInputPath, final String dbOutputPath) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mExportRunnables.add(this);

                BufferedInputStream bufferedInputStream = null;
                BufferedOutputStream bufferedOutputStream = null;
                try {
                    FileInputStream fileInputStream = new FileInputStream(dbInputPath);
                    bufferedInputStream = new BufferedInputStream(fileInputStream);
                    FileOutputStream fileOutputStream = new FileOutputStream(dbOutputPath);
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                    byte[] data = new byte[2048];
                    while (bufferedInputStream.read(data) != -1) {
                        bufferedOutputStream.write(data);
                    }

                    mExportRunnables.remove(this);
                    mCountDownLatch.countDown();
                } catch (java.io.IOException e) {
                    LogUtils.e(e);
                    mCountDownLatch.countDown();
                } finally {
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e) {
                            LogUtils.e(e);
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            LogUtils.e(e);
                        }
                    }
                }
            }
        });
    }

    /**
     * 将数据库数据用json导出到指定目录
     */
    private void doExportDbToJson() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                DataCenter.getInstance().getWholeSupportList();
            }
        });

    }

    private void notifyExportFinish(boolean success, String errorMsg) {
        for (FileAccessListener fileAccessListener : mAccessListeners) {
            fileAccessListener.onExportFinish(success, errorMsg);
        }
    }

    private void notifyImportFinish(boolean success, String errorMsg) {
        for (FileAccessListener fileAccessListener : mAccessListeners) {
            fileAccessListener.onImportFinish(success, errorMsg);
        }
    }

    public interface FileAccessListener {
        void onExportFinish(boolean success, String errorMsg);

        void onImportFinish(boolean success, String errorMsg);
    }
}

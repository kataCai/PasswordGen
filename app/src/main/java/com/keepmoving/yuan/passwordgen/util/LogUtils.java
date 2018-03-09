package com.keepmoving.yuan.passwordgen.util;

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.keepmoving.yuan.passwordgen.BuildConfig;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by caihanyuan on 2017/12/14.
 * <p>
 * 日志记录工具
 */
public class LogUtils {
    private static final boolean IS_DEBUG = BuildConfig.IS_DEBUG;
    public static final String TAG = "yuan_passwordgen";

    static {
        Logger.addLogAdapter(new LogcatAdapter());
        Logger.addLogAdapter(new DiskLogAdapter());
    }

    public static void v(String message, Object... params) {
        Logger.t(TAG);
        Logger.v(message, params);
    }

    public static void v(String tag, String message, Object... params) {
        Logger.t(tag);
        Logger.v(message, params);
    }

    public static void d(Object object) {
        Logger.t(TAG);
        Logger.d(object);
    }

    public static void d(String message, Object... params) {
        Logger.t(TAG);
        Logger.d(message, params);
    }

    public static void d(String tag, String message, Object... params) {
        Logger.t(tag);
        Logger.d(message, params);
    }

    public static void i(String message, Object... params) {
        Logger.t(TAG);
        Logger.i(message, params);
    }

    public static void i(String tag, String message, Object... params) {
        Logger.t(tag);
        Logger.i(message, params);
    }


    public static void w(String message, Object... params) {
        Logger.t(TAG);
        Logger.w(message, params);
    }

    public static void w(String tag, String message, Object... params) {
        Logger.t(tag);
        Logger.w(tag, message, params);
    }

    public static void e(String message, Object... params) {
        Logger.t(TAG);
        Logger.e(message, params);
    }

    public static void e(String tag, String message, Object... params) {
        Logger.t(tag);
        Logger.e(message, params);
    }

    public static void e(Throwable ex) {
        Logger.t(TAG);
        Logger.e(ex, null);
    }

    public static void e(String tag, Throwable ex) {
        Logger.t(tag);
        Logger.e(ex, null);
    }

    public static void e(String tag, Throwable ex, String message, Object... params) {
        Logger.t(tag);
        Logger.e(ex, message, params);
    }

    public static void wtf(String message, Object... params) {
        Logger.t(TAG);
        Logger.wtf(message, params);
    }

    public static void wtf(String tag, String message, Object... params) {
        Logger.t(tag);
        Logger.wtf(message, params);
    }

    public static void json(String json) {
        Logger.t(TAG);
        Logger.json(json);
    }

    public static void json(String tag, String json) {
        Logger.t(tag);
        Logger.json(json);
    }

    public static void xml(String xml) {
        Logger.t(TAG);
        Logger.xml(xml);
    }

    public static void xml(String tag, String xml) {
        Logger.t(tag);
        Logger.xml(xml);
    }

    /**
     * Logcat控制台日志适配器
     */
    private static class LogcatAdapter implements LogAdapter {
        FormatStrategy mFormatStrategy;

        public LogcatAdapter() {
            mFormatStrategy = PrettyFormatStrategy.newBuilder()
                    .methodCount(0) //方法调用数0
                    .showThreadInfo(false) //不显示当前线程
                    .logStrategy(new LogcatLogStrategy()) //记录在Logcat控制台
                    .tag(TAG)
                    .build();
        }

        @Override
        public boolean isLoggable(int priority, String tag) {
            return LogUtils.isLoggable(priority);
        }

        @Override
        public void log(int priority, String tag, String message) {
            mFormatStrategy.log(priority, tag, message);
        }
    }

    /**
     * 硬盘日志适配器
     */
    private static class DiskLogAdapter implements LogAdapter {
        static final String LOG_FOLDER_NAME = TAG; //硬盘Log目录
        static final int LOG_DIR_MAX_SIZE = 350 * 1024 * 1024; //LOG目录最大限制350M
        static final int SINGLE_FILE_MAX_SIZE = 500 * 1024; //单个日志文件最大限制500k

        FormatStrategy mFormatStrategy;

        public DiskLogAdapter() {
            String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String folderName = diskPath + File.separatorChar + LOG_FOLDER_NAME;

            HandlerThread diskLogThread = new HandlerThread("KS_LOAN_LOGTHREAD");
            diskLogThread.start();
            final WriterHandler writerHandler = new WriterHandler(folderName,
                    diskLogThread.getLooper(), LOG_DIR_MAX_SIZE, SINGLE_FILE_MAX_SIZE);


            mFormatStrategy = CsvFormatStrategy.newBuilder()
                    .tag(TAG)
                    .logStrategy(new DiskLogStrategy(writerHandler) {
                        @Override
                        public void log(int level, String tag, String message) {
                            Message message1 = writerHandler.obtainMessage(WriterHandler.WRITE_LOG, message);
                            writerHandler.sendMessage(message1);
                        }
                    })
                    .build();
        }

        @Override
        public boolean isLoggable(int priority, String tag) {
            return LogUtils.isLoggable(priority);
        }

        @Override
        public void log(int priority, String tag, String message) {
            mFormatStrategy.log(priority, tag, message);
        }
    }

    /**
     * 日志输出级别配置
     *
     * @param priority 日志级别
     * @return
     */
    private static boolean isLoggable(int priority) {
        if (BuildConfig.DEBUG) { //如果是debug版本，则全部日志输入（在开发阶段）
            return true;
        } else if (IS_DEBUG) { //如果是release版本，手动开启了日志输出, 则输出全部日志 (测试阶段)
            return true;
        } else { //发布版本，warn级别以上输出
            return priority >= Log.WARN;
        }
    }

    /**
     * 硬盘日志操作Handler
     */
    private static class WriterHandler extends Handler {
        static final int CHECK_MAX_INIT = 1;
        static final int WRITE_LOG = 2;

        private String mLogFolder; //存放的目录名
        private int mFolderMaxSize; //目录上限大小
        private int mSingleFileMaxSize; //单个文件上限大小

        private FileWriter mWriter;
        private File mCurrentFile;

        public WriterHandler(String logFolder, Looper looper, int maxFolderSize, int maxFileSize) {
            super(looper);
            mLogFolder = logFolder;
            mFolderMaxSize = maxFolderSize;
            mSingleFileMaxSize = maxFileSize;

            sendEmptyMessage(CHECK_MAX_INIT);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WRITE_LOG:
                    String content = (String) msg.obj;
                    getLogFile();
                    writeLog(content);
                    break;
                case CHECK_MAX_INIT:
                    deleteIfOutMax();
                    break;
            }
        }

        /**
         * 如果目录大小超出上限，删掉最早的日志文件
         */
        private void deleteIfOutMax() {
            File rootDir = new File(mLogFolder);
            try {
                if (!rootDir.exists()) {
                    rootDir.mkdir();
                } else {
                    long rootSize = getFileSize(rootDir);
                    if (rootSize >= mFolderMaxSize) {
                        File[] childFiles = rootDir.listFiles();
                        List<File> fileList = new ArrayList<>(Arrays.asList(childFiles));
                        Collections.sort(fileList, new Comparator<File>() {
                            @Override
                            public int compare(File file1, File file2) {
                                return file2.compareTo(file1);
                            }
                        });
                        int index = 0;
                        while (rootSize >= mFolderMaxSize) {
                            File file = fileList.get(index);
                            rootSize -= file.length();
                            file.delete();
                            index++;
                        }
                    }
                }
            } catch (SecurityException ex) {
                e(ex);
            }
        }

        /**
         * 获取文件或者目录大小
         *
         * @param file
         * @return
         */
        private long getFileSize(File file) {
            if (file == null) {
                return 0;
            } else if (file.isFile()) {
                return file.length();
            } else {
                File[] children = file.listFiles();
                if (children == null || children.length == 0) {
                    return 0;
                }
                long size = 0;
                for (File f : children)
                    size += getFileSize(f);
                return size;
            }
        }

        /**
         * 写入磁盘日志
         *
         * @param content
         */
        private void writeLog(String content) {
            try {
                if (mWriter != null) {
                    mWriter.append(content);
                    mWriter.flush();
                }
            } catch (IOException e) {
                e(e);
                closeFileWhenIOException();
            }
        }

        /**
         * 获取当前要写入的日志文件
         * <p>
         * 查看是否存在当天的日志文件，如果存在，判断这个文件是否超出单个文件限制大小，如果超出，则新建一个日志文件
         *
         * @return
         */
        private void getLogFile() {
            try {
                File rootFolder = new File(mLogFolder);
                if (!rootFolder.exists()) {
                    rootFolder.mkdirs();
                }
                if (mCurrentFile == null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    final String datePrefix = sdf.format(new Date());

                    //找到当天日志文件，可能存在多个，找到未超出大小的那个
                    File[] childFiles = rootFolder.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.getName().startsWith(datePrefix);
                        }
                    });
                    if (childFiles != null && childFiles.length > 0) {
                        List<File> fileList = new ArrayList<>(Arrays.asList(childFiles));
                        Collections.sort(fileList, new Comparator<File>() {
                            @Override
                            public int compare(File file1, File file2) {
                                return (int) (file2.length() - file1.length());
                            }
                        });
                        //如果最小的文件都超出了单个最大上限大小，则新建一个文件
                        File smallestFile = fileList.get(fileList.size() - 1);
                        if (smallestFile.length() >= mSingleFileMaxSize) {
                            createNewFile(rootFolder);
                        } else {
                            mCurrentFile = smallestFile;
                            mWriter = new FileWriter(mCurrentFile, true);

                            //重新进入后，可能应用版本号变更，重新写入设备信息
                            String deviceInfo = "Brand:%s Manufacturer:%s Type:%s Version:%s Sdk:%s " +
                                    "DeviceName:%s DeviceModel:%s AppChannel:%s AppVersion:%s \n";
                            deviceInfo = String.format(deviceInfo, Build.BRAND, Build.MANUFACTURER, Build.TYPE,
                                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.PRODUCT, Build.MODEL,
                                    AppDataUtils.getChannelID(), AppDataUtils.getAppVersion());
                            mWriter.append(deviceInfo);
                            mWriter.flush();
                        }
                    } else {
                        createNewFile(rootFolder);
                    }

                } else if (mCurrentFile.length() >= mSingleFileMaxSize) {
                    //当前记录文件超过大小，则新建文件
                    createNewFile(rootFolder);
                }
            } catch (SecurityException ex) {
                e(ex);
            } catch (IOException ex) {
                e(ex);
                closeFileWhenIOException();
            }
        }

        /**
         * 创建新日志文件
         *
         * @param rootDir
         */
        private void createNewFile(File rootDir) {
            try {
                if (mWriter != null) {
                    mWriter.flush();
                    mWriter.close();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                final String fileName = sdf.format(new Date());

                File file = new File(rootDir, String.format("%s.csv", fileName));
                file.createNewFile();
                mCurrentFile = file;
                mWriter = new FileWriter(mCurrentFile, true);

                //将设备信息写入文件第一行
                String deviceInfo = "Brand:%s Manufacturer:%s Type:%s Version:%s Sdk:%s " +
                        "DeviceName:%s DeviceModel:%s AppChannel:%s AppVersion:%s \n";
                deviceInfo = String.format(deviceInfo, Build.BRAND, Build.MANUFACTURER, Build.TYPE,
                        Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.PRODUCT, Build.MODEL,
                        AppDataUtils.getChannelID(), AppDataUtils.getAppVersion());
                mWriter.append(deviceInfo);
                mWriter.flush();
            } catch (IOException e) {
                e(e);
                closeFileWhenIOException();
            }
        }

        /**
         * IO错误时确保关闭文件,不能确保一定能关闭
         */
        private void closeFileWhenIOException() {
            try {
                mCurrentFile = null;
                if (mWriter != null) {
                    mWriter.flush();
                    mWriter.close();
                }
                mWriter = null;
            } catch (IOException e) {
                e(e);
            } finally {
                //再次确保文件关闭
                if (mWriter != null) {
                    try {
                        mWriter.flush();
                        mWriter.close();
                    } catch (IOException e1) {
                        e(e1);
                    }
                }
            }
        }
    }
}
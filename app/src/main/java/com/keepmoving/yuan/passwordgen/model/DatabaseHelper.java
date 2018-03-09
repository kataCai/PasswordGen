package com.keepmoving.yuan.passwordgen.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.BaseColumns;
import android.support.v4.util.ArraySet;
import android.text.TextUtils;

import com.keepmoving.yuan.passwordgen.MainApplication;
import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;
import com.keepmoving.yuan.passwordgen.model.bean.SupportBean;
import com.keepmoving.yuan.passwordgen.model.bean.UserBean;
import com.keepmoving.yuan.passwordgen.model.iaccess.IKeyAccess;
import com.keepmoving.yuan.passwordgen.model.iaccess.IUserAccess;
import com.keepmoving.yuan.passwordgen.util.AppDataUtils;
import com.keepmoving.yuan.passwordgen.util.LogUtils;
import com.keepmoving.yuan.passwordgen.util.TokenProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by caihanyuan on 2017/11/19.
 */

public class DatabaseHelper extends SQLiteOpenHelper implements IKeyAccess, IUserAccess {

    private final static String DATABASE_NAME = "passwords.db";
    static final String SUPPORT_TABLE_NAME = "supports";
    static final String KEY_TABLE_NAME = "keys";
    static final String USER_TABLE_NAME = "users";

    static final String COLUMN_ID = "objectId";

    private static DatabaseHelper sInstance;

    private SQLiteDatabase mReadDatabase;
    private SQLiteDatabase mWriteDatabase;

    private SupportAddListener mSupportAddListener;

    private static final String CREATE_TABLE_SUPPORT = "create table " + SUPPORT_TABLE_NAME + " ("
            + COLUMN_ID + " text, "
            + SupportColumns.NAME + " text)";

    private static final String CREATE_TABLE_KEYS = "create table " + KEY_TABLE_NAME + " ("
            + COLUMN_ID + " text, "
            + KeyColumns.SUPPORT + " text, "
            + KeyColumns.ACCOUNT_NAME + " text not null, "
            + KeyColumns.USER_NAME + " text, "
            + KeyColumns.VERSION + " integer default 1,"
            + KeyColumns.LENGTH + " integer default 6)";

    private static final String CREATE_TABLE_USERS = "create table " + USER_TABLE_NAME + " ("
            + COLUMN_ID + " text, "
            + UsersColumns.TOKEN + " text, "
            + UsersColumns.NAME + " text, "
            + UsersColumns.COMPANY + " text, "
            + UsersColumns.CHECK + " integer default 0)";

    private static final String DROP_TABLE_SUPPORT = "DROP TABLE IF EXISTS " + SUPPORT_TABLE_NAME;

    private static final String DROP_TABLE_KEYS = "DROP TABLE IF EXISTS " + KEY_TABLE_NAME;

    private interface KeyColumns extends BaseColumns {
        String SUPPORT = "_support";
        String ACCOUNT_NAME = "_account_name";
        String USER_NAME = "_username";
        String VERSION = "_version";
        String LENGTH = "_len";
    }

    private interface SupportColumns extends BaseColumns {
        String NAME = "_name";
    }

    private interface UsersColumns extends BaseColumns {
        String TOKEN = "_token";
        String NAME = "_name";
        String COMPANY = "_company";
        String CHECK = "_check";
    }

    static DatabaseHelper getInstance(int dbVersion) {
        if (sInstance == null) {
            synchronized (DatabaseHelper.class) {
                if (sInstance == null) {
                    sInstance = new DatabaseHelper(MainApplication.getContext(), DATABASE_NAME, dbVersion);
                }
            }
        }
        return sInstance;
    }

    private DatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
        getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_KEYS);
        db.execSQL(CREATE_TABLE_USERS);
        initUserData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void setSupportAddListener(SupportAddListener supportAddListener) {
        this.mSupportAddListener = supportAddListener;
    }

    public List<String> getMatchSupportList(String support) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(SUPPORT_TABLE_NAME, new String[]{SupportColumns.NAME},
                SupportColumns.NAME + " like ?", new String[]{"%" + support + "%"},
                null, null, SupportColumns.NAME + " asc", null);
        Set<String> supportList = new ArraySet<>();
        while (cursor.moveToNext()) {
            supportList.add(cursor.getString(0));
        }
        return new ArrayList<>(supportList);
    }

    public List<String> getUserNameList(String username) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(KEY_TABLE_NAME, new String[]{KeyColumns.USER_NAME},
                KeyColumns.USER_NAME + " like ?", new String[]{"%" + username + "%"},
                null, null, KeyColumns.USER_NAME + " asc", null);
        Set<String> usernameSet = new ArraySet<>();
        while (cursor.moveToNext()) {
            usernameSet.add(cursor.getString(0));
        }
        return new ArrayList<>(usernameSet);
    }

    public KeyBean getMatchKey(String support) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(KEY_TABLE_NAME, null,
                KeyColumns.SUPPORT + " = ?", new String[]{support},
                null, null, KeyColumns.USER_NAME + " asc", null);
        if (cursor.moveToFirst()) {
            KeyBean keyBean = new KeyBean();
            keyBean.setObjectId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            keyBean.setSupport(support);
            keyBean.setUsername(cursor.getString(cursor.getColumnIndex(KeyColumns.USER_NAME)));
            keyBean.setVersion(cursor.getInt(cursor.getColumnIndex(KeyColumns.VERSION)));
            keyBean.setPasswordLen(cursor.getInt(cursor.getColumnIndex(KeyColumns.LENGTH)));
            keyBean.setAccountName(cursor.getString(cursor.getColumnIndex(KeyColumns.ACCOUNT_NAME)));
            return keyBean;
        }
        return null;
    }

    public KeyBean getMatchKey(String support, String username) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(KEY_TABLE_NAME, null,
                KeyColumns.SUPPORT + " = ? and " + KeyColumns.USER_NAME + " = ?",
                new String[]{support, username}, null, null, KeyColumns.USER_NAME + " asc", null);
        if (cursor.moveToFirst()) {
            KeyBean keyBean = new KeyBean();
            keyBean.setObjectId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            keyBean.setSupport(support);
            keyBean.setUsername(cursor.getString(cursor.getColumnIndex(KeyColumns.USER_NAME)));
            keyBean.setVersion(cursor.getInt(cursor.getColumnIndex(KeyColumns.VERSION)));
            keyBean.setPasswordLen(cursor.getInt(cursor.getColumnIndex(KeyColumns.LENGTH)));
            keyBean.setAccountName(cursor.getString(cursor.getColumnIndex(KeyColumns.ACCOUNT_NAME)));
            return keyBean;
        }
        return null;
    }

    public boolean hasMatchKey(KeyBean keyBean) {
        initReadDatabase();
        return getMatchKey(keyBean.getSupport(), keyBean.getUsername()) != null;
    }

    boolean hasMatchSupport(String supportName) {
        initReadDatabase();

        Cursor cursor = mReadDatabase.query(SUPPORT_TABLE_NAME, new String[]{SupportColumns.NAME},
                SupportColumns.NAME + " = ?", new String[]{supportName},
                null, null, null, null);
        return cursor.moveToFirst();
    }

    public void createOrUpdateKey(KeyBean keyBean) {
        initWriteDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, keyBean.getObjectId());
        contentValues.put(KeyColumns.ACCOUNT_NAME, keyBean.getAccountName());
        contentValues.put(KeyColumns.SUPPORT, keyBean.getSupport());
        contentValues.put(KeyColumns.USER_NAME, keyBean.getUsername());
        if (keyBean.getVersion() != 0) {
            contentValues.put(KeyColumns.VERSION, keyBean.getVersion());
        }
        if (keyBean.getPasswordLen() != 0) {
            contentValues.put(KeyColumns.LENGTH, keyBean.getPasswordLen());
        }
        if (hasMatchKey(keyBean)) {
            mWriteDatabase.update(KEY_TABLE_NAME, contentValues,
                    KeyColumns.SUPPORT + " = ? and " + KeyColumns.USER_NAME + " = ?",
                    new String[]{keyBean.getSupport(), keyBean.getUsername()});
            LogUtils.d(LogUtils.TAG, "key update in database success");

        } else {
            mWriteDatabase.insert(KEY_TABLE_NAME, null, contentValues);
            LogUtils.d(LogUtils.TAG, "key save in database success");
        }

        String supportName = keyBean.getSupport();
        if (!hasMatchSupport(supportName)) {
            if (mSupportAddListener != null) {
                mSupportAddListener.needAddSupport(supportName);
            }
        }
    }

    /**
     * 添加一个供应商
     *
     * @param supportBean
     */
    public void addSupport(SupportBean supportBean) {
        ContentValues supportValue = new ContentValues();
        supportValue.put(COLUMN_ID, supportBean.getObjectId());
        supportValue.put(SupportColumns.NAME, supportBean.getName());
        mWriteDatabase.insert(SUPPORT_TABLE_NAME, null, supportValue);
    }

    @Override
    public boolean isLogin() {
        initReadDatabase();

        Cursor cursor = mReadDatabase.query(USER_TABLE_NAME, new String[]{UsersColumns.NAME},
                UsersColumns.CHECK + " = ?", new String[]{"1"},
                null, null, null, null);

        return cursor.moveToFirst();
    }

    @Override
    public void login(UserBean userBean) {
        if (isLogin()) {
            logOut();
        }
        login(userBean);

        SharePreferenceData.login(userBean.getName());
    }

    @Override
    public void logOut() {
        initWriteDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(UsersColumns.CHECK, 0);

        mWriteDatabase.update(USER_TABLE_NAME, contentValues,
                UsersColumns.NAME + " = ?",
                new String[]{SharePreferenceData.getLoginName()});

        SharePreferenceData.logout();
    }

    @Override
    public UserBean getLoginUser() {
        initReadDatabase();

        UserBean userBean = null;

        Cursor cursor = mReadDatabase.query(USER_TABLE_NAME, null,
                UsersColumns.CHECK + " = ?", new String[]{"1"},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            userBean = new UserBean();
            userBean.setObjectId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            userBean.setName(cursor.getString(cursor.getColumnIndex(UsersColumns.NAME)));
            userBean.setCompany(cursor.getString(cursor.getColumnIndex(UsersColumns.COMPANY)));
            userBean.setCheck(cursor.getInt(cursor.getColumnIndex(UsersColumns.CHECK)));
        }
        return userBean;
    }

    private void initReadDatabase() {
        if (mReadDatabase == null) {
            mReadDatabase = getReadableDatabase();
        }
    }

    private void initWriteDatabase() {
        if (mWriteDatabase == null) {
            mWriteDatabase = getWritableDatabase();
        }
    }

    private void initUserData(SQLiteDatabase database) {
        String userName = AppDataUtils.getPhoneNumber();
        String company = AppDataUtils.getNetworkOperatorName();
        if (TextUtils.isEmpty(userName)) {
            userName = AppDataUtils.getSerialNumber();
        }
        if (TextUtils.isEmpty(company)) {
            company = Build.BRAND;
        }
        String token = TokenProcessor.generateToken(userName, false);

        ContentValues contentValues = new ContentValues();
        contentValues.put(UsersColumns.TOKEN, token);
        contentValues.put(UsersColumns.NAME, userName);
        contentValues.put(UsersColumns.COMPANY, company);
        contentValues.put(UsersColumns.CHECK, 1);
        database.insert(USER_TABLE_NAME, null, contentValues);

        SharePreferenceData.login(userName);
    }

    /**
     * 更新供应商列表
     *
     * @param supportBeanList
     */
    public void syncSupportList(List<SupportBean> supportBeanList) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL(DROP_TABLE_SUPPORT);
        sqLiteDatabase.execSQL(CREATE_TABLE_SUPPORT);
        new SyncSupportTask(supportBeanList).executeOnExecutor
                (AsyncTask.THREAD_POOL_EXECUTOR, sqLiteDatabase);
    }

    /**
     * 更新密码键值表
     *
     * @param keyBeanList
     */
    public void syncKeyList(List<KeyBean> keyBeanList) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL(DROP_TABLE_KEYS);
        sqLiteDatabase.execSQL(CREATE_TABLE_KEYS);
        new SyncKeyListTask(keyBeanList).executeOnExecutor
                (AsyncTask.THREAD_POOL_EXECUTOR, sqLiteDatabase);
    }


    private static class SyncSupportTask extends AsyncTask<SQLiteDatabase, Integer, Boolean> {

        List<SupportBean> mSupportList;

        public SyncSupportTask(List<SupportBean> supportList) {
            this.mSupportList = supportList;
        }

        @Override
        protected Boolean doInBackground(SQLiteDatabase... params) {
            SQLiteDatabase database = params[0];
            try {
                database.beginTransaction();

                for (SupportBean supportBean : mSupportList) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLUMN_ID, supportBean.getObjectId());
                    contentValues.put(SupportColumns.NAME, supportBean.getName());
                    database.insert(SUPPORT_TABLE_NAME, null, contentValues);
                }

                database.setTransactionSuccessful();
            } catch (Exception e) {
                LogUtils.e(e);
                return false;
            } finally {
                database.endTransaction();
            }
            return true;
        }
    }

    private static class SyncKeyListTask extends AsyncTask<SQLiteDatabase, Integer, Boolean> {

        List<KeyBean> mKeyBeanList;

        public SyncKeyListTask(List<KeyBean> keyBeanList) {
            this.mKeyBeanList = keyBeanList;
        }

        @Override
        protected Boolean doInBackground(SQLiteDatabase... params) {
            SQLiteDatabase database = params[0];
            try {
                database.beginTransaction();

                for (KeyBean keyBean : mKeyBeanList) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLUMN_ID, keyBean.getObjectId());
                    contentValues.put(KeyColumns.SUPPORT, keyBean.getSupport());
                    contentValues.put(KeyColumns.ACCOUNT_NAME, keyBean.getAccountName());
                    contentValues.put(KeyColumns.USER_NAME, keyBean.getUsername());
                    contentValues.put(KeyColumns.VERSION, keyBean.getVersion());
                    contentValues.put(KeyColumns.LENGTH, keyBean.getPasswordLen());
                    database.insert(KEY_TABLE_NAME, null, contentValues);
                }

                database.setTransactionSuccessful();
            } catch (Exception e) {
                LogUtils.e(e);
                return false;
            } finally {
                database.endTransaction();
            }
            return true;
        }
    }


    interface SupportAddListener {
        /**
         * 需要添加供应商名
         */
        void needAddSupport(String support);
    }
}

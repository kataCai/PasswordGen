package com.keepmoving.yuan.passwordgen.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import com.keepmoving.yuan.passwordgen.MainApplication;
import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caihanyuan on 2017/11/19.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String SUPPORT_TABLE_NAME = "supports";
    static final String KEY_TABLE_NAME = "keys";

    private SQLiteDatabase mReadDatabase;
    private SQLiteDatabase mWriteDatabase;

    private static final String CREATE_TABLE_SUPPORT = "create table " + SUPPORT_TABLE_NAME + " ("
            + SupportColumns._ID + " integer primary key autoincrement, "
            + SupportColumns.NAME + " text)";

    private static final String CREATE_TABLE_KEYS = "create table " + KEY_TABLE_NAME + " ("
            + KeyColumns._ID + " integer primary key autoincrement, "
            + KeyColumns.SUPPORT + " text, "
            + KeyColumns.USER_NAME + " text, "
            + KeyColumns.VERSION + " integer default 1,"
            + KeyColumns.LENGTH + " integer default 6)";

    private interface KeyColumns extends BaseColumns {
        String SUPPORT = "_support";
        String USER_NAME = "_username";
        String VERSION = "_version";
        String LENGTH = "_len";
    }

    private interface SupportColumns extends BaseColumns {
        String NAME = "_name";
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        getReadableDatabase();
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SUPPORT);
        db.execSQL(CREATE_TABLE_KEYS);
        initSupportData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    List<String> getSupportList(String support) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(SUPPORT_TABLE_NAME, new String[]{SupportColumns.NAME},
                SupportColumns.NAME + " like ?", new String[]{"%" + support + "%"},
                null, null, SupportColumns.NAME + " asc", null);
        List<String> supportList = new ArrayList<>();
        while (cursor.moveToNext()) {
            supportList.add(cursor.getString(0));
        }
        return supportList;
    }

    List<String> getUserNameList(String username) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(KEY_TABLE_NAME, new String[]{KeyColumns.USER_NAME},
                KeyColumns.USER_NAME + " like ?", new String[]{"%" + username + "%"},
                null, null, KeyColumns.USER_NAME + " asc", null);
        List<String> usernameList = new ArrayList<>();
        while (cursor.moveToNext()) {
            usernameList.add(cursor.getString(0));
        }
        return usernameList;
    }

    KeyBean getMatchKey(String support) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(KEY_TABLE_NAME, null,
                KeyColumns.SUPPORT + " = ?", new String[]{support},
                null, null, KeyColumns.USER_NAME + " asc", null);
        if (cursor.moveToFirst()) {
            KeyBean keyBean = new KeyBean();
            keyBean.setSupport(support);
            keyBean.setUsername(cursor.getString(cursor.getColumnIndex(KeyColumns.USER_NAME)));
            keyBean.setVersion(cursor.getInt(cursor.getColumnIndex(KeyColumns.VERSION)));
            keyBean.setPasswordLen(cursor.getInt(cursor.getColumnIndex(KeyColumns.LENGTH)));
            return keyBean;
        }
        return null;
    }

    KeyBean getMatchKey(String support, String username) {
        initReadDatabase();
        Cursor cursor = mReadDatabase.query(KEY_TABLE_NAME, null,
                KeyColumns.SUPPORT + " = ? and " + KeyColumns.USER_NAME + " = ?",
                new String[]{support, username}, null, null, KeyColumns.USER_NAME + " asc", null);
        if (cursor.moveToFirst()) {
            KeyBean keyBean = new KeyBean();
            keyBean.setSupport(support);
            keyBean.setUsername(cursor.getString(cursor.getColumnIndex(KeyColumns.USER_NAME)));
            keyBean.setVersion(cursor.getInt(cursor.getColumnIndex(KeyColumns.VERSION)));
            keyBean.setPasswordLen(cursor.getInt(cursor.getColumnIndex(KeyColumns.LENGTH)));
            return keyBean;
        }
        return null;
    }

    boolean hasMathKey(KeyBean keyBean) {
        initReadDatabase();
        return getMatchKey(keyBean.getSupport(), keyBean.getUsername()) != null;
    }

    void createOrUpdateKey(KeyBean keyBean) {
        initWriteDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KeyColumns.SUPPORT, keyBean.getSupport());
        contentValues.put(KeyColumns.USER_NAME, keyBean.getUsername());
        if (keyBean.getVersion() != 0) {
            contentValues.put(KeyColumns.VERSION, keyBean.getVersion());
        }
        if (keyBean.getPasswordLen() != 0) {
            contentValues.put(KeyColumns.LENGTH, keyBean.getPasswordLen());
        }
        if (hasMathKey(keyBean)) {
            mWriteDatabase.update(KEY_TABLE_NAME, contentValues,
                    KeyColumns.SUPPORT + " = ? and " + KeyColumns.USER_NAME + " = ?",
                    new String[]{keyBean.getSupport(), keyBean.getUsername()});

        } else {
            mWriteDatabase.insert(KEY_TABLE_NAME, null, contentValues);
        }
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

    private void initSupportData(SQLiteDatabase sqLiteDatabase) {
        new InitAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sqLiteDatabase);
    }


    private static class InitAsyncTask extends AsyncTask<SQLiteDatabase, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(SQLiteDatabase... params) {
            Context context = MainApplication.getContext();
            AssetManager assetManager = context.getAssets();
            SQLiteDatabase database = params[0];
            try {
                database.beginTransaction();
                InputStream inputStream = assetManager.open("supports");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String supportName = null;
                while ((supportName = bufferedReader.readLine()) != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(SupportColumns.NAME, supportName);
                    database.insert(SUPPORT_TABLE_NAME, null, contentValues);
                }
                bufferedReader.close();
                database.setTransactionSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                database.endTransaction();
            }
            return true;
        }
    }
}

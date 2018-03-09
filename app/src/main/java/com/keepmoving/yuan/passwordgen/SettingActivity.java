package com.keepmoving.yuan.passwordgen;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.keepmoving.yuan.passwordgen.model.DatabaseHelper;
import com.keepmoving.yuan.passwordgen.model.DbFileAcceessHelper;
import com.keepmoving.yuan.passwordgen.util.LogUtils;

import java.io.File;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, DbFileAcceessHelper.FileAccessListener {

    private final static String TAG = SettingActivity.class.getSimpleName();

    private final int REQUEST_CODE_CHOOSE_DIR = 0x01;

    public static String DEFAULT_OUT_DB_DIR = null;

    private TextView mImportText;
    private TextView mExportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mImportText = (TextView) findViewById(R.id.import_text);
        mExportText = (TextView) findViewById(R.id.export_text);

        mImportText.setOnClickListener(this);
        mExportText.setOnClickListener(this);

        File file = new File(Environment.getExternalStorageDirectory(), "PASSWORD_CREATOR");
        if (!file.exists()) {
            file.mkdir();
        }
        DEFAULT_OUT_DB_DIR = file.getAbsolutePath();

        DbFileAcceessHelper.getInstance().addAccessListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DbFileAcceessHelper.getInstance().removeAccessListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mImportText) {

        } else if (v == mExportText) {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            try {
//                startActivityForResult(Intent.createChooser(intent, "Select a File to Export"), REQUEST_CODE_CHOOSE_DIR);
//            } catch (android.content.ActivityNotFoundException ex) {
//                Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
//                DbFileAcceessHelper.getInstance().exportDb(this, DatabaseHelper.DATABASE_NAME, DEFAULT_OUT_DB_DIR);
//            }
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("确定导出数据吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DbFileAcceessHelper.getInstance().exportDb(SettingActivity.this, DatabaseHelper.DATABASE_NAME, DEFAULT_OUT_DB_DIR);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_DIR:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    LogUtils.d(TAG, "File Uri: " + uri.toString());
                } else {
                    DbFileAcceessHelper.getInstance().exportDb(this, DatabaseHelper.DATABASE_NAME, DEFAULT_OUT_DB_DIR);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onExportFinish(boolean success, String errorMsg) {

    }

    @Override
    public void onImportFinish(boolean success, String errorMsg) {

    }
}

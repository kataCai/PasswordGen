package com.keepmoving.yuan.passwordgen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.keepmoving.yuan.passwordgen.model.DataCenter;
import com.keepmoving.yuan.passwordgen.model.SharePreferenceData;
import com.keepmoving.yuan.passwordgen.model.bean.KeyBean;
import com.keepmoving.yuan.passwordgen.util.ToastUtils;
import com.keepmoving.yuan.passwordgen.widget.ClearEditText;
import com.keepmoving.yuan.passwordgen.widget.PwdEditText;
import com.yuan.passwordcore.PasswordCreator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String PREFERENCE_NAME = "com.keepmoving.yuan.passwordgen";
    private static final String PRIVATE_KEY = "private_key";

    private static final String SAVE_KEY_SUPPORT = "support";
    private static final String SAVE_KEY_USERNAME = "username";
    private static final String SAVE_KEY_VERSION = "version";
    private static final String SAVE_KEY_LEN = "len";
    private static final String SAVE_KEY_ID = "key_id";

    private PwdEditText keyText;
    private ClearEditText domainText;
    private ClearEditText usernameText;
    private ClearEditText codeText;
    private ClearEditText lengthText;
    private TextView btnCreate;
    private RadioGroup radioGroup;
    private EditText resultText;

    private ArrayAdapter<String> domainAdapter;
    private ArrayAdapter<String> usernameAdapter;

    private GetKeyRunnable getKeyRunnable;

    private KeyBean currentKeyBean = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData(savedInstanceState);
    }

    private void initView() {
        keyText = (PwdEditText) findViewById(R.id.key_text);
        domainText = (ClearEditText) findViewById(R.id.domain_text);
        usernameText = (ClearEditText) findViewById(R.id.username_text);
        codeText = (ClearEditText) findViewById(R.id.code_text);
        lengthText = (ClearEditText) findViewById(R.id.length_text);
        btnCreate = (TextView) findViewById(R.id.btn_create);
        radioGroup = (RadioGroup) findViewById(R.id.type_group);
        resultText = (EditText) findViewById(R.id.passwrod_result);

        btnCreate.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                String result = resultText.getText().toString();
                if (!TextUtils.isEmpty(result)) {
                    showCreatePassword();
                }
            }
        });

        domainText.setOnItemClickListener(this);
        usernameText.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // TODO: 2018/1/8 跳转设置页面
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_KEY_ID, currentKeyBean);
        outState.putString(SAVE_KEY_SUPPORT, domainText.getText().toString());
        outState.putString(SAVE_KEY_USERNAME, usernameText.getText().toString());
        outState.putString(SAVE_KEY_VERSION, codeText.getText().toString());
        outState.putString(SAVE_KEY_LEN, lengthText.getText().toString());
    }

    @Override
    public void onClick(View v) {
        showCreatePassword();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String domain = domainText.getText().toString();
        final String username = usernameText.getText().toString();

        Handler handler = MainApplication.getDataIOHandler();
        handler.removeCallbacks(getKeyRunnable);

        getKeyRunnable.setDomain(domain);
        getKeyRunnable.setUserName(username);
        handler.post(getKeyRunnable);
    }

    private void initData(Bundle savedInstanceState) {
        domainAdapter = new ObscureArrayAdapter(this, R.layout.text_item,
                R.id.text_view, ObscureArrayAdapter.DATA_TYPE_DOMAIN);
        usernameAdapter = new ObscureArrayAdapter(this, R.layout.text_item,
                R.id.text_view, ObscureArrayAdapter.DATA_TYPE_USERNAME);

        domainText.setAdapter(domainAdapter);
        usernameText.setAdapter(usernameAdapter);

        getKeyRunnable = new GetKeyRunnable();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        String privateKey = sharedPreferences.getString(PRIVATE_KEY, "");
        if (!TextUtils.isEmpty(privateKey)) {
            keyText.setText(privateKey);
        }

        if (savedInstanceState != null) {
            currentKeyBean = (KeyBean) savedInstanceState.getSerializable(SAVE_KEY_ID);
            String support = savedInstanceState.getString(SAVE_KEY_SUPPORT);
            String username = savedInstanceState.getString(SAVE_KEY_USERNAME);
            String version = savedInstanceState.getString(SAVE_KEY_VERSION);
            String len = savedInstanceState.getString(SAVE_KEY_LEN);

            if (!TextUtils.isEmpty(support)) {
                domainText.setText(support);
            }
            if (!TextUtils.isEmpty(username)) {
                usernameText.setText(username);
            }
            if (!TextUtils.isEmpty(version)) {
                codeText.setText(version);
            }
            if (!TextUtils.isEmpty(len)) {
                lengthText.setText(len);
            }
        }
    }

    private void showCreatePassword() {
        String keyData = keyText.getText().toString();
        if (TextUtils.isEmpty(keyData)) {
            ToastUtils.showToast("请输入秘钥");
            return;
        }

        String domain = domainText.getText().toString();
        if (TextUtils.isEmpty(domain)) {
            ToastUtils.showToast("请输入服务商，如网站名，银行名或应用名");
            return;
        }

        String userName = usernameText.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            ToastUtils.showToast("请输入用户名，如email，手机号等");
            return;
        }

        String versionCode = codeText.getText().toString();
        int version = 1;
        if (TextUtils.isEmpty(versionCode) || versionCode.equals("0")) {
            versionCode = "1";
        }
        version = Integer.valueOf(versionCode);

        String length = lengthText.getText().toString();
        int len = 6;
        if (TextUtils.isEmpty(length) || TextUtils.equals(length, "0")) {
            length = "6";
        }
        len = Integer.valueOf(length);

        String password = "";

        int passwordType = radioGroup.getCheckedRadioButtonId();
        if (passwordType == R.id.number_check) {
            password = PasswordCreator.createNumberPassword(keyData, domain, userName, versionCode, len);
        } else {
            password = PasswordCreator.createMixPassword(keyData, domain, userName, versionCode, len);
        }
        resultText.setText(password);

        //保存到数据库
        currentKeyBean = getMatchKey(domain, userName);
        if (currentKeyBean == null) {
            currentKeyBean = new KeyBean();
        }
        currentKeyBean.setSupport(domain);
        currentKeyBean.setUsername(userName);
        currentKeyBean.setVersion(version);
        currentKeyBean.setPasswordLen(len);
        currentKeyBean.setAccountName(SharePreferenceData.getLoginName());

        MainApplication.getDataIOHandler().post(new Runnable() {
            @Override
            public void run() {
                DataCenter.getInstance().createOrUpdateKey(currentKeyBean);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PRIVATE_KEY, keyData);
        editor.apply();
    }

    /**
     * 获取匹配的key
     *
     * @param support
     * @param username
     * @return
     */
    private KeyBean getMatchKey(String support, String username) {
        KeyBean keyBean = null;
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(support)) {
            keyBean = DataCenter.getInstance().getMatchKey(support, username);
        } else if (!TextUtils.isEmpty(support)) {
            keyBean = DataCenter.getInstance().getMatchKey(support);
        } else {
            keyBean = null;
        }
        return keyBean;
    }

    /**
     * 异步处理数据请求
     */
    class GetKeyRunnable implements Runnable {
        String mDomain;
        String mUserName;

        public void setDomain(String mDomain) {
            this.mDomain = mDomain;
        }

        public void setUserName(String mUserName) {
            this.mUserName = mUserName;
        }

        @Override
        public void run() {
            currentKeyBean = getMatchKey(mDomain, mUserName);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentKeyBean != null) {
                        String support = currentKeyBean.getSupport();
                        String userName = currentKeyBean.getUsername();
                        int versionCode = currentKeyBean.getVersion();
                        int len = currentKeyBean.getPasswordLen();

                        if (!TextUtils.isEmpty(support)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                domainText.setText(support, false);
                            } else {
                                domainText.setText(support);
                            }
                        }
                        if (!TextUtils.isEmpty(userName)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                usernameText.setText(userName, false);
                            } else {
                                usernameText.setText(userName);
                            }
                        }
                        if (versionCode != 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                codeText.setText(String.valueOf(versionCode), false);
                            } else {
                                codeText.setText(String.valueOf(versionCode));
                            }
                        }
                        if (len != 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                lengthText.setText(String.valueOf(len), false);
                            } else {
                                lengthText.setText(String.valueOf(len));
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 支持模糊搜索的适配器
     *
     * @param <T>
     */
    static class ObscureArrayAdapter<T extends String> extends ArrayAdapter {

        static int DATA_TYPE_DOMAIN = 1;
        static int DATA_TYPE_USERNAME = 2;

        private Filter mFilter;
        private int mDataType;

        public ObscureArrayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, int mDataType) {
            super(context, resource, textViewResourceId);
            this.mDataType = mDataType;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ObscureFilter<T>();
            }
            return mFilter;
        }

        @Override
        public int getCount() {
            int count = super.getCount();
            return count;
        }

        class ObscureFilter<T extends String> extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();

                if (TextUtils.isEmpty(constraint)) {
                    final ArrayList<T> list = new ArrayList<>(0);
                    results.values = list;
                    results.count = list.size();
                } else {
                    List<String> values = null;
                    if (mDataType == DATA_TYPE_DOMAIN) {
                        values = DataCenter.getInstance().getMatchSupportList((String) constraint);
                    } else if (mDataType == DATA_TYPE_USERNAME) {
                        values = DataCenter.getInstance().getUserNameList((String) constraint);
                    }
                    results.values = values;
                    results.count = values.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                setNotifyOnChange(false);
                clear();
                addAll((List<T>) results.values);

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
                setNotifyOnChange(true);
            }
        }
    }
}



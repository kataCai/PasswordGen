package com.keepmoving.yuan.passwordgen.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.keepmoving.yuan.passwordgen.MainApplication;
import com.keepmoving.yuan.passwordgen.R;

/**
 * Created by wangmiaomiao on 2017/6/30.
 * 居中位置的toast
 */

public class ToastUtils {
    private static ToastEntry sLastToastEntry = null;

    public static void showToast(String message) {
        Toast toast = null;
        if (isToastShowing(message)) {
            toast = sLastToastEntry.getToast();
            toast.show();
        } else {
            toast = newCenterToast(message);
            toast.show();
            sLastToastEntry = new ToastEntry(message, toast);
        }
    }

    /**
     * 相同的提示在屏幕上只显示一个toast
     *
     * @param messageResId
     */
    public static void showToast(int messageResId) {
        String message = MainApplication.getContext().getString(messageResId);
        showToast(message);
    }

    private static Toast newCenterToast(String message) {
        Context context = MainApplication.getContext();

        View toastView = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        TextView mTextView = (TextView) toastView.findViewById(R.id.tv_toast_text);
        mTextView.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(toastView);
        return toast;
    }

    private static boolean isToastShowing(String message) {
        if (sLastToastEntry == null) {
            return false;
        } else {
            return TextUtils.equals(message, sLastToastEntry.mMessage);
        }
    }

    /**
     * Toast和Message对应实体类
     */
    static class ToastEntry {
        String mMessage;
        Toast mToast;

        public ToastEntry(String message, Toast toast) {
            this.mMessage = message;
            this.mToast = toast;
        }

        public Toast getToast() {
            return mToast;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ToastEntry) {
                ToastEntry otherEntry = (ToastEntry) other;
                return TextUtils.equals(mMessage, otherEntry.mMessage);
            }
            return false;
        }
    }
}

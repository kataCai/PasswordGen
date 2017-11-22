package com.keepmoving.yuan.passwordgen.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.keepmoving.yuan.passwordgen.R;

/**
 * Created by wangmiaomiao on 2017/5/31.
 */

@SuppressLint("AppCompatCustomView")
public class ClearEditText extends AutoCompleteTextView implements View.OnFocusChangeListener, TextWatcher {

    private Drawable mClearDrawable;
    private boolean bFocus;
    private OnFocusChangeListener mFocusListener;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOnFocusChangeListener(OnFocusChangeListener focusListener) {
        mFocusListener = focusListener;
    }

    private void init() {
        mFocusListener = null;
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = ResourcesCompat.getDrawable(getResources(), R.mipmap.icon_login_clean, null);
        }
        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        super.setOnFocusChangeListener(this);
        addTextChangedListener(this);
        setDrawableVisible(false);
        setThreshold(1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                int start = getWidth() - getTotalPaddingRight() + getPaddingRight(); // 起始位置
                int end = getWidth(); // 结束位置
                boolean available = (event.getX() > start) && (event.getX() < end);
                if (available) {
                    this.setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.bFocus = hasFocus;
        if (hasFocus && getText().length() > 0) {
            setDrawableVisible(true);
        } else {
            setDrawableVisible(false);
        }

        if (mFocusListener != null) {
            mFocusListener.onFocusChange(v, hasFocus);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (bFocus) {
            setDrawableVisible(s.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    protected void setDrawableVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }
}

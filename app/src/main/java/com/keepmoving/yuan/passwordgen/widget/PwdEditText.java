package com.keepmoving.yuan.passwordgen.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.keepmoving.yuan.passwordgen.R;

@SuppressLint("AppCompatCustomView")
public class PwdEditText extends EditText {

    private Drawable mShowPassWord;
    private Drawable mShowNormal;
    private boolean bShowNormal;
    private IClickEventCallback mClickCallback;

    public PwdEditText(Context context) {
        this(context, null);
    }

    public PwdEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PwdEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setClickEventCallback(IClickEventCallback callback) {
        mClickCallback = callback;
    }

    private void init() {
        setTypeface(Typeface.DEFAULT);
        bShowNormal = false;
        if (mShowPassWord == null) {
            mShowPassWord = ResourcesCompat.getDrawable(getResources(), R.mipmap.icon_hide_pwd, null);
        }
        mShowPassWord.setBounds(0, 0, mShowPassWord.getIntrinsicWidth(), mShowPassWord.getIntrinsicHeight());

        if (mShowNormal == null) {
            mShowNormal = ResourcesCompat.getDrawable(getResources(), R.mipmap.icon_show_pwd, null);
        }
        mShowNormal.setBounds(0, 0, mShowNormal.getIntrinsicWidth(), mShowNormal.getIntrinsicHeight());

        drawableRight(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                int start = getWidth() - getTotalPaddingRight() + getPaddingRight(); // 起始位置
                int end = getWidth(); // 结束位置
                boolean available = (event.getX() > start) && (event.getX() < end);
                if (available) {
                    if (!bShowNormal) {
                        bShowNormal = true;
                        this.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        drawableRight(false);
                    } else {
                        bShowNormal = false;
                        this.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        drawableRight(true);
                    }

                    setTypeface(Typeface.DEFAULT);
                    setCursorAtEnd();

                    if (mClickCallback != null) {
                        mClickCallback.onClickEvent();
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void drawableRight(boolean bShowPwd) {
        Drawable right;
        if (bShowPwd) {
            right = mShowPassWord;
        } else {
            right = mShowNormal;
        }
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    private void setCursorAtEnd() {
        CharSequence text = this.getText();
        Spannable spanText = (Spannable) text;
        Selection.setSelection(spanText, text.length());
    }
}

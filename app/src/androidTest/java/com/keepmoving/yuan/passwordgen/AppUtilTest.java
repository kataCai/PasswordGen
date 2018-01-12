package com.keepmoving.yuan.passwordgen;

import android.support.test.runner.AndroidJUnit4;

import com.keepmoving.yuan.passwordgen.util.AppDataUtils;
import com.keepmoving.yuan.passwordgen.util.LogUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by caihanyuan on 2018/1/9.
 */
@RunWith(AndroidJUnit4.class)
public class AppUtilTest {

    @Test
    public void teseGetPhoneId() {
        String phoneNumber = AppDataUtils.getPhoneNumber();
        LogUtils.d("number is :" + phoneNumber);

        Assert.assertNotSame(phoneNumber, "");
    }

    @Test
    public void testGetOperatorName() {
        String operatorName = AppDataUtils.getNetworkOperatorName();
        LogUtils.d("operatorName is :" + operatorName);
        Assert.assertNotSame(operatorName, "");
    }
}

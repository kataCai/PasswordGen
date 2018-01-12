package com.keepmoving.yuan.passwordgen.util;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by caihanyuan on 2018/1/9.
 * <p>
 * token生成器
 */

public class TokenProcessor {
    private static long sPrevious;

    /**
     * 根据当前时间戳生成token
     *
     * @return
     */
    public static synchronized String generateToken() {
        return generateToken(null, true);
    }

    /**
     * 根据msg或者当前时间生成token
     *
     * @param msg        自定义字符信息
     * @param timeChange 是否用当前时间戳
     * @return
     */
    public static synchronized String generateToken(String msg, boolean timeChange) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            if (TextUtils.isEmpty(msg) || timeChange) {
                long current = System.currentTimeMillis();
                if (current == sPrevious) current++;
                sPrevious = current;
                byte now[] = (new Long(current)).toString().getBytes();
                md.update(now);
            } else {
                md.update(msg.getBytes());
            }

            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String toHex(byte buffer[]) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);
        for (int i = 0; i < buffer.length; i++) {
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
        }

        return sb.toString();
    }
}

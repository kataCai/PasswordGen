//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.keepmoving.yuan.passwordLib;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordCreator {
    private static char[] specialChar = new char[]{'~', '@', '#', '$', '^', '&', '*', '[', ']'};
    public static MessageDigest md5Digest;

    /**
     * 生成纯数字密码
     *
     * @param mainKey
     * @param domain
     * @param username
     * @param versionCode
     * @param numberLen
     * @return
     */
    public static String createNumberPassword(String mainKey, String domain, String username, String versionCode, int numberLen) {
        String password = "";
        byte[] secret = createSecretByte(mainKey, domain, username, versionCode);
        if (secret != null) {
            password = (new BigInteger(1, secret)).toString(10);
        }

        if (password.length() > numberLen) {
            password = password.substring(0, numberLen);
        }

        return password;
    }

    /**
     * 生成特殊字符，数字，字母混合密码
     *
     * @param mainKey
     * @param domain
     * @param username
     * @param versionCode
     * @param numberLen
     * @return
     */
    public static String createMixPassword(String mainKey, String domain, String username, String versionCode, int numberLen) {
        String password = "";
        byte[] secret = createSecretByte(mainKey, domain, username, versionCode);
        BigInteger bigInteger = null;
        if (secret != null) {
            if (secret != null) {
                bigInteger = new BigInteger(1, secret);
                password = bigInteger.toString(16);
            }

            if (password.length() > numberLen) {
                StringBuilder stringBuilder = new StringBuilder(password);
                int bigNumber = bigInteger.bitCount();
                int specialOffset = bigNumber % numberLen;
                specialOffset %= specialChar.length;
                char special = specialChar[specialOffset];
                stringBuilder.replace(specialOffset, specialOffset + 1, special + "");
                int letterIndex = -1;

                int numIndex;
                for (numIndex = 0; numIndex < numberLen; ++numIndex) {
                    char currentChar = stringBuilder.charAt(numIndex);
                    if (Character.isLetter(currentChar)) {
                        currentChar = Character.toUpperCase(currentChar);
                        stringBuilder.replace(numIndex, numIndex + 1, currentChar + "");
                        letterIndex = numIndex;
                        break;
                    }
                }

                int i;
                if (letterIndex == -1) {
                    for (numIndex = 0; numIndex < numberLen; ++numIndex) {
                        if (numIndex != specialOffset) {
                            i = bigNumber % 25 + 65;
                            stringBuilder.replace(numIndex, numIndex + 1, (char) i + "");
                            letterIndex = numIndex;
                            break;
                        }
                    }
                }

                numIndex = -1;

                for (i = 0; i < numberLen; ++i) {
                    char currentChar = stringBuilder.charAt(i);
                    if (Character.isDigit(currentChar)) {
                        numIndex = i;
                        break;
                    }
                }

                if (numIndex == -1) {
                    for (i = 0; i < numberLen; ++i) {
                        if (i != specialOffset && i != letterIndex) {
                            int numberCount = bigNumber % 10;
                            stringBuilder.replace(i, i + 1, numberCount + "");
                            break;
                        }
                    }
                }

                password = stringBuilder.substring(0, numberLen);
            }
        }

        return password;
    }

    /**
     * 生成数字，字母混合密码
     *
     * @param mainKey
     * @param domain
     * @param username
     * @param versionCode
     * @param numberLen
     * @return
     */
    public static String createNumberCharactorPassword(String mainKey, String domain, String username, String versionCode, int numberLen) {
        String password = "";
        byte[] secret = createSecretByte(mainKey, domain, username, versionCode);
        BigInteger bigInteger = null;

        if (secret != null) {
            if (secret != null) {
                bigInteger = new BigInteger(1, secret);
                password = bigInteger.toString(16);
            }

            if (password.length() > numberLen) {
                int bigNumber = bigInteger.bitCount();
                StringBuilder stringBuilder = new StringBuilder(password);
                int letterIndex = -1;

                int numIndex;
                for (numIndex = 0; numIndex < numberLen; ++numIndex) {
                    char currentChar = stringBuilder.charAt(numIndex);
                    if (Character.isLetter(currentChar)) {
                        currentChar = Character.toUpperCase(currentChar);
                        stringBuilder.replace(numIndex, numIndex + 1, currentChar + "");
                        letterIndex = numIndex;
                        break;
                    }
                }

                int i;
                if (letterIndex == -1) {
                    for (numIndex = 0; numIndex < numberLen; ++numIndex) {
                            i = bigNumber % 25 + 65;
                            stringBuilder.replace(numIndex, numIndex + 1, (char) i + "");
                            letterIndex = numIndex;
                            break;
                    }
                }

                numIndex = -1;

                for (i = 0; i < numberLen; ++i) {
                    char currentChar = stringBuilder.charAt(i);
                    if (Character.isDigit(currentChar)) {
                        numIndex = i;
                        break;
                    }
                }

                if (numIndex == -1) {
                    for (i = 0; i < numberLen; ++i) {
                        if (i != letterIndex) {
                            int numberCount = bigNumber % 10;
                            stringBuilder.replace(i, i + 1, numberCount + "");
                            break;
                        }
                    }
                }

                password = stringBuilder.substring(0, numberLen);
            }

        }

        return password;
    }

    private static byte[] createSecretByte(String mainKey, String domain, String username, String versionCode) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(mainKey).append(domain).append(username).append(versionCode);
        String key = keyBuilder.toString();
        if (md5Digest != null) {
            md5Digest.update(key.getBytes(Charset.forName("UTF-8")));
            return md5Digest.digest();
        } else {
            return null;
        }
    }

    static {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var1) {
            var1.printStackTrace();
        }

    }
}

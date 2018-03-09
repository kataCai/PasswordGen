package com.keepmoving.yuan.passwordgen;

import com.keepmoving.yuan.passwordLib.PasswordCreator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void passwordTest(){
        String password = PasswordCreator.createNumberCharactorPassword("caiddd","疼",
                "1350771828", "1", 8);
        System.out.println(password);

        password = PasswordCreator.createMixPassword("caiddd","疼",
                "1350771828", "1", 8);
        System.out.println(password);
    }
}
package test;

import gui.Login;
import util.SQLDate;

import java.util.Date;
import java.util.Map;

/**
 * Created by baislsl on 17-4-26.
 */
public class Test {
    public static void main(String[] args) {
        loginTest();
        // test();
    }

    public static void loginTest() {
        Login login = new Login();
        login.start("lib_root", "lib_root");
    }

    public static void test(){
        Map<Integer, Integer> result3;
        Date date = new Date();
        System.out.println(date);
        System.out.print(SQLDate.getDifferDay("2017-5-22"));
    }

}



package com.zuel.syzc.spark.test;


public class AbnormalTest {

    static int a = 1;
    public static void main(String[] args) {
        System.out.println(AbnormalTest.a);

        AbnormalTest.a = 6;
        System.out.println(AbnormalTest.a);
        AbnormalTest.a = 7;
        System.out.println(AbnormalTest.a);
        System.out.println(new AbnormalTest());

    }
}

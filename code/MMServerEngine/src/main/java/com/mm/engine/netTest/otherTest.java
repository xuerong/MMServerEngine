package com.mm.engine.netTest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by a on 2016/11/14.
 */
public class otherTest {
    static int a;
    public static void main(String[] args){
//        Integer a = 1;
//        Integer b = 2;
//        Integer c = 3;
//        Integer d = 3;
//        Integer e = 321;
//        Integer f = 321;
//
//        Long g = 3l;
//        System.out.println(c==d);
//        System.out.println(e==f);
//        System.out.println(c==(a+b));
//        System.out.println(c.equals(a+b));
//        System.out.println(g==(a+b));
//        System.out.println(g.equals(a+b));


        final CountDownLatch latch = new CountDownLatch(10);
        for (int i=0;i<10;i++) {

            new Thread(){
                public void run(){
                    try{
                        Thread.sleep((int)(Math.random()*1000));
                        latch.countDown();
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        for(int i=0;i<3;i++) {
            final int index = i;
            new Thread(){
                public void run(){
                    try{
                        System.out.println("en"+index);
                        latch.await();
                        System.out.println("de"+index);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        try{
            System.out.println("en"+11);
            latch.await();
            System.out.println("de"+11);
        }catch (Throwable e){
            e.printStackTrace();
        }


//        System.out.println(a);
//
//        String str1 = new StringBuilder("hahas").append("heiheiha").toString();
////        String b = str1.intern();
//        System.out.println(str1.intern() == str1);
//        String str2 = new StringBuilder("ja").append("va").toString();
////        String a = str2.intern();
//        System.out.println(str2.intern() == str2);
    }

//    public static String myMethod(List<String> first){
//
//        return "";
//    }
    public static int myMethod(List<Integer> first){
        List<Integer> a= Arrays.asList(1,2,3,4);
        return 0;
    }

    public void test(){
        final Lock lock = new ReentrantLock();
        final Object locker = new Object();
        Thread t1=new Thread(){
            @Override
            public void run(){
                try {
                    synchronized (locker){
                        locker.wait();
                        System.out.println("do it");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2=new Thread(){
            @Override
            public void run(){
                try {
                    Thread.sleep(100);
                    synchronized (locker){
                        locker.notify();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        t1.start();
        t2.start();
    }
}

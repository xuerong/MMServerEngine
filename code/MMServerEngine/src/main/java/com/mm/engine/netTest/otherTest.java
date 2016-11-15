package com.mm.engine.netTest;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by a on 2016/11/14.
 */
public class otherTest {
    public static void main(String[] args){
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

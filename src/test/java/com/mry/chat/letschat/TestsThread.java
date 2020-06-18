package com.mry.chat.letschat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestsThread {


    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ThreadPoolExecutor tpool = new ThreadPoolExecutor(10, 10, 20L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new XbThreadFactory("normal_sender"));

    public static void main(String[] args) {

        for (int i = 0; i < 100; i++) {
            final int j = i;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                  /*  try {
                        System.out.println(Thread.currentThread().getName() + "xiancheng kaishi dengdai ");
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    System.out.println(Thread.currentThread().getName() + "-----------------------" + j);
                }
            };
            System.out.println("-------------the thread pool ..."+tpool.getActiveCount());
            tpool.execute(run);
        }
        System.out.println(" the thread action ....");
        //countDownLatch.countDown();

    }
}

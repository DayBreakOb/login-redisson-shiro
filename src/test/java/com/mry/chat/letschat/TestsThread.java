package com.mry.chat.letschat;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;

import com.google.common.collect.Maps;
import com.mry.http.pool.Config;
import com.mry.http.pool.HttpResponeEntity;
import com.mry.http.pool.HttpsClient;

public class TestsThread {


    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ThreadPoolExecutor tpool = new ThreadPoolExecutor(20, 20, 20L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new IThreadFactory("normal_sender"));
	static Config cfg = new Config();
	static RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(20000).setConnectTimeout(20000)
			.setSocketTimeout(20000).build();
    public static void main(String[] args) {
    	String uuid = UUID.randomUUID().toString().replace("-", "");
		cfg.setUuid(uuid);
		cfg.setRequestConfig(requestConfig);
		final String url = "http://localhost:10080/test_str";
        for (int i = 0; i < 100; i++) {
         tpool.execute(new Runnable() {
                @Override
                public void run() {
                	try {
						countDownLatch.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	HttpResponeEntity response = HttpsClient.httpsGet(url, Maps.newTreeMap(), cfg);
               
                }
            });
            System.out.println("-------------the thread pool ..."+tpool.getActiveCount());
        };
        countDownLatch.countDown();
        try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(" the thread action ....");
        //countDownLatch.countDown();

    }
}

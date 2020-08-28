package com.mry.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class IThreadFactory implements ThreadFactory{

	private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
	private ThreadGroup group;
	private AtomicInteger threadNumber = new AtomicInteger(1);
	private String namePrefix;
	
	
	
	
	public IThreadFactory(String threadname) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = "mry-"+threadname+ POOL_NUMBER.getAndIncrement() + "-thread-";
	}




	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		t.setDaemon(true);
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}
}

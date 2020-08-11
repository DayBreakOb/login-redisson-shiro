package com.mry.chat.letschat;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;



public class IThreadFactory implements ThreadFactory{

	
	private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
	private ThreadGroup group;
	private AtomicInteger threadNumber = new AtomicInteger(1);
	private String namePrefix;
	
	
	
	
	public IThreadFactory(String thrname) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = "client--"+thrname+ "-thread-";
	}




	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement() );
		System.out.println("the thread " +t.getName()+"...has been create ...");
		t.setDaemon(true);
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}
	
	

}

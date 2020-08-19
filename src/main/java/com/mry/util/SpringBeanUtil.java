package com.mry.util;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanUtil implements ApplicationContextAware{
	private static ApplicationContext appcontext;
	
	
	public static void setAppcontext(ApplicationContext appcontext) {
		SpringBeanUtil.appcontext = appcontext;
	}


	public static Object getBean(Class<?> clazz) {
		return appcontext.getBean(clazz);
	}

	public static Object getBean(String name) {
		return appcontext.getBean(name);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		SpringBeanUtil.appcontext  = applicationContext;
	}


}

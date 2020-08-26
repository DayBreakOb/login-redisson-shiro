package com.mry.datasource.druid;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class DynamicDataSourceAspect {

	
	private static Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);
	
	public DynamicDataSourceAspect(){}
	
	  @Pointcut("execution(* com.mry.dao..*.*(..))")
	    public void dynamicDataPointCut() {

	    }

	    @Before(value = "dynamicDataPointCut()")
	    public Object aroundMapperMethod(JoinPoint joinPoint) throws Throwable {
	    	Signature signnature = joinPoint.getSignature();
	    	String name = signnature.getDeclaringTypeName();
	    	if (name.contains("data1")) {
	    		DynamicDataSourceContextHolder.setDataSourceKey("datasource");
			}else {
		   		DynamicDataSourceContextHolder.setDataSourceKey("datasource1");
			}
			return joinPoint;
	    }
	    
	    @After("dynamicDataPointCut())")
	    public void restoreDataSource(JoinPoint point) {
	        DynamicDataSourceContextHolder.clear();
	        logger.info("Restore DataSource to [{}] in Method [{}]",
	                DynamicDataSourceContextHolder.getDataSourceKey(), point.getSignature());
	    }
}

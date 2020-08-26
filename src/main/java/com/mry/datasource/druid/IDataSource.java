package com.mry.datasource.druid;

import java.io.IOException;
import java.util.HashMap;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;


@Configuration
public class IDataSource {

	private static Logger logger = LoggerFactory.getLogger(IDataSource.class);

	public DataSource dataSource() {
		DruidDataSource datasource = new DruidDataSource();
		datasource.setUsername(DataSourceConfig.username1);
		datasource.setPassword(DataSourceConfig.password1);
		datasource.setUrl(DataSourceConfig.datasource1url);
		datasource.setDriverClassName(DataSourceConfig.driver1);
		return datasource;
	}
	

	public DataSource dataSource1() {
		DruidDataSource datasource = new DruidDataSource();
		datasource.setUsername(DataSourceConfig.username2);
		datasource.setPassword(DataSourceConfig.password2);
		datasource.setUrl(DataSourceConfig.datasource1url2);
		datasource.setDriverClassName(DataSourceConfig.driver2);
		return datasource;
	}
	
	@Bean
	public DynamicDataSource dynamicDataSource() {
		
		DynamicDataSource dynamicDataSource = DynamicDataSource.getInstance();
		DataSource datasource = dataSource();
		DataSource datasource1 = dataSource1();
		HashMap<Object, Object> targetDataSources = new HashMap<Object, Object>();
		targetDataSources.put("datasource", datasource);
		targetDataSources.put("datasource1", datasource1);
		dynamicDataSource.setTargetDataSources(targetDataSources);
		dynamicDataSource.setDefaultTargetDataSource(datasource);
		return dynamicDataSource;
	}

	@Bean
	public SqlSessionFactoryBean sqlSessionFactoryBean(DynamicDataSource dataSource) {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		PathMatchingResourcePatternResolver resourceResplver = new PathMatchingResourcePatternResolver();
		try {
			// new resources"classpath*:sqlmap/*-mapper.xml"
			Resource[] mappers = resourceResplver.getResources("classpath*:sqlmap/*-mapper.xml");
			sqlSessionFactoryBean.setMapperLocations(mappers);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("resources\"classpath*:sqlmap/*-mapper.xml  loading error");
		}
		return sqlSessionFactoryBean;

	}

	@Bean
	public MapperScannerConfigurer mapperScannerConfigurer() {
		MapperScannerConfigurer mapperscan = new MapperScannerConfigurer();
		mapperscan.setBasePackage("com.mry.dao.data1,com.mry.dao.data2");
		return mapperscan;
	}

	@Bean
	public PlatformTransactionManager dataSourceTransactionManager(DynamicDataSource dataSource) {
		DataSourceTransactionManager transactionmanager = new DataSourceTransactionManager();
		transactionmanager.setDataSource(dataSource);
		return transactionmanager;
	}

}

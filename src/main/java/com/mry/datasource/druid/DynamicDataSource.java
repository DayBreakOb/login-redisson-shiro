package com.mry.datasource.druid;

import java.util.Map;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.google.common.collect.Maps;

public class DynamicDataSource extends AbstractRoutingDataSource {

	private static DynamicDataSource dataSource;


	private final static Map<Object, Object> dataSourceMap = Maps.newConcurrentMap();
	// load the current datasource ...
	@Override
	protected Object determineCurrentLookupKey() {
		// TODO Auto-generated method stub
		return DynamicDataSourceContextHolder.getDataSourceKey();
	}

	@Override
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		// TODO Auto-generated method stub
		super.setTargetDataSources(targetDataSources);
		dataSourceMap.putAll(targetDataSources);
	}

	public Map<Object, Object> getDataSourceMap() {
		return dataSourceMap;
	}

	/**
	 * 单例方法
	 * 
	 * @return
	 */
	public static synchronized DynamicDataSource getInstance() {
		if (dataSource == null) {
			synchronized (DynamicDataSource.class) {
				if (dataSource == null) {
					dataSource = new DynamicDataSource();
				}
			}
		}
		return dataSource;
	}

	public static boolean isExistsDataSource(String key) {
		return dataSourceMap.containsKey(key);
	}

}

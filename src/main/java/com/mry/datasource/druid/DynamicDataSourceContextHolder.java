package com.mry.datasource.druid;

public class DynamicDataSourceContextHolder {

	private static final ThreadLocal<String> dataSourceKey = new ThreadLocal<String>() {
		protected String initialValue() {
			return "datasource";
		};
	};

	public static synchronized void setDataSourceKey(String key) {
		dataSourceKey.set(key);
	}

	public static String getDataSourceKey() {
		// TODO Auto-generated method stub
		return dataSourceKey.get();
	}

	public static void clear() {
		dataSourceKey.remove();
	}

}

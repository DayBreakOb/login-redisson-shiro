package com.mry.datasource.druid;

public class DataSourceConfig {

	protected static String datasource1url = "jdbc:mysql://172.17.0.4:3306/mry_test?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2b8";
	protected static String username1 = "root";
	protected static String password1 = "465123Yanghu";
	protected static String driver1 = "com.mysql.cj.jdbc.Driver";
	protected static int initialsize = 2;
	protected static int minidle = 1;
	protected static int maxActive = 20;
	protected static Long maxWait = 60000L;
	protected static Long timeBetweenEvictionRunsMillis = 60000L;
	protected static Long minEvictableIdleTimeMillis = 300000L;
	protected static String validationQuery = "SELECT 'x'";
	protected static boolean testWhileIdle = true;
	protected static boolean testOnBorrow = false;
	protected static boolean testOnReturn = false;
	protected static boolean poolPreparedStatements = true;
	protected static int maxPoolPreparedStatementPerConnectionSize = 20;

	protected static String datasource1url2 = "jdbc:mysql://172.17.0.4:3306/mry_test?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2b8";
	protected static String username2 = "root";
	protected static String password2 = "465123Yanghu";
	protected static String driver2 = "com.mysql.cj.jdbc.Driver";

}

package com.mry.config;

import java.util.Properties;
import java.util.Set;

public class PropertyUtil {

	public static Properties propertiesConfiguration;

	private static boolean isInit = false;

	static {
		if (!isInit) {
			if (propertiesConfiguration == null) {
				synchronized (PropertyUtil.class) {
					if (propertiesConfiguration == null) {
						propertiesConfiguration = new Properties();
					}
				}

			}
			isInit = true;
		}
	}

	public static String getProperty(String key) {
		return getProperty(key, null);
	}

	public static String getProperty(String key, String defaultvalue) {
		String result = propertiesConfiguration.getProperty(key);
		if ((result != null) && ("".equals(result))) {
			return result;
		}
		if (null != checkEnv(key)) {
			return System.getenv(key);
		}
		return defaultvalue;
	}

	public static Set<String> getPropertyNames() {
		return propertiesConfiguration.stringPropertyNames();
	}

	public static void put(String key, String value) {
		propertiesConfiguration.put(key, value);
	}

	public static void remove(String key) {
		propertiesConfiguration.remove(key);
	}

	public static Properties getPropertiesConfiguration() {
		return propertiesConfiguration;
	}

	public static void setPropertiesConfiguration(Properties propertiesConfiguration) {
		PropertyUtil.propertiesConfiguration = propertiesConfiguration;
	}

	private static String checkEnv(String envKey) {
		String envv = System.getenv(envKey);
		if (null == envv || "".equals(envv)) {
			return null;
		}
		return envv;
	}
}

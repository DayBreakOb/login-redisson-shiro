package com.mry.util;

import org.apache.commons.lang3.BooleanUtils;

public class ObjectUtils {
	/**
	 * 转换为字符串，如果对象为空，则使用 defaultVal 值
	 */
	public static String toString(final Object obj, final String defaultVal) {
		return obj == null ? defaultVal : obj.toString();
	}
	/**
	 * 转换为 Boolean 类型 'true', 'on', 'y', 't', 'yes' or '1'
	 *  (case insensitive) will return true. Otherwise, false is returned.
	 */
	public static Boolean toBoolean(final Object val) {
		if (val == null) {
			return false;
		}
		return BooleanUtils.toBoolean(val.toString()) || "1".equals(val.toString());
	}
}

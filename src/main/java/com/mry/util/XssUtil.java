package com.mry.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class XssUtil {
	private static final String DEFAULT_URL_ENCODING = "UTF-8";
	private static Logger logger = LoggerFactory.getLogger(XssUtil.class);
	// 预编译XSS过滤正则表达式
	private static List<Pattern> xssPatterns = Lists.newArrayList(Pattern.compile(
			"(<\\s*(script|link|style|iframe)([\\s\\S]*?)(>|<\\/\\s*\\1\\s*>))|(</\\s*(script|link|style|iframe)\\s*>)",
			Pattern.CASE_INSENSITIVE),
			Pattern.compile(
					"\\s*(href|src)\\s*=\\s*(\"\\s*(javascript|vbscript):[^\"]+\"|'\\s*(javascript|vbscript):[^']+'|(javascript|vbscript):[^\\s]+)\\s*(?=>)",
					Pattern.CASE_INSENSITIVE),
			Pattern.compile("\\s*on[a-z]+\\s*=\\s*(\"[^\"]+\"|'[^']+'|[^\\s]+)\\s*(?=>)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(eval\\((.*?)\\)|xpression\\((.*?)\\))", Pattern.CASE_INSENSITIVE),
			Pattern.compile("^(javascript:|vbscript:)", Pattern.CASE_INSENSITIVE));

	/**
	 * XSS 非法字符过滤，内容以<!--HTML-->开头的用以下规则（保留标签）
	 * 
	 * @author ThinkGem
	 */
	public static String xssFilter(String text) {
		String oriValue = StringUtils.trim(text);
		if (text != null) {
			String value = oriValue;
			for (Pattern pattern : xssPatterns) {
				Matcher matcher = pattern.matcher(value);
				if (matcher.find()) {
					value = matcher.replaceAll(StringUtils.EMPTY);
				}
			}
			// 如果开始不是HTML，XML，JOSN格式，则再进行HTML的 "、<、> 转码。
			if (!StringUtils.startsWithIgnoreCase(value, "<!--HTML-->") // HTML
					&& !StringUtils.startsWithIgnoreCase(value, "<?xml ") // XML
					&& !StringUtils.contains(value, "id=\"FormHtml\"") // JFlow
					&& !(StringUtils.startsWith(value, "{") && StringUtils.endsWith(value, "}")) // JSON Object
					&& !(StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) // JSON Array
			) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < value.length(); i++) {
					char c = value.charAt(i);
					switch (c) {
					case '>':
						sb.append("＞");
						break;
					case '<':
						sb.append("＜");
						break;
					case '\'':
						sb.append("＇");
						break;
					case '\"':
						sb.append("＂");
						break;
//					case '&':
//						sb.append("＆");
//						break;
//					case '#':
//						sb.append("＃");
//						break;
					default:
						sb.append(c);
						break;
					}
				}
				value = sb.toString();
			}
			if (logger.isInfoEnabled() && !value.equals(oriValue)) {
				logger.info("xssFilter: {}   <=<=<=   {}", value, text);
			}
			return value;
		}
		return null;
	}

	/**
	 * URL 编码, Encode默认为UTF-8.
	 */
	public static String encodeUrl(String part) {
		return encodeUrl(part, DEFAULT_URL_ENCODING);
	}

	/**
	 * URL 编码, Encode默认为UTF-8.
	 */
	public static String encodeUrl(String part, String encoding) {
		if (part == null) {
			return null;
		}
		try {
			return URLEncoder.encode(part, encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * URL 解码, Encode默认为UTF-8.
	 */
	public static String decodeUrl(String part) {
		return decodeUrl(part, DEFAULT_URL_ENCODING);
	}

	/**
	 * URL 解码, Encode默认为UTF-8.
	 */
	public static String decodeUrl(String part, String encoding) {
		if (part == null) {
			return null;
		}
		try {
			return URLDecoder.decode(part, encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}

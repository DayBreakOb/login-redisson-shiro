package com.mry.util;

import java.io.IOException;
import java.util.TimeZone;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * @author root
 */
public class IXmlMapper extends XmlMapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IXmlMapper() {
		new Jackson2ObjectMapperBuilder().configure(this);
		this.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
	}

	private static final class XmlMapperHoler {
		private static final IXmlMapper instance = new IXmlMapper();
	}

	/**
	 * Object可以是POJO，也可以是Collection或数组。（根据 JsonView 渲染）
	 */
	private String toXmlString(Object object, Class<?> jsonView) {
		try {
			return this.writerWithView(jsonView).writeValueAsString(object);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Object可以是POJO，也可以是Collection或数组。
	 */
	public String toXmlString(Object object) {
		try {
			return this.writeValueAsString(object);
		} catch (IOException e) {
			return null;
		}
	}

	public static String toXml(Object obj, Class<?> jsonView) {

		return XmlMapperHoler.instance.toXmlString(obj, jsonView);

	}

	
	public static String toXml(Object obj) {

		return XmlMapperHoler.instance.toXmlString(obj);

	}
}

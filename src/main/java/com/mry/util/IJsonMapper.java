package com.mry.util;

import java.io.IOException;
import java.util.TimeZone;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class IJsonMapper extends ObjectMapper{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	
	
	private static  final class JsonMapperHoler{
		private static final IJsonMapper instance = new IJsonMapper();
	}
	
	private IJsonMapper() {
		
		new Jackson2ObjectMapperBuilder().configure(this);
		this.setSerializationInclusion(Include.NON_NULL);
		
		this.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		this.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		this.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
		this.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
			@Override
			public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
				// TODO Auto-generated method stub
				gen.writeString(StringUtils.EMPTY);
			}
		});
		
	}
	
	
	public static String toJson(Object object, Class<?> jsonView) {
	
		return JsonMapperHoler.instance.toJsonString(object,jsonView);
	}

	public static String toJson(Object object) {
		
		return JsonMapperHoler.instance.toJsonString(object);
	}

	private String toJsonString(Object object, Class<?> jsonView) {
		// TODO Auto-generated method stub
		try {
			return this.writerWithView(jsonView).writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Object可以是POJO，也可以是Collection或数组。
	 * 如果对象为Null, 返回"null".
	 * 如果集合为空集合, 返回"[]".
	 */
	private String toJsonString(Object object) {
		try {
			return this.writeValueAsString(object);
		} catch (IOException e) {
			return null;
		}
	}
}

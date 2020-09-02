package com.mry.rocketmq;

import java.util.HashMap;

// 消息承载对象 ， 真正消息内容是  Object msg 字段，    其他的字段为  辅助消息 字段
public class RmqItem {
	private Object msg;   // 消息内容
	private String key;    // 消息key
	private String memo;   // 消息备注
	private String tag;    // 消息表情
	private HashMap<String, Object> ext;   // 用户扩展的消息信息，不是正文， 正文用于业务， 扩展用户用自定义信息，备注等
	private HashMap<String, Object> context; // 消息系统上下文， 这个是消息系统 预埋点 功能， 与用户无关
	
	
	
	public RmqItem() {
	}
	public Object getMsg() {
		return msg;
	}
	public void setMsg(Object msg) {
		this.msg = msg;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public HashMap<String, Object> getExt() {
		return ext;
	}
	public void setExt(HashMap<String, Object> ext) {
		this.ext = ext;
	}
	public HashMap<String, Object> getContext() {
		return context;
	}
	public void setContext(HashMap<String, Object> context) {
		this.context = context;
	}

}

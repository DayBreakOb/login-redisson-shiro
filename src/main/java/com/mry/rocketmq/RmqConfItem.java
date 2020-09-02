package com.mry.rocketmq;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class RmqConfItem {
	private String topic; // 消息主题
	private String group; // 消息分组
	private String passwd; // 这个分组的 访问密码
	private String memo; // 相关备注
	private String res = "0"; // 0, ok, 1, error, > 0 错误信息码
	private int groupType = MESSAGE_GROUP_TYPE_RECEIVER;

	/**
	 * 如果用fastjson或jackson进行对象与字符串转化，必须增加无惨构造方法。
	 * */
	public RmqConfItem() {
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	private String working = "1"; // 0, 暂停， > 0 运行中
	private int tryCount = 3; // 消息默认投递次数, 最小1次， 最大10次， 写在程序逻辑中， 不能调整， 若是选择了1次，
								// 若是消息出现了处理问题， 程序不会在发送给你
	private boolean retry = true; // 若是关闭重试服务， 有处理不了的消息， 就会被丢弃

	public final static String RETRY_MESSAGE_INSERT_OK = "0";
	public final static String RETRY_MESSAGE_INSERT_ERROR = "1";

	public final static int MESSAGE_GROUP_TYPE_SENDER = 1;
	public final static int MESSAGE_GROUP_TYPE_RECEIVER = 2;

	/**
	 * 
	 * 对于业务系统必须打开 幂等性， 这样您可以选择 推荐使用 tryCount = 3, retry = true, 这样消息不丢
	 * 
	 * 否则， 在您处理不了消息时， 就会丢消息
	 * 
	 * 
	 **/
	private HashMap<String, Object> ext =  new HashMap<String, Object>();

	public int getTryCount() {
		return tryCount;
	}

	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public RmqConfItem(String topic, String group, String passwd, String memo,HashMap<String, Object> ext) {
		super();
		this.topic = topic;
		this.group = group;
		this.passwd = passwd;
		this.memo = memo;
		this.setExt(ext);
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getRes() {
		return res;
	}

	public void setRes(String res) {
		this.res = res;
	}

	public String getWorking() {
		return working;
	}

	public void setWorking(String working) {
		this.working = working;
	}

	public HashMap<String, Object> getExt() {
		return ext;
	}

	public void setExt(HashMap<String, Object> ext) {
		if (this.ext.size()!=0) {
			Iterator<Entry<String, Object>> temp = ext.entrySet().iterator();
			while (temp.hasNext()) {
				Entry<String, Object> entry = temp.next();
				this.ext.put(entry.getKey(), entry.getValue());
			}
		}else {
			this.ext = ext;
		}
			
	}


}

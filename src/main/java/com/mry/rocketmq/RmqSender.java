package com.mry.rocketmq;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class RmqSender {
	final static Logger logger = LoggerFactory.getLogger(RmqSender.class);

	private int SendMsgTimeout = 3000;

	private DefaultMQProducer producer = null;
	private ServiceState serviceState = ServiceState.CREATE_JUST;

	public int getSendMsgTimeout() {
		return SendMsgTimeout;
	}

	public void setSendMsgTimeout(int sendMsgTimeout) {
		SendMsgTimeout = sendMsgTimeout;
	}

	public DefaultMQProducer getProducer() {
		return producer;
	}

	public void setProducer(DefaultMQProducer producer) {
		this.producer = producer;
	}

	private String names; // rmq 名称服务器 地址列表
	private String zk; // zk 地址， 在重试消息， 死信等功能时
	private String topic; // 消息主题
	private String group; // 消息分组
	private String passwd; // 这个分组的 访问密码
	private String memo; // 相关备注
	// Multithread use the sender.sendMsg
	private volatile AtomicInteger sendcount = new AtomicInteger(1);

	private boolean working = false; // 标志程序是否 工作状态

	private String namespace;

	private ReentrantLock lock = new ReentrantLock();

	public String getNames() {
		return names;
	}

	public void setNames(String names) {
		this.names = names;
	}

	public String getZk() {
		return zk;
	}

	public void setZk(String zk) {
		this.zk = zk;
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

	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public RmqSender(String names, String zk, String topic, String group, String passwd, String memo) {
		super();
		this.names = names;
		this.zk = zk;
		this.topic = topic;
		this.group = group;
		this.passwd = passwd;
		this.memo = memo;
	}

	public void shutdown(boolean shutdownNow) {
		if ((!working) && (this.serviceState == ServiceState.RUNNING)) {
			producer.shutdown();
			logger.debug("the sender has been shutdown with no working...");
			return;
		}
		long start = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep(1000);
				if ((this.sendcount.get() == 1)) {
					logger.debug(
							"In  three seconds ,there is no thread for send msg ,so the producer could be shutdown ...");
					break;
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug("there is " + (sendcount.get() - 1)
					+ " thread is running to send msg ...; the sender will be shutdown when all send_thread over");
			long end = System.currentTimeMillis();
			if ((end - start) > (30 * 1000)) {
				logger.debug("In thirty seconds ,the send  will be shutdown immediately...");
				break;
			}
		}
		producer.shutdown();
		logger.debug("the sender has been shutdown naturaly...");
	}

	public boolean start() {
		return InitProducer();
	}

	/**
	 * 初始化消息的发生对象， 全部参数都由外界 在构造函数中 设置
	 * 
	 * @return 返回 null， 或者 "" 为正确初始化， 其他字符串为错误信息
	 */
	private boolean InitProducer() {
		logger.info(">>>>>>>>>>>>>>>>>>>>>初始化生产者>>>>>>>>>>>>>>>>>>>>>>>");
		lock.lock();
		// Launch the instance.
		try {
			if (null == producer) {
				if (namespace != null) {
					producer = new DefaultMQProducer(namespace, group);
				} else {
					producer = new DefaultMQProducer(group);
				}
				// Specify name server addresses.
				producer.setNamesrvAddr(this.getNames());
				producer.setSendMsgTimeout(this.getSendMsgTimeout());
				producer.setVipChannelEnabled(false);
				producer.setSendLatencyFaultEnable(true);
				producer.setRetryAnotherBrokerWhenNotStoreOK(true);
				logger.info("the producer has been init by the group " + this.group + "  the topic " + this.getTopic()
						+ " the namespace " + this.getNamespace());
				// 到配置服务器去检查 配置是否正确
				HashMap<String, Object> ext = new HashMap<String, Object>();
				ext.put("source", "producer");
				RmqConfItem conf = new RmqConfItem(this.topic, this.group, this.passwd, this.memo, ext);
				conf.setGroupType(RmqConfItem.MESSAGE_GROUP_TYPE_SENDER);
				working = true;
				producer.start();
				this.serviceState = ServiceState.RUNNING;
			}
		} catch (Throwable e) {
			logger.error(INIT_PRODUCER_ERROR, e);
			return false;
		} finally {
			lock.unlock();
		}
		return true;
	}

	final static String NOT_INIT_PRODUCER = "----- 没有初始化  producer !!  无法进行后续操作  ";
	final static String INIT_PRODUCER_ERROR = "----- 没有初始化  producer 时发生错误  ";
	final static String SEND_MSG_ERROR = "----- 发送消息时发生错误 ----  ";
	final static String SEND_MSG_START = "----- 开始发送消息 ----  ";
	final static String SEND_MSG_END = "----- 结束消息发送 ----  ";

	static AtomicLong count = new AtomicLong(0L);

	/**
	 * 发送消息
	 * 
	 * @param msg  消息内容
	 * @param key  消息的关键字
	 * @param memo 消息的备注
	 * @param ext  消息扩展信息
	 * @return 返回 null， 为发生异常， 其他为消息对象， 具体查询消息对象信息
	 */

	public SendResult SendMsgDefault(Object msg, String key, String tag, String memo, HashMap<String, Object> ext) {
		this.sendcount.incrementAndGet();
		logger.debug("the sendcount on send tiem  is " + sendcount);
		if (null == producer) {
			boolean res = InitProducer();
			if (!res) {
				return null;
			}
		}

		if (null == producer) {
			logger.error(NOT_INIT_PRODUCER);
			return null;
		}

		if (!working) {
			// logger.error(">>>>>>>>>>> work is pause ,暂停工作中------------");
			return null;
		}
		RmqItem item = new RmqItem();
		item.setMsg(msg);
		item.setKey(key);
		item.setMemo(memo);
		item.setExt(ext);
		HashMap<String, Object> context = new HashMap<String, Object>();
		context.put("rmq.client.api.ver", "0.1");
		context.put("rmq.topic.memo", this.getMemo());
		item.setContext(context);
		String s = new Gson().toJson(item);
		try {
			Message message = new Message(this.getTopic(), tag, key, s.getBytes(RemotingHelper.DEFAULT_CHARSET));
			SendResult sendResult = producer.send(message);
			return sendResult;
		} catch (Throwable e) {
			logger.error(SEND_MSG_ERROR, e);
		} finally {
			this.sendcount.decrementAndGet();
			logger.debug("the sendcount on finally tiem  is " + sendcount);
		}
		return null;
	}

}

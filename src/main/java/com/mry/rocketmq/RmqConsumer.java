package com.mry.rocketmq;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class RmqConsumer {

	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	public boolean isShowDetail() {
		return showDetail;
	}

	public void setShowDetail(boolean showDetail) {
		this.showDetail = showDetail;
	}

	final static Logger logger = LoggerFactory.getLogger(RmqConsumer.class);

	final static String error_msg_info = "请您  重载这个方法， 来接受消息！！！";

	public static class MsgItemReceiver {

		// 处理消息， 抛出异常， 或者 返回 false 通知消息处理失败
		public boolean doWork(Object msg, String key, String tag, String memo, HashMap<String, Object> ext) {
			logger.error(error_msg_info);
			return false; // 通知消息处理失败
		}
	}

	private int SendMsgTimeout = 3000;

	private DefaultMQPushConsumer consumer = null;

	public int getSendMsgTimeout() {
		return SendMsgTimeout;
	}

	public void setSendMsgTimeout(int sendMsgTimeout) {
		SendMsgTimeout = sendMsgTimeout;
	}

	private String names; // rmq 名称服务器 地址列表

	public DefaultMQPushConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(DefaultMQPushConsumer consumer) {
		this.consumer = consumer;
	}

	private String zk; // zk 地址， 在重试消息， 死信等功能时
	private String topic; // 消息主题
	private String group; // 消息分组
	private String passwd; // 这个分组的 访问密码
	private String memo; // 相关备注
	private String from = "0"; // 开始接受位置， 0 从最后位置， 1从最开始位置
	private String broadcast = "0"; // 是否广播消息， 0 非广播消息，就是集群消息， 1广播消息

	private RPCHook rpcHook; // rpcHook RPC hook to execute before each remoting command.

	private String namespace;// used for multiple topic or the group to create the receiver cli
	private int tryCount = 3; // 消息默认投递次数, 最小1次， 最大10次， 写在程序逻辑中， 不能调整， 若是选择了1次，
								// 若是消息出现了处理问题， 程序不会在发送给你

	private boolean retry = true;

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

	public String getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(String broadcast) {
		this.broadcast = broadcast;
	}

	private boolean showDetail = false;

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

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public RPCHook getRpcHook() {
		return rpcHook;
	}

	public void setRpcHook(RPCHook rpcHook) {
		this.rpcHook = rpcHook;
	}

	public RmqConsumer(String names, String zk, String topic, String group, String passwd, String memo) {
		this.names = names;
		this.zk = zk;
		this.topic = topic;
		this.group = group;
		this.passwd = passwd;
		this.memo = memo;
	}

	public void shutdown() {
		consumer.shutdown();
	}

	private ReentrantLock lock = new ReentrantLock();
	private boolean working = false;

	public void OnReceivedMessage(MsgItemReceiver receiver) {
		lock.lock();
		try {
			if (null != consumer) {
				// 若是已经初始化了， 就不在 允许初始化了！
				return;
			}
			if (null == consumer) {
				if (namespace != null && rpcHook != null) {
					consumer = new DefaultMQPushConsumer(namespace, group, rpcHook);
				} else if (namespace != null) {
					consumer = new DefaultMQPushConsumer(namespace, group);
				} else {
					consumer = new DefaultMQPushConsumer(group);
				}
			}
			consumer.setNamesrvAddr(names);
			if (this.from.endsWith("0")) {
				consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
			} else {
				consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
			}
			if (("1").equals(this.broadcast)) {
				consumer.setMessageModel(MessageModel.BROADCASTING);
			} else {
				consumer.setMessageModel(MessageModel.CLUSTERING);
			}
			// subscription expression.it only support or operation such as "tag1 || tag2 ||
			// tag3 if null or * expression,meaning subscribe all
			consumer.subscribe(topic, "*");

			consumer.registerMessageListener(new MessageListenerConcurrently() {
				@Override
				public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
						ConsumeConcurrentlyContext context) {
					for (MessageExt e : msgs) {
						// 获取一条消息的字符串
						try {
							String s = new String(e.getBody());
							// 判断目前是否 在显示详细细节的情况下
							if (showDetail) {
								// 判断是否处于调试模式下
								if (logger.isDebugEnabled()) {
									logger.debug(s + "\t\t 详细信息：" + e.toString());
								}
							}

							// 将json方式的数据， 转换为 对象进行处理
							RmqItem c = new Gson().fromJson(s, RmqItem.class);
							c.setTag(e.getTags());
							c.setKey(e.getKeys());
							// 调用处理函数
							DoWork(c);
						} catch (Throwable e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
				}
			});

			// 到配置服务器去检查 配置是否正确
			HashMap<String, Object> ext = new HashMap<String, Object>();
			ext.put("source", "consumer");
			RmqConfItem conf = new RmqConfItem(this.topic, this.group, this.passwd, this.memo, ext);
			conf.setGroupType(RmqConfItem.MESSAGE_GROUP_TYPE_RECEIVER);
			consumer.start();
			working = true;
		} catch (MQClientException e) {
			logger.error(">>>>>>>>>>>>>>>>>>>Consumer Started failed!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

	}

	// 处理消息对象
	private void DoWork(RmqItem item) {
		boolean bres = DoWorkOnce(item);
		if (bres) {
			return;
		} else {
			int times = 0;
			while (true) {
				bres = DoWorkOnce(item);
				times++;
				if (times == 2) {
					// save the message to local place ...
					break;
				}
			}
		}
	}

	private MsgItemReceiver receiver;

	private boolean DoWorkOnce(RmqItem item) {
		if (null == item) {
			return false;
		}
		if (null == this.receiver) {
			return false;
		}
		try {
			boolean b = this.receiver.doWork(item.getMsg(), item.getKey(), item.getTag(), item.getMemo(),
					item.getExt());
			if (b) {
				return true;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	public abstract void saveFailureMessage(RmqItem item);
}

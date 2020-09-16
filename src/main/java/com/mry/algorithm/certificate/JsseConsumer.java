package com.mry.algorithm.certificate;

import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @Author: chengsh05
 * @Date: 2019/3/8 15:22
 */
public class JsseConsumer {

	public static final String HOST = "ssl://10.95.197.3:8883";
	public static final String TOPIC1 = "taikang/rulee";
	private static final String clientid = "dev001";
	private MqttClient client;
	private MqttConnectOptions options;
	private String userName = "shihuc"; // 非必须
	private String passWord = "shihuc"; // 非必须
	@SuppressWarnings("unused")
	private ScheduledExecutorService scheduler;
	private String sslPemPath = "E:\\HOWTO\\emqtt-ssl\\self1\\";
	private String sslCerPath = "E:\\HOWTO\\emqtt-ssl\\self1\\java\\";

	/*
	 * 
	 * 导入证书和私钥的地方，需要注意，java代码中有个奇怪的现象，对应私钥的导入，私钥文件内容（字符串）中，不能含有类似BEGIN PRIVATE
	 * KEY的头和尾部信息，才能正确的导入并生成PrivateKey对象，但是呢，对应Certificate文件内容（字符串）导入并创建证书对象的时候，
	 * 文件内容中必须要含有BEGIN CERTIFICATE之类的头部信息，否则导入证书失败。
	 * 
	 * 
	 */
	
	
	static class MySSL {

		public static final Charset CHARSET = Charset.defaultCharset();

		public static X509Certificate getCertficate(String devCrtCtx) {
			// TODO Auto-generated method stub
			return null;
		}

		public static PrivateKey getPrivateKey(String devKeyCtx) {
			// TODO Auto-generated method stub
			return null;
		}

		public static PrivateKey getPrivateKey(File file) {
			// TODO Auto-generated method stub
			return null;
		}

		public static SSLSocketFactory getSSLSocketFactory(X509Certificate ca, X509Certificate devCrt,
				PrivateKey devKey, String string) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	private void start() {
		try {
			// host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
			client = new MqttClient(HOST, clientid, new MemoryPersistence());
			// MQTT的连接设置
			options = new MqttConnectOptions();

			String devCrtCtx = FileUtils.readFileToString(new File(sslCerPath, "dev003.crt"), MySSL.CHARSET);
			String devKeyCtx = FileUtils.readFileToString(new File(sslCerPath, "dev003.key"), MySSL.CHARSET);
			X509Certificate devCrt = MySSL.getCertficate(devCrtCtx);
			PrivateKey devKey = MySSL.getPrivateKey(devKeyCtx);

			String rootCACrtPath = "E:\\HOWTO\\emqtt-ssl\\self1\\java\\rootCA0.crt";
			String rootCAKeyPath = "E:\\HOWTO\\emqtt-ssl\\self1\\java\\ca0.key";

			CertificateFactory cAf = CertificateFactory.getInstance("X.509");
			FileInputStream caIn = new FileInputStream(rootCACrtPath);
			X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
			PrivateKey caKey = (PrivateKey) MySSL.getPrivateKey(new File(rootCAKeyPath));

			SSLSocketFactory factory = MySSL.getSSLSocketFactory(ca, devCrt, devKey, "shihuc");
			options.setSocketFactory(factory);

			// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，设置为true表示每次连接到服务器都以新的身份连接
			options.setCleanSession(false);
			// 设置连接的用户名
			options.setUserName(userName);
			// 设置连接的密码
			options.setPassword(passWord.toCharArray());
			// 设置超时时间 单位为秒
			options.setConnectionTimeout(10);
			// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
			options.setKeepAliveInterval(20);
			// 设置重连机制
			options.setAutomaticReconnect(false);
			// 设置回调
			client.setCallback(new BuizCallback());
			MqttTopic topic = client.getTopic(TOPIC1);
			// setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
			// options.setWill(topic, "close".getBytes(), 2, true);//遗嘱
			client.connect(options);
			// 订阅消息
			int[] Qos = { 1 };
			String[] topic1 = { TOPIC1 };
			client.subscribe(topic1, Qos);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws MqttException {
		System.setProperty("javax.net.debug", "ssl,handshake");
		JsseConsumer client = new JsseConsumer();
		client.start();
	}
	
	class BuizCallback implements MqttCallback{

		@Override
		public void connectionLost(Throwable cause) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
}
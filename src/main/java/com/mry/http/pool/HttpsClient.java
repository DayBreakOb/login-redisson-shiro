package com.mry.http.pool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsClient {

	private static Logger logger = LoggerFactory.getLogger(HttpsClient.class);

	private static final int MAX_TOTAL = 10;
	private static final int MAX_PER_ROUTE = 1;
	private static PoolingHttpClientConnectionManager connectionManager;
	private static ConcurrentHashMap<String, PoolingHttpClientConnectionManager> clients = new ConcurrentHashMap<String, PoolingHttpClientConnectionManager>();

	public static BasicCookieStore cookie = new BasicCookieStore();

	private synchronized static void build(Config config) {
		String uuid = config.getUuid();
		Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
		try {

			String httpsisverify = config.getHttpsisverify();
			SSLContext sc = null;
			if ("true".equals(httpsisverify)) {
				String keypath = config.getKeypath();
				String keypass = config.getKeypass();
				String keyinstance = config.getKeyinstance();
				sc = InitSslContext.initSSlContext(keypath, keyinstance, keypass);
			} else {
				sc = InitSslContext.initSSlContext(config);
			}
			socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", new PlainConnectionSocketFactory())
					.register("https", InitSslContext.initSSl(sc, config)).build();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			logger.error(uuid + " clients pool create faulire ...");
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(uuid + " clients pool create faulire ...");
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			logger.error(uuid + " clients pool create faulire ...by https key ");
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			logger.error(uuid + " clients pool create faulire ...by https key ");
			throw new RuntimeException(e);
		}
		connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		// 设置最大连接数
		connectionManager.setMaxTotal(MAX_TOTAL);
		// 设置最大路由
		connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
		new IdleConnectionMonitorThread(connectionManager, uuid).start();
		clients.put(uuid, connectionManager);
	}

	private static CloseableHttpClient getConnection(Config config) {
		PoolingHttpClientConnectionManager cm = clients.get(config.getUuid());
		if (cm == null) {
			HttpsClient.build(config);
			cm = connectionManager;
		}
		CloseableHttpClient httpsclient = HttpClients.custom()
				// 设置连接池管理
				.setConnectionManager(cm)
				// 设置请求配置
				.setDefaultRequestConfig(config.getRequestConfig()).setDefaultCookieStore(cookie)
				// 设置重试次数
				.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
		if (cm != null && cm.getTotalStats() != null) {
			logger.info("now client pool " + config.getUuid() + cm.getTotalStats().toString());
		}
		return httpsclient;
	}

	private static HttpPost createPost(Map<String, Object> headers, String url) {
		HttpPost post = null;
		if ((url != null) && (!"".equals(url))) {
			post = new HttpPost(url);
		}
		if (headers != null) {
			Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				post.setHeader(entry.getKey(), entry.getValue().toString());
			}
		}
		return post;
	}

	public static HttpResponeEntity httpsGet(String url, TreeMap<String, Object> paraMap, Config cfg) {
		CloseableHttpResponse httpResponse = null;
		url = handleUrlGet(url, paraMap);
		String entity = null;
		HttpResponeEntity result = null;
		HttpGet httpget = null;
		try {
			httpget = new HttpGet(url);
			httpResponse = getConnection(cfg).execute(httpget);
			HttpEntity reentity = httpResponse.getEntity();
			entity = EntityUtils.toString(reentity, "UTF-8");
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			if (httpStatusCode == HttpStatus.SC_OK) {
				result = new HttpResponeEntity(httpStatusCode, entity, null);
			} else {
				result = new HttpResponeEntity(httpStatusCode, null, httpResponse.getStatusLine().getReasonPhrase());
			}
			EntityUtils.consume(reentity);
		} catch (ParseException | IOException e) {
			throw new ParseException(e.getMessage());
		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (httpget != null) {
				httpget.releaseConnection();
			}

		}
		return result;

	}

	protected static String handleUrlGet(String url, TreeMap<String, Object> parmmap) {
		if (parmmap.size() > 0) {
			url += "?";
			Set<Entry<String, Object>> m1 = parmmap.entrySet();
			Iterator<Entry<String, Object>> mi = m1.iterator();
			while (mi.hasNext()) {
				Entry<String, Object> cc = mi.next();
				url += cc.getKey() + "=" + cc.getValue();
				if (mi.hasNext()) {
					url += "&";
				}
			}
		}

		return url;
	}

	public static HttpResponeEntity httpsPostDataByte(Config cfg, Map<String, Object> headers, String json,
			String url) {
		logger.info("the requst url = " + url);
		logger.info("the requst json = " + json);
		CloseableHttpResponse httpResponse = null;
		HttpPost post = createPost(headers, url);
		HttpResponeEntity result = null;
		ByteArrayOutputStream ms = null;
		InputStream inentity = null;
		String entity = null;
		try {
			post.setEntity(new StringEntity(json, ContentType.create("application/json", "UTF-8")));
			httpResponse = getConnection(cfg).execute(post);
			HttpEntity entity1 = httpResponse.getEntity();
			Header head = entity1.getContentType();
			String contentType = head.getValue();
			if (!"application/octet-stream".equals(contentType)) {
				entity = EntityUtils.toString(entity1, Consts.UTF_8);
				int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
				if (httpStatusCode == HttpStatus.SC_OK) {
					result = new HttpResponeEntity(httpStatusCode, entity, null);
				} else {
					result = new HttpResponeEntity(httpStatusCode, null,
							httpResponse.getStatusLine().getReasonPhrase());
				}
				EntityUtils.consume(entity1);
			} else {
				inentity = entity1.getContent();
				ms = new ByteArrayOutputStream();
				byte[] buf = new byte[1024 * 10];
				int count = 0;
				while ((count = inentity.read(buf, 0, buf.length)) > 0) {
					ms.write(buf, 0, count);
				}
				byte[] bytefile = ms.toByteArray();
				String rstr = Base64.encodeBase64String(bytefile);
				int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
				if (httpStatusCode == HttpStatus.SC_OK) {
					result = new HttpResponeEntity(httpStatusCode, rstr, null);
				} else {
					result = new HttpResponeEntity(httpStatusCode, null,
							httpResponse.getStatusLine().getReasonPhrase());
				}
				EntityUtils.consume(entity1);
				ms.flush();
				ms.close();
				inentity.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				if (inentity != null) {
					inentity.close();
				}
				if (ms != null) {
					ms.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("关闭流出现异常" + e.getMessage(), e);
			}
		}
		return result;
	}

	private static class IdleConnectionMonitorThread extends Thread {

		private final HttpClientConnectionManager connMgr;
		private final String uuid;
		private volatile boolean shutdown;

		public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr, String uuid) {
			super();
			this.connMgr = connMgr;
			this.uuid = uuid;
		}

		@Override
		public void run() {
			try {
				while (!shutdown) {
					synchronized (this) {
						wait(5000);
						// Close expired connections
						connMgr.closeExpiredConnections();
						logger.info("the connmgr will clise closeExpiredConnections of " + uuid);
						// Optionally, close connections
						// that have been idle longer than 30 sec
						connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
					}
				}
			} catch (InterruptedException ex) {
				// terminate
			}
		}

		public void shutdown() {
			shutdown = true;
			synchronized (this) {
				notifyAll();
			}
		}

	}

}

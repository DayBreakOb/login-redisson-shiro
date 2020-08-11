package com.mry.http.pool;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InitSslContext {

	private static Logger logger = LoggerFactory.getLogger(InitSslContext.class);

	private static X509TrustManager trustManager = new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	};

	public static SSLConnectionSocketFactory initSSl(SSLContext sc, Config config)
			throws NoSuchAlgorithmException, KeyManagementException {
		String isignorehostname ="true";
		HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
		if (((!"".equals(isignorehostname) && (null != isignorehostname))) && ("true".equals(isignorehostname))) {
			hostnameVerifier = ignoreHostnameVerifier;
		}
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sc,
				new String[] { "TLSv1", "TLSv1.1", "TLSv1.2","SSLv3" }, null, hostnameVerifier);
		return sslsf;
	}

	private static HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	public static SSLContext initSSlContext(Config cfg)
			throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("TLSv1");
		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	public static SSLContext initSSlContext(String keypath, String keyInstance, String keypss)
			throws IOException, KeyStoreException {
		KeyStore ks = KeyStore.getInstance(keyInstance);
		FileInputStream inputstream = new FileInputStream(keypath);
		try {
			ks.load(inputstream, keypss.toCharArray());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} finally {
			try {
				inputstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(), e);
			}
		}
		SSLContext sc = null;
		try {
			sc = SSLContexts.custom().loadKeyMaterial(ks, keypss.toCharArray()).build();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}

		return sc;
	}
}

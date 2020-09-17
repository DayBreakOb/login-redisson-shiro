package com.mry.algorithm.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mry.config.PropertyUtil;
import com.mry.util.StringUtils;

public class CryptoUtils {

	private static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

	static PublicKey getPublicKeyByCert(String certificatePath) throws Exception {
		Certificate certificate = getCertificate(certificatePath);
		PublicKey publicKey = certificate.getPublicKey();
		return publicKey;
	}

	private static Certificate getCertificate(String certificatePath) throws Exception {
		InputStream in = null;
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance(PropertyUtil.getProperty(CryptoConfig.CERT_TYPE, "X.509"));
			in = new FileInputStream(certificatePath);
			Certificate certificate = certificateFactory.generateCertificate(in);
			return certificate;
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public static PublicKey getPublicKeyFromFile(String pubCerPath) {
		FileInputStream pubKeyStream = null;
		try {
			pubKeyStream = new FileInputStream(pubCerPath);
			byte[] reads = new byte[pubKeyStream.available()];
			pubKeyStream.read(reads);
			return getPublicKeyByStream(reads);
		} catch (FileNotFoundException e) {
			logger.error("公钥文件不存在:", e);
		} catch (IOException e) {
			logger.error("公钥文件读取失败:", e);
		} finally {
			if (pubKeyStream != null) {
				try {
					pubKeyStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static PublicKey getPublicKeyByStream(byte[] pubKeyBytes) {
		try (ByteArrayInputStream kbytes = new ByteArrayInputStream(pubKeyBytes)) {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance(PropertyUtil.getProperty(CryptoConfig.CERT_TYPE, "X.509"));

			Certificate certificate = certificateFactory.generateCertificate(kbytes);
			return certificate.getPublicKey();
		} catch (Throwable e) {
			// log.error("解析公钥内容失败:", e);
			e.printStackTrace();
		} finally {

		}
		return null;
	}

	public static PrivateKey getPrivateKeyFromFile(String prikeypath, String priKeyPass, String keystore) {
		InputStream priKeyStream = null;
		try {
			priKeyStream = new FileInputStream(prikeypath);
			byte[] reads = new byte[priKeyStream.available()];
			priKeyStream.read(reads);
			return getPrivateKeyByStream(reads, priKeyPass, keystore);
		} catch (Throwable e) {
			// log.error("解析文件，读取私钥失败:", e);
			e.printStackTrace();
			// throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (priKeyStream != null) {
				try {
					priKeyStream.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	private static PrivateKey getPrivateKeyByStream(byte[] prikeyBytes, String priKeyPass, String keystore) {
		// { "JCEKS", "JKS", "PKCS12", "PKCS11", "DKS", "BKS" };
		try {
			if (StringUtils.isEmpty(keystore)) {
				keystore = KeyStore.getDefaultType();
			}
			KeyStore ks = KeyStore.getInstance(keystore);
			char[] charPriKeyPass = priKeyPass.toCharArray();
			ks.load(new ByteArrayInputStream(prikeyBytes), charPriKeyPass);
			Enumeration<String> aliasEnum = ks.aliases();
			String keyAlias = null;
			if (aliasEnum.hasMoreElements()) {
				keyAlias = (String) aliasEnum.nextElement();
			}
			return (PrivateKey) ks.getKey(keyAlias, charPriKeyPass);
		} catch (IOException e) {
			// 加密失败
			// log.error("解析文件，读取私钥失败:", e);
			throw new RuntimeException("解析文件，读取私钥失败,加密失败", e);
		} catch (KeyStoreException e) {
			// log.error("私钥存储异常:", e);
			e.printStackTrace();
			throw new RuntimeException("私钥存储异常", e);
		} catch (NoSuchAlgorithmException e) {
			// log.error("不存在的解密算法:", e);
			throw new RuntimeException("不存在的解密算法", e);
		} catch (CertificateException e) {
			// log.error("证书异常:", e);
			throw new RuntimeException("证书异常", e);
		} catch (UnrecoverableKeyException e) {
			// log.error("不可恢复的秘钥异常", e);
			throw new RuntimeException("不可恢复的秘钥异常", e);
		}
	}

	public static byte[] crypto(boolean ispub, String filepath, byte[] bytes, String prikeypwd, boolean isen,
			String keystore) throws Exception {
		byte[] result = null;
		if (ispub) {
			PublicKey pk = getPublicKeyFromFile(filepath);
			Cipher cipher = Cipher.getInstance(pk.getAlgorithm());
			if (isen) {
				cipher.init(Cipher.ENCRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.ENCRYPT_MODE, bytes, keystore);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.DECRYPT_MODE, bytes, keystore);
			}
		} else {
			PrivateKey pk = getPrivateKeyFromFile(filepath, prikeypwd, keystore);
			Cipher cipher = Cipher.getInstance(pk.getAlgorithm());
			if (isen) {
				cipher.init(Cipher.ENCRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.ENCRYPT_MODE, bytes, keystore);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.DECRYPT_MODE, bytes, keystore);
			}
		}
		return result;
	}

	private static byte[] cryptoMessage(Cipher cipher, int mode, byte[] bytes, String keystore) {
		ByteArrayOutputStream out = null;
		int blocksize = Integer
				.valueOf(PropertyUtil.getProperty(CryptoConfig.ENCRYPT_KEYSIZE, "" + cipher.getBlockSize() + ""));
		try {
			out = new ByteArrayOutputStream();
			int inputLen = bytes.length;
			byte[] cache = null;
			int offset = 0;
			int i = 0;
			while (inputLen - offset > 0) {
				if (inputLen - offset > blocksize) {
					cache = cipher.doFinal(bytes, offset, blocksize);
				} else {
					cache = cipher.doFinal(bytes, offset, inputLen - offset);
				}
				out.write(cache, 0, cache.length);
				i++;
				offset = i * blocksize;
			}
			byte[] decryptData = out.toByteArray();
			return decryptData;
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * use the str to the 16 hex --byte[]
	 */
	public static byte[] hex2Bytes(String source) {
		byte[] sourceBytes = new byte[source.length() / 2];
		for (int i = 0; i < sourceBytes.length; i++) {
			sourceBytes[i] = (byte) Integer.parseInt(source.substring(i * 2, i * 2 + 2), 16);
		}
		return sourceBytes;
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String bytes2Hex(byte[] bytes) {
		int len = bytes.length;
		char[] hexChars = new char[len * 2];
		for (int i = 0; i < len; i++) {
			int v = bytes[i] & 0xFF;
			hexChars[i * 2] = HEX_ARRAY[v >>> 4];
			hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0f];
		}
		return new String(hexChars);
	}

	public static byte[] base642Byte(String base64str) {
		return Base64.decodeBase64(base64str);
	}

	public static String bytes2Base64(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}

	public static byte[] getBytesUtf8(String str) {

		return str.getBytes(Charset.defaultCharset());
	}

	public static String bytes2Utf8(byte[] bytes) {
		return new String(bytes, Charset.defaultCharset());
	}
	
	
	public static void main(String[] args) {
		String sdds = "dsadjoidkmasldjaskldsa;erkqwpod,as;d";
		String str=bytes2Hex(sdds.getBytes());
		byte[] bytes = hex2Bytes(str);
		String str1 = new String(bytes);
		System.out.println(str1+"___________________");
	}

}

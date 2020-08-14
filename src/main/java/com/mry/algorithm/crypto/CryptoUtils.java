package com.mry.algorithm.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mry.config.PropertyUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoUtils {

	private static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

	/**
	 * 根据证书获得公钥
	 * 
	 * @param certificatePath 证书存储路径
	 * @param keystore        配置服务名称
	 * @return
	 * @throws Exception
	 */
	static PublicKey getPublicKey(String certificatePath, String keystore) throws Exception {
		Certificate certificate = getCertificate(certificatePath, keystore);
		PublicKey publicKey = certificate.getPublicKey();
		return publicKey;
	}

	private static Certificate getCertificate(String certificatePath, String keystore) throws Exception {
		InputStream in = null;
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance(PropertyUtil.getProperty(CryptoConfig.CERT_TYPE, "X.509"));
			in = new FileInputStream(certificatePath);
			Certificate certificate = certificateFactory.generateCertificate(in);
			return certificate;
		} catch (Exception e) {
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

	/**
	 * 根据Cer文件读取公钥
	 * 
	 * @param pubCerPath
	 * @return
	 */
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

	/**
	 * 根据公钥Cer文本串读取公钥
	 * 
	 * @param pubKeyText
	 * @return
	 */
	public static PublicKey getPublicKeyByStream(byte[] pubKeyBytes) {
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance(PropertyUtil.getProperty(CryptoConfig.CERT_TYPE, "X.509"));
			Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(pubKeyBytes));
			return certificate.getPublicKey();
		} catch (Exception e) {
			// log.error("解析公钥内容失败:", e);
		}
		return null;
	}

	/**
	 * 
	 * @param prikeypath 私匙路径
	 * @param priKeyPass 私匙密码
	 * @param keystore   调用服务名称
	 * @return
	 */
	public static PrivateKey getPrivateKeyFromFile(String prikeypath, String priKeyPass, String keystore) {
		InputStream priKeyStream = null;
		try {
			priKeyStream = new FileInputStream(prikeypath);
			byte[] reads = new byte[priKeyStream.available()];
			priKeyStream.read(reads);
			return getPrivateKeyByStream(reads, priKeyPass, keystore);
		} catch (Exception e) {
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
		try {

			switch (keystore) {
			case "RSA":
				try {
					PKCS8EncodedKeySpec pkcs8encode = new PKCS8EncodedKeySpec(prikeyBytes);
					KeyFactory kf = KeyFactory.getInstance(keystore);
					PrivateKey pk = kf.generatePrivate(pkcs8encode);
					return pk;
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			default:
				KeyStore ks = KeyStore.getInstance(keystore);
				char[] charPriKeyPass = priKeyPass.toCharArray();
				ks.load(new ByteArrayInputStream(prikeyBytes), charPriKeyPass);
				Enumeration<String> aliasEnum = ks.aliases();
				String keyAlias = null;
				if (aliasEnum.hasMoreElements()) {
					keyAlias = (String) aliasEnum.nextElement();
				}
				return (PrivateKey) ks.getKey(keyAlias, charPriKeyPass);
			}
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
		return null;
	}

	public static byte[] crypto(boolean ispub, String filepath, String source, String prikeypwd, boolean isen,
			String keystore) throws Exception {
		byte[] result = null;
		if (ispub) {
			PublicKey pk = getPublicKey(filepath, keystore);
			Cipher cipher = Cipher.getInstance(pk.getAlgorithm());
			if (isen) {
				cipher.init(Cipher.ENCRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.ENCRYPT_MODE, source, keystore);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.DECRYPT_MODE, source, keystore);
			}
		} else if (!ispub) {
			PrivateKey pk = getPrivateKeyFromFile(filepath, prikeypwd, keystore);
			Cipher cipher = Cipher.getInstance(pk.getAlgorithm());
			if (isen) {
				cipher.init(Cipher.ENCRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.ENCRYPT_MODE, source, keystore);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, pk);
				result = cryptoMessage(cipher, Cipher.DECRYPT_MODE, source, keystore);
			}
		}
		return result;
	}

	private static byte[] cryptoMessage(Cipher cipher, int mode, String source, String keystore) {
		ByteArrayOutputStream out = null;
		int blocksize = 0;
		boolean isen = true;
		if (mode == Cipher.ENCRYPT_MODE) {
			blocksize = Integer.valueOf(PropertyUtil.getProperty(CryptoConfig.ENCRYPT_KEYSIZE, "128"));
		} else {
			isen = false;
			blocksize = Integer.valueOf(PropertyUtil.getProperty(CryptoConfig.DECRYPT_KEYSIZE, "128"));
		}
		byte[] sor = null;
		try {
			if (isen) {
				sor = source.getBytes("utf-8");
			} else {
				sor = hex2Bytes(source);
			}
			out = new ByteArrayOutputStream();
			int inputLen = sor.length;
			byte[] cache = null;
			int offset = 0;
			int i = 0;
			while (inputLen - offset > 0) {
				if (inputLen - offset > blocksize) {
					cache = cipher.doFinal(sor, offset, blocksize);
				} else {
					cache = cipher.doFinal(sor, offset, inputLen - offset);
				}
				out.write(cache, 0, cache.length);
				i++;
				offset = i * blocksize;
			}
			byte[] decryptData = out.toByteArray();
			return decryptData;
		} catch (Exception e) {
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
	private static byte[] hex2Bytes(String source) {
		byte[] sourceBytes = new byte[source.length() / 2];
		for (int i = 0; i < sourceBytes.length; i++) {
			sourceBytes[i] = (byte) Integer.parseInt(source.substring(i * 2, i * 2 + 2), 16);
		}
		return sourceBytes;
	}

}

package com.mry.algorithm.crypto.process.impl;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import com.mry.algorithm.crypto.RsaSignUtil;
import com.mry.config.BaseConfig;

public class RsaProcess extends AbstractProcess {

	@Override
	public byte[] decode(ByteBuffer byteBuffer) throws Exception {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public byte[] encode(ByteBuffer byteBuffer) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static String decryByPrivateKey(String keyPath, String data) {
		try {
			byte[] bytes = data.getBytes();
			byte[] datas = Base64.decodeBase64(bytes);
			byte[] result = initCipher(keyPath, datas);
			return new String(result, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static String decryByPrivateKey1(String keyPath, String data) {
		byte[] datas = hex2Bytes(data);
		byte[] bytes = initCipher(keyPath, datas);
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private static byte[] initCipher(String keypath, byte[] bytes) {
		try {
			String key = RsaSignUtil.loadPrivateKeyByFile(keypath);
			RSAPrivateKey keystore = RsaSignUtil.loadPrivateKeyByStr(key);
			Cipher cipher = Cipher.getInstance(keystore.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, keystore);
			byte[] datas1 = cipher.doFinal(bytes);
			return datas1;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String encryByPublicKey1(String keyPath, String data) {
		try {
			String key = RsaSignUtil.loadPublicKeyByFile(keyPath);
			RSAPublicKey keystore = RsaSignUtil.loadPublicKeyByStr(key);
			Cipher cipher = Cipher.getInstance(keystore.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, keystore);
			byte[] datas = cipher.doFinal(data.getBytes());
			return byte2Hex(datas);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String key = "dsaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		String enk = RsaProcess.encryByPublicKey1(BaseConfig.restRsapubFilePath, key);
		String dek = RsaProcess.decryByPrivateKey1(BaseConfig.loginRsaPriFilePath, enk);
		System.out.println("dek==================" + dek);
	}

}

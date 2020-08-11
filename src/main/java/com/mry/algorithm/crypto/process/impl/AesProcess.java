package com.mry.algorithm.crypto.process.impl;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.mry.util.StringUtils;



public class AesProcess extends AbstractProcess {

	private static AesProcess aesProcess;

	public static AesProcess getInstance() {
		if (aesProcess == null) {
			synchronized (aesProcess) {
				if (aesProcess == null) {
					aesProcess = new AesProcess();
				}
			}
		}
		return aesProcess;
	}

	private AesProcess() {
	}

	

	/**
	 * aes加密-128位
	 * 
	 */
	
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
	
	
	
	

	// ==Aes加解密==================================================================
	/**
	 * aes解密-128位
	 */
	public static String AesDecrypt(String encryptContent, String password) {
		if (StringUtils.isEmpty(password) || password.length() != 16) {
			throw new RuntimeException("密钥长度为16位");
		}
		try {
			String key = password;
			String iv = "1234567812345678";
			//byte[] encrypted1 = hex2Bytes(encryptContent);
			byte[] encrypted1 = Base64DecodeTobytes(encryptContent);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			byte[] original = cipher.doFinal(encrypted1);
			return new String(original,"UTF-8").trim();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("aes解密发生错误", e);
		}
	}

	/**
	 * aes加密-128位
	 * 
	 */
	public static  String AesEncrypt(String content ,String key){
		if (StringUtils.isEmpty(key) || key.length() != 16) {
			throw new RuntimeException("密钥长度为16位");
		}
		try {
			String iv = key;
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			int blockSize = cipher.getBlockSize();
			byte[] dataBytes = content.getBytes("utf-8");
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}
			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);
			return byte2Hex(encrypted);

		} catch (Exception e) {
			throw new RuntimeException("aes加密发生错误", e);
		}
	}
	
	protected static  SecretKey getGenerateSecrekey() {
		KeyGenerator keyGenerator = null;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		keyGenerator.init(256);
		SecretKey secretkey = keyGenerator.generateKey();
		return secretkey;
	}

}

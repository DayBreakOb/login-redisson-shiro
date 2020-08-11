package com.mry.algorithm.crypto.process.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.mry.algorithm.crypto.process.CryptoProcess;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public abstract class AbstractProcess implements CryptoProcess{
	
	// ==Base64加解密==================================================================
	/**
	 * Base64加密
	 */
	public static String Base64Encode(String str) throws UnsupportedEncodingException {
		return new BASE64Encoder().encode(str.getBytes("UTF-8"));
	}

	/**
	 * 解密
	 */
	public static String Base64Decode(String str) throws UnsupportedEncodingException, IOException {
//		str = str.replaceAll(" ", "+");
		return new String(new BASE64Decoder().decodeBuffer(str), "UTF-8");
	}



	/**
	 * 解密
	 */
	public static byte[] Base64DecodeTobytes(String str) throws UnsupportedEncodingException, IOException {
//		str = str.replaceAll(" ", "+");
		return new BASE64Decoder().decodeBuffer(str);
	}

	/**
	 * 将byte[] 转换成字符串
	 */
	public static String byte2Hex(byte[] srcBytes) {
		StringBuilder hexRetSB = new StringBuilder();
		for (byte b : srcBytes) {
			String hexString = Integer.toHexString(0x00ff & b);
			hexRetSB.append(hexString.length() == 1 ? 0 : "").append(hexString);
		}
		return hexRetSB.toString();
	}

	/**
	 * 将16进制字符串转为转换成字符串
	 */
	public static byte[] hex2Bytes(String source) {
		byte[] sourceBytes = new byte[source.length() / 2];
		for (int i = 0; i < sourceBytes.length; i++) {
			sourceBytes[i] = (byte) Integer.parseInt(source.substring(i * 2, i * 2 + 2), 16);
		}
		return sourceBytes;
	}


}

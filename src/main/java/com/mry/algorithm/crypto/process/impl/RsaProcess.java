package com.mry.algorithm.crypto.process.impl;

import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;

import com.mry.algorithm.crypto.RsaSignUtil;

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
			String key = RsaSignUtil.loadPrivateKeyByFile(keyPath);
			RSAPrivateKey keystore = RsaSignUtil.loadPrivateKeyByStr(key);
			Cipher cipher = Cipher.getInstance(keystore.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, keystore);
			byte[] datas = cipher.doFinal(org.apache.commons.codec.binary.Base64.decodeBase64(data.getBytes()));
			return new String(datas,"UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

}

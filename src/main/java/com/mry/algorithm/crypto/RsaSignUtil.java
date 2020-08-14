package com.mry.algorithm.crypto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Decoder;

public class RsaSignUtil {

	/**
	 * 加密算法RSA
	 */
	public static final String KEY_ALGORITHM = "RSA";

	/**
	 * 签名算法
	 */
	public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

	/**
	 * @encryptStr 摘要
	 * @pfxPath pfx证书路径
	 * @priKeyPass 私钥
	 * @charset 编码方式 签名
	 */
	public static byte[] encryptByRSA(String encryptStr, String pfxPath, String systemName, String passwd)
			throws Exception {
		String privateKey = RsaSignUtil.loadPrivateKeyByFile(pfxPath);
		RSAPrivateKey prik = RsaSignUtil.loadPrivateKeyByStr(privateKey);
		return sign(encryptStr.toUpperCase().getBytes("UTF-8"), prik.getEncoded());
	}

	/**
	 * 从文件中加载私钥
	 *
	 * @param keyFileName 私钥文件名
	 * @return 是否成功
	 * @throws Exception
	 */
	public static String loadPrivateKeyByFile(String path) throws Exception {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String readLine = null;
			StringBuilder sb = new StringBuilder();
			while ((readLine = br.readLine()) != null) {
				if ((readLine.contains("BEGIN PRIVATE KEY") || readLine.contains("END PRIVATE KEY"))) {
					continue;
				}
				sb.append(readLine);
			}
			br.close();
			return sb.toString();
		} catch (IOException e) {
			throw new Exception("私钥数据读取错误");
		} catch (NullPointerException e) {
			throw new Exception("私钥输入流为空");
		}
	}

	public static RSAPrivateKey loadPrivateKeyByStr(String privateKeyStr) throws Exception {
		try {
			BASE64Decoder base64decoder = new BASE64Decoder();
			byte[] buffer = base64decoder.decodeBuffer(privateKeyStr);
			KeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			throw new Exception("私钥非法");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}
	}

	/**
	 *
	 * 用私钥对信息生成数字签名
	 * 
	 * @param data 已加密数据
	 * @return
	 * @throws Exception
	 */
	public static byte[] sign(byte[] data, byte[] keyBytes) throws Exception {
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateK);
		signature.update(data);
		return signature.sign();
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
    public static final Charset UTF_8 = Charset.forName("UTF-8");
	/**
	 * @encryptStr 摘要
	 * @signature 签名
	 * @pubCerPath 公钥路径 验签
	 */
	public static boolean verifySignature(String pubCerPath, String encryptStr, byte[] signature, String systemName)
			throws Exception {
		String str = RsaSignUtil.loadPublicKeyByFile(pubCerPath);
		PublicKey publicKey = RsaSignUtil.loadPublicKeyByStr(str);
		return verify(encryptStr.getBytes(UTF_8), signature,publicKey);
	}


	/**
	 * 校验数字签名
	 * 
	 * @param data     已加密数据
	 * @param keyBytes 公钥
	 * @param sign     数字签名
	 * @throws Exception
	 *
	 */
	public static boolean verify(byte[] data, byte[] keyBytes, String sign) throws Exception {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PublicKey publicK = keyFactory.generatePublic(keySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicK);
		signature.update(data);
		return signature.verify(hex2Bytes(sign));
	}
	
	/**
	 * 
	 * 验证签名 <br>
	 * 
	 * @param data c
	 * @param sign 签名值
	 * @param pubk 公钥
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @since 10.15
	 */
	public static boolean verify(byte[] data, byte[] sign, PublicKey pubk) throws NoSuchAlgorithmException,
	        InvalidKeyException, SignatureException {
	    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
	    signature.initVerify(pubk);
	    signature.update(data);
	    return signature.verify(sign);
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

	 /**
     * 从文件中输入流中加载公钥
     *
     * @param in
     *            公钥输入流
     * @throws Exception
     *             加载公钥时产生的异常
     */
    public static String loadPublicKeyByFile(String path) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if ((readLine.contains("BEGIN PUBLIC KEY") || readLine.contains("END PUBLIC KEY"))) {
                    continue;
                }
                sb.append(readLine);
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr
     *            公钥数据字符串
     * @throws Exception
     *             加载公钥时产生的异常
     */
    public static RSAPublicKey loadPublicKeyByStr(String publicKeyStr)
            throws Exception {
        try {
            Base64 base64 = new Base64();
            byte[] buffer = base64.decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }
}

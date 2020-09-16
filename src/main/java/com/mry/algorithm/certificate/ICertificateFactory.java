package com.mry.algorithm.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.apache.catalina.authenticator.DigestAuthenticator.DigestInfo;
import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Maps;
import com.sun.javafx.css.CalculatedValue;

import sun.misc.BASE64Encoder;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.AlgorithmId;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class ICertificateFactory {

	private static final String NEW_LINE = System.getProperty("line.separator");
	/** 证书摘要及签名算法组 */
	public static final String MSG_DIGEST_SIGN_ALGO = "SHA256withRSA";

	/** 在将java生成的证书导出到文件的时候，需要将下面两行信息对应的添加到证书内容的头部后尾部 */
	private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
	private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

	/** 在将java生成的私钥导出到文件的时候，需要将下面两行信息对应的添加到私钥内容的头部后尾部 */
	private static final String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
	private static final String END_RSA_PRIVATE_KEY = "-----END PRIVATE KEY-----";
	private static final String X509 = "X509";
	private static final String RSA_ALGORITHM = "";
	private String sigAlg = "";

	private PublicKey publicKey;

	private PrivateKey privateKey;

	/**
	 * 创建根证书， 并保存根证书到指定路径的文件中， crt和key分开存储文件。
	 * 创建SSL根证书的逻辑，很重要，此函数调用频次不高，创建根证书，也就是自签名证书。
	 *
	 * @param algorithm      私钥安全算法，e.g. RSA
	 * @param keySize        私钥长度，越长越安全，RSA要求不能小于512， e.g. 2048
	 * @param digestSignAlgo 信息摘要以及签名算法 e.g. SHA256withRSA
	 * @param subj           证书所有者信息描述，e.g. CN=iotp,OU=tkcloud,O=taikang,L=wuhan,S=hubei,C=CN
	 * @param validDays      证书有效期天数，e.g. 3650即10年
	 * @param rootCACrtPath  根证书所要存入的全路径，e.g. /opt/certs/iot/rootCA.crt
	 * @param rootCAKeyPath  根证书对应秘钥key所要存入的全路径，e.g. /opt/certs/iot/rootCA.key
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws SignatureException
	 * @throws UnrecoverableKeyException
	 * @return 私钥和证书对的map对象
	 */
	
	
	public static void main(String[] args) {
		ICertificateFactory.createRootCa("RSA", 2048, "SHA256withRSA", "CN=iotp,OU=tkcloud,O=taikang,L=wuhan,S=hubei,C=CN", 60L, "/u01/certificate/create/RootCa.crt", "/u01/certificate/create/rootCA.key");
	}
	
	public static HashMap<PrivateKey, X509Certificate> createRootCa(String algorithm, int keysize,
			String digestSignAlgorithm, String subj, Long validays, String rootCaPath, String rootCaPkPath) {

		CertAndKeyGen cak = null;

		try {
			cak = new CertAndKeyGen(algorithm, digestSignAlgorithm, null);
			cak.generate(keysize);
			cak.setRandom(new SecureRandom());
			X500Name subject = new X500Name(subj);

			PublicKey pubk = cak.getPublicKey();
			PrivateKey prik = cak.getPrivateKey();

			CertificateExtensions certext = new CertificateExtensions();

			certext.set("SubjectKeyIdentifier",
					new SubjectKeyIdentifierExtension((new KeyIdentifier(pubk)).getIdentifier()));
			certext.set("AuthorityKeyIdentifier",
					new AuthorityKeyIdentifierExtension(new KeyIdentifier(pubk), null, null));
			certext.set("BasicConstraints", new BasicConstraintsExtension(false, true, 0));

			X509Certificate certficate = cak.getSelfCertificate(subject, new Date(), validays * 24L * 60L * 60L,
					certext);
			HashMap<PrivateKey, X509Certificate> maps = Maps.newHashMap();
			maps.put(prik, certficate);
			exportCrt(certficate, rootCaPath);
			exportKey(prik, rootCaPkPath);

//            String rootPath = "E:\\2018\\IOT\\MQTT\\javassl\\jsseRoot.keystore";
//            String rootPfxPath = "E:\\2018\\IOT\\MQTT\\javassl\\jsseRoot.pfx";
//            /**
//             * 通过下面的指令，可以将keystore里面的内容转为DER格式的证书jsseRoot.cer
//             * keytool -export -alias rootCA -storepass abcdef -file jsseRoot.cer -keystore jsseRoot.keystore
//             *
//             * 通过下面的指令，可以将DER格式的证书转化为OPENSSL默认支持的PEM证书：
//             * openssl x509 -inform der -in jsseRoot.cer -out jsseRoot.pem
//             */
//            saveJks("rootCA", privateKey, "abcdef", new Certificate[]{certificate}, rootPath);
//
//            /**
//             * 通过下面的指令，可以获取证书的私钥
//             * openssl pkcs12 -in jsseRoot.pfx -nocerts -nodes -out jsseRoot.key
//             */
//            savePfx("rootCA", privateKey, "abcdef", new Certificate[]{certificate}, rootPfxPath);

			return maps;
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// TODO: handle finally clause
		}
		return null;

	}

	/**
	 * 将JAVA创建的证书内容导出到文件， 基于BASE64转码了。
	 *
	 *
	 * @param devCrt  设备证书对象
	 * @param crtPath 设备证书存储路径
	 */
	public static void exportCrt(Certificate devCrt, String crtPath) {
		BASE64Encoder base64Crt = new BASE64Encoder();
		FileOutputStream fosCrt = null;
		try {
			fosCrt = new FileOutputStream(new File(crtPath));
			String cont = BEGIN_CERTIFICATE + NEW_LINE;
			fosCrt.write(cont.getBytes());
			base64Crt.encodeBuffer(devCrt.getEncoded(), fosCrt);
			cont = END_CERTIFICATE;
			fosCrt.write(cont.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fosCrt != null) {
				try {
					fosCrt.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 导出私钥内容到文件中，以base64编码。 注意，java生成的私钥文件默认是PKCS#8的格式，加载的时候，要注意对应关系。
	 *
	 * @param key
	 * @param keyPath
	 */
	public static void exportKey(PrivateKey key, String keyPath) {
		BASE64Encoder base64Crt = new BASE64Encoder();
		FileOutputStream fosKey = null;
		try {
			fosKey = new FileOutputStream(new File(keyPath));
			String cont = BEGIN_RSA_PRIVATE_KEY + NEW_LINE;
			fosKey.write(cont.getBytes());
			base64Crt.encodeBuffer(key.getEncoded(), fosKey);
			cont = END_RSA_PRIVATE_KEY;
			fosKey.write(cont.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fosKey != null) {
				try {
					fosKey.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 创建X509的证书， 由ca证书完成签名。
	 *
	 * subject,issuer都遵循X500Principle规范， 即： X500Principal由可分辨名称表示，例如“CN = Duke，OU =
	 * JavaSoft，O = Sun Microsystems，C = US”。
	 *
	 * @param ca        根证书对象
	 * @param caKey     CA证书对应的私钥对象
	 * @param publicKey 待签发证书的公钥对象
	 * @param subj      证书拥有者的主题信息，签发者和主题拥有者名称都转写X500Principle规范，格式：CN=country,ST=state,L=Locality,OU=OrganizationUnit,O=Organization
	 * @param validDays 证书有效期天数
	 * @param sginAlgo  证书签名算法， e.g. SHA256withRSA
	 *
	 * @return cert 新创建得到的X509证书
	 */
	public static X509Certificate createUserCert(X509Certificate ca, PrivateKey caKey, PublicKey publicKey, String subj,
			long validDays, String sginAlgo) {

		// 获取ca证书
		X509Certificate caCert = ca;

		X509CertInfo x509CertInfo = new X509CertInfo();

		try {
			// 设置证书的版本号
			x509CertInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));

			// 设置证书的序列号，基于当前时间计算
			x509CertInfo.set(X509CertInfo.SERIAL_NUMBER,
					new CertificateSerialNumber((int) (System.currentTimeMillis() / 1000L)));

			/**
			 * 下面这个设置算法ID的代码，是错误的，会导致证书验证失败，但是报错不是很明确。 若将生成的证书存为keystore，让后keytool转换 会出现异常。
			 * AlgorithmId algorithmId = new AlgorithmId(AlgorithmId.SHA256_oid);
			 */

			// 将代码配置成正确的样子（如下）：

			//AlgorithmId algorithmId = AlgorithmId.get(sginAlgo);
			//cert.sign(caPrivateKey, sginAlgo);
			AlgorithmId algorithmId = AlgorithmId.get(sginAlgo);
			x509CertInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithmId));

			// 设置证书的签发者信息
			X500Name issuer = new X500Name(caCert.getIssuerX500Principal().toString());
			x509CertInfo.set(X509CertInfo.ISSUER, issuer);

			// 设置证书的拥有者信息
			X500Name subject = new X500Name(subj);
			x509CertInfo.set(X509CertInfo.SUBJECT, subject);

			// 设置证书的公钥
			x509CertInfo.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));

			// 设置证书有效期
			Date beginDate = new Date();
			Date endDate = new Date(beginDate.getTime() + validDays * 24 * 60 * 60 * 1000L);
			CertificateValidity cv = new CertificateValidity(beginDate, endDate);
			x509CertInfo.set(X509CertInfo.VALIDITY, cv);

			CertificateExtensions exts = new CertificateExtensions();

			/*
			 * 以上是证书的基本信息 如果要添加用户扩展信息 则比较麻烦 首先要确定version必须是v3否则不行 然后按照以下步骤
			 *
			 */
			exts.set("SubjectKeyIdentifier",
					new SubjectKeyIdentifierExtension((new KeyIdentifier(publicKey)).getIdentifier()));
			exts.set("AuthorityKeyIdentifier",
					new AuthorityKeyIdentifierExtension(new KeyIdentifier(ca.getPublicKey()), null, null));
			exts.set("BasicConstraints", new BasicConstraintsExtension(false, false, 0));
			x509CertInfo.set("extensions", exts);

		} catch (CertificateException cee) {
			cee.printStackTrace();
		} catch (IOException eio) {
			eio.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// 获取CA私钥
		PrivateKey caPrivateKey = caKey;
		// 用CA的私钥给当前证书进行签名，获取最终的下游证书（证书链的下一节点）
		X509CertImpl cert = new X509CertImpl(x509CertInfo);
		try {
			cert.sign(caPrivateKey, sginAlgo);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e3) {
			e3.printStackTrace();
		}
		return cert;
	}

	public X509Certificate getSelfCertificate(X500Name var1, Date var2, long var3, CertificateExtensions var5)
			throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException,
			NoSuchProviderException {
		try {
			Date var7 = new Date();
			var7.setTime(var2.getTime() + var3 * 1000L);
			CertificateValidity var8 = new CertificateValidity(var2, var7);
			X509CertInfo var9 = new X509CertInfo();
			var9.set("version", new CertificateVersion(2));
			var9.set("serialNumber", new CertificateSerialNumber((new Random()).nextInt() & 2147483647));
			AlgorithmId var10 = AlgorithmId.get(this.sigAlg);
			var9.set("algorithmID", new CertificateAlgorithmId(var10));
			var9.set("subject", var1);
			var9.set("key", new CertificateX509Key(this.publicKey));
			var9.set("validity", var8);
			var9.set("issuer", var1);
			if (var5 != null) {
				var9.set("extensions", var5);
			}

			X509CertImpl var6 = new X509CertImpl(var9);
			var6.sign(this.privateKey, this.sigAlg);
			return var6;
		} catch (IOException var11) {
			throw new CertificateEncodingException("getSelfCert: " + var11.getMessage());
		}
	}

	/**
	 * 得到私钥, 记得这个文件是类似PEM格式的问题，需要将文件头部的----BEGIN和尾部的----END信息去掉
	 *
	 * @param privateKey 密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static RSAPrivateKey getPrivateKey(String privateKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 通过PKCS#8编码的Key指令获得私钥对象
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
		if (privateKey.startsWith(BEGIN_RSA_PRIVATE_KEY)) {
			int bidx = BEGIN_RSA_PRIVATE_KEY.length();
			privateKey = privateKey.substring(bidx);
		}
		if (privateKey.endsWith(END_RSA_PRIVATE_KEY)) {
			int eidx = privateKey.indexOf(END_RSA_PRIVATE_KEY);
			privateKey = privateKey.substring(0, eidx);
		}
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
		RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
		return key;
	}

	/**
	 * 从经过base64转化后的证书文件中构建证书对象,是一个标准的X509证书，
	 *
	 * 且非常重要的是，文件头部含有-----BEGIN CERTIFICATE----- 文件的尾部含有 -----END CERTIFICATE-----
	 * 若没有上述头和尾部，证书验证的时候会报certificate_unknown。
	 *
	 * @param crtFile 经过base64处理的证书文件
	 * @return X509的证书
	 */
	public static X509Certificate getCertficate(File crtFile) {
		// 这个客户端证书，是用来发送给服务端的，准备做双向验证用的。
		CertificateFactory cf;
		X509Certificate cert = null;
		FileInputStream crtIn = null;
		try {
			cf = CertificateFactory.getInstance(X509);
			crtIn = new FileInputStream(crtFile);
			cert = (X509Certificate) cf.generateCertificate(crtIn);
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (crtIn != null) {
				try {
					crtIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return cert;
	}
}

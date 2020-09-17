package com.mry.algorithm.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Maps;

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
	 * @param digestSignAlgo 信息摘要以及签名算法 e.g.
	 *                       SHA256withRSA//SHA1withRSA//SHA256withECDSA
	 *                       ---------------------------------------------- EC.
	 *                       P-192，也称为secp192r1和prime192v1
	 *                       P-256，也称为secp256r1和prime256v1 P-224也称为 secp224r1
	 *                       P-384也称为 secp384r1 P-521也称为 secp521r1 secp256k1 （比特币曲线）
	 *                       ECDSA Ecdsa_Sha1（"ecdsaWithSHA1"）
	 *                       Ecdsa_Sha224（"ecdsaWithSHA224"）
	 *                       Ecdsa_Sha256（"ecdsaWithSHA256"）
	 *                       Ecdsa_Sha384（"ecdsaWithSHA384"）
	 *                       Ecdsa_Sha512（"ecdsaWithSHA512"）
	 * @param subj           证书所有者信息描述，e.g.
	 *                       CN=iotp,OU=tkcloud,O=taikang,L=wuhan,S=hubei,C=CN
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
		int tempname = (int)Math.floor(Math.random() * 10000) ;
		String subj = "CN=china,OU=huyun,O=yanghu,L=BEIJING,S=BEIJING,C=CN";
		String signAlgo = "SHA256withECDSA";
		HashMap<CertAndKeyGen, X509Certificate> cc = ICertificateFactory.createRootCa("EC", 256, signAlgo, subj, 60L,
				"/u01/certificate/create/" + tempname+ "RootCa.crt",
				"/u01/certificate/create/" + tempname+ "rootCA.key");
		Iterator<CertAndKeyGen> cca = cc.keySet().iterator();
		while (cca.hasNext()) {
			CertAndKeyGen key = cca.next();
			String[] xxa = { "Dsad", "Uiui", "Popop" };
			for (int i = 0; i < 3; i++) {
				String subj1 = "CN=china,OU=huyun,O=" + xxa[i] + ",L=BEIJING,S=BEIJING,C=CN";
				ICertificateFactory.createUserCert(cc.get(key), key.getPrivateKey(), key.getPublicKey(), subj1, 60L,
						signAlgo,tempname);
			}
		}
	}

	public static HashMap<CertAndKeyGen, X509Certificate> createRootCa(String algorithm, int keysize,
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
			HashMap<CertAndKeyGen, X509Certificate> maps = Maps.newHashMap();
			maps.put(cak, certficate);
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
			long validDays, String sginAlgo,int tempname) {

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

			// AlgorithmId algorithmId = AlgorithmId.get(sginAlgo);
			// cert.sign(caPrivateKey, sginAlgo);
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
		exportCrt(cert, "/u01/certificate/create/usercert" + tempname+ ".crt");
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

	/*
	 * 
	 * X.509是常见通用的证书格式。所有的证书都符合为Public Key Infrastructure (PKI) 制定的 ITU-T X509
	 * 国际标准。X.509是国际电信联盟-电信（ITU-T）部分标准和国际标准化组织（ISO）的证书格式标准。作为ITU-ISO目录服务系列标准的一部分，X.
	 * 509是定义了公钥证书结构的基本标准。1988年首次发布，1993年和1996年两次修订。当前使用的版本是X.509
	 * V3，它加入了扩展字段支持，这极大地增进了证书的灵活性。X.509
	 * V3证书包括一组按预定义顺序排列的强制字段，还有可选扩展字段，即使在强制字段中，X.509证书也允许很大的灵活性，因为它为大多数字段提供了多种编码方案.
	 * 
	 * PKCS#7 常用的后缀是： .P7B .P7C .SPC PKCS#12 常用的后缀有： .P12 .PFX X.509 DER
	 * 编码(ASCII)的后缀是： .DER .CER .CRT X.509 PAM 编码(Base64)的后缀是： .PEM .CER .CRT
	 * .cer/.crt是用于存放证书，它是2进制形式存放的，不含私钥。 .pem跟crt/cer的区别是它以Ascii来表示。
	 * pfx/p12用于存放个人证书/私钥，他通常包含保护密码，2进制方式 p10是证书请求 p7r是CA对证书请求的回复，只用于导入
	 * p7b以树状展示证书链(certificate chain)，同时也支持单个证书，不含私钥。
	 * 
	 * 一 用openssl创建CA证书的RSA密钥(PEM格式)： openssl genrsa -des3 -out ca.key 1024
	 * 
	 * 二用openssl创建CA证书(PEM格式,假如有效期为一年)： openssl req -new -x509 -days 365 -key ca.key
	 * -out ca.crt -config openssl.cnf
	 * openssl是可以生成DER格式的CA证书的，最好用IE将PEM格式的CA证书转换成DER格式的CA证书。
	 * 
	 * 三 x509到pfx pkcs12 -export –in keys/client1.crt -inkey keys/client1.key -out
	 * keys/client1.pfx
	 * 
	 * 四 PEM格式的ca.key转换为Microsoft可以识别的pvk格式。 pvk -in ca.key -out ca.pvk -nocrypt
	 * -topvk 五 PKCS#12 到 PEM 的转换 openssl pkcs12 -nocerts -nodes -in cert.p12 -out
	 * private.pem 验证 openssl pkcs12 -clcerts -nokeys -in cert.p12 -out cert.pem 六 从
	 * PFX 格式文件中提取私钥格式文件 (.key) openssl pkcs12 -in mycert.pfx -nocerts -nodes -out
	 * mycert.key 七 转换 pem 到到 spc openssl crl2pkcs7 -nocrl -certfile venus.pem
	 * -outform DER -out venus.spc 用 -outform -inform 指定 DER 还是 PAM 格式。例如： openssl
	 * x509 -in Cert.pem -inform PEM -out cert.der -outform DER 八 PEM 到 PKCS#12 的转换，
	 * openssl pkcs12 -export -in Cert.pem -out Cert.p12 -inkey key.pem
	 * 
	 * 
	 * 
	 * 密钥库文件格式【Keystore】
	 * 
	 * 格式 : JKS 扩展名 : .jks/.ks 描述 : 【Java Keystore】密钥库的Java实现版本，provider为SUN 特点 :
	 * 密钥库和私钥用不同的密码进行保护
	 * 
	 * 格式 : JCEKS 扩展名 : .jce 描述 : 【JCE Keystore】密钥库的JCE实现版本，provider为SUN JCE 特点 :
	 * 相对于JKS安全级别更高，保护Keystore私钥时采用TripleDES
	 * 
	 * 格式 : PKCS12 扩展名 : .p12/.pfx 描述 : 【PKCS #12】个人信息交换语法标准 特点 : 1、包含私钥、公钥及其证书
	 * 2、密钥库和私钥用相同密码进行保护
	 * 
	 * 格式 : BKS 扩展名 : .bks 描述 : Bouncycastle Keystore】密钥库的BC实现版本，provider为BC 特点 :
	 * 基于JCE实现
	 * 
	 * 格式 : UBER 扩展名 : .ubr 描述 : 【Bouncycastle UBER
	 * Keystore】密钥库的BC更安全实现版本，provider为BC
	 * 
	 * 
	 * 证书文件格式【Certificate】 格式 : DER 扩展名 : .cer/.crt/.rsa
	 * 
	 * 描述 : 【ASN .1 DER】用于存放证书 特点 : 不含私钥、二进制
	 * 
	 * 格式 : PKCS7 扩展名 : .p7b/.p7r 描述 : 【PKCS #7】加密信息语法标准
	 * 
	 * 特点 : 1、p7b以树状展示证书链，不含私钥 2、p7r为CA对证书请求签名的回复，只能用于导入
	 * 
	 * 格式 : CMS 扩展名 : .p7c/.p7m/.p7s 描述 : 【Cryptographic Message Syntax】 特点 :
	 * 1、p7c只保存证书 2、p7m：signature with enveloped data 3、p7s：时间戳签名文件
	 * 
	 * 格式 : PEM 扩展名 : .pem 描述 : 【Printable Encoded Message】 特点 :
	 * 1、该编码格式在RFC1421中定义，其实PEM是【Privacy-Enhanced Mail】的简写，但他也同样广泛运用于密钥管理 2、ASCII文件
	 * 3、一般基于base 64编码
	 * 
	 * 格式 : PKCS10 扩展名 : .p10/.csr 描述 : 【PKCS #10】公钥加密标准【Certificate Signing
	 * Request】 特点 : 1、证书签名请求文件 2、ASCII文件 3、CA签名后以p7r文件回复
	 * 
	 * 格式 : SPC 扩展名 : .pvk/.spc 描述 : 【Software Publishing Certificate】 特点 :
	 * 微软公司特有的双证书文件格式，经常用于代码签名，其中 1、pvk用于保存私钥 2、spc用于保存公钥
	 */
}

/*
 * Copyright (c) 2018 ellipticSecure - https://ellipticsecure.com
 *
 * All rights reserved.
 *
 * You may only use this code under the terms of the ellipticSecure software license.
 *
 */
package com.mry.algorithm.certificate;

import sun.security.ec.SunEC;
import sun.security.jca.ProviderList;
import sun.security.jca.Providers;
import sun.security.pkcs10.PKCS10;
import sun.security.pkcs11.SunPKCS11;
import sun.security.provider.Sun;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Demonstrate the use of elliptic curve cryptography using the eHSM hardware
 * security module.
 *
 * @author Kobus Grobler
 */
public class Example1 {
	private static String configName = "src/main/resources/ehsm.cfg";

	private static void eccDemo(String p, KeyStore ks, String algo, String curve) throws Exception {
		System.out.println("Testing curve " + curve);

		String alias = "example1_test";
		// Delete previous test entry.
		ks.deleteEntry(alias);

		// Generate an EC key pair
		// Notice the use of the provider to force generation on the eHSM instead of
		// software.
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", p);
		ECGenParameterSpec kpgparams = new ECGenParameterSpec(curve);
		keyPairGenerator.initialize(kpgparams);
		System.out.println("Generating key pair.");
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		// Create a selfsigned certificate to store with the public key. This is a java
		// keystore requirement.
		// the certificate is signed using the eHSM
		System.out.println("Creating self signed certificate.");
		X509Certificate cert = generateCert(keyPair, 1, "SHA256withECDSA", "CN=example, L=Town, C=ZZ", null);
		ks.setKeyEntry(alias, keyPair.getPrivate(), null, new X509Certificate[] { cert });
		ICertificateFactory.exportCrt(cert, "/u01/certificate/create/test_bks.pem");
		ICertificateFactory.exportKey(keyPair.getPrivate(), "/u01/certificate/create/test_key.pem");
		// sign some data
		Signature sig = Signature.getInstance(algo, p);
		sig.initSign(keyPair.getPrivate());
		byte[] data = "test".getBytes();
		sig.update(data);
		byte[] s = sig.sign();
		System.out.println("Signed with hardware key.");

		// verify the signature
		sig.initVerify(keyPair.getPublic());
		sig.update(data);
		if (!sig.verify(s)) {
			throw new Exception("signature did not verify");
		}
		System.out.println("Verified with hardware key.");
	}

	private static X509Certificate generateCert(KeyPair pair, int days, String algorithm, String dn, String provider)
			throws Exception {
		X500Name issuerName = new X500Name(dn);

		BigInteger serial = BigInteger.valueOf(new SecureRandom().nextInt()).abs();
		Calendar calendar = Calendar.getInstance();
		Date startDate = new Date();
		calendar.setTime(startDate);
		calendar.add(Calendar.DAY_OF_YEAR, days);

		Date endDate = calendar.getTime();
		X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuerName, serial, startDate, endDate,
				issuerName, pair.getPublic());
		builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

		KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment
				| KeyUsage.dataEncipherment | KeyUsage.cRLSign);
		builder.addExtension(Extension.keyUsage, false, usage);

		ASN1EncodableVector purposes = new ASN1EncodableVector();
		purposes.add(KeyPurposeId.id_kp_serverAuth);
		purposes.add(KeyPurposeId.id_kp_clientAuth);
		purposes.add(KeyPurposeId.anyExtendedKeyUsage);
		builder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));
		ContentSigner contentSigner = new JcaContentSignerBuilder(algorithm).build(pair.getPrivate());

		JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
		if (provider != null)
			converter.setProvider(provider);
		X509Certificate cert = converter.getCertificate(builder.build(contentSigner));
		cert.checkValidity(new Date());
		cert.verify(pair.getPublic());
		return cert;
	}

	/*
	 * 
	 * KeyFactory: EC AlgorithmParameters: EC Signature: NONEwithECDSA Signature:
	 * SHA1withECDSA Signature: SHA256withECDSA Signature: SHA384withECDSA
	 * Signature: SHA512withECDSA KeyPairGenerator: EC KeyAgreement: ECDH
	 */

	public static void main(String[] args) throws Exception {
		// if (args.length < 1) {
		// System.out.println("usage: example1 <SU password>");
		// System.out.println("\tPlease provide the user (SU) password as the first
		// parameter");
		// return;
		// }

		args = new String[5];
		args[0] = "password";
		// Create PKCS11 Provider
		// SunPKCS11 p = new SunPKCS11(configName);

		// Login to the eHSM
		String[] types = { "JCEKS", "JKS", "PKCS12", "PKCS11", "DKS", "BKS" };
		// KeyStore ks = KeyStore.getInstance("JCEKS", p);
		// KeyStore ks = KeyStore.getInstance("JKS", p);
		// KeyStore ks = KeyStore.getInstance("PKCS12", p);
		// KeyStore ks = KeyStore.getInstance("PKCS11", p);
		// KeyStore ks = KeyStore.getInstance("DKS", p);
		// KeyStore ks = KeyStore.getInstance("BKS", p);
		String p = "BC";

		Security.addProvider(new BouncyCastleProvider());
		/*
		 * KeyStore ks = null; for (int i = 0; i < types.length; i++) { try {
		 * 
		 * System.out.println(types[i]); } catch (Exception e) { // TODO: handle
		 * exception e.printStackTrace(); continue; }
		 * 
		 * }
		 */
		KeyStore ks = KeyStore.getInstance("BKS", p);
		ks.load(null, args[0].toCharArray());

		//eccDemo(p, ks, "SHA256withECDSA", "secp256r1");
		eccDemo(p, ks, "SHA256withECDSA", "secp384r1");
	}

	/*
	 * 
	 * PKCS标准汇总 版本 名称 简介 PKCS #1 2.1 RSA密码编译标准（RSA Cryptography Standard）
	 * 定义了RSA的数理基础、公/私钥格式，以及加/解密、签/验章的流程。1.5版本曾经遭到攻击[1]。 PKCS #2 - 弃用
	 * 原本是用以规范RSA加密摘要的转换方式，现已被纳入PKCS#1之中。 PKCS #3 1.4 DH密钥协议标准（Diffie-Hellman key
	 * agreement Standard） 规范以DH密钥协议为基础的密钥协议标准。其功能，可以让两方透过金议协议，拟定一把会议密钥(Session
	 * key)。 PKCS #4 - 弃用 原本用以规范转换RSA密钥的流程。已被纳入PKCS#1之中。 PKCS #5 2.0
	 * 密码基植加密标准（Password-based Encryption Standard） 参见RFC 2898与PBKDF2。 PKCS #6 1.5
	 * 证书扩展语法标准（Extended-Certificate Syntax Standard） 将原本X.509的证书格式标准加以扩展。 PKCS #7
	 * 1.5 密码消息语法标准（Cryptographic Message Syntax Standard） 参见RFC
	 * 2315。规范了以公开密钥基础设施（PKI）所产生之签名/密文之格式。其目的一样是为了拓展数字证书的应用。其中，包含了S/MIME与CMS。 PKCS
	 * #8 1.2 私钥消息表示标准（Private-Key Information Syntax Standard）. Apache读取证书私钥的标准。
	 * PKCS #9 2.0 选择属性格式（Selected Attribute Types） 定义PKCS#6、7、8、10的选择属性格式。 PKCS #10
	 * 1.7 证书申请标准（Certification Request Standard） 参见RFC
	 * 2986。规范了向证书中心申请证书之CSR（certificate signing request）的格式。 PKCS #11 2.20
	 * 密码设备标准接口（Cryptographic Token Interface (Cryptoki)） 定义了密码设备的应用程序接口（API）之规格。
	 * PKCS #12 1.0 个人消息交换标准（Personal Information Exchange Syntax Standard）
	 * 定义了包含私钥与公钥证书（public key
	 * certificate）的文件格式。私钥采密码(password)保护。常见的PFX就履行了PKCS#12。 PKCS #13 –
	 * 椭圆曲线密码学标准（Elliptic curve cryptography Standard）
	 * 制定中。规范以椭圆曲线密码学为基础所发展之密码技术应用。椭圆曲线密码学是新的密码学技术，其强度与效率皆比现行以指数运算为基础之密码学算法来的优秀。然而，
	 * 该算法的应用尚不普及。 PKCS #14 – 拟随机数产生器标准（Pseudo-random Number Generation）
	 * 制定中。规范拟随机数产生器的使用与设计。 PKCS #15 1.1 密码设备消息格式标准（Cryptographic Token Information
	 * Format Standard） 定义了密码设备内部数据的组织结构。
	 */
}

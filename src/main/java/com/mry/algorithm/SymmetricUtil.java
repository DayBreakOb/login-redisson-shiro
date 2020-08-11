package com.mry.algorithm;

import java.nio.ByteBuffer;

import com.mry.algorithm.crypto.process.CryptoProcess;
import com.mry.algorithm.crypto.process.CryptoProcessBuilder;

public class SymmetricUtil {

	public static byte[] decrypt(String algorithm, String algorithm_version, ByteBuffer byteBuffer) throws Exception {
		CryptoProcess process = CryptoProcessBuilder.builder(algorithm, algorithm_version);
		return process.decode(byteBuffer);
	}

	public static byte[] encrypt(String algorithm, String algorithm_version, ByteBuffer byteBuffer) throws Exception {
		CryptoProcess process = CryptoProcessBuilder.builder(algorithm, algorithm_version);
		return process.encode(byteBuffer);
	}
}

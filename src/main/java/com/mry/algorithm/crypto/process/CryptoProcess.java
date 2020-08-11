package com.mry.algorithm.crypto.process;

import java.nio.ByteBuffer;


public interface CryptoProcess {

	
	
	byte[] decode(ByteBuffer byteBuffer) throws Exception;


	byte[] encode(ByteBuffer byteBuffer) throws Exception;
	


}

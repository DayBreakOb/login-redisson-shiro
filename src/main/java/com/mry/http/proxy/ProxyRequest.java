package com.mry.http.proxy;

import org.redisson.RedissonScript;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;

import com.mry.redis.session.IRedissonSessionScript;

public class ProxyRequest {

	@Autowired
	private RedissonClient redissonClient;
	
	private static String PROXT_MAP_NAME = "useful_proxy";

	public void checkProxyIp() {

		RedissonScript script = (RedissonScript) redissonClient.getScript(new StringCodec());
		Long remainTimeToLive = script.eval(PROXT_MAP_NAME, RScript.Mode.READ_ONLY, IRedissonSessionScript.READ_SCRIPT,
				RScript.ReturnType.INTEGER, null);

	}
}

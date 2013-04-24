package com.orange.common.redis;

import java.util.concurrent.Callable;

import redis.clients.jedis.Jedis;

public interface RedisCallable<V> {

	public V call(final Jedis jedis);
}

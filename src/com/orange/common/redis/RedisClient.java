package com.orange.common.redis;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {

	static Logger log = Logger.getLogger(RedisClient.class.getName()); 
	JedisPool pool = null; 	
	static RedisClient defaultClient = new RedisClient();
	
	private RedisClient(){
		
		String address = System.getProperty("redis.address");
		if (address == null) {
			address = "127.0.0.1";
		}
		
		log.info("Create redis client pool on address "+address);
		pool = new JedisPool(new JedisPoolConfig(), address);
	}

	public static RedisClient getInstance(){
		return defaultClient;
	}
	
	public Object execute(RedisCallable callable){
		Jedis jedis = pool.getResource();
		Object result = null;
		try{
			result = callable.call(jedis);			
		}
		catch(Exception e){
			log.error("<execute redis> but catch exception="+e.toString(), e);
			result = null;
		}
		finally{
			pool.returnResource(jedis);
			jedis = null;
		}
		
		return result;
	}
	
	public void hset(final String table, final String key, final String hashKey, final String hashValue){
		execute(new RedisCallable() {			
			@Override
			public Object call(Jedis jedis) {
				if (table == null || key == null || hashKey == null){
					log.error("<hset> but table or key or hashKey is null, table="+table+", key="+key+", hashKey="+hashKey);
					return null;
				}
				
				String jedisKey = table + ":" + key;
				jedis.hset(jedisKey, hashKey, hashValue);
				return null;
			}
		});
	}
	
	public void destroyPool(){

		execute(new RedisCallable() {

			@Override
			public Object call(Jedis jedis) {
				return null;
			}
		});		
		
		if (pool != null){
			log.info("<RedisClient> destory pool");
			pool.destroy();
			pool = null;
		}
	}
	
}

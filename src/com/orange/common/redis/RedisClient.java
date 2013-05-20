package com.orange.common.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.orange.game.model.manager.feed.HotFeedManagerFactory;

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
	
	public boolean zadd(final String key, final double score, final String member){
		Object result = (Boolean)execute(new RedisCallable<Boolean>() {
			@Override
			public Boolean call(Jedis jedis) {				
				if (key == null || member == null){
					log.error("<RedisClient> ADD but key or member is null");
					return Boolean.FALSE;
				}
				jedis.zadd(key, score, member);
				return Boolean.TRUE;
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();
	}
	
	// get TOP N
	@SuppressWarnings("unchecked")
	public Set<String> ztop(final String key, final int offset, final int limit) {
		Object retList = RedisClient.getInstance().execute(new RedisCallable<Set<String>>() {

			@Override
			public Set<String> call(Jedis jedis) {
				
				Set<String> set = jedis.zrevrangeByScore(key, Double.MAX_VALUE, Double.MIN_VALUE, offset, limit);
				if (set != null){
					return set;
				}
				
				return Collections.emptySet();
			}
			
		});
		
		if (retList == null)
			return Collections.emptySet();
		
		return (Set<String>)retList;
	}
	
	// delete data after TOP N
	public boolean zdeletebelowtop(final String key, final int maxTopCount){
		Object result = (Boolean)execute(new RedisCallable<Boolean>() {
			@Override
			public Boolean call(Jedis jedis) {				
				Long removeCount = jedis.zremrangeByRank(key, 0, -maxTopCount);
				log.info("<RedisClient> "+removeCount+" DELETED @"+key);
				return Boolean.TRUE;
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();
	}	
	
//	public void hset(final String table, final String key, final String hashKey, final String hashValue){
//		execute(new RedisCallable() {			
//			@Override
//			public Object call(Jedis jedis) {
//				if (table == null || key == null || hashKey == null){
//					log.error("<hset> but table or key or hashKey is null, table="+table+", key="+key+", hashKey="+hashKey);
//					return null;
//				}
//				
//				String jedisKey = table + ":" + key;
//				jedis.hset(jedisKey, hashKey, hashValue);
//				return null;
//			}
//		});
//	}
	
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
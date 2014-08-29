package com.orange.common.redis;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.orange.common.scheduler.ScheduleService;
import com.orange.common.utils.PropertyUtil;
import org.apache.log4j.Logger;

import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisClient {

    public static final int RANK_NOT_FOUND = -1;
    static Logger log = Logger.getLogger(RedisClient.class.getName());
	JedisPool pool = null; 	
	static RedisClient defaultClient = new RedisClient();
	
//	final private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);
	
	private RedisClient(){
		
		String address = System.getProperty("redis.address");
		if (address == null) {
			address = "127.0.0.1";
		}

        int port = PropertyUtil.getIntProperty("redis.port", 6379);

		log.info("Create redis client pool on address "+address);

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxActive(128);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxWait(1000);
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(60*1000);

        int timeout = 30*1000;
		pool = new JedisPool(poolConfig, address, port, timeout);
	}

	public static RedisClient getInstance(){
		return defaultClient;
	}
	
	public Object execute(RedisCallable callable){
        if (callable == null){
            log.warn("<execute redis> warning!!! callabel is null");
            return null;
        }

		Jedis jedis = pool.getResource();
        if (jedis == null){
            log.error("<execute redis> exception!!! no jedis in resource pool!");
            return null;
        }

		Object result = null;
		try{
			result = callable.call(jedis);			
		}
		catch (JedisConnectionException e){
			log.error("<execute redis> but catch JedisConnectionException="+e.toString(), e);
            pool.returnBrokenResource(jedis);
            log.info("<execute redis> return broken jedis resource");
            jedis = null;
			result = null;
		}
        catch (Exception e1){
            log.error("<execute redis> but catch exception="+e1.toString(), e1);
            result = null;
        }
		finally{
            if (jedis != null){
                pool.returnResource(jedis);
			    jedis = null;
            }
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
				log.info("<RedisClient> "+member+","+score+" ADDED @"+key);
				return Boolean.TRUE;
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();
	}

    public boolean zinc(final String key, final double increment, final String member) {
        Object result = (Boolean)execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                if (key == null || member == null){
                    log.error("<RedisClient> ADD but key or member is null");
                    return Boolean.FALSE;
                }
                jedis.zincrby(key, increment, member);
                log.info("<RedisClient> "+member+","+increment+" ZINCBY @"+key);
                return Boolean.TRUE;
            }
        });

        if (result == null)
            return false;

        return ((Boolean)result).booleanValue();

    }

    @SuppressWarnings("unchecked")
    public Set<String> zbelowScore(final String key, final double score) {
            Object retList = RedisClient.getInstance().execute(new RedisCallable<Set<String>>() {

                    @Override
                    public Set<String> call(Jedis jedis) {
                            
                            Set<String> set = jedis.zrangeByScore(key, Double.MIN_VALUE, score, 0, Integer.MAX_VALUE);
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


    // get TOP N
	@SuppressWarnings("unchecked")
	public Set<String> ztop(final String key, final int offset, final int limit) {
		Object retList = RedisClient.getInstance().execute(new RedisCallable<Set<String>>() {

			@Override
			public Set<String> call(Jedis jedis) {
				
				Set<String> set = jedis.zrevrangeByScore(key, Double.MAX_VALUE, -Double.MIN_VALUE, offset, limit);
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



	public boolean zrem(final String key, final String member) {
		Object result = (Boolean)execute(new RedisCallable<Boolean>() {
			@Override
			public Boolean call(Jedis jedis) {				
				if (key == null || member == null){
					log.error("<RedisClient> REMOVE but key or member is null");
					return Boolean.FALSE;
				}
				Long count = jedis.zrem(key, member);
				log.info("<RedisClient> "+member+" " + count + " REMOVED @"+key);
				return Boolean.TRUE;
			}			
		});		
		
		if (result == null)
			return false;
		
		return ((Boolean)result).booleanValue();
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
	
	public int ztopcount(final String key) {
		Object result = (Object)execute(new RedisCallable<Long>() {
			@Override
			public Long call(Jedis jedis) {				
				Long count = jedis.zcount(key, Double.MIN_VALUE, Double.MAX_VALUE);
				log.info("<RedisClient> "+count+" COUNT @"+key);
				return count;
			}			
		});		
		
		if (result == null)
			return 0;
		
		return ((Long)result).intValue();
	}

    public int zcountbelow(final String key, final double score) {
        Object result = (Object)execute(new RedisCallable<Long>() {
            @Override
            public Long call(Jedis jedis) {
                Long count = jedis.zcount(key, Double.MIN_VALUE, score);
                log.info("<RedisClient> "+count+" COUNT @"+key);
                return count;
            }
        });

        if (result == null)
            return 0;

        return ((Long)result).intValue();
    }

    public int zrevrank(final String key, final String member) {
        Object result = (Object)execute(new RedisCallable<Long>() {
            @Override
            public Long call(Jedis jedis) {
                Long rank = jedis.zrevrank(key, member);
                log.info("<RedisClient> rank = "+rank+" ZREVRANK @"+key);
                return rank;
            }
        });

        if (result == null)
            return RANK_NOT_FOUND;

        return ((Long)result).intValue();
    }

    public int zrank(final String key, final String member) {
        Object result = (Object)execute(new RedisCallable<Long>() {
            @Override
            public Long call(Jedis jedis) {
                Long rank = jedis.zrank(key, member);
                log.info("<RedisClient> rank = "+rank+" ZRANK @"+key);
                return rank;
            }
        });

        if (result == null)
            return RANK_NOT_FOUND;

        return ((Long)result).intValue();
    }

    public boolean hset(final String key, final String field, final String value){
        Object result = (Boolean)execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                if (key == null || field == null || value == null){
                    log.error("<RedisClient> HSET but key or field or value is null");
                    return Boolean.FALSE;
                }

                jedis.hset(key, field, value);
                log.info("<RedisClient> hset "+field+","+value+" HSET @"+key);
                return Boolean.TRUE;
            }
        });

        if (result == null)
            return false;

        return ((Boolean)result).booleanValue();
    }

    public String hget(final String key, final String field) {
        String result = (String)execute(new RedisCallable<String>() {
            @Override
            public String call(Jedis jedis) {
                if (key == null || field == null){
                    log.error("<RedisClient> HGET but key or field is null");
                    return null;
                }

                String value = jedis.hget(key, field);
                log.info("<RedisClient> HGET "+field+","+value+" HGET @"+key+", value="+value);
                return value;
            }
        });

        return result;
    }


    public int hinc(final String key, final String field, final int increment) {
        Object result = (Object)execute(new RedisCallable<Long>() {
            @Override
            public Long call(Jedis jedis) {
            Long val = jedis.hincrBy(key, field, increment);
            log.info("<RedisClient> field = "+field+ " incby "+increment+" HINC @"+key);
            return val;
            }
        });

        if (result == null)
            return 0;

        return ((Long)result).intValue();
    }

    public boolean sismember(final String key, final String member) {
        Object result = (Object)execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                Boolean result = jedis.sismember(key, member);
                log.info("<RedisClient> field = "+member+ " SISMEMBER @"+key+", result="+result);
                return result;

            }
        });

        if (result == null)
            return false;

        return ((Boolean)result).booleanValue();
    }

    public double zscore(final String key, final String member){

        Double result = (Double) execute(new RedisCallable<Double>() {
            @Override
            public Double call(Jedis jedis) {
                return jedis.zscore(key, member);
            }
        });

        if (result == null){
            return 0;
        }

        return result;
    }

    public boolean zismember(final String key, final String member) {
        Object result = (Object)execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                Object result = jedis.zscore(key, member);
                if(result == null){
                    return Boolean.FALSE;
                }
                log.info("<RedisClient> field = "+member+ " SISMEMBER @"+key+", result="+result);
                return Boolean.TRUE;

            }
        });

        if (result == null)
            return false;

        return ((Boolean)result).booleanValue();
    }


    public boolean sadd(final String key, final String member) {
        Object result = (Boolean)execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
                if (key == null || member == null){
                    log.error("<RedisClient> SADD but key or member is null");
                    return Boolean.FALSE;
                }
                Long count = jedis.sadd(key, member);
                log.info("<RedisClient> "+member+" " + count + " SADD @"+key);
                return Boolean.TRUE;
            }
        });

        if (result == null)
            return false;

        return ((Boolean)result).booleanValue();
    }

    public boolean del(final String key) {
        Object result = (Boolean)execute(new RedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis jedis) {
            if (key == null){
                log.error("<RedisClient> DEL but key is null");
                return Boolean.FALSE;
            }
            Long count = jedis.del(key);
            log.info("<RedisClient> DEL @"+key);
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

    public void zremRangeByScore(final String key, final double minScore, final double maxScore) {

        // clean useless data
        RedisClient.getInstance().execute(new RedisCallable<Boolean>() {

            @Override
            public Boolean call(Jedis jedis) {
                Long removeCount = jedis.zremrangeByScore(key, minScore, maxScore);
                log.info("<RedisClient> "+removeCount+" CLEANED @"+key);
                return Boolean.TRUE;
            }

        });
    }


	public void scheduleRemoveRecordAfterZSetTop(final String key, final int maxCount, final int interval) {

        ScheduleService.getInstance().scheduleEverySecond(interval, new Runnable() {

            @Override
            public void run() {
                // clean useless data
                RedisClient.getInstance().execute(new RedisCallable<Boolean>() {

                    @Override
                    public Boolean call(Jedis jedis) {
                        Long removeCount = jedis.zremrangeByRank(key, 0, -maxCount);
                        log.info("<RedisClient> "+removeCount+" CLEANED @"+key);
                        return Boolean.TRUE;
                    }

                });
                return;
            }
        });




//		scheduleService.scheduleAtFixedRate(new Runnable() {
//
//			@Override
//			public void run() {
//				// clean useless data
//				RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
//
//					@Override
//					public Boolean call(Jedis jedis) {
//						Long removeCount = jedis.zremrangeByRank(key, 0, -maxCount);
//						log.info("<RedisClient> "+removeCount+" CLEANED @"+key);
//						return Boolean.TRUE;
//					}
//
//				});
//				return;
//			}
//		}, 1, interval, TimeUnit.SECONDS);
		
	}
	
	public void scheduleRemoveRecordAfterListTop(final String key, final int maxCount, final int interval) {

        ScheduleService.getInstance().scheduleEverySecond(interval, new Runnable() {

            @Override
            public void run() {
                // clean useless data
                RedisClient.getInstance().execute(new RedisCallable<Boolean>() {

                    @Override
                    public Boolean call(Jedis jedis) {

                        Long count = jedis.llen(key);
                        if (count == null){
                            return Boolean.FALSE;
                        }

                        if (count.intValue() <= maxCount){
                            return Boolean.TRUE;
                        }

                        int popCount = count.intValue() - maxCount;

                        Pipeline p = jedis.pipelined();
//                        p.multi();
                        for (int i=0; i<popCount; i++){
                            p.rpop(key);
                        }
//                        Response<List<Object>> result = p.exec();
                        List result = p.syncAndReturnAll();
                        log.info("<RedisClient> "+popCount+" CLEANED @"+key);

                        if (result == null){
                            log.warn("<RedisClient> pipeline exec result null @"+key);
                            return Boolean.FALSE;
                        }

                        return Boolean.TRUE;
                    }

                });
                return;
            }
        });

//		scheduleService.scheduleAtFixedRate(new Runnable() {
//
//			@Override
//			public void run() {
//				// clean useless data
//				RedisClient.getInstance().execute(new RedisCallable<Boolean>() {
//
//					@Override
//					public Boolean call(Jedis jedis) {
//
//						Long count = jedis.llen(key);
//						if (count == null){
//							return Boolean.FALSE;
//						}
//
//						if (count.intValue() <= maxCount){
//							return Boolean.TRUE;
//						}
//
//						int popCount = count.intValue() - maxCount;
//
//						Transaction transaction = jedis.multi();
//						for (int i=0; i<popCount; i++){
//							transaction.rpop(key);
//						}
//						transaction.exec();
//						log.info("<RedisClient> "+popCount+" CLEANED @"+key);
//						return Boolean.TRUE;
//					}
//
//				});
//				return;
//			}
//		}, 1, interval, TimeUnit.SECONDS);
		
	}


}



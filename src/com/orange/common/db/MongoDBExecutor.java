package com.orange.common.db;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.orange.common.mongodb.MongoDBClient;
import org.apache.log4j.Logger;

// TODO not implement yet
public abstract class MongoDBExecutor {


    static Logger log = Logger.getLogger(MongoDBExecutor.class.getName());

	public static final String DB_NAME = "game";
		
//	final MongoDBClient[] mongoClientList;   
	
	final MongoDBClient mongoClient;  // mongo client is a share DB pool, it's thread safe and don't need to have a list
    
	public static final int EXECUTOR_POOL_NUM = 5;

	CopyOnWriteArrayList<ExecutorService> executorList = new CopyOnWriteArrayList<ExecutorService>();
	
	abstract public String getDBName();
	
	public MongoDBExecutor(){		
		
		mongoClient = new MongoDBClient(getDBName());
		
    	for (int i=0; i<EXECUTOR_POOL_NUM; i++){
    		ExecutorService executor = Executors.newSingleThreadExecutor();
    		executorList.add(executor);
    	}
    	
	} 	    
    
    public void executeDBRequest(final int sessionId, Runnable runnable){
    	ExecutorService executor = getExecutor(sessionId);
    	executor.execute(runnable);    	
    }
    
    private ExecutorService getExecutor(int sessionId) {
    	int index = sessionId % EXECUTOR_POOL_NUM;    	
		return executorList.get(index);
	}
    
    public MongoDBClient getMongoDBClient(int sessionId){
    	return mongoClient;
    }
    
    public MongoDBClient getMongoDBClient(){
    	return mongoClient;
    }

    public void shutdown(){
        int WAIT_SECONDS = 10;
        for (ExecutorService service : executorList){
            service.shutdown();
            try {
                log.info("wait "+WAIT_SECONDS+" seconds for mongo db executor termination");
                if (service.awaitTermination(10, TimeUnit.SECONDS)){
                    log.info("mongo db executor terminated");
                }
            } catch (InterruptedException e) {
                log.error("mongo db executor catch exception="+e.toString(), e);
            }
        }


    }
}
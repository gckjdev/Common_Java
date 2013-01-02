package com.orange.common.db;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.orange.common.mongodb.MongoDBClient;

// TODO not implement yet
public abstract class MongoDBExecutor {

	public static final String DB_NAME = "game";
		
	final MongoDBClient[] mongoClientList;   
    
	public static final int EXECUTOR_POOL_NUM = 5;

	CopyOnWriteArrayList<ExecutorService> executorList = new CopyOnWriteArrayList<ExecutorService>();
	
	abstract public String getDBName();
	
	public MongoDBExecutor(){		
		
		mongoClientList = new MongoDBClient[EXECUTOR_POOL_NUM];
		
    	for (int i=0; i<EXECUTOR_POOL_NUM; i++){
    		mongoClientList[i] = new MongoDBClient(getDBName());
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
    	int index = sessionId % EXECUTOR_POOL_NUM;
    	return mongoClientList[index];
    }
}
package com.orange.common.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.eclipse.jetty.util.ConcurrentHashSet;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.orange.common.api.service.CommonParameter;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;



public class BlackUserService {

	Logger log = Logger.getLogger(BlackUserService.class.getName());
	
	final Set<String> blackUsers = new ConcurrentHashSet<String>();
	final Set<String> blackDevices = new ConcurrentHashSet<String>();
	
	volatile boolean hasLoadUsers = false;
	volatile boolean hasLoadDevices = false;
	
	final private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);
		
	// thread-safe singleton implementation
	private static BlackUserService service = new BlackUserService();     
	private BlackUserService(){		
		super();
	} 	    	

	private synchronized void loadBlackDevice(MongoDBClient mongoClient) {
		if (hasLoadDevices){
			return;
		}
		
		log.info("Loading black device");
		hasLoadDevices = true;
		DBCursor cursor =  mongoClient.findAll(CommonParameter.TABLE_BLACK_DEVICE);
		if (cursor == null){
			log.info("Loading black device, no black device");			
			return;
		}
		
		while (cursor.hasNext()){
			DBObject obj = cursor.next();
			if (obj != null){
				ObjectId id = (ObjectId)obj.get("_id");
				String black = id.toStringMongod();
				blackDevices.add(black);
				log.info("Add black device "+black);
			}	
		}
		log.info("Loading black device, total "+blackDevices.size());			

		cursor.close();
	}
	
	
	private synchronized void loadBlackUser(MongoDBClient mongoClient) {
		if (hasLoadUsers){
			return;
		}
		
		log.info("Loading black user");
		hasLoadUsers = true;
		DBCursor cursor =  mongoClient.findAll(CommonParameter.TABLE_BLACK_USER);
		if (cursor == null){
			log.info("Loading black user, no black user");			
			return;
		}
		
		while (cursor.hasNext()){
			DBObject obj = cursor.next();
			if (obj != null){
				ObjectId id = (ObjectId)obj.get("_id");
				String black = id.toStringMongod();
				blackUsers.add(id.toStringMongod());
				log.info("Add black user "+black);
			}	
		}
		log.info("Loading black user, total "+blackUsers.size());			
		
		cursor.close();
	}
	
	public void load(final MongoDBClient mongoClient){
		
		scheduleService.scheduleWithFixedDelay(new  Runnable(){

			@Override
			public void run() {		
				log.info("Schedule Load Black User & Device");
				hasLoadDevices = false;
				hasLoadUsers = false;
				loadBlackDevice(mongoClient);
				loadBlackUser(mongoClient);				
			}
			
		}, 0, 5, TimeUnit.MINUTES);
	}
	
	public static BlackUserService getInstance() { 						
		return service; 
	}	
	
	public boolean isBlackUser(String userId){
		if (StringUtil.isEmpty(userId)){
			return false;
		}	
		
		if (!hasLoadUsers){
			log.warn("Black User Not Loaded");
		}
		
		if (blackUsers.contains(userId)){
	    	log.info("Check Black User, userId="+userId+" in black list!!!");
			return true;
		}
		
		return false;		
	}
	
	public boolean isBlackDevice(String deviceId){
		if (StringUtil.isEmpty(deviceId)){
			return false;
		}	
		
		if (!hasLoadDevices){
			log.warn("Black Devices Not Loaded");
		}

		if (blackDevices.contains(deviceId)){
	    	log.info("Check Black Device, deviceId="+deviceId+" in black list!!!");
			return true;
		}
		
		return false;		
	}

	
	
}

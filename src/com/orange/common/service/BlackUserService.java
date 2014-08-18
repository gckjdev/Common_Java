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
		
//		log.info("Loading black device");
		hasLoadDevices = true;
		DBCursor cursor =  mongoClient.findAll(CommonParameter.TABLE_BLACK_DEVICE);
		if (cursor == null){
//			log.info("Loading black device, no black device");
			return;
		}
		
		try{
			blackDevices.clear();
			while (cursor.hasNext()){
				DBObject obj = cursor.next();
				if (obj != null){
					String id = (String)obj.get("_id");
					blackDevices.add(id);
//					log.info("Add black device "+id);
				}	
			}
//			log.info("Loading black device, total "+blackDevices.size());
		}
		catch(Exception e){
			log.error("Loading black device but catch exception="+e.toString(), e);			
		}

		cursor.close();
	}
	
	
	private synchronized void loadBlackUser(MongoDBClient mongoClient) {
		if (hasLoadUsers){
			return;
		}
		
//		log.info("Loading black user");
		hasLoadUsers = true;
		DBCursor cursor =  mongoClient.findAll(CommonParameter.TABLE_BLACK_USER);
		if (cursor == null){
//			log.info("Loading black user, no black user");
			return;
		}
		
		try{
			blackUsers.clear();
			while (cursor.hasNext()){
				DBObject obj = cursor.next();
				if (obj != null){
					ObjectId id = (ObjectId)obj.get("_id");
					String black = id.toStringMongod();
					blackUsers.add(id.toStringMongod());
//					log.info("Add black user "+black);
				}	
			}
//			log.info("Loading black user, total "+blackUsers.size());
		}
		catch(Exception e){
			log.error("Loading black user but catch exception="+e.toString(), e);			
		}
		
		cursor.close();
	}
	
	private void executeLoad(final MongoDBClient mongoClient){
//		log.info("Schedule Load Black User & Device");
		hasLoadDevices = false;
		hasLoadUsers = false;
		loadBlackDevice(mongoClient);
		loadBlackUser(mongoClient);	
	}
	
	public void load(final MongoDBClient mongoClient){
		
		executeLoad(mongoClient);
		
		scheduleService.scheduleWithFixedDelay(new  Runnable(){

			@Override
			public void run() {		
				executeLoad(mongoClient);
			}
			
		}, 5, 5, TimeUnit.MINUTES);
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

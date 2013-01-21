package com.orange.common.network;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.orange.common.log.ServerLog;

@ThreadSafe
public class ChannelUserManager {

	protected static final Logger logger = Logger.getLogger("ChannelUserManager");
		
	ConcurrentMap<Channel, CopyOnWriteArrayList<String>> channelUserMap 
		= new ConcurrentHashMap<Channel, CopyOnWriteArrayList<String>>();
	
	// thread-safe singleton implementation
    private static ChannelUserManager manager = new ChannelUserManager();     
    private ChannelUserManager(){		
	} 	    
    public static ChannelUserManager getInstance() { 
    	return manager; 
    } 
    
    public void addChannel(Channel channel){
		ServerLog.info(0, "<addChannel> Channel " + channel.toString() + ", total " + channelUserMap.size());
		CopyOnWriteArrayList<String> userList = new CopyOnWriteArrayList<String>();
		channelUserMap.putIfAbsent(channel, userList);
    }
    
    public void addUserIntoChannel(Channel channel, String userId){    	
		
		CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
		if (userList == null){
			logger.warn("<addUserIntoChannel> Add " + userId + " Into Channel " + channel.toString() +", but channel not found");
			return;
		}
	
		ServerLog.info(0, "<addUserIntoChannel> Add " + userId + " Into Channel " + channel.toString());
		userList.add(userId);
    }
    
    public void removeUserFromChannel(Channel channel, String userId){
    	CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
    	if (userList == null){
    		ServerLog.info(0, "<ChannelUserManager.removeUserFromChannel> Remove " + userId + " From Channel " + channel.toString() + ", but channel not found");    		
    		return;
    	}

		ServerLog.info(0, "<ChannelUserManager.removeUserFromChannel> Remove " + userId + " From Channel " + channel.toString());    		
		userList.remove(userId);
    }
    
    public void removeChannel(Channel channel){
    	
    	if (channel == null){
    		return;
    	}
    	
    	try{    	
    		if (!channelUserMap.containsKey(channel)){
    			return;
    		}
    			    	
	    	ChannelFuture closeFuture = channel.close();
	    	closeFuture.await(1000);
	    	if (closeFuture.isSuccess()){
				ServerLog.info(0, "<ChannelUserManager.removeChannel> Close success! channel=" + channel.toString() + ", before remove count = " + channelUserMap.size());
	    	}
	    	else{
	    		ServerLog.info(0, "<ChannelUserManager.removeChannel> Wait channel close future time out, channel=" + channel.toString());
	    	}
    	} catch (Exception e){    	
    		logger.error("<ChannelUserManager.removeChannel> channel="+channel.toString() + " catch exception = "+e.toString(), e);
    	} finally{
        	channelUserMap.remove(channel);    	    		
    	}
		
    }
    
    // return users in channel
	public List<String> findUsersInChannel(Channel channel) {		
		List<String> list = channelUserMap.get(channel);
		if (list == null){
//			logger.error("<findUsersInChannel> channel="+channel.toString() + " but no user???");
			return Collections.emptyList();
		}
		
		return list;
	}
	
    // return the first user in channel
	public String findUserInChannel(Channel channel) {		
		List<String> list = channelUserMap.get(channel);
		if (list == null || list.size() == 0){
			logger.error("<ChannelUserManager.findUserInChannel> channel="+channel.toString() + " but no user???");
			return null;
		}
		
		return list.get(0);
	}


//	public void processDisconnectChannel(Channel channel){
//		List<String> userIdList = findUsersInChannel(channel);
//		for (String userId : userIdList){
//			int sessionId = UserManager.getInstance().findGameSessionIdByUserId(userId);
//			if (sessionId != -1){
//				GameSession session = GameSessionManager.getInstance().findGameSessionById(sessionId);
//				GameSessionManager.getInstance().userQuitSession(userId, session, true);
//			}
//
//			UserManager.getInstance().removeOnlineUserById(userId);
//		}
//		
//		// remove channel
//		ChannelUserManager.getInstance().removeChannel(channel);		
//	}
}

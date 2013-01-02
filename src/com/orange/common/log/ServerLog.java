package com.orange.common.log;

import org.apache.log4j.Logger;

public class ServerLog {
		
	private static final Logger logger = Logger.getLogger(ServerLog.class.getName());
	
	private static String getMessage(int sessionId, String...msg){
		String s = "";
		switch (msg.length){
		case 1:
			s = String.format("[%010d] %s", sessionId, msg[0]);
			break;
		case 2:
			s = String.format("[%010d] %s, %s", sessionId, msg[0], msg[1]);
			break;
		case 3:
		default:
			s = String.format("[%010d] %s, %s, %s", sessionId, msg[0], msg[1], msg[2]);
			break;
		}		
		
		return s;
	}
	
	public static void debug(int sessionId, String...msg){
		if (msg.length == 0)
			return;
		
		logger.debug(getMessage(sessionId, msg));		
	}

	public static void info(int sessionId, String...msg){
		if (msg.length == 0)
			return;
		
		logger.info(getMessage(sessionId, msg));		
	}
	
	public static void warn(int sessionId, String...msg){
		if (msg.length == 0)
			return;
		
		logger.warn(getMessage(sessionId, msg));				
	}

	public static void error(int sessionId, Exception e, String...msg){
		if (msg.length == 0)
			return;
		
		logger.error(getMessage(sessionId, msg).concat(", exception=").concat(e.toString()), e);						
	}	
	
	public static void error(int sessionId, Exception e){
		if (e == null)
			return;
		
		logger.error(getMessage(sessionId, " catch exception=".concat(e.toString())), e);						
	}

	
	public static void fatal(int sessionId, String string, Exception e) {
		logger.fatal(getMessage(sessionId, " catch exception=".concat(e.toString())), e);
	}

}

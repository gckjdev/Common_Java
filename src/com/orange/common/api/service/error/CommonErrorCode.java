package com.orange.common.api.service.error;

import com.orange.common.api.service.CommonParameter;


public class CommonErrorCode {
	
	static public final int ERROR_PARAMETER                 = 10001;
	static public final int ERROR_PARA_METHOD_NOT_FOUND     = 10002;
	static public final int ERROR_PARAMETER_TIMESTAMP_EMPTY = 10003;
	static public final int ERROR_PARAMETER_TIMESTAMP_NULL 	= 10004;
	static public final int ERROR_PARAMETER_MAC_EMPTY 		= 10005;
	static public final int ERROR_PARAMETER_MAC_NULL 		= 10006;		


	
	// DB Error
	static public final int ERROR_CASSANDRA                 = 80001;
	static public final int ERROR_CASSANDRA_UNAVAILABLE     = 80002;

	// System Error
	static public final int ERROR_SYSTEM                    = 90001;
	static public final int ERROR_NOT_GET_METHOD            = 90002;
	static public final int ERROR_INVALID_SECURITY			= 90003;
	static public final int ERROR_NAME_VALUE_NOTMATCH		= 90004;
	static public final int ERROR_JSON 						= 90005;
	public static final int ERROR_BLACK_USER 				= 90006;
	public static final int ERROR_BLACK_DEVICE 				= 90007;

	
	static public String getJSONByErrorCode(int errorCode){
		return String.format("{\"%s\":%d}", CommonParameter.RET_CODE, errorCode);		
	}

	
}

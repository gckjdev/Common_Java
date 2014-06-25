package com.orange.common.api.service;

public class CommonParameter {

	public static final String RET_CODE = "ret";
	public static final String RET_DATA = "dat";
	
	
	public static final String METHOD = "m";
	public static final String METHOD_SECURE = "m1";
	public static final String FORMAT = "format";	
	
	public static final String PARA_TIMESTAMP = "ts";
	public static final String PARA_MAC = "mac";
	public static final String PARA_USER_ID = "uid";
	public static final String PARA_DEVICE_ID = "did";
	
	public static final String TEXT_HTML = "text/html; charset=utf-8";
	
	public static final String APPLICATION_JSON = "application/json; charset=utf-8";
	public static final int LANGUAGE_CHINESE = 1;
	public static final int LANGUAGE_UNKNOW = 0;
	
	public static final int  VERIFY_SUCCESS = 1;
	public static final String RESPONSE_VERIFY_SUCCESS = "<html><h1 align='center'>注册成功</h1></html>";

	public static final String JSON = "json";
	public static final String PROTOCOL_BUFFER = "pb";
    public static final String JSONP_CALLBACK = "jsonpcallback";
    public static final String PARA_JSONP_CALLBACK = "jsonpcallback";
	
	public static final String TABLE_BLACK_DEVICE = "black_device";
	public static final String TABLE_BLACK_USER = "black_user";
	
	public static boolean isSecureMethod(String method){
		return (method != null && method.equals(METHOD_SECURE));
	}
}

package com.orange.common.api.service;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.cassandra.cli.CliParser.value_return;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.orange.common.cassandra.CassandraClient;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;

public abstract class CommonService {
	
	/** 
     * Read the next line of input. 
     * 
     * @return a byte array containing all post data 
     * @exception IOException 
     *                if an input or output exception has occurred. 
     */  
    protected byte[] readPostData(ServletInputStream in) throws IOException{  
    	
    	log.info("<readPostData>");
    	
    	int MAX_BUFFER_SIZE = 8*1024;
        byte[] buf = new byte[MAX_BUFFER_SIZE];  
        List<byte[]> byteList = new ArrayList<byte[]>();

        int totalLen = 0;
        int result;    
        do {  
            result = in.read(buf, 0, MAX_BUFFER_SIZE); // does +=  
            if (result > 0) {
            	totalLen += result;
            	byte[] bytes = new byte[result];
            	for (int i=0; i<result; i++){
            		bytes[i] = buf[i];
            	}
            	byteList.add(bytes);
            }  
        }  
        while (result > 0); // loop only if the buffer was filled  
        in.close();

        if (totalLen <= 0){
        	return null;
        }
        
        ByteBuffer retByteBuffer = ByteBuffer.allocate(totalLen);
        for (byte[] bytes : byteList){
        	retByteBuffer.put(bytes);
        }
               
        byte[] data = retByteBuffer.array();
		log.info("<readPostData> total "+data.length + " bytes read");
        
        return data;  
    }  
	
	// response data
	protected int resultCode = 0;
	protected Object resultData = null;
	protected byte[] byteData = null;
	protected String resultType = CommonParameter.APPLICATION_JSON;
	protected String format = CommonParameter.JSON;
	
	
	protected CassandraClient cassandraClient = null;
	protected MongoDBClient mongoClient = null;
	
	HttpServletRequest request = null;		

	public MongoDBClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(MongoDBClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	@SuppressWarnings("unchecked")
	protected
	static Map<String, Class> methodMap = new HashMap<String, Class>();

	public CassandraClient getCassandraClient() {
		return cassandraClient;
	}

	public void setCassandraClient(CassandraClient cassandraClient) {
		this.cassandraClient = cassandraClient;
	}
	
	public static final Logger log = Logger.getLogger(CommonService.class
			.getName());

	
	public static CommonService createServiceObjectByMethod(String method)
			throws InstantiationException, IllegalAccessException {
		return null;
	}

	// save data from request to object fields
	public abstract boolean setDataFromRequest(HttpServletRequest request);

	public void printData(){
		log.info(toString());
	}

	// return false if this method doesn't need security check
	public abstract boolean needSecurityCheck();

	// handle request, business logic implementation here
	// need to set responseData and resultCode and return them as JSON string
	public abstract void handleData();

	public String getResponseString() {
	    String retString = "";
	    if (resultCode == CommonParameter.VERIFY_SUCCESS){
	        retString = CommonParameter.RESPONSE_VERIFY_SUCCESS;
	    } else {
	        JSONObject resultObject = new JSONObject();
	        if (resultData != null) {
	            resultObject.put(CommonParameter.RET_DATA, resultData);
	        }
	        resultObject.put(CommonParameter.RET_CODE, resultCode);

	        retString = resultObject.toString();
	    }
		
		return retString;
	}

	public boolean check(String value, int errorCodeEmpty, int errorCodeNull) {
		if (value == null) {
			resultCode = errorCodeNull;
			return false;
		}
		if (value.length() == 0) {
			resultCode = errorCodeEmpty;
			return false;
		}
		return true;
	}

	static final String SHARE_KEY = "NetworkRequestShareKey";

	public boolean validateSecurity(HttpServletRequest request) {

		if (needSecurityCheck() == false) {
			log.warn("<validateSecurity> skip security check");
			return true;
		}

		String timeStamp = request.getParameter(CommonParameter.PARA_TIMESTAMP);
		String mac = request.getParameter(CommonParameter.PARA_MAC);

		// if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
		// ErrorCode.ERROR_PARAMETER_USERID_NULL))
		// return false;

		if (!check(timeStamp, CommonErrorCode.ERROR_PARAMETER_TIMESTAMP_EMPTY,
				CommonErrorCode.ERROR_PARAMETER_TIMESTAMP_NULL))
			return false;

		if (!check(mac, CommonErrorCode.ERROR_PARAMETER_MAC_EMPTY,
				CommonErrorCode.ERROR_PARAMETER_MAC_NULL))
			return false;

		String input = timeStamp + SHARE_KEY;
		String encodeStr = StringUtil.md5base64encode(input);
		if (encodeStr == null) {
			log.warn("<validateSecurity> failure, input=" + input
					+ ",client mac=" + mac + ",server mac=null");
			return false;
		}

		if (encodeStr.equals(mac)) {
			log.info("<validateSecurity> OK, input=" + input + ",client mac="
					+ mac + ",server mac=" + encodeStr);
			return true;
		} else {
			log.warn("<validateSecurity> failure, input=" + input
					+ ",client mac=" + mac + ",server mac=" + encodeStr);
			return false;
		}
	}

	public void setDataFormat(String format) {
		this.format = format;
	}

	public byte[] getResponseByteData() {
		return byteData;
	}
	
	protected int getIntValueFromRequeset(HttpServletRequest request, String key, int defaultValue) {
		String value = request.getParameter(key);
		if (value != null && value.length() != 0) {
			return Integer.valueOf(value);
		}
		return defaultValue;
	}
	
}

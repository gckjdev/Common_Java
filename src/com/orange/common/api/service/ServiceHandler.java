package com.orange.common.api.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.prettyprint.hector.api.exceptions.HectorException;
import net.sf.json.JSONException;

import org.apache.log4j.Logger;

import com.orange.common.api.service.error.CommonErrorCode;
import com.orange.common.cassandra.CassandraClient;
import com.orange.common.mongodb.MongoDBClient;

public class ServiceHandler {
	
	private static final Logger log = Logger.getLogger(ServiceHandler.class
			.getName());

	CassandraClient cassandraClient = null;	
	MongoDBClient mongoClient = null;
	CommonServiceFactory serviceFactory = null;

	
	public static ServiceHandler getServiceHandler(CassandraClient cassandraClient, CommonServiceFactory serviceFactory) {
		ServiceHandler handler = new ServiceHandler();
		handler.cassandraClient = cassandraClient;
		handler.serviceFactory = serviceFactory;
		return handler;
	}

	public static ServiceHandler getServiceHandler(MongoDBClient mongoClient, CommonServiceFactory serviceFactory) {
		ServiceHandler handler = new ServiceHandler();
		handler.mongoClient = mongoClient;
		handler.serviceFactory = serviceFactory;
		return handler;
	}

	
	public void handlRequest(HttpServletRequest request,
			HttpServletResponse response){
		
		long startTime = System.currentTimeMillis();
		handle(request, response, startTime);
		long endTime = System.currentTimeMillis();
		long execTime = (endTime-startTime);
		if (execTime > 1000){
			log.warn("[STAT] "+ this.getClass().getSimpleName() + " execution time="+execTime);
		}
		else{
			log.debug("[STAT] execution time="+execTime);
		}
	}
	
	private void handle(HttpServletRequest request,
			HttpServletResponse response, long startTime) {
		
		boolean gzip = isGzipEncoding(request); 

		printRequest(request);

		boolean isSecureMethod = false;
		String method = request.getParameter(CommonParameter.METHOD);
		if (method == null){
			isSecureMethod = true;
			method = request.getParameter(CommonParameter.METHOD_SECURE);
		}
		
		String format = request.getParameter(CommonParameter.FORMAT);
		if (format == null){
			format = CommonParameter.JSON;
		}
		
		CommonService obj = null;		
		obj = serviceFactory.createServiceObjectByMethod(method);

		try {

			if (obj == null) {
				sendResponseByErrorCode(response,
						CommonErrorCode.ERROR_PARA_METHOD_NOT_FOUND, gzip, format);
				return;
			}

			obj.setSecureMethod(isSecureMethod);
			obj.setCassandraClient(cassandraClient);
			obj.setMongoClient(mongoClient);
			obj.setRequest(request);
			obj.setDataFormat(format);
			
			if (!obj.validateSecurity(request)) {
				sendResponseByErrorCode(response,
						CommonErrorCode.ERROR_INVALID_SECURITY, gzip, format);
				return;
			}
			
			// common black user handling here, hard code for quick implementation
			String userId = request.getParameter(CommonParameter.PARA_USER_ID);
			if (obj.isBlackUser(userId)){
				sendResponseByErrorCode(response,
						CommonErrorCode.ERROR_BLACK_USER, gzip, format);
				return;				
			}
			
			// check black device
			String deviceId = request.getParameter(CommonParameter.PARA_DEVICE_ID);
			if (obj.isBlackDevice(deviceId)){
				sendResponseByErrorCode(response,
						CommonErrorCode.ERROR_BLACK_DEVICE, gzip, format);
				return;				
			}

			// parse request parameters
			if (!obj.setDataFromRequest(request)) {
				sendResponseByErrorCode(response, obj.resultCode, gzip, format);
				return;
			}

			// print parameters
			obj.printData();

			// handle request
			obj.handleData();
			
			long endTime = System.currentTimeMillis();
			long execTime = (endTime-startTime);
			if (execTime > 1000){
				log.warn("[STAT] "+ obj.getClass().getSimpleName() + " logic execution time="+execTime);
			}
		} catch (HectorException e) {
			obj.resultCode = CommonErrorCode.ERROR_CASSANDRA;
			log.error("catch DB exception=" + e.toString(), e);
		} catch (JSONException e) {
			obj.resultCode = CommonErrorCode.ERROR_JSON;
			log.error("catch JSON exception=" + e.toString(), e);
		} catch (Exception e) {
			obj.resultCode = CommonErrorCode.ERROR_SYSTEM;
			log.error("catch general exception=" + e.toString(), e);
		} finally {
		}

		if (format.equals(CommonParameter.PROTOCOL_BUFFER)){
			byte[] responseData = obj.getResponseByteData();
			String responseType = obj.resultType;

			// send back response
			if (responseData != null){
				sendResponse(response, responseData, responseType, gzip);
			}
			else{
				sendResponse(response, responseData, responseType, false);
			}
		}
		else {
			String responseData = obj.getResponseString();
			String responseType = obj.resultType;

			// send back response
			sendResponse(response, responseData, responseType, gzip);			
		}

	}

	void printRequest(HttpServletRequest request) {
		log.info("[RECV] request = " + request.getQueryString());
	}

	void printResponse(HttpServletResponse reponse, String responseData) {
		String printStr = "";
		int len = responseData.length();
		final int MAX_PRINT_LEN = 500;
		if (len <= MAX_PRINT_LEN){
			printStr = responseData;
		}
		else{
			printStr = responseData.substring(0, MAX_PRINT_LEN).concat("..."); 
		}
		log.info("[SEND] response data = " + printStr);
	}
	
	private static boolean isGzipEncoding(HttpServletRequest request){ 
        boolean flag=false; 
        String encoding=request.getHeader(ACCEPT_ENCODING); 
        if (encoding == null || encoding.length() == 0) {
            return flag;
        }
        
        if (encoding.indexOf(CONTENT_ENCODING_GZIP)!=-1) { 
            flag=true; 
        } 
        return flag; 
    } 

    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String CONTENT_ENCODING_GZIP = "gzip";
    private static final String CONTENT_TYPE_TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8";
//    private static final String GENERIC_FAILURE_MSG = "The call failed on the server; see server log for details";
	
    void sendResponse(HttpServletResponse response, byte[] responseData, String responseType, boolean gzip) {
//		printResponse(response, responseData);
		response.setContentType(responseType);
		try {

			if (gzip){
				byte[] reply = responseData;
		        String contentType = CONTENT_TYPE_TEXT_PLAIN_UTF8;
				
				ByteArrayOutputStream output = null;
	            GZIPOutputStream gzipOutputStream = null;
	            try {
	                output = new ByteArrayOutputStream(reply.length);
	                gzipOutputStream = new GZIPOutputStream(output);
	                gzipOutputStream.write(reply);
	                gzipOutputStream.finish();
	                gzipOutputStream.flush();
	                response.setHeader(CONTENT_ENCODING, CONTENT_ENCODING_GZIP);
	                reply = output.toByteArray();
	            } catch (IOException e) {
	                log.error("send gzip reponse but catch exception = " + e.toString(), e);
	                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            } finally {
	                if (null != gzipOutputStream) {
	                    gzipOutputStream.close();
	                }
	                if (null != output) {
	                    output.close();
	                }
	            }
	            
	            response.setContentLength(reply.length);
	            response.setContentType(contentType);
	            response.setStatus(HttpServletResponse.SC_OK);
	            response.getOutputStream().write(reply);
	            response.getOutputStream().flush();
	        }
			else{
				if (responseData != null){
					response.getOutputStream().write(responseData);
					response.getOutputStream().flush();
				}
				else{
					response.getOutputStream().flush();
				}
			}
		} catch (IOException e) {
			log.error("sendResponse, catch exception=" + e.toString(), e);			
		}
	}
    
	void sendResponse(HttpServletResponse response, String responseData, String responseType, boolean gzip) {
		printResponse(response, responseData);
		response.setContentType(responseType);
		try {

			if (gzip){
				byte[] reply = responseData.getBytes(CHARSET_UTF8);
		        String contentType = CONTENT_TYPE_TEXT_PLAIN_UTF8;
				
				ByteArrayOutputStream output = null;
	            GZIPOutputStream gzipOutputStream = null;
	            try {
	                output = new ByteArrayOutputStream(reply.length);
	                gzipOutputStream = new GZIPOutputStream(output);
	                gzipOutputStream.write(reply);
	                gzipOutputStream.finish();
	                gzipOutputStream.flush();
	                response.setHeader(CONTENT_ENCODING, CONTENT_ENCODING_GZIP);
	                reply = output.toByteArray();
	            } catch (IOException e) {
	                log.error("send gzip reponse but catch exception = " + e.toString(), e);
	                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            } finally {
	                if (null != gzipOutputStream) {
	                    gzipOutputStream.close();
	                }
	                if (null != output) {
	                    output.close();
	                }
	            }
	            
	            response.setContentLength(reply.length);
	            response.setContentType(contentType);
	            response.setStatus(HttpServletResponse.SC_OK);
	            response.getOutputStream().write(reply);
	            response.getOutputStream().flush();
	        }
			else{
				response.getWriter().write(responseData);
				response.getWriter().flush();
			}
		} catch (IOException e) {
			log.error("sendResponse, catch exception=" + e.toString(), e);			
		}
	}

	void sendResponseByErrorCode(HttpServletResponse response, int errorCode, boolean gzip, String responseFormat) {
		if (responseFormat.equals(CommonParameter.PROTOCOL_BUFFER)){
			sendResponse(response, errorCode);
		}
		else{
			String resultString = CommonErrorCode.getJSONByErrorCode(errorCode);
			sendResponse(response, resultString,CommonParameter.APPLICATION_JSON, gzip);
		}
	}

	private void sendResponse(HttpServletResponse response, int errorCode) {
        try {
            response.setStatus(errorCode);
            response.getWriter().write("");
			response.getWriter().flush();
		} catch (Exception e) {
			log.error("sendResponse, catch exception=" + e.toString(), e);			
		}
	}
}

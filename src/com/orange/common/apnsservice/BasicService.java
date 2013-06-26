package com.orange.common.apnsservice;

import org.apache.log4j.Logger;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.exceptions.NetworkIOException;
import com.orange.common.urbanairship.ErrorCode;
import com.orange.common.utils.StringUtil;

public abstract class BasicService {
    
    protected static final Logger log = Logger.getLogger(BasicService.class.getName());

    protected final ApnsService apnsService;
    public static final boolean IS_TEST = initIsTest(); 
        
    private static boolean initIsTest(){
    	
    	String isTestString = System.getProperty("push.test");
    	boolean isTest = false;
    	if (!StringUtil.isEmpty(isTestString)){
    		isTest = (Integer.parseInt(isTestString) != 0);
    	}
    	
    	return isTest;
    }
    
    public static ApnsService createApnsService(String certificatePath, String password){
    	ApnsService apnsService = null;
        if (IS_TEST){
	        apnsService = APNS.newService()
	                .withCert(certificatePath, password)
	                .withSandboxDestination()
	                .build();
	        
	        log.info("Create Test APNS Service With "+certificatePath+", "+ password);
        }
        else{
	        apnsService = APNS.newService()
	        		.withCert(certificatePath, password)
	        		.withProductionDestination()
	        		.build();     	       

	        log.info("Create Production APNS Service With "+certificatePath+", "+ password);
        }    	
        
        return apnsService;
    }
    
    public BasicService(ApnsService apnsService) {
        super();
        this.apnsService = apnsService;
    }        

    abstract String getPayload();
    abstract public int handleServiceRequest();
}


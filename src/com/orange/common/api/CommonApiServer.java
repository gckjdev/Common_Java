
package com.orange.common.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.orange.common.api.service.ServiceHandler;

public abstract class CommonApiServer extends AbstractHandler
{
	public static final Logger log = Logger.getLogger(CommonApiServer.class.getName());		
	
	public abstract String getSpringContextFile();
	public abstract String getLog4jFile();	
	public abstract String getAppNameVersion();
	public abstract ServiceHandler getServiceHandler();	
	public abstract int getPort();		

	// not used so far
	private void readConfig(String filename){

		   InputStream inputStream = null;		   
		   try {
			   inputStream = new FileInputStream(filename);
		   } catch (FileNotFoundException e) {
			   log.info("configuration file "+filename+"not found exception");
			   e.printStackTrace();
		   }
		   Properties p = new Properties();   
		   try {   
			   p.load(inputStream);   
		   } catch (IOException e1) {   
			   log.info("read configuration file exception");
			   e1.printStackTrace();   
		   }   		   		   
	}
		
	private static void initSpringContext(String... context){
		try {
			new ClassPathXmlApplicationContext(
					context );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initLog4j(){
		log.info("Initializing log4j with: " + getLog4jFile());
		//PropertyConfigurator.configure(LOG4J_FLE);
	}
		
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        baseRequest.setHandled(true);        
		try{			
			
	        getServiceHandler().handlRequest(request, response);
                       
		} catch (Exception e){
			log.error("<handleHttpServletRequest> catch Exception="+e.toString());		
		} finally {
		}		
    }

    public static void startServer(CommonApiServer serverHandler) throws Exception{
        
    	//init the spring context
    	String[] springFiles = new String[]{serverHandler.getSpringContextFile()};
    	initSpringContext(springFiles);    	
    	
    	log.info(serverHandler.getAppNameVersion());
    	
    	Server server = new Server(serverHandler.getPort());
        server.setHandler(serverHandler);
        
        QueuedThreadPool threadPool = new QueuedThreadPool();  
        threadPool.setMaxThreads(100);
        threadPool.setMinThreads(25);
        server.setThreadPool(threadPool);  
        
        server.setStopAtShutdown(true);
        server.start();
        server.join();
    }    
}

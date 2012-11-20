
package com.orange.common.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
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
		
	public static void initSpringContext(String... context) {
		try {
			new ClassPathXmlApplicationContext(
					context );
		} catch (Exception e) {
			log.info("initSpringContext exception");
			e.printStackTrace();
		}
	}
	
    @Override
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
			log.error("<handleHttpServletRequest> catch Exception="+e.toString(), e);		
		} finally {
		}		
    }

	public void startServer() throws Exception {
    	//init the spring context
		String[] springFiles = new String[] { getSpringContextFile() };
    	initSpringContext(springFiles);    	
    	
		log.info(getAppNameVersion());
    	
		Server server = new Server(getPort());
		for (Connector connector : server.getConnectors()) {
			connector.setRequestHeaderSize(1024 * 30);
		}		
		server.setHandler(getHandler());
        
		
        QueuedThreadPool threadPool = new QueuedThreadPool();  
        threadPool.setMaxThreads(100);
        threadPool.setMinThreads(25);
        server.setThreadPool(threadPool);  
        server.setStopAtShutdown(true);
        server.start();
        server.join();
	}
	
	public Handler getHandler(){
		return this;
	}
}

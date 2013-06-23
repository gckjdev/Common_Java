package com.orange.common.utils.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.orange.common.log.ServerLog;

public class HttpDownload {

	public static final Logger log = Logger.getLogger(HttpDownload.class.getName());
	private static final int CONNECTION_TIMEOUT = 30 * 1000;	// 30 seconds		
	
    public static boolean downloadFile(String httpURL, String localFilePath)
    {
    	boolean result = true;
    	
        if (httpURL == null || httpURL.trim().equals(""))
        {
        	log.warn("<downloadFile> http URL is null");
            return false;
        }

        HttpURLConnection tHttpURLConnection = null;
        BufferedInputStream tBufferedInputStream = null;
        FileOutputStream tFileOutputStream = null;
        
        try
        {
            URL tURL = new URL(httpURL);
            tHttpURLConnection = (HttpURLConnection)tURL.openConnection();
            tHttpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            tHttpURLConnection.setReadTimeout(CONNECTION_TIMEOUT);
            tHttpURLConnection.connect();
            tBufferedInputStream = new BufferedInputStream(tHttpURLConnection.getInputStream());
            tFileOutputStream = new FileOutputStream(localFilePath);

            int nBufferSize = 1024;
            byte[] bufContent = new byte[nBufferSize];
            int nContentSize = 0;
            while ((nContentSize = tBufferedInputStream.read(bufContent)) != -1)
            {
                tFileOutputStream.write(bufContent, 0, nContentSize);
            }
        }
        catch (Exception e)
        {
        	log.error("<downloadFile> catch exception, http URL=" + httpURL + 
        			", local path=" + localFilePath + ", exception=" + e.toString(), e);
        	
        	result = false;
        }
        finally {
        	
				try {
		        	if (tHttpURLConnection != null)
		        		tHttpURLConnection.disconnect();

		        	if (tBufferedInputStream != null)
		        		tBufferedInputStream.close();
		        	
		        	if (tFileOutputStream != null)
		        		tFileOutputStream.close();
		        	
		            tHttpURLConnection = null;
		            tBufferedInputStream = null;
		            tFileOutputStream = null;

				} catch (IOException e) {
		        	log.error("<downloadFile> catch exception at finally stage, http URL=" + httpURL + 
		        			", local path=" + localFilePath + ", exception=" + e.toString(), e);        	        	
				}
        	
        }

        return result;
    }
    
    /**
     *  从指定URL下载内容,　以String形式返回页面内容,　以供进一步处理
     *  
     * @param url: 欲下载的页面URL
     * 
     * @return 欲下载的页面内容,以字符串形式; 如果下载失败,　则为空字符串
     * 
     * @throws IOException　下载过程出现的异常 
     */
    public static String downloadFromURL(String url) throws IOException {
		 
	     String result = "";  
	     BufferedReader in = null;  
	     
	     try {
	    	 URL connURL = new java.net.URL(url);  
	    	 HttpURLConnection httpConn = (HttpURLConnection)connURL.openConnection();  
	         // 建立实际的连接  
	         httpConn.connect();  
	         in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));  
	         
	         // 读取返回的内容  
	         String line;  
	         while ((line = in.readLine()) != null) {  
	            result += line;  
	         }  
	     } catch(IOException ie) {
	    	 	ServerLog.info(0, "<downloadFromURL> Query fails due to " + ie.toString());
	    	 	throw ie;
	     } finally {  
	    	  try {  
		           if (in != null) {  
		              in.close();  
		            }  
		       } catch (IOException ex) {  
		           ex.printStackTrace();  
		        }  
	        }  
	      
	     return result;  
	 }
    
}

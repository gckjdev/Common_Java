/*
package com.orange.common.snsrequest;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import weibo4j.Paging;
import weibo4j.Status;
import weibo4j.Weibo;

public class SinaWeiboSNS extends SNS {	

	public SinaWeiboSNS(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
	}

	@Override
	public void publishWeibo(String consumerKey, String consumerSecret,
			SNSRequest request) {
		if (!SNSRequestManager.checkConsumerToken(consumerKey, consumerSecret)) {
			log.info("<SinaWeiboSNS.publishSNSRequest>: "
					+ "consumerKey and consumerSecret is null or empty!");
			return;
		}
		Weibo.CONSUMER_KEY = consumerKey;
		Weibo.CONSUMER_SECRET = consumerSecret;
		File file = request.getImageFile();
		String text = request.getText();
		String tokenKey = request.getTokenKey();
		String tokenSecret = request.getTokenSecret();
		try {
			System.setProperty("weibo4j.oauth.consumerKey", Weibo.CONSUMER_KEY);
			System.setProperty("weibo4j.oauth.consumerSecret",
					Weibo.CONSUMER_SECRET);
			Weibo sinaWeibo = new Weibo();

			sinaWeibo.setToken(tokenKey, tokenSecret);
			Status status = null;
			if (file == null || !file.exists()) {
				status = sinaWeibo.updateStatus(text);
			} else {
				status = sinaWeibo.uploadStatus(text, file);
			}
			if (status == null) {
				log.info("<SinaWeiboSNS.publishWeibo>: "
						+ "update weibo status is null!");
			}
		} catch (Exception ioe) {
			String logInfo = toLogString(request,
					"fail to publish sina weibo request.");
			SNSRequestManager.logToFile(logInfo);
		}
	}

	@Override
	public String toLogString(SNSRequest request, String reason) {
		// TODO Auto-generated method stub
		if (reason == null || reason.length() < 0)
			reason = "";
		String prefix = "[class=SinaWeiboRequest][reason=" + reason + "]";
		String logInfo = prefix;
		if (request != null)
			logInfo += request.toString();
		return logInfo;
	}
	
	
	public List<Status> getRecentWeibo(String consumerKey, String consumerSecret,SNSRequest request,int num){
		Weibo.CONSUMER_KEY = consumerKey;
		Weibo.CONSUMER_SECRET = consumerSecret;
		
		String tokenKey = request.getTokenKey();
		String tokenSecret = request.getTokenSecret();
		List<Status> userStatus = null;
		try {
			System.setProperty("weibo4j.oauth.consumerKey", Weibo.CONSUMER_KEY);
			System.setProperty("weibo4j.oauth.consumerSecret",
					Weibo.CONSUMER_SECRET);
			Weibo sinaWeibo = new Weibo();

			sinaWeibo.setToken(tokenKey, tokenSecret);
			userStatus = sinaWeibo.getUserTimeline(new Paging(1,num));
						
	
		} catch (Exception ioe) {
			String logInfo = toLogString(request,
					"fail to get sina weibo request.");
			SNSRequestManager.logToFile(logInfo);
		}
		
		return userStatus;
	}
	
	

}
*/
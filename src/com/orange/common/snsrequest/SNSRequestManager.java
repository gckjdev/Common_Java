package com.orange.common.snsrequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.orange.common.utils.DateUtil;

public class SNSRequestManager {

	public BlockingQueue<SNSRequest> snsRequestQueue = new LinkedBlockingQueue<SNSRequest>();
	public static final Logger log = Logger.getLogger(SNSRequestManager.class
			.getName());
	private String QQWeiboConsumerKey = null;
	private String QQWeiboConsumerSecret = null;
	private String SinaWeiboConsumerKey = null;
	private String SinaWeiboConsumerSecret = null;
	private List<Thread> threadList = new ArrayList<Thread>();
	private static String ERROR_LOG_FILE = "sns_sync_error.log";
	private int threadCount = 1;

	public static final long INTERVAL = 1000;
	public static final int MAX_REQUEST_COUNT_PER_INTERVAL = 10;

	public static AtomicLong lasttime = new AtomicLong(0);
	public static AtomicLong requestCount = new AtomicLong(0);

	public SNSRequestManager(int threadCount) {
		super();
		if (threadCount < 1) {
			log.info("<SNSRequestManager.SNSRequestManager(int)>: "
					+ "threadCount < 1, forced to be 1");
			this.threadCount = 1;
		} else {
			this.threadCount = threadCount;
		}
	}

	public SNSRequestManager() {
		super();
	}

	public void start() {
		SNSRequestConsumer consumer = new SNSRequestConsumer(snsRequestQueue,
				this);

		long l = DateUtil.getCurrentTime();

		lasttime.set(DateUtil.getCurrentTime());
		System.out.println(l + "vs" + lasttime.get());
		for (int i = 0; i < threadCount; ++i) {
			Thread thread = new Thread(consumer);
			thread.start();
			threadList.add(thread);
		}
	}

	public static boolean checkConsumerToken(String key, String secret) {
		if (key == null || key.length() <= 0)
			return false;
		if (secret == null || secret.length() <= 0)
			return false;
		return true;
	}

	public static SNS SNSFactory(int snsRequestType,
			BlockingQueue<SNSRequest> snsRequestQueue, SNSRequestManager manager) {
		String key = null;
		String secret = null;
		switch (snsRequestType) {
//		case SNSRequest.REQUEST_Type_QQ_WEIBO:
//			key = manager.getQQWeiboConsumerKey();
//			secret = manager.getQQWeiboConsumerSecret();
//			if (!checkConsumerToken(key, secret))
//				return null;
//			return new QQWeiboSNS(key, secret);
//		case SNSRequest.REQUEST_Type_SINA_WEIBO:
//			key = manager.getSinaWeiboConsumerKey();
//			secret = manager.getSinaWeiboConsumerSecret();
//			if (!checkConsumerToken(key, secret))
//				return null;
//			return new SinaWeiboSNS(key, secret);
		default:
			return null;
		}
	}

	public static SNS SNSFactory(int snsRequestType, SNSRequestManager manager) {
		String key = null;
		String secret = null;
		switch (snsRequestType) {
//		case SNSRequest.REQUEST_Type_QQ_WEIBO:
//			key = manager.getQQWeiboConsumerKey();
//			secret = manager.getQQWeiboConsumerSecret();
//			if (!checkConsumerToken(key, secret))
//				return null;
//			return new QQWeiboSNS(key, secret);
//		case SNSRequest.REQUEST_Type_SINA_WEIBO:
//			key = manager.getSinaWeiboConsumerKey();
//			secret = manager.getSinaWeiboConsumerSecret();
//			if (!checkConsumerToken(key, secret))
//				return null;
//			return new SinaWeiboSNS(key, secret);
		default:
			return null;
		}
	}

	public static void logToFile(String info) {
		try {
			log.info(info);
			FileWriter fileWriter = new FileWriter(ERROR_LOG_FILE, true);
			fileWriter.write(info + "\r\n");
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log
					.info("<SNSRequestManager.logToFile>: fail to log to file. file="
							+ ERROR_LOG_FILE);
			e.printStackTrace();
		}
	}

	private void putSNSRequest(SNSRequest request) throws InterruptedException {
		snsRequestQueue.put(request);
	}

	private SNSRequest getSNSRequest(SNSRequest request) throws InterruptedException {
		return snsRequestQueue.take();
	}
	
	public void putSNSRequest(int snsRequestType, String text, File iamgeFile,
			String tokenKey, String tokenSecret) throws InterruptedException {
		SNSRequest request = new SNSRequest(snsRequestType, text, iamgeFile,
				tokenKey, tokenSecret);
		SNS sns = SNSFactory(snsRequestType, snsRequestQueue, this);
		if (sns == null) {
			log.info("<SNSRequestManager.putSNSRequest>:fail to create sns instance.");
		}
		putSNSRequest(request);
	}

	public void setQQWeiboConsumerKey(String qQWeiboConsumerKey) {
		QQWeiboConsumerKey = qQWeiboConsumerKey;
	}

	public void setQQWeiboConsumerSecret(String qQWeiboConsumerSecret) {
		QQWeiboConsumerSecret = qQWeiboConsumerSecret;
	}

	public void setSinaWeiboConsumerKey(String sinaWeiboConsumerKey) {
		SinaWeiboConsumerKey = sinaWeiboConsumerKey;
	}

	public void setSinaWeiboConsumerSecret(String sinaWeiboConsumerSecret) {
		SinaWeiboConsumerSecret = sinaWeiboConsumerSecret;
	}

	public BlockingQueue<SNSRequest> getSnsRequestQueue() {
		return snsRequestQueue;
	}

	public String getQQWeiboConsumerKey() {
		return QQWeiboConsumerKey;
	}

	public String getQQWeiboConsumerSecret() {
		return QQWeiboConsumerSecret;
	}

	public String getSinaWeiboConsumerKey() {
		return SinaWeiboConsumerKey;
	}

	public String getSinaWeiboConsumerSecret() {
		return SinaWeiboConsumerSecret;
	}

}

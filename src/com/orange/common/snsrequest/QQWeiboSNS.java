/*
package com.orange.common.snsrequest;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import com.mime.qweibo.examples.QWeiboSyncApi;
import com.mime.qweibo.examples.QWeiboType.ResultType;

public class QQWeiboSNS extends SNS {

	public QQWeiboSNS(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void publishWeibo(String consumerKey, String consumerSecret,
			SNSRequest request) {
		if (!SNSRequestManager.checkConsumerToken(consumerKey, consumerSecret)) {
			log.info("<QQWeiboSNS.publishWeibo>: "
					+ "consumerKey and consumerSecret is null or empty!");
			return;
		}
		QWeiboSyncApi api = new QWeiboSyncApi();
		String tokenKey = request.getTokenKey();
		String tokenSecret = request.getTokenSecret();
		String text = request.getText();

		File imageFile = request.getImageFile();
		String imagePath = null;
		if (imageFile != null && imageFile.exists()) {
			imagePath = imageFile.getPath();
		}
		String result = api.publishMsg(consumerKey, consumerSecret, tokenKey,
				tokenSecret, text, imagePath, ResultType.ResultType_Json);
		if (result == null) {
			String logInfo = toLogString(request,
					"fail to publish qq weibo request.");
			SNSRequestManager.logToFile(logInfo);
			return;
		}

	}

	@Override
	public String toLogString(SNSRequest request, String reason) {
		// TODO Auto-generated method stub
		if (reason == null || reason.length() < 0)
			reason = "";
		String prefix = "[class=QQWeiboRequest][reason=" + reason + "]";
		String logInfo = prefix;
		if (request != null)
			logInfo += request.toString();
		return logInfo;
	}

}
*/
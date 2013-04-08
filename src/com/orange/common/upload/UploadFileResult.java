package com.orange.common.upload;

public class UploadFileResult {

	UploadErrorCode errorCode;
	String localPathURL;
	String remotePathURL;
	String relativeURL;
	
	public UploadFileResult(UploadErrorCode errorCode, String localPathURL, String remotePathURL, String relativeURL){
		this.errorCode = errorCode;
		this.localPathURL = localPathURL;
		this.remotePathURL = remotePathURL;
		this.relativeURL = relativeURL;
	}

	public UploadErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(UploadErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public String getLocalPathURL() {
		return localPathURL;
	}

	public void setLocalPathURL(String localPathURL) {
		this.localPathURL = localPathURL;
	}

	public String getRemotePathURL() {
		return remotePathURL;
	}

	public void setRemotePathURL(String remotePathURL) {
		this.remotePathURL = remotePathURL;
	}

	public String getRelativeURL() {
		return relativeURL;
	}

	public void setRelativeURL(String relativeURL) {
		this.relativeURL = relativeURL;
	}
	
	
}

package com.orange.common.upload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import me.prettyprint.cassandra.utils.TimeUUIDUtils;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;

import com.orange.common.api.service.CommonService;
import com.orange.common.utils.FileUtils;
import com.orange.common.utils.ZipUtil;

public class UploadManager {

	static final Logger log = Logger.getLogger(UploadManager.class.getName());
	public static final String DEFAULT_ZIP_FILE_NAME = "data";
    private static final int DEFAULT_THUMB_IMAGE_WIDTH = 500;
    static String ENCODING_UTF8 = "UTF-8";

	public static UploadFileResult uploadFile(HttpServletRequest request,
			String localDir, String remoteDir) {
		String localPath = "";
		String httpPath = "";
		String relativePath = "";
		try {
			request.setCharacterEncoding(ENCODING_UTF8);
			ServletFileUpload upload = new ServletFileUpload();
			// upload.setProgressListener(progressListener);
			if (!ServletFileUpload.isMultipartContent(request)) {
				log
						.info("<uploadFile> the request doesn't contain a multipart/form-data "
								+ "or multipart/mixed stream, content type header is null .");
				return new UploadFileResult(UploadErrorCode.ERROR_NO_MIME_DATA,
						localPath, httpPath, relativePath);
			}

			FileItemIterator iter = upload.getItemIterator(request);
			if (iter == null) {
				log.info("<uploadFile> the item iterator is null.");
				return new UploadFileResult(UploadErrorCode.ERROR_NO_FORM_ITEM,
						localPath, httpPath, relativePath);
			}

			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					log.info("<uploadFile> Form field " + name + " with value "
							+ Streams.asString(stream) + " detected.");
				} else {
					log.info("<uploadFile> File field " + name
							+ " with file name " + item.getName()
							+ " detected.");
					String filename = "";
					if (item.getName() != null) {
						filename = item.getName();
					}
					// Process the input stream
					ArrayList<Integer> byteArray = new ArrayList<Integer>();
					int tempByte;
					do {
						tempByte = stream.read();
						byteArray.add(tempByte);
					} while (tempByte != -1);
					stream.close();

					int size = byteArray.size();
					log.info("<uploadFile> total " + size + " bytes read");

					byteArray.remove(size - 1);
					byte[] bytes = new byte[size];
					int i = 0;
					for (Integer tByte : byteArray) {
						bytes[i++] = tByte.byteValue();
					}

					// generate direcotry
					String timeDir = getTimeFilePath();
					String dir = localDir + timeDir;
					FileUtils.createDir(dir);

					// generate file name
					String generateFileName = getTimeFileName(filename);

					// construction path and write file
					localPath = dir + "/" + generateFileName;

					// construct return http path
					httpPath = remoteDir + timeDir + "/" + generateFileName;
					relativePath = timeDir + "/" + generateFileName;

					// write to file
					log.info("<uploadFile> write to file=" + localPath
							+ ", http path = " + httpPath);
					FileOutputStream fw = new FileOutputStream(localPath);
					fw.write(bytes);
					fw.close();
				}
			}
		} catch (Exception e) {
			log.error("<uploadFile> file path=" + localPath
					+ ", but catch exception=" + e.toString(), e);
			return new UploadFileResult(UploadErrorCode.ERROR_UPLOAD_EXCEPTION,
					"", "", "");
		}

		if (localPath.length() > 0 && httpPath.length() > 0) {
			return new UploadFileResult(UploadErrorCode.ERROR_SUCCESS,
					localPath, httpPath, relativePath);
		} else {
			return new UploadFileResult(UploadErrorCode.ERROR_NO_DATA,
					localPath, httpPath, relativePath);
		}
	}

	private static String createTimeBasedDir(String topDir){
		String timeDir = getTimeFilePath();
		String dir = topDir + timeDir;
		FileUtils.createDir(dir);
		return dir;
	}
	
	private static ParseResult saveImage(FileItemStream item, String localDir,
			String remoteDir) {
		String localPath = "";
		String httpPath = "";
		String filename = "";

		InputStream stream = null;
		FileOutputStream fw = null;
		try {
			stream = item.openStream();

			if (item.getName() != null) {
				filename = item.getName();
			}
			// Process the input stream
			ArrayList<Integer> byteArray = new ArrayList<Integer>();
			int tempByte;
			do {
				tempByte = stream.read();
				byteArray.add(tempByte);
			} while (tempByte != -1);
			stream.close();
			stream = null;

			int size = byteArray.size();
			log.info("<uploadFile> total " + size + " bytes read");

			byteArray.remove(size - 1);
			byte[] bytes = new byte[size];
			int i = 0;
			for (Integer tByte : byteArray) {
				bytes[i++] = tByte.byteValue();
			}
			
			// generate direcotry
			String timeDir = getTimeFilePath();
			String dir = localDir + timeDir;
			FileUtils.createDir(dir);
			
			// generate file name
			String timeFileString = TimeUUIDUtils.getUniqueTimeUUIDinMillis().toString();
			String largeImageName = timeFileString + ".jpg";

			// construction path and write file
			localPath = dir + "/" + largeImageName;

			// construct return http path
			httpPath = remoteDir + timeDir + "/" + largeImageName;

			// write to file
			log.info("<uploadFile> write to file=" + localPath
					+ ", http path = " + httpPath);
			fw = new FileOutputStream(localPath);
			fw.write(bytes);
			fw.close();
			fw = null;

			// create thumb image
			String thumbImageName = timeFileString + "_m.jpg";
			String localThumbPath = dir + "/" + thumbImageName;
			String remoteThumbPath = remoteDir + timeDir + "/" + thumbImageName;
			try {
				// ImageManager.createThumbImage(localPath, localThumbPath, 256,
				// 245);
				String result = ImageManager.createThumbnail(localPath,
						localThumbPath, DEFAULT_THUMB_IMAGE_WIDTH);
				log.info("<UploadManager> create thumb file result=" + result);
			} catch (Exception e) {
				remoteThumbPath = null;
				log.error("<UploadManager>: fail to save thumb image", e);
			}
			ParseResult parseResult = new ParseResult();

			parseResult.setLocalImageUrl(timeDir + "/" + largeImageName);
			parseResult.setLocalThumbUrl(timeDir + "/" + thumbImageName);
			parseResult.setThumbUrl(remoteThumbPath);
			parseResult.setImageUrl(httpPath);

			log.info("<debug> image parse result=" + parseResult.toString());
			return parseResult;

		} catch (Exception e) {
			log.error("error: <saveImage> error, catch exception:", e);
			if (stream != null){
				try {
					stream.close();
				} catch (IOException e1) {
					log.error("error: <saveImage> close stream error", e1);
				}
			}
			
			if (fw != null){
				try {
					fw.close();
				} catch (IOException e1) {
					log.error("error: <saveImage> close file stream error", e1);
				}
			}
			
			return null;
		}

	}

	public static ParseResult readFormData(
			HttpServletRequest request, 		
			String metaDataFieldName,
			String dataFieldName,
			String imageFieldName, 
			String uploadDataType,
			String localImageDir, 
			String remoteImageDir,
			String localDataDir,
			String remoteDataDir,
			boolean zipDataFile
			) {
		
		try {
			ParseResult result = new ParseResult();
			request.setCharacterEncoding(ENCODING_UTF8);
			ServletFileUpload upload = new ServletFileUpload();
			if (!ServletFileUpload.isMultipartContent(request)) {
				log.info("<readFormData> the request doesn't contain a multipart/form-data or multipart/mixed stream, content type header is null .");
				return null;
			}

			FileItemIterator iter = upload.getItemIterator(request);
			if (iter == null) {
				log.info("<readFormData> the item iterator is null.");
				return null;
			}

			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (!item.isFormField() && name != null) {
					if (metaDataFieldName != null && name.equalsIgnoreCase(metaDataFieldName)){
						log.info("<readFormData> meta data detected "+name);						
						byte[] data = CommonService.readPostData(stream);
						result.setMetaData(data)	;	
						if (data != null){
							log.info("<readFormData> meta data total "+data.length+" read.");							
						}
					}
					else if (dataFieldName != null && name.equalsIgnoreCase(dataFieldName)) {						
						log.info("<readFormData> data file detected "+name);

						String timeDir = getTimeFilePath();
						String dir = localDataDir + timeDir;
						FileUtils.createDir(dir);
						
						String timeFileString = TimeUUIDUtils.getUniqueTimeUUIDinMillis().toString();
						String dataFilePath = null;
						
						int dataLen = 0;						
						if (zipDataFile){
							// write data and create zip file
							String zipFileName = DEFAULT_ZIP_FILE_NAME;			// IMPORTANT, THIS MUST ALIGN WITH CLIENT
							dataFilePath = dir+"/"+timeFileString+".zip";								
							dataLen = ZipUtil.createZipFile(dataFilePath, zipFileName, stream);

							String localZipFileUrl = timeDir + "/" + timeFileString+".zip"; 
							String remoteZipFileUrl = remoteDataDir + localZipFileUrl;
							
							result.setDataLen(dataLen);
							result.setLocalZipFileUrl(localZipFileUrl);
							result.setRemoteZipFileUrl(remoteZipFileUrl);						
						}
						else{
							String dataType = "."+uploadDataType;
							
							// write data as file directly
							dataFilePath = dir+"/"+timeFileString+dataType;											
							dataLen = writeToFile(dataFilePath, stream);
							
							String localDataFileUrl = timeDir + "/" + timeFileString+dataType; 
							String remoteDataFileUrl = remoteDataDir + localDataFileUrl;
							
							result.setDataLen(dataLen);
							result.setLocalZipFileUrl(localDataFileUrl);
							result.setRemoteZipFileUrl(remoteDataFileUrl);						
						}
						
						
					} else if (imageFieldName != null && name.equalsIgnoreCase(imageFieldName)) {
						log.info("<readFormData> image data file detected for "+imageFieldName);
						ParseResult pr = saveImage(item, localImageDir, remoteImageDir);

						if (pr != null) {
							result.setImageUrl(pr.getImageUrl());
							result.setThumbUrl(pr.getThumbUrl());
							result.setLocalImageUrl(pr.getLocalImageUrl());
							result.setLocalThumbUrl(pr.getLocalThumbUrl());
						}
					}
					else{
						log.info("<readFormData> unknown form fields "+name);						
					}
				}
			}
			return result;
		} catch (Exception e) {
			log.error("<readFormData> catch exception=" + e.toString(), e);
			return null;
		}

	}
	
	public static ParseResult getFormDataAndSaveImage(
			HttpServletRequest request, 
			String dataFieldName,
			String imageFieldName, 
			String localImageDir, 
			String remoteImageDir,
			boolean isReturnByteData,
			boolean isDataZip,
			boolean isDataCompressed, 
			String localDataDir,
			String remoteDataDir) {
		try {
			ParseResult result = new ParseResult();
			request.setCharacterEncoding(ENCODING_UTF8);
			ServletFileUpload upload = new ServletFileUpload();
			if (!ServletFileUpload.isMultipartContent(request)) {
				log
						.info("<getFormDataAndSaveImage> the request doesn't contain a multipart/form-data "
								+ "or multipart/mixed stream, content type header is null .");
				return null;
			}

			FileItemIterator iter = upload.getItemIterator(request);
			if (iter == null) {
				log.info("<getFormDataAndSaveImage> the item iterator is null.");
				return null;
			}

			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (!item.isFormField()) {
					if (name != null && dataFieldName != null
							&& name.equalsIgnoreCase(dataFieldName)) {
						
						log.info("<getFormDataAndSaveImage> data file detected "+dataFieldName);
						if (isReturnByteData){
							byte[] data = CommonService.readPostData(stream);
							result.setData(data);
						}
						else{
							String timeDir = getTimeFilePath();
							String dir = localDataDir + timeDir;
							FileUtils.createDir(dir);
							
							String timeFileString = TimeUUIDUtils.getUniqueTimeUUIDinMillis().toString();
							String zipFilePath = null;
							if (isDataCompressed){
								zipFilePath = dir+"/"+timeFileString+"_c.zip";
							}
							else{
								zipFilePath = dir+"/"+timeFileString+".zip";								
							}
							
							String zipFileName = DEFAULT_ZIP_FILE_NAME;			// IMPORTANT, THIS MUST ALIGN WITH CLIENT
							int dataLen = 0;
							
							if (isDataZip){
								// write data as zip file directly
								dataLen = writeToFile(zipFilePath, stream);
							}
							else{
								// write data and create zip file
								dataLen = ZipUtil.createZipFile(zipFilePath, zipFileName, stream);
							}
							
							String localZipFileUrl = timeDir + "/" + timeFileString+".zip"; 
							String remoteZipFileUrl = remoteDataDir + localZipFileUrl;
							
							result.setDataLen(dataLen);
							result.setLocalZipFileUrl(localZipFileUrl);
							result.setRemoteZipFileUrl(remoteZipFileUrl);
						}
						
					} else if (name != null
							&& name.equalsIgnoreCase(imageFieldName)) {
						log
								.info("<getFormDataAndSaveImage> image data file detected.");
						ParseResult pr = saveImage(item, localImageDir, remoteImageDir);

						if (pr != null) {
							result.setImageUrl(pr.getImageUrl());
							result.setThumbUrl(pr.getThumbUrl());
							result.setLocalImageUrl(pr.getLocalImageUrl());
							result.setLocalThumbUrl(pr.getLocalThumbUrl());
						}
					}
				}
			}
			return result;
		} catch (Exception e) {
			log.error(
					"<getFormDataValueByField> fail to parse data and save image"
							+ ", but catch exception=" + e.toString(), e);
			return null;
		}

	}

	private static int writeToFile(String filePath, InputStream stream) {

		int BUFFER = 2048;
		byte data[] = new byte[BUFFER];
		int dataLen = 0;
		
		BufferedInputStream origin = null;

		FileOutputStream fw = null;
		BufferedOutputStream out = null;
		
		boolean result = true;
		
		try {
			fw = new FileOutputStream(filePath);
			out = new BufferedOutputStream(fw);
			
			origin = new BufferedInputStream(stream, BUFFER);  
			int count = -1;  
			int totalSize = 0;
	        while ((count = origin.read(data, 0, BUFFER)) != -1) {  
	        	totalSize += count;
	        	out.write(data, 0, count);  
	        }

            out.flush();
            out.close();
            out = null;

//            long fileSize = FileUtils.fileSize(filePath);
//			log.info("<writeToFile> total " + totalSize + " bytes read, write to "+filePath+" success, fileSize= "+fileSize);
            log.info("<writeToFile> total " + totalSize + " bytes read, write to "+filePath+" success");
			dataLen = totalSize;
		} catch (Exception e) {
			log.error("<writeToFile> error, catch exception:", e);
			result = false;
		}
		finally{
			if (stream != null){
				try {
					stream.close();
				} catch (IOException e1) {
					log.error("<writeToFile> close stream error", e1);
				}
			}
			
			if (fw != null){
				try {
					fw.close();
				} catch (IOException e1) {
					log.error("<writeToFile> close file stream error", e1);
				}
			}
			
			try {
				if (origin != null){
					origin.close();					
				}
			} catch (IOException e) {
				log.error("<writeToFile> close stream error", e);
			}
			
			try {
				if (out != null){
					out.close();
				}
			} catch (IOException e) {
				log.error("<writeToFile> close stream error", e);
			}			
		}		
		
		return dataLen;
	}

	private static String getTimeFileName(String filename) {
		return TimeUUIDUtils.getUniqueTimeUUIDinMillis().toString() + ".jpg";
	}

	private static String getTimeFilePath() {
		Date now = new Date();
		return getTimeFilePath(now);
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
//		String datePath = formatter.format(now);
//		return datePath;
	}

	public static String getTimeFilePath(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String datePath = formatter.format(date);
		return datePath;
	}	
	
	// Create a progress listener
	ProgressListener progressListener = new ProgressListener() {
		public void update(long pBytesRead, long pContentLength, int pItems) {
			System.out.println("We are currently reading item " + pItems);
			if (pContentLength == -1) {
				System.out.println("So far, " + pBytesRead
						+ " bytes have been read.");
			} else {
				System.out.println("So far, " + pBytesRead + " of "
						+ pContentLength + " bytes have been read.");
			}
		}
	};
	
	public static class ParseDataResult{
		
		int returnDataType;
		
		// for data file, e.g. draw data
		String dataLocalUrl;
		String dataRemoteUrl;
		
		// if it's image, there are thumb image created
		String thumbLocalUrl;
		String thumbRemoteUrl;

		// return byte data
		byte[] byteData;

		int dataLen;
		
	}

	public static class ParseResult {
		private String imageUrl;
		private String thumbUrl;
		private String localImageUrl;		// relative URL without full path
		private String localThumbUrl;		// relative URL without full path
		
		private String localZipFileUrl;		// both zip file and non-zip file will use this field
		private String remoteZipFileUrl;	

		private byte[] data;					// this field is used for old implementation
		private int dataLen;
		
		private byte[] metaData;		
		
		public byte[] getMetaData() {
			return metaData;
		}

		public void setMetaData(byte[] metaData) {
			this.metaData = metaData;
		}

		public int getDataLen() {
			return dataLen;
		}

		public void setDataLen(int dataLen) {
			this.dataLen = dataLen;
		}

		public ParseResult() {
			super();
		}

		public ParseResult(String url, byte[] data) {
			super();
			this.imageUrl = url;
			this.setData(data);
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public void setData(byte[] data) {
			this.data = data;
			if (data != null){
				this.setDataLen(data.length);
			}
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public byte[] getData() {
			return data;
		}

		public String getThumbUrl() {
			return thumbUrl;
		}

		public void setThumbUrl(String thumbUrl) {
			this.thumbUrl = thumbUrl;
		}

		public String getLocalImageUrl() {
			return localImageUrl;
		}

		public void setLocalImageUrl(String localImageUrl) {
			this.localImageUrl = localImageUrl;
		}

		public String getLocalThumbUrl() {
			return localThumbUrl;
		}

		public void setLocalThumbUrl(String localThumbUrl) {
			this.localThumbUrl = localThumbUrl;
		}

		public String getLocalZipFileUrl() {
			return localZipFileUrl;
		}

		public void setLocalZipFileUrl(String localDataFileUrl) {
			this.localZipFileUrl = localDataFileUrl;
		}

		public String getRemoteZipFileUrl() {
			return remoteZipFileUrl;
		}

		public void setRemoteZipFileUrl(String remoteDataFileUrl) {
			this.remoteZipFileUrl = remoteDataFileUrl;
		}
		
		public String getLocalDataFileUrl() {
			return localZipFileUrl;
		}

		public void setLocalDataFileUrl(String localDataFileUrl) {
			this.localZipFileUrl = localDataFileUrl;
		}

		public String getRemoteDataFileUrl() {
			return remoteZipFileUrl;
		}

		public void setRemoteDataFileUrl(String remoteDataFileUrl) {
			this.remoteZipFileUrl = remoteDataFileUrl;
		}

		@Override
		public String toString() {
			return "ParseResult [imageUrl=" + imageUrl + ", thumbUrl="
					+ thumbUrl + ", localImageUrl=" + localImageUrl
					+ ", localThumbUrl=" + localThumbUrl + ", localZipFileUrl="
					+ localZipFileUrl + ", remoteZipFileUrl="
					+ remoteZipFileUrl + ", dataLen=" + dataLen + "]";
		}

	}
}

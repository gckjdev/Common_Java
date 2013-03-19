package com.orange.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.elasticsearch.common.jackson.core.util.ByteArrayBuilder;

public class ZipUtil {
	
	static final int BUFFER = 2048;
	public static final Logger log = Logger.getLogger(ZipUtil.class.getName());	

	public static int createZipFile(String zipFilePath, String zipFileName,
			InputStream inputStream) {

		BufferedInputStream origin = null;
		FileOutputStream dest = null;
		ZipOutputStream out = null;
		int dataLen = 0;

		try {

			if (zipFileName == null || zipFilePath == null) {
				log.warn("<createZipFile> but input parameters null");				
				return 0;
			}

			dest = new FileOutputStream(zipFilePath);
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			
			byte data[] = new byte[BUFFER];
			origin = new BufferedInputStream(inputStream, BUFFER);
			
			ZipEntry entry = new ZipEntry(zipFileName);
			out.putNextEntry(entry);
			
			int count = -1;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
				dataLen += count;
			}

			log.info("<createZipFile> success, path="+zipFilePath+" , data length="+dataLen);				
		} catch (Exception e) {
			log.error("<createZipFile> catch exception="+e.toString(), e);
		}
		finally{			
			if (origin != null){
				try {
					origin.close();
				} catch (IOException e) {
					log.error("<createZipFile> catch exception="+e.toString(), e);
				}
			}
			
			if (out != null){
				try {
					out.close();
				} catch (IOException e) {
					log.error("<createZipFile> catch exception="+e.toString(), e);
				}
			}
			
			if (dest != null){
				try {
					dest.close();
				} catch (IOException e) {
					log.error("<createZipFile> catch exception="+e.toString(), e);
				}
			}
			
			if (inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("<createZipFile> catch exception="+e.toString(), e);
				}				
			}
		}
		
		return dataLen;
	}
	
	public static int createZipFile(String zipFilePath, String zipFileName, byte[] inputByteData) {

		FileOutputStream dest = null;
		ZipOutputStream out = null;
		int dataLen = 0;

		try {

			if (zipFileName == null || zipFilePath == null) {
				log.warn("<createZipFile> but input parameters null");				
				return 0;
			}

			dest = new FileOutputStream(zipFilePath);
			out = new ZipOutputStream(new BufferedOutputStream(dest));
						
			ZipEntry entry = new ZipEntry(zipFileName);
			out.putNextEntry(entry);
			
			out.write(inputByteData);
			dataLen = inputByteData.length;			

			log.info("<createZipFile> success, path="+zipFilePath+" , data length="+dataLen);				
		} catch (Exception e) {
			log.error("<createZipFile> catch exception="+e.toString(), e);
		}
		finally{			
			if (out != null){
				try {
					out.close();
				} catch (IOException e) {
					log.error("<createZipFile> catch exception="+e.toString(), e);
				}
			}
			
			if (dest != null){
				try {
					dest.close();
				} catch (IOException e) {
					log.error("<createZipFile> catch exception="+e.toString(), e);
				}
			}			
		}
		
		return dataLen;
	}
	
	public static byte[] unzipFile(String fileName){
        ZipFile zipFile = null;
        byte[] byteData = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
		try {
			zipFile = new ZipFile(fileName);
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while(emu.hasMoreElements()){
                ZipEntry entry = (ZipEntry)emu.nextElement();
                if (entry.isDirectory())
                {
                    continue;
                }

                is = zipFile.getInputStream(entry);
                bis = new BufferedInputStream(is);

				int tempByte;
				do {
					tempByte = bis.read();
					if (tempByte != -1){
						byteArrayBuilder.append(tempByte);
					}
				} while (tempByte != -1);
				byteData = byteArrayBuilder.toByteArray();

				// close all streams
				bis.close(); 
				bis = null;
				
				is.close();
				is = null;
				
				byteArrayBuilder.close();
				byteArrayBuilder = null;
				break;
            }
            zipFile.close();
            zipFile = null;
        } catch (Exception e) {
			log.error("<unzipFile> file="+fileName+", catch exception="+e.toString(), e);
        }		
		finally{
			// close all streams
			if (bis != null){
				try {
					bis.close();
				} catch (IOException e) {
					log.error("<unzipFile> file="+fileName+", catch exception="+e.toString(), e);
				} 
				bis = null;
			}
			
			if (is != null){
				try {
					is.close();
				} catch (IOException e) {
					log.error("<unzipFile> file="+fileName+", catch exception="+e.toString(), e);
				}
				is = null;
			}
			
			if (byteArrayBuilder != null){
				byteArrayBuilder.close();
				byteArrayBuilder = null;
			}
			
			if (zipFile != null){
				try {
					zipFile.close();
				} catch (IOException e) {
					log.error("<unzipFile> file="+fileName+", catch exception="+e.toString(), e);
				}
				zipFile = null;
			}
        }
			
		int byteDataLen = (byteData == null) ? 0 : byteData.length;
		log.info("<unzipFile> file="+fileName+", return byte data length="+byteDataLen);
		return byteData;
	}
	
	
	
}

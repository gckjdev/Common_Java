package com.orange.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class FileUtils {

	public static final Logger log = Logger.getLogger(FileUtils.class
			.getName());
	
	public static String stringFromFile(File file) {

		if (file == null || !file.exists()) {
			return null;
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			StringBuilder builder = new StringBuilder();
			String str = null;
			while ((str = in.readLine()) != null) {
				builder.append(str);
			}
			in.close();
			return builder.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static void createDir(String dir) {
		if (dir == null || dir.isEmpty())
			return;
		
		File path = new File(dir);
		if (!path.exists()) {
			path.mkdirs();
		}
	}

	public static void delFileOrDir(File f) throws IOException {

		if ((f == null)) {
			throw new IllegalArgumentException("Argument " + f
					+ " is not a file or directory. ");
		}
		if (f.exists() && f.isDirectory()) {
			if (f.listFiles().length == 0) {
				f.delete();
			} else {
				File delFiles[] = f.listFiles();
				for (File subf : delFiles) {
					if (subf.isDirectory()) {
						delFileOrDir(new File(subf.getAbsolutePath()));// Recursive
					}
					subf.delete();
				}
				f.delete();
			}
		}
	}
	
	public static int writeToFile(String filePath, byte[] byteData) {

		if (byteData == null)
			return 0;
		
		int retDataLen = 0;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			fos.write(byteData);
			retDataLen = byteData.length;
			log.info("<writeToFile> sucess! file="+filePath+", len="+retDataLen);
			
		} catch (Exception e) {
			log.error("<writeToFile> catch exception="+e.toString(), e);
		}
		finally{
			if (fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					log.error("<writeToFile> catch exception="+e.toString(), e);
				}		
			}
		}

		return retDataLen;
	}	
}

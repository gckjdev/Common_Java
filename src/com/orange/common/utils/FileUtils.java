package com.orange.common.utils;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class FileUtils {

	public static final Logger log = Logger.getLogger(FileUtils.class
			.getName());
	
	public static String stringFromFile(File file) {

		if (file == null || !file.exists()) {
            log.error("<stringFromFile> but file is null or file not exists!");
			return null;
		}

        BufferedReader in = null;
		try {
            in = new BufferedReader(new FileReader(file));
			StringBuilder builder = new StringBuilder();
			String str = null;
			while ((str = in.readLine()) != null) {
				builder.append(str);
                builder.append("\n");
			}
			in.close();
            in = null;
			return builder.toString();
		} catch (Exception e) {
            log.error("<stringFromFile> catch exception="+e.toString(), e);
            if (in != null){
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
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

	public static boolean isFileExists(String path) {
		File file = new File(path);
		return file.exists();
	}

    public static boolean copyFile(String src, String des)
    {
        FileInputStream FIS = null;
        FileOutputStream FOS = null;
        try
        {
            FIS = new FileInputStream(src);
            FOS = new FileOutputStream(des);
            byte[] bt = new byte[1024];
            int readNum = 0;
            while ((readNum = FIS.read(bt)) != -1)
            {
                FOS.write(bt, 0, bt.length);
            }
            FIS.close();
            FOS.close();
            log.error("<copyFile> from "+src+" to "+des+" success");
            return true;
        }
        catch (Exception e)
        {
            try
            {
                if (FIS != null){
                    FIS.close();
                }

                if (FOS != null){
                    FOS.close();
                }
            }
            catch (IOException f)
            {
                log.error("<copyFile> from "+src+" to "+des+", catch exception="+e.toString(), e);
            }
            return false;
        }
        finally
        {
        }
    }

    public static String combine (String path1, String path2)
    {
        // TODO this can be resolved better by using commons-io  FilenameUtils.concat(path1, path2);
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    public static boolean copyFileToDir(String path, String exportDir) {

        File srcFile = new File(path);
        if (srcFile == null || !srcFile.exists() || srcFile.isDirectory()){
            log.warn("<copyFileToDir> but srcFile "+path+" is null or not exists or is directory");
            return false;
        }

        String destPath = combine(exportDir, srcFile.getName());
        return copyFile(path, destPath);

    }

    public static long fileSize(String filePath) {
        File file = new File(filePath);
        return file.length();
    }

    public static String getFileMD5String(File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream((int)file.length());
            byte[] cache = new byte[1048576];
            for(int i = in.read(cache);i != -1;i = in.read(cache)){
                out.write(cache, 0, i);
            }
            in.close();
            String ret = StringUtil.md5base64encode(out.toByteArray());
            out.close();
            return ret;
        } catch (Exception e) {
            log.error("<getFileMD5String> but catch exception = "+e.toString(), e);
            return null;
        }
    }
}

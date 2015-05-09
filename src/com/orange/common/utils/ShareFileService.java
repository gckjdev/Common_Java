package com.orange.common.utils;

import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 15-5-9
 * Time: 下午2:10
 * To change this template use File | Settings | File Templates.
 */
public class ShareFileService {

    public static final Logger log = Logger.getLogger(ShareFileService.class.getName());

    private static final String SHARE_FILE_KEY = "share_bg_file_key";
    private static final String SHARE_FILE_DIR = PropertyUtil.getStringProperty("upload.shareBgImage", "bg_image");
    private static final String SHARE_FILE_PATH = PropertyUtil.getStringProperty("upload.local.drawImage", "/data/draw_image/") + SHARE_FILE_DIR + "/";
    private static ShareFileService ourInstance = new ShareFileService();
    private ConcurrentHashMap<String, String> shareFileMap = null;

    public static ShareFileService getInstance() {
        return ourInstance;
    }

    private ShareFileService() {
        loadShareFiles();
    }

    public String getShareDir() {
        return SHARE_FILE_DIR;
    }

    private void loadShareFiles() {

        log.info("<loadShareFiles> share file path = "+SHARE_FILE_PATH+", dir = "+SHARE_FILE_DIR);

        shareFileMap = new ConcurrentHashMap<String, String>();

        // read files from dir and store into redis
        File dir = new File(SHARE_FILE_PATH);
        File[] file = dir.listFiles();//存放的是一级目录下的文件以及文件夹
        if (file == null){
            return;
        }

        for (int i = 0; i < file.length; i++) {
            if (file[i].isDirectory()){
                continue;
            }
            else{
                String value = file[i].getName();
                String key = FileUtils.getFileMD5String(file[i]);
                shareFileMap.put(key, value);
            }
        }

        log.info("<ShareFileMap> "+shareFileMap.toString());
    }

    public String getAndUpdateShareMap(byte[] data){

        if (data == null || data.length == 0){
            return null;
        }

        String key = StringUtil.md5base64encode(data);
        String path = null;
        if (shareFileMap.containsKey(key)){
            path = shareFileMap.get(key);
            log.info("<writeAndUpdateShareMap> file "+path+" found for key +"+key);
            return path;
        }
        else{
            // not found
            String timeFileString = TimeUUIDUtils.getUniqueTimeUUIDinMillis().toString();
            path = timeFileString + ".jpg";

            // update map firstly
            shareFileMap.put(key, path);

            // write to file
            FileOutputStream fw = null;
            String fullPath = SHARE_FILE_PATH + path;
            log.info("<writeAndUpdateShareMap> write to file=" + fullPath +", key="+key);
            try {
                fw = new FileOutputStream(fullPath);
                fw.write(data);
                fw.close();
            } catch (Exception e) {
                log.error("<writeAndUpdateShareMap> but catch exception = " + e.toString(), e);
            }
            fw = null;
            log.info("<writeAndUpdateShareMap> write to file " + path + " done");
            return path;
        }


    }

}

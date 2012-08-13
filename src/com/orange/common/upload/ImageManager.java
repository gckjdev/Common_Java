package com.orange.common.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Image;

import com.orange.common.utils.StringUtil;
import com.sun.image.codec.jpeg.*;
import javax.imageio.ImageIO;

public class ImageManager {

	/*
	private static String getThumbImagePath(String imagePath) {
		String thumbPath = null;
		int index = imagePath.lastIndexOf(".");
		if (index != -1) {
			thumbPath = imagePath.substring(0, index) + "_m"
					+ imagePath.substring(index, imagePath.length());
		} else {
			thumbPath = imagePath + "_m";
		}
		return thumbPath;
	}
	*/
	private static boolean createThumbImage(Image bigJpg, String thumbPath,
			int width, int height) throws Exception {
		BufferedImage tag = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		tag.getGraphics().drawImage(bigJpg, 0, 0, width, height, null);
		FileOutputStream newimage = new FileOutputStream(thumbPath);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
		encoder.encode(tag);
		newimage.close();
		return true;
	}

	public static boolean createThumbImage(String imagePath, String thumbPath,
			int width, int height) throws Exception {
		if (StringUtil.isEmpty(imagePath) || StringUtil.isEmpty(thumbPath)) {
			return false;
		}
		Image bigJpg = ImageIO.read(new java.io.File(imagePath));
		if (bigJpg != null) {
			boolean flag = createThumbImage(bigJpg, thumbPath, width, height);
			bigJpg.flush();
			return flag;
		}
		return false;
	}

	public static boolean createThumbImage(String imagePath, String thumbPath,
			float scale) throws Exception {
		if (scale <= 0) {
			return false;
		}
		if (StringUtil.isEmpty(imagePath) || StringUtil.isEmpty(thumbPath)) {
			return false;
		}
		Image bigJpg = ImageIO.read(new java.io.File(imagePath));		
		if (bigJpg != null) {
			int height = bigJpg.getHeight(null);
			int width = bigJpg.getWidth(null);
			if (height < 0 || width < 0) {
				return false;
			}
			height *= scale;
			width *= scale;
			
			boolean flag = createThumbImage(bigJpg, thumbPath, width, height);
			bigJpg.flush();
			return flag;
		}
		return false;

	}
}

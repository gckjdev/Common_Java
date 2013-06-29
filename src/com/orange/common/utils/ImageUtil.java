package com.orange.common.utils;

//import java.awt.Image;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import javax.imageio.ImageIO;
//
////import com.sun.image.codec.jpeg.JPEGCodec;
////import com.sun.image.codec.jpeg.JPEGImageEncoder;
//
//public class ImageUtil {
//
//	private static String SmallImageSuffix = "_s.png";
//
//	private static String getSmallImageName(File imageFile) {
//		if (imageFile == null)
//			return null;
//		String imageName = imageFile.getAbsolutePath();
//		int index = imageName.lastIndexOf(".");
//		if (index < 0)
//			return null;
//		String smallImageName = imageName.substring(0, index)
//				+ SmallImageSuffix;
//		return smallImageName;
//	}
//
//	public static String getSmallImagePath(String httpPath){
//		int index = httpPath.lastIndexOf(".");
//		if (index < 0)
//			return null;
//		String smallImageName = httpPath.substring(0, index)
//				+ SmallImageSuffix;
//		return smallImageName;
//	}
//	
//	public static File setImageSize(File imageFile, int height){
//		Image src = null;
//		File outFile = null;
//		try {
//			src = ImageIO.read(imageFile);
//			int oWidth = src.getWidth(null);
//			int oHeight = src.getHeight(null);
//			int width = oWidth * height / oHeight;
//			String name = getSmallImageName(imageFile);
//			outFile = setImageSize(imageFile, width, height, name);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//		return outFile;
//	}
//	public static File setImageSize(File imageFile, double scale) {
//		Image src = null;
//		File outFile = null;
//		try {
//			src = ImageIO.read(imageFile);
//			int width = src.getWidth(null);
//			int height = src.getHeight(null);
//			width = (int) Math.round(width * scale);
//			height = (int) Math.round(height * scale);
//			String name = getSmallImageName(imageFile);
//			outFile = setImageSize(imageFile, width, height, name);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//		return outFile;
//	}
//
//	public static File setImageSize(File imageFile, int width, int height,
//			String outImageName) {
//		Image src = null;
//		File outFile = null;
//		try {
//			src = ImageIO.read(imageFile);
//
//			BufferedImage tag = new BufferedImage(width, height,
//					BufferedImage.TYPE_INT_RGB);
//			tag.getGraphics().drawImage(src, 0, 0, width, height, null); 
//			outFile = new File(outImageName);
//			FileOutputStream newimage = new FileOutputStream(outFile);
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
//			encoder.encode(tag); 
//			newimage.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return outFile;
//	}
//}

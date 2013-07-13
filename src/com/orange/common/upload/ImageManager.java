package com.orange.common.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import com.orange.common.utils.StringUtil;
//import com.sun.image.codec.jpeg.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;

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

    /*
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
	*/

	/**
	 * Write a JPEG file setting the compression quality.
	 * 
	 * @param image
	 *                a BufferedImage to be saved
	 * @param destFile
	 *                destination file (absolute or relative path)
	 * @param quality
	 *                a float between 0 and 1, where 1 means uncompressed.
	 * @throws IOException
	 *                 in case of problems writing the file
	 */
	private static void writeJpeg(BufferedImage image, String destFile, float quality) throws IOException {
	    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
	    ImageWriteParam param = writer.getDefaultWriteParam();
	    FileImageOutputStream output = new FileImageOutputStream(new File(destFile));
	    try{
		    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		    param.setCompressionQuality(quality);
		    writer.setOutput(output);
		    IIOImage iioImage = new IIOImage(image, null, null);
		    writer.write(null, iioImage, param);
	    }
	    catch (Exception e){
	    	e.printStackTrace();
	    }
	    finally{
	    	writer.dispose();
	    	output.close();
	    }
	}
	
	/**
	* Reads an image in a file and creates a thumbnail in another file.
	*/
	public static String createThumbnail(String inFilename, String outFilename, int largestDimension)
	{
		try
		{
			double scale;
			int sizeDifference, originalImageLargestDim;
			if(!inFilename.endsWith(".jpg") && !inFilename.endsWith(".jpeg") && !inFilename.endsWith(".gif") && !inFilename.endsWith(".png"))
			{
				return "Error: Unsupported image type, please only either JPG, GIF or PNG";
			}
			else
			{
				Image inImage = ImageIO.read(new java.io.File(inFilename));
				if(inImage.getWidth(null) == -1 || inImage.getHeight(null) == -1)
				{
					return "Error loading file: \"" + inFilename + "\"";
				}
				else
				{
					//find biggest dimension	    
					if(inImage.getWidth(null) > inImage.getHeight(null))
					{
						scale = (double)largestDimension/(double)inImage.getWidth(null);
						sizeDifference = inImage.getWidth(null) - largestDimension;
						originalImageLargestDim = inImage.getWidth(null);
					}
					else
					{
						scale = (double)largestDimension/(double)inImage.getHeight(null);
						sizeDifference = inImage.getHeight(null) - largestDimension;
						originalImageLargestDim = inImage.getHeight(null);
					}
					//create an image buffer to draw to
					BufferedImage outImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB); //arbitrary init so code compiles
					Graphics2D g2d;
					AffineTransform tx;
					if(scale < 1.0d) //only scale if desired size is smaller than original
					{
						int numSteps = sizeDifference / 100;
						if (numSteps == 0){
							numSteps = 1;
						}
						
						int stepSize = sizeDifference / numSteps;
						int stepWeight = stepSize/2;
						int heavierStepSize = stepSize + stepWeight;
						int lighterStepSize = stepSize - stepWeight;
						int currentStepSize, centerStep;
						double scaledW = inImage.getWidth(null);
						double scaledH = inImage.getHeight(null);
						if(numSteps % 2 == 1) //if there's an odd number of steps
							centerStep = (int)Math.ceil((double)numSteps / 2d); //find the center step
						else
							centerStep = -1; //set it to -1 so it's ignored later
						Integer intermediateSize = originalImageLargestDim, previousIntermediateSize = originalImageLargestDim;
						Integer calculatedDim;
						for(Integer i=0; i<numSteps; i++)
						{
							if(i+1 != centerStep) //if this isn't the center step
							{
								if(i == numSteps-1) //if this is the last step
								{
									//fix the stepsize to account for decimal place errors previously
									currentStepSize = previousIntermediateSize - largestDimension;
								}
								else
								{
									if(numSteps - i > numSteps/2) //if we're in the first half of the reductions
										currentStepSize = heavierStepSize;
									else
										currentStepSize = lighterStepSize;
								}
							}
							else //center step, use natural step size
							{                        
								currentStepSize = stepSize;
							}
							intermediateSize = previousIntermediateSize - currentStepSize;
							scale = (double)intermediateSize/(double)previousIntermediateSize;
							scaledW = (int)scaledW*scale;
							scaledH = (int)scaledH*scale;
							outImage = new BufferedImage((int)scaledW, (int)scaledH, BufferedImage.TYPE_INT_RGB);
							g2d = outImage.createGraphics();
							g2d.setBackground(Color.WHITE);
							g2d.clearRect(0, 0, outImage.getWidth(), outImage.getHeight());
							g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
							g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
							tx = new AffineTransform();
							tx.scale(scale, scale);
							g2d.drawImage(inImage, tx, null);
							g2d.dispose();
							inImage = new ImageIcon(outImage).getImage();
							previousIntermediateSize = intermediateSize;
						}                
					}
					else
					{
						//just copy the original
						outImage = new BufferedImage(inImage.getWidth(null), inImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
						g2d = outImage.createGraphics();
						g2d.setBackground(Color.WHITE);
						g2d.clearRect(0, 0, outImage.getWidth(), outImage.getHeight());
						tx = new AffineTransform();
						tx.setToIdentity(); //use identity matrix so image is copied exactly
						g2d.drawImage(inImage, tx, null);
						g2d.dispose();
					}
					
					/*
					//JPEG-encode the image and write to file.
					OutputStream os = new FileOutputStream(outFilename);
					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
					
					encoder.encode(outImage);
					os.close();
					*/
					writeJpeg(outImage, outFilename, 0.9f);
				}
			}
		}
		catch(Exception ex)
		{
			String errorMsg = "";
			errorMsg += "<br>Exception: " + ex.toString();
			errorMsg += "<br>Cause = " + ex.getCause();
			errorMsg += "<br>Stack Trace = ";
			StackTraceElement stackTrace[] = ex.getStackTrace();
			for(int traceLine=0; traceLine<stackTrace.length; traceLine++)
			{
				errorMsg += "<br>" + stackTrace[traceLine];
			}
			return errorMsg;
		}
		return ""; //success
	}	

    /*
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
	*/

    /*
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
	*/
}

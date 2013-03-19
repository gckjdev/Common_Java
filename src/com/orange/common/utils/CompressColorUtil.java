package com.orange.common.utils;

public class CompressColorUtil {

	
	// new compress, with 8 bits for alpha
	public static int compressColor8WithRed(float red, float green, float blue, float alpha){
	    int ret = (int)(alpha * 255.0f) +
	                     ((int)(blue * 255.0f) << 8) +
	                     ((int)(green * 255.0f) << 16) +
	                     ((int)(red * 255.0f) << 24);
	    return ret;
	}
	
	// old compress, with 6 bits for alpha
	public static int compressColor6WithRed(float red, float green, float blue, float alpha){
	    int ret = (int)(alpha * 63.0f) +
	                     ((int)(blue * 255.0f) << 6) +
	                     ((int)(green * 255.0f) << 14) +
	                     ((int)(red * 255.0f) << 22);
	    return ret;
	}

	public static float getRedFromColor8(int intColor){
		return ((intColor >> 24) % (1<<8)) / 255.0f;
	}

	public static float getGreenFromColor8(int intColor){
		return  ((intColor >> 16) % (1<<8)) / 255.0f;
	}
	
	public static float getBlueFromColor8(int intColor){
		return ((intColor >> 8) % (1<<8)) / 255.0f;
	}
	
	public static float getAlphaFromColor8(int intColor){
		return (intColor % (1<<8)) / 255.0f;
	}	
	
	public static float getRedFromColor6(int intColor){
		return ((intColor >> 22) % (1<<8)) / 255.0f;
	}

	public static float getGreenFromColor6(int intColor){
		return  ((intColor >> 14) % (1<<8)) / 255.0f;
	}
	
	public static float getBlueFromColor6(int intColor){
		return ((intColor >> 6) % (1<<8)) / 255.0f;
	}
	
	public static float getAlphaFromColor6(int intColor){
		return (intColor % (1<<6)) / 255.0f;
	}		
}

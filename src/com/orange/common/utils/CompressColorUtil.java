package com.orange.common.utils;

public class CompressColorUtil {

	
	// new compress, with 8 bits for alpha
	public static long compressColor8WithRed(float red, float green, float blue, float alpha){
		long ret = (long)(alpha * 255.0f) +
	                     ((long)(blue * 255.0f) << 8) +
	                     ((long)(green * 255.0f) << 16) +
	                     ((long)(red * 255.0f) << 24);
	    return ret;
	}
	
	// old compress, with 6 bits for alpha
	public static long compressColor6WithRed(float red, float green, float blue, float alpha){
		long ret = (long)(alpha * 63.0f) +
	                     ((long)(blue * 255.0f) << 6) +
	                     ((long)(green * 255.0f) << 14) +
	                     ((long)(red * 255.0f) << 22);
	    return ret;
	}

	public static float getRedFromColor8(long longColor){
		return ((longColor >> 24) % (1<<8)) / 255.0f;
	}

	public static float getGreenFromColor8(long longColor){
		return  ((longColor >> 16) % (1<<8)) / 255.0f;
	}
	
	public static float getBlueFromColor8(long longColor){
		return ((longColor >> 8) % (1<<8)) / 255.0f;
	}
	
	public static float getAlphaFromColor8(long longColor){
		return (longColor % (1<<8)) / 255.0f;
	}	
	
	public static float getRedFromColor6(long longColor){
		return ((longColor >> 22) % (1<<8)) / 255.0f;
	}

	public static float getGreenFromColor6(long longColor){
		return  ((longColor >> 14) % (1<<8)) / 255.0f;
	}
	
	public static float getBlueFromColor6(long longColor){
		return ((longColor >> 6) % (1<<8)) / 255.0f;
	}
	
	public static float getAlphaFromColor6(long longColor){
		return (longColor % (1<<6)) / 63.0f;
	}		
}

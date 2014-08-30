package com.orange.common.utils;

import java.util.Random;

public class RandomUtil {

	public static int random(int n){
		if (n <= 0){
			return 0;
		}
		
		Random random = new Random();
		int val = random.nextInt(n);
		return val;		
	}

    public static int random(int min, int max){
        if (min <= 0 || max <= 0){
            return 0;
        }

        Random random = new Random();
        int val = min + random.nextInt(max - min) % (max - min);
        return val;
    }

    private static final String NUMBER_LETTER = //"!@#$%^&*()" +
            "0123456789" +
            "abcdefghijklmnopqrstuvwxyz"; // +
//            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String randomString(int length) {
        return randomString(NUMBER_LETTER, length);
//        Random rand = new Random(System.currentTimeMillis());
//        StringBuffer sb = new StringBuffer();
//        int charSetLen = charset.length();
//        for (int i = 0; i <= length; i++ ) {
//            int pos = rand.nextInt(charSetLen);
//            sb.append(charset.charAt(pos));
//        }
//        return sb.toString();
    }

    private static final String NUMBER = "0123456789";

    public static String randomNumberString(int length){
        return randomString(NUMBER, length);
    }

    public static String randomString(String charset, int length) {
        Random rand = new Random(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer();
        int charSetLen = charset.length();
        for (int i = 0; i < length; i++ ) {
            int pos = rand.nextInt(charSetLen);
            sb.append(charset.charAt(pos));
        }
        return sb.toString();
    }

}

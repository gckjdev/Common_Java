package com.orange.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import com.orange.common.log.ServerLog;

public class StringUtil {

	public static String md5base64encode(String input) {
		try {
			if (input == null)
				return null;

			if (input.length() == 0)
				return null;

			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes("UTF-8"));
			byte[] enc = md.digest();
			String base64str = Base64.encodeBase64String(enc);
			return base64str;
			
		} 
		catch (NoSuchAlgorithmException e) {
			return null;			
		} 
		catch (UnsupportedEncodingException e) {
			return null;
		}
		
	}
	
	public static String base64encode(String input) {
		if (input == null)
			return null;

		if (input.length() == 0)
			return null;
      
		String base64str = Base64.encodeBase64String(input.getBytes());
		return base64str;
	}
	
	public static String[] getStringList(String... stringList){
		return stringList;
	}
	
	public static boolean isEmpty(String str){
		return (str == null || str.trim().length() == 0);
	}
	
	public static Date dateFromIntString(String str){
		if (str == null || str.length() == 0)
			return null;
		Integer time = Integer.parseInt(str);		
		return new Date(time.longValue()*1000);
	}
	
	// 2011-07-19T00:00:00+08:00
	public static Date dateFromString(String str){
		try {
			SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'");
			Date date = myFormatter.parse(str);
			return date;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	public static int intFromString(String str){
		if (str == null || str.trim().length() == 0)
			return 0;

		if (!str.matches("[0-9]*"))
			return 0;
		
		Integer i = Integer.parseInt(str);
		return i.intValue();
	}

	public static double doubleFromString(String str){
		if (str == null || str.length() == 0)
			return -1;
		
		str = str.replaceAll("\\-", "");
		if (str.length() == 0){
			return -1;
		}
		
		Double i = Double.parseDouble(str);
		return i.doubleValue();
	}

	public static List<String> stringToList(String strings) {
		if (strings == null)
			return null;
		
		String[] list = strings.split(",");
		if (list == null || list.length == 0)
			return null;
		
		List<String> stringList = new ArrayList<String>();
		for (int i=0; i<list.length; i++)
			stringList.add(list[i]);

		return stringList;
	}

	public static boolean booleanFromString(String isPostString) {
		if (isPostString == null)
			return false;
		
		if (isPostString.equalsIgnoreCase("no"))
			return false;
		else
			return true;
	}

	public static boolean isValidMail(String mail){
		if(null == mail) return false;
    	
		int length = mail.length();
		if (length<10) {
			return false;
		}
		String retMail = "^[a-zA-Z0-9_.\\-]{1,}@[a-zA-Z0-9_.\\-]{1,}\\.[a-zA-Z0-9_\\-.]{1,}$";

		if ( Pattern.matches(retMail, mail)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String randomUUID(){
		return UUID.randomUUID().toString();
	}
	
	
	/**
	 *  把sa与sb中字符交替合并为新的字符串，剩下的直接并入结果字符串
	 *  
	 *  如：
	 *    abc  + def  ==> adbecf
	 *    abcd + ef   ==> aebfcd
	 *    ab   + cdef ==> acbdef
	 */
	public static String intersetTwoStrings(String sa, String sb) {
		
		if ( sa == null )
			return sb;
		if ( sb == null ) 
			return sa;
			
		char[] sac = sa.toCharArray();
		char[] sbc = sb.toCharArray();
		char[] rc = new char[sac.length+sbc.length];
		
		int minLen = sac.length <= sbc.length ? sac.length : sbc.length;
		int i = 0;
		for ( ; i < minLen; i++ ) {
			rc[i*2] = sac[i];
			rc[i*2+1] = sbc[i];
		}
		if ( sac.length < sbc.length ) {
			for (int j = 2*i; j < rc.length; j++) {
				rc[j] = sbc[i+j-2*i];
			}
		} else if ( sac.length > sbc.length ){
			for (int j = 2*i; j < rc.length; j++) {
				rc[j] = sac[i+j-2*i];
			}
		}
		
		return new String(rc);
	}
	
	/**
	 *  intersetTwoStrings的逆操作，
	 *   从sa中sb第一个字符出现的位置开始，
	 *   间隔的剔除sb中的字符。如果sa中没有
	 *   sb，返回null。
	 *   
	 *  
	 *  如：
	 *   sa = "adbecf" sb = "def" ==> abc
	 *   sa = "adbecf" sb = "abc" ==> def
	 */
	public static String deIntersetTwoStrings(String sa, String sb) {
		
		if ( sa == null ) 
			return null;
		if ( sb == null )
			return sa;
		if ( sa.length() <= sb.length() )
			return null;
		
		int startIndex = sa.indexOf(sb.substring(0, 1));
		if ( startIndex == -1 ) 
			return null;
		
		byte[] sab = sa.getBytes();
		byte[] sbb = sb.getBytes();
		byte[] rb = new byte[sab.length-sbb.length];

		int i = 0;
		for ( ; i < startIndex; i++ ) {
			rb[i] = sab[i];
		}
		
		for ( ; i < rb.length; i++ ) {
			if ( i - startIndex < sbb.length ) {
				rb[i] = sab[startIndex+1+2*(i-startIndex)];
			} else {
				rb[i] = sab[sbb.length+i];
			}
		}
		
		return new String(rb);
		
	}
	
	public static void main(String[] args) {
		
		String userName = "gckj";
		String passWord = "gckjdev123";
		String saultForUserName = "Alpha";
		String saultForPassWord = "Gamma";
		ServerLog.info(0, "1. Before encoded : userName = " + userName+", passWord = "+passWord);
		
		// 使用sault,对目标字符串进行打乱
		String saultedUserName = intersetTwoStrings(userName, saultForUserName);
		String saultedPassWord = intersetTwoStrings(passWord, saultForPassWord);
		ServerLog.info(0, "2. Saulted: sault for userName : "+saultForUserName+", saultedUserName = "+saultedUserName+";  sault for password : "+saultForPassWord+", saultedPassWord = "+saultedPassWord);
		
		// 加密打乱后的字符串, 得到密文
		String encodedSaultedUserName = Base64.encodeBase64String(saultedUserName.getBytes());
		String encodedSaultedPassWord = Base64.encodeBase64String(saultedPassWord.getBytes());
		ServerLog.info(0, "3. encoded: encodedSaultedUserName = "+encodedSaultedUserName+", encodedSaultedPassWord = "+encodedSaultedPassWord);

		// 解密密文
		String decodedSaultedUserName = new String(Base64.decodeBase64(encodedSaultedUserName));
		String decodedSaultedPassWord = new String(Base64.decodeBase64(encodedSaultedPassWord));
		ServerLog.info(0, "4. decoded: decodedSaultedUserName = "+decodedSaultedUserName+", decodedSaultedPassWord = "+decodedSaultedPassWord);
		
		// 从解密后的字符串中剔除sault得到目标字符串
		ServerLog.info(0, "5. desaulted: userName = "+deIntersetTwoStrings(decodedSaultedUserName,saultForUserName)
	    + ", passWord = "+deIntersetTwoStrings(decodedSaultedPassWord,saultForPassWord));
	}
}

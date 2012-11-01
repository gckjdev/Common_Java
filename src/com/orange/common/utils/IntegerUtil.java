package com.orange.common.utils;

import org.apache.log4j.Logger;



public class IntegerUtil {
	
	private static final Logger logger = Logger.getLogger(IntegerUtil.class.getName());
	/*
	 * <num>里有多少个<bitType>?
	 * 
	 * para1: 待检查的数。
	 * para2: 检查哪一部分，不检查的位必须置为0. 比如， 0XFF表示检查num的最低8位即可。
	 * para3: 要检查的位是什么，是0，还是1。
	 */
	public static int howManyBits(int num, int mask, int bitType) {
		
		int count = 0;

		// 如果bitType是0, 只要反转num的各个位，
		// 即可归约为bitType是1的情况
		if (bitType == 1) {
			num = num & mask;
		} else {
			num = (~num) & mask;
		}
		
		while (num != 0 ){
		  count += num & 1;
		  num >>= 1; 
		}
		
		return count;
	}

	/* 
	 * <num>里是否有<count>个连续的<bitType>? 
	 *
	 * para1: 待检查的数。
	 * para2: 检查哪一部分，不检查的位必须置为0. 比如， 0XFF表示检查num的最低8位即可。 
	 * para3: 我们希望有多少个相邻的位。
	 * para4: 我们希望这count个相邻的位是什么，是0，还是1。
	 */
	public static boolean hasConsecutiveBit(int num, int mask, int count, int bitType ) {
		
		// Java 中的int类型是确定的32位，
		// 所以肯定不能超过这个限制。
		if ( count > 32 ) {
			return false;
		}
		
		// 如果bitType是0, 只要反转num的各个位，
		// 即可归约为bitType是1的情况
		if ( bitType == 1 ) {
			num = num & mask;
		} else {
			num = (~num) & mask;
		}
		
		// pattern表示一个模式值，比如要找3们相邻的1, pattern就是111。
		int pattern = (1 << count) - 1;
		// window表示当前num中被判断的部分，所以称为窗口。
		int window;

		// 开始作判断，我们要找的就是和pattern一样的窗口值。
		// 如果当前窗口与pattern进行[和]操作后仍为pattern，就说明num有连续count个bitType,
		// 于是结束该方法。 否则，把num右移，继续作判断。直到num为0，那表示num没有，返回false.
		// 其中每次num右移的位数是有技巧的，因为我们要找的是pattern这个值，如num的最低3位是011,
		// 那么num直接右移3位; 如果是110,那只能右移1位.
		// 所以，里层的while循环就是为了找出当前窗口中最高位的0, 然后右移相应的位。
		while ( num != 0 ) {
			window = num & pattern;
			if ( window == pattern ) {
				return true;
			}
			else  {
				int howManyBitToShift = count;
				while ( ( window & (1 << (howManyBitToShift-1)) ) != 0 )
					howManyBitToShift--;
				
				num >>= howManyBitToShift;
			}
		}	
		
		return false;
	}

}

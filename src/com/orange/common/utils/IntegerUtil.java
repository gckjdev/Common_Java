package com.orange.common.utils;



public class IntegerUtil {
	
	public static int howManyOneBit(int num) {
		
		int count = 0;
		
		while (num != 0 ){
		  count += num & 1;
		  num >>= 1; 
		}
		
		return count;
	}

	// is there <count> consecutive <bitNum> bits in num? bitNum: 0 or 1
	public static boolean hasConsecutiveBit(int num, int count, int bitNum ) {
		
		
		if ( count > 32 ) {
			return false;
		}
		
		
		int pattern = (1 << count) - 1;
		int window;
		
		num = (bitNum == 1 ? num : ~num);
		while ( num != 0 ) {
			window = num & pattern;
			if ( window == pattern ) {
				return true;
			}
			else  {
				while ( (window & (1 << (count-1))) != 0 )
					count--;
				num >>= count;
			}
		}	
		
		return false;
	}

}

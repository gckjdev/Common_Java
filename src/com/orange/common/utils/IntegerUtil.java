package com.orange.common.utils;

import org.apache.log4j.Logger;



public class IntegerUtil {
	
	private static final Logger logger = Logger.getLogger(IntegerUtil.class.getName());
	
	/**
	 * <num>里有多少个<bitType>?
	 * 
	 * @param num: 待检查的数。
	 * @param mask: 检查哪一部分，不检查的位必须置为0. 比如， 0XFF表示检查num的最低8位即可。
	 * @param bitType: 要检查的位模式，是0，还是1。
	 * @return: 多少个<bitType>.
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
		  num >>>= 1; 
		}
		
		return count;
	}

	/**
	 * <num>里是否有<count>个连续的<bitType>? 
	 *
	 * @param num： 待检查的数。
	 * @param mask: 检查哪一部分，不检查的位必须置为0. 比如， 0XFF表示检查num的最低8位即可。 
	 * @param count： 希望有多少个相邻的位。
	 * @param bitType： 要检查的位模式，是0，还是1。
	 * @return: 有返回true, 没有就返回false.
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
				
				num >>>= howManyBitToShift;
			}
		}	
		
		return false;
	}

	/**
	 * 
	 * <num>中第<skip+1>次出现<bitType>的位置, 从最高位开始。
	 * 
	 * @param num： 待检查的数。
	 * @param mask: 检查哪一部分，不检查的位必须置为0. 比如， 0XFF表示检查num的最低8位即可。
	 * @param skip: 跳过前<skip>次。比如 skip为0, 表示跳过第0次，也就是返回第一次出现的位置，以此类推。
	 * @param bitType: 要检查的位模式，是0，还是1。
	 * @return： 见描述。返回-1表示失败。
	 */
	public static int forPosition(int num, int mask, int skip, int bitType) {
		
		int position = -1;
		
		// 如果bitType是0, 只要反转num的各个位，
		// 即可归约为bitType是1的情况
		if (bitType == 1) {
			num = num & mask;
		} else {
			num = (~num) & mask;
		}
		
		// 把游标置于mask最高位。
		int cursor = (mask >>> 1)+1;
		
		// 开始找，遇到0直接跳过, 遇到1就判断是否
		// 跳过足够次数，是就返回，否则跳过。
		while ( cursor != 0 ) { // 游标还没移出界...
			if ( (num & cursor) == 0 ) {// 遇到0 
				cursor >>>= 1;
			} 
			else { // 遇到1
				if ( skip-- == 0 ) {
					// 找到，退出循环
					position = howManyBits(cursor, (cursor<<1)-1, 0);
					break;
				} else { 
					cursor >>>= 1;
				}
			}
		}
		
		return position;
	}

	/**
	 * 
	 * <num>中第<position>位是否为<bitType>. 
	 * 
	 * @param num： 待检查的数。
	 * @param position: 从最低位(0)开始的位置，例如： 最高位的position值为31.
	 * @param bitType: 要检查的位模式，是0，还是1。
	 * @return： 是就返回true; 不是就返回false。
	 */
	public static boolean testBit(int num, int position, int bitType) {
		
		// Java 中的int类型是确定的32位，
		// 且由于position是从0开始计起，所以不能超过31.
		if ( position > 31 ) {
			return false;
		}
				
		// 如果bitType是0, 只要反转num的各个位，
		// 即可归约为bitType是1的情况
		num = ( bitType == 1 ? num : ~num);
		
		int testNum = 1 << position;
		return (num & testNum) == testNum;
	}
	 
}

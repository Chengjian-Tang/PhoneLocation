/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-9-3
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.util;

/**
 * @author feelow
 *
 */
public class BytesUtil {
	
	public static int readInt(byte[] bytes, int start, int end) {
		int ret = 0;
		
		for (int i = start; i < end; i++) {
			ret |= ((bytes[i] << (end - i - 1) * 8 & (0xff << (end - i - 1) * 8)));
		}
		
		return ret;
	}
	
	public static long readLong(byte[] bytes, int start, int end) {
		return readInt(bytes, start, end);
	}
	
	public static short readShort(byte[] bytes, int start, int end) {
		return (short) readInt(bytes, start, end);
	}
	
//	public static void main(String[] args) {
//		byte[] bytes = {(byte) 0xaa, (byte) 0xab, (byte) 0xac};
//		
//		System.out.println(readLong(bytes,0, 3));
//	}
}

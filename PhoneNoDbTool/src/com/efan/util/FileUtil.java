/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-9-2
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.util;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author feelow
 *
 */
public class FileUtil {

	/**
	 * 将Long型的偏移量低3字节写入文件
	 * @param num
	 * @throws IOException 
	 */
	private static void writeLong3(RandomAccessFile outputFile, Long num) throws IOException {
		//高1字节
		outputFile.writeByte((num.intValue() >> 16) & 0x000000ff);
		//高2字节
		outputFile.writeByte((num.intValue() >> 8) & 0x000000ff);
		//高3字节
		outputFile.writeByte(num.intValue() & 0x000000ff);
	}
	
	/**
	 * 将int的低两位字节写文件
	 * @param num
	 * @throws IOException 
	 */
	private static void writeInt2(RandomAccessFile outputFile, int num) throws IOException {
		outputFile.writeByte((num >> 8) & 0x000000ff);
		outputFile.writeByte(num & 0x000000ff);
	}
	
	public static Long read3Long(RandomAccessFile file) throws IOException {
		byte[] bytes = new byte[3];
		
		file.read(bytes);
		
		return (long) ((bytes[0] << 16) | (bytes[1] << 8) | bytes[2] );
	}
}

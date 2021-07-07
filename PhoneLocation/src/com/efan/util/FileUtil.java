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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * @author feelow
 *
 */
public class FileUtil {

//	/**
//	 * 将Long型的偏移量低3字节写入文件
//	 * @param num
//	 * @throws IOException 
//	 */
//	public static void writeLong3(RandomAccessFile outputFile, Long num) throws IOException {
//		//高1字节
//		outputFile.writeByte((num.intValue() >> 16) & 0x000000ff);
//		//高2字节
//		outputFile.writeByte((num.intValue() >> 8) & 0x000000ff);
//		//高3字节
//		outputFile.writeByte(num.intValue() & 0x000000ff);
//	}
//	
//	/**
//	 * 将int的低两位字节写文件
//	 * @param num
//	 * @throws IOException 
//	 */
//	public static void writeInt2(RandomAccessFile outputFile, int num) throws IOException {
//		outputFile.writeByte((num >> 8) & 0x000000ff);
//		outputFile.writeByte(num & 0x000000ff);
//	}
	
	/**
	 * 读3个字节返回long型
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Long read3Long(RandomAccessFile file) throws IOException {
		byte[] bytes = new byte[3];
		
		file.read(bytes);
		
		return (long)((bytes[0] << 16 & 0xff0000) | 
				(bytes[1] << 8 & 0xff00) |
				(bytes[2] & 0xff)) ;
	}
	
	/**
	 * 读3个字节返回int型
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static int read3Int(RandomAccessFile file) throws IOException {
		return read3Long(file).intValue();
	}
	
	/**
	 * 读两个字节返回int
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Short read2Short(RandomAccessFile file) throws IOException {
		byte[] bytes = new byte[2];
		file.read(bytes);
		
		return (short) ((bytes[0] << 8 & 0xff00) |
				bytes[1] & 0xff);
	}
	
	public static String getDbFileVersion(File file) {
		String version = "0";
		RandomAccessFile tmpfile;
		try {
			tmpfile = new RandomAccessFile(file, "r");
			byte[] headBytes = new byte[Constants.HEAD_VERSION_LENGTH];
			tmpfile.read(headBytes);
			version = BytesUtil.readString(headBytes, 0, Constants.HEAD_VERSION_LENGTH);
			tmpfile.close();
			} catch (Exception e) {
			e.printStackTrace();
		}
		
		return version;
	}
	
	/**
	 * 从输入流复制到输出流
	 * @param in
	 * @param out
	 * @throws IOException 
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] bytes = new byte[4096];
		int len = 0;

		while ((len = in.read(bytes)) > 0) {
			out.write(bytes, 0, len);
		}
	}
}

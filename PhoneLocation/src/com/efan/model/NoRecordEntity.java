/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-9-6
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.model;

import com.efan.util.Constants;

/**
 * @author feelow
 * 号段记录实体类
 */
public class NoRecordEntity {
	private short pidStart; //开始号码前三位 如 135
	private short pidEnd; //结束号码前三位
	private short noStart; //开始号段 3位
	private short noEnd; //结束号段 3位
	private short zoneOffset; //区号索引地址 3位
//	private String city;
	/**
	 * @param noStart
	 * @param noEnd
	 * @param zoneOffset
	 */

	public NoRecordEntity() {}

//	public NoRecordEntity(byte[] bytes) {
//		parseBytes(bytes);
//	}
	
	public int getCompleteNoStart() {
		return pidStart * 10000 + noStart;
	}
	
	public int getCompleteNoEnd() {
		return pidEnd * 10000 + noEnd;
	}

	public void parseBytes(byte[] bytes) {
		pidStart = (short) (bytes[0] & 0xff);
		pidEnd = (short) (bytes[1] & 0xff);
		noStart = (short) (((bytes[2] & 0xff) << 8 | (bytes[3] & 0xff)) >> 2);
		noEnd = (short) ((((bytes[3] & 0x03) << 16) | ((bytes[4] & 0xff) << 8) | (bytes[5] & 0xff)) >> 4);
		zoneOffset = (short) ((((bytes[5] & 0x0f) << 8) | (bytes[6] & 0xff)));
	}
	
//	public byte[] getBytes() {
//		byte[] bytes = new byte[Constants.NO_RECORD_LENGTH];
//		bytes[0] = (byte) (pidStart & 0x00ff);
//		bytes[1] = (byte) (pidEnd & 0x00ff);
//		long tmp = (long) ((((long)noStart) & 0x3fff) << 26 | (((long)noEnd) & 0x3fff) << 12 |
//			(zoneOffset & 0x000fffff));
//		bytes[2] = (byte) ((tmp & 0x000000ff00000000l) >> 32);
//		bytes[3] = (byte) ((tmp & 0x00000000ff000000l) >> 24);
//		bytes[4] = (byte) ((tmp & 0x0000000000ff0000l) >> 16);
//		bytes[5] = (byte) ((tmp & 0x000000000000ff00l) >> 8);
//		bytes[6] = (byte)  (tmp & 0x00000000000000ffl);
//		
//		NoRecordEntity no = new NoRecordEntity();
//		no.parseBytes(bytes);
//		return bytes;
//	}
//
//	/**
//	 * @return the pidStart
//	 */
//	public short getPidStart() {
//		return pidStart;
//	}
//
//	/**
//	 * @param pidStart the pidStart to set
//	 */
//	public void setPidStart(short pidStart) {
//		this.pidStart = pidStart;
//	}
//
//	/**
//	 * @return the pidEnd
//	 */
//	public short getPidEnd() {
//		return pidEnd;
//	}
//
//	/**
//	 * @param pidEnd the pidEnd to set
//	 */
//	public void setPidEnd(short pidEnd) {
//		this.pidEnd = pidEnd;
//	}
//
//	/**
//	 * @return the noStart
//	 */
//	public short getNoStart() {
//		return noStart;
//	}
//
//	/**
//	 * @param noStart the noStart to set
//	 */
//	public void setNoStart(short noStart) {
//		this.noStart = noStart;
//	}
//
//	/**
//	 * @return the noEnd
//	 */
//	public short getNoEnd() {
//		return noEnd;
//	}
//
//	/**
//	 * @param noEnd the noEnd to set
//	 */
//	public void setNoEnd(short noEnd) {
//		this.noEnd = noEnd;
//	}
//
	/**
	 * @return the zoneOffset
	 */
	public short getZoneOffset() {
		return zoneOffset;
	}
//
//	/**
//	 * @param zoneOffset the zoneOffset to set
//	 */
//	public void setZoneOffset(short zoneOffset) {
//		this.zoneOffset = zoneOffset;
//	}
//
//	/**
//	 * @return the city
//	 */
//	public String getCity() {
//		return city;
//	}
//
//	/**
//	 * @param city the city to set
//	 */
//	public void setCity(String city) {
//		this.city = city;
//	}

}
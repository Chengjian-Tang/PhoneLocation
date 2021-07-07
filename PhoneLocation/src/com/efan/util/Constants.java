/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-8-29
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.util;

/**
 * @author feelow
 *　常量类
 */
public class Constants {
	public static final short PHONE_TYPE_TEL = 0;
	public static final short PHONE_TYPE_MOB = 1;
	public static final short PHONE_TYPE_OTHER = 2;
	
	public static final short HEAD_VERSION_LENGTH = 8; //首部版本号字段 如:20100101
	public static final short HEAD_LENGTH = 12 + HEAD_VERSION_LENGTH;
	public static final short HEAD_FILED_LENGTH = 3;
	
	public static final short ZONE_RECORD_LENGTH = 5; //区号记录长度
	public static final short ZONE_RECORD_ZONECODE_LENGTH = 2; //区号记录区号段长度
	public static final short ZONE_RECORD_CITYOFFSET_LENGTH = 3; //区号记录城市索引长度
	
	
	public static final short NO_RECORD_LENGTH = 7; //号段记录长度
	public static final short NO_RECORD_NOSTART_LENGTH = 3; //号段记录开始号段长度
	public static final short NO_RECORD_NOEND_LENGTH = 3; //号段记录结束号段长度
	public static final short NO_RECORD_ZONEOFFSET_LENGTH = 3; //号段记录区号索引地址长度	
	
	//运营商
	public static final String AGENT_NAME_CM = "中国移动";
	public static final String AGENT_NAME_CT = "中国电信";
	public static final String AGENT_NAME_CU = "中国联通";
	public static final String AGENT_NAME_UKN = "未知运营商";
	
	//最大偏移量
	public static final Long MAX_OFFSET = 0x0000000000ffffffl;
	
}

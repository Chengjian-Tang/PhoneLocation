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
package com.efan.model;

/**
 * @author feelow
 *
 */
public class Location {
	
	/**
	 * 区号
	 */
	private String zoneCode = "";
	/**
	 * 省
	 */
	private String province = "";
	/**
	 * 市
	 */
	private String city = "";
	/**
	 * 运营商名称
	 */
	private String agentName = "";
	
	public Location() {}
	/**
	 * @param zoneCode
	 * @param province
	 * @param city
	 * @param agnentName
	 */
	public Location(String zoneCode, String province, String city,
			String agentName) {
		super();
		this.zoneCode = zoneCode;
		this.province = province;
		this.city = city;
		this.agentName = agentName;
	}
	@Override
	public String toString() {
		return zoneCode + " " + province + " " + city +
			" " + agentName;
	}
	/**
	 * @return the zoneCode
	 */
	public String getZoneCode() {
		return zoneCode;
	}
	/**
	 * @param zoneCode the zoneCode to set
	 */
	public void setZoneCode(String zoneCode) {
		this.zoneCode = zoneCode;
	}
	/**
	 * @return the province
	 */
	public String getProvince() {
		return province;
	}
	/**
	 * @param province the province to set
	 */
	public void setProvince(String province) {
		this.province = province;
	}
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return the agentName
	 */
	public String getAgentName() {
		return agentName;
	}
	/**
	 * @param agentName the agentName to set
	 */
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

}

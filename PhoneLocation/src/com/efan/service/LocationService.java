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
package com.efan.service;


import java.io.IOException;

import com.efan.model.Location;

/**
 * @author feelow
 *
 */
public abstract class LocationService {
	
			
	/**
	 * 根据src串查出对应的归属地信息
	 * @param src
	 * @return
	 * @throws Exception 
	 */
	public abstract Location getLocation(String src) throws Exception;
	public abstract void destroy() throws IOException;
	public abstract String getDbFileVersion();
}

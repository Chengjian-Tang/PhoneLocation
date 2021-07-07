/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-10-8
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.util;

import com.efan.service.DbMerge20101008Service;

/**
 * @author feelow
 *
 */
public class MergeMain {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		DbMerge20101008Service mergeService = new DbMerge20101008Service();
		mergeService.merge("C:\\Documents and Settings\\feelow\\桌面\\mobile.txt", 
				"C:\\Documents and Settings\\feelow\\桌面\\mobile_20101003.txt");
	}

}

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

import java.io.IOException;

import com.efan.service.PhoneNoDbGenService;

/**
 * @author feelow
 *
 */
public class Main {
	public static void main(String[] args) throws IOException {
		PhoneNoDbGenService service = new PhoneNoDbGenService(
				"C:\\Documents and Settings\\feelow\\桌面\\省份表.txt", 
				"C:\\Documents and Settings\\feelow\\桌面\\城市区号表.txt", 
				"C:\\Documents and Settings\\feelow\\桌面\\mobile.txt", 
			"C:\\Documents and Settings\\feelow\\桌面\\database", "20100908");
		service.createDatfile();
	}

}

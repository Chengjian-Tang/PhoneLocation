/*
 * 系统名称: 
 * 模块名称: 指定义归属地查询服务
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-10-18
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.service;

import java.io.IOException;

import android.content.Context;

import com.efan.model.Location;
import com.efan.phonelocation.CustlocDbAdapter;

/**
 * @author feelow
 *
 */
public class CustLocationService extends LocationService {
	private CustlocDbAdapter mDbHelper = null;
	private static CustLocationService instance = null;

	public static LocationService getInstance(Context ctx) {
		if (instance == null) {
			instance = new CustLocationService(ctx);
		}

		return instance;
	}

	protected CustLocationService(Context ctx) {			
		mDbHelper = new CustlocDbAdapter(ctx);
		mDbHelper.open();
	}


	/* 
	 * 
	 */
	@Override
	public Location getLocation(String src) throws Exception {
		Location location = null;
		String locationStr = null;

		locationStr = mDbHelper.fetchCursorloc(src);

		if (locationStr != null) {
			location = new Location();
			location.setProvince(locationStr);
		}

		return location;

	}

	@Override
	protected void finalize() throws Throwable {
		destroy();

		super.finalize();
	}

	@Override
	public void destroy() throws IOException {
		mDbHelper.close();
	}

	@Override
	public String getDbFileVersion() {
		return null;
	}

}

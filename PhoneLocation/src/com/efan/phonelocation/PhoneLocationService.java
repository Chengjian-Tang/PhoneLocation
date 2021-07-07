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
package com.efan.phonelocation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.efan.model.Location;
import com.efan.phonelocation.view.MyToast;
import com.efan.service.CustLocationService;
import com.efan.service.LocationService;
import com.efan.service.PhoneNoLocationService;
import com.efan.util.BytesUtil;
import com.efan.util.Constants;
import com.efan.util.FileUtil;
import com.efan.util.StringUtil;

/**
 * @author feelow
 *
 */
public class PhoneLocationService extends Service 
{
	public final static String filename = "database.dat";

	private static File dbFile = null;

	private static LocationService locationService = null;
	private static LocationService custLocationService = null;//自定义归属地服务

	private static MyToast toast = null;

	private static int xOffset = 0;
	private static int yOffset = 0;
	private static SharedPreferences prefs = null;
	
	private boolean isDisplayed = false;
	private Location location = null;
	private PhoneStateListener callStateListener = null;

	@Override
	public void onCreate() {
		super.onCreate();
		init();
		setupIncomingCallListener();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroyLocationService();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Bundle bundle = null;
		String phoneNo = null;


		//取出电话号码
		if (intent != null) {
			bundle = intent.getExtras();
			if (bundle != null) {
				phoneNo = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
			}
		} 
		
		if (phoneNo != null) {
			location = getLocation(phoneNo, this);
		} 

		return Service.START_STICKY;
	}

	private void init() {
		Context ctx = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		//设置成员
		loadPrefs();
		toast = new MyToast(xOffset, yOffset, 200, "", this);

		try {
			dbFile = new File("/data/data/" + ctx.getPackageName() + "/files", filename);
			InputStream in = ctx.getAssets().open(filename);

			if (!dbFile.exists()) {
				//复制文件
				OutputStream out = ctx.openFileOutput(filename, BIND_AUTO_CREATE);
				FileUtil.copy(in, out);
				in.close();
				out.close();

			} else {
				//比较版本号
				if (Integer.parseInt(getDbVersion(ctx)) > Integer.parseInt(FileUtil.getDbFileVersion(dbFile))) {
					//复制文件
					OutputStream out = ctx.openFileOutput(filename, BIND_AUTO_CREATE);
					FileUtil.copy(in, out);
					in.close();
					out.close();
				}
			}
		}  catch (Exception e) {
			dbFile.delete();
			dbFile = null;
			stopSelf();
			e.printStackTrace();
		}
	}

	/**
	 * 返回数据库版本
	 * @param ctx
	 * @return
	 */
	public static String getDbVersion(Context ctx) {
		String newVersion = "0";
		byte[] bytes = new byte[Constants.HEAD_VERSION_LENGTH];
		try {
			InputStream in = ctx.getAssets().open(filename);
			in.read(bytes);
			newVersion = BytesUtil.readString(bytes, 0, Constants.HEAD_VERSION_LENGTH);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return newVersion;
	}

	/* 
	 * 取消显示后停止服务自身
	 */
	public void afterCancelAction() {
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(
				getApplicationContext().TELEPHONY_SERVICE);
		
		isDisplayed = false;
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
		stopSelf();
	}
	
	/**
	 * 安装通话状态监听器
	 */
	private void setupIncomingCallListener() {
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(
				getApplicationContext().TELEPHONY_SERVICE);

		callStateListener = new PhoneStateListener() {
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING ://收到来电在CallActionReceiver中处理
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (location != null && !isDisplayed) {
						displayLocation(location);
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					if (isDisplayed || location == null) {
						cancelDisplayLocation();
					}
				}
			}
		};
		telephonyManager.listen(callStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	/**
	 * 显示归属地信息
	 * @param location
	 */
	private void displayLocation(Location location) {
		String msg = "";
		isDisplayed = true;

		if (!StringUtil.isEmpty(location.getAgentName())) {
			msg += location.getAgentName() + " ";
		}

		if (!StringUtil.isEmpty(location.getProvince())) {
			msg += location.getProvince() + " ";
		}

		if (!StringUtil.isEmpty(location.getCity())) {
			msg +=  location.getZoneCode() + location.getCity();
		}

		if (StringUtil.isEmpty(msg)) {
			return ;
		}

		displayText(msg);
	}

	/**
	 * 显示信息
	 * @param msg
	 */
	private void displayText(String msg) {
		loadPrefs();

		toast.setPosition(xOffset, yOffset);
		toast.setMsg(msg);
		toast.show();
	}

	/**
	 * 载入选项
	 */
	private void loadPrefs() {
		xOffset = prefs.getInt(MainActivity.XOFFSET, 0);
		yOffset = prefs.getInt(MainActivity.YOFFSET, 0);
		
	}

	/**
	 * 取消归属地
	 */
	private void cancelDisplayLocation() {
		if (toast != null) {
			toast.cancel();
		}
		afterCancelAction();
	}

	/**
	 * 启动归属地数据库服务
	 */
	private static void startLocationService(Context ctx) {

		if (custLocationService == null) {
			custLocationService = CustLocationService.getInstance(ctx);
		}

		if (dbFile == null) {
			dbFile = new File("/data/data/" + ctx.getPackageName() + "/files", filename);
		}

		if (locationService == null) {
			locationService = PhoneNoLocationService.getInstance(dbFile);
		}
	}

	/**
	 * 停止归属地数据库服务
	 */
	private static void destroyLocationService() {
		try {
			if (locationService != null) {
				locationService.destroy();
			}
			//			custLocationService.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			locationService = null;
			custLocationService = null;
		}
	}

	/**
	 * 查自定义归属地信息
	 * @param phoneno
	 * @return
	 */
	public static Location getCustLocation(String phoneno, Context ctx) {
		Location location = null;

		try {
			location = custLocationService.getLocation(phoneno);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * 取手机号码对应的归属地
	 * @param phoneNo
	 * @return
	 */
	public static Location getLocation(String phoneNo, Context ctx) {
		Location location = null;

		startLocationService(ctx);
		//查自定义归属地信息
		location = getCustLocation(phoneNo, ctx);

		if (location != null) {
			return location;
		}
		//若未查到,则查自身数据库
		//查询归属地数据
		if (locationService != null) {
			try {
				location = locationService.getLocation(phoneNo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//		destroyLocationService();

		return location;
	}

}

/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-9-4
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.phonelocation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

/**
 * @author feelow
 * 接收来电和去电
 */
public class CallActionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String phoneNo = null;
		Intent serviceIntent = new Intent(context, PhoneLocationService.class);

		//去电
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			phoneNo = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
			//启动归属地服务

			startService(context, serviceIntent, phoneNo);
		} else { //来电
			TelephonyManager tm = (TelephonyManager)context.getSystemService(
					Service.TELEPHONY_SERVICE);
			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING://启动服务
				phoneNo = intent.getStringExtra("incoming_number");
				startService(context, serviceIntent, phoneNo);
				break;
			}
		}
	}

	/**
	 * 已指定号码启动归属地服务
	 * @param context
	 * @param phoneNo
	 */
	private void startService(Context context, Intent serviceIntent,
			String phoneNo) {
		Bundle bundle = new Bundle();			

		bundle.putString(Intent.EXTRA_PHONE_NUMBER, phoneNo);
		serviceIntent.putExtras(bundle);
		
		context.startService(serviceIntent);
	}
}

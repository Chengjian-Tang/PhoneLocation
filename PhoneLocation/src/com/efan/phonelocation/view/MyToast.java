/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-9-7
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.phonelocation.view;

import java.util.Timer;
import java.util.TimerTask;

import com.efan.util.StringUtil;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author feelow
 * 自定义Toast
 */
public class MyToast {
	private int xOffset = 0;
	private int yOffset = 0;
	private int delay = 10;
	private boolean isShow = false;
	private TextView msgTextView = null;
	private String msg = null;

	private Context context = null;
	private Toast toast = null;
	private static Timer toastShowTimer = null;

	public MyToast(int xOffset, int yOffset, int delay,
			String msg, Context context) {
		super();
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.delay = delay;
		this.context = context;
		this.msg = msg;
		msgTextView = new TextView(context);
		msgTextView.setTextSize(20);
		
		init();

	}

	class ToastTimerTask extends TimerTask {

		@Override
		public void run() {
			while (true) {
				if (toast != null) {
					synchronized (this) {
						if (!isShow) {
							break;
						}
						toast.show();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void show() {
		isShow = true;
		setupToast();
		toastShowTimer.schedule(new ToastTimerTask(), delay);
	}

	synchronized public void cancel() {
		isShow = false;

		if (toast != null) {
			toast.cancel();
		}
	}

	/**
	 * 设置显示位置
	 * @param xOffset
	 * @param yOffset
	 */
	public void setPosition(int xOffset, int yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * @return the xOffset
	 */
	public int getxOffset() {
		return xOffset;
	}

	/**
	 * @param xOffset the xOffset to set
	 */
	public void setxOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	/**
	 * @return the yOffset
	 */
	public int getyOffset() {
		return yOffset;
	}

	/**
	 * @param yOffset the yOffset to set
	 */
	public void setyOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	/**
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * 设置Toast
	 */
	private void setupToast() {
//		System.out.println("##########In setupToast:msg=" + msg + "##########");
		if (StringUtil.isEmpty(msg)) {
			msg = "";
		}
		
		toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, xOffset, yOffset);
		msgTextView.setText(msg);
		toast.setView(msgTextView);
	}

	/**
	 * 初始化
	 */
	private void init() {
		toastShowTimer = new Timer();
	}
}


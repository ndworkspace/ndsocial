package com.nd.voice.meetingroom.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BackServerUtils {

//	public static void add
	
	public static void registerBackReceiver(){
		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK); 
//		MyBroadcastReceiver receiver = new MyBroadcastReceiver(); 
//		registerReceiver(receiver, filter); 
	}
	
	
	/**
	 * 打开唯一的服务
	 * @param context
	 * @param serviceClass 服务
	 */
	public static void beginSignleService(Context context,Class<Service> serviceClass){
		boolean isServiceRunning = false;
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getPackage().getName().equals(service.service
					.getClassName()))
			// Service的类名           
			{
				isServiceRunning = true;
			}
		}
		if (!isServiceRunning) {
			Intent i = new Intent(context, serviceClass);
			context.startService(i);
		}
	}
	
	
}


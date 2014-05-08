package cn.nd.social;


import java.lang.ref.WeakReference;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import cn.nd.social.account.CloundServer;
import cn.nd.social.services.SocialService;
import cn.nd.social.services.SocialService.ServiceBinder;
import cn.nd.social.services.WifiService.WifiServiceBinder;
import cn.nd.social.updater.UpdateService;


public class SocialApplication extends Application {
	public static final String TAG = "SocialApplication";
	public static SocialApplication sSocialApp;

	private int mCurrReqCode = 0;

	
	public final static int APP_EVENT_TOAST_MSG = 51;
	
	private SparseArray<WeakReference<Handler>> mPendingHandler = new SparseArray<WeakReference<Handler>>();
	
	public void sendToastMessage(String info) {
		Message msg = mHandler.obtainMessage();
		msg.what = APP_EVENT_TOAST_MSG;
		msg.obj = info;
		mHandler.sendMessage(msg);
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case APP_EVENT_TOAST_MSG:
				String info = (String)msg.obj;
				Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	/**
	 * api start
	 */
	public int getCurrReqCode() {
		return mCurrReqCode;
	}
	
	public void setCurrReqCode(int code) {
		mCurrReqCode = code;
	}
	
	public boolean isReqPending(int reqCode) {
		if (mPendingHandler.indexOfKey(reqCode) >= 0) {
			return true;
		}
		return false;
	}

	public void setReqPending(int reqCode,Handler handler) {
		mPendingHandler.put(reqCode, new WeakReference<Handler>(handler));
	}
	
	public void removeReqPending(int reqCode) {
		mPendingHandler.remove(reqCode);
	}
	
	public Handler getReqHandler(int reqCode) {
		WeakReference<Handler> wkRef = mPendingHandler.get(reqCode);
		return (wkRef != null ? wkRef.get():null);
	}

	
	/**
	 * api end
	 */
	
	private ServiceBinder mServiceBinder;
	private WifiServiceBinder mWifiServiceBinder;
	private ServiceConnection mServiceConn;
	private void startLongRunService() {
		
		mServiceConn = new ServiceConnection() {
	        
	        @Override
	        public void onServiceDisconnected(ComponentName name) {
	        	mServiceBinder = null;
	        }
	        
	        @Override
	        public void onServiceConnected(ComponentName name, IBinder service) {
	        	ServiceBinder binder = (ServiceBinder)service;
	        	mServiceBinder = binder;
	        	mServiceBinder.StartVoice();
	        }
	    };
	    
	    Intent intent = new Intent();
	    intent.setClass(this, SocialService.class);
		boolean flag = this.getApplicationContext().bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "bind Social Service return flag : " + flag);		
	}
	
	public ServiceBinder getServiceBinder() {
		return mServiceBinder;
	}
	
	public WifiServiceBinder getWifiServiceBinder() {
		return mWifiServiceBinder;
	}
	
	
	@Override
	public void onCreate(){
		super.onCreate();
		sSocialApp = this;
		
		startLongRunService();		
		
		//connect to clound server
		CloundServer.getInstance().init();
	}
	
	
	//only available on emulator
	@Override
	public void onTerminate() {
		super.onTerminate();
	}
	

	public static SocialApplication getAppInstance() {
		return sSocialApp;
	}


	/**
	 * stop everything
	 * */
	public void exit() {
		
		CloundServer.getInstance().fini();
				
		try {
			Intent intent = new Intent(this,UpdateService.class);
			stopService(intent);		
			if(mServiceConn != null) {
				unbindService(mServiceConn);
				mServiceConn = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}	
		
		//mHandler.postDelayed(mExitApp, 1000);
	}
	
	private Runnable mExitApp = new Runnable() {
		
		@Override
		public void run() {
			System.exit(0);			
		}
	};
}

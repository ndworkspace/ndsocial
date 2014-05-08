package cn.nd.social.net;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import cn.nd.social.R;
import cn.nd.social.util.Utils;

public class PrivateSwitcher {
	private boolean mIsMobileDataPreOn;
	private boolean mIsWifiPreOn;
	private boolean mIsBTOn;
	
	
	
	private ConnectivityManager mConMan;
	private WifiManager mWifiManager;
	public PrivateSwitcher() {
		mConMan = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiManager = (WifiManager)getContext().getSystemService(Context.WIFI_SERVICE);
	}
	
	public void enterPrivateState() {
		enterPrivateState(false);
	}
	
	public void enterPrivateState(boolean closeWifi) {
		mIsWifiPreOn = mWifiManager.isWifiEnabled();
		if(mIsWifiPreOn && closeWifi) {
			mWifiManager.setWifiEnabled(false);
		}
		mIsMobileDataPreOn = isDataOn();
		if(mIsMobileDataPreOn) {
			turnData(false);
		}
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		mIsBTOn = btAdapter.isEnabled();
		if(mIsBTOn) {
			btAdapter.disable();
		}
	}
	
	public void exitPrivateState() {
		if(mIsMobileDataPreOn) {
			turnData(true);
		}
		
		if(mIsWifiPreOn) {
			mWifiManager.setWifiEnabled(true);
		}
		
		if(mIsBTOn) {
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			btAdapter.enable();
		}
		
	}
	
	private Context getContext() {
		return Utils.getAppContext();
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean isDataOn() {
		Class cmClass = mConMan.getClass();
		Class[] argClasses = null;
		Object[] argObject = null;
		Boolean isOpen = false;
		try {
			Method method = cmClass.getMethod("getMobileDataEnabled", argClasses);
			isOpen = (Boolean) method.invoke(mConMan, argObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isOpen;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void turnData(boolean on) {
		try {
			Class conmanClass = Class.forName(mConMan.getClass().getName());
			final Field iConMgrField = conmanClass
					.getDeclaredField("mService");
			iConMgrField.setAccessible(true);
			final Object iConMgr = iConMgrField
					.get(mConMan);
			final Class cmClass = Class
					.forName(iConMgr.getClass().getName());
			
			final Method setDataEnableMethod = cmClass
					.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setDataEnableMethod.setAccessible(true);
			setDataEnableMethod.invoke(iConMgr, on);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showPrivateDialog(Context context,final String key,int msgId) {
		if (Utils.getAppSharedPrefs().getBoolean(key, false)) {
			return;
		}
		AlertDialog.Builder fileDialog = new AlertDialog.Builder(context);
		fileDialog.setTitle(R.string.hint);
		fileDialog.setMessage(msgId);
		fileDialog.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		fileDialog.setNegativeButton(R.string.enter_private_no_hint,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						SharedPreferences.Editor editor = Utils
								.getAppSharedPrefs().edit();
						editor.putBoolean(key, true);
						editor.commit();
						dialog.dismiss();
					}
		});
		fileDialog.create().show();
	}
}

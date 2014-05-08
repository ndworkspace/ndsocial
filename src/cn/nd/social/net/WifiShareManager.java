package cn.nd.social.net;

import java.util.List;

import cn.nd.social.hotspot.WifiApAdmin;
import cn.nd.social.net.WifiController.WifiConnectState;
import cn.nd.social.util.LogToFile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiShareManager {
	
	public static String WIFIACTION_WIFI_OPENING = "com.nd.android.wifi.WIFIACTION_WIFI_OPENING";
	public static String WIFIACTION_WIFI_OPENERROR = "com.nd.android.wifi.WIFIACTION_WIFI_OPENERROR";
	public static String WIFIACTION_WIFI_OPENSUCESS = "com.nd.android.wifi.WIFIACTION_WIFI_OPENSUCESS";
	public static String WIFIACTION_WIFI_OPENTIMEOUT = "com.nd.android.wifi.WIFIACTION_WIFI_OPENTIMEOUT";
	
	public static String WIFIACTION_WIFI_CLOSEING = "com.nd.android.wifi.WIFIACTION_WIFI_CLOSEING";
	public static String WIFIACTION_WIFI_CLOSED = "com.nd.android.wifi.WIFIACTION_WIFI_CLOSED";
	
	public static String WIFIACTION_AP_CREATING = "com.nd.android.wifi.WIFIACTION_AP_CREATING";
	public static String WIFIACTION_AP_CREATE_SUCCESS = "com.nd.android.wifi.WIFIACTION_AP_CREATE_SUCCESS";
	public static String WIFIACTION_AP_CREATE_TIMEOUT = "com.nd.android.wifi.WIFIACTION_AP_CREATE_TIMEOUT";
	public static String WIFIACTION_AP_CREATE_ERROR = "com.nd.android.wifi.WIFIACTION_AP_CREATE_ERROR";
	public static String WIFIACTION_AP_BREAK = "com.nd.android.wifi.WIFIACTION_AP_BREAK";
	public static String WIFIACTION_AP_CLOSED = "com.nd.android.wifi.WIFIACTION_AP_CLOSED";
	
	public static String WIFIACTION_CONNECT_CONNECTING = "com.nd.android.wifi.WIFIACTION_CONNECT_CONNECTING";
	public static String WIFIACTION_CONNECT_CONNECT_ERROR = "com.nd.android.wifi.WIFIACTION_CONNECT_CONNECT_ERROR";
	public static String WIFIACTION_CONNECT_CONNECT_SUCCESS = "com.nd.android.wifi.WIFIACTION_CONNECT_CONNECT_SUCCESS";
	public static String WIFIACTION_CONNECT_CONNECT_TIMEOUT = "com.nd.android.wifi.WIFIACTION_CONNECT_CONNECT_TIMEOUT";
	public static String WIFIACTION_CONNECT_CONNECT_APNOTFIND = "com.nd.android.wifi.WIFIACTION_CONNECT_APNOTFIND";
	public static String WIFIACTION_CONNECT_CONNECT_BREAK = "com.nd.android.wifi.WIFIACTION_CONNECT_BREAK";
	
	static WifiShareManager mWifiShareManager;

	private WifiManager mWifiManager;
	private WifiApManager mwifiApManager;
	private Context mContext = null;
	private int wifiState = 0; 
	private int apState = 0;
	private int connectState = 0;
	private int DEFAULT_TIMEOUT = 30;
	
	private static int STATE_WIFI_OPENING = 1;
	private static int STATE_WIFI_OPENERROR = 2;
	private static int STATE_WIFI_OPENED = 3;
	private static int STATE_WIFI_OPENTIMEOUT = 4;
		
	private static int STATE_WIFI_CLOSEING = 5;
	private static int STATE_WIFI_CLOSED = 6;
	private static int STATE_WIFI_OPENING_PAUSE = 7;
	
	private static int STATE_AP_CREATING = 1;
	private static int STATE_AP_CREATE_ERROR = 2;
	private static int STATE_AP_CREATE_SUCCESS = 3;
	private static int STATE_AP_CREATE_TIMEOUT = 4;
	private static int STATE_AP_CREATE_BREAK = 5;
	public static int STATE_AP_CLOSED = 6;
	public static int STATE_AP_CREATED_PAUSE = 7;
	
	public static int STATE_CONNECT_CONNECTING = 1;
	public static int STATE_CONNECT_CONNECT_ERRO = 2;
	public static int STATE_CONNECT_CONNECT_SUCCESS = 3;
	public static int STATE_CONNECT_CONNECT_TIMEOUT = 4;
	public static int STATE_CONNECT_CONNECT_APNOTFIND = 5;
	public static int STATE_CONNECT_CONNECT_BREAK = 6;
	
	
	private boolean wifiOldStateEnabled;
	private boolean wifiApOldStateEnabled;
	
	@SuppressWarnings("unused")
	private String mSSID = null;
	private String mPassword = null;
	
	private boolean wifiScaning;
	
	
	/**
	 * temporary solution, restore the original wifi accesspoint
	 * use static in case creatAp and closeAp not call in pairs 
	 *TODO: find elegant way
	 **/	
	private static WifiConfiguration sOriginalConfig = null;
	
	private WifiShareManager() {
		super();
	}

	
	private void notifyWifiAPState() {
	    
	    if(apState == STATE_AP_CREATE_SUCCESS) {
	    	boolean wifiApEnable = WifiApAdmin.isWifiApEnabled(mWifiManager);
	    	if(!wifiApEnable) {
	    		apState = STATE_AP_CREATE_BREAK;
	    		sendBroadcast(WIFIACTION_AP_BREAK);
	    	}
	    }
	    LogToFile.e("WifiShareManager", "notifyWifiAPState :" + WifiApAdmin.isWifiApEnabled(mWifiManager));
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
				if(connectState == STATE_CONNECT_CONNECT_SUCCESS){
					int state = (Integer) intent.getExtras().get(WifiManager.EXTRA_WIFI_STATE);
					if(state == WifiManager.WIFI_STATE_DISABLED || state == WifiManager.WIFI_STATE_UNKNOWN){
						wifiState = STATE_CONNECT_CONNECT_BREAK;
						disconnectWifi();
						sendBroadcast(WIFIACTION_CONNECT_CONNECT_BREAK);
					}
				}
			 }else if (intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {//WifiManager.WIFI_AP_STATE_CHANGED_ACTION
                notifyWifiAPState();
            } 
		}
	};
	
	private WifiShareManager(Context context){
		mContext = context.getApplicationContext();
		// ȡ��WifiManager����
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mwifiApManager = new WifiApManager(mWifiManager);
		wifiOldStateEnabled = (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
		if(wifiOldStateEnabled){
			wifiState = STATE_WIFI_CLOSED;
		}else if(wifiOldStateEnabled){
			wifiState = STATE_WIFI_OPENED;
		}
		wifiApOldStateEnabled = mwifiApManager.isWifiApEnabled();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mContext.registerReceiver(receiver, filter);
	}
	
	public void setOldWifiState(){
		if(apState==STATE_AP_CREATE_SUCCESS){
			apState = STATE_AP_CREATED_PAUSE;
			mwifiApManager.closeWifiAp();
		}
		if(!wifiOldStateEnabled && wifiState==STATE_WIFI_OPENED){
			mWifiManager.disconnect();
			connectState = STATE_CONNECT_CONNECT_BREAK;
			mWifiManager.setWifiEnabled(false);
			wifiState = STATE_WIFI_OPENING_PAUSE;
		}
	}
	
	
	public void resetWifiState(){
		if(apState == STATE_AP_CREATED_PAUSE){
			createAPAsync(mSSID, mPassword);
		}
		if(wifiState == STATE_WIFI_OPENING_PAUSE){
			openWifiAndScanAsync(-1);
		}
		
	}
	
	private void sendBroadcast(final String action){
		Intent intnet = new Intent(action); 
		mContext.sendBroadcast(intnet); 
	}
	
	@Override
	protected void finalize() throws Throwable {
		mContext.unregisterReceiver(receiver);
		super.finalize();
	}
	
	public static WifiShareManager getShareInstance(Context context){
		if(mWifiShareManager == null){
			mWifiShareManager = new WifiShareManager(context);
		}
		return mWifiShareManager;
	}
	
	public void connectWifiAsync(final String ssid,final String password){
		new Thread(new Runnable() {
			@Override
			public void run() {
				LogToFile.e("WifiShareMgr","openWifi start");
				boolean flag = openWifi(30);
				LogToFile.e("WifiShareMgr","openWifi end");
				if(flag){
					connectWifi(ssid, password, -1);
				}
			}
		}).start();
	}
	
	public void createAPAsync(final String ssid,final String password){
		new Thread(new Runnable() {
			@Override
			public void run() {
				closeWifi();
				createAP(ssid,password, 15);

			}
		}).start();
	}
	
	public  void scanWifiAsync() {
		mWifiManager.startScan();
	}
	

	public void openWifiAsync(final int timeout) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int waitTime = timeout ;
				if(timeout == -1) {
					waitTime = 30;
				}
				openWifi(waitTime);
			}
		}).start();
	}
	
	public void openWifiAndScanAsync(final int timeout) {		
		new Thread(new Runnable() {
			@Override
			public void run() {
				int waitTime = timeout ;
				if(timeout == -1) {
					waitTime = 30;
				}
				if(openWifi(waitTime)) {
					wifiScaning = true;
					int time = 10;
					while(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING && wifiScaning && time>0 ){
						scanWifiAsync();
						try {
							time -- ;
							Thread.sleep(1000);
						}catch(InterruptedException e) {
							wifiScaning = false;
						}
					}
					wifiScaning = false;
				}
			}
		}).start();
	}
	
	public boolean openWifi(int timeout){
        mwifiApManager.closeWifiAp();
		boolean bRet = true;
		while(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!mWifiManager.isWifiEnabled()) {
			wifiState = STATE_WIFI_OPENING;
			sendBroadcast(WIFIACTION_WIFI_OPENING);
			bRet = mWifiManager.setWifiEnabled(true);
			if(!bRet){
				wifiState = STATE_WIFI_OPENERROR;
				sendBroadcast(WIFIACTION_WIFI_OPENERROR);
				mWifiManager.setWifiEnabled(false);
				return bRet;
			}
			if(timeout == - 1)
				timeout = DEFAULT_TIMEOUT;
			int count = timeout * 2;
			while(count > 0){
				if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
					wifiState = STATE_WIFI_OPENED;
					sendBroadcast(WIFIACTION_WIFI_OPENSUCESS);
					break;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count--;
			}
			if(wifiState != STATE_WIFI_OPENED){
				bRet = false;
				wifiState = STATE_WIFI_OPENTIMEOUT;
				mWifiManager.setWifiEnabled(false);
				sendBroadcast(WIFIACTION_WIFI_OPENTIMEOUT);
			}
		}
		return bRet;
	}
	
	private void removeSSID(){
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(existingConfigs != null){
			for (WifiConfiguration existingConfig : existingConfigs) {
				//tangtaotao@20140408 normal should not be null, but it happens, so just ensure not null
				if(existingConfig == null || existingConfig.SSID == null) {
					continue;
				}
				if (existingConfig.SSID.equals("\"" + mSSID + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {
					mWifiManager.disableNetwork(existingConfig.networkId);
					mWifiManager.removeNetwork(existingConfig.networkId);
					mWifiManager.saveConfiguration();
					break;
				}
			}
		}
	}
	
	public void closeWifi(){
		removeSSID();
		if (mWifiManager.isWifiEnabled()) {
			wifiState = STATE_WIFI_CLOSEING;
			sendBroadcast(WIFIACTION_WIFI_CLOSEING);
			mWifiManager.setWifiEnabled(false);
			sendBroadcast(WIFIACTION_WIFI_CLOSED);
			mSSID = null;
		}
		wifiState = STATE_WIFI_CLOSED;
		connectState = STATE_CONNECT_CONNECT_BREAK;
	}
	
	
	public void disconnectWifi(){
		removeSSID();
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.disconnect();
			connectState = STATE_CONNECT_CONNECT_BREAK;
		}
	}
	

	
	public boolean createAP(String ssid,String password,int timeout){
		
		if(sOriginalConfig == null) {
			sOriginalConfig = mwifiApManager.getWifiApConfiguration();
		}
		
		
		this.mSSID = ssid;
		this.mPassword = password;
		boolean flag = true;
		if (mWifiManager.isWifiEnabled()){
			closeWifi();
		}
		apState = STATE_AP_CREATING;
		sendBroadcast(WIFIACTION_AP_CREATING);
		flag = mwifiApManager.stratWifiAp(ssid,password);
		if(!flag){
			apState = STATE_AP_CREATE_ERROR;
			mwifiApManager.closeWifiAp();//tangtaotao add_20140331
			sendBroadcast(WIFIACTION_AP_CREATE_ERROR);
			return false;
		}
		if(timeout == - 1)
			timeout = DEFAULT_TIMEOUT;
		int count = timeout * 2;
		while(count > 0){
			if(mwifiApManager.isWifiApEnabled()){
				WifiConfiguration configuration = mwifiApManager.getWifiApConfiguration();
				if(configuration.SSID.equals(ssid)){
					apState = STATE_AP_CREATE_SUCCESS;
					sendBroadcast(WIFIACTION_AP_CREATE_SUCCESS);
					flag = true;
					break;
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count--;
		}
		if(apState != STATE_AP_CREATE_SUCCESS){
			apState = STATE_AP_CREATE_TIMEOUT;
			mwifiApManager.closeWifiAp();//tangtaotao add_20140331
			sendBroadcast(WIFIACTION_AP_CREATE_TIMEOUT);
			flag = false; 
		}
		return flag;
		
	}
	
	public void closeAp(){
		apState = STATE_AP_CLOSED;
		sendBroadcast(WIFIACTION_AP_CLOSED);
		if(sOriginalConfig != null) {
			mwifiApManager.setWifiApConfiguration(sOriginalConfig);
			sOriginalConfig = null;
		}
		mwifiApManager.closeWifiAp();
	}
	
	public boolean isWifiApEnabled(){
		return mwifiApManager.isWifiApEnabled();
	}
	
	
	public boolean connectWifi(String ssid,String password,int timeout){
		wifiScaning = false;
		
		boolean flag = true;		
		
		int scanfCount = 10;
		boolean findflag = false;
		while(!findflag && scanfCount>0){
			List<ScanResult> results =  mWifiManager.getScanResults();
			if(results != null){
				for (ScanResult scanResult : results) {
					if(scanResult.SSID != null && (scanResult.SSID.equals("\"" + ssid + "\"") || scanResult.SSID.equals(ssid) )) {
						findflag = true;
						break;
					}
				}
			}

			
			if(!findflag){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mWifiManager.startScan();     
				scanfCount--;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(!findflag){
			connectState = STATE_CONNECT_CONNECT_APNOTFIND;
			sendBroadcast(WIFIACTION_CONNECT_CONNECT_APNOTFIND);
			flag = false; 
			return flag;
		}
		
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + ssid + "\"";
		this.mSSID = ssid;
		config.status = WifiConfiguration.Status.ENABLED;
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(existingConfigs != null){
			for (WifiConfiguration existingConfig : existingConfigs) {
				//tangtaotao@20140408 normal should not be null, but it happens, so just ensure not null
				if(existingConfig == null || existingConfig.SSID == null) {
					continue;
				}
				if (existingConfig.SSID.equals("\"" + ssid + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {
					mWifiManager.removeNetwork(existingConfig.networkId);
				}
			}
		}
		config.hiddenSSID = true;
//		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		
		
		
		 config.preSharedKey = "\"" + password + "\"";  
		 
		 config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);  
		 config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		 config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		 config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);  
		 config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);  
		 config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);  
		 config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
		 config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  

		
		
		int wcgID = mWifiManager.addNetwork(config);
		if(wcgID == -1){
			connectState = STATE_CONNECT_CONNECT_ERRO;
			disconnectWifi();
			sendBroadcast(WIFIACTION_CONNECT_CONNECT_ERROR);
			return false;
		}
		connectState = STATE_CONNECT_CONNECTING;
		sendBroadcast(WIFIACTION_CONNECT_CONNECTING);
		flag = mWifiManager.enableNetwork(wcgID, true);
		mWifiManager.saveConfiguration();
		if(!flag){
			connectState = STATE_CONNECT_CONNECT_ERRO;
			sendBroadcast(WIFIACTION_CONNECT_CONNECT_ERROR);
			return false;
		}
		
		

		List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
		for( WifiConfiguration i : list ) {
		    if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
		    	mWifiManager.disconnect();
		    	wcgID = i.networkId;
		    	mWifiManager.enableNetwork(i.networkId, true);
		    	mWifiManager.reconnect(); 
		        break;
		    }           
		 }

		
		if(timeout == - 1)
			timeout = DEFAULT_TIMEOUT;
		int count = timeout * 2;
		while(count > 0){
			WifiInfo wifiInfo =  mWifiManager.getConnectionInfo();
			if(wifiInfo !=null && wifiInfo.getSSID()!=null && (wifiInfo.getSSID().equals("\"" + ssid + "\"") || wifiInfo.getSSID().equals(ssid) )&& wifiInfo.getSupplicantState() == SupplicantState.COMPLETED){
				boolean isWifiConnected = false;
				int i = 0;
				while (!isWifiConnected && i < 50) {
					try {
						Thread.currentThread();
						Thread.sleep(300);
					} catch (InterruptedException ie) {
					}

					ConnectivityManager connManager = (ConnectivityManager) mContext
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo mNetworkInfo = connManager
							.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					// NetworkInfo mWifi = connManager.getActiveNetworkInfo();
					isWifiConnected = (mNetworkInfo != null
							&& mNetworkInfo.isConnected() && (mNetworkInfo.getState() == NetworkInfo.State.CONNECTED));

					i++;
				}
				if(isWifiConnected) {
				connectState = STATE_CONNECT_CONNECT_SUCCESS;
				sendBroadcast(WIFIACTION_CONNECT_CONNECT_SUCCESS);
				}
				break;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count--;
		}
		if(connectState != STATE_CONNECT_CONNECT_SUCCESS){
			connectState = STATE_CONNECT_CONNECT_TIMEOUT;
			disconnectWifi();
			sendBroadcast(WIFIACTION_CONNECT_CONNECT_TIMEOUT);
			flag = false; 
		}
		return flag;
		
	}

}

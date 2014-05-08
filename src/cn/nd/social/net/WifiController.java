package cn.nd.social.net;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import cn.nd.social.hotspot.MyTimerCheck;
import cn.nd.social.hotspot.WifiApAdmin;
import cn.nd.social.hotspot.WifiCheckThread;
import cn.nd.social.util.LogToFile;
import cn.nd.social.util.Utils;


public class WifiController {
	private WifiCheckThread mWifiCheckThread;
	private Context mContext;
	private WifiConnectState mState = WifiConnectState.NONE;
	private WifiManager mWifiManager;
	private WifiApAdmin mWifiAp;
	private HotspotCheckThread mHotspotCheck;
	
	 private static final String WPA_CAP = "[WPA";
	  private static final String WEP_CAP = "[WEP";
	  
	  // faster than enums
	  public static final int CRYPTO_NONE = 0;
	  public static final int CRYPTO_WEP = 1;
	  public static final int CRYPTO_WPA = 2;
	
	private static  WifiController sWifiController = new WifiController();
	
	public enum WifiConnectState {
		NONE,
		CONNECT_PENDING,
		WIFI_CONNECTED,
		WIFI_CONNECT_FAIL,
		CREATE_AP_PENDING,
		CREATE_AP_FAIL,
		WIFI_AP,
		WIFI_CONNECT_CUT_OFF
	}
	
	private static String HOTSPOT_TYPE = "WPA PSK";
	
	public final static String ACTION_WIFI_EVENT_NOTIFY = "cn.nd.social.wifi_event_notify";
	public final static String ACTION_HOTSPOT_EVENT_NOTIFY = "cn.nd.social.hotspot_event_notify";
	public final static String ACTION_AP_STATE_CHANGE = "cn.nd.social.ap_state_change";
	public final static String KEY_CONNECT_STATE = "connect_state";
	public final static String KEY_EXTRA_INFO = "extra_info";
	
	private final static String KEY_AP_SSID = "ap_ssid";// access point name (hotspot ssid)
	private final static String KEY_AP_PWD = "ap_passwd";
	
	private WifiController() {
		mContext = Utils.getAppContext();
		mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		WifiReceiver wifiReceiver = new WifiReceiver();
		IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
		mContext.registerReceiver(wifiReceiver, filter);
	}
	
	public static WifiController getInstance() {
		return sWifiController;
	}
	
	public static int getCryptoType(final ScanResult scanResult){
		
		String capabilities = (scanResult.capabilities==null?"":scanResult.capabilities);
		if ( capabilities.indexOf( WPA_CAP ) >= 0 ) {
		    return CRYPTO_WPA;
		}
		else if ( capabilities.indexOf( WEP_CAP ) >= 0 ) {
		    return CRYPTO_WEP;
		}
		else {
		    return CRYPTO_NONE;
		}
	}
	
	public void connectToWifi(String ssid,String passwd) {
		// need to close hotspot first
		closeHotspot(); 

		mWifiCheckThread = new WifiCheckThread();
		mWifiCheckThread.init(mContext, true);
		mWifiCheckThread.setWifiInfo(ssid, passwd, HOTSPOT_TYPE);
		mWifiCheckThread.start();
		LogToFile.e("WifiController", "connectToWifi: ssid:" + ssid + " passwd:" + passwd);

		MyTimerCheck timerCheck = new MyTimerCheck() {
			@Override
			public void doTimerCheckWork() {
				int netState = mWifiCheckThread.mNetworkState;
				if (netState != WifiCheckThread.WIFI_STATE_NONE) {
					if (netState == WifiCheckThread.WIFI_STATE_CONNECT_SUCCESS) {
						wifiEventNotify(WifiConnectState.WIFI_CONNECTED);
					} else {
						wifiEventNotify(WifiConnectState.WIFI_CONNECT_FAIL);
					}
					exit();
				} 
			}

			@Override
			public void doTimeOutWork() {
				wifiEventNotify(WifiConnectState.WIFI_CONNECT_FAIL);
			}
		};
		timerCheck.start(24, 1000);
	}
	
	public void closeWifi() {
		//WifiCheckThread.findConfigIfExist(mWifiManager,);
		//mWifiManager.disableNetwork(netId);
		//mWifiManager.disconnect();
	}
	
	public void closeWifi(String ssid,String passwd) {
		WifiConfiguration config = WifiCheckThread.findConfigIfExist(mWifiManager,ssid,passwd);
		config.hiddenSSID =true;
		if(config != null) {
			mWifiManager.disableNetwork(config.networkId);
			mWifiManager.removeNetwork(config.networkId);
			
			//mWifiManager.updateNetwork(config);
			//mWifiManager.
		}
		//
		//mWifiManager.disconnect();
	}
	
	
	public static String genHotspotName() {
		String apSSID = Utils.getAppSharedPrefs().getString(KEY_AP_SSID, "");		
		if(apSSID != null && !apSSID.equals("")) {
			return apSSID;
		}
		int num = (int) (Math.random() * 1000000) + 123456;
		apSSID = "nd_" + Build.MANUFACTURER.substring(0, 3) + num;
		Utils.getAppSharedPrefs().edit().putString(KEY_AP_SSID, apSSID).commit();
		return apSSID;
	}
	
	public static String genHotspotPasswd() {		
		String apPwd = Utils.getAppSharedPrefs().getString(KEY_AP_PWD, "");		
		if(apPwd != null && !apPwd.equals("")) {
			return apPwd;
		}
		int num = (int) (Math.random() * 100000) + 54321;
		apPwd = "go_" + num;
		Utils.getAppSharedPrefs().edit().putString(KEY_AP_PWD, apPwd).commit();
		return apPwd;
	}
	
	
	
	public class HotspotCheckThread extends MyTimerCheck {
		String ssid;
		String passwd;
		boolean isCancelled = false;
		HotspotCheckThread(String ssid,String passwd) {
			this.ssid = ssid;
			this.passwd = passwd;
		}
		@Override
		public void doTimerCheckWork() {
			if (WifiApAdmin.isWifiApEnabled(mWifiManager)) {
				if(isCancelled) {
					WifiApAdmin.closeWifiAp(mWifiManager);				
				} else {
					String content = ssid + " " + passwd;
					hotspotNotify(WifiConnectState.WIFI_AP,content);					
				}
				mHotspotCheck = null;
				exit();
			}
		}
		@Override
		public void doTimeOutWork() {
			if(!isCancelled) {
				hotspotNotify(WifiConnectState.CREATE_AP_FAIL,"");
			}
			mHotspotCheck = null;
		}

		public void cancel(){
			isCancelled = true;
		}
	}
	
	public void createHotspot() {
		final String hotspotName = genHotspotName();
		final String passwd = genHotspotPasswd();
		createHotspot(hotspotName,passwd);
	}

	public void createHotspot(String ssid,String passwd) {
		if(mState == WifiConnectState.WIFI_CONNECTED) {
			wifiEventNotify(WifiConnectState.WIFI_CONNECT_CUT_OFF);
		}
		
		if(mWifiAp == null) {
			mWifiAp = new WifiApAdmin(mWifiManager);			
		} else if(WifiApAdmin.isWifiApEnabled(mWifiManager)) {
			//if the wifiAp is started, we need to close old accessPoint for update
			//if not, the ssid and passWd will not get updated
			WifiApAdmin.closeWifiAp(mWifiManager);
		}

		mWifiAp.startWifiAp(ssid, passwd);

		mHotspotCheck = new HotspotCheckThread(ssid,passwd);
		mHotspotCheck.start(30, 1000);
	}
	
	public void closeHotspot() {
		if(mHotspotCheck != null) {
			mHotspotCheck.cancel();
		} else if(WifiApAdmin.isWifiApEnabled(mWifiManager)) {
			WifiApAdmin.closeWifiAp(mWifiManager);
		}
	}
	
	public WifiConnectState getWifiState() {
		return mState;
	}
	
	private void wifiEventNotify(WifiConnectState state) {
		wifiEventNotify(state,"");
	}
	
	private void wifiEventNotify(WifiConnectState state,String info) {
		mState = state;
		Intent intent = new Intent(ACTION_WIFI_EVENT_NOTIFY);
		intent.putExtra(KEY_EXTRA_INFO, info);
		intent.putExtra(KEY_CONNECT_STATE, state.ordinal());
		Utils.getAppContext().sendBroadcast(intent);
	}
	
	private void hotspotNotify(WifiConnectState state,String info) {
		mState = state;
		Intent intent = new Intent(ACTION_HOTSPOT_EVENT_NOTIFY);
		intent.putExtra(KEY_EXTRA_INFO, info);
		intent.putExtra(KEY_CONNECT_STATE, state.ordinal());
		Utils.getAppContext().sendBroadcast(intent);
	}
	
	private void hotspotStateChange(WifiConnectState state,String info) {
		mState = state;
		Intent intent = new Intent(ACTION_AP_STATE_CHANGE);
		intent.putExtra(KEY_EXTRA_INFO, info);
		intent.putExtra(KEY_CONNECT_STATE, state.ordinal());
		Utils.getAppContext().sendBroadcast(intent);
	}
	
	
	
    private class WifiReceiver extends BroadcastReceiver {
    	//WifiManager.NETWORK_STATE_CHANGED_ACTION
    	int mWifiState = WifiManager.WIFI_STATE_UNKNOWN;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("WifiReceiver", "onReceive() is calleld with " + intent);
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                notifyWifiState();
            } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {//WifiManager.WIFI_AP_STATE_CHANGED_ACTION
                notifyWifiAPState();
            } else {
                return;
            }
        }
        
       private void notifyWifiState() {
    	   LogToFile.e("WifiController", "notifyWifiState :" + mWifiState);
       }
       
       private void notifyWifiAPState() {
    	   boolean wifiApEnable = WifiApAdmin.isWifiApEnabled(mWifiManager);
    	   if(wifiApEnable) {
    		   if(mState == WifiConnectState.CREATE_AP_FAIL) {
        		   //hotspotStateChange(WifiConnectState.WIFI_AP, "");
        	   }
    	   } else {
    		   if(mState == WifiConnectState.WIFI_AP) {
    			   hotspotStateChange(WifiConnectState.CREATE_AP_FAIL, "");
        	   }
    	   }
    	   
    	   LogToFile.e("WifiController", "notifyWifiAPState :" + WifiApAdmin.isWifiApEnabled(mWifiManager));
       }
       
    }
    
    
    public boolean disconnectAP() {
        // remove saved networks
        List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();
        Log.v("WifiController", "size of wifiConfigList: " + wifiConfigList.size());
        for (WifiConfiguration wifiConfig: wifiConfigList) {
            Log.v("WifiController", "Remove wifi configuration: " + wifiConfig.networkId);
            int netId = wifiConfig.networkId;
            //TODO use reflection
            //mWifiManager.forget(netId, null);
        }
        return true;
    }
}

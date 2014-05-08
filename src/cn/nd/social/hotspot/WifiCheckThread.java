package cn.nd.social.hotspot;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiCheckThread extends Thread {

	public final static int WIFI_STATE_NONE = 0;
	public final static int WIFI_STATE_CONNECT_SUCCESS = 1;
	public final static int WIFI_STATE_CONNECT_FAILED = 2;

	private Context mContext = null;

	public int mNetworkState = WIFI_STATE_NONE;

	private boolean mIsOpen = false;

	public void init(Context context, boolean isOpen) {
		mContext = context;

		mIsOpen = isOpen;
		mNetworkState = WIFI_STATE_NONE;
	}

	private String mWifiName = "";
	private String mWifiPwd = "";
	private String mWifiSecurity = "";

	public void setWifiInfo(String name, String pwd, String security) {
		Log.d("WifiCheckThread", "wifi name [" + name + "]");
		mWifiName = name;
		mWifiPwd = pwd;
		mWifiSecurity = security;
	}

	public void run() {
		if (mIsOpen) {
			//comment out by tangtaotao 20140110_16_00
			//closeWifi();

			mNetworkState = WIFI_STATE_NONE;

			// try {
			// Thread.sleep(1000);
			// }
			// catch (InterruptedException e) {
			// e.printStackTrace();
			// }

			mNetworkState = connectWifi();
			//mNetworkState = connectWifi(mWifiName,mWifiPwd);
		} else {
			closeWifi();

			mNetworkState = WIFI_STATE_NONE;
		}
	}
	
	public static WifiConfiguration findConfigIfExist(WifiManager wifi,String ssid,String passwd) {
		WifiConfiguration config = null;
		List<WifiConfiguration> existingConfigs = wifi
				.getConfiguredNetworks();
		if (existingConfigs != null) {
			for (WifiConfiguration existingConfig : existingConfigs) {
				if(existingConfig == null) {
					continue;
				}
				boolean passwdSame,ssidSame;
/*				if(existingConfig.preSharedKey == null 
						|| existingConfig.preSharedKey.equals("")) {
					passwdSame = (passwd == null || passwd.equals(""));
				} else {
					passwdSame = existingConfig.preSharedKey.equals("\"" + passwd + "\"");
				}*/

				ssidSame = existingConfig.SSID.equals("\"" + ssid + "\"");

				if(/*passwdSame &&*/ ssidSame) {
					config = existingConfig;
					break;
				}
			}
		}
		return config;
	}
	
	private int getWifiConfigNetId(WifiManager wifi) {
		WifiConfiguration wcg = findConfigIfExist(wifi,mWifiName,mWifiPwd);
		if(wcg != null) {
			return wcg.networkId;
		} else {
			wcg = new WifiConfiguration();
			wcg.SSID = "\"" + mWifiName + "\"";
	
			// wcg.BSSID = wifi.getConnectionInfo().getMacAddress();
	
			wcg.preSharedKey = "\"" + mWifiPwd + "\"";
	
			wcg.status = WifiConfiguration.Status.ENABLED;
	
			wcg.allowedAuthAlgorithms.clear();
			wcg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wcg.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
	
			wcg.allowedGroupCiphers.clear();
			wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	
			wcg.allowedKeyManagement.clear();
			wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	
			wcg.allowedPairwiseCiphers.clear();
			wcg.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			wcg.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
	
			wcg.allowedProtocols.clear();
			wcg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wcg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			int netID = wifi.addNetwork(wcg);
			return netID;
		}

	}
	
	
	private int connectWifi() {
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);

		int connectCount = 0;

		while (connectCount < 5) {

			if (!wifi.isWifiEnabled()) {
				wifi.setWifiEnabled(true);

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// todo : prevent forever loop
				while (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
					try {
						Thread.currentThread();
						Thread.sleep(200);
					} catch (InterruptedException ie) {
					}
				}
			}


			
			wifi.startScan();

			connectCount++;

			int netID = getWifiConfigNetId(wifi);
			
			Log.d("WifiPreference", "add Network returned netID : " + netID);
			
			



			// String mSecurity = mWifiSecurity ;
			// //String mSecurity = "WPA PSK";
			//
			// wcg.hiddenSSID = false;
			// wcg.status = WifiConfiguration.Status.ENABLED;
			// wcg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			// wcg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			// wcg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// wcg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			// wcg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			// wcg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			//
			// // WEP
			// if (mSecurity.equals("WEP")) {
			// wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			// wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			// wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			// //wcg.wepKeys[0] = "\"gameo\""; // This is the WEP Password
			// wcg.wepKeys[0] = "\"" + mWifiPwd + "\"";
			// wcg.wepTxKeyIndex = 0;
			// }
			// // WPA EPA
			// else if (mSecurity.equals("WPA EAP")) {
			// wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
			// wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			// wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			// // wcg.preSharedKey = "\"" + editText.getText().toString() +
			// "\"";
			// wcg.preSharedKey = "\"" + mWifiPwd + "\"";
			// }
			// // WPA PSK
			// else if (mSecurity.equals("WPA PSK")) {
			// wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			// wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			// wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			// //wcg.preSharedKey = "\"12345678\"";
			// wcg.preSharedKey = "\"" + mWifiPwd + "\"";
			// }
			// //
			// else {
			// wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			// }



			// if (netID == -1){
			// wifi.startScan();
			//
			// try {
			// Thread.sleep(3000);
			// }
			// catch(InterruptedException e) {
			// e.printStackTrace();
			// }
			//
			// Log.w("WifiPreference", "netID is -1!!!");
			//
			// List<WifiConfiguration> existingConfigs =
			// wifi.getConfiguredNetworks();
			// if (existingConfigs != null) {
			// for (WifiConfiguration existingConfig : existingConfigs)
			// {
			// if (existingConfig.SSID.equals(wcg.SSID))
			// {
			// //wifi.removeNetwork(existingConfig.networkId);
			// netID = existingConfig.networkId;
			// }
			// }
			//
			// Log.w("WifiPreference", "netID from existing config is " +
			// netID);
			// }
			// else {
			// Log.w("WifiPreference", "can't find existing configs!!!");
			// }
			// }

			boolean b = wifi.enableNetwork(netID, true);
			Log.d("WifiPreference", "enableNetwork returned " + b);

			if (!b) {
				wifi.setWifiEnabled(false);

				if (connectCount < 5) {
					try {
						Thread.currentThread();
						Thread.sleep(500);
					} catch (InterruptedException ie) {
					}

					continue;
				}

				break;
			} else {
				break;
			}
		}
		// Toast.makeText(mContext, "WIFI connect...",
		// Toast.LENGTH_LONG).show();

		// check wifi real connected
		boolean isWifiConnected = false;
		int i = 0;
		while (!isWifiConnected && i < 400) {
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

		WifiInfo info = wifi.getConnectionInfo();
		String curSSID = info.getSSID();
		if ((curSSID == null)
				|| !((curSSID.contentEquals(mWifiName) || curSSID
						.contentEquals("\"" + mWifiName + "\"")))) {
			Log.e("WifiCheckThread", "wifi open SSID failed, want ["
					+ mWifiName + "] actual open[" + info.getSSID() + "]");
			return WIFI_STATE_CONNECT_FAILED;
		}

		if (i < 400) {
			// Toast.makeText(mContext, "Wifi Connected!",
			// Toast.LENGTH_LONG).show();
			return WIFI_STATE_CONNECT_SUCCESS;
		} else {
			// Toast.makeText(mContext, "Wifi Connect Failed!",
			// Toast.LENGTH_LONG).show();
			return WIFI_STATE_CONNECT_FAILED;
		}
	}

	private boolean closeWifi() {
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()) {
			wifi.setWifiEnabled(false);
		}

		return true;
	}
}

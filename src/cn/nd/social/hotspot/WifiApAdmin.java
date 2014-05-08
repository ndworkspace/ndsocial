package cn.nd.social.hotspot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * �����ȵ�
 * 
 */
public class WifiApAdmin {
	public static final String TAG = "WifiApAdmin";

	private WifiManager mWifiManager = null;

	private String mSSID = "";
	private String mPasswd = "";
	
	private static WifiConfiguration sWifiConfig = null;

	public WifiApAdmin(WifiManager wifiManager) {
		mWifiManager = wifiManager;

		closeWifiAp(mWifiManager);
	}

	public void startWifiAp(String ssid, String passwd) {
		mSSID = ssid;
		mPasswd = passwd;

		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}

		setWifiApEnabled(true);
	}


	public static boolean isHtcROM() {
		boolean isHtc;
		try {
			isHtc = null != WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
		} catch (java.lang.NoSuchFieldException e) {
			isHtc = false;
		}
		return isHtc;
	}
	
	public boolean setWifiApEnabled(boolean enabled) {
		boolean isHtc = isHtcROM();		

		try {

			WifiConfiguration apConfig = getWifiConfiguration(mWifiManager,
					mSSID, mPasswd);

			if (isHtc)
				setHTCSSID(apConfig);

			Method method = mWifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

			return (Boolean) method.invoke(mWifiManager, apConfig, enabled);

		} catch (Exception e) {
			return false;
		}
	}

	public void setHTCSSID(WifiConfiguration config) {
		try {
			Field mWifiApProfileField = WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
			mWifiApProfileField.setAccessible(true);
			Object apProfile = mWifiApProfileField.get(config);
			mWifiApProfileField.setAccessible(false);

			if (apProfile != null) {
				Field ssidField = apProfile.getClass().getDeclaredField(
						"SSID");
				ssidField.setAccessible(true);
				ssidField.set(apProfile, config.SSID);
				ssidField.setAccessible(false);

				Field preShareKey = apProfile.getClass().getDeclaredField(
						"key");
				preShareKey.setAccessible(true);
				preShareKey.set(apProfile, config.preSharedKey);
				preShareKey.setAccessible(false);

				Field bssid = apProfile.getClass().getDeclaredField(
						"BSSID");
				bssid.setAccessible(true);
				bssid.set(apProfile, config.BSSID);
				bssid.setAccessible(false);

				Field secureType = apProfile.getClass().getDeclaredField(
						"secureType");
				secureType.setAccessible(true);
				secureType.set(apProfile, "wpa-psk");
				secureType.setAccessible(false);

				Field dhcpEnable = apProfile.getClass().getDeclaredField(
						"dhcpEnable");
				dhcpEnable.setAccessible(true);
				dhcpEnable.setInt(apProfile, 1);
				dhcpEnable.setAccessible(false);

			}
		} catch (Exception e) {
			e.printStackTrace();
			sWifiConfig = null;//exception happen, reset the state
		}
	}


	private static WifiConfiguration getCachedConfig(WifiManager wifiManager,boolean hasPasswd) {
		WifiConfiguration wifiConfig = null;
		if(sWifiConfig != null)  {
			wifiConfig = sWifiConfig;
		} else {		
			try {
				Object config = wifiManager.getClass()
						.getDeclaredMethod("getWifiApConfiguration", null)
						.invoke(wifiManager, new Object[0]);
				if ((config != null)
						&& (config instanceof WifiConfiguration)) {
					wifiConfig = (WifiConfiguration) config;
				} else {
					wifiConfig = new WifiConfiguration();
				}
			} catch (Exception localException) {
				Log.e("WifiApAdmin", "getWifiApConfiguration failed ");
				wifiConfig = new WifiConfiguration();
			}
			sWifiConfig = wifiConfig;
		}
		
		
		
		wifiConfig.allowedAuthAlgorithms.clear();
		wifiConfig.allowedAuthAlgorithms
				.set(WifiConfiguration.AuthAlgorithm.OPEN);

		wifiConfig.allowedProtocols.clear();
		wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

		wifiConfig.allowedKeyManagement.clear();
		wifiConfig.allowedKeyManagement.set(0);
		if (hasPasswd) {
			wifiConfig.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.WPA_PSK);

			wifiConfig.allowedPairwiseCiphers.clear();

			wifiConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			wifiConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);

			wifiConfig.allowedGroupCiphers.clear();
			wifiConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.CCMP);
			wifiConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.TKIP);

			wifiConfig.hiddenSSID = false;

			Log.e("get wifi configuration", "set config");

			return wifiConfig;
		}
		
		return wifiConfig;
	}
	
	public static WifiConfiguration getWifiConfiguration(
			WifiManager wifiManager, String ssid, String passwd) {
		WifiConfiguration wifiConfig = getCachedConfig(wifiManager,(passwd != null));
		wifiConfig.SSID = ssid;

		wifiConfig.BSSID = wifiManager.getConnectionInfo().getMacAddress();

		wifiConfig.preSharedKey = passwd;
		
		return wifiConfig;
	}

	public static void closeWifiAp(WifiManager wifiManager) {
		if (isWifiApEnabled(wifiManager)) {
			try {
				Method getConfig = wifiManager.getClass().getMethod(
						"getWifiApConfiguration");
				getConfig.setAccessible(true);

				WifiConfiguration config = (WifiConfiguration) getConfig
						.invoke(wifiManager);

				Method setApEnable = wifiManager.getClass().getMethod(
						"setWifiApEnabled", WifiConfiguration.class,
						boolean.class);
				setApEnable.invoke(wifiManager, config, false);
			} catch (NoSuchMethodException e) {
				Log.e("WifiApAdmin","setWifiApEnabled: NoSuchMethodException");
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				Log.e("WifiApAdmin","setWifiApEnabled: IllegalArgumentException");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.e("WifiApAdmin","setWifiApEnabled: IllegalAccessException");
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				Log.e("WifiApAdmin","setWifiApEnabled: InvocationTargetException");
				e.printStackTrace();
			}
		}
	}

	public static boolean isWifiApEnabled(WifiManager wifiManager) {
		try {
			Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (Boolean) method.invoke(wifiManager);

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}

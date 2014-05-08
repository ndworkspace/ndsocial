package cn.nd.social.net;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public class WifiApManager {
	private static final String tag = "WifiApManager";

	private static final String METHOD_GET_WIFI_AP_STATE = "getWifiApState";
	private static final String METHOD_SET_WIFI_AP_ENABLED = "setWifiApEnabled";
	private static final String METHOD_GET_WIFI_AP_CONFIG = "getWifiApConfiguration";
	private static final String METHOD_IS_WIFI_AP_ENABLED = "isWifiApEnabled";

	public static final int WIFI_AP_STATE_UNKWON = 2;
	public static final int WIFI_AP_STATE_OPEN = 1;
	public static final int WIFI_AP_STATE_CLOSE = 0;

	private static final Map<String, Method> methodMap = new HashMap<String, Method>();
	private static Boolean mIsSupport;
	private static boolean mIsHtc;

	public synchronized static final boolean isSupport() {
		if (mIsSupport != null) {
			return mIsSupport;
		}

		boolean result = Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO;
		if (result) {
			try {
				Field field = WifiConfiguration.class
						.getDeclaredField("mWifiApProfile");
				mIsHtc = field != null;
			} catch (Exception e) {
			}
		}

		if (result) {
			try {
				String name = METHOD_GET_WIFI_AP_STATE;
				Method method = WifiManager.class.getMethod(name);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = METHOD_SET_WIFI_AP_ENABLED;
				Method method = WifiManager.class.getMethod(name,
						WifiConfiguration.class, boolean.class);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = METHOD_GET_WIFI_AP_CONFIG;
				Method method = WifiManager.class.getMethod(name);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = getSetWifiApConfigName();
				Method method = WifiManager.class.getMethod(name,
						WifiConfiguration.class);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		if (result) {
			try {
				String name = METHOD_IS_WIFI_AP_ENABLED;
				Method method = WifiManager.class.getMethod(name);
				methodMap.put(name, method);
				result = method != null;
			} catch (SecurityException e) {
				Log.e(tag, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				Log.e(tag, "NoSuchMethodException", e);
			}
		}

		mIsSupport = result;
		return isSupport();
	}

	private final WifiManager mWifiManager;

	WifiApManager(WifiManager manager) {
		if (!isSupport()) {
			throw new RuntimeException("Unsupport Ap!");
		}
		Log.i(tag, "Build.BRAND -----------> " + Build.BRAND);

		mWifiManager = manager;
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	public int getWifiApState() {
		try {
			Method method = methodMap.get(METHOD_GET_WIFI_AP_STATE);
			return (Integer) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return WIFI_AP_STATE_UNKWON;
	}

	private WifiConfiguration getHtcWifiApConfiguration(
			WifiConfiguration standard) {
		WifiConfiguration htcWifiConfig = standard;

		try {
			Field mWifiApProfileField = WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
			mWifiApProfileField.setAccessible(true);
			Object apProfile = mWifiApProfileField.get(htcWifiConfig);
			mWifiApProfileField.setAccessible(false);

			if (apProfile != null) {
				Field ssidField = apProfile.getClass().getDeclaredField("SSID");
				htcWifiConfig.SSID = (String) ssidField.get(apProfile);
			}
		} catch (Exception e) {
			Log.e(tag, "" + e.getMessage(), e);
		}
		// not working in htc
		/*
		 * try { Object mWifiApProfileValue = BeanUtils.getProperty(standard,
		 * "mWifiApProfile");
		 * 
		 * if (mWifiApProfileValue != null) { htcWifiConfig.SSID =
		 * (String)BeanUtils.getProperty(mWifiApProfileValue, "SSID"); } } catch
		 * (Exception e) { Log.e(tag, "" + e.getMessage(), e); }
		 */
		return htcWifiConfig;
	}

	public WifiConfiguration getWifiApConfiguration() {
		WifiConfiguration configuration = null;
		try {
			Method method = methodMap.get(METHOD_GET_WIFI_AP_CONFIG);
			configuration = (WifiConfiguration) method.invoke(mWifiManager);
			if (isHtc()) {
				configuration = getHtcWifiApConfiguration(configuration);
			}
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return configuration;
	}

	public boolean setWifiApConfiguration(WifiConfiguration netConfig) {
		boolean result = false;
		try {
			if (isHtc()) {
				setupHtcWifiConfiguration(netConfig);
			}

			Method method = methodMap.get(getSetWifiApConfigName());
			Class<?>[] params = method.getParameterTypes();
			for (Class<?> clazz : params) {
				Log.i(tag, "param -> " + clazz.getSimpleName());
			}

			if (isHtc()) {
				int rValue = (Integer) method.invoke(mWifiManager, netConfig);
				Log.i(tag, "rValue -> " + rValue);
				result = rValue > 0;
			} else {
				result = (Boolean) method.invoke(mWifiManager, netConfig);
			}
		} catch (Exception e) {
			Log.e(tag, "", e);
		}
		return result;
	}

	public boolean setWifiApEnabled(WifiConfiguration configuration,
			boolean enabled) {
		boolean result = false;
		if (isHtc()) {
			setHTCSSID(configuration);
		}
		try {
			Method method = methodMap.get(METHOD_SET_WIFI_AP_ENABLED);
			result = (Boolean) method.invoke(mWifiManager, configuration,
					enabled);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	public boolean isWifiApEnabled() {
		boolean result = false;
		try {
			Method method = methodMap.get(METHOD_IS_WIFI_AP_ENABLED);
			result = (Boolean) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	public boolean stratWifiAp(String ssid, String password) {
		WifiConfiguration netConfig = new WifiConfiguration();
		netConfig.SSID = ssid;
		// netConfig.preSharedKey = mPasswd;
		// netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		netConfig.preSharedKey = password;

		// netConfig.BSSID = wifiManager.getConnectionInfo().getMacAddress();

		// netConfig.preSharedKey = paramString2;
		netConfig.allowedAuthAlgorithms
				.set(WifiConfiguration.AuthAlgorithm.OPEN);
		netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		netConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		netConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

		netConfig.hiddenSSID = false;

		return setWifiApEnabled(netConfig, true);
	}

	public boolean closeWifiAp() {

		WifiConfiguration configuration = getWifiApConfiguration();
		return setWifiApEnabled(configuration, false);
	}

	private void setupHtcWifiConfiguration(WifiConfiguration config) {
		try {
			Log.d(tag, "config=  " + config);
			Object mWifiApProfileValue = BeanUtils.getProperty(config,
					"mWifiApProfile");

			if (mWifiApProfileValue != null) {
				BeanUtils.setProperty(mWifiApProfileValue, "SSID", config.SSID);
				BeanUtils.setProperty(mWifiApProfileValue, "BSSID",
						config.BSSID);
				BeanUtils
						.setProperty(mWifiApProfileValue, "secureType", "open");
				BeanUtils.setProperty(mWifiApProfileValue, "dhcpEnable", 1);
			}
		} catch (Exception e) {
			Log.e(tag, "" + e.getMessage(), e);
		}
	}

	public static boolean isHtc() {
		return mIsHtc;
	}

	public void setHTCSSID(WifiConfiguration config) {
		try {
			Field mWifiApProfileField = WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
			mWifiApProfileField.setAccessible(true);
			Object apProfile = mWifiApProfileField.get(config);
			mWifiApProfileField.setAccessible(false);

			if (apProfile != null) {
				Field ssidField = apProfile.getClass().getDeclaredField("SSID");
				ssidField.setAccessible(true);
				ssidField.set(apProfile, config.SSID);
				ssidField.setAccessible(false);

				Field preShareKey = apProfile.getClass()
						.getDeclaredField("key");
				preShareKey.setAccessible(true);
				preShareKey.set(apProfile, config.preSharedKey);
				preShareKey.setAccessible(false);

				Field bssid = apProfile.getClass().getDeclaredField("BSSID");
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
		}
	}

	private static String getSetWifiApConfigName() {
		return mIsHtc ? "setWifiApConfig" : "setWifiApConfiguration";
	}
}
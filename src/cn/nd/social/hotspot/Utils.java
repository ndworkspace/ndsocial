package cn.nd.social.hotspot;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.content.Context;
import android.graphics.PixelFormat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import cn.nd.social.R;

public class Utils {
	public static final int OP_SUCCESS = 0;
	public static final int OP_FAILED = -1;

	public static final int BUFF_SIZE = 1024;

	public static final int MAX_MSG_SIZE = 4 * 1024;

	public final static int SOCKET_TIMEOUT = 2 * 3 * 1000; // 3 second

	public static Boolean isLocalMachineHost() {
		return isHostIpAddress(getLocalIpAddressAlter());
	}

	public static Boolean isHostIpAddress(String ip) {
		return ip.endsWith(".1");
	}

	public static String getHostIpViaConnectIp(String ip) {
		if (ip.length() == 0) {
			return "";
		}

		StringBuffer myIp = new StringBuffer(ip);
		int start = myIp.lastIndexOf(".");
		myIp.delete(start, myIp.length());
		myIp.append(".1");
		return myIp.toString();
	}

	public static String getWlanIpAddr() {
		String ipString = "";
		try {
		   WifiManager wifiManager = (WifiManager) cn.nd.social.util.Utils.getAppContext()
				   													.getSystemService(Context.WIFI_SERVICE);
		   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		   int ip = wifiInfo.getIpAddress();

		   ipString = String.format(
		   "%d.%d.%d.%d",
		   (ip & 0xff),
		   (ip >> 8 & 0xff),
		   (ip >> 16 & 0xff),
		   (ip >> 24 & 0xff));
		} catch (Exception e) {
			
		}
		return ipString;
	}
	
	public static String getLocalIpAddressAlter() {
		String ip = "";
		ArrayList<String> ipArray = new ArrayList<String>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if(intf.getName().contains("wlan")) {
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()
								&& inetAddress instanceof Inet4Address) {
							ipArray.add(inetAddress.getHostAddress().toString());
							// ip = ip + inetAddress.getHostAddress().toString() +
							// " ";
						}
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());

			return null;
		}
		Boolean match = false;
		for (int i = 0; i < ipArray.size(); i++) {
			String addr = ipArray.get(i);
			if (addr.endsWith(".1")) {
				if (addr.startsWith("192.")) {
					if (match == true) {
						Log.d("Utils",
								"be carefully: mutiple match found for ip add");
					}
					match = true;
					ip = addr;
				}
				if (match == false)
					ip = addr;
			}
		}
		if (ip == "") {
			for (int i = 0; i < ipArray.size(); i++) {
				ip = ipArray.get(i);
				if (ip.startsWith("192.")) { // we think it's valid when the ip
												// addr start with 192.xxx
					break;
				}
			}
		}
		return ip;
	}

	// /----------------------------------------------------------------------------///
	// /////////////////////////////
	// waiting function
	// /////////////////////////////
	public static boolean isWaiting = false;

	public static View waitView = null;

	protected static final void showWaiting(Context context, String info,
			boolean isBlur) {
		isWaiting = true;
		try {
			WindowManager.LayoutParams lp = null;
			if (isBlur) {
				lp = new WindowManager.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_APPLICATION,
						WindowManager.LayoutParams.FLAG_FULLSCREEN
								| WindowManager.LayoutParams.FLAG_BLUR_BEHIND
								| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
						PixelFormat.TRANSLUCENT);
			} else {
				lp = new WindowManager.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_APPLICATION,
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
								| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
								| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
						PixelFormat.TRANSLUCENT);
			}
			WindowManager mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);

			if (waitView == null) {
				LayoutInflater inflate = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				waitView = inflate.inflate(R.layout.qe_waiting_layout, null);

				if (waitView != null) {
					TextView text = (TextView) waitView
							.findViewById(R.id.identify_label);
					text.setText(info);
				}

			}
			mWindowManager.addView(waitView, lp);
		} catch (Throwable e) {
			isWaiting = false;
			System.out.println("[showWaiting]");
		}
	}

	protected static final void hideWaiting(Context context) {
		isWaiting = false;
		try {
			if (waitView != null) {
				WindowManager mWindowManager = (WindowManager) context
						.getSystemService(Context.WINDOW_SERVICE);
				mWindowManager.removeView(waitView);
				waitView = null;
			}
		} catch (Throwable e) {
			System.out.println("[hideWaiting]");
		}
	}

	// -----------------------------------------------------------------------//
	// /file operator function
	/**
	 * 
	 * @param path
	 */
	public static void isExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static String getFileName(String pathname) {
		int start = pathname.lastIndexOf("/");
		return ((start == -1) ? null : pathname.substring(start + 1));
	}

	public static String getFileExtName(String pathname) {
		int start = pathname.lastIndexOf(".");
		return ((start == -1) ? null : pathname.substring(start + 1));
	}
	
}

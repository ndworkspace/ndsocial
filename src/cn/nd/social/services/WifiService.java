package cn.nd.social.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.nd.social.hotspot.MyTimerCheck;
import cn.nd.social.hotspot.WifiApAdmin;
import cn.nd.social.hotspot.WifiCheckThread;

public class WifiService extends Service {

	private final static String TAG = "WifiService";

	public Context mContext;

	public WifiServiceBinder mServicebinder;

	private HandlerThread mHandlerThread;
	private ControlHandler mControlHandler;

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return mServicebinder;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "Services onCreate");

		mContext = this;

		mHandlerThread = new HandlerThread("Wifi Service HThread");
		mHandlerThread.start();

		mControlHandler = new ControlHandler(mHandlerThread.getLooper());

		mServicebinder = new WifiServiceBinder();

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return 0;
	}

	// /////////////////////////////////////////////////////////////////////////
	// internal class
	// /////////////////////////////////////////////////////////////////////////

	// define
	private final static int EVENT_WIFI_OPEN = 1000;
	private final static int EVENT_WIFI_CLOSE = 1001;

	// / class : WifiServiceBinder
	public class WifiServiceBinder extends Binder implements IWifiService {

		public int openWifi(NDWifiInfo info) {
			Message msg = mControlHandler.obtainMessage();
			msg.what = EVENT_WIFI_OPEN;
			msg.obj = info;
			mControlHandler.sendMessage(msg);

			return 0;
		}

		public int closeWifi() {
			Message msg = mControlHandler.obtainMessage();
			msg.what = EVENT_WIFI_CLOSE;
			mControlHandler.sendMessage(msg);

			return 0;
		}

		public NDWifiInfo getNDWifiInfo() {
			return mWifiInfo;
		}
	}

	private WifiApAdmin mWifiAp = null;
	private WifiCheckThread mWifiCheckThread = null;

	private IWifiService.NDWifiInfo mWifiInfo = null;

	// / class : ControlHandler
	private class ControlHandler extends Handler {
		public ControlHandler(Looper loop) {
			super(loop);
		}

		@Override
		public void handleMessage(Message msg) {
			System.out.println("CurrentThread = "
					+ Thread.currentThread().getName() + " handleMessage "
					+ msg.what);

			switch (msg.what) {
			case EVENT_WIFI_OPEN:
				handlerWifiOpen(msg);
				break;

			case EVENT_WIFI_CLOSE:
				handlerWifiClose(msg);
				break;

			default:
				break;
			}
		}

		private void handlerWifiOpen(Message msg) {
			IWifiService.NDWifiInfo info = (IWifiService.NDWifiInfo) msg.obj;

			mWifiInfo = new IWifiService.NDWifiInfo(info.mWifiType, info.mSsid,
					info.mPasswd);

			if (info.mWifiType == IWifiService.WIFI_TYPE_HOTSPOT) {
				if (mWifiAp == null) {
					WifiManager wifiManager = (WifiManager) mContext
							.getSystemService(Context.WIFI_SERVICE);

					mWifiAp = new WifiApAdmin(wifiManager);
					Log.d(TAG, "create WifiApAdmin");
				}

				mWifiAp.startWifiAp(info.mSsid, info.mPasswd);

				mWifiInfo.mState = IWifiService.WIFI_STATE_HOTSOPT_OPENING;

				MyTimerCheck timerCheck = new MyTimerCheck() {

					@Override
					public void doTimerCheckWork() {
						WifiManager wifiManager = (WifiManager) mContext
								.getSystemService(Context.WIFI_SERVICE);
						if (WifiApAdmin.isWifiApEnabled(wifiManager)) {
							Log.v(TAG, "Wifi Hotspot enabled success!");

							mWifiInfo.mState = IWifiService.WIFI_STATE_HOTSOPT_OPEN_SUCCESS;

							networkFinish();

							exit();
						} else {
							Log.v(TAG, "Wifi Hotspot enabled failed!");
						}
					}

					@Override
					public void doTimeOutWork() {
						mWifiInfo.mState = IWifiService.WIFI_STATE_HOTSOPT_OPEN_FAILED;

						networkFinish();
					}
				};
				timerCheck.start(15, 1000);
			} else {
				WifiManager wifiManager = (WifiManager) mContext
						.getSystemService(Context.WIFI_SERVICE);
				WifiApAdmin.closeWifiAp(wifiManager); // need to close wifiAp
														// first

				mWifiInfo.mState = IWifiService.WIFI_STATE_WIFI_CONNECTING;

				mWifiCheckThread = new WifiCheckThread();
				mWifiCheckThread.init(mContext, true);
				mWifiCheckThread.setWifiInfo(info.mSsid, info.mPasswd,
						"WPA PSK");
				mWifiCheckThread.start();

				MyTimerCheck timerCheck = new MyTimerCheck() {
					@Override
					public void doTimerCheckWork() {
						if (mWifiCheckThread.mNetworkState != WifiCheckThread.WIFI_STATE_NONE) {
							int result = 0;
							if (mWifiCheckThread.mNetworkState != WifiCheckThread.WIFI_STATE_CONNECT_SUCCESS) {
								result = 1;
								Log.e(TAG, "Wifi hotspot  connect failed!");
							} else {
								Log.d(TAG, "Wifi hotspot  connect success!");
							}

							mWifiInfo.mState = IWifiService.WIFI_STATE_WIFI_CONNECT_SUCCESS;

							networkFinish();

							exit();
						} else {
							Log.v(TAG, "Wifi enabled failed! trying again");
						}
					}

					@Override
					public void doTimeOutWork() {

						mWifiInfo.mState = IWifiService.WIFI_STATE_WIFI_CONNECT_FAILED;

						networkFinish();
					}
				};
				timerCheck.start(30, 1000);
			}
		}

		private void handlerWifiClose(Message msg) {
			if (mWifiInfo == null) {
				Log.d(TAG, "wifi close : wifi info is null");
			}
			else {
				if (mWifiInfo.mWifiType == IWifiService.WIFI_TYPE_HOTSPOT) {
					WifiManager wifiManager = (WifiManager) mContext
							.getSystemService(Context.WIFI_SERVICE);
					WifiApAdmin.closeWifiAp(wifiManager);
				} else {
					WifiManager wifiManager = (WifiManager) mContext
							.getSystemService(Context.WIFI_SERVICE);
					wifiManager.disconnect();
				}
				
				mWifiInfo.mWifiType = IWifiService.WIFI_TYPE_NONE;
			}
		}

		private void networkFinish() {
			Intent intent = new Intent(
					IWifiService.INTENAL_ACTION_WIFI_SERVICE_NOTIFY);
			intent.putExtra("wifi", mWifiInfo);

			sendBroadcast(intent);
		}
	}
}

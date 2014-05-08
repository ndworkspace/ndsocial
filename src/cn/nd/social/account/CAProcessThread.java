package cn.nd.social.account;

import NDCSdk.INDCCallback;
import NDCSdk.INDCClient;
import android.util.Log;


public class CAProcessThread extends Thread {
	//unit seconds
//	private static final int[] RECONNECT_INTERVAL = {10,15,30,30,60,180,300,600};
	private static final int[] RECONNECT_INTERVAL = {10,10};
	private final static String TAG = "CAProcessThread";
	
	//public static final String IP_ADDRESS="192.168.62.135";
	
	//public static final short PORT = 6060;
	public static final String IP_ADDRESS="42.62.77.23";
	public static final short PORT = 6061;
	 
	
	public static final byte NDC_CLIENT_VERSION = 2;
	
	boolean serverConnected = false;
	private int connectTimes = 0;
	private INDCCallback callback;
	private INDCClient caclient;
	private boolean mStopCAProc;
	private ConnectCbk mConnectCbk;
	
	public interface ConnectCbk {
		void onServerConnected();
		void onServerConnectBreak();
	}
	
	
	public CAProcessThread(INDCCallback cbk,INDCClient caclient,ConnectCbk connectCbk) {
		super("CAProcess");
		callback = cbk;
		mConnectCbk = connectCbk;
		this.caclient = caclient;
	}
	
	public void init() {
		setDaemon(true);
		start();
	}
	
	public void fini() {
		mStopCAProc = true;
		if(isAlive()) {
			this.interrupt();
			try {
				this.join(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isCAClientConnected() {
		return serverConnected;
	}
	
	public void resetConnect(ConnectCbk callback) {
		if(serverConnected) {
			serverConnected = false;
			connectTimes = 0;
			mConnectCbk = callback;
		}
	}
	
	
	@Override
	public void run() {
		while (!mStopCAProc) {
			if (!serverConnected) {
				if(CAUtils.isNetworkConnected()) {
					serverConnected = caclient.Init(IP_ADDRESS, PORT,
							callback,NDC_CLIENT_VERSION);
					
				}
				if(connectTimes < RECONNECT_INTERVAL.length) {
					connectTimes++;
				}					
				if (!serverConnected) {
					try {
						int sleepInterval = RECONNECT_INTERVAL[connectTimes-1]*1000;
						Thread.sleep(sleepInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				} else {
					if(mConnectCbk != null) {
						mConnectCbk.onServerConnected();
						mConnectCbk = null;
					}
					Log.e(TAG, "connection established");
				}
			}
			caclient.Process();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

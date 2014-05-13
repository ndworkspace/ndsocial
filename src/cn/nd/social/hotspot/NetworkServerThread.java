package cn.nd.social.hotspot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.util.Log;
import cn.nd.social.prishare.PriShareConstant;

public class NetworkServerThread extends Thread {

	private final static String TAG = "NetworkServerThread";

	public static class FileInfo {
		public String userName;
		public String appName;
		public String fileName;
		public long fileSize;
		public int fileType;
		public int grantType;
		public int grantValue;
		public int grantReserve;
	}

	public static class SyncBrowsingInfo {
		public int pageNumber;
		public int pageVersion;
		public String pageInfo;
	}
	
	private Handler mSelfHandler = null;
	private Handler mParentHandler = null;

	private int mServerPort;

	private ServerSocket mServerSocket = null;

	private NetworkCommunicateThread mConnectServerThread = null;

	private Map<String, NetworkCommunicateThread> mFileSendThreadMap = null;
	private Set<NetworkCommunicateThread> mNewConnectThreadSet = null;
	
	private boolean mIsHotspot = false;

	public NetworkServerThread(boolean isHotspot) {
		mIsHotspot = isHotspot;
	}
	
	public boolean init(Handler handler, int serverPort) {
		if (handler == null) {
			return false;
		}

		if ((serverPort < 1024) || (serverPort > 65535)) {
			return false;
		}

		mParentHandler = handler;

		mServerPort = serverPort;

		mFileSendThreadMap = new HashMap<String, NetworkCommunicateThread>();

		mNewConnectThreadSet = new HashSet<NetworkCommunicateThread>();

		return true;
	}

	public void fini() {
		try {
			if (mServerSocket != null) {
				mServerSocket.close();
			}

			Thread.sleep(300);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean sendLogin() {
		if (mConnectServerThread != null) {
			return mConnectServerThread.sendLoginReq();
		} else {
			return false;
		}
	}

	public boolean sendLogout(int hotspotFlag) {
		if (mConnectServerThread != null) {
			return mConnectServerThread.sendLogoutReq(hotspotFlag);
		} else {
			for (NetworkCommunicateThread item : mNewConnectThreadSet) {
				if (!item.checkIsFileThread()) {
					item.sendLogoutReq(hotspotFlag);
				}
			}

			return false;
		}
	}

	public boolean sendKickout(String kickoutUserName) {
		if (mConnectServerThread != null) {
			return false;
		}

		for (NetworkCommunicateThread item : mNewConnectThreadSet) {
			if (!item.checkIsFileThread()) {
				item.sendKickoutReq(kickoutUserName);
			}
		}

		return true;
	}

	public boolean sendFile(String userName, String appName, String fileName, int fileType) {
		UserManagerSingleton.UserInfo info = UserManagerSingleton.getInstance()
				.getUserInfo(userName, false);
		if (info == null) {
			Log.e(TAG,
					"user name error, can't find it in user manager singleton");
			return false;
		}

		NetworkCommunicateThread fileSendThread = mFileSendThreadMap
				.get(userName);
		if (fileSendThread == null) {
			try {
				fileSendThread = new NetworkCommunicateThread(this, true);
				fileSendThread.init(mParentHandler, null, info.ip, 6000);
				fileSendThread.start();

				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();

				return false;
			}

			mFileSendThreadMap.put(userName, fileSendThread);
		}

		return fileSendThread.sendFileReq(userName, appName, fileName, fileType, 0, PriShareConstant.INFINITE_TIME, 0);
	}

	public boolean sendFile(String userName, String appName, String fileName, int fileType,
			int grantType, int grantValue, int grantReserve) {
		UserManagerSingleton.UserInfo info = UserManagerSingleton.getInstance()
				.getUserInfo(userName, false);
		if (info == null) {
			Log.e(TAG,
					"user name error, can't find it in user manager singleton");
			return false;
		}

		NetworkCommunicateThread fileSendThread = mFileSendThreadMap
				.get(userName);
		if (fileSendThread == null) {
			try {
				fileSendThread = new NetworkCommunicateThread(this, true);
				fileSendThread.init(mParentHandler, null, info.ip, 6000);
				fileSendThread.start();

				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();

				return false;
			}

			mFileSendThreadMap.put(userName, fileSendThread);
		}

		return fileSendThread.sendFileReq(userName, appName, fileName,fileType,
				grantType, grantValue, grantReserve);
	}

	public int queryFileRecvProgress(String userName, String fileName) {
		UserManagerSingleton.FileReceiveInfo info = UserManagerSingleton
				.getInstance().getFileReceiveInfo(userName, fileName);
		if (info != null) {
			if (info.fileSize != 0 && info.receivedSize != 0) {
				return (int) ((info.receivedSize * 100) / info.fileSize);
			}
		}

		return 0;
	}

	public int queryFileSendProgress(String userName, String fileName) {
		UserManagerSingleton.FileSendInfo info = UserManagerSingleton
				.getInstance().getFileSendInfo(fileName);
		if (info != null) {
			if (info.fileSize != 0) {

				int sendCount = 0;
				try {
					sendCount = info.fileSize - info.fileInput.available();
				} catch (IOException e) {
					sendCount = -1;
				}

				if (sendCount >= 0)
					return (int) ((sendCount * 100) / info.fileSize);
			} else {
				Log.e(TAG, "file : " + fileName + " file size is 0");
			}
		} else {
			Log.e(TAG, "file : " + fileName + " can't find send info");
		}

		return -1;
	}
	
	// sync browsing operator
	public boolean sendSyncBrowsingShakeHand(int ver) {
		if (mConnectServerThread != null) {
			return mConnectServerThread.sendSyncBrowsingShakeHand(ver);
		}

		return false;
	}
	
	public boolean sendSyncBrowsingShakeHandAck(String userName, int state, int width, int height, int pageCount, int curPage) {
		for (NetworkCommunicateThread item : mNewConnectThreadSet) {
			if (!item.checkIsFileThread()) {
				if (item.checkUserName(userName)) {
					return item.sendSyncBrowsingShakeHandAck(state, width, height, pageCount, curPage);
				}				
			}
		}
		
		return false;
	}
	
	public boolean sendPageSyncBroadcast(int pageNumber, int pageVersion) {
		for (NetworkCommunicateThread item : mNewConnectThreadSet) {
			item.sendPageSyncBroadcast(pageNumber, pageVersion);
		}
		
		return true;
	}
	
	public boolean sendPageSyncBroadcastAction(int pageNumber, int pageVersion, byte[] action) {
		for (NetworkCommunicateThread item : mNewConnectThreadSet) {
			item.sendPageSyncBroadcastAction(pageNumber, pageVersion, action);
		}
		
		return true;
	}
	
	public boolean sendPageSyncBroadcastActionAck(byte[] action) {
		if (mConnectServerThread != null) {
			return mConnectServerThread.sendPageSyncBroadcastActionAck(action);
		}

		return false;
	}
	
	public boolean sendSyncBrowsingRequestPage(int pageNumber, int curPageVersion) {
		if (mConnectServerThread != null) {
			return mConnectServerThread.sendSyncBrowsingRequestPage(pageNumber, curPageVersion);
		}

		return false;
	}
	
	public boolean sendSyncBrowsingRequestPageAck(String userName, int state, int pageNumber, int pageVersion, String pageInfo) {
		for (NetworkCommunicateThread item : mNewConnectThreadSet) {
			if (!item.checkIsFileThread()) {
				if (item.checkUserName(userName)) {
					return item.sendSyncBrowsingRequestPageAck(state, pageNumber, pageVersion, pageInfo);
				}				
			}
		}
		
		return false;
	}

	public void run() {
		if (mParentHandler == null) {
			// write log
			;
		}

		try {
			mServerSocket = new ServerSocket();

			mServerSocket.setReuseAddress(true);

			mServerSocket.setSoTimeout(0);

			mServerSocket.bind(new InetSocketAddress(mServerPort));

			if (!mIsHotspot) {//Utils.isLocalMachineHost()
				
				String ipAddr = Utils.getWlanIpAddr();
				if(ipAddr != null && ipAddr.contains("192.168.")) {
					ipAddr = Utils.getHostIpViaConnectIp(ipAddr);
				} else {
					ipAddr = Utils.getHostIpViaConnectIp(Utils
							.getLocalIpAddressAlter());
				}
				mConnectServerThread = new NetworkCommunicateThread(this, false);
				mConnectServerThread
						.init(mParentHandler, null, ipAddr, mServerPort);
				mConnectServerThread.start();
			}

			Log.d(TAG, "port [" + mServerPort + "] listening");

		} catch (Exception e) {
			e.printStackTrace();
		}

		while (!mServerSocket.isClosed()
				&& !Thread.currentThread().isInterrupted()) {
			try {

				Socket socket = mServerSocket.accept();

				Log.d(TAG, "has new socket");

				NetworkCommunicateThread communicate = new NetworkCommunicateThread(
						this, false);

				communicate.init(mParentHandler, socket, null, 0);

				communicate.start();

				mNewConnectThreadSet.add(communicate);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (mServerSocket != null) {
			try {
				mServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mServerSocket = null;
		}

		// stop all communicate thread
		for (NetworkCommunicateThread item : mNewConnectThreadSet) {
			item.fini();
		}

		for (Map.Entry entry : mFileSendThreadMap.entrySet()) {
			NetworkCommunicateThread thread = (NetworkCommunicateThread) entry
					.getValue();

			thread.fini();
		}

		if (mConnectServerThread != null) {
			mConnectServerThread.fini();
		}

		UserManagerSingleton.getInstance().clear();
	}

	public boolean broadcastLogin(UserManagerSingleton.UserInfo info) {
		if (mConnectServerThread != null) {
			Log.e(TAG, "only hotspot can call broadcastCmd.");

			return false;
		} else {
			for (NetworkCommunicateThread item : mNewConnectThreadSet) {
				if (!item.checkIsFileThread()) {
					item.broadcastLoginReq(info);
				}
			}

			return true;
		}
	}

	public boolean broadcastLogout(String userName, int hotspotFlag) {
		
		//tangtaotao@ND_20140221 add start
		//when user log out, should remove the obsolete socket connection
		NetworkCommunicateThread fileSendThread = mFileSendThreadMap
				.get(userName);
		if(fileSendThread != null) {
			fileSendThread.fini();
			mFileSendThreadMap.remove(userName);
		}
		//tangtaotao@ND_20140221 add end
		
		if (mConnectServerThread != null) {
			Log.e(TAG, "only hotspot can call broadcastCmd.");

			return false;
		} else {
			for (NetworkCommunicateThread item : mNewConnectThreadSet) {
				if (!item.checkIsFileThread()) {
					item.broadcastLogoutReq(userName, hotspotFlag);
				}
			}

			return true;
		}
	}
}

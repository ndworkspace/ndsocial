package cn.nd.social.net;

import android.os.Handler;
import cn.nd.social.hotspot.NetworkServerThread;

public class NetworkProtocolImpl {
	private NetworkServerThread mNetworkThread;
	private final static int SERVER_PORT = 6000;

	public NetworkProtocolImpl(Handler handler,boolean isHotspot) {
		mNetworkThread = new NetworkServerThread(isHotspot);
		mNetworkThread.init(handler, SERVER_PORT);
		mNetworkThread.start();
	}

	public void sendFile(String userName, String fileName, String appName,
			int fileType) {
		mNetworkThread.sendFile(userName, appName, fileName, fileType);
	}

	public void sendFile(String userName, String fileName, String appName,
			int fileType, int grantType, int grantValue, int grantReserve) {
		mNetworkThread.sendFile(userName, appName, fileName, fileType,
				grantType, grantValue, grantReserve);
	}

	public void sendLogin() {
		mNetworkThread.sendLogin();
	}

	public void sendLogout(int hostFlag) {
		mNetworkThread.sendLogout(hostFlag);
	}

	public void sendKickout(String kickoutUserName) {
		mNetworkThread.sendKickout(kickoutUserName);
	}

	public int queryFileRecvProgress(String user, String fileName) {
		return mNetworkThread.queryFileRecvProgress(user, fileName);
	}

	public int queryFileSendProgress(String user, String fileName) {
		return mNetworkThread.queryFileSendProgress(user, fileName);
	}

	public void cleanup() {
		if (mNetworkThread != null) {
			mNetworkThread.fini();
		}
	}
}

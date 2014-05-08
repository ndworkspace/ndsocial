package cn.nd.social.syncbrowsing.ui;

import java.lang.ref.WeakReference;

import android.os.Handler;
import cn.nd.social.hotspot.NetworkServerThread;

public class SyncNetworkProtocol {
	
	NetworkServerThread mNetworkThread;
	private final static int SERVER_PORT = 6000;
	private static WeakReference<SyncNetworkProtocol> sNetworkProtocol;
	public SyncNetworkProtocol(Handler handler,boolean isHotspot) {
		mNetworkThread = new NetworkServerThread(isHotspot);
		mNetworkThread.init(handler, SERVER_PORT);
		mNetworkThread.start();
		sNetworkProtocol = new WeakReference<SyncNetworkProtocol>(this);
	}
	
	public static SyncNetworkProtocol getSyncNetworkProtocol() {
		if(sNetworkProtocol == null) {
			return null;
		}
		return sNetworkProtocol.get();
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
			sNetworkProtocol = null;//force clean up
		}
	}
	
	public void syncHandShake(int verno) {//client
		mNetworkThread.sendSyncBrowsingShakeHand(verno);
	}
	
	public void syncHandShakeAck(String user,int state,int width,int height,int pageCount,int curPage) { //server
		mNetworkThread.sendSyncBrowsingShakeHandAck(user,state,width,height, pageCount, curPage);
	}
	
	public void sycnSendPageBroadcast(int page,int pageVersion) {
		mNetworkThread.sendPageSyncBroadcast(page, pageVersion);
	}
	
	public void sycnRequestPage(int page,int pageVersion) {
		mNetworkThread.sendSyncBrowsingRequestPage(page, pageVersion);
	}
	
	public void sycnRequestPageAck(String user,int state,int pageNum, int pageVer,String pageInfo) {
		mNetworkThread.sendSyncBrowsingRequestPageAck(user,state,pageNum,pageVer,pageInfo);
	}
	
	public void sycnBroadcastAction(int pageNum, int pageVersion, byte[]action) {
		mNetworkThread.sendPageSyncBroadcastAction(pageNum, pageVersion, action);
	}	
}

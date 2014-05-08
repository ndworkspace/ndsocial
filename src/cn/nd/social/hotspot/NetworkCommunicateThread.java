package cn.nd.social.hotspot;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.LogToFile;

//todo : NetworkCommunicateThread need split off 
public class NetworkCommunicateThread extends Thread {
	private final static String TAG = "NetworkCommunicateThread";

	private NetworkServerThread mNetworkServerThread = null;

	private boolean mIsFileThread = false;

	private String mPeerUserName = null;

	private Handler mParentHandler = null;

	private Looper mThreadLooper = null;
	private Handler mThreadLooperHandler = null;

	private Socket mSocket = null;

	String mServerIP = null;
	int mServerPort = 0;

	private boolean mIsServerThread = false;

	private RDataInputStream mDataInput = null;
	private RDataOutputStream mDataOutput = null;
	

	// todo : change sendFileSeq for multi file
	int mSendFileSeq = 0;

	public NetworkCommunicateThread(NetworkServerThread networkServerThread,
			boolean isFileThread) {
		mNetworkServerThread = networkServerThread;

		mIsFileThread = isFileThread;
	}

	public boolean checkIsFileThread() {
		return mIsFileThread;
	}
	
	public boolean checkUserName(String userName) {
		if (mPeerUserName == null) {
			Log.e(TAG, "can't find peer user name when broadcastLoginReq call");
			return false;
		}

		if (mPeerUserName.contentEquals(userName)) {
			return true;
		}
		
		return false;
	}

	public boolean init(Handler handler, Socket socket, String serverIP,
			int serverPort) {

		if (handler == null) {
			return false;
		}

		mParentHandler = handler;

		if (socket != null) {
			mSocket = socket;

			mIsServerThread = true;
		} else {
			mServerIP = serverIP;
			mServerPort = serverPort;

			mIsServerThread = false;
		}

		return true;
	}

	public void fini() {
		if (mThreadLooper != null) {
			mThreadLooper.quit();
		}
	}

	public boolean sendLoginReq() {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_LOGIN;

		mThreadLooperHandler.sendMessage(m);

		mIsFileThread = false;

		return true;
	}

	public boolean broadcastLoginReq(UserManagerSingleton.UserInfo info) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		if (mPeerUserName == null) {
			Log.e(TAG, "can't find peer user name when broadcastLoginReq call");
			return false;
		}

		if (mPeerUserName.contentEquals(info.userName)) {
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_BROADCAST_MSG_LOGIN;
		m.obj = info;

		mThreadLooperHandler.sendMessage(m);

		return true;
	}

	public boolean sendLogoutReq(int hotspotFlag) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_LOGOUT;
		m.arg1 = hotspotFlag;

		mThreadLooperHandler.sendMessage(m);

		return true;
	}

	public boolean broadcastLogoutReq(String userName, int hotspotFlag) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		if (mPeerUserName == null) {
			Log.e(TAG, "can't find peer user name when broadcastLoginReq call");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT;
		m.arg1 = hotspotFlag;
		m.obj = userName;

		mThreadLooperHandler.sendMessage(m);

		return true;
	}

	public boolean sendKickoutReq(String kickoutUserName) {
		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_KICKOUT;
		m.obj = kickoutUserName;

		mThreadLooperHandler.sendMessage(m);

		return false;
	}

	public boolean sendFileReq(String userName, String appName,
			String fileName, int fileType, int grantType, int grantValue,
			int grantReserve) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		NetworkServerThread.FileInfo info = new NetworkServerThread.FileInfo();
		info.userName = userName;
		info.appName = appName;
		info.fileName = fileName;
		info.fileType = fileType;
		info.grantType = grantType;
		info.grantValue = grantValue;
		info.grantReserve = grantReserve;

		Message m = new Message();
		m.what = MsgDefine.HANDLER_MSG_FILE_SEND_REQ;
		m.obj = info;

		mThreadLooperHandler.sendMessage(m);

		mIsFileThread = true;

		return true;
	}

	public boolean sendSyncBrowsingShakeHand(int ver) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND;
		m.arg1 = ver;
		m.obj = mPeerUserName;

		mThreadLooperHandler.sendMessage(m);
		
		return true;
	}
	
	public boolean sendSyncBrowsingShakeHandAck(int state, int width, int height, int pageCount, int curPage) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		int[] ackArray = new int[5];
		ackArray[0] = state;
		ackArray[1] = width;
		ackArray[2] = height;
		ackArray[3] = pageCount;
		ackArray[4] = curPage;
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND_ACK;
		m.obj = ackArray;

		mThreadLooperHandler.sendMessage(m);
		
		return true;
	}
	
	public boolean sendPageSyncBroadcast(int pageNumber, int pageVersion) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST;
		m.arg1 = pageNumber;
		m.arg2 = pageVersion;

		mThreadLooperHandler.sendMessage(m);
		
		return true;
	}
	
	public boolean sendPageSyncBroadcastAction(int pageNumber, int pageVersion, byte[] action) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION;
		m.arg1 = pageNumber;
		m.arg2 = pageVersion;
		m.obj = action;

		mThreadLooperHandler.sendMessage(m);
		
		return true;
	}
	
	public boolean sendPageSyncBroadcastActionAck(byte[] action) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION_ACK;
		m.obj = action;

		mThreadLooperHandler.sendMessage(m);
		
		return true;
	}
	
	public boolean sendSyncBrowsingRequestPage(int pageNumber, int curPageVersion) {
		if (mThreadLooperHandler == null) {
			Log.e(TAG, "looper didn't ready");
			return false;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST;
		m.arg1 = pageNumber;
		m.arg2 = curPageVersion;
		m.obj = mPeerUserName;

		mThreadLooperHandler.sendMessage(m);
		
		return true;
	}
	
	public boolean sendSyncBrowsingRequestPageAck(int state, int pageNumber, int pageVersion, String pageInfo) {
		NetworkServerThread.SyncBrowsingInfo info = new NetworkServerThread.SyncBrowsingInfo();
		
		info.pageNumber = pageNumber;
		info.pageVersion = pageVersion;
		info.pageInfo = pageInfo;
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK;
		m.arg1 = state;
		m.obj = info;

		mThreadLooperHandler.sendMessage(m);
		
		return false;
	}
	
	public void run() {

		boolean isConnected = false;
		try {

			Looper.prepare();

			mThreadLooperHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					processHandlerMsg(msg);

					super.handleMessage(msg);
				}
			};

			mThreadLooper = Looper.myLooper();

			connect2Server();

			mDataInput = new RDataInputStream(new DataInputStream(
					mSocket.getInputStream()));
			mDataOutput = new RDataOutputStream(new DataOutputStream(
					new BufferedOutputStream(mSocket.getOutputStream(),
							Utils.MAX_MSG_SIZE)));

			isConnected = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		Message m = new Message();

		if (mIsServerThread) {
			m.what = MsgDefine.HANDLER_MSG_NEW_CONNECT;
		} else {
			m.what = MsgDefine.HANDLER_MSG_CONNECTED;
		}

		if (isConnected) {
			m.arg1 = Utils.OP_SUCCESS;
			m.obj = this;
		} else {
			m.arg1 = Utils.OP_FAILED;
			m.obj = this;
		}

		mParentHandler.sendMessage(m);

		mThreadLooperHandler.sendEmptyMessageDelayed(0, 20);

		Looper.loop();

		if (mDataOutput != null) {
			mDataOutput = null;
		}

		if (mDataInput != null) {
			mDataInput = null;
		}

		mThreadLooper = null;

		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mSocket = null;
		}
	}

	private void connect2Server() throws IOException {
		if (mSocket != null) {
			return;
		}

		mSocket = new Socket();
		mSocket.setKeepAlive(true);
		mSocket.setSoLinger(false, 0);
		mSocket.setSoTimeout(Utils.SOCKET_TIMEOUT);

		SocketAddress addrSock = new InetSocketAddress(mServerIP, mServerPort);
		mSocket.connect(addrSock, Utils.SOCKET_TIMEOUT);
		
		int bufferSize = 64 * 1024;
		mSocket.setSendBufferSize(bufferSize);
		mSocket.setReceiveBufferSize(bufferSize);
	}

	private void processHandlerMsg(Message msg) {
		switch (msg.what) {
		case 0:
			processLooper();

			mThreadLooperHandler.sendEmptyMessageDelayed(0, 10);

			break;

		case MsgDefine.NETWORK_MSG_LOGIN:
			processHandlerLogin(msg);
			break;

		case MsgDefine.NETWORK_BROADCAST_MSG_LOGIN:
			processHandlerBroadcastLogin(msg);
			break;

		case MsgDefine.NETWORK_MSG_LOGOUT:
			processHandlerLogout(msg);
			break;

		case MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT:
			processHandlerBroadcastLogout(msg);
			break;

		case MsgDefine.NETWORK_MSG_KICKOUT:
			processHandlerKickout(msg);
			break;

		case MsgDefine.HANDLER_MSG_FILE_SEND_REQ:
			processHandlerSendFile(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND:
			processHandlerSyncBrowsingShakeHand(msg);
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND_ACK:
			processHandlerSyncBrowsingShakeHandAck(msg);
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST:
			processHandlerSyncBrowsingPageSyncBroadcast(msg);
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION:
			processHandlerSyncBrowsingBroadcastAction(msg);
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION_ACK:
			processHandlerSyncBrowsingBroadcastActionAck(msg);
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST:
			processHandlerSyncBrowsingPageRequest(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK:
			processHandlerSyncBrowsingPageRequestAck(msg);
			break;
			
		default:
			Log.e(TAG, "unknown handler msg : " + msg.what);
			break;
		}
	}

	private void exceptionLogout(String userName) {
		if (UserManagerSingleton.getInstance().delUserInfo(userName) == null) {
			Log.e(TAG, "exception on [" + userName
					+ "] logout, but couldn't find the info");
			return;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_LOGOUT;
		m.arg1 = 0;
		m.obj = userName;
		LogToFile.e(TAG, "exceptionLogout " + userName);
		mParentHandler.sendMessage(m);
	}
	
	private void processLooper() {
		if (mSocket.isClosed() || !mSocket.isConnected() || Thread.currentThread().isInterrupted()) {
			mThreadLooper.quit();
			if(mPeerUserName != null) {
				exceptionLogout(mPeerUserName);
			}
			
		}

		boolean hasException = false;

		try {
			sendFileData();

			onPacketArrive();

			mSocket.setKeepAlive(true);
		} catch (IOException e) {
			e.printStackTrace();

			hasException = true;
		} catch(Exception e) {
			e.printStackTrace();
			LogToFile.e(TAG, "processLooper " + e.toString());
			hasException = true;
		}

		if (hasException) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void processHandlerLogin(Message msg) {
		try {
			UserManagerSingleton singleton = UserManagerSingleton.getInstance();
			UserManagerSingleton.UserInfo info = singleton.getUserInfo(null,
					true);

			byte[] nameData = info.userName.getBytes();
			byte[] ipData = info.ip.getBytes();
			byte[] macData = info.mac.getBytes();			

			int uMsgLen = 8 + 2 + nameData.length + 2 + ipData.length + 2
					+ macData.length;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_MSG_LOGIN);

			// user name
			mDataOutput.writeShort(nameData.length);
			mDataOutput.write(nameData);

			// user ip
			mDataOutput.writeShort(ipData.length);
			mDataOutput.write(ipData);

			// user mac
			mDataOutput.writeShort(macData.length);
			mDataOutput.write(macData);

			mDataOutput.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processHandlerBroadcastLogin(Message msg) {
		try {
			UserManagerSingleton.UserInfo info = (UserManagerSingleton.UserInfo) msg.obj;

			byte[] nameData = info.userName.getBytes();
			byte[] ipData = info.ip.getBytes();
			byte[] macData = info.mac.getBytes();

			int uMsgLen = 8 + 2 + nameData.length + 2 + ipData.length + 2
					+ macData.length;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_BROADCAST_MSG_LOGIN);

			// user name
			mDataOutput.writeShort(nameData.length);
			mDataOutput.write(nameData);

			// user ip
			mDataOutput.writeShort(ipData.length);
			mDataOutput.write(ipData);

			// user mac
			mDataOutput.writeShort(macData.length);
			mDataOutput.write(macData);

			mDataOutput.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processHandlerLogout(Message msg) {
		try {
			UserManagerSingleton singleton = UserManagerSingleton.getInstance();
			UserManagerSingleton.UserInfo info = singleton.getUserInfo(null,
					true);

			byte[] nameData = info.userName.getBytes();

			int uMsgLen = 8 + 2 + nameData.length;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_MSG_LOGOUT);

			// hotspot flag
			mDataOutput.writeInt(msg.arg1);

			// user name
			mDataOutput.writeShort(nameData.length);
			mDataOutput.write(nameData);

			mDataOutput.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processHandlerBroadcastLogout(Message msg) {
		try {
			String userName = (String) msg.obj;

			byte[] nameData = userName.getBytes();

			int uMsgLen = 8 + 2 + nameData.length;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT);

			// hotspot flag
			mDataOutput.writeInt(msg.arg1);

			// user name
			mDataOutput.writeShort(nameData.length);
			mDataOutput.write(nameData);

			mDataOutput.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processHandlerKickout(Message msg) {
		try {
			String userName = (String) msg.obj;

			byte[] nameData = userName.getBytes();

			int uMsgLen = 8 + 2 + nameData.length;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_MSG_KICKOUT);

			// user name
			mDataOutput.writeShort(nameData.length);
			mDataOutput.write(nameData);

			mDataOutput.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processHandlerSendFile(Message msg) {
		try {
			// todo : check file exist

			// String fileName = (String) msg.obj;
			NetworkServerThread.FileInfo fileInfo = (NetworkServerThread.FileInfo) msg.obj;

			UserManagerSingleton.FileSendInfo fileSendInfo = new UserManagerSingleton.FileSendInfo();
			fileSendInfo.fileName = Utils.getFileName(fileInfo.fileName);
			if (fileSendInfo.fileName == null) {

				Message m = new Message();
				m.what = MsgDefine.HANDLER_NOTIFY_INFO;
				m.obj = "file [" + fileInfo.fileName + "] can't read";

				mParentHandler.sendMessage(m);

				Log.e(TAG, "file [" + fileInfo.fileName + "] can't read");

				return;
			}

			fileSendInfo.sendTo = fileInfo.userName;
			fileSendInfo.appName = fileInfo.appName;
			fileSendInfo.fileFullName = fileInfo.fileName;
			fileSendInfo.fileInput = new FileInputStream(fileInfo.fileName);
			fileSendInfo.fileSize = fileSendInfo.fileInput.available();
			fileSendInfo.fileType = fileInfo.fileType;
			fileSendInfo.grantType = fileInfo.grantType;
			fileSendInfo.grantValue = fileInfo.grantValue;
			fileSendInfo.grantReserve = fileInfo.grantReserve;
			fileSendInfo.md5 = "abcdefghjkl";

			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			// get self user info
			UserManagerSingleton.UserInfo info = singleton.getUserInfo(null,
					true);

			// save send file info
			int sendFileSeq = singleton.getSequence();
			singleton.addFileSendInfo(sendFileSeq, fileSendInfo);

			byte[] nameData = info.userName.getBytes();
			byte[] appNameData = fileSendInfo.appName.getBytes();
			byte[] fileNameData = fileSendInfo.fileName.getBytes();
			byte[] md5Data = fileSendInfo.md5.getBytes();

			int uMsgLen = 8 + 4 + 2 + nameData.length + 2 + fileNameData.length
					+ 4 + 2 + md5Data.length;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_MSG_FILE_SEND_REQ);

			mDataOutput.writeInt(sendFileSeq);

			// user name
			mDataOutput.writeShort(nameData.length);
			mDataOutput.write(nameData);

			// app name
			mDataOutput.writeShort(appNameData.length);
			mDataOutput.write(appNameData);

			// file name
			mDataOutput.writeShort(fileNameData.length);
			mDataOutput.write(fileNameData);

			mDataOutput.writeInt(fileSendInfo.fileSize);

			// file type
			mDataOutput.writeInt(fileSendInfo.fileType);
			
			// grant type
			mDataOutput.writeInt(fileInfo.grantType);
			// grant value
			mDataOutput.writeInt(fileInfo.grantValue);
			// grant reserve
			mDataOutput.writeInt(fileInfo.grantReserve);

			// file md5
			mDataOutput.writeShort(md5Data.length);
			mDataOutput.write(md5Data);

			mDataOutput.flush();

			fileInfo.fileSize = fileSendInfo.fileSize;

			Message notifyMsg = new Message();
			notifyMsg.what = MsgDefine.HANDLER_MSG_FILE_SEND_REQ;
			notifyMsg.obj = fileInfo;
			mParentHandler.sendMessage(notifyMsg);

			//comment out by tangtaotao 2014_0126
/*			Message showInfoMsg = new Message();
			showInfoMsg.what = MsgDefine.HANDLER_NOTIFY_INFO;
			showInfoMsg.obj = "file :" + fileSendInfo.fileName + " send start";
			mParentHandler.sendMessage(showInfoMsg);*/

			Log.d(TAG, "file :" + fileSendInfo.fileName + " send start");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processHandlerSyncBrowsingShakeHand(Message msg) {
		try {
			int ver = msg.arg1;
			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			int sendSeq = singleton.getSequence();

			int uMsgLen = 8 + 4 + 2;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND);

			mDataOutput.writeInt(sendSeq);
			
			mDataOutput.writeShort(ver);

			mDataOutput.flush();

			Log.d(TAG, "sync browsing shake hand send");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processHandlerSyncBrowsingShakeHandAck(Message msg) {
		try {

			int[] ackArray = (int[]) msg.obj;
			
			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			int sendSeq = singleton.getSequence();

			int uMsgLen = 8 + 4 + 20;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND_ACK);

			mDataOutput.writeInt(sendSeq);
			
			// state
			mDataOutput.writeInt(ackArray[0]);

			// view width
			mDataOutput.writeInt(ackArray[1]);
			
			// view height
			mDataOutput.writeInt(ackArray[2]);
			
			// page count
			mDataOutput.writeInt(ackArray[3]);
			
			// current page number
			mDataOutput.writeInt(ackArray[4]);

			mDataOutput.flush();

			Log.d(TAG, "sync browsing shake hand ack send");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processHandlerSyncBrowsingPageSyncBroadcast(Message msg) {
		try {

			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			int sendSeq = singleton.getSequence();
			
			int uMsgLen = 8 + 4 + 8;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST);

			mDataOutput.writeInt(sendSeq);
			
			// page number
			mDataOutput.writeInt(msg.arg1);
			
			// page version
			mDataOutput.writeInt(msg.arg2);

			mDataOutput.flush();

			Log.d(TAG, "sync browsing page sync broadcast send");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processHandlerSyncBrowsingBroadcastAction(Message msg) {
		try {

			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			int sendSeq = singleton.getSequence();
			
			int uMsgLen = 8 + 4 + 8;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION);

			mDataOutput.writeInt(sendSeq);
			
			// page number
			mDataOutput.writeInt(msg.arg1);
			
			// page version
			mDataOutput.writeInt(msg.arg2);
			
			// action
			byte[] action = (byte[]) msg.obj;
			mDataOutput.writeShort(action.length);
			mDataOutput.write(action);

			mDataOutput.flush();

			Log.d(TAG, "sync browsing broadcast action send");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processHandlerSyncBrowsingBroadcastActionAck(Message msg) {
		try {

			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			int sendSeq = singleton.getSequence();
			
			int uMsgLen = 8 + 4 + 8;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION_ACK);

			mDataOutput.writeInt(sendSeq);
			
			// action ack
			byte[] actionAck = (byte[]) msg.obj;
			mDataOutput.writeShort(actionAck.length);
			mDataOutput.write(actionAck);

			mDataOutput.flush();

			Log.d(TAG, "sync browsing broadcast action send");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processHandlerSyncBrowsingPageRequest(Message msg) {
		try {

			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			int sendSeq = singleton.getSequence();
			
			int uMsgLen = 8 + 4 + 8;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST);

			mDataOutput.writeInt(sendSeq);
			
			// page number
			mDataOutput.writeInt(msg.arg1);
			
			// current page version
			mDataOutput.writeInt(msg.arg2);

			mDataOutput.flush();

			Log.d(TAG, "sync browsing page request send");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processHandlerSyncBrowsingPageRequestAck(Message msg) {
		try {
			NetworkServerThread.SyncBrowsingInfo info = (NetworkServerThread.SyncBrowsingInfo) msg.obj; 
			
			byte[] pageInfo = info.pageInfo.getBytes();
			
			UserManagerSingleton singleton = UserManagerSingleton.getInstance();

			int sendSeq = singleton.getSequence();
			
			int uMsgLen = 8 + 4 + 12 + 2 + pageInfo.length;

			// len
			mDataOutput.writeInt(uMsgLen);
			// version
			mDataOutput.writeShort(0);
			// type
			mDataOutput.writeShort(MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK);

			mDataOutput.writeInt(sendSeq);

			// state
			mDataOutput.writeInt(msg.arg1);
			
			// page number
			mDataOutput.writeInt(info.pageNumber);
			
			// page version
			mDataOutput.writeInt(info.pageVersion);
			
			// page info
			mDataOutput.writeShort(pageInfo.length);
			mDataOutput.write(pageInfo);

			mDataOutput.flush();

			Log.d(TAG, "sync browsing page request send");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private int mPacketLen = 0;
	private short mVersion = 0;
	private short mPacketType = 0;

	private void onPacketArrive() throws IOException {
		if (mDataInput.available() <= 0) {
			return;
		}

		while (mDataInput.available() > 8) {

			if (mPacketLen == 0 && mVersion == 0 && mPacketType == 0) {
				mPacketLen = mDataInput.readInt();

				mVersion = mDataInput.readShort();
				mPacketType = mDataInput.readShort();
			}

			if ((mPacketType < MsgDefine.NETWORK_MSG_LOGIN)
					|| (mPacketType > MsgDefine.NETWORK_MSG_TYPE_END)) {
				Log.e(TAG, "unlawful packet type : " + mPacketType
						+ ", socket is closed!!!");

				mSocket.close();
				return;
			}

			if (mDataInput.available() < (mPacketLen - 8)) {
				return;
			}

			switch (mPacketType) {
			case MsgDefine.NETWORK_MSG_LOGIN:
				onReceiveLogin(mPacketLen);

				mIsFileThread = false;
				break;

			case MsgDefine.NETWORK_MSG_LOGIN_ACK:
				onReceiveLoginAck(mPacketLen);
				break;

			case MsgDefine.NETWORK_MSG_LOGOUT:
				onReceiveLogout(mPacketLen);
				break;

			case MsgDefine.NETWORK_BROADCAST_MSG_LOGIN:
				onReceiveBroadcastLogin(mPacketLen);
				break;

			case MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT:
				onReceiveBroadcastLogout(mPacketLen);
				break;

			case MsgDefine.NETWORK_MSG_KICKOUT:
				onReceiveKickout(mPacketLen);
				break;

			case MsgDefine.NETWORK_MSG_FILE_SEND_REQ:
				onReceiveFileRequest(mPacketLen);

				mIsFileThread = true;
				break;

			case MsgDefine.NETWORK_MSG_FILE_SEND_REQ_ACK:
				onReceiveFileRequestAck(mPacketLen);
				break;

			case MsgDefine.NETWORK_MSG_FILE_SEND_FIN:
				onReceiveFileFinish(mPacketLen);
				break;

			case MsgDefine.NETWORK_MSG_FILE_DATA:
				onReceiveFileData(mPacketLen);
				break;

			case MsgDefine.NETWORK_MSG_FILE_DATA_ACK:
				onReceiveFileDataAck(mPacketLen);
				break;

			case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND:
				onReceiveSyncBrowingShakeHand(mPacketLen);
				break;
				
			case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND_ACK:
				onReceiveSyncBrowingShakeHandAck(mPacketLen);
				break;
				
			case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST:
				onReceiveSyncBrowingPageSyncBroadcast(mPacketLen);
				break;
				
			case MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION:
				onReceiveSyncBrowingBroadcastAction(mPacketLen);
				break;
				
			case MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION_ACK:
				onReceiveSyncBrowingBroadcastActionAck(mPacketLen);
				break;
				
			case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST:
				onReceiveSyncBrowingPageRequest(mPacketLen);
				break;
				
			case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK:
				onReceiveSyncBrowingPageRequestAck(mPacketLen);
				break;
				
			default:
				// TODO : unknown packet type
				int len = mDataInput.available();
				if (len > 0) {
					byte[] temp = new byte[len];
					mDataInput.read(temp);
				}

				Log.e(TAG, "unknown packet type :" + mPacketType
						+ ", throw data len : " + len);
			}

			mPacketLen = 0;
			mVersion = 0;
			mPacketType = 0;
		}
	}

	private void onReceiveLogin(int PacketLen) throws IOException {
		UserManagerSingleton.UserInfo userInfo = new UserManagerSingleton.UserInfo();

		// request user name
		short len = mDataInput.readShort();
		byte[] temp = new byte[len];
		mDataInput.read(temp, 0, len);
		userInfo.userName = new String(temp);
		Log.d(TAG, "login name : " + userInfo.userName);

		// check user conflict

		// ip
		len = mDataInput.readShort();
		temp = new byte[len];
		mDataInput.read(temp, 0, len);
		userInfo.ip = new String(temp);
		Log.d(TAG, "login ip : " + userInfo.ip);

		// MAC
		len = mDataInput.readShort();
		temp = new byte[len];
		mDataInput.read(temp, 0, len);
		userInfo.mac = new String(temp);
		Log.d(TAG, "login mac : " + userInfo.mac);

		UserManagerSingleton.getInstance().addUserInfo(userInfo, false);

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_LOGIN;
		m.obj = userInfo.userName;
		mParentHandler.sendMessage(m);

		Log.d(TAG, "user [" + userInfo.userName + "] login");

		mPeerUserName = userInfo.userName;
		mNetworkServerThread.broadcastLogin(userInfo);

		UserManagerSingleton.UserInfo selfUserInfo = UserManagerSingleton
				.getInstance().getUserInfo(null, true);

		sendLoginAck(userInfo, selfUserInfo);
	}

	private void sendLoginAck(UserManagerSingleton.UserInfo loginUserInfo,
			UserManagerSingleton.UserInfo selfUserInfo) throws IOException {
		byte[] nameData = selfUserInfo.userName.getBytes();
		byte[] ipData = selfUserInfo.ip.getBytes();
		byte[] macData = selfUserInfo.mac.getBytes();

		// /xlr todo : temporary code
		Set<UserManagerSingleton.UserInfo> usersInfo = UserManagerSingleton
				.getInstance().getRegisterUsersInfo();
		int otherMemberDataLen = 0;
		short memberCount = 0;
		if (usersInfo != null) {
			for (UserManagerSingleton.UserInfo item : usersInfo) {

				if (loginUserInfo.userName.contentEquals(item.userName))
					continue;

				byte[] otherNameData = item.userName.getBytes();
				byte[] otherIPData = item.ip.getBytes();
				byte[] otherMacData = item.mac.getBytes();

				memberCount++;
				otherMemberDataLen = otherMemberDataLen + 2
						+ otherNameData.length + 2 + otherIPData.length + 2
						+ otherMacData.length;
			}
		}

		int ackLen = 8 + 4 + 2 + nameData.length + 2 + ipData.length + 2
				+ macData.length + otherMemberDataLen;

		mDataOutput.writeInt(ackLen);
		// ack version
		mDataOutput.writeShort(0);
		// ack type
		mDataOutput.writeShort(MsgDefine.NETWORK_MSG_LOGIN_ACK);
		// ack result
		mDataOutput.writeShort(0);

		// member count
		mDataOutput.writeShort(memberCount + 1);

		// user name
		mDataOutput.writeShort(nameData.length);
		mDataOutput.write(nameData);

		// user ip
		mDataOutput.writeShort(ipData.length);
		mDataOutput.write(ipData);

		// user mac
		mDataOutput.writeShort(macData.length);
		mDataOutput.write(macData);

		if (usersInfo != null) {
			for (UserManagerSingleton.UserInfo item : usersInfo) {
				if (loginUserInfo.userName.contentEquals(item.userName))
					continue;

				byte[] otherNameData = item.userName.getBytes();
				byte[] otherIPData = item.ip.getBytes();
				byte[] otherMacData = item.mac.getBytes();

				// user name
				mDataOutput.writeShort(otherNameData.length);
				mDataOutput.write(otherNameData);

				// user ip
				mDataOutput.writeShort(otherIPData.length);
				mDataOutput.write(otherIPData);

				// user mac
				mDataOutput.writeShort(otherMacData.length);
				mDataOutput.write(otherMacData);
			}
		}

		mDataOutput.flush();
	}

	private void onReceiveLoginAck(int PacketLen) throws IOException {
		// ack result
		short ackResult = mDataInput.readShort();

		// member count
		short memberCount = mDataInput.readShort();

		for (int i = 0; i < memberCount; i++) {
			UserManagerSingleton.UserInfo userInfo = new UserManagerSingleton.UserInfo();

			// request user name
			short len = mDataInput.readShort();
			byte[] temp = new byte[len];
			mDataInput.read(temp, 0, len);
			userInfo.userName = new String(temp);
			Log.d(TAG, "login name : " + userInfo.userName);

			// ip
			len = mDataInput.readShort();
			temp = new byte[len];
			mDataInput.read(temp, 0, len);
			userInfo.ip = new String(temp);
			Log.d(TAG, "login ip : " + userInfo.ip);

			// MAC
			len = mDataInput.readShort();
			temp = new byte[len];
			mDataInput.read(temp, 0, len);
			userInfo.mac = new String(temp);
			Log.d(TAG, "login mac : " + userInfo.mac);

			UserManagerSingleton.getInstance().addUserInfo(userInfo, false);

			Message m = new Message();
			m.what = MsgDefine.NETWORK_MSG_LOGIN_ACK;
			m.obj = userInfo.userName;
			mParentHandler.sendMessage(m);

			Log.d(TAG, "user login ack, name" + userInfo.userName);
		}

	}

	private void onReceiveLogout(int PacketLen) throws IOException {
		// hotspot flag
		int flag = mDataInput.readInt();

		// request user name
		short len = mDataInput.readShort();
		byte[] temp = new byte[len];
		mDataInput.read(temp, 0, len);
		String userName = new String(temp);

		// check user conflict
		int result = 0;
		if (UserManagerSingleton.getInstance().delUserInfo(userName) == null) {
			result = 1;
			Log.e(TAG, "receive [" + userName
					+ "] logout, but couldn't find the info");
			return;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_LOGOUT;
		m.arg1 = flag;
		m.obj = userName;

		mParentHandler.sendMessage(m);

		mNetworkServerThread.broadcastLogout(userName, flag);
	}

	private void onReceiveBroadcastLogin(int PacketLen) throws IOException {
		UserManagerSingleton.UserInfo userInfo = new UserManagerSingleton.UserInfo();

		// request user name
		short len = mDataInput.readShort();
		byte[] temp = new byte[len];
		mDataInput.read(temp, 0, len);
		userInfo.userName = new String(temp);
		Log.d(TAG, "broadcast login name : " + userInfo.userName);

		// check user conflict

		// ip
		len = mDataInput.readShort();
		temp = new byte[len];
		mDataInput.read(temp, 0, len);
		userInfo.ip = new String(temp);
		Log.d(TAG, "broadcast login ip : " + userInfo.ip);

		// MAC
		len = mDataInput.readShort();
		temp = new byte[len];
		mDataInput.read(temp, 0, len);
		userInfo.mac = new String(temp);
		Log.d(TAG, "broadcast login mac : " + userInfo.mac);

		UserManagerSingleton.getInstance().addUserInfo(userInfo, false);

		Message m = new Message();
		m.what = MsgDefine.NETWORK_BROADCAST_MSG_LOGIN;
		m.obj = userInfo.userName;
		mParentHandler.sendMessage(m);

		Log.d(TAG, "user [" + userInfo.userName + "] login broadcast ");
	}

	private void onReceiveBroadcastLogout(int PacketLen) throws IOException {
		// hotspot flag
		int flag = mDataInput.readInt();

		// request user name
		short len = mDataInput.readShort();
		byte[] temp = new byte[len];
		mDataInput.read(temp, 0, len);
		String userName = new String(temp);

		// check user conflict
		int result = 0;
		if (UserManagerSingleton.getInstance().delUserInfo(userName) == null) {
			result = 1;
			Log.e(TAG, "receive [" + userName
					+ "] logout broadcast , but couldn't find the info");
			return;
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT;
		m.arg1 = flag;
		m.obj = userName;

		mParentHandler.sendMessage(m);

		Log.d(TAG, "user [" + userName + "] logout broadcast ");
	}

	private void onReceiveKickout(int PacketLen) throws IOException {
		// kick user name
		short len = mDataInput.readShort();
		byte[] temp = new byte[len];
		mDataInput.read(temp, 0, len);
		String userName = new String(temp);

		int isKickoutSelf = 0;
		UserManagerSingleton.UserInfo info = UserManagerSingleton.getInstance()
				.getUserInfo(null, true);
		if (info.userName != null) {
			if (info.userName.contentEquals(userName)) {
				isKickoutSelf = 1;
			}
		}

		if (UserManagerSingleton.getInstance().delUserInfo(userName) == null) {
			Log.e(TAG, "receive [" + userName
					+ "] kickout broadcast , but couldn't find the info");
		}

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_KICKOUT;
		m.arg1 = isKickoutSelf;
		m.obj = userName;

		mParentHandler.sendMessage(m);

		Log.d(TAG, "user [" + userName + "] kickout broadcast, kickout type : "
				+ isKickoutSelf);
	}

	private void onReceiveFileRequest(int PacketLen) throws IOException {

		UserManagerSingleton.FileReceiveInfo info = new UserManagerSingleton.FileReceiveInfo();

		int requestSequence = mDataInput.readInt();

		// request user name
		short len = mDataInput.readShort();
		byte[] temp = new byte[len];
		mDataInput.read(temp, 0, len);
		info.userName = new String(temp);

		// app name
		len = mDataInput.readShort();
		temp = new byte[len];
		mDataInput.read(temp, 0, len);
		info.appName = new String(temp);

		// todo : get file name except path
		// file name
		len = mDataInput.readShort();
		temp = new byte[len];
		mDataInput.read(temp, 0, len);
		info.fileName = new String(temp);

		// file size
		info.fileSize = mDataInput.readInt();

		// file type
		info.fileType = mDataInput.readInt();
		
		// grant type
		info.grantType = mDataInput.readInt();

		// grant value
		info.grantValue = mDataInput.readInt();

		// grant reserve
		info.grantReserve = mDataInput.readInt();

		// md5
		len = mDataInput.readShort();
		info.md5 = new byte[len];
		mDataInput.read(info.md5, 0, len);

		//TODO: customize save path tangtaotao_20140326		
		//String path = cn.nd.social.util.Utils.getPrivateSharePath();
		String path = FilePathHelper.getPrivateSharePath(info.fileType);
		String filePath = path + File.separator + info.fileName;
		// todo : check file exist
		info.fileOutput = new FileOutputStream(filePath);

		int curSequence = UserManagerSingleton.getInstance().getSequence();

		UserManagerSingleton.getInstance()
				.addFileReceiveInfo(curSequence, info);

		NetworkServerThread.FileInfo notifyFileInfo = new NetworkServerThread.FileInfo();
		notifyFileInfo.userName = info.userName;
		notifyFileInfo.appName = info.appName;
		notifyFileInfo.fileName = info.fileName;
		notifyFileInfo.fileSize = info.fileSize;
		
		notifyFileInfo.fileType = info.fileType;

		notifyFileInfo.grantType = info.grantType;
		notifyFileInfo.grantValue = info.grantValue;
		notifyFileInfo.grantReserve = info.grantReserve;

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_FILE_SEND_REQ;
		m.obj = notifyFileInfo;
		mParentHandler.sendMessage(m);

		Log.d(TAG, "Has file [" + info.fileName + "] request, req user ["
				+ info.userName + "] seq : " + requestSequence + "ack seq : "
				+ curSequence);

		mPeerUserName = info.userName;

		sendFileRequestAck(requestSequence, curSequence);
	}

	private void sendFileRequestAck(int reqSeq, int curSeq) throws IOException {
		int ackLen = 8 + 12;
		mDataOutput.writeInt(ackLen);
		// ack version
		mDataOutput.writeShort(0);
		// ack type
		mDataOutput.writeShort(MsgDefine.NETWORK_MSG_FILE_SEND_REQ_ACK);

		// request sequence
		mDataOutput.writeInt(reqSeq);
		// ack result
		mDataOutput.writeInt(0);
		// ack sequence
		mDataOutput.writeInt(curSeq);

		mDataOutput.flush();
	}

	private void onReceiveFileRequestAck(int PacketLen) throws IOException {
		// request sequence
		int sendSeq = mDataInput.readInt();
		// ack result
		int result = mDataInput.readInt();
		// //ack sequence
		int recvSeq = mDataInput.readInt();

		UserManagerSingleton.FileSendInfo info = UserManagerSingleton
				.getInstance().getFileSendInfo(sendSeq);
		if (info != null) {
			Log.d(TAG, "file send start; send seq[" + sendSeq
					+ "], file recv seq[" + recvSeq + "]");

			mSendFileSeq = sendSeq;

			info.recvSequence = recvSeq;

			UserManagerSingleton.getInstance().addFileSendInfo(sendSeq, info);

			sendFileData();
		} else {
			Log.e(TAG, "file send sequence [" + sendSeq + "] can't find");
		}
	}

	private void sendFileData() throws IOException {
		if (mSendFileSeq == 0) {
			return;
		}

		UserManagerSingleton.FileSendInfo info = UserManagerSingleton
				.getInstance().getFileSendInfo(mSendFileSeq);

		byte[] readBytes = new byte[Utils.BUFF_SIZE];
		int len = Utils.BUFF_SIZE;

		short fileDataSN = info.fileDataSN;

		for (int i = 0; i < 20; i++) {
			len = info.fileInput.read(readBytes);
			if (len > 0) {
				int ackLen = 8 + 4 + 2 + 2 + len;
				mDataOutput.writeInt(ackLen);
				// ack version
				mDataOutput.writeShort(0);
				// ack type
				mDataOutput.writeShort(MsgDefine.NETWORK_MSG_FILE_DATA);

				// request sequence
				mDataOutput.writeInt(info.recvSequence);

				// file data send serial number, unique for one file;
				fileDataSN++;

				mDataOutput.writeShort(fileDataSN);

				// ack sequence
				mDataOutput.writeShort(len);

				mDataOutput.write(readBytes, 0, len);

				mDataOutput.flush();

				Log.d(TAG, "send file data seq[" + info.recvSequence + "] len["
						+ len + "]SN[" + fileDataSN + "]");
			}
		}

		info.fileDataSN = fileDataSN;
		UserManagerSingleton.getInstance().addFileSendInfo(mSendFileSeq, info);

		if (len < Utils.BUFF_SIZE) {
			// send file finish
			sendFileFinish(info);
			
			//commentted by tangtaotao 2014_0126 
/*			Message m = new Message();
			m.what = MsgDefine.HANDLER_NOTIFY_INFO;
			m.obj = "file :" + info.fileName + " sent";
			mParentHandler.sendMessage(m);*/

			Log.d(TAG, "file :" + info.fileName + " sent");

			mSendFileSeq = 0;
		}
	}

	private void onReceiveFileData(int PacketLen) throws IOException {
		int sequence = mDataInput.readInt();

		short fileDataSN = mDataInput.readShort();

		short dataLen = mDataInput.readShort();

		byte[] buffer = new byte[dataLen];
		int len = mDataInput.read(buffer, 0, dataLen);
		if (len <= 0) {
			return;
		}

		int result = 0;
		UserManagerSingleton.FileReceiveInfo info = UserManagerSingleton
				.getInstance().getFileReceiveInfo(sequence);
		if (info == null) {
			Log.e(TAG, "receive file data error , unknown seq " + sequence
					+ " SN " + fileDataSN);

			result = 1;
		} else {
			Log.d(TAG, "receive file data sequence " + sequence + " SN "
					+ fileDataSN);

			info.fileOutput.write(buffer, 0, len);

			if ((fileDataSN - info.fileDataSN) <= 0
					|| (fileDataSN - info.fileDataSN) > 1) {
				Log.e(TAG, "SN error, last SN " + info.fileDataSN + " SN "
						+ fileDataSN);
			}

			info.receivedSize = info.receivedSize + len;

			info.fileDataSN = fileDataSN;

			UserManagerSingleton.getInstance().addFileReceiveInfo(sequence,
					info);
		}

		sendFileDataAck(sequence, fileDataSN, result);
	}

	private void sendFileDataAck(int curSeq, short fileDataSN, int result)
			throws IOException {
		int ackLen = 8 + 10;
		mDataOutput.writeInt(ackLen);
		// ack version
		mDataOutput.writeShort(0);
		// ack type
		mDataOutput.writeShort(MsgDefine.NETWORK_MSG_FILE_DATA_ACK);

		// request sequence
		mDataOutput.writeInt(curSeq);
		// ack data SN
		mDataOutput.writeShort(fileDataSN);
		// ack result
		mDataOutput.writeInt(result);

		mDataOutput.flush();
	}

	private void onReceiveFileDataAck(int PacketLen) throws IOException {
		// req sequence
		int sequence = mDataInput.readInt();

		int fileDataSN = mDataInput.readShort();

		int result = mDataInput.readInt();
	}

	private void sendFileFinish(UserManagerSingleton.FileSendInfo info)
			throws IOException {
		int ackLen = 8 + 4;
		mDataOutput.writeInt(ackLen);
		// ack version
		mDataOutput.writeShort(0);
		// ack type
		mDataOutput.writeShort(MsgDefine.NETWORK_MSG_FILE_SEND_FIN);

		// request sequence
		mDataOutput.writeInt(info.recvSequence);

		mDataOutput.flush();

		NetworkServerThread.FileInfo handlerFileInfo = new NetworkServerThread.FileInfo();
		handlerFileInfo.userName = info.sendTo;
		handlerFileInfo.appName = info.appName;
		handlerFileInfo.fileName = info.fileName;

		Message notifyMsg = new Message();
		notifyMsg.what = MsgDefine.HANDLER_MSG_FILE_SEND_FIN;
		notifyMsg.obj = handlerFileInfo;
		mParentHandler.sendMessageDelayed(notifyMsg,100);
	}

	private void onReceiveFileFinish(int PacketLen) throws IOException {

		int sequence = mDataInput.readInt();

		int result = 0;

		UserManagerSingleton.FileReceiveInfo info = UserManagerSingleton
				.getInstance().getFileReceiveInfo(sequence);
		if (info == null) {
			Log.e(TAG, "receive file command(end) error : unknown sequence"
					+ sequence);

			result = 1;
		} else {
			info.fileOutput.flush();

			info.fileOutput.close();

			// todo : check file md5
			if (info.fileSize != info.receivedSize) {
				result = 2;
			}

			Log.d(TAG, "file : " + info.fileName + ",file size:"
					+ info.fileSize + ",actual size : " + info.receivedSize);
		}

		NetworkServerThread.FileInfo notifyFileInfo = new NetworkServerThread.FileInfo();
		notifyFileInfo.userName = info.userName;
		notifyFileInfo.fileName = info.fileName;
		notifyFileInfo.appName = info.appName;
		notifyFileInfo.fileType = info.fileType;
		notifyFileInfo.grantType = info.grantType;
		notifyFileInfo.grantValue = info.grantValue;
		notifyFileInfo.grantReserve = info.grantReserve;

		Message m = new Message();
		m.what = MsgDefine.NETWORK_MSG_FILE_SEND_FIN;
		m.arg1 = result;
		m.obj = notifyFileInfo;
		mParentHandler.sendMessage(m);
	}
	
	
	// sync browsing
	private void onReceiveSyncBrowingShakeHand(int PacketLen) throws IOException {
		int sequence = mDataInput.readInt();
		int ver = mDataInput.readShort();
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND;
		m.arg1 = ver;
		m.obj = mPeerUserName;
		mParentHandler.sendMessage(m);
	}
	
	private void onReceiveSyncBrowingShakeHandAck(int PacketLen) throws IOException {
		int sequence = mDataInput.readInt();
		
		int state = mDataInput.readInt();
		
		int width = mDataInput.readInt();
		
		int height = mDataInput.readInt();
		
		int pageCount = mDataInput.readInt();
		
		int curPage = mDataInput.readInt();
		
		int[] ackArray = new int[5];
		ackArray[0] = state;
		ackArray[1] = width;
		ackArray[2] = height;
		ackArray[3] = pageCount;
		ackArray[4] = curPage;
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND_ACK;
		m.obj = ackArray;

		mParentHandler.sendMessage(m);
	}
	
	private void onReceiveSyncBrowingPageSyncBroadcast(int PacketLen) throws IOException {
		int sequence = mDataInput.readInt();
		
		int pageNumber = mDataInput.readInt();
		
		int pageVer = mDataInput.readInt();
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST;
		m.arg1 = pageNumber;
		m.arg2 = pageVer;

		mParentHandler.sendMessage(m);
	}
	
	private void onReceiveSyncBrowingBroadcastAction(int PacketLen) throws IOException {
		int sequence = mDataInput.readInt();
		
		int pageNumber = mDataInput.readInt();
		
		int pageVer = mDataInput.readInt();
		
		short actionLen = mDataInput.readShort();
		byte[] action = new byte[actionLen];
		
		int len = mDataInput.read(action, 0, actionLen);
		if (len <= 0) {
			throw new IOException("can't read action data from socket connect");
		}
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION;
		m.arg1 = pageNumber;
		m.arg2 = pageVer;
		m.obj = action;

		mParentHandler.sendMessage(m);
	}
	
	private void onReceiveSyncBrowingBroadcastActionAck(int PacketLen) throws IOException {
		int sequence = mDataInput.readInt();
		
		short actionAckLen = mDataInput.readShort();
		byte[] actionAck = new byte[actionAckLen];
		
		int len = mDataInput.read(actionAck, 0, actionAckLen);
		if (len <= 0) {
			throw new IOException("can't read action data from socket connect");
		}
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION_ACK;
		m.obj = actionAck;

		mParentHandler.sendMessage(m);
	}
	
	private void onReceiveSyncBrowingPageRequest(int PacketLen) throws IOException {
		int sequence = mDataInput.readInt();
		
		int pageNumber = mDataInput.readInt();
		
		int curPageVer = mDataInput.readInt();
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST;
		m.arg1 = pageNumber;
		m.arg2 = curPageVer;
		m.obj = mPeerUserName;

		mParentHandler.sendMessage(m);
	}
	
	private void onReceiveSyncBrowingPageRequestAck(int PacketLen) throws IOException {
		
		NetworkServerThread.SyncBrowsingInfo info =  new NetworkServerThread.SyncBrowsingInfo(); 
		
		int sequence = mDataInput.readInt();
		
		// state
		int state = mDataInput.readInt();
		
		// page number
		info.pageNumber = mDataInput.readInt();
		
		// page version
		info.pageVersion = mDataInput.readInt();
		
		int len = mDataInput.readShort();
		byte [] temp = new byte[len];
		mDataInput.read(temp, 0, len);
		info.pageInfo = new String(temp);
		
		Message m = new Message();
		m.what = MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK;
		m.arg1 = state;
		m.obj = info;

		mParentHandler.sendMessage(m);
	}
}

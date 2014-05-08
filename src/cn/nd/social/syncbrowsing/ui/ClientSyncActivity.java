package cn.nd.social.syncbrowsing.ui;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.hotspot.NetworkCommunicateThread;
import cn.nd.social.hotspot.NetworkServerThread;
import cn.nd.social.hotspot.UserManagerSingleton;
import cn.nd.social.net.WifiController;
import cn.nd.social.net.WifiController.WifiConnectState;
import cn.nd.social.net.WifiStateConstant;
import cn.nd.social.syncbrowsing.SyncProtocol;
import cn.nd.social.syncbrowsing.manager.IClientSyncEventListener;
import cn.nd.social.syncbrowsing.utils.DialogUtil;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;

public class ClientSyncActivity extends Activity {
	
	public static final String TAG = "ClientSyncActivity";
	
	private final static int SUCCESS = 0;
	
	private final static int EVENT_CLIENT_USER_CONNECTED = 1001;
	
	private Context mContext;
	
	private View mClientReadPage;
	private ViewGroup mProgressWait;
	private ImageView mStartImg;
	private ConnectUserView mConnUserView;

	
	private int mWifiType;
	private ArrayList<String> mConnectedUser = new ArrayList<String>();
	private SyncNetworkProtocol mNetProtocol;
	
	private ClientSyncReadView mClientController;
	private int mNotifyCount = 0;
	
	private Handler mProtocolHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if(clientMsgHandler(msg)) {
				return;
			}

			if(fileTransMsgHandler(msg)) {
				return;
			}
			
			if (msg.what == MsgDefine.HANDLER_NOTIFY_INFO) {
				showInfo((String) msg.obj);
			} else if (msg.what == MsgDefine.NETWORK_MSG_LOGOUT) {
				String userName = (String) msg.obj;				
				onUserLogout(userName);
			} else if (msg.what == MsgDefine.NETWORK_MSG_KICKOUT) {

			}  
			super.handleMessage(msg);
		}
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {			
			case EVENT_CLIENT_USER_CONNECTED:
				clientSendProtocolVer();
				break;				
			default:
				break;
			}
		}
	};
	
	
	/*
	 * BroadcastReceiver receiver wifi event
	 */
	private BroadcastReceiver mWifiEventNotify = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int wifiState = intent.getIntExtra(WifiController.KEY_CONNECT_STATE, 
					WifiConnectState.NONE.ordinal());
				if(action.equals(WifiController.ACTION_WIFI_EVENT_NOTIFY)) {
					if (wifiState == WifiConnectState.WIFI_CONNECTED.ordinal()) {
						Log.v(TAG, "Wifi Hotspot enabled success!");
						networkFinish(WifiStateConstant.WIFI_TYPE_CLIENT, 0, "Wifi connected");
					} else if (wifiState == WifiConnectState.WIFI_CONNECT_FAIL.ordinal()) {
						Log.e(TAG, "Wifi Hotspot connect failed!");
						mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
						networkFinish(WifiStateConstant.WIFI_TYPE_CLIENT, 1,"Wifi connect failed");
					}
				}			
		}
	};
	

	private int getUserConnectCount() {
		return mConnectedUser.size();
	}
	
	private int getUserNotifyCount() {
		return mNotifyCount;
	}
	
	
	private boolean fileTransMsgHandler(Message msg) {
		boolean handled = true;
		switch(msg.what) {
		case MsgDefine.HANDLER_MSG_FILE_SEND_REQ:  //sender			
			break;
			
		case MsgDefine.HANDLER_MSG_FILE_SEND_FIN: {//sender
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;
			onSendOneFileFinish(info.userName, info.fileName);
		} 
			break;
			
		case MsgDefine.NETWORK_MSG_FILE_SEND_REQ: //receiver
			break;
			
		case MsgDefine.NETWORK_MSG_FILE_SEND_FIN: //receiver
			break;
			
		default:
			handled = false;
			break;
		}
		return handled;
	}
	

	
	private boolean clientMsgHandler(Message msg) {
		boolean handled = true;
		switch(msg.what) {
		case MsgDefine.HANDLER_MSG_CONNECTED: {
			NetworkCommunicateThread thread = (NetworkCommunicateThread) msg.obj;
			showInfo("client send login");
			if (!thread.checkIsFileThread()) {
				mNetProtocol.sendLogin();
			}
		}
			break;
			
		case MsgDefine.NETWORK_MSG_LOGIN_ACK: {
			String userName = (String) msg.obj;
			mConnectedUser.add(userName);
			mHandler.sendEmptyMessage(EVENT_CLIENT_USER_CONNECTED);
		}
			
			if(mConnUserView !=null) {
				mConnUserView.invalidate();
			}
			break;
			
		case MsgDefine.NETWORK_BROADCAST_MSG_LOGIN: {
			// /xlr todo : login broadcast
			String userName = (String) msg.obj;
			showInfo("broadcast login");
			mConnectedUser.add(userName);
		}
			if(mConnUserView !=null) {
				mConnUserView.invalidate();
			}
			
			break;
	
		case  MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT: {
			String userName = (String) msg.obj;
			int hotspotFlag = msg.arg1;
			mConnectedUser.remove(userName);
		}
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND_ACK: {
			
			int[] ackArray = (int[]) msg.obj;

			int state = ackArray[0];
			int width = ackArray[1];
			int height = ackArray[2];
			int pageCount = ackArray[3];
			int curPage = ackArray[4];
			onHandShakeAck(state, pageCount, curPage, width, height);
		}
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK: {
			int state = msg.arg1;
			NetworkServerThread.SyncBrowsingInfo info = (NetworkServerThread.SyncBrowsingInfo) msg.obj;
			clientOnRequestPageAck(state,info.pageNumber,info.pageVersion,info.pageInfo);
		}
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST: {
			int pageNumber = msg.arg1;
			int pageVer = msg.arg2;
			clientNotifyPage(pageNumber,pageVer);
		}
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION: {
			int pageNumber = msg.arg1;
			int pageVer = msg.arg2;
			
			byte[] action = (byte[]) msg.obj;
			clientNotifyAciton(pageNumber,pageVer,action);
		}
			break;

		default:
			handled = false;
			break;
		}
		return handled;
	}
	
	
	private void onHandShakeAck(int state,int pageCount,int curPage,int width,int height) {
		if(state == SyncConstant.STATE_NOT_READY || state == SyncConstant.STATE_SELF_VIEW) {
			mHandler.sendEmptyMessageDelayed(EVENT_CLIENT_USER_CONNECTED,100);
		} else {
			if(mClientController == null) {
				mClientController = new ClientSyncReadView(mClientEventListener);
				mClientController.init(mClientReadPage, pageCount,width,height);
			}
			if(state == SyncConstant.STATE_CONNECTED) {
				mClientReadPage.setVisibility(View.INVISIBLE);
			} else if(state == SyncConstant.STATE_VIEW) {
				toggleShowClientRead();
			}
		}
	}
	
	private void onSendOneFileFinish(String user,String fileName) {
		int pageNum = Integer.parseInt(fileName.substring(fileName.lastIndexOf("_")+1));
		String name = FilePathHelper.getNameFromFilepath(fileName);
		
		mNetProtocol.sycnRequestPageAck(user, 0, pageNum, 0, name);
	}
	
	private IClientSyncEventListener mClientEventListener = new IClientSyncEventListener() {		
		@Override
		public void onExitSync() {
			onBackPressed();
			
		}

		@Override
		public void requestPage(int page, int pageVer) {
			mNetProtocol.sycnRequestPage(page, pageVer)	;	
		}
	};
	
	/*******************wifi connection manager start******************/
	private void networkFinish(int type, int result, String info) {
		if(result == SUCCESS) {
			mWifiType = type;
			if (mNetProtocol != null) {
				mNetProtocol.cleanup();
			}
			UserManagerSingleton singleton = UserManagerSingleton.getInstance();
			singleton.clear();
			singleton.addUserInfo(null, true);
			
			mNetProtocol = new SyncNetworkProtocol(mProtocolHandler,mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT);
			
		} else {
			mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
		}
	}
	
	/*******************wifi connection manager end********************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.sync_browsing_client);
		setupViews();
		//tangtaotao_20140102 add start,
		networkFinish(WifiStateConstant.WIFI_TYPE_CLIENT,SUCCESS,null);
		toggleShowProgress();
		//tangtaotao_20140102 add end
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiController.ACTION_WIFI_EVENT_NOTIFY);
		registerReceiver(mWifiEventNotify, filter);
	}
	

	private void setupViews() {		
		mClientReadPage = findViewById(R.id.client_read_page);
		
		mProgressWait = (ViewGroup)findViewById(R.id.rl_progress);
		
		mStartImg = (ImageView) findViewById(R.id.start_img);
		
		findViewById(R.id.conn_wait_back).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();				
			}
		});
	}
	
	private void toggleShowProgress( ) {
		mClientReadPage.setVisibility(View.GONE);
		mProgressWait.setVisibility(View.VISIBLE);
		if (mConnUserView == null) {
			mConnUserView = new ConnectUserView(mContext,mStartImg,new ConnectUserView.CountSolver() {				
				@Override
				public int getNotifyCount() {
					return getUserNotifyCount();
				}
				
				@Override
				public int getConnectCount() {
					return getUserConnectCount();
				}
			});
			mProgressWait.addView(mConnUserView);
		}
	}
	
	private void toggleShowClientRead() {
		mClientReadPage.setVisibility(View.VISIBLE);
		mProgressWait.setVisibility(View.GONE);
	}
	
	
	private void onUserLogout(String userName) {		
		if(mNotifyCount >0) {
			mNotifyCount--;
		}

		if(mWifiType == WifiStateConstant.WIFI_TYPE_CLIENT) {//client should exit when host logout
			//the first user is the host
			if(mConnectedUser.size()<= 1 || mConnectedUser.get(0).equals(userName))
				mNetProtocol.cleanup();
				mNetProtocol = null;
				finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mWifiEventNotify);
		
		quitNetwork();
		
		if(mClientController != null) {
			mClientController.fini();
			mClientController = null;
		}
		
		if(mHandler != null) {
			mHandler.removeMessages(EVENT_CLIENT_USER_CONNECTED);
			mHandler = null;
		}
		
		super.onDestroy();
	}
	
	
	private void quitNetwork() {		
		if (mNetProtocol != null) {
			int isHost = mWifiType == 
					WifiStateConstant.WIFI_TYPE_HOTSPOT ? 1:0;
			mNetProtocol.sendLogout(isHost);
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mNetProtocol.cleanup();
			mNetProtocol = null;
		}		
		clearWifiState();
	}
	
	private void clearWifiState() {
		if(mWifiType == WifiStateConstant.WIFI_TYPE_CLIENT) {
			WifiController.getInstance().closeWifi();
		}
		mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
	}
	
	private void showInfo(String info) {
		Toast.makeText(mContext, info, Toast.LENGTH_LONG).show();
	}
	
	/*******************************************************************/
	/*********API for ClientSyncRead start*************/
	/*******************************************************************/

	public void clientRequestPage(int page) {
		mNetProtocol.sycnRequestPage(page, 0);
	}
	
	private void clientOnRequestPageAck(int state,int pageNum,int pageVer,String pageInfo) {
		if(state == 0) {
			String pathName = getRecvFilePath(pageInfo);
			byte [] image = DataFactory.getBytesFromFile(new File(pathName));
			mClientController.onPageRequestAck(image,pageNum);
		} else {
			showInfo("request page failed");
		}
	}
	
	private void clientSendProtocolVer() {
		mNetProtocol.syncHandShake(SyncProtocol.CLIENT_VERSION);
	}
	
	private void clientNotifyPage(int page,int pageVer) {
		mClientController.notifyPage(page);
	}
	
	
	private void clientNotifyAciton(int pageNum, int pageVer,byte[]action) {
		mClientController.notifyAction(pageNum,action);
	}
	
	/*******************************************************************/
	/*********API for ClientSyncRead end***************/
	/*******************************************************************/
	
	public static String getRecvFilePath(String name) {
		return (FilePathHelper.getPrivateSharePath(MsgDefine.FILE_TYPE_FILE) + File.separator + name);
	}
	

	@Override
	public void onBackPressed() {
		if(mWifiType != WifiStateConstant.WIFI_TYPE_NONE) {
			DialogUtil.showExitDialog(this);
		} else {
			super.onBackPressed();
		}
	}
}

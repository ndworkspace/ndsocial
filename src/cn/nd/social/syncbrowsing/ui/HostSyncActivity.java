package cn.nd.social.syncbrowsing.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.data.SyncHistoryProvider;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.hotspot.NetworkServerThread;
import cn.nd.social.hotspot.UserManagerSingleton;
import cn.nd.social.net.WifiController;
import cn.nd.social.net.WifiController.WifiConnectState;
import cn.nd.social.net.WifiStateConstant;
import cn.nd.social.qrcode.EncodingHandler;
import cn.nd.social.syncbrowsing.SyncProtocol;
import cn.nd.social.syncbrowsing.manager.IHostSyncEventListener;
import cn.nd.social.syncbrowsing.utils.DialogUtil;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.DimensionUtils;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.Utils;
import cn.nd.social.util.WifiInfoDataPacket;

import com.example.ofdmtransport.Modulation;
import com.example.ofdmtransport.ModulationAudioPlay;
import com.google.zxing.WriterException;

public class HostSyncActivity extends Activity {
	private final static String TAG = "HostSyncActivity";
	
	public static final String FILE_ID_KEY = "file_path";
	
	private final static int REQUEST_CODE_PICK_FILE = 1000;
	private final static int REQUEST_CODE_REPICK_FILE = 1001;
	
	private final static int EVENT_VIEW_DOC = 1003;
	private final static int EVENT_SERVER_USER_CONNECTED = 1000;
	
	private final static int SUCCESS = 0;
	
	private final static int MSG_LOGIN = 1;
	

	
	
	private View mUserListPage;
	private View mHostReadPage;
	private ViewGroup mProgressWait;
	private TextView mWaitHint;
	//private TextView mTxtFileName;
	private ImageView mStartImg;
//	private ConnectUserView mConnUserView;
	private ListView mConnUserListView;
	private View mPressAddMember;
	private ImageView mBCodeImg;
	private View mRadraRoot;
	private ProgressBar mRadar;
	private ImageView mRadarBg;
	private View mRadarTouch;


	private Context mContext;	
	private static WeakReference<HostSyncActivity> sHostActivity;
	
	private ModulationAudioPlay mAudioPlay = null;	
	private ArrayList<String> mConnectedUser = new ArrayList<String>();
	private SyncNetworkProtocol mNetProtocol;
	private HostSyncReadView mHostController;
	
	private String mHotspotName;
	private String mPasswd;
	private String mPath;
	private int mWifiType;
	private int mNotifyCount = 0;
	
	private int mHostState = SyncConstant.STATE_NOT_READY;
	
	private boolean mIsSelectingFile = true;
	private boolean mContinuePlay = false;
	private boolean mClientPending = false;

	
	public static HostSyncActivity getSyncActivity() {
		if(sHostActivity != null) {
			return sHostActivity.get();
		}
		return null;
	}
	
	private Handler mProtocolHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {			
			if(hostMsgHandler(msg)) {
				return;
			}

			if(fileTransMsgHandler(msg)) {
				return;
			}
			
			if (msg.what == MsgDefine.HANDLER_NOTIFY_INFO) {
				String info = (String) msg.obj;
				showInfo(info);
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
			case EVENT_SERVER_USER_CONNECTED:
				if (mHostController == null) {
					mHostController = new HostSyncReadView(mHostEventListener);
					mHostController.init(mHostReadPage, mPath);
					mHostReadPage.setVisibility(View.INVISIBLE);
				}
				if(mHostState == SyncConstant.STATE_SELF_VIEW) {
					mHostState = SyncConstant.STATE_VIEW;
					onSyncReady();
				} else if(mHostState != SyncConstant.STATE_VIEW){
					mHostState = SyncConstant.STATE_CONNECTED;
				}
				break;
				
			case EVENT_VIEW_DOC:
				if (mHostController == null) {
					mHostController = new HostSyncReadView(mHostEventListener);
					mHostController.init(mHostReadPage, mPath);
				} 
				toggleShowHostRead();

				if(mHostState == SyncConstant.STATE_CONNECTED) {
					mHostState = SyncConstant.STATE_VIEW;
					onSyncReady();
				} else {
					mHostState = SyncConstant.STATE_SELF_VIEW;
				}
				break;
				
			case Modulation.MODULATION_HANDLER_RECV_NOTIFY_ACK: { //server
					/*stopWifiInfoTrans();*/
					mNotifyCount++;
//					if(mConnUserView !=null) {
//						mConnUserView.invalidate();
//					}
				}
				break;
			case Modulation.MODULATION_HANDLER_PLAY_FINISH:
				if(mAudioPlay != null && mAudioPlay.isAlive()) {
					//comment out by tangtaotao_20140320 join may interrupt by this.interrupt
					/*try {
						mAudioPlay.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
				}
				mAudioPlay = null;
				if(mContinuePlay) {
					transWifiInfoIfNeed(1);
				}
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
			if(action.equals(WifiController.ACTION_HOTSPOT_EVENT_NOTIFY)) {				
				if(wifiState == WifiConnectState.WIFI_AP.ordinal()) {
					String  info = intent.getStringExtra(WifiController.KEY_EXTRA_INFO);
					networkFinish(WifiStateConstant.WIFI_TYPE_HOTSPOT, 0, "hotspot opened");
					onHotspotCreated(info);
				} else if(wifiState == WifiConnectState.CREATE_AP_FAIL.ordinal()) {
					mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
					networkFinish(WifiStateConstant.WIFI_TYPE_HOTSPOT, 1, "hotspot opened failed");
				} else {
					Log.e(TAG,"error hotspot state:" + wifiState);
				}
			}			
		}
	};


	
	

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
	
	private boolean hostMsgHandler(Message msg) {
		boolean handled = true;
		switch(msg.what) {
		case MsgDefine.HANDLER_MSG_NEW_CONNECT:
			break;
			
		case MsgDefine.NETWORK_MSG_LOGIN: {
				String userName = (String) msg.obj;
				showInfo(userName + "has just login");
				mConnectedUser.add(userName);
				//stopWifiInfoTrans(); // temporary solution

				Message mesage = mHandler.obtainMessage(EVENT_SERVER_USER_CONNECTED, HostSyncActivity.MSG_LOGIN, 0);
				mHandler.sendMessage(mesage);
				
//				if(mConnUserView != null) {
//					mConnUserView.invalidate();
//				}
				updateUserList();

			}
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND: {
				int ver = msg.arg1;
				String peerUserName = (String)msg.obj;
				onHandShake(peerUserName, ver);
			}
			break;
			
		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST: {
				int pageNumber = msg.arg1;
				int curPageVer = msg.arg2;
				String peerUserName = (String)msg.obj;
				serverOnPageRequest(peerUserName, pageNumber,curPageVer);
			}
			break;
			
		default:
			handled = false;
			break;
		}
		return handled;
	}
	    
	private void handlePendingHandShake(int ver) {
		//TODO: verify the version
		if(mHostController != null && mHostController.isReadyForSyncRead()) {
			for(String userName : mConnectedUser) {
				mNetProtocol.syncHandShakeAck(userName, mHostState,
						mHostController.getDocViewWidth(),
						mHostController.getDocViewHeight(),
						mHostController.getPageCount(),
						mHostController.getCurrPage());
			}
			mClientPending = false;
		} 
	}
	
	private void onHandShake(String peerUserName, int ver) {
		//TODO: verify the version
		Log.e("peerUserName", peerUserName);
		if(mHostController != null && mHostController.isReadyForSyncRead()) {
			mNetProtocol.syncHandShakeAck(peerUserName, mHostState,
					mHostController.getDocViewWidth(),
					mHostController.getDocViewHeight(),
					mHostController.getPageCount(),
					mHostController.getCurrPage());
		} else {
			mClientPending = true;
			//Add to pendding list
			mNetProtocol.syncHandShakeAck(peerUserName, SyncConstant.STATE_NOT_READY,0,0,
					0, 0);
		}
	}
	
	
	private void onSendOneFileFinish(String user,String fileName) {
		int pageNum = Integer.parseInt(fileName.substring(fileName.lastIndexOf("_")+1));
		String name = FilePathHelper.getNameFromFilepath(fileName);
		
		mNetProtocol.sycnRequestPageAck(user, 0, pageNum, 0, name);
	}
	
	/*******************wifi connection manager start******************/
	
	private void createHotspot() {
		if(mHotspotName == null) {
			mHotspotName = WifiController.genHotspotName();
			mPasswd = WifiController.genHotspotPasswd();
		}
		mWifiType = WifiStateConstant.WIFI_HOTSPOT_PENDING;
		/*when hotspot open, notify the user by broadcast */
		if(mHotspotName != null && mPasswd !=null) {
			WifiController.getInstance().createHotspot(mHotspotName,mPasswd);
		} else {
			WifiController.getInstance().createHotspot();
		}
	}
	
	private void onHotspotCreated(String info) {
		if(info != null && !info.equals("")) {
			String []wifiInfo = info.split(" ");
			mHotspotName = wifiInfo[0];
			mPasswd = wifiInfo[1];
		}	
		transWifiInfoIfNeed();
	}


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
		sHostActivity = new WeakReference<HostSyncActivity>(this);
		mContext = this;

		setContentView(R.layout.sync_browsing_host);
		setupViews();
		//tangtaotao_20140102 add start,
		Intent intent = getIntent();
		boolean isHost = intent.getBooleanExtra(Utils.SYNC_READ_HOST_KEY, false);
		boolean isNoSelect = intent.getBooleanExtra(Utils.SYNC_HOST_NO_SELECT, false);
		String passPath = intent.getStringExtra(Utils.SYNC_READ_PATH);
		if(isHost) {
			if (isNoSelect) {
				mIsSelectingFile = false;
				createSyncReadNoSelect(passPath);
			}else{
				mIsSelectingFile = true;
				createSyncRead();		
			}	
			
			showQrCode();				
		}
		toggleShowProgress();
		//tangtaotao_20140102 add end		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiController.ACTION_HOTSPOT_EVENT_NOTIFY);
		registerReceiver(mWifiEventNotify, filter);
	}	
	
	
	private void showQrCode() {
		WifiInfoDataPacket packer = new WifiInfoDataPacket(AudioDataPacker.TYPE_WIFI_SYNC_READ);
		String content = packer.packAudioData(new WifiInfoDataPacket.WifiInfoHolder(
							mHotspotName, mPasswd));
		if (!content.equals("") && content != null) {
//			mQrCode.setVisibility(View.VISIBLE);
			Bitmap qrCodeBitmap;
			try {
				qrCodeBitmap = EncodingHandler.createQRCode(content, DimensionUtils.getQrCodeDimen());
				mBCodeImg.setImageBitmap(qrCodeBitmap);
			} catch (WriterException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		intent.getBooleanExtra(SyncBrowserActivity.KEY_RESELECTED, false);
		boolean noSelect = intent.getBooleanExtra(Utils.SYNC_HOST_NO_SELECT, false);
		if(noSelect) {
			mPath = intent.getStringExtra(Utils.SYNC_READ_PATH);
			if(mHostController != null) {
				mHostController.updateDoc(mPath);
			}
		} else {
			Intent pickIntent = new Intent(this,FileViewActivity.class);		
			startActivityForResult(pickIntent, REQUEST_CODE_REPICK_FILE);
		}
		super.onNewIntent(intent);
	}
	

	private void setupViews() {
		
		mUserListPage = findViewById(R.id.connect_user_page);
		mHostReadPage = findViewById(R.id.host_read_page);		
		mProgressWait = (ViewGroup)findViewById(R.id.rl_progress);
		
		mWaitHint = (TextView)findViewById(R.id.sync_hint);		
		mStartImg = (ImageView) findViewById(R.id.start_img);
		//mTxtFileName = (TextView) findViewById(R.id.file_name);

		
		findViewById(R.id.wait_bottom).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				startViewDoc();
			}
		});
		
//		findViewById(R.id.conn_reback_btn).setOnClickListener(new View.OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				reselectFile();
//				
//			}
//		});
		
		findViewById(R.id.conn_wait_back).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();				
			}
		});
		
		
		findViewById(R.id.conn_user_back).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				toggleShowHostRead();				
			}
		});
		
		
		mRadraRoot = findViewById(R.id.sync_wait_for_connect);
		
		mRadarTouch =  mRadraRoot.findViewById(R.id.wait_touch);
		mRadar = (ProgressBar) mRadraRoot.findViewById(R.id.sync_radar_rotate);
		mRadarBg = (ImageView)mRadraRoot.findViewById(R.id.sync_radar_rotate_bg);		
		mBCodeImg = (ImageView)findViewById(R.id.sync_qr_image);
		//mRadraView.setOnTouchListener(mPressAddTouchListener);
		mRadarTouch.setOnTouchListener(mPressAddTouchListener);
		
		mConnUserListView = (ListView)findViewById(R.id.lv_conn_user);
		mPressAddMember = findViewById(R.id.add_member);
		
		mPressAddMember.setOnTouchListener(mPressAddTouchListener);

	}
	
	private View.OnTouchListener mPressAddTouchListener = new View.OnTouchListener() {			
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				transWifiInfoIfNeed(1);
				mContinuePlay = true;
				if(v == mPressAddMember){
					v.setBackgroundResource(R.color.sync_import_new_pressed);
				} else if(v == mRadarTouch) {
					mRadarBg.setImageResource(R.drawable.radar_cover);
				}
			} else if(event.getAction() == MotionEvent.ACTION_UP) {
				mContinuePlay = false;
				if(v == mPressAddMember){
					v.setBackgroundResource(R.color.sync_import_new_normal);
				} else if(v == mRadarTouch) {
					mRadarBg.setImageResource(R.drawable.radar_bg);
				}
			}
			return true;
		}
	};
	
	

	private Animation rotateAnim;
	
	private Animation getRotateAnim() {
		if(rotateAnim == null) {
			rotateAnim = AnimationUtils.loadAnimation(this,
					R.anim.radar_rotate_anim);
		}
		return rotateAnim;
	}
	
	private void toggleShowProgress( ) {
		mUserListPage.setVisibility(View.GONE);
		mHostReadPage.setVisibility(View.GONE);
		mProgressWait.setVisibility(View.VISIBLE);
		mRadar.startAnimation(getRotateAnim());
//		if (mConnUserView == null) {
//			mConnUserView = new ConnectUserView(mContext);
//			mProgressWait.addView(mConnUserView);
//		}
	
	}
	
	private void toggleShowHostRead() {
		mRadar.clearAnimation();
		mUserListPage.setVisibility(View.GONE);
		mHostReadPage.setVisibility(View.VISIBLE);
		mProgressWait.setVisibility(View.GONE);
	}	

	private void toggleShowUserList() {
		mUserListPage.setVisibility(View.VISIBLE);
		mHostReadPage.setVisibility(View.GONE);
		mProgressWait.setVisibility(View.GONE);
		ConnUserListAdapter adapter = new ConnUserListAdapter(mConnectedUser);
		mConnUserListView.setAdapter(adapter);		
	}
	
	private void updateUserList() {
		if(mUserListPage.getVisibility() == View.VISIBLE) {
			ConnUserListAdapter adapter = new ConnUserListAdapter(mConnectedUser);
			mConnUserListView.setAdapter(adapter);
		}
	}
	
	private void createSyncReadNoSelect(String path){
		createHotspot();
		mPath = path;
		mWaitHint.setText(R.string.sync_progress_connect);
		toggleShowProgress();//show progress
		
		String name = FilePathHelper.getNameFromFilepath(path);
		long currSec = System.currentTimeMillis();	
		SyncHistoryProvider hisProvider = SyncHistoryProvider.getInstance();
		if (!hisProvider.queryAndUpdate(name, MsgDefine.FILE_TYPE_FILE,path, currSec)) {
			hisProvider.addHistory(name, 
					MsgDefine.FILE_TYPE_FILE, path, currSec);
		}			
		//mTxtFileName.setText(name);
	}
	
	private void createSyncRead() {
		createHotspot();
		mWaitHint.setText(R.string.sync_progress_connect);
		toggleShowProgress();//show progress
		Intent intent = new Intent(this,FileViewActivity.class);		
		startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
	}

	
	private void pickFileResult(Intent data) {

		mIsSelectingFile = false;
		mPath = data.getStringExtra(FILE_ID_KEY);
		transWifiInfoIfNeed();
		
		String fileName = FilePathHelper.getNameFromFilepath(mPath);
		//mTxtFileName.setText(fileName);
		
		//add file to history database
		long currSec = System.currentTimeMillis();	
		SyncHistoryProvider hisProvider = SyncHistoryProvider.getInstance();
		if (!hisProvider.queryAndUpdate(fileName, MsgDefine.FILE_TYPE_FILE, mPath, currSec)) {
			hisProvider.addHistory(fileName, 
					MsgDefine.FILE_TYPE_FILE, mPath, currSec);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_CODE_PICK_FILE:
			if(resultCode != RESULT_OK) {
				return;
			}
			pickFileResult(data);
			break;
			
		case REQUEST_CODE_REPICK_FILE: 
			if(resultCode != RESULT_OK) {
				return;
			}
			pickFileResult(data);
			if(mHostController != null) {
				mHostController.updateDoc(mPath);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	private void transWifiInfoIfNeed() {
		if(mConnectedUser.size() < 1) {
			transWifiInfoIfNeed(5);
		}
	}
	
	private void startViewDoc() {
		if(mPath == null) {
			Toast.makeText(mContext, "no file select", Toast.LENGTH_SHORT).show();
			return;
		}
		if(!new File(mPath).exists()) {
			Toast.makeText(mContext, "file "+mPath + " not exist,please reselect file", Toast.LENGTH_SHORT).show();
			return;
		}
		
		mHandler.sendEmptyMessage(EVENT_VIEW_DOC);
	}
	
	private void transWifiInfoIfNeed(int playCount) {
		if(!mIsSelectingFile && mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT 
			&& mAudioPlay == null) {
			mAudioPlay = new ModulationAudioPlay();
			WifiInfoDataPacket packer = new WifiInfoDataPacket(AudioDataPacker.TYPE_WIFI_SYNC_READ);		
			String content = packer.packAudioData(
					new WifiInfoDataPacket.WifiInfoHolder(mHotspotName,mPasswd));
			mAudioPlay.set(mHandler, content, playCount, 800);
			mAudioPlay.setPlayFinishListener(new ModulationAudioPlay.PlayFinishListener() {				
				@Override
				public boolean onPlayFinish() {
					return !mContinuePlay;
				}
			});
			mAudioPlay.startPlay();
		}
	}
	
	private void onUserLogout(String userName) {		
		if(mNotifyCount >0) {
			mNotifyCount--;
		}
		mConnectedUser.remove(userName);
//		if(mConnUserView != null) {
//			mConnUserView.requestLayout();
//		}
		updateUserList();
	}
	
	@Override
	protected void onDestroy() {
		sHostActivity = null;
		unregisterReceiver(mWifiEventNotify);
		
		quitNetwork();
		
		stopWifiInfoTrans();
		
		if(mHostController != null) {
			mHostController.fini();
			mHostController = null;
		}
		
	
		if(mHandler != null) {
			mHandler.removeMessages(EVENT_SERVER_USER_CONNECTED);
			mHandler = null;
		}
		
		super.onDestroy();
	}
	
	
	private void quitNetwork() {		
		if (mNetProtocol != null) {
			int isHost = 
					mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT ? 1:0;
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
		if(mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT || mWifiType == WifiStateConstant.WIFI_HOTSPOT_PENDING) {
			WifiController.getInstance().closeHotspot();
		} 
		mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
	}
	
	private void onSyncReady() {
		handlePendingHandShake(0);
		if(mHostController.getCurrPage() != 0) {
			mNetProtocol.sycnSendPageBroadcast(mHostController.getCurrPage(), 0);
		}
	}
	
	private void showInfo(String info) {
		Toast.makeText(mContext, info, Toast.LENGTH_LONG).show();
	}
	
	/*******************************************************************/
	/*********API for HostSycnRead and ClientSyncRead start*************/
	/*******************************************************************/
	public void notifyPageFirst(int page) {
		if(mClientPending) {
			handlePendingHandShake(SyncProtocol.CLIENT_VERSION);			
		}
		//mNetProtocol.sycnSendPageBroadcast(page, 0);
	}
	
	public void notifyPage(int page) {
		if(!hasUserConnected()) {
			return;
		}
		mNetProtocol.sycnSendPageBroadcast(page, 0);
	}
	
	public void notifyAction(int pageNum,byte[] action) {
		if(!hasUserConnected()) {
			return;
		}
		mNetProtocol.sycnBroadcastAction(pageNum, 0, action);
	}
	
	
	public void sendPage(String peerUserName, byte[] image,int pageNum) {
		String pathName = "";
		pathName = getFilePath("" + System.currentTimeMillis() + "_" + pageNum);
		DataFactory.getFileFromBytes(image, pathName);
		mNetProtocol.sendFile(peerUserName, pathName, "empty", MsgDefine.FILE_TYPE_FILE);
		//mNetProtocol.sendFile(mConnectedUser.get(0), mPathName, "empty", MsgDefine.FILE_TYPE_UNKNOWN);
	}
	
	private void serverOnPageRequest(String peerUserName, int pageNum,int pageVer) {
		Log.e("peerUserName", peerUserName);
		mHostController.onPageRequest(peerUserName, pageNum);
	}
	
	/*******************************************************************/
	/*********API for HostSycnRead and ClientSyncRead end***************/
	/*******************************************************************/
	
	private boolean hasUserConnected() {
		return mConnectedUser.size() > 0;
	}
	
	public static String getFilePath(String name) {
		return (FilePathHelper.getSyncPath() + File.separator + name);
	}

	@Override
	public void onBackPressed() {
		if(mWifiType != WifiStateConstant.WIFI_TYPE_NONE) {
			DialogUtil.showExitDialog(this);
		} else {
			super.onBackPressed();
		}
	}
	private IHostSyncEventListener mHostEventListener = new IHostSyncEventListener() {

		@Override
		public void onExitSync() {
			onBackPressed();			
		}

		@Override
		public void reselectFile() {
			Intent intent = new Intent(mContext,SyncBrowserActivity.class);
			intent.putExtra(SyncBrowserActivity.KEY_RESELECT_DOC, true);
			startActivity(intent);
			
		}

		@Override
		public void showConnectedUserList() {
			toggleShowUserList();			
		}

	
		@Override
		public void notifyPageFirst(int page) {
			if(mClientPending) {
				handlePendingHandShake(SyncProtocol.CLIENT_VERSION);			
			}
			//mNetProtocol.sycnSendPageBroadcast(page, 0);
		}
		
		@Override
		public void notifyPage(int page,byte[]image) {
			if(!hasUserConnected()) {
				return;
			}
			mNetProtocol.sycnSendPageBroadcast(page, 0);
		}
		
		@Override
		public void notifyAction(int pageNum,byte[] action) {
			if(!hasUserConnected()) {
				return;
			}
			mNetProtocol.sycnBroadcastAction(pageNum, 0, action);
		}
		
		@Override
		public void sendPage(int state,String peerUserName, byte[] image,int pageNum) {
			String pathName = "";
			pathName = getFilePath("" + System.currentTimeMillis() + "_" + pageNum);
			DataFactory.getFileFromBytes(image, pathName);
			mNetProtocol.sendFile(peerUserName, pathName, "empty", MsgDefine.FILE_TYPE_FILE);
			//mNetProtocol.sendFile(mConnectedUser.get(0), mPathName, "empty", MsgDefine.FILE_TYPE_UNKNOWN);
		}

		@Override
		public void syncReady() {
			// TODO Auto-generated method stub
			
		}
		
	};

	
	private void stopWifiInfoTrans() {
		if(mAudioPlay != null) {
			mAudioPlay.stopPlay();
			mAudioPlay = null;
		}
	}

}

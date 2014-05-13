package cn.nd.social.prishare.component;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;
import cn.nd.social.data.MsgDBHelper;
import cn.nd.social.data.MsgProviderSingleton;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.hotspot.NetworkCommunicateThread;
import cn.nd.social.hotspot.NetworkServerThread;
import cn.nd.social.hotspot.UserManagerSingleton;
import cn.nd.social.net.NetworkProtocolImpl;
import cn.nd.social.net.ProtocolHandler;
import cn.nd.social.net.ProtocolHandler.HandlerInterface;
import cn.nd.social.net.WifiController;
import cn.nd.social.net.WifiShareManager;
import cn.nd.social.net.WifiStateConstant;
import cn.nd.social.prishare.PriShareConstant;
import cn.nd.social.prishare.PriShareSendActivity;
import cn.nd.social.prishare.PriShareSendActivity.UIHandler;
import cn.nd.social.prishare.component.MainHandler.MainMsgHandlerInterface;
import cn.nd.social.prishare.history.HistoryItemHelper;
import cn.nd.social.prishare.history.PrivacyShareHistoryView;
import cn.nd.social.privategallery.PrivateGalleryProvider;
import cn.nd.social.privategallery.PrivateItemEntity;
import cn.nd.social.privategallery.PrivateItemEntity.PrivateItemOrginalInfo;
import cn.nd.social.services.ISocialService;
import cn.nd.social.services.SocialService.ServiceBinder;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.CommonUtils;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.LogToFile;
import cn.nd.social.util.WifiInfoDataPacket;

import com.example.ofdmtransport.ModulationAudioPlay;
import com.example.ofdmtransport.ModulationAudioRecord;

/**
 * mainly handle the messages received by Main.java
 * 
 * @author Administrator
 * 
 */
public class InterfaceHandlerForMain {

	private final static String TAG = "Main";

	/** Member variables **/
	private Activity mMainAct;
	HistoryItemHelper mHisItemHelper;
	ArrayList<String> mConnectedUser;

	private MainHandler mPrivateHandler;
	private ProtocolHandler mPublicHandler;
	UIHandler mUIHandler;

	private PriShareSendActivity mMainClass;
	private String wifiInfoToTrans = "";

	/** network variables **/

	private ModulationAudioPlay mAudioPlay = null;
	private ModulationAudioRecord mAudioRecorder = null;
	private int mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
	

	NetworkProtocolImpl mNetProtocol;
	
	private String mHotspotName;
	private String mPasswd;

	public InterfaceHandlerForMain(Activity act, PrivacyShareHistoryView PSHistory, PriShareSendActivity m) {
		this.mMainAct = act;
		this.mMainClass = m;
		this.mHisItemHelper = PSHistory.getHistoryHelper();

		mPrivateHandler = new MainHandler();
		mPrivateHandler.setDisposer(mMainMsgHandlerListerner);
		mPublicHandler = new ProtocolHandler();
		mPublicHandler.setDisposer(mMainHandlerListener);

		mConnectedUser = mMainClass.getmConnectedUser();		
		
		createHotspot();
	}
	
	public void regReceiver() {
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(WifiShareManager.WIFIACTION_AP_CREATE_SUCCESS);
		filter.addAction(WifiShareManager.WIFIACTION_AP_CREATE_TIMEOUT);
		filter.addAction(WifiShareManager.WIFIACTION_AP_CREATE_ERROR);
		filter.addAction(WifiShareManager.WIFIACTION_AP_CLOSED);
		filter.addAction(WifiShareManager.WIFIACTION_AP_BREAK);
		mMainAct.registerReceiver(mWifiMgrNotify, filter);
	}
	
	private void createHotspot() {		/* when hot spot open, notify the user by broadcast */
		if(mHotspotName == null) {
			mHotspotName = WifiController.genHotspotName();
			mPasswd = WifiController.genHotspotPasswd();
		}
		WifiShareManager.getShareInstance(mMainAct).createAPAsync(mHotspotName,mPasswd);
		mWifiType = WifiStateConstant.WIFI_HOTSPOT_PENDING;
		if(mNetProtocol != null) {
			mNetProtocol.cleanup();
			mNetProtocol = null;
		}
		
		genWifiInfo();
	}
	
	private void genWifiInfo() {
		WifiInfoDataPacket packer = new WifiInfoDataPacket(AudioDataPacker.TYPE_WIFI_PRIVATE_SHARE);
		wifiInfoToTrans = packer.packAudioData(new WifiInfoDataPacket.WifiInfoHolder(
						mHotspotName, mPasswd));
	}
	
	public String getWifiInfoToTrans() {
		if(wifiInfoToTrans == null) {
			genWifiInfo();
		}
		return wifiInfoToTrans;
	}
	
	public void setUIHandler(UIHandler handler) {
		mUIHandler = handler;
	}

	/** gets & sets **/

	public int getWifiType() {
		return mWifiType;
	}


	/** Handler **/

	MainMsgHandlerInterface mMainMsgHandlerListerner = new MainMsgHandlerInterface() {

		@Override
		public void onShowInfo(Message msg) {
			String info = (String) msg.obj;
			Toast.makeText(mMainAct, info, Toast.LENGTH_LONG).show();
		}

		//receive wifi info ack
		@Override
		public void onModulationHandlerRecvNotifyAck(Message msg) {
			stopTransWifiInfo();
			//showInfo("wifi info ack arrived");
		}

		@Override
		public void onModulationHandlerRecvNotify(Message msg) {
			String info = (String) msg.obj;
			//showInfo("wifi info arrived : " + info);

			stopRecvWifiInfo();

			String[] temp = null;
			temp = info.split(" ");

			connectWifi(temp[0], temp[1]);
		}

		@Override
		public void onHandlerMsgCreateHotSpot(Message msg) {
			int result = msg.arg1;
		}

		@Override
		public void onHandlerMsgConnectWifi(Message msg) {
			int result = msg.arg1;

		}

		@Override
		public void onModulationHandlerPlayFinish(Message msg) {
			if(mAudioPlay != null && mAudioPlay.isAlive()) {
				//mAudioPlay.stopPlay();
				//comment out tangtaotao_20140320 join may cause problem here
/*				try {
					
					mAudioPlay.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
				
			}
			mAudioPlay = null;
			if(mContinuePlay) {
				pressTransWifiInfo(1);
			}		
		}
	};

	HandlerInterface mMainHandlerListener = new HandlerInterface() {

		/** user manager */
		@Override
		public void onUserLogout(Message msg) {
			String userName = (String) msg.obj;
			//showUserName(userName);
			userLogout(userName, 0);
			showInfo("user " + userName + " logout");

		}


		@Override
		public void onKickedOut(Message msg) {
			int isKickoutSelf = msg.arg1;
			String kickoutName = (String) msg.obj;
			updateUIOnBeKickouted(kickoutName, isKickoutSelf);
			showInfo("user " + kickoutName + " is kickout");

		}

		@Override
		public void onUserLoginAck(Message msg) {
			String userName = (String) msg.obj;
			//showUserName(userName);
			userLogin(userName);
			showInfo(userName + "login ack");

		}

		//broadcast login message
		@Override
		public void onUserLogin(Message msg) {
			//ignore broadcast message for hotspot
			if(mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT) {
				return;
			}
			String userName = (String) msg.obj;
			userLogin(userName);

		}

		@Override
		public void onUserLogoutBroadCastMsg(Message msg) {
			String userName = (String) msg.obj;
			int hotspotFlag = msg.arg1;
			userLogout(userName, hotspotFlag);
		}

		@Override
		public void onNewConnect(Message msg) {
			NetworkCommunicateThread thread = (NetworkCommunicateThread) msg.obj;
			if (!thread.checkIsFileThread()) {
				mNetProtocol.sendLogin();
				showInfo("send login info");
			}

		}

		/** user manager */

		/** Sync start **/

		@Override
		public void onSyncShakehandAck(Message msg) {
		}

		@Override
		public void onSyncShakehand(Message msg) {
		}

		@Override
		public void onSyncPageBroadcast(Message msg) {
		}

		@Override
		public void onSyncGetPageRequest(Message msg) {
		}

		@Override
		public void onSyncGetPageAck(Message msg) {
		}

		@Override
		public void onSyncActionBroadcast(Message msg) {
		}

		@Override
		public void onSynActioncAck(Message msg) {
		}

		/** Sync End **/

		/** file transfer start */

		@Override
		public void onSendFileStart(Message msg) {
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;

			sendFileStart(info.userName, info.appName, info.fileName,
					info.fileSize, info.fileType, info.grantType,
					info.grantValue, info.grantReserve);

		}

		@Override
		public void onSendFileEnd(Message msg) {
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;

			sendOneFileFinish(info.userName, info.appName, info.fileName);
			mMainClass.sendRemainingFiles(info.userName);

		}

		@Override
		public void onReceiveFileStart(Message msg) {
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;
			recvFileStart(info.userName, info.appName, info.fileName,
					info.fileSize, info.fileType, info.grantType,
					info.grantValue, info.grantReserve);

		}

		@Override
		public void onReceiveFileEnd(Message msg) {
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;
			Long utcOfCurrent = System.currentTimeMillis();
			recvOneFileFinish(info.userName, info.appName, info.fileName,
					info.fileType, info.grantType, info.grantValue,
					info.grantReserve,utcOfCurrent);

		}

		/** file transfer end */

		/** Host Handler **/

		@Override
		public void onHandleerNewConnect(Message msg) {
			//showInfo("has new connected arrive");
		}

		@Override
		public void onNetworkMsgLogin(Message msg) {
			String userName = (String) msg.obj;
			//showUserName(userName);
			userLogin(userName);
			stopTransWifiInfo();
		}

		@Override
		public void onShowInfo(Message msg) {
			String info = (String) msg.obj;
			Toast.makeText(mMainAct, info, Toast.LENGTH_LONG).show();
		}

		/** Host Handler End **/

	};
	
	
	public void stopTransWifiInfo() {
		if (mAudioPlay != null) {
			mAudioPlay.stopPlay();
			mAudioPlay = null;
		}
	}
	
	private void stopRecvWifiInfo() {
		if (mAudioRecorder != null) {
			mAudioRecorder.stopRecord();
			mAudioRecorder = null;
		}
	}

	/** member function **/

	private void showInfo(String info) {
		Toast.makeText(mMainAct, info, Toast.LENGTH_LONG).show();
	}


	private void connectWifi(String ssid, String passwd) {
		// clear the state
		mConnectedUser.clear();
		mWifiType = WifiStateConstant.WIFI_CONNECT_PENDING;
		WifiShareManager.getShareInstance(mMainAct).connectWifiAsync(ssid, passwd);
	}

	/** User Manager Handler **/

	private void updateUIOnBeKickouted(String name, int isKickoutSelf) {
		mConnectedUser.remove(name);

		if (isKickoutSelf == 1 ) { //|| mConnectedUser.size() == 0
			if (mNetProtocol != null) {
				mNetProtocol.cleanup();
				mNetProtocol = null;
			}

			clearWifiState();
			stopTransWifiInfo();

			mMainClass.userKickedOutAll();
			
		} else {

			mMainClass.userKickedOutSingle(name);

		}
	}

	public void clearWifiState() {
		if (mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT || mWifiType == WifiStateConstant.WIFI_HOTSPOT_PENDING) {
			WifiShareManager.getShareInstance(mMainAct).closeAp();
		} else if (mWifiType == WifiStateConstant.WIFI_TYPE_CLIENT) {
			WifiShareManager.getShareInstance(mMainAct).closeWifi();
		}
		mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
	}

	// original name: onUserLogin

	private void userLogin(String name) {
		if(!mConnectedUser.contains(name)) {
			mConnectedUser.add(name);
	
			// check if we can pending send request
			Message message = mUIHandler.obtainMessage();
			message.obj = name;
			message.what = MsgDefine.MAIN_UI_HANDLER_USER_LOGIN;
			message.arg1 = 0;
			mUIHandler.sendMessage(message);
		}

	}

	// original name: onUserLogout
	private void userLogout(String name, int hotspotFlag) {
		mConnectedUser.remove(name);

		if (mWifiType == WifiStateConstant.WIFI_TYPE_CLIENT && mConnectedUser.size() == 0) {

			if (mNetProtocol != null) {
				mNetProtocol.cleanup();
				mNetProtocol = null;
			}

			clearWifiState();

			stopTransWifiInfo();
			
			updateUIOnLogout(name, true);
		} else {
			updateUIOnLogout(name, false);
		}

	}

	private void updateUIOnLogout(String name, boolean clearView) {
/*		if (clearView) {
			mMainClass.userKickedOutAll();
		} else {*/
			mMainClass.userKickedOutSingle(name);
		//}
	}

	/** Send files **/
	private String mCurrentSendFile = null;
	private int mCurrentSendFileIndex;
	private String mCurrentSendUser = null;

	// original name: onSendFileStart
	void sendFileStart(String sendUser, String appName, String fileName,
			long fileSize, int fileType, int grantType, int grantValue,
			int grantReserve) {
		mCurrentSendFile = fileName;
		mCurrentSendUser = sendUser;

		mMainClass.pagerChange(PriShareConstant.HISTORY_INDEX);

		mCurrentSendFileIndex = mHisItemHelper.addItem("me", sendUser, appName,
				fileName, fileSize, fileType, grantType, grantValue,
				grantReserve);

		Log.d(TAG, "history send index : " + mCurrentSendFileIndex);

		mPrivateHandler.postDelayed(mSendCyclicTransProgressCheck, 100);
	}

	private int getSendProgress(String user, String fileName) {
		return mNetProtocol.queryFileSendProgress(user, fileName);
	}

	// origianl name: onSendOneFileFinish
	void sendOneFileFinish(String sendUser, String appName, String fileName) {

		mHisItemHelper.changeItemProgress(mCurrentSendFileIndex, 100);

		Log.d(TAG, "history send index : " + mCurrentSendFileIndex
				+ " process show finished");

		mPrivateHandler.removeCallbacks(mSendCyclicTransProgressCheck);
		
		
		//tangtaotao@ND_20140219
		MsgProviderSingleton.getInstance().addRecord(
				-1, 
				sendUser, 
				"send file:" + fileName, 
				MsgDBHelper.NullValue,
				fileName,
				fileName,
				"send",
				MsgDefine.FILE_TYPE_UNKNOWN,
				PriShareConstant.INFINITE_TIME,
				MsgDefine.STATUS_DO_NOTHING);
	}

	/** Receive Files **/
	private String mCurrentRecvUser = null;
	private String mCurrentRecvFile = null;
	private int mCurrentRecvFileIndex;
	private boolean mCheckFileDestory = true;

	// original name onRecvFileStart
	void recvFileStart(String recvUser, String appName, String fileName,
			long fileSize, int fileType, int grantType, int grantValue,
			int grantReserve) {
		mCurrentRecvFile = fileName;
		mCurrentRecvUser = recvUser;

		mMainClass.pagerChange(PriShareConstant.HISTORY_INDEX);

		String fileExtName = FilePathHelper.getExtFromFilename(fileName);
		if (fileExtName.contentEquals("jpg")
				|| fileExtName.contentEquals("jpeg")
				|| fileExtName.contentEquals("png")) {
			fileType = MsgDefine.FILE_TYPE_IMAGE;
		}

		mCurrentRecvFileIndex = mHisItemHelper.addItem(recvUser, "me", appName,
				fileName, fileSize, fileType, grantType, grantValue,
				grantReserve);
		Log.d(TAG, "history recv index : " + mCurrentRecvFileIndex);

		mPrivateHandler.postDelayed(mRecvCyclicTransProgressCheck, 300);
	}

	private int getRecvProgress(String user, String fileName) {
		return mNetProtocol.queryFileRecvProgress(user, fileName);
	}

	// original name: onRecvOneFileFinish
	void recvOneFileFinish(String recvUser, final String appName,
			String fileName, int fileType, int grantType, int grantValue,
			int grantReserve,Long utcForFileName) {

		mPrivateHandler.removeCallbacks(mRecvCyclicTransProgressCheck);

		Log.d(TAG, "history recv index : " + mCurrentRecvFileIndex
				+ " process show finished");
		String path = FilePathHelper.getPrivateSharePath(fileType);
		String newName = appName;
		switch (fileType) {
		case MsgDefine.FILE_TYPE_APP:
			if (grantType == MsgDefine.GRANT_APK_SILENT_INSTALL) {
				Log.e(TAG, "silent install file:" + fileName + " apk:"
						+ appName);

				final String pathName = path + File.separator + fileName;
				path = pathName;
				new Thread() {
					public void run() {
						final boolean result = CommonUtils.installApkRooted(new File(pathName),
								mMainAct);
						Message message = mUIHandler.obtainMessage();
						message.obj = appName;
						message.what = MsgDefine.MAIN_UI_HANDLER_RECV_FILE_FINISH;
						message.arg1 = result ? 1 : 0;
						mUIHandler.sendMessage(message);

					}
				}.start();
			}
			
			break;

		case MsgDefine.FILE_TYPE_IMAGE: {
			Log.d(TAG, "file type : image");

			
			path = path + File.separatorChar + fileName;
			PrivateItemOrginalInfo info = new PrivateItemOrginalInfo();
			info.orgPath = path;
			info.filename = fileName;
			info.controlType = grantType == MsgDefine.GRANT_FILE_AUTO_DESTROY ? 1:0;

			PrivateGalleryProvider provider = PrivateGalleryProvider
					.getInstance();
			PrivateItemEntity entity = PrivateItemEntity.from(info,utcForFileName);

			if (PrivateItemEntity.addFileToPrivateGallery(provider, entity)) {
				if (grantType == MsgDefine.GRANT_FILE_AUTO_DESTROY) {
					ServiceBinder binder = SocialApplication.getAppInstance()
							.getServiceBinder();
					ISocialService.FileControlPara para = new ISocialService.FileControlPara();
					para.startTime = entity.createUtc / 1000;
					para.fileName = entity.path;
					para.expireTime = grantValue;
					binder.AddFileControl(para);
				}

				mHisItemHelper.changeItemFileName(mCurrentRecvFileIndex,
						entity.path);				
			} else {
				Log.e(TAG, "addFileToPrivateGallery failed");
			}
			break;
		}

		default:
			break;
		}

		mHisItemHelper.changeItemProgress(mCurrentRecvFileIndex, 100);

		if (mCheckFileDestory) {
			mPrivateHandler.postDelayed(mFileAutoDestoryCheck, 500);

			mCheckFileDestory = false;
		}
		if (fileType == MsgDefine.FILE_TYPE_IMAGE) {
			path = cn.nd.social.privategallery.Utils.getPrivateFilePath();
		}
		
		final int status;
		if (grantValue != -1) {		
			status = MsgDefine.STATUS_NEED_DELETE;
		}else{
			status = MsgDefine.STATUS_DO_NOTHING;
		}
		MsgProviderSingleton.getInstance().addRecord(
				-1, 
				recvUser, 
				"receive file:" + appName, 
				path, 
				fileName,
				String.valueOf(utcForFileName / 1000),
				"recv", 
				fileType, 
				grantValue,
				status);

	}

	
	/** Runnable functions **/

	// check the file transfering progress
	Runnable mSendCyclicTransProgressCheck = new Runnable() {
		@Override
		public void run() {
			if (mCurrentSendFile != null) {
				int progress = getSendProgress(mCurrentSendUser,
						mCurrentSendFile);

				Log.d("sendProgress", "sending progress:" + progress);

				mHisItemHelper.changeItemProgress(mCurrentSendFileIndex,
						progress);
				mUIHandler.sendMessage(mUIHandler.obtainMessage(MsgDefine.MAIN_UI_HANDLER_SEND_PROGRESS, progress, 0));

				mPrivateHandler.postDelayed(this, 200);
				
			} else {
				mPrivateHandler.removeCallbacks(this);
			}

		}
	};

	// check the file transfering progress
	Runnable mRecvCyclicTransProgressCheck = new Runnable() {
		@Override
		public void run() {
			if (mCurrentRecvFile != null) {
				int progress = getRecvProgress(mCurrentRecvUser,
						mCurrentRecvFile);

				Log.d("recvProgress", "recving progress:" + progress);

				mHisItemHelper.changeItemProgress(mCurrentRecvFileIndex,
						progress);

				mPrivateHandler.postDelayed(this, 300);
			} else {
				mPrivateHandler.removeCallbacks(this);
			}
		}
	};

	// check the file transfering progress
	Runnable mFileAutoDestoryCheck = new Runnable() {
		@Override
		public void run() {
			mHisItemHelper.refreshTimer();

			mPrivateHandler.postDelayed(this, 500);
		}
	};

	/** network **/

	private void networkFinish(int type, int result, String info) {
		
		Message message = mPrivateHandler.obtainMessage();
		if (result == 0) {
			mWifiType = type;
			
			if (type == WifiStateConstant.WIFI_TYPE_HOTSPOT) {
				message.obj = "create wifi hotspot success";
				message.what = MsgDefine.HANDLER_MSG_CREATE_HOTSPOT;
				message.arg1 = 0;
				mPrivateHandler.sendMessage(message);
			} else {
				message.obj = "connect wifi success";
				message.what = MsgDefine.HANDLER_MSG_CONNECT_WIFI;
				message.arg1 = 0;
				mPrivateHandler.sendMessage(message);
			}
			
			
			if (mNetProtocol != null) {
				mNetProtocol.cleanup();
			}
			mNetProtocol = new NetworkProtocolImpl(mPublicHandler,
					mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT);

			UserManagerSingleton singleton = UserManagerSingleton.getInstance();
			singleton.addUserInfo(null, true);
		} else if (result == 1) {
			mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
			
			if (type == WifiStateConstant.WIFI_TYPE_HOTSPOT) {				
				message.obj = "create wifi hotspot timeout";
				message.what = MsgDefine.HANDLER_NOTIFY_INFO;
				mPrivateHandler.sendMessage(message);
				Message notifyMsg = mPrivateHandler.obtainMessage();
				notifyMsg.obj = "create wifi hotspot timeout";
				notifyMsg.what = MsgDefine.HANDLER_MSG_CREATE_HOTSPOT;
				notifyMsg.arg1 = 1;
				mPrivateHandler.sendMessage(notifyMsg);
			} else {
				message.obj = "wifi connect timeout";
				message.what = MsgDefine.HANDLER_MSG_CONNECT_WIFI;
				message.arg1 = 1;
				mPrivateHandler.sendMessage(message);
			}

		} else {
			mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
			
			message.obj = "wifi enable failed";
			message.what = MsgDefine.HANDLER_MSG_CONNECT_WIFI;
			message.arg1 = 2;
			mPrivateHandler.sendMessage(message);
			
		}
	}

	private void onHotspotCreated() {
		networkFinish(WifiStateConstant.WIFI_TYPE_HOTSPOT, 0, "hotspot opened");
		sendAPChangeMsg(WifiStateConstant.WIFI_TYPE_HOTSPOT);
	}
	
	
	private void sendAPChangeMsg(int wifiType) {
		Message msg = mUIHandler.obtainMessage(MsgDefine.MAIN_UI_HANDLER_HOTSPOTCREATE, wifiType, 0);
		mUIHandler.sendMessage(msg);
	}
	
	
	public void transWifiInfoIfNeed() {
		transWifiInfoIfNeed(false);
	}
	
	public void transWifiInfoIfNeed(boolean force) {
		if((force || mConnectedUser.size() <=0 )
				&& (mAudioPlay == null ||!mAudioPlay.isAlive())) {
			mAudioPlay = new ModulationAudioPlay();
			mAudioPlay.set(mPrivateHandler, getWifiInfoToTrans(), 5, 800);
			mAudioPlay.startPlay();
		}
	}

	public void pressTransWifiInfo(int times) {
		if(mAudioPlay == null ||!mAudioPlay.isAlive()) {
			mAudioPlay = new ModulationAudioPlay();
			mAudioPlay.set(mPrivateHandler, getWifiInfoToTrans(), times, 800);
			mAudioPlay.setPlayFinishListener(new ModulationAudioPlay.PlayFinishListener() {				
				@Override
				public boolean onPlayFinish() {
					LogToFile.e("PrivateShare","pressTransWifiInfo onPlayFinish");
					return !mContinuePlay;
				}
			});
			mAudioPlay.startPlay();
		}
	}
	
	private boolean mContinuePlay = false;
	
	public void setContinuePlay(boolean keepPlaying) {
		mContinuePlay = keepPlaying;
		if(keepPlaying) {
			LogToFile.e("PrivateShare","setContinuePlay start play");
		} else{
			LogToFile.e("PrivateShare","setContinuePlay finish play");
		}
	}
	
	
	public BroadcastReceiver mWifiMgrNotify = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if(action.equals(WifiShareManager.WIFIACTION_AP_CREATE_SUCCESS)) {
				onHotspotCreated();
			} else if(action.equals(WifiShareManager.WIFIACTION_AP_CREATE_ERROR)) {
				LogToFile.e(TAG, "creat hotspot fail");
				mWifiType = WifiStateConstant.WIFI_TYPE_ERROR;
				sendAPChangeMsg(WifiStateConstant.WIFI_TYPE_ERROR);
				showWifiApCorrupt();
			} else if(action.equals(WifiShareManager.WIFIACTION_AP_CREATE_TIMEOUT)) {
				LogToFile.e(TAG, "creat hotspot timeout");
				mWifiType = WifiStateConstant.WIFI_TYPE_ERROR;
				sendAPChangeMsg(WifiStateConstant.WIFI_TYPE_ERROR);
				showWifiApCorrupt();
			} else if(action.equals(WifiShareManager.WIFIACTION_AP_BREAK)) {
				LogToFile.e(TAG, "creat hotspot timeout");
				sendAPChangeMsg(WifiStateConstant.WIFI_TYPE_ERROR);
				mWifiType = WifiStateConstant.WIFI_TYPE_ERROR;
				showWifiApCorrupt();
			}

		}
	};
	
	
	private void showWifiApCorrupt() {
		AlertDialog.Builder fileDialog = new AlertDialog.Builder(mMainAct);
		fileDialog.setTitle(R.string.hint);
		fileDialog.setMessage(R.string.connect_invalid);
		fileDialog.setPositiveButton(R.string.retry, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
				createHotspot();
				sendAPChangeMsg(WifiStateConstant.WIFI_HOTSPOT_PENDING);
			}
		});
		fileDialog.setNegativeButton(R.string.exit_share,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
						mMainAct.finish();
					}
		});
		fileDialog.create().show();
	}


	/** Utils Functions **/
	
	public void sendFile(String userName, String fileName, String appName, int fileType) {
		if (mConnectedUser.size() < 1) {
			Toast.makeText(mMainAct, "need user login at first", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mNetProtocol.sendFile(userName, fileName, appName, fileType);
	}

	public void sendFile(String userName, String fileName, String appName,
			int fileType, int grantType, int grantValue, int grantReserve) {
		if (mConnectedUser.size() < 1) {
			Toast.makeText(mMainAct, "need user login at first", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mNetProtocol.sendFile(userName, fileName, appName, fileType, grantType,
				grantValue, grantReserve);
	}
	
	
	public void kickoutUser(String userName) {
		mNetProtocol.sendKickout(userName);
	}
	
	public void quitNetworkImp() {
		if (mNetProtocol != null) {
			mNetProtocol.cleanup();
			mNetProtocol = null;
		}

		clearWifiState();
		stopTransWifiInfo();

		stopRecvWifiInfo();

		mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
	}

	public void quitCurrentGroupImp() {

		if (mNetProtocol != null) {
			int isHost = (mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT ? 1 : 0);
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
		stopTransWifiInfo();
		stopRecvWifiInfo();

		mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
	}

	
	public void onQuit() {
		if (mNetProtocol != null) {
			int isHost = (mWifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT ? 1 : 0);
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
		stopTransWifiInfo();
		if(mWifiMgrNotify !=null) {
			mMainAct.unregisterReceiver(mWifiMgrNotify);
			mWifiMgrNotify = null;
		}
	}

}

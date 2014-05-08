package cn.nd.social.tobedelete;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.TabLauncherUI.InvokeOptionsMenu;
import cn.nd.social.TabLauncherUI.TitleEventCallBack;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardUtil.CardData;
import cn.nd.social.card.ReceiveCardHandler;
import cn.nd.social.common.ImageViewer;
import cn.nd.social.common.RecordAudioThread;
import cn.nd.social.data.MsgProviderSingleton;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.hotspot.NetworkCommunicateThread;
import cn.nd.social.hotspot.NetworkServerThread;
import cn.nd.social.hotspot.UserManagerSingleton;
import cn.nd.social.net.PrivateSwitcher;
import cn.nd.social.net.WifiNetProtocol;
import cn.nd.social.net.WifiShareManager;
import cn.nd.social.net.WifiStateConstant;
import cn.nd.social.privategallery.PrivateGalleryProvider;
import cn.nd.social.privategallery.PrivateItemEntity;
import cn.nd.social.privategallery.PrivateItemEntity.PrivateItemOrginalInfo;
import cn.nd.social.services.FileControlProvider;
import cn.nd.social.services.ISocialService;
import cn.nd.social.services.SocialService.ServiceBinder;
import cn.nd.social.syncbrowsing.ui.ClientSyncActivity;
import cn.nd.social.tresure.PrivateTreasure;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.CommonUtils;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.LogToFile;
import cn.nd.social.util.WifiInfoDataPacket;
import cn.nd.social.util.WifiInfoDataPacket.WifiInfoHolder;

import com.thirdparty.barcode.core.CaptureActivity;

public class TabMgr extends Fragment implements TitleEventCallBack,
		InvokeOptionsMenu {

	private final static String TAG = "TagMgr";

	private final static int REQ_VIEW_IMAGE = 10001;

	private TabLauncherUI mActivity;
	private View mRootView;

	//private ImageView mMgrTransIcon;
	private ImageView mConnectView;
	private ProgressBar mCircleAnim;
	private ImageView mTransPercent;
	private FrameLayout mRecvLayout;
	private TextView mTextCaption;
	private TextView mTextSubCaption;
	private ImageView mBinaryCodeIcon;


	private View mConnectLayout;
	private View mTransLayout;

	private PrivateSwitcher mPriSwitcher;

	public enum ActionState {
		NONE, RECEIVE_CARD, PRIVATE_SHARE, SYNC_READ,WAIT_CMD
	}

	private RecordAudioThread mSoundWaveRecorder;
	private WifiNetProtocol mNetProtocol;

	private int mListenState = 0;
	private ActionState mCurrentState = ActionState.NONE;
	
	private ArrayList<String> mConnectedUser = new ArrayList<String>();
	private int mTransactType = -1;

	private TextView mTitleText;
	private View mActionBtn;
	
	private int mWifiType = WifiStateConstant.WIFI_TYPE_NONE;

	public final static int[] PROGRESS_DRAWABLE = { R.drawable.percent_0,
			R.drawable.percent_10, R.drawable.percent_20,
			R.drawable.percent_30, R.drawable.percent_40,
			R.drawable.percent_50, R.drawable.percent_60,
			R.drawable.percent_70, R.drawable.percent_80,
			R.drawable.percent_90, R.drawable.percent_100 };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (TabLauncherUI) getActivity();
		mRootView = inflater.inflate(R.layout.main_tab_mgr, container, false);
		setupViews();
		registerEvent();

		return mRootView;
	}

	@Override
	public void onDestroyView() {
		mActivity.unregisterReceiver(mWifiEventNotify);
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void setupViews() {

		mConnectView = (ImageView) mRootView.findViewById(R.id.connect_btn);
		mCircleAnim = (ProgressBar) mRootView.findViewById(R.id.mgr_circle);
		//mMgrTransIcon = (ImageView) mRootView.findViewById(R.id.mgr_trans_icon);
		mTextSubCaption = (TextView) mRootView
				.findViewById(R.id.mgr_second_connect_hint);
		mTextCaption = (TextView) mRootView
				.findViewById(R.id.mgr_connect_hint);
		
		mConnectLayout = mRootView.findViewById(R.id.ll_connect);
		mTransLayout = mRootView.findViewById(R.id.trans_container);
		mTransPercent = (ImageView) mRootView
				.findViewById(R.id.trans_percentage);
		mRecvLayout = (FrameLayout) mRootView.findViewById(R.id.mgr_sound_recv);
		mBinaryCodeIcon = (ImageView) mRootView
				.findViewById(R.id.mgr_switch_to_qrcode);
		toggleTransState(false);

		mRecvLayout.setVisibility(View.INVISIBLE);

		// tangtaotao@ND_20140310 show seperated title
		mTitleText = (TextView) mRootView.findViewById(R.id.main_title);
		mActionBtn = mRootView.findViewById(R.id.right_btn);
		mTitleText.setText(R.string.title_share);

	}

	private void toggleTransState(boolean isTrans) {
		if (isTrans) {
			mConnectLayout.setVisibility(View.GONE);
			mTransLayout.setVisibility(View.VISIBLE);			
		} else {
			mConnectLayout.setVisibility(View.VISIBLE);
			mTransLayout.setVisibility(View.GONE);
		}
	}

	private boolean isPrivateSharing() {
		return (mCurrentState == ActionState.PRIVATE_SHARE) 
				|| (mCurrentState == ActionState.WAIT_CMD); 
	}
	
	private void registerEvent() {
		mConnectView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {				
				if (mListenState == 0) {
					onListenStart();
					startRecorder();
				} else {
					if (mCurrentState == ActionState.NONE) {
						onListenFinish();
						stopRecorder();
					} else if (isPrivateSharing() ) {
						showLogoutDialog();
					} else if (mCurrentState == ActionState.SYNC_READ) {
						onListenFinish();
						stopRecorder();
					}
					
				}
			}
		});

		IntentFilter filter = new IntentFilter(WifiShareManager.WIFIACTION_CONNECT_CONNECT_APNOTFIND);
		filter.addAction(WifiShareManager.WIFIACTION_WIFI_OPENING);
		filter.addAction(WifiShareManager.WIFIACTION_WIFI_OPENSUCESS);
		filter.addAction(WifiShareManager.WIFIACTION_WIFI_OPENTIMEOUT);
		filter.addAction(WifiShareManager.WIFIACTION_CONNECT_CONNECT_SUCCESS);
		filter.addAction(WifiShareManager.WIFIACTION_CONNECT_CONNECT_TIMEOUT);
		filter.addAction(WifiShareManager.WIFIACTION_CONNECT_CONNECT_ERROR);
		filter.addAction(WifiShareManager.WIFIACTION_CONNECT_CONNECT_BREAK);
		mActivity.registerReceiver(mWifiEventNotify,filter);

		mBinaryCodeIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mCurrentState == ActionState.NONE) {
					onListenFinish();
					stopRecorder();
				} else if (isPrivateSharing()) {
					stopNetwork();
				} else if (mCurrentState == ActionState.SYNC_READ) {
					onListenFinish();
					stopRecorder();
				}
				mRecvLayout.setVisibility(View.INVISIBLE);

				Intent intent = new Intent(mActivity, CaptureActivity.class);
				startActivityForResult(intent, 0);

			}
		});

		mActionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.showDefaultActionMenu((View) v.getParent());
			}
		});
	}

	private void showLogoutDialog() {
		new AlertDialog.Builder(mActivity)
		.setTitle(mActivity.getString(R.string.hint))
		.setMessage(R.string.quit_share_hint)
		.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						stopNetwork();
						stopRecorder();
					}
				})
		.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						dialog.dismiss();
					}
				}).show();
		
	}

	private void onListenStart() {
		if(mListenState == 1) {
			return;
		}
		mListenState = 1;		
		mConnectView.setImageResource(R.drawable.new_send_btn_strong);
		mRecvLayout.setVisibility(View.VISIBLE);
		mTextSubCaption.setVisibility(View.VISIBLE);
		mTextSubCaption.setText(R.string.connecting_dev_warning);
		//time out: 20 sec(no host initiate a transmission)
		mHandler.sendEmptyMessageDelayed(EVENT_LISTEN_TIME_OUT, 20000);
	}

	private void onListenFinish() {
		if(mListenState == 0) {
			return;
		}
		exitPrivateMode();

		mListenState = 0;
		mConnectView.setImageResource(R.drawable.new_send_btn);

		setActionState(ActionState.NONE);
		// tangtaotao@ND_20140226
		removeTimeoutHint();
		mRecvLayout.setVisibility(View.INVISIBLE);
		mTextCaption.setText(R.string.searching_friend);
		mTextSubCaption.setText(R.string.connecting_dev_warning);
		mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
		
	}

	// tangtaotao@ND_20140226
	private void onListenTimeOut() {
		stopRecorder();
		onListenFinish();
		new AlertDialog.Builder(mActivity)
				.setTitle(mActivity.getString(R.string.hint))
				.setMessage(R.string.not_friend_found)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {								
								dialog.dismiss();
								startRecorder();
								onListenStart();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						}).show();
	}

	// tangtaotao@ND_20140226
	private void removeTimeoutHint() {
		if (mHandler.hasMessages(EVENT_LISTEN_TIME_OUT)) {
			mHandler.removeMessages(EVENT_LISTEN_TIME_OUT);
		}
	}

	private final static int EVENT_SOUND_WAVE_MSG = 1000;
	private final static int EVENT_LISTEN_TIME_OUT = 1002;
	private final static int EVENT_DISMISS_DIALOG = 1003;
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SOUND_WAVE_MSG:
				handleSoundWaveMsg((String) msg.obj);
				break;

			case EVENT_LISTEN_TIME_OUT:
				onListenTimeOut();
				break;
			case EVENT_DISMISS_DIALOG:
				dismissProgressDialog();
				break;
			}
		}
	};
	
	
	private Animation mAnimRotate = null;
	private Animation getRotateAnim() {
		if(mAnimRotate == null) {
			mAnimRotate = AnimationUtils.loadAnimation(mActivity,
				R.anim.radar_rotate_anim);	
		}
		return mAnimRotate;
	}

	private boolean startRecorder() {
		
		mCircleAnim.setVisibility(View.VISIBLE);			
		mCircleAnim.startAnimation(getRotateAnim());
		WifiShareManager.getShareInstance(mActivity).openWifiAsync(-1);
		if(mSoundWaveRecorder == null || !mSoundWaveRecorder.isAlive()) {
			mSoundWaveRecorder = new RecordAudioThread(mHandler,
					EVENT_SOUND_WAVE_MSG);
			return mSoundWaveRecorder.initRecord();
		}
		
		return true;
	}

	private void stopRecorder() {
		if (mSoundWaveRecorder != null) {
			mSoundWaveRecorder.finiRecord();
/*			try {
				// tangtaotao@ND_20140227: TODO:add may cause unstable
				mSoundWaveRecorder.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			mSoundWaveRecorder = null;

			mCircleAnim.clearAnimation();
			mCircleAnim.setVisibility(View.INVISIBLE);
		}
	}

	private void enterPrivateMode() {
		if (mPriSwitcher == null) {
			mPriSwitcher = new PrivateSwitcher();
			mPriSwitcher.enterPrivateState();
		}
	}

	private void exitPrivateMode() {
		if (mPriSwitcher != null) {
			mPriSwitcher.exitPrivateState();
			mPriSwitcher = null;
		}
	}

	private void setActionState(ActionState state) {
		mCurrentState = state;
		// tangtaotao@ND_20140226
		if (state != ActionState.NONE) {
			removeTimeoutHint();
		}
	}

	private void onCardDataArrival(String rawData) {
		ReceiveCardHandler cardHandler = ReceiveCardHandler.getInstance();
		// cardHandler.onCardDataArrival(rawData, mActivity);
		CardData cardData = cardHandler.getCardData(rawData);
		if (cardData == null) {
			return;
		}

		Intent intent = CardUtil.getAddContactIntent(cardData);
		startActivity(intent);
		// startActivityForResult(intent, REQ_CODE_ADD_CONTACT);
		// temprary solution add contact activity will not return result
		// so we can't know the contact is select or not, just store the card
		CardUtil.storeCardFromNFC(mActivity, cardData);
	}

	private String mSSID;
	private String mPasswd;
	
	private void connectToHostspot(String ssid,String passwd) {
		mSSID = ssid;
		mPasswd = passwd;
		WifiShareManager.getShareInstance(mActivity).connectWifiAsync(ssid, passwd);
		mWifiType = WifiStateConstant.WIFI_CONNECT_PENDING;
		showProgressDialog(mActivity.getString(R.string.wifi_connecting));
	}
	
	private ProgressDialog mProgress;
	private void showProgressDialog(String message) {
		if(mProgress == null) {
			mProgress = new ProgressDialog(mActivity);
			mProgress.setMessage(message);
			mProgress.setIndeterminate(true);
			mProgress.setCancelable(false);
			mProgress.show();
			mHandler.sendEmptyMessageDelayed(EVENT_DISMISS_DIALOG,60000);
		}
	}

	private void dismissProgressDialog() {
		if (mProgress != null) {
			mHandler.removeMessages(EVENT_DISMISS_DIALOG);
			mProgress.dismiss();
			mProgress = null;
		}
	}
	
	/** sound wave handler */
	void handleSoundWaveMsg(String rawWaveData) {
		stopRecorder(); // TODO:change the strategy
		int type = AudioDataPacker.getType(rawWaveData);
		if (type == AudioDataPacker.TYPE_CARD_STRING) { // receive card
			setActionState(ActionState.RECEIVE_CARD);
			onCardDataArrival(rawWaveData);

			onListenFinish();
			setActionState(ActionState.NONE);
			
		} else if (type == AudioDataPacker.TYPE_WIFI_PRIVATE_SHARE
				|| type == AudioDataPacker.TYPE_WIFI_CARDS_SHARE) {// receive
																	// wifiAp
																	// name and
																	// password
			WifiInfoHolder wifiInfo = new WifiInfoHolder();
			WifiInfoDataPacket packet = new WifiInfoDataPacket(AudioDataPacker.TYPE_WIFI_PRIVATE_SHARE);
			packet.extractData(rawWaveData, wifiInfo);

			enterPrivateMode();

			connectToHostspot(wifiInfo.ssid,wifiInfo.passwd);
			
			setActionState(ActionState.PRIVATE_SHARE);

			AudioDataPacker.playModulationAck();// TODO: add this function to
												// service
			
		} else if (type == AudioDataPacker.TYPE_WIFI_SYNC_READ) {
			WifiInfoHolder wifiInfo = new WifiInfoHolder();
			WifiInfoDataPacket packet = new WifiInfoDataPacket(AudioDataPacker.TYPE_WIFI_SYNC_READ);
			packet.extractData(rawWaveData, wifiInfo);

			enterPrivateMode();

			connectToHostspot(wifiInfo.ssid,wifiInfo.passwd);
			
			setActionState(ActionState.SYNC_READ);

			AudioDataPacker.playModulationAck();// TODO: add this function to
												// service
		}
		mTransactType = type;
	}

	private Handler mProtocolHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (clientMsgHandler(msg)) {
				return;
			}

			if (fileTransMsgHandler(msg)) {
				return;
			}
		}
	};

	private boolean fileTransMsgHandler(Message msg) {
		boolean handled = true;
		switch (msg.what) {
		case MsgDefine.HANDLER_MSG_FILE_SEND_REQ: // sender
			break;

		case MsgDefine.HANDLER_MSG_FILE_SEND_FIN: {// sender
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;
		}
			break;

		case MsgDefine.NETWORK_MSG_FILE_SEND_REQ: { // receiver
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;
			onRecvFileStart(info.userName, info.fileName, info.fileType,
					info.grantType, info.grantValue, info.grantReserve);
		}
			break;

		case MsgDefine.NETWORK_MSG_FILE_SEND_FIN: {// receiver
			NetworkServerThread.FileInfo fileInfo = (NetworkServerThread.FileInfo) msg.obj;
			Long utcOfCurrent = System.currentTimeMillis();
			onRecvFileEnd(fileInfo.userName, fileInfo.appName,
					fileInfo.fileName, fileInfo.fileType, fileInfo.grantType,
					fileInfo.grantValue, fileInfo.grantReserve, utcOfCurrent);
		}
			break;

		default:
			handled = false;
			break;
		}
		return handled;
	}

	private boolean clientMsgHandler(Message msg) {
		boolean handled = true;
		switch (msg.what) {
		case MsgDefine.HANDLER_MSG_CONNECTED: {
			NetworkCommunicateThread thread = (NetworkCommunicateThread) msg.obj;			
			if (!thread.checkIsFileThread()) {
				showInfo("client send login");
				mNetProtocol.sendLogin();
				onConnectEstablish();
			}

		}
			break;

		case MsgDefine.NETWORK_MSG_LOGIN_ACK: {
			String userName = (String) msg.obj;
			showInfo("login ack");
			mConnectedUser.add(userName);
			
		}
			break;

		case MsgDefine.NETWORK_MSG_LOGOUT: {
			String userName = (String) msg.obj;
			clearNetwork();
			setActionState(ActionState.NONE);
		}
			break;

		case MsgDefine.NETWORK_BROADCAST_MSG_LOGIN: {
			// TODO: login broadcast
			String userName = (String) msg.obj;
			showInfo("broadcast login");
			mConnectedUser.add(userName);
		}
			break;

		case MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT: {
			String userName = (String) msg.obj;
			int hotspotFlag = msg.arg1;
			mConnectedUser.remove(userName);
			clearNetwork();
		}
			break;

		case MsgDefine.NETWORK_MSG_KICKOUT:
			int isKickoutSelf = msg.arg1;
			String kickoutName = (String) msg.obj;
			onBeKickedOut(kickoutName, isKickoutSelf);
			showInfo("user " + kickoutName + " is kickout");
			break;
		default:
			handled = false;
			break;
		}
		return handled;
	}

	private void onBeKickedOut(String name, int isKickoutSelf) {
		mConnectedUser.remove(name);

		if (isKickoutSelf == 1 || mConnectedUser.size() == 0) { 
			clearNetwork();
		}
	}

	private void showInfo(String info) {
		Toast.makeText(mActivity, info, Toast.LENGTH_SHORT).show();
	}

	/*
	 * BroadcastReceiver for wifi event
	 */
	private BroadcastReceiver mWifiEventNotify = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(WifiShareManager.WIFIACTION_WIFI_OPENING)) {
				
			} else if(action.equals(WifiShareManager.WIFIACTION_WIFI_OPENSUCESS)) {
				LogToFile.e(TAG,"wifi open success");
			} else if(action.equals(WifiShareManager.WIFIACTION_WIFI_OPENTIMEOUT)) {
				connectWifiError(WIFI_OPEN_ERROR);
				LogToFile.e(TAG,"wifi open timeout");
			} else if(action.equals(WifiShareManager.WIFIACTION_WIFI_OPENERROR)) {
				connectWifiError(WIFI_OPEN_ERROR);
				LogToFile.e(TAG,"wifi open error");
			} else if(action.equals(WifiShareManager.WIFIACTION_CONNECT_CONNECT_APNOTFIND)) {
				connectWifiError(WIFI_AP_NOTFIND_ERROR);
				LogToFile.e(TAG,"wifi connect ap not found");
			} 
			else if (action.equals(WifiShareManager.WIFIACTION_CONNECT_CONNECT_SUCCESS)) {
					onWifiConnected();
			} else if (action.equals(WifiShareManager.WIFIACTION_CONNECT_CONNECT_BREAK)) {
					clearNetwork();
					LogToFile.e(TAG,"wifi connect break");
			} else if (action.equals(WifiShareManager.WIFIACTION_CONNECT_CONNECT_ERROR)
					|| action.equals(WifiShareManager.WIFIACTION_CONNECT_CONNECT_TIMEOUT)) {
				connectWifiError(WIFI_CONNECT_ERROR);
				LogToFile.e(TAG,"wifi connect fail");
			}
		}
	};

	private final static int WIFI_CONNECT_ERROR = 1;
	private final static int WIFI_OPEN_ERROR = 2;
	private final static int WIFI_AP_NOTFIND_ERROR = 3;
	
	
	
	private void connectWifiError(int errorType) {
		setActionState(ActionState.NONE);
		onListenFinish();
		dismissProgressDialog();
		int resId = R.string.wifi_connect_fail;
		switch(errorType) {
		case WIFI_CONNECT_ERROR:
			resId = R.string.wifi_connect_fail;
			break;
		case WIFI_OPEN_ERROR:
			resId = R.string.wifi_open_fail;
			break;
		case WIFI_AP_NOTFIND_ERROR:
			resId = R.string.wifi_ap_notfind;
			break;
		default:
			resId = R.string.wifi_connect_fail;
			break;
		}
		showWifiConnectFail(resId);
	}
	
	private void showWifiConnectFail(int resId) {
		AlertDialog.Builder fileDialog = new AlertDialog.Builder(mActivity);
		fileDialog.setTitle(R.string.hint);
		fileDialog.setMessage(resId);
		fileDialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
				if(mWifiType != WifiStateConstant.WIFI_TYPE_CLIENT) {
					connectToHostspot(mSSID, mPasswd);
				}
			}
		});
		fileDialog.setNegativeButton(R.string.exit_share,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
		});
		fileDialog.create().show();
	}
	
	private void onWifiConnected() {

		mTextSubCaption.setText(R.string.waiting_sharing);
		
		mWifiType = WifiStateConstant.WIFI_TYPE_CLIENT;

		if (mCurrentState == ActionState.SYNC_READ) {
			dismissProgressDialog();
			onListenFinish();
			Intent intent = new Intent(mActivity, ClientSyncActivity.class);
			startActivity(intent);
			return;
		} else {
/*			onListenFinish();
			Intent intent = new Intent(mActivity, ReceiveFileActivity.class);
			startActivity(intent);*/
			if (mNetProtocol != null) {
				mNetProtocol.cleanup();
			}
			mNetProtocol = new WifiNetProtocol(mProtocolHandler, false);
			UserManagerSingleton singleton = UserManagerSingleton.getInstance();
			singleton.clear();
			singleton.addUserInfo(null, true);
		}
	}

	private void onConnectEstablish() {
		dismissProgressDialog();
		setActionState(ActionState.WAIT_CMD);
		mRecvLayout.setVisibility(View.VISIBLE);
		mTextCaption.setText(R.string.wait_for_share);
		mTextSubCaption.setVisibility(View.GONE);
	}

	private String mCurrentRecvUser = null;
	private String mCurrentRecvFile = null;

	private void onRecvFileStart(String userName, String fileName, int type,
			int grantType, int grantValue, int grantReserved) {
		showInfo("start receiving file :" + fileName);
		mCurrentRecvFile = fileName;
		mCurrentRecvUser = userName;

		toggleTransState(true);

		mTransPercent.setImageResource(R.drawable.percent_0);

		mHandler.postDelayed(mRecvCyclicTransProgressCheck, 300);
	}

	private void onRecvFileEnd(String userName, String shortFileName,
			String fileName, int fileType, int grantType, int grantValue,
			int grantReserved, Long utcForFileName) {
		showInfo("file :" + fileName + " has received");
		mTransPercent.setImageResource(PROGRESS_DRAWABLE[10]);
		mHandler.removeCallbacks(mRecvCyclicTransProgressCheck);

		// mRecvLayout.setVisibility(View.VISIBLE);
		mCurrentRecvFile = null;
		mCurrentRecvUser = null;
		mHandler.postDelayed(mRecvEndCheck, 1000);

		String path = FilePathHelper.getPrivateSharePath(fileType);
		if (mTransactType == AudioDataPacker.TYPE_WIFI_CARDS_SHARE) {

			final String pathName = path + "/" + fileName;

			byte[] bytes = DataFactory.getBytesFromFile(new File(pathName));
			String str = new String(bytes);
			String[] arr = str.split(CardUtil.CARD_RECORD_SEPERATOR);
			for (int i = 0; i < arr.length; i++) {
				ReceiveCardHandler.getInstance().slientStoreCard(arr[i]);
			}
		} else {
			if (fileType == MsgDefine.FILE_TYPE_IMAGE) {
				path = cn.nd.social.privategallery.Utils.getPrivateFilePath();
			}
			
			
			final int status;
			if (grantValue != -1) {
				status = MsgDefine.STATUS_NEED_DELETE;
			} else {
				status = MsgDefine.STATUS_DO_NOTHING;				
			}
			MsgProviderSingleton.getInstance().addRecord(-1, 
					userName,
					"receive file:" + fileName, 
					path, 
					fileName,
					String.valueOf(utcForFileName / 1000), 
					"recv", 
					fileType,
					grantValue, 
					status);

			onRecvOneFileFinish(userName, shortFileName, fileName, fileType,
					grantType, grantValue, grantReserved, utcForFileName);
		}

	}

	// check the file transfering progress
	Runnable mRecvEndCheck = new Runnable() {
		@Override
		public void run() {
			if (mCurrentRecvFile == null) {
				toggleTransState(false);
			}
		}
	};

	// check the file transfering progress
	Runnable mRecvCyclicTransProgressCheck = new Runnable() {
		@Override
		public void run() {
			if (mCurrentRecvFile != null) {
				int progress = mNetProtocol.queryFileRecvProgress(
						mCurrentRecvUser, mCurrentRecvFile);

				Log.d("recvProgress", "recving progress:" + progress);

				mTransPercent
						.setImageResource(PROGRESS_DRAWABLE[progress / 10]);
				mHandler.postDelayed(this, 300);
			} else {
				mHandler.removeCallbacks(this);
			}
		}
	};

	private void stopNetwork() {
		if (mNetProtocol != null) {
			mNetProtocol.sendLogout(0);
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Log.e("TagMgr", "get InterruptedException");
		}
		clearNetwork();

	}

	private void clearNetwork() {
		mConnectedUser.clear();
		
		if (mNetProtocol != null) {
			mNetProtocol.cleanup();
			mNetProtocol = null;
		}
		if(mWifiType == WifiStateConstant.WIFI_TYPE_CLIENT) {
			WifiShareManager.getShareInstance(mActivity).disconnectWifi();			
		}
		mWifiType = WifiStateConstant.WIFI_TYPE_NONE;
		onListenFinish();
	}

	private void installApk(final String fileName,final String appName) {
		String path = FilePathHelper.getPrivateSharePath(MsgDefine.FILE_TYPE_APP);
		final String pathName = path + File.separator + fileName;
		new Thread() {
			public void run() {
				final boolean result = CommonUtils.installApkRooted(
												new File(pathName), mActivity);
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String text;
						if (result) {
							text = appName
									+ getString(R.string.app_installed);
							Toast.makeText(mActivity, text,
									Toast.LENGTH_SHORT).show();
						} else {
							showNoramlInstallApk(pathName);
						}
					}
				});
			}
		}.start();
	}
	
	private void showNoramlInstallApk(final String apkPath) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.hint);
		builder.setMessage(R.string.unroot_device_manual_install_apk);
		builder.setPositiveButton(R.string.confirm_install, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				CommonUtils.installApkNormal(apkPath);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();								
			}
		});
	}
	
	
	private void onRecvOneFileFinish(String recvUser, final String appName,
			String fileName, int fileType, int grantType, int grantValue,
			int grantReserve, long utcForFileName) {

		switch (fileType) {
		case MsgDefine.FILE_TYPE_APP:
			if (grantType == MsgDefine.GRANT_APK_SILENT_INSTALL) {
				installApk(fileName,appName);			
			}
			
			break;

		case MsgDefine.FILE_TYPE_IMAGE: {
			Log.d(TAG, "file type : image");

			String path = FilePathHelper.getPrivateSharePath(fileType);
			path = path + File.separator + fileName;
			PrivateItemOrginalInfo info = new PrivateItemOrginalInfo();
			info.orgPath = path;
			info.filename = fileName;
			info.controlType = grantType == MsgDefine.GRANT_FILE_AUTO_DESTROY ? 1
					: 0;

			PrivateGalleryProvider provider = PrivateGalleryProvider
					.getInstance();
			PrivateItemEntity entity = PrivateItemEntity.from(info,
					utcForFileName);

			if (PrivateItemEntity.addFileToPrivateGallery(provider, entity)) {
				if (grantType == MsgDefine.GRANT_FILE_AUTO_DESTROY) {

					if (mIsViewImage) { // temporary solution
						addFileControl(entity.createUtc, entity.path,
								grantValue, grantValue);
						return;
					} else {
						addFileControl(entity.createUtc, entity.path,
								grantValue, 0);
					}

				} else if (mIsViewImage) {
					return;
				}
				// viewImage(grantType,grantValue,entity.path);
			} else {
				Log.e(TAG, "addFileToPrivateGallery failed");
			}
			break;
		}

		default:
			break;
		}
		
		PrivateTreasure.getToTreasure(mActivity, fileType);

	}

	private void viewImage(int grantType, int grantValue, String path) {
		Intent intent = new Intent(mActivity, ImageViewer.class);

		intent.putExtra("type", grantType);
		intent.putExtra("value", grantValue);

		intent.putExtra("filename", path);

		startActivityForResult(intent, REQ_VIEW_IMAGE);
		mIsViewImage = true;
	}

	private void addFileControl(long startTime, String path, int validTime,
			int storeTime) {
		ServiceBinder binder = SocialApplication.getAppInstance()
				.getServiceBinder();
		ISocialService.FileControlPara para = new ISocialService.FileControlPara();
		para.startTime = startTime / 1000;
		para.fileName = path;
		para.expireTime = validTime;
		if (storeTime == 0) {
			para.staticTime = FileControlProvider.DEFAULT_STORE_TIME;
		} else {
			para.staticTime = storeTime;
		}
		binder.AddFileControl(para);
	}

	private boolean mIsViewImage = false;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_VIEW_IMAGE) {
			mIsViewImage = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onRightBtnEvent(View v) {
		
	}



	@Override
	public boolean onBackPressEvent() {
		stopRecorder();
		if (mCurrentState == ActionState.NONE) {
			onListenFinish();
		} else if (isPrivateSharing()) {
			stopNetwork();
		} else if (mCurrentState == ActionState.SYNC_READ) {
			onListenFinish();
			stopRecorder();
		}
		return false;
	}
	


	@Override
	public void onInvokeOptionsMenu(Menu menu, boolean isCurrent) {
		mActivity.showDefaultActionMenu(null);
	}

	@Override
	public boolean onItemSelected(MenuItem item) {
		return false;
	}
	
	public void onFoucsChange(boolean hasFocus) {
		if(!hasFocus) {
			//stopRecorder();
			//onListenFinish();
		}
	}

}

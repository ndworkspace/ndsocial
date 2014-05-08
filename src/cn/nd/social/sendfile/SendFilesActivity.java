package cn.nd.social.sendfile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.common.ResourceConstant;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.hotspot.NetworkServerThread;
import cn.nd.social.hotspot.UserManagerSingleton;
import cn.nd.social.net.ProtocolHandler;
import cn.nd.social.net.ProtocolHandler.DefaultDisposal;
import cn.nd.social.net.WifiController;
import cn.nd.social.net.WifiController.WifiConnectState;
import cn.nd.social.net.WifiNetProtocol;
import cn.nd.social.prishare.component.SetTimeActivity;
import cn.nd.social.prishare.items.CellItemBase;
import cn.nd.social.privategallery.ImageThumbnailViewer;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.WifiInfoDataPacket;

import com.example.ofdmtransport.Modulation;
import com.example.ofdmtransport.ModulationAudioPlay;

public class SendFilesActivity extends Activity {
	private ModulationAudioPlay mAudioPlay;
	private WifiNetProtocol mNetProtocol;
	private ProtocolHandler mProtocolHandler = new ProtocolHandler();
	private String mFileName;
	private Context mContext;
	private ProgressDialog progDialog;
	private String mCurrentSendUser = null;
	private String mCurrentSendFile = null;
	private ImageView mTransPercent;
	private int mDataPacketType;

	private int mSendSource = 0;
	private int mExpirTime = 0;
	private CellItemBase[] mItemArr;
	private int mItemInx = 0;
	private final static String TAG = "SendFilesActivity";

	public final static String KEY_SEND_FILENAME = "file_name";
	public final static String KEY_DATA_PACKET_TYPE = "data_packet_type";
	public final static String SEND_SOURCE = "send_source";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sendfiles);

		Intent intent = getIntent();
		mFileName = intent.getStringExtra(KEY_SEND_FILENAME);
		mDataPacketType = intent.getIntExtra(KEY_DATA_PACKET_TYPE,
				AudioDataPacker.TYPE_WIFI_PRIVATE_SHARE);
		mSendSource = intent.getIntExtra(SEND_SOURCE, 0);
		mExpirTime = intent.getIntExtra(SetTimeActivity.EXPIRE_TIME, 0);

		mContext = this;

		mContext.registerReceiver(mAPEventNotify, new IntentFilter(
				WifiController.ACTION_HOTSPOT_EVENT_NOTIFY));

		WifiController.getInstance().createHotspot();
		showSendWaiting();

		setupViews();

	}

	@Override
	protected void onDestroy() {
		mContext.unregisterReceiver(mAPEventNotify);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		onQuit();
		if (mSendSource == 1) {
			finish();
		} else {
			super.onBackPressed();
		}

	}

	private void setupViews() {
		mTransPercent = (ImageView) findViewById(R.id.trans_percent);
	}

	private void showSendWaiting() {
		progDialog = new ProgressDialog(mContext);
		progDialog.setTitle(R.string.hint);
		progDialog.setMessage(getString(R.string.wait_conn));
		progDialog.show();
	}

	private void hideSendWaiting() {
		if (progDialog != null) {
			progDialog.dismiss();
			progDialog = null;
		}
	}

	private void onHotspotCreated(String info) {
		if (mNetProtocol != null) {
			mNetProtocol.cleanup();
			mNetProtocol = null;
		}
		mNetProtocol = new WifiNetProtocol(mProtocolHandler, true);
		mProtocolHandler.setDisposer(new ProtocolMsgDisposal());
		UserManagerSingleton singleton = UserManagerSingleton.getInstance();
		singleton.clear();
		singleton.addUserInfo(null, true);
		String[] wifiInfo = info.split(" ");
		WifiInfoDataPacket packer = new WifiInfoDataPacket(mDataPacketType);
		String content = packer
				.packAudioData(new WifiInfoDataPacket.WifiInfoHolder(
						wifiInfo[0], wifiInfo[1]));

		mAudioPlay = new ModulationAudioPlay();// always create a new
												// player(thread)

		mAudioPlay.set(mEventHandler, content, 30, 800);
		mAudioPlay.startPlay();
	}

	private void onHotspotFailed() {
		Toast.makeText(mContext, "hotspot created failed", Toast.LENGTH_SHORT)
				.show();
		finish();
	}

	private Handler mEventHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Modulation.MODULATION_HANDLER_RECV_NOTIFY_ACK:
				if (mAudioPlay != null) {
					mAudioPlay.stopPlay();
					mAudioPlay = null;
				}
				break;
			default:
				break;
			}
		}
	};

	/*
	 * BroadcastReceiver for AccessPoint event
	 */
	private BroadcastReceiver mAPEventNotify = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(WifiController.ACTION_HOTSPOT_EVENT_NOTIFY)) {
				int wifiState = intent.getIntExtra(
						WifiController.KEY_CONNECT_STATE,
						WifiConnectState.NONE.ordinal());
				if (wifiState == WifiConnectState.WIFI_AP.ordinal()) {
					String info = intent
							.getStringExtra(WifiController.KEY_EXTRA_INFO);
					onHotspotCreated(info);
				} else if (wifiState == WifiConnectState.CREATE_AP_FAIL
						.ordinal()) {
					onHotspotFailed();

				} else {
					Log.e(TAG, "error hotspot state:" + wifiState);
				}
			}
		}
	};

	public class ProtocolMsgDisposal extends DefaultDisposal {
		public String mUser;

		@Override
		public void onUserLogin(Message msg) {
			String userName = (String) msg.obj;
			mUser = userName;
			hideSendWaiting();

			if (mSendSource == 0) {
				mNetProtocol.sendFile(mUser, mFileName, "normal", 0);
			} else {
				mItemArr = ImageThumbnailViewer.sThumbnailViewer.getmItemArr();

				String filePath = mItemArr[mItemInx].getItemPath();
				String appName = mItemArr[mItemInx].getFileShortName();
				if (mExpirTime == 0) {
					mNetProtocol.sendFile(userName, filePath, appName,
							MsgDefine.FILE_TYPE_IMAGE);
				} else {
					mNetProtocol.sendFile(userName, filePath, appName,
							MsgDefine.FILE_TYPE_IMAGE,
							MsgDefine.GRANT_FILE_AUTO_DESTROY, mExpirTime, 0);
				}
				mItemInx++;
			}

		}

		@Override
		public void onUserLoginAck(Message msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onNewConnect(Message msg) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onUserLogout(Message msg) {
			clearNetwork();
		}


		@Override
		public void onKickedOut(Message msg) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSendFileEnd(Message msg) {
			
			if (mSendSource == 1) {
				if (mItemArr.length == mItemInx){
					onSendFinish();
					return;
				}				
				String filePath = mItemArr[mItemInx].getItemPath();
				String appName = mItemArr[mItemInx].getFileShortName();
				if (mExpirTime == 0) {
					mNetProtocol.sendFile(mUser, filePath, appName,
							MsgDefine.FILE_TYPE_IMAGE);
				} else {
					mNetProtocol.sendFile(mUser, filePath, appName,
							MsgDefine.FILE_TYPE_IMAGE,
							MsgDefine.GRANT_FILE_AUTO_DESTROY, mExpirTime, 0);
				}
				mItemInx++;
			}else{
				onSendFinish();
			}
		}

		@Override
		public void onSendFileStart(Message msg) {
			NetworkServerThread.FileInfo info = (NetworkServerThread.FileInfo) msg.obj;
			onSendStart(info.userName, info.appName, info.fileName,
					info.fileSize, info.fileType, info.grantType,
					info.grantValue, info.grantReserve);// update ui for send
		}

		@Override
		public void onUserLogoutBroadCastMsg(Message msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onHandleerNewConnect(Message msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onNetworkMsgLogin(Message msg) {
			// TODO Auto-generated method stub
			onUserLogin(msg);
		}
	}

	private void quitConnect() {
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
		if (mNetProtocol != null) {
			mNetProtocol.cleanup();
			mNetProtocol = null;
		}
		WifiController.getInstance().closeHotspot();
	}

	public void sendFile(String userName, String fileName, String appName,
			int fileType, int grantType, int grantValue, int grantReserve) {
		mNetProtocol.sendFile(userName, fileName, appName, fileType, grantType,
				grantValue, grantReserve);
	}

	private void onSendStart(String sendUser, String appName, String fileName,
			long fileSize, int fileType, int grantType, int grantValue,
			int grantReserve) {
		mCurrentSendFile = fileName;
		mCurrentSendUser = sendUser;
		mTransPercent.setImageResource(ResourceConstant.PROGRESS_DRAWABLE[0]);
		mEventHandler.postDelayed(mCyclicTransProgressCheck, 100);
	}

	private void onSendFinish() {
		mTransPercent
				.setImageResource(ResourceConstant.PROGRESS_DRAWABLE[ResourceConstant.PROGRESS_DRAWABLE.length - 1]);
		mEventHandler.removeCallbacks(mCyclicTransProgressCheck);
		quitConnect();
		mEventHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, 500);
		
		
	}

	// check the file transfering progress
	private Runnable mCyclicTransProgressCheck = new Runnable() {
		@Override
		public void run() {
			if (mCurrentSendFile != null) {
				int progress = mNetProtocol.queryFileSendProgress(
						mCurrentSendUser, mCurrentSendFile);

				Log.d("recvProgress", "sending progress:" + progress);

				mTransPercent.setImageResource(ResourceConstant.PROGRESS_DRAWABLE[progress/10]);
				mEventHandler.postDelayed(this, 100);
			} else {
				mEventHandler.removeCallbacks(this);
			}
		}
	};

	private void onQuit() {
		clearNetwork();
		if (mAudioPlay != null) {
			mAudioPlay.stopPlay();
			mAudioPlay = null;
		}
	}

}

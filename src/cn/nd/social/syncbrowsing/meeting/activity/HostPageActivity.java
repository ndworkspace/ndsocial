package cn.nd.social.syncbrowsing.meeting.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.syncbrowsing.manager.IHostSyncEventListener;
import cn.nd.social.syncbrowsing.manager.TestSyncHostNetImpl;
import cn.nd.social.syncbrowsing.ui.HostSyncReadView;
import cn.nd.social.syncbrowsing.ui.SyncConstant;
import cn.nd.social.syncbrowsing.utils.DialogUtil;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;

import com.nd.voice.meetingroom.manager.MeetingDetailEntity;

public class HostPageActivity extends Activity {
	private final static String TAG = "HostPageActivity";
	
	public static final String FILE_ID_KEY = "file_path";

	private View mHostReadPage;

	private Context mContext;	
	

	private TestSyncHostNetImpl mNetProtocol;
	private HostSyncReadView mHostController;

	private String mPath;
	
	private int mHostState = SyncConstant.STATE_NOT_READY;
	

	private String mMeetingId = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	

		earlyInit();
		setContentView(R.layout.sync_host_page);
		setupViews();
		showDoc();
				
	}	
	
	private void earlyInit() {
		mContext = this;
		
		Intent intent = getIntent();
		mPath = intent.getStringExtra(FILE_ID_KEY);
		String meetingId = intent.getStringExtra("meetingid");
		mMeetingId = MeetingDetailEntity.getMeetingIdByUid(meetingId);
		mNetProtocol = new TestSyncHostNetImpl(mMeetingId);
	}
	
	private Handler mHandler = new Handler();

	private void setupViews() {

		mHostReadPage = findViewById(R.id.host_read_page);	
		
		mHostReadPage.findViewById(R.id.btn_view_back).setVisibility(View.GONE);
		mHostReadPage.findViewById(R.id.btn_close).setVisibility(View.GONE);
	}
	
	private void showDoc() {

		mHostState = SyncConstant.STATE_VIEW;
		
		mNetProtocol.syncEnter(mMeetingId);
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mHostController = new HostSyncReadView(mDocEventListener);
				mHostController.init(mHostReadPage, mPath);
				
			}
		}, 2000);
	}
	
	
	@Override
	protected void onDestroy() {
		
		quitNetwork();
		
		if(mHostController != null) {
			mHostController.fini();
			mHostController = null;
		}
		
		super.onDestroy();
	}
	
	
	private void quitNetwork() {		
		if (mNetProtocol != null) {	
			mNetProtocol.syncExit();
			mNetProtocol = null;
		}
	}
	

	
	private void onSyncReady() {		
		mNetProtocol.syncHandShake("", mHostState,
				mHostController.getDocViewWidth(),
				mHostController.getDocViewHeight(),
				mHostController.getPageCount(),
				mHostController.getCurrPage());
	}
	
	
	private void showInfo(String info) {
		Toast.makeText(mContext, info, Toast.LENGTH_LONG).show();
	}
	
	
	
	public static String getFilePath(String name) {
		return (FilePathHelper.getSyncPath() + File.separator + name);
	}

	@Override
	public void onBackPressed() {		
		DialogUtil.showExitDialog(this);
	}
	
/*	private IHostNetMsgReceiver mNetMsgReceiver = new IHostNetMsgReceiver() {
		
		@Override
		public void onRecvPageRequest(String username, int page, int pageVer) {
			mHostController.onPageRequest(username, page);
		}
		
		@Override
		public void onRecvHandshake(String username, int ver) {
			onHandShake(username, ver);	
		}
	};*/
	
	private IHostSyncEventListener mDocEventListener = new IHostSyncEventListener() {

		@Override
		public void onExitSync() {
			onBackPressed();			
		}

		@Override
		public void reselectFile() {
		
		}

		@Override
		public void showConnectedUserList() {
					
		}
		
		@Override
		public void notifyPageFirst(int page) {
			//mNetProtocol.sycnSendPageBroadcast(page, 0);
		}
		
		@Override
		public void notifyPage(int page,byte[]image) {
			mNetProtocol.syncNotifyPage(page,image, 0);
		}
		
		@Override
		public void notifyAction(int pageNum,byte[] action) {
			mNetProtocol.syncSendAction(pageNum, 0, action);
		}
		
		//not using this currently
		@Override
		public void sendPage(int state,String peerUserName, byte[] image,int pageNum) {
			String pathName = "";
			pathName = getFilePath("" + System.currentTimeMillis() + "_" + pageNum);
			DataFactory.getFileFromBytes(image, pathName);
			mNetProtocol.syncSendPage(state,peerUserName, pathName, pageNum);
		}

		@Override
		public void syncReady() {
			onSyncReady();			
		}
		
	};

}

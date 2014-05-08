package cn.nd.social.syncbrowsing.meeting.activity;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.syncbrowsing.manager.IHostSyncEventListener;
import cn.nd.social.syncbrowsing.manager.TestSyncHostNetImpl;
import cn.nd.social.syncbrowsing.ui.HostSyncReadView;
import cn.nd.social.syncbrowsing.ui.SyncConstant;
import cn.nd.social.syncbrowsing.utils.DialogUtil;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;


public class HostPageFrameLayout extends FrameLayout {
	

	private final static String TAG = "HostPageActivity";
	
	public static final String FILE_ID_KEY = "file_path";

	private View mHostReadPage;
	private Button btn_close;
	private Button btn_up_down;
	
	private Context mContext;	
	private View rootView;
	

	private TestSyncHostNetImpl mNetProtocol;
	private HostSyncReadView mHostController;

	private String mPath;
	
	private int mHostState = SyncConstant.STATE_NOT_READY;
	
	private String mMeetingId = "";
	
	private WeakReference<HostLayoutDelegate> mdelegate;
	
	private boolean mDownFlag;
	

	public void setDelegate(HostLayoutDelegate delegate) {
		this.mdelegate = new WeakReference(delegate);
	}

	public HostPageFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.sync_host_page, this);
		setupViews();
	}


	public HostPageFrameLayout(Context context) {
		super(context);
		mContext = context;
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.sync_host_page, this);
		setupViews();
	}


	public HostPageFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.sync_host_page, this);
		setupViews();
	}
	
	public String getmPath() {
		return mPath;
	}


	public void setmPath(String mPath) {
		this.mPath = mPath;
	}


	public String getmMeetingId() {
		return mMeetingId;
	}


	public void setmMeetingId(String mMeetingId) {
		mNetProtocol = new TestSyncHostNetImpl(mMeetingId);
		this.mMeetingId = mMeetingId;
	}

	private Handler mHandler = new Handler();
	
	
	public void exitHint() {
		new AlertDialog.Builder(mContext).setMessage("是否确定要退出?").setNegativeButton("取消", null).setPositiveButton("确定", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				release();
				mdelegate.get().onCloseAction();
			}
		}).create().show();
	}

	private void setupViews() {
		mHostReadPage = rootView.findViewById(R.id.host_read_page);	
		btn_close = (Button) mHostReadPage.findViewById(R.id.btn_close);
		btn_up_down = (Button) mHostReadPage.findViewById(R.id.btn_up_down);
		btn_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				exitHint();
			}
		});
		
		btn_up_down.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mdelegate.get().onDownAction();
//				mDownFlag = !mDownFlag;
//				if(mDownFlag){
//					btn_up_down.setBackgroundResource(R.drawable.button_up);
//					mdelegate.get().onDownAction();
//				}else{
//					btn_up_down.setBackgroundResource(R.drawable.button_down);
//					mdelegate.get().onUpAction();
//				}
			}
		});
		
		
	}
	
	
	public void showDoc() {
		
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
	
	private void release() {
		// TODO Auto-generated method stub
		quitNetwork();
		if(mHostController != null) {
			mHostController.fini();
			mHostController = null;
		}
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

	
	public void onBackPressed() {		
		DialogUtil.showExitDialog((Activity)mContext);
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

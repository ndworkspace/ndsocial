package cn.nd.social.syncbrowsing.meeting.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.business.BusinessEventRsp;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.syncbrowsing.manager.IClientNetMsgReceiver;
import cn.nd.social.syncbrowsing.manager.IClientSyncEventListener;
import cn.nd.social.syncbrowsing.manager.TestSyncClientNetImpl;
import cn.nd.social.syncbrowsing.ui.ClientSyncReadView;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;

public class ClientPageFrameLayout extends FrameLayout {
	
	public static final String TAG = "ClientSyncActivity";
	
	private Context mContext;
	
	private View mClientReadPage;
	
	private TestSyncClientNetImpl mNetProtocol;
	
	private ClientSyncReadView mClientController;
	
	private View rootView;
	private Handler mHandler = new Handler();
	View mBtnUpDown;
	
	private ClientLayoutDelegate mDelegate;
	
	private boolean mDownFlag;
	
	public ClientPageFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		earlyInit();
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.sync_client_page, this);
		setupViews();
	}


	public ClientPageFrameLayout(Context context) {
		super(context);
		mContext = context;
		earlyInit();
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.sync_client_page, this);
		setupViews();
	}
	
	private void earlyInit() {
		mNetProtocol = new TestSyncClientNetImpl();
		BusinessEventRsp.getInstance().setSyncReadReceiver(mNetMsgReceiver);
	}
	
	
	public void setDelegate(ClientLayoutDelegate delegate) {
		mDelegate = delegate;
	}
	
	public void laterInit(boolean hasExtra,Bundle extras) {
		if(hasExtra) {
			int width = extras.getInt("width");
			int height = extras.getInt("height");
			final int page = extras.getInt("page");
			int pageCount = extras.getInt("pagecount");
			final byte []pageData = extras.getByteArray("pagedata");

			onHandShake(0, pageCount, page, width, height);
			mHandler.postDelayed(new Runnable() {
				
				@Override 
				public void run() {
					if(mClientController == null || !mClientController.isDocViewReady()) {
						mHandler.postDelayed(this, 100);
					} else {
						clientNotifyPage(page,pageData,0);
						mHandler.removeCallbacks(this);
					}
				}
			},100);
		}
	}
	
	private void setupViews() {		
		mClientReadPage = findViewById(R.id.client_read_page);
		mClientReadPage.findViewById(R.id.iv_back).setVisibility(View.GONE);
		mBtnUpDown = mClientReadPage.findViewById(R.id.btn_up_down);
		
		mBtnUpDown.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDownFlag = !mDownFlag;
				if(mDownFlag){
					mBtnUpDown.setBackgroundResource(R.drawable.button_up);
					mDelegate.onDownAction();
				}else{
					mBtnUpDown.setBackgroundResource(R.drawable.button_down);
					mDelegate.onUpAction();
				}	
			}
		});
	}
	
	private IClientNetMsgReceiver mNetMsgReceiver = new IClientNetMsgReceiver() {
		

		@Override
		public void onRecvNotifyPage(int page, byte[]pageData,int pageVer) {
			clientNotifyPage(page,pageData,pageVer);		
		}
		
		@Override
		public void onRecvHandshake(int state, int pageCount, int curPage,
				int hostWidth, int hostHeight) {
			onHandShake(state, pageCount, curPage, hostWidth, hostHeight);			
		}
		
		@Override
		public void onRecvExitSync() {
			mDelegate.onCloseAction();			
		}
		
		@Override
		public void onRecvAction(int page, int pageVer, byte[] action) {
			clientNotifyAciton(page,pageVer,action);			
		}
		
		@Override
		public void onRecvEnterSync() {
					
		}
		
		//not using currently
		@Override
		public void onRecvReselectFile() {
						
		}
		
		//not using currently
		@Override
		public void onRecvPageRequestAck(int state, int pageNum, int pageVer,
				String pageData) {
			//clientOnRequestPageAck(state,pageNum,pageVer,pageData);
		}
		
	};
	

	
	
	private void onHandShake(int state,int pageCount,int curPage,int width,int height) {
		mClientController = new ClientSyncReadView(mClientEventListener);
		mClientController.init(mClientReadPage, pageCount, width, height);
	}
	
	
	
	private IClientSyncEventListener mClientEventListener = new IClientSyncEventListener() {		
		@Override
		public void onExitSync() {
		}

		@Override
		public void requestPage(int page, int pageVer) {
			mNetProtocol.syncRequestPage(page, pageVer);			
		}
	};


	

	public void release() {		
		if(mClientController != null) {
			mClientController.fini();
			mClientController = null;
		}
		BusinessEventRsp.getInstance().setSyncReadReceiver(null);
	}
	

	/*******************************************************************/
	/*********API for ClientSyncRead start*************/
	/*******************************************************************/

	public void clientRequestPage(int page) {
		mNetProtocol.syncRequestPage(page, 0);
	}
	

	
	private void clientNotifyPage(int page, byte[] pageData,int pageVer) {
		if(mClientController.isDocViewReady()) {
			mClientController.notifyPage(page);
			if(pageData != null) {
				mClientController.onPageRequestAck(pageData,page);
			}
		}
		
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

}

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
import cn.nd.social.account.business.BusinessEventRsp;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.syncbrowsing.manager.IClientNetMsgReceiver;
import cn.nd.social.syncbrowsing.manager.IClientSyncEventListener;
import cn.nd.social.syncbrowsing.manager.TestSyncClientNetImpl;
import cn.nd.social.syncbrowsing.ui.ClientSyncReadView;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;

public class ClientPageActivity extends Activity {
	
	public static final String TAG = "ClientSyncActivity";
	
	private Context mContext;
	
	private View mClientReadPage;
	
	private TestSyncClientNetImpl mNetProtocol;
	
	private ClientSyncReadView mClientController;
	
	private Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		earlyInit();
		
		setContentView(R.layout.sync_client_page);
		
		setupViews();
		
		laterInit();
	}
	
	private void earlyInit() {
		mContext = this;
		mNetProtocol = new TestSyncClientNetImpl();
		BusinessEventRsp.getInstance().setSyncReadReceiver(mNetMsgReceiver);
	}
	
	
	private void laterInit() {
		Intent intent = getIntent();
		boolean hasExtra = intent.getBooleanExtra("hasextra", false);
		if(hasExtra) {
			Bundle extras = intent.getExtras();
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
			finish();			
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
			onBackPressed();
		}

		@Override
		public void requestPage(int page, int pageVer) {
			mNetProtocol.syncRequestPage(page, pageVer);			
		}
	};


	
	@Override
	protected void onDestroy() {
		
		if(mClientController != null) {
			mClientController.fini();
			mClientController = null;
		}
		BusinessEventRsp.getInstance().setSyncReadReceiver(null);
		super.onDestroy();
	}
	
	
	private void showInfo(String info) {
		Toast.makeText(mContext, info, Toast.LENGTH_LONG).show();
	}
	
	/*******************************************************************/
	/*********API for ClientSyncRead start*************/
	/*******************************************************************/

	public void clientRequestPage(int page) {
		mNetProtocol.syncRequestPage(page, 0);
	}
	
	
	private void clientOnRequestPageAck(int state,int pageNum,int pageVer,String pageInfo) {
		if(state == 0) {
			String pathName = getRecvFilePath(pageInfo);
			byte [] image = DataFactory.getBytesFromFile(new File(pathName));
			mClientController.onPageRequestAck(image,pageNum);
		} else {
			//TODO: request page later
			showInfo("request page failed");
		}
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

	@Override
	public void onBackPressed() {
		//DialogUtil.showExitDialog(this);
		//super.onBackPressed();
	}
}

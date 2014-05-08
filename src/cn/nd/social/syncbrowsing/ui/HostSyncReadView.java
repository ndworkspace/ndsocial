package cn.nd.social.syncbrowsing.ui;


import cn.nd.social.R;
import cn.nd.social.syncbrowsing.Document;
import cn.nd.social.syncbrowsing.manager.IHostSyncEventListener;
import cn.nd.social.syncbrowsing.ui.HostDocView.OpMode;
import cn.nd.social.syncbrowsing.ui.SyncAction.DrawState;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.NDConfig;
import cn.nd.social.util.Utils;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class HostSyncReadView  {	
	HostDocView mDocView;
	View mNext,mPrev;
	ImageView mSwitchBtn;
	TextView mPageText;
	Document mDoc;
	TextView mDocName;
	View mExit;
	boolean mLocalSource;
	View mReselectFile;
	View mViewConnectUser;
	private final static int CREATE_VIEW = 1000;
	private final static int UPDATE_VIEW = 1001;
	private final static int REQUEST_PAGE = 1002;
	private final static int SYNC_READY = 1003;
	private final static int FIRST_NOTIFY = 1004;
	
	private String mPath;
	
	private int mCurrPage = 0;
	private View mRootView;
	private int mPageCount;
	private boolean mInitialled = false;
	private boolean mReselect = false;
	private static final String TAG = "HostSyncReadView";
	
	IHostSyncEventListener mHostListener;
	
	private boolean mPageLoading = false;

	public HostSyncReadView(IHostSyncEventListener activity) {
		mHostListener = activity;
	}
	
	public void init(View root,String path) {
		mRootView = root;
		mPath = path;
		mLocalSource = true;
		setupViews();
		registerEvent();
		mHandler.sendEmptyMessageDelayed(CREATE_VIEW, 50);
	}
	
	public void updateDoc(String path) {
		if(mDoc != null) {
			mHandler.removeMessages(CREATE_VIEW);
			mHandler.removeMessages(UPDATE_VIEW);		
			mHandler.removeMessages(REQUEST_PAGE);
			mDoc.cleanup();
			mDocView.setDocument(null);
			mDoc = null;
		}
		mReselect = true;
		mPath = path;
		mCurrPage = 0;
		mDocName.setText(FilePathHelper.getNameFromFilepath(mPath));
		
		mHandler.sendEmptyMessageDelayed(CREATE_VIEW, 100);		

	}
	
	public void fini() {
		if(mHandler != null) {
			mHandler.removeMessages(CREATE_VIEW);
			mHandler.removeMessages(UPDATE_VIEW);		
			mHandler.removeMessages(REQUEST_PAGE);
			mHandler = null;
		}
		if(mDoc != null) {
			mDoc.cleanup();
		}
	}
	
	public boolean isReadyForSyncRead() {
		return mInitialled;
	}

	public int getPageCount() {
		return mPageCount;
	}
	
	public int getCurrPage() {
		return mCurrPage;
	}
	
	public int getDocViewWidth() {
		return mDocView.getWidth();
	}
		
	public int getDocViewHeight() {
		return mDocView.getHeight();
	}
	
	private void checkIsReselect() {
		if(mReselect) {
			try {
				byte[] action;
				action = DataFactory.getBytesFromObject(
						SyncAction.getUpdateDocAction(mPageCount,mCurrPage));
				notifyAction(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mReselect = false;
		}
	}
	
	private void initDoc() {
		mDoc = Document.openLocalPDF(mPath, mDocView);
		mPageCount = mDoc.getPageCount();
		mDoc.setCurrentPage(mCurrPage);
		mDoc.setPageLoadListener(mPageLoadListener);
		
		mInitialled = true;
		mDocView.setDocument(mDoc);//add for get bitmap
		mDocView.setHostView(HostSyncReadView.this);	
		checkIsReselect();
		updatePage();
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg)  {
			switch(msg.what) {
			case CREATE_VIEW:
				initDoc();
				sendEmptyMessageDelayed(SYNC_READY, 100);
				break;
				
			case UPDATE_VIEW:
				mDocView.invalidate();
				updatePage();
				break;
				
			case REQUEST_PAGE:
				onPageRequest((String)msg.obj, msg.arg1);
				break;
				
			case SYNC_READY:
				if(mDocView.getWidth() == 0 ) {
					sendEmptyMessageDelayed(SYNC_READY, 100);
				} else {
					removeMessages(SYNC_READY);
					mHostListener.syncReady();
					sendEmptyMessageDelayed(FIRST_NOTIFY, 100);
				}
				break;
			
			case FIRST_NOTIFY:
				Bitmap bmp = mDoc.getPage(mCurrPage);
				if(bmp != null) {
					byte[] imageArray = Document.compressImage(bmp);
					mHostListener.notifyPage(mCurrPage,imageArray);
				} else {
					mPageLoading = true;
				}
				break;
				
			default:
				Log.e(TAG,"unexpect msg:" + msg.what);
				break;
			}
		}
	};
	private void setupViews() {
		mDocView = (HostDocView)mRootView.findViewById(R.id.doc_area);
		mPageText = (TextView)mRootView.findViewById(R.id.tv_pagenum);
		mSwitchBtn = (ImageView)mRootView.findViewById(R.id.btn_switch);
		mDocName = (TextView)mRootView.findViewById(R.id.tv_file_name);
//		mExit = mRootView.findViewById(R.id.btn_close);
		mReselectFile = mRootView.findViewById(R.id.btn_view_back);
//		mViewConnectUser = mRootView.findViewById(R.id.btn_close);
		
		mDocName.setText(FilePathHelper.getNameFromFilepath(mPath));
	}
	
	
	private void registerEvent() {
		
		mDocView.setOnScrollListener(mScollListener);
		mDocView.setOnZoomListener(mZoomListener);
		mDocView.setOnDrawListener(mDrawListener);
		
		mSwitchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDocView.switchMode();
				if(mDocView.getOpMode() == OpMode.COMMENT) {
					mSwitchBtn.setImageResource(R.drawable.comment_switch_enable);					
				} else {
					mSwitchBtn.setImageResource(R.drawable.comment_switch_disable);
				}
				
			}
		});
		
//		mExit.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				mHostListener.onExitSync();				
//			}
//		});
		
		mReselectFile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				mHostListener.reselectFile();
			}
		});
		
//		mViewConnectUser.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				mHostListener.showConnectedUserList();
//			}
//		});

	}

	private int mFlipState = 0;
	
	
	private final static int FLIP_NEXT = 1;
	private final static int FLIP_PREVIOUS = 2;
	
	public void showPreviousPage() {
		if(mCurrPage > 0) {
			mFlipState = FLIP_PREVIOUS;
			showPage(--mCurrPage);			
		}
	}
	
	public void showNextPage() {
		if(mCurrPage < mDoc.getPageCount() - 1) {
			mFlipState = FLIP_NEXT;
			showPage(++mCurrPage);
		}
	}
	
	private void ensureNormalMode() {
		mDocView.ensureNormalMode();
		mSwitchBtn.setImageResource(R.drawable.comment_switch_disable);
	}
	
	private void showPage(int page) {
		ensureNormalMode();/**set to normal mode when enter new page*/
		
		mCurrPage = page;
		mDoc.setCurrentPage(page);
		//mHandler.sendEmptyMessageDelayed(UPDATE_VIEW, 50);
		
		//TODO: notify the network layer
		 Bitmap bmp = mDoc.getPage(page);
		if(bmp != null) {
			byte[] imageArray = Document.compressImage(bmp);
			mHostListener.notifyPage(page,imageArray);
		} else {
			mPageLoading = true;
		}
		
	}
	
	 
	
	private void updatePage() {
		mPageText.setText(""+(mCurrPage+1)+" / "+getPageCount());
	}
	
	public void onPageRequest(String peerUserName, int pageNum) {
		Bitmap bmp = mDoc.getPage(pageNum);
		if(bmp == null) {
			mHostListener.sendPage(-1,peerUserName, null,pageNum);
		} else {
			byte[] imageArray = Document.compressImage(bmp);
			mHostListener.sendPage(0,peerUserName, imageArray,pageNum);
		}
	}
	
	private void notifyAction(byte[] action) {
		mHostListener.notifyAction(mCurrPage,action);
	}
	
	
	private AnimationSet mNextAnim = null;
	private AnimationSet mPreAnim = null;
	
	private AnimationSet getFlipNextAnim() {
		if(mNextAnim == null) {
			mNextAnim = (AnimationSet)AnimationUtils.loadAnimation(Utils.getAppContext(), R.anim.push_left_in);
			mNextAnim.setDuration(300);
		}
		return mNextAnim;
	}
	
	private AnimationSet getFlipPreAnim() {
		if(mPreAnim == null) {
			mPreAnim = (AnimationSet)AnimationUtils.loadAnimation(Utils.getAppContext(), R.anim.push_right_in);
			mPreAnim.setDuration(300);
		}
		return mPreAnim;
	}
	
	private void updatePageView(int page) {
		if(page == mCurrPage) {
			mDocView.invalidate();
			updatePage();
			
			if(mFlipState == FLIP_NEXT) {
				mDocView.startAnimation(getFlipNextAnim());
			} else if(mFlipState == FLIP_PREVIOUS) {
				mDocView.startAnimation(getFlipPreAnim());
			}
		}
	}
	
	private Document.PageLoadListener mPageLoadListener = new  Document.PageLoadListener() {

		@Override
		public void onPageLoaded(int page) {
			
			updatePageView(page);
			
			if(mPageLoading) {
				mPageLoading = false;
				
				Bitmap bmp = mDoc.getPage(page);
				byte[] imageArray = Document.compressImage(bmp);
				mHostListener.notifyPage(page,imageArray);				
			}
		}
		
	};
	
	HostDocView.OnScrollListener mScollListener = new HostDocView.OnScrollListener() {

		@Override
		public void onScroll(float distanceX, float distanceY) {			
			byte[] action;
			try {
				distanceX = SyncAction.toUniformCoor(distanceX, mDocView.getWidth());
				distanceY = SyncAction.toUniformCoor(distanceY, mDocView.getHeight());
				action = DataFactory.getBytesFromObject(SyncAction.getScrollAction(distanceX, distanceY, 0));
				notifyAction(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void onScroll(float distanceX, float distanceY, int timeInMills) {
			byte[] action;
			try {
				distanceX = SyncAction.toUniformCoor(distanceX, mDocView.getWidth());
				distanceY = SyncAction.toUniformCoor(distanceY, mDocView.getHeight());
				action = DataFactory.getBytesFromObject(SyncAction.getScrollAction(distanceX, distanceY, timeInMills));
				notifyAction(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	};
	
	HostDocView.OnZoomListener mZoomListener = new HostDocView.OnZoomListener() {

		@Override
		public void onZoom(float scale, float centerX, float centerY,
				int timeInMills) {			
			byte[] action;
			try {
				centerX = SyncAction.toUniformCoor(centerX, mDocView.getWidth());
				centerY = SyncAction.toUniformCoor(centerY, mDocView.getHeight());
				action = DataFactory.getBytesFromObject(SyncAction.getZoomAction(scale, centerX,centerY, timeInMills));
				notifyAction(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onZoom(float scale, float centerX, float centerY) {
			byte[] action;
			try {
				centerX = SyncAction.toUniformCoor(centerX, mDocView.getWidth());
				centerY = SyncAction.toUniformCoor(centerY, mDocView.getHeight());
				action = DataFactory.getBytesFromObject(SyncAction.getZoomAction(scale, centerX,centerY, 0));
				notifyAction(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
						
		}

		@Override
		public void onZoom(float scale, int timeInMills) {
			byte[] action;
			try {
				action = DataFactory.getBytesFromObject(SyncAction.getZoomAction(scale, timeInMills));
				notifyAction(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		
	};
	
	HostDocView.OnDrawListener mDrawListener = new HostDocView.OnDrawListener() {

		@Override
		public void onDraw(DrawState state, float x1, float y1) {			
			byte[] action;
			try {
				x1 = SyncAction.toUniformCoor(x1, mDocView.getWidth());
				y1 = SyncAction.toUniformCoor(y1, mDocView.getHeight());
				action = DataFactory.getBytesFromObject(SyncAction.getDrawAction(state,x1, y1));
				notifyAction(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	};
}

package cn.nd.social.syncbrowsing.ui;


import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.privategallery.imageviewer.ImageViewTouchBase.DisplayType;
import cn.nd.social.syncbrowsing.Document;
import cn.nd.social.syncbrowsing.codec.LoadPageCallback;
import cn.nd.social.syncbrowsing.manager.IClientSyncEventListener;
import cn.nd.social.syncbrowsing.ui.SyncAction.DrawState;
import cn.nd.social.syncbrowsing.ui.SyncAction.PageDrawAction;
import cn.nd.social.syncbrowsing.ui.SyncAction.PageTransAction;
import cn.nd.social.syncbrowsing.ui.SyncAction.SyncActionBase;
import cn.nd.social.syncbrowsing.ui.SyncAction.SyncActionType;
import cn.nd.social.syncbrowsing.ui.SyncAction.UpdateDocAction;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.Utils;

public class ClientSyncReadView  {	


	ClientDocView mDocView;
	Document mDoc;
	private TextView mPageText;
	private View mBack;
	boolean mLocalSource;
	private final static int CREATE_VIEW = 1000;
	private final static int UPDATE_VIEW = 1001;
	private final static int CREATE_DOC = 1002;

	private int mCurrPage = 0;

	private View mRootView;
	private int mPageCount;
	
	private int mServerViewWidth = 0;
	private int mServerViewHeight = 0;
	
	private boolean mIsDocInited = false;
	
	private IClientSyncEventListener mClientListener;
	
	private final static String TAG = "ClientSyncReadView";
	
	public ClientSyncReadView(IClientSyncEventListener clientOp) {
		mClientListener = clientOp;
	}
	
	public void init(View root,int pageCount,int width, int height) {
		mRootView = root;
		mLocalSource = false;
		mPageCount = pageCount;
		mServerViewWidth = width;
		mServerViewHeight = height;
		setupViews();
		registerEvent();
		mHandler.sendEmptyMessageDelayed(CREATE_VIEW, 50);
	}
	
	public void fini() {
		if(mHandler != null) {
			mHandler.removeMessages(CREATE_VIEW);
			mHandler.removeMessages(CREATE_DOC);
			mHandler.removeMessages(UPDATE_VIEW);
		}
		
		if(mDoc != null) {
			mDoc.cleanup();
			mDoc = null;
		}
	}
	
	private void updateDoc(int pageCount,int currPage) {
		if(mHandler != null) {
			mHandler.removeMessages(CREATE_VIEW);
			mHandler.removeMessages(CREATE_DOC);
			mHandler.removeMessages(UPDATE_VIEW);
		}
		
		if(mDoc != null) {
			mDoc.cleanup();
			mDocView.setDocument(null);
			mDoc = null;
		}
		mPageCount = pageCount;
		mCurrPage = 0;
		mHandler.sendEmptyMessageDelayed(CREATE_VIEW, 100);
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg)  {
			switch(msg.what) {
			case CREATE_VIEW:
				Log.e(TAG,"width:" + mDocView.getWidth() + " height:" + mDocView.getHeight());
				int []size = new int[2];		
				if(adjustDocViewSize(mDocView.getWidth(),mDocView.getHeight(),size)) {
					ViewGroup.LayoutParams params = mDocView.getLayoutParams();
					params.width = size[0];
					params.height = size[1];
					mDocView.setDisplayType(DisplayType.FIT_TO_SCREEN);
					mDocView.requestLayout();
					mHandler.sendEmptyMessageDelayed(CREATE_DOC, 50);
				} else {
					initDocument();
				}
				break;
			case CREATE_DOC:
				initDocument();
				break;
			case UPDATE_VIEW:
				updatePage();
				mDocView.invalidate();
				break;
			}
		}
	};
	
	private void initDocument() {		
		mDoc = Document.createNetwork(mPageCount, mDocView,mNetPageLoader);
		mDoc.setCurrentPage(mCurrPage);
		mDocView.setDocument(mDoc);

		mDoc.setCurrentPage(mCurrPage);
		mHandler.sendEmptyMessageDelayed(UPDATE_VIEW, 50);
		updatePage();
		
		mIsDocInited = true;
	}
	
	public boolean isDocViewReady() {
		return mIsDocInited;
	}
	
	private void setupViews() {
		mDocView = (ClientDocView)mRootView.findViewById(R.id.doc_area);
		mBack = mRootView.findViewById(R.id.iv_back);
		mPageText = (TextView)mRootView.findViewById(R.id.tv_pagenum);
		mPageText.setVisibility(View.GONE);
		
		mDocView.setScrollEnabled(false);
		mDocView.setScaleEnabled(false);
	}
	
	
	private void updatePage() {
		mPageText.setText(""+(mCurrPage+1)+"/"+ mPageCount);
	}
	
	private boolean  adjustDocViewSize(int width, int height,int[]outSize) {
		if(mServerViewWidth == 0 || mServerViewHeight == 0) {
			return false;
		}
		outSize[0] = width;
		outSize[1] = height;
		float scaleX = (float)width / mServerViewWidth;
		float scaleY = (float)height / mServerViewHeight;
		if(scaleX == scaleY) {
			return false;
		} else if(scaleX > scaleY) {
			outSize[0] =(int) (mServerViewWidth * scaleY);
		} else {
			outSize[1] =(int) (mServerViewHeight * scaleX);
		}
		return true;
	}
	
	
	private void registerEvent() {
		mBack.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mClientListener.onExitSync();
				
			}
		});
	}
	
	private void showPage(int page) {
		mCurrPage = page;
		if(mDoc != null) {
			mDoc.setCurrentPage(page);
			mHandler.sendEmptyMessageDelayed(UPDATE_VIEW, 50);
		}
		
	}
	
	public void notifyPage(int page) {
		showPage(page);
	}
	
	public void notifyAction(int page,byte[]action) {		
		try {
			SyncActionBase baseAct;
			baseAct = (SyncActionBase)DataFactory.getObjectFromBytes(action);
			//TODO: seperate the update doc action with other pageAction
			if(baseAct.action == SyncActionType.UPDATE_DOC.ordinal()) {
				//TODO: update Doc
				UpdateDocAction upAct = (UpdateDocAction)baseAct;
				updateDoc(upAct.pageCount,upAct.currPage);
				return;
			} 
			if(page != mCurrPage) {
				Log.e(TAG,"action page and mCurrPage mismatch");
				return;
			}
			if(baseAct.action == SyncActionType.COMMENT_MODE.ordinal()) {
				PageDrawAction drawAct = (PageDrawAction)baseAct;
				drawAct.x1 =  SyncAction.toNativeCoor(drawAct.x1, mDocView.getWidth());
				drawAct.y1 =  SyncAction.toNativeCoor(drawAct.y1, mDocView.getHeight());
				mDocView.onDrawAction(DrawState.fromInt(drawAct.state),drawAct.x1,drawAct.y1);
			} else {
				PageTransAction transAct = (PageTransAction)baseAct;
				if(transAct.action == SyncActionType.SCROLL.ordinal()) {
					transAct.distanceX = SyncAction.toNativeCoor(transAct.distanceX, mDocView.getWidth());
					transAct.distanceY = SyncAction.toNativeCoor(transAct.distanceY, mDocView.getHeight());
					mDocView.onScroll(transAct.distanceX, transAct.distanceY);				
				} else if(transAct.action == SyncActionType.SCROOL_ANIM.ordinal()) {
					transAct.distanceX = SyncAction.toNativeCoor(transAct.distanceX, mDocView.getWidth());
					transAct.distanceY = SyncAction.toNativeCoor(transAct.distanceY, mDocView.getHeight());
					mDocView.onScroll(transAct.distanceX, transAct.distanceY,transAct.timeMills);
				} else if(transAct.action == SyncActionType.ZOOM_ANIM.ordinal()) {
					mDocView.onZoom(transAct.zoomScale, transAct.timeMills);
				} else if(transAct.action == SyncActionType.ZOOM_IN_CENTER.ordinal()) {
					transAct.centerX = SyncAction.toNativeCoor(transAct.centerX, mDocView.getWidth());
					transAct.centerY =SyncAction.toNativeCoor(transAct.centerY, mDocView.getHeight());
					mDocView.onZoom(transAct.zoomScale, transAct.centerX,transAct.centerY);
				} else if(transAct.action == SyncActionType.ZOOM_IN_CENTER_ANIM.ordinal()) {
					transAct.centerX = SyncAction.toNativeCoor(transAct.centerX, mDocView.getWidth());
					transAct.centerY = SyncAction.toNativeCoor(transAct.centerY, mDocView.getHeight());
					mDocView.onZoom(transAct.zoomScale, transAct.centerX,transAct.centerY,transAct.timeMills);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void onPageRequestAck(byte[]imageArray,int pageNum) {
		if(imageArray == null) {
			Log.e(TAG, "onPageRequestAck error");
			Toast.makeText(Utils.getAppContext(), "get Page error", Toast.LENGTH_SHORT).show();
			return;
		}
		Bitmap bmp = Document.decompressImage(imageArray, 0, imageArray.length);
		mNetPageLoader.onPageArrival(bmp,pageNum);
	}
	
	ClientSyncReadView.NetworkPageLoader mNetPageLoader = new ClientSyncReadView.NetworkPageLoader();
	
	public class NetworkPageLoader {
		
		LoadPageCallback mCallback;
		Object mObjectKey;
		int mIndex = -1;
		
		//TODO:if we request page too frequently, the old request will be flushed;
		//an array list may fix this
		public void requestPage(Object obj,int index,LoadPageCallback callback) {
			mObjectKey = obj;
			mIndex = index;
			mCallback = callback;
			//mClientListener.requestPage(index, 0);
		}
		
		public void onPageArrival(Bitmap bmp,int pageNum) {
			if (pageNum == mIndex) {
				mCallback.onLoadComplete(mObjectKey, mIndex, bmp);
			} else {
				mCallback.onLoadComplete(null, pageNum, bmp);
			}
		}
		
		public void setCallBack(LoadPageCallback cbk) {
			mCallback = cbk;
		}
	}
}

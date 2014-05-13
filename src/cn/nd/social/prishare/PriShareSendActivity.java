package cn.nd.social.prishare;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.common.FlyAnimation;
import cn.nd.social.common.VibratorController;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.net.PrivateSwitcher;
import cn.nd.social.net.WifiStateConstant;
import cn.nd.social.prishare.component.AudioListViewAdapter;
import cn.nd.social.prishare.component.FilesGridViewAdapter;
import cn.nd.social.prishare.component.GalleryGridViewAdapter;
import cn.nd.social.prishare.component.GridViewAdapter;
import cn.nd.social.prishare.component.InterfaceForMain;
import cn.nd.social.prishare.component.InterfaceHandlerForMain;
import cn.nd.social.prishare.component.MainListViewAdapter;
import cn.nd.social.prishare.component.SetTimeActivity;
import cn.nd.social.prishare.component.UserHead;
import cn.nd.social.prishare.history.PrivacyShareHistoryView;
import cn.nd.social.prishare.items.AppCellItem;
import cn.nd.social.prishare.items.AudioCellItem;
import cn.nd.social.prishare.items.CellItemBase;
import cn.nd.social.prishare.items.FilesCellItem;
import cn.nd.social.prishare.items.ImageCellItem;
import cn.nd.social.qrcode.EncodingHandler;
import cn.nd.social.sendfile.SendMutipleItem.SendSingleItemHandle;
import cn.nd.social.util.BitmapUtils;
import cn.nd.social.util.DimensionUtils;
import cn.nd.social.util.UnitConverter;
import cn.nd.social.util.Utils;

import com.google.zxing.WriterException;


public class PriShareSendActivity extends Activity implements InterfaceForMain{	
	private final static String TAG = "Main";

	private RadioGroup mNavGroup;
	private ImageView mNavIndicator;
	private int mIndicatorWidth;
	
	private RelativeLayout mWaitTouch;
	private ImageView mRadarBg;
	private ImageView mQrCode;
	private ProgressBar mRadar;
	
	
	private Button mCollectBtn;
	private ImageView mShopCarImg;
	private TextView mShopNum;
	private RelativeLayout mPageContainer;
	
	private int mCurrPage = 0;
	private int currIndLeft = 0;
	
	private int mSelectedApps = 0;
	private int mSelectedImgs = 0;
	private int mSelectedAuds = 0;
	
	private Map<CellItemBase, View> mMultiMap = new HashMap<CellItemBase, View>();
	private HashMap<String, View> mFileList = new HashMap<String, View>();

	private RelativeLayout mBodyContainer;
	private View mWaitContainer;
	private View mUserListContainer;
	
		

	private PrivacyShareHistoryView mHistory;
	private InterfaceHandlerForMain mInterHandForMain;
	
	private boolean mShowWaitingShare = false;	

	
	
	private ArrayList<AppCellItem> mCellList;

	private GridView mAppGrid;
	private GridView mGalleryGrid;
	private GridView mFilesGrid;
	private ListView mAudioList;
	
	private View mBackBtn;	
	private View mSendBtn;

	private int mScreenWidth;

	private UIHandler mUiHandler;
	private Handler mWorkerHandler;
	private HandlerThread mWorkerThread;

	private LayoutInflater mInflater;
	
	private GridViewAdapter mAppGridAdapter;
	private FilesGridViewAdapter mFilesAdapter;
	private AudioListViewAdapter mAudioListAdapter;
	private GalleryGridViewAdapter mGalleryAdapter;
	
	private String mFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	private Context mContext;
	public ArrayList<String> mConnectedUser = new ArrayList<String>();
	
	private PrivateSwitcher mPrivateSwitcher;
	
	
	public ArrayList<String> getmConnectedUser() {
		return mConnectedUser;
	}

	public void setmConnectedUser(ArrayList<String> connectedUser) {
		this.mConnectedUser = connectedUser;
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		earlyInit();
		
		setContentView(R.layout.qe_main);

		setupViews();

		setupPager();

		/* initialize variables */
		initFuntion();
		
		initNavTab();
		
		registerEventListener();

		initResourceLoader();
		
		PrivateSwitcher.showPrivateDialog(mContext,PriShareConstant.KEY_HIDE_PRIVATE_HIDE,R.string.enter_private_mode_hint);
	}
	
	
	private void earlyInit() {
		// 2014_0117 close mobile data add start
		mPrivateSwitcher = new PrivateSwitcher();
		mPrivateSwitcher.enterPrivateState();
		
		mContext = this;
		sMultiMode = false;
		mInflater = LayoutInflater.from(this);
		
		mScreenWidth = DimensionUtils.getDisplayWidth();
	}

	private void initFuntion() {
		mHistory = new PrivacyShareHistoryView(this);
		mInterHandForMain = new InterfaceHandlerForMain(this, mHistory, this);
		mUiHandler = new UIHandler();
		mInterHandForMain.setUIHandler(mUiHandler);
	}
	
	private void initResourceLoader() {
		mWorkerThread = new HandlerThread("ResourceWorkerThread");
		mWorkerThread.setPriority(Thread.MIN_PRIORITY);
		mWorkerThread.start();
		mWorkerHandler = new Handler(mWorkerThread.getLooper());
	}



	class GetAppInfo implements Runnable {

		@Override
		public void run() {
			ArrayList<AppCellItem> mItemList = new ArrayList<AppCellItem>();
			List<Map<String, Object>> listItems = PriUtils.getAPPInstalled(PriShareSendActivity.this);
			for (int i = 0; i < listItems.size(); i++) {
				Map<String, Object> map = listItems.get(i);
				mItemList.add(new AppCellItem((String) map.get("app_name"),
						(Drawable) map.get("app_logo"), (String) map
								.get("package_name"), (String) map
								.get("app_dir")));
			}
			mCellList = mItemList;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideWaiting(mPagerViews[PriShareConstant.APP_INDEX].v);
					mAppGridAdapter = new GridViewAdapter(PriShareSendActivity.this,
							R.layout.qe_app_cell, mCellList, 0); 
					mAppGrid.setAdapter(mAppGridAdapter);

				}
			});
		}
	}


	class GetImageThumbnails implements Runnable {

		@Override
		public void run() {
			final ArrayList<ImageCellItem> cellList = PriUtils.getThumbList();
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideWaiting(mPagerViews[PriShareConstant.GALLERY_INDEX].v);
					mGalleryAdapter = new GalleryGridViewAdapter(PriShareSendActivity.this,
							R.layout.qe_gallery_cell, cellList); 
					mGalleryGrid.setAdapter(mGalleryAdapter);

				}
			});

		}

	}
	
	
	class GetFilesThumbnails implements Runnable {

		@Override
		public void run() {
			ArrayList<FilesCellItem> itemList = new ArrayList<FilesCellItem>();

			File f = new File(mFilesPath);
			if (!mFilesPath.equals(PriShareConstant.ROOT_FILE_DIRECTORY)) {

				Drawable localDrawable = mContext.getResources().getDrawable(
						R.drawable.zapya_data_folder_folder);

				itemList.add(new FilesCellItem(0, localDrawable, "../", f
						.getParent(), 1));
			}

			int i = 1;
			File[] directoryContents = f.listFiles();

			for (File target : directoryContents) {
				if (target.isHidden())
					continue;

				if (target.isDirectory()) {
					Drawable localDrawable = mContext.getResources()
							.getDrawable(R.drawable.zapya_data_folder_folder);

					itemList.add(new FilesCellItem(i, localDrawable, target
							.getName(), target.getPath(), 1));
				} else {
					Drawable localDrawable = mContext.getResources()
							.getDrawable(R.drawable.zapya_data_folder_doc);

					itemList.add(new FilesCellItem(i, localDrawable, target
							.getName(), target.getPath(),0));
				}

				i++;
			}

			Collections.sort(itemList, new PriUtils.FileComparator());

			final ArrayList<FilesCellItem> cellList = itemList;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideWaiting(mPagerViews[PriShareConstant.FILE_INDEX].v);
					mFilesAdapter = new FilesGridViewAdapter(PriShareSendActivity.this,
							R.layout.qe_files_cell, cellList, 0, mFileList);

					mFilesGrid.setAdapter(mFilesAdapter);
				}
			});
		}
	}

	
	class GetAudioThumbnails implements Runnable {

		@Override
		public void run() {
			final ArrayList<AudioCellItem> cellList = PriUtils.getAudioItemList();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideWaiting(mPagerViews[PriShareConstant.AUDIO_INDEX].v);
					mAudioListAdapter = new AudioListViewAdapter(PriShareSendActivity.this,
							R.layout.qe_list_audio_item, cellList); 
					mAudioList.setAdapter(mAudioListAdapter);
				}
			});

		}

	}
	
	private void showWaiting(View parent) {
		parent.findViewById(R.id.gridprogresslay).setVisibility(View.VISIBLE);
	}

	private void hideWaiting(View parent) {
		parent.findViewById(R.id.gridprogresslay).setVisibility(View.GONE);
	}

	private void registerEventListener() {
		
		mBackBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();				
			}
		});

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mCurrPage = position;

				if (mNavGroup != null && mNavGroup.getChildCount() > position) {
					mNavGroup.check(position);
//					mNavGroup.getChildAt(position).performClick();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		mNavGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (mNavGroup.getChildAt(checkedId) != null) {
					resetRadioBtn();
					mNaviItem[checkedId].rb.setChecked(true);
					mCurrRbID = checkedId;
					onCheckChange(checkedId);					
				}
			}
		});

		mCollectBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// showPopupWindow(v.getLeft(),v.getBottom(),v);
				if (!checkHasSendItem()) {
					Toast.makeText(mContext, "no file for transmitting",
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				mShowWaitingShare = true;
				toggleShareUserView(mShowWaitingShare);
				mInterHandForMain.transWifiInfoIfNeed();
				showQrCode();				
			}
		});

		mShopCarImg.setOnClickListener(new ShopCarListener());
		
		mInterHandForMain.regReceiver();
	}


	private boolean checkHasSendItem() {
		Set<CellItemBase> set = (Set<CellItemBase>) mMultiMap.keySet();
		Set<String> fileSet = (Set<String>) mFileList.keySet();
		return (set.size() > 0) || (fileSet.size() > 0);
	}
	
	private boolean mEnableSend = false; 
	
	private void refreshConnectUI(int wifiType) {
		if(wifiType == WifiStateConstant.WIFI_HOTSPOT_PENDING) {
			mWaitHint.setText(R.string.ap_initializing);
			mSendBtn.setVisibility(View.GONE);
			mEnableSend = false;
		} else if(wifiType == WifiStateConstant.WIFI_TYPE_HOTSPOT) {
			
			if(getConncetUserNum() > 0) {
				String str = getString(R.string.find_friends);
				mWaitHint.setText(String.format(str, getConncetUserNum()));
				//mSendBtn.setVisibility(View.VISIBLE);
				mEnableSend = true;
			} else {
				mWaitHint.setText(R.string.wait_conn);
				mSendBtn.setVisibility(View.GONE);
				mEnableSend = false;
			}
		} else {
			mWaitHint.setText(R.string.ap_initialize_fail);
			mSendBtn.setVisibility(View.GONE);
			mEnableSend = false;
		}
	}
	
	private void toggleShareUserView(boolean showWait) {
		if (showWait) {
			refreshConnectUI(mInterHandForMain.getWifiType());
			mRadar.startAnimation(getRotateAnim());
			mUserListContainer.setVisibility(View.VISIBLE);
			mPageContainer.setVisibility(View.GONE);
		} else {		
			exitSend();
			mInterHandForMain.stopTransWifiInfo();
			mRadar.clearAnimation();
			mPageContainer.setVisibility(View.VISIBLE);
			mUserListContainer.setVisibility(View.GONE);
		}
	}

	
	
	private Animation mRotateAnim;
	private Animation mTurnOutAnim;
	
	private Animation getRotateAnim() {
		if(mRotateAnim == null) {
			mRotateAnim = AnimationUtils.loadAnimation(PriShareSendActivity.this,
					R.anim.radar_rotate_anim);
		}
		return mRotateAnim;
	}
	
	private Animation getUserTurnOutAnim() {
		if(mTurnOutAnim == null) {
			mTurnOutAnim = AnimationUtils.loadAnimation(this,
					R.anim.radar_turn_out);
		}
		return mTurnOutAnim;
	}
	
	// /////////////////////////////////////////////////
	
	/** Navigation Group Operation & decoration**/

	class ViewHolder {		
		RadioButton rb;
		ImageView unsentHint;
	}
	
	private int mCurrRbID ;
	private ViewHolder []mNaviItem = new ViewHolder[PriShareConstant.TAB_TITLE.length];
	
	private void initNavTab() {
		
		mIndicatorWidth = mScreenWidth / PriShareConstant.TAB_COUNT;
		ViewGroup.LayoutParams cursorParam = mNavIndicator.getLayoutParams();
		cursorParam.width = mIndicatorWidth;
		mNavIndicator.setLayoutParams(cursorParam);
		
		mNavGroup.removeAllViews();

		for (int i = 0; i < PriShareConstant.TAB_TITLE.length; i++) {
			mNaviItem[i] = new ViewHolder();
			FrameLayout fl = (FrameLayout) mInflater.inflate(
					R.layout.qe_nav_radiogroup_item,mNavGroup, false);
			
			mNaviItem[i].rb = (RadioButton) fl.findViewById(R.id.radio_button);
			mNaviItem[i].rb.setId(i);
			mNaviItem[i].rb.setText(PriShareConstant.TAB_TITLE[i]);
			mNaviItem[i].rb.setCompoundDrawablesWithIntrinsicBounds(0, PriShareConstant.TAB_TITLE_LOGO[i], 0,
					0);
			ViewGroup.LayoutParams params = fl.getLayoutParams();
			if( params != null) {
				params.height = LayoutParams.MATCH_PARENT;
				params.width = mIndicatorWidth;
			} else {
				mNaviItem[i].rb.setLayoutParams(new LinearLayout.LayoutParams(mIndicatorWidth,
						LayoutParams.MATCH_PARENT));
			}

			mNaviItem[i].unsentHint = (ImageView)fl.findViewById(R.id.main_tab_unread_tv);
			
			mNaviItem[i].rb.setOnClickListener(mRadioButtonListener);
			mNavGroup.addView(fl);
		}
		
		mNaviItem[0].rb.setChecked(true);
		mCurrRbID = 0;
	}
	
	private View.OnClickListener mRadioButtonListener = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			if (mNaviItem[mCurrRbID].rb == v) return;
			resetRadioBtn();
			((RadioButton)v).setChecked(true);
			mCurrRbID = ((RadioButton)v).getId();
			onCheckChange(mCurrRbID);
		}
	};
	
	private void onCheckChange(int checkedId){
		TranslateAnimation animation = new TranslateAnimation(
				currIndLeft, ( mNavGroup.getChildAt(checkedId)).getLeft(), 0f, 0f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(100);
		animation.setFillAfter(true);

		mNavIndicator.startAnimation(animation);

		mPager.setCurrentItem(checkedId);

		// record the distance between current position and leftmost
		currIndLeft = mNavGroup.getChildAt(checkedId).getLeft();
	}
	
	private void resetRadioBtn() {		
		for (int i = 0; i < mNaviItem.length; i++) {
			mNaviItem[i].rb.setChecked(false);
		}
	}

	/////////////////////////////////////////////////////////
	
	private int mTimeToExpire = 5; // default time:5 secs
	
	
	private View mSendingProgress;
	private AlertDialog mSendingDialog;
	private ProgressBar mSendingProgressBar;
	private ImageView mSendingIcon;
	private TextView mFilenameView;
	private View mCancelBtn;
	
	private void showSendingProgress() {
		mShareSendingScreen.setVisibility(View.VISIBLE);
		if(mSendingDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mSendingProgress = mInflater.inflate(R.layout.pri_sending_progress_dialog,mBodyContainer,false);
			
			mSendingProgressBar =  (ProgressBar)mSendingProgress.findViewById(R.id.progress_bar);
			mSendingIcon = (ImageView)mSendingProgress.findViewById(R.id.bg_cover);
			mFilenameView = (TextView)mSendingProgress.findViewById(R.id.file_name);
			mCancelBtn = mSendingProgress.findViewById(R.id.cancel);
			
			builder.setView(mSendingProgress);
			mSendingDialog = builder.create();
			mSendingDialog.setCancelable(false);
			
			mCancelBtn.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					hideSendingProgress();
				}
			});
			
		} 
		mSendingDialog.show();
	}
	
	private void hideSendingProgress() {
		if(mSendingDialog != null) {
			mSendingDialog.hide();
		}
	}
	
	private void closeSendingProgress() {
		mShareSendingScreen.setVisibility(View.GONE);
		if(mSendingDialog != null) {
			mSendingDialog.dismiss();
			mSendingDialog = null;
		}
		mSendingProgress = null;
		mSendingProgressBar = null;
		mSendingIcon = null;
		mFilenameView = null;
	}
	
	private void onSendFinish() {
		closeSendingProgress();
	}
	
	private void onUpdateSendProgress(int progress,int subProgress) {
		if(mSendingProgressBar != null) {
			mSendingProgressBar.setProgress(progress);
		}
	}
	
	private void onSendItem(CellItemBase item) {
		if(mSendingIcon == null) {
			return;
		}
		
		mFilenameView.setText(item.getFileShortName());
		int type = item.getType();
		switch(type) {
		case CellItemBase.IMAGE_TYPE:
			Bitmap bmp = BitmapUtils.decodeBitmapByFile(item.getItemPath(), 
					UnitConverter.dpToPx(getResources(), 100),UnitConverter.dpToPx(getResources(), 100));
			if(bmp != null) {
				mSendingIcon.setImageBitmap(bmp);
			} else {
				mSendingIcon.setImageResource(R.drawable.pri_share_gallery);
			}
			break;
			
		case CellItemBase.APP_TYPE:
			mSendingIcon.setImageDrawable(item.getItemIcon());
			break;
			
		case CellItemBase.VIDEO_TYPE:
			mSendingIcon.setImageResource(R.drawable.pri_share_music);
			break;
			
		case CellItemBase.FILES_TYPE:
			mSendingIcon.setImageResource(R.drawable.file_icon);
			break;
			
		default:
			break;
		}
		mSendingProgressBar.setProgress(0);
	}
	
	
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PriShareConstant.REQ_CODE_SET_EXPIRE_TIME) {
			if (resultCode == RESULT_OK) {
				String expireTime = data
						.getStringExtra(SetTimeActivity.EXPIRE_TIME);
				mTimeToExpire = PriUtils.getExpireSec(expireTime);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// /////////////////////////////////////////////////

	class ShopCarListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {

			final MainListViewAdapter listAdapter = new MainListViewAdapter(
					mMultiMap, mFileList);
			/**
			 * 
			 */
			AlertDialog.Builder fileDialog = new AlertDialog.Builder(mContext);
			fileDialog.setTitle(R.string.list);
			fileDialog.setAdapter(listAdapter, null);
			fileDialog.setPositiveButton(R.string.all_delete,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							AlertDialog.Builder deleteDialog = new AlertDialog.Builder(
									mContext);
							deleteDialog.setTitle(R.string.all_delete);
							deleteDialog.setPositiveButton(R.string.sure,
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											clearSelectState();
										}
									});

							deleteDialog.setNegativeButton(R.string.cancel,
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {

											ShopCarListener scar = new ShopCarListener();
											scar.onClick(mShopCarImg);
										}
									});
							deleteDialog.show();

						}
					});
			fileDialog.setNegativeButton(R.string.cancel, null);

			final AlertDialog diaglog = fileDialog.create();

			listAdapter.setCheckListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					CellItemBase cellItem = (CellItemBase) v.getTag();
					cellItem.setSelected(false);
					if (cellItem.getType() == CellItemBase.FILES_TYPE) {
						GridViewAdapter.Holder hold = (GridViewAdapter.Holder) mFileList
								.get(cellItem.getItemPath()).getTag();
						mFileList.remove(cellItem.getItemPath());
						if (hold.item.getItemPath().equals(
								cellItem.getItemPath())) {
							hold.chkBox.setChecked(false);

						}
						if (mFileList.isEmpty()) {
							mNaviItem[PriShareConstant.FILE_INDEX].unsentHint.setVisibility(View.GONE);
						}
						getFileAysnc(mPagerViews[3].v);

					} else {
						GridViewAdapter.Holder hold = (GridViewAdapter.Holder) mMultiMap
								.get(cellItem).getTag();
						mMultiMap.remove(cellItem);
						if (hold.item == cellItem) {
							hold.chkBox.setChecked(false);
						}												
						
						setUnreadDots(cellItem,false);

					}
					if (mMultiMap.size() == 0 && mFileList.size() == 0) {
						diaglog.dismiss();
					}
					listAdapter.removeItemFromArr(cellItem);
					listAdapter.notifyDataSetChanged();
					setMultiCount();

				}
			});
			diaglog.show();

		}
	}
	
	private void resetCounts(){
		mSelectedApps = 0;
		mSelectedImgs = 0;
		mSelectedAuds = 0;
	}
	
	private void setUnreadDots(CellItemBase cell, boolean addOrRemove){
		switch (cell.getType()) {
		case  CellItemBase.APP_TYPE:
			if (addOrRemove) mSelectedApps++;
			else mSelectedApps--;
			break;
		case  CellItemBase.IMAGE_TYPE:
			if (addOrRemove) mSelectedImgs++;
			else mSelectedImgs--;
			break;
		case  CellItemBase.VIDEO_TYPE:
			if (addOrRemove) mSelectedAuds++;
			else mSelectedAuds--;
			break;
		default:
			break;
		}				
		
		int visible = mSelectedApps > 0 ? View.VISIBLE : View.GONE;
		mNaviItem[PriShareConstant.APP_INDEX].unsentHint.setVisibility(visible);
		
		
		visible = mSelectedImgs > 0 ? View.VISIBLE : View.GONE;
		mNaviItem[PriShareConstant.GALLERY_INDEX].unsentHint.setVisibility(visible);

		visible = mSelectedAuds > 0 ? View.VISIBLE : View.GONE;
		mNaviItem[PriShareConstant.AUDIO_INDEX].unsentHint.setVisibility(visible);
		
	}

	

	public void clearSelectState() {
		
		Set<CellItemBase> itemSet = mMultiMap.keySet();
		Set<String> fileSet = (Set<String>) mFileList.keySet();
		for (CellItemBase it : itemSet) {
			it.setSelected(false);
			GridViewAdapter.Holder hold = (GridViewAdapter.Holder) mMultiMap
					.get(it).getTag();
			if (hold.item == it) {
				hold.chkBox.setChecked(false);
			}
		}
		for (String str : fileSet) {
			GridViewAdapter.Holder hold = (GridViewAdapter.Holder) mFileList
					.get(str).getTag();
			hold.item.setSelected(false);
			if (hold.item.getItemPath().equals(str)) {
				hold.chkBox.setChecked(false);
			}
		}
		
		mMultiMap.clear();
		mFileList.clear();
		
		resetCounts();
		for (int i = 0; i < PriShareConstant.TAB_TITLE.length; i++) {
			mNaviItem[i].unsentHint.setVisibility(View.GONE);
		}
		setMultiCount();
	}
	
	private void sendAppFile(final String userName,final String filePath,final String appName) {
		// APP type need help install
		new AlertDialog.Builder(PriShareSendActivity.this)
				.setTitle(mContext.getString(R.string.hint))
				.setMessage(
						mContext.getString(R.string.hint_send_apk_authority))
				.setNegativeButton(R.string.Cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								mInterHandForMain.sendFile(userName, filePath, appName,
										MsgDefine.FILE_TYPE_APP);
							}
						})
				.setPositiveButton(R.string.OK,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								mInterHandForMain.sendFile(
										userName,
										filePath,
										appName,
										MsgDefine.FILE_TYPE_APP,
										MsgDefine.GRANT_APK_SILENT_INSTALL,
										mTimeToExpire, 0);
							}
						}).show();
	}
	

	public void sendSingleItem(String userName, CellItemBase item) {

		int type = item.getType();
		String filePath = item.getItemPath();
		String shortName = item.getFileShortName();

		switch (type) {
		case CellItemBase.IMAGE_TYPE: {
			if (mTimeToExpire == PriShareConstant.INFINITE_TIME) {
				mInterHandForMain.sendFile(userName, filePath, shortName,
						MsgDefine.FILE_TYPE_IMAGE);
			} else {
				mInterHandForMain.sendFile(userName, filePath, shortName,
						MsgDefine.FILE_TYPE_IMAGE,
						MsgDefine.GRANT_FILE_AUTO_DESTROY, mTimeToExpire, 0);
			}
			break;
		}

		case CellItemBase.APP_TYPE:
			sendAppFile(userName, filePath, shortName);
			break;

		case CellItemBase.VIDEO_TYPE:
			mInterHandForMain.sendFile(userName, filePath, shortName,
					MsgDefine.FILE_TYPE_MEDIA);
			break;

		default:
			mInterHandForMain.sendFile(userName, filePath, shortName,
					MsgDefine.FILE_TYPE_UNKNOWN);
			break;
		}
	}

	// /////////////////////////////////////////////////

	private void dismissSendWaiting() {
		mShowWaitingShare = false;
		toggleShareUserView(mShowWaitingShare);
	}

	private void initAppGrid(View parent) {
		mAppGrid = (GridView) parent.findViewById(R.id.gridid);
		setGridViewItemEvent();
		mWorkerHandler.post(new GetAppInfo());
		showWaiting(parent);
	}

	private void initGalleryGrid(View parent) {
		mGalleryGrid = (GridView) parent.findViewById(R.id.gridid);
		mGalleryGrid.setNumColumns(3);
		mGalleryGrid.setColumnWidth((mScreenWidth - 20) / 3);
		setGalleryViewItemEvent();
		mWorkerHandler.post(new GetImageThumbnails());
		showWaiting(parent);
	}

	private void initAudioListView(View parent) {
		parent.findViewById(R.id.gridid).setVisibility(View.GONE);
		mAudioList = (ListView) parent.findViewById(R.id.listid);
		mAudioList.setVisibility(View.VISIBLE);
		setAudioViewItemEvent();
		mWorkerHandler.post(new GetAudioThumbnails());
		showWaiting(parent);
	}

	private void initFilesGridView(View parent) {
		mFilesGrid = (GridView) parent.findViewById(R.id.gridid);
		setFilesViewItemEvent();
		getFileAysnc(parent);
	}

	private void getFileAysnc(View parent) {
		mWorkerHandler.post(new GetFilesThumbnails());
		showWaiting(parent);
	}

	// //////////////////////////////////////////////////////
	/**
	 * gridView listeners and animations collections
	 */
	/**
	 * apptab
	 */
	void setGridViewItemEvent() {
		mAppGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// AppCellItem item = ((GridViewAdapter.Holder)view.getTag());

				if (isMultiSelectMode()) {

					CheckBox cb = ((GridViewAdapter.Holder) view.getTag()).chkBox;
					if (cb.isChecked()) {
						cb.setChecked(false);
						mMultiMap.remove(mAppGridAdapter.getItem(position));
						mAppGridAdapter.getItem(position).setSelected(false);
						setUnreadDots(mAppGridAdapter.getItem(position),false);
						setMultiCount();
						return;
					} else {
						cb.setChecked(true);
						mAppGridAdapter.getItem(position).setSelected(true);
						mMultiMap.put(mAppGridAdapter.getItem(position), view);
						setUnreadDots(mAppGridAdapter.getItem(position),true);
						setMultiCount();
					}

				} else {
					enterMutliSelectMode(view, position);
				}
				
				startFlyAnimation(view);
			}

		});

	}

	private void startFlyAnimation(View v) {
		FlyAnimation fly = new FlyAnimation(mContext);
		fly.startFly(v, mShopCarImg, mPageContainer);
	}

	private void setGalleryViewItemEvent() {
		mGalleryGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (isMultiSelectMode()) {
					
					CheckBox cb = ((GalleryGridViewAdapter.Holder) view
							.getTag()).chkBox;
					if (cb.isChecked()) {
						cb.setChecked(false);
						mMultiMap.remove(mGalleryAdapter.getItem(position));
						mGalleryAdapter.getItem(position).setSelected(false);
						setUnreadDots(mGalleryAdapter.getItem(position),false);
						setMultiCount();
						return;
					} else {
						cb.setChecked(true);
						mMultiMap.put(mGalleryAdapter.getItem(position), view);
						mGalleryAdapter.getItem(position).setSelected(true);
						setUnreadDots(mGalleryAdapter.getItem(position),true);
						setMultiCount();
					}

				} else {
					enterMutliSelectMode(view, position);
				}

				startFlyAnimation(view);
			}

		});
	}


	private void setFilesViewItemEvent() {
		mFilesGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				FilesGridViewAdapter.Holder holder = (FilesGridViewAdapter.Holder) view
						.getTag();

				FilesCellItem item = (FilesCellItem) holder.item;
				if (item.isDirectory()) {
					mFilesPath = item.getItemPath();

					getFileAysnc((View) parent.getParent());
				} else {

					if (isMultiSelectMode()) {
						CheckBox cb = ((FilesGridViewAdapter.Holder) view
								.getTag()).chkBox;
						
						if (cb.isChecked()) {

							cb.setChecked(false);
							
							mFileList.remove(mFilesAdapter.getItem(position)
									.getItemPath());
							mFilesAdapter.getItem(position).setSelected(false);
							setMultiCount();
							
							if (mFileList.isEmpty()) {
								mNaviItem[PriShareConstant.FILE_INDEX].unsentHint.setVisibility(View.GONE);
							}
							return;

						} else {
							cb.setChecked(true);

							mFileList.put(mFilesAdapter.getItem(position)
									.getItemPath(), view);
							if (mFileList.size() == 1) {
								mNaviItem[PriShareConstant.FILE_INDEX].unsentHint.setVisibility(View.VISIBLE);
							}
							mFilesAdapter.getItem(position).setSelected(true);
							setMultiCount();
							

						}
					} else {
						enterMutliSelectMode(view, position);

					}
					
					startFlyAnimation(view);
				}

			}

		});
	}

	void setAudioViewItemEvent() {
		mAudioList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (isMultiSelectMode()) {

					CheckBox cb = ((GridViewAdapter.Holder) view.getTag()).chkBox;
					if (cb.isChecked()) {
						cb.setChecked(false);
						mMultiMap.remove(mAudioListAdapter.getItem(position));
						mAudioListAdapter.getItem(position).setSelected(false);
						setUnreadDots(mAudioListAdapter.getItem(position),false);
						setMultiCount();
						return;
					} else {
						cb.setChecked(true);
						mMultiMap.put(mAudioListAdapter.getItem(position), view);
						mAudioListAdapter.getItem(position).setSelected(true);
						setUnreadDots(mAudioListAdapter.getItem(position),true);
						setMultiCount();
					}

				} else {
					enterMutliSelectMode(view, position);

				}
				
				startFlyAnimation(view);
			}

		});
	}

	// /////////////////////////
	private void enterMutliSelectMode(View view, int pos) { // pos is useless

		setMultiSelectMode(true);
		GridViewAdapter.Holder holder = ((GridViewAdapter.Holder) view.getTag());
		holder.chkBox.setChecked(true);
		holder.item.setSelected(true);

		if (mCurrPage == PriShareConstant.FILE_INDEX) {
			mFileList.put(holder.item.getItemPath(), view);
			if (mFileList.size() == 1) {
				mNaviItem[PriShareConstant.FILE_INDEX].unsentHint.setVisibility(View.VISIBLE);
			}
		} else {
			mMultiMap.put(holder.item, view);
			setUnreadDots(holder.item,true);
		}
		setMultiCount();
	}

	// //////////////////////////////////////////////////////

	private void setupViews() {
		mCollectBtn = (Button) findViewById(R.id.collect);
		mShopCarImg = (ImageView) findViewById(R.id.shop_car);
		mShopNum = (TextView) findViewById(R.id.shop_num);

		mBodyContainer = (RelativeLayout) findViewById(R.id.containerBody);

		mBackBtn = findViewById(R.id.back_btn);
		
		mPageContainer = (RelativeLayout) findViewById(R.id.drag_layer);		
		
		mUserListContainer = findViewById(R.id.container_connect);		
		
		mNavGroup = (RadioGroup) findViewById(R.id.qe_main_nav_content);
		mNavIndicator = (ImageView) findViewById(R.id.qe_main_nav_indicator);
		
		initShareUserPage(mUserListContainer);		
	}
	
	private View mTimeBtn;
	private View mShareUserBack;
	private View mShareSendingScreen;
	class UserAvatar {
		ImageView headView = null;
		String userName = null;
		boolean empty = true;
	}
	
	private final static int MAX_USER_NUM = 5;
	private UserAvatar []mUserHeadArr = new UserAvatar[MAX_USER_NUM];
	private TextView mWaitHint;
	private AnimationSet mAnimInteract;
	
	private void initShareUserPage(View parent) {
		for(int i=0; i<mUserHeadArr.length; i++) {
			mUserHeadArr[i] = new UserAvatar();
		}
		
		mSendBtn = parent.findViewById(R.id.share_user_send);
		mTimeBtn = parent.findViewById(R.id.ll_timer);
		mShareUserBack = parent.findViewById(R.id.share_user_back_btn);
		mShareSendingScreen = parent.findViewById(R.id.share_user_sending);
		mShareSendingScreen.setVisibility(View.GONE);
		
		mUserHeadArr[0].headView = (ImageView)parent.findViewById(R.id.userhead1);
		mUserHeadArr[1].headView = (ImageView)parent.findViewById(R.id.userhead2);
		mUserHeadArr[2].headView = (ImageView)parent.findViewById(R.id.userhead3);
		mUserHeadArr[3].headView = (ImageView)parent.findViewById(R.id.userhead4);
		mUserHeadArr[4].headView = (ImageView)parent.findViewById(R.id.userhead5);
		
		mAnimInteract = (AnimationSet)AnimationUtils.loadAnimation(this, R.anim.tool_open);
		for(int i=0;i<mUserHeadArr.length; i++) {
			mUserHeadArr[i].headView.setOnClickListener(mAvatarClickListener);
			mUserHeadArr[i].headView.setOnTouchListener(mAvatarTouchListener);
		}
		
		mWaitContainer = parent.findViewById(R.id.container_wait);
		mWaitTouch = (RelativeLayout) mWaitContainer.findViewById(R.id.wait_touch);
		mRadar = (ProgressBar) mWaitContainer.findViewById(R.id.radar_rotate);
		mRadarBg = (ImageView)mWaitContainer.findViewById(R.id.radar_rotate_bg);		
		mQrCode = (ImageView) mWaitContainer.findViewById(R.id.iv_qr_image);
		
		mWaitHint = (TextView)mWaitContainer.findViewById(R.id.wait_connect_text);
		
		
		mTimeBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PriShareSendActivity.this, SetTimeActivity.class);
				startActivityForResult(intent, PriShareConstant.REQ_CODE_SET_EXPIRE_TIME);				
			}
		});
		
		mSendBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(mConnectedUser.size() > 0) {
					startSend(mConnectedUser.get(0));
				} else {
					Toast.makeText(mContext, "no user for sending", Toast.LENGTH_SHORT).show();
					return;
				}
				//mShowWaitingShare = false;
				//toggleShareUserView(mShowWaitingShare);
			}
		});
		

		
		mShareUserBack.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mShowWaitingShare = false;
				toggleShareUserView(mShowWaitingShare);				
			}
		});
		
		
		mQrCode.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					mInterHandForMain.setContinuePlay(true);
					mInterHandForMain.pressTransWifiInfo(1);
					mRadarBg.setImageResource(R.drawable.radar_cover);
				} else if(event.getAction() == MotionEvent.ACTION_UP) {
					mInterHandForMain.setContinuePlay(false);
					mRadarBg.setImageResource(R.drawable.radar_bg);
				}
				return true;
			}
		});
		
		
		mShareSendingScreen.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				showSendingProgress();			
			}
		});
	}
	
	
	private View.OnClickListener mAvatarClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!mEnableSend) {
				return;
			}
			for(int i=0; i<mUserHeadArr.length; i++) {
				if(!mUserHeadArr[i].empty && mUserHeadArr[i].headView == v) {
					if(mConnectedUser.contains(mUserHeadArr[i].userName)) {
						startSend(mUserHeadArr[i].userName);
					}else {
						Toast.makeText(mContext, R.string.user_head_no_user, Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		}
	};
	
	private View.OnTouchListener mAvatarTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			boolean findMatch =false;
			for(int i=0; i<mUserHeadArr.length; i++) {
				if(!mUserHeadArr[i].empty && mUserHeadArr[i].headView == v) {
					findMatch = true;
					break;
				}
			}
			if(findMatch) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						v.startAnimation(mAnimInteract);
						break;
					}
				}
			}
			return false;
		}
		
	};
	

	
	private final static int[]HEAD_VIEW = {
		R.drawable.face1,
		R.drawable.face2,
		R.drawable.face3,
		R.drawable.face4,
		R.drawable.face5
	};
	
	private int getHeadViewId(int index) {
		int i;
		if(index < 0) {
			i = 0;
		} else {
			i = index % HEAD_VIEW.length;
		}
		return HEAD_VIEW[i];
		
	}
	
	private int getConncetUserNum() {
		return mConnectedUser.size();
	}
	
	private void showUserHead(String userName) {
		for(int i=0; i<mUserHeadArr.length;i++) {
			if(mUserHeadArr[i].empty) {
				mUserHeadArr[i].headView.setImageResource(getHeadViewId(i));
				mUserHeadArr[i].userName = userName;
				mUserHeadArr[i].empty = false;
				mUserHeadArr[i].headView.startAnimation(getUserTurnOutAnim());
				Utils.playTurnOutVoice();
				break;
			}
		}
		
		refreshConnectUI(mInterHandForMain.getWifiType());
	}
	
	private void hideUserHead(String userName) {
		for(int i=0; i<mUserHeadArr.length;i++) {
			if(!mUserHeadArr[i].empty 
				&& userName.equals(mUserHeadArr[i].userName)) {
				mUserHeadArr[i].headView.setImageBitmap(null);
				mUserHeadArr[i].userName = null;
				mUserHeadArr[i].empty = true;
				break;
			}
		}
		refreshConnectUI(mInterHandForMain.getWifiType());
	}

	private static Boolean sMultiMode = false;

	public static boolean isMultiSelectMode() {
		return sMultiMode;
	}

	public void setMultiSelectMode(Boolean multiMode) {
		if (sMultiMode == multiMode) {
			return;
		}
		if (!multiMode) {
			mMultiMap.clear();
			mFileList.clear();
			resetCounts();
		}
		
		sMultiMode = multiMode;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}


	@Override
	public void onDestroy() {
		mInterHandForMain.onQuit();
		// recover mobile data
		if (mPrivateSwitcher != null) {
			mPrivateSwitcher.exitPrivateState();
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (mShowWaitingShare) {
			dismissSendWaiting();
		} else {
			if(mInterHandForMain.getWifiType() == WifiStateConstant.WIFI_TYPE_HOTSPOT) {
				showQuitDialog();
			} else {
				mInterHandForMain.onQuit();
				super.onBackPressed();
			}
		}
		/*else if (isMultiSelectMode()) {
			mShopNum.setText(String.format(getString(R.string.shop_count)));
			setMultiSelectMode(false);
		}*/
	}

	
	
	private void showQuitDialog() {		
		PriUtils.showQuitDialog(mContext,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int whichButton) {
				mInterHandForMain.onQuit();
				finish();
			}
		});
	}
	

	private CellItemBase[] mItemArr;
	private int mItemIdx = 0;
	private boolean mSendListPrepared = false;
	
	/**
	 * send start
	 * */
	private void sendMultipleItems(String user) {
		
		if(!mSendListPrepared) {			
			Set<CellItemBase> set = (Set<CellItemBase>) mMultiMap.keySet();
			Set<String> fileSet = (Set<String>) mFileList.keySet();
	
			mItemArr = new CellItemBase[set.size() + mFileList.size()];
			
			int i = 0;
			for (CellItemBase item : set) {
				mItemArr[i] = item;
				i++;
			}
	
			for (String item : fileSet) {
				mItemArr[i] = FilesCellItem.getFilesCellItem(item);
				i++;
			}
			mSendListPrepared  = true;
		} 
		mItemIdx = 0;
		sendRemainings(user);
	}
	
	/**
	 * continue send files
	 * */
	private void sendRemainings(String userName) {
		if (mItemArr != null && mItemIdx < mItemArr.length) {
			sendSingleItem(userName, mItemArr[mItemIdx]);
			onSendItem(mItemArr[mItemIdx]);
			mItemIdx++;			
		} else {
			onSendFinish();
		}
	}
	
	public void sendRemainingFiles(String userName) {
		sendRemainings(userName);
	}
	
	
	private void startSend(String username) {
		showSendingProgress();
		sendMultipleItems(username);
		
		//mShowWaitingShare = false;
		//toggleShareUserView(mShowWaitingShare);
	}	
	
	private void exitSend() {
		if(mSendListPrepared) {
			clearSelectState();
			mItemArr = null;
		}
		mSendListPrepared = false;				
	}
	


	private NdPagerAdapter mPagerAdapter;
	private ViewPager mPager;

	private PageView[] mPagerViews = new PageView[5];

	private class PageView {
		Boolean isLoaded = false;
		View v = null;
	}

	private View loadView(int index) {
		View v;
		if (index == PriShareConstant.GALLERY_INDEX) {
			v = mInflater.inflate(R.layout.qe_adapter_layout, null);
			initGalleryGrid(v);
		} else if (index == PriShareConstant.AUDIO_INDEX) {
			v = mInflater.inflate(R.layout.qe_adapter_layout, null);
			initAudioListView(v);
		} else if (index == PriShareConstant.APP_INDEX) {			
			v = mInflater.inflate(R.layout.qe_adapter_layout, null);
			initAppGrid(v);
		} else if (index == PriShareConstant.FILE_INDEX) {
			v = mInflater.inflate(R.layout.qe_adapter_layout, null);
			initFilesGridView(v);

		} else if (index == 4) {
			v = mInflater.inflate(R.layout.qe_history_message, null);
			mHistory.initHistoryView(v);
		} else {
			v = mInflater.inflate(R.layout.qe_not_support_layout, null);
		}
		return v;
	}

	private void setupPager() {
		for (int i = 0; i < mPagerViews.length; i++) {
			mPagerViews[i] = new PageView();
		}
		mPagerAdapter = new NdPagerAdapter(this);
		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(1);
	}

	private class NdPagerAdapter extends PagerAdapter {
		NdPagerAdapter(Activity activity) {
		}

		public final void destroyItem(ViewGroup group, int index, Object item) {
			((ViewPager) group).removeView((View) item);
		}

		public final void finishUpdate(ViewGroup group) {
		}

		public final int getCount() {
			return mPagerViews.length;
		}

		public final Object instantiateItem(ViewGroup parent, int index) {
			if (!mPagerViews[index].isLoaded) {
				mPagerViews[index].v = loadView(index);
				mPagerViews[index].isLoaded = true;
			}
			parent.addView(mPagerViews[index].v);
			return mPagerViews[index].v;
		}

		public final boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		public final void restoreState(Parcelable parcel, ClassLoader loader) {
		}

		public final Parcelable saveState() {
			return null;
		}

		public final void startUpdate(ViewGroup group) {
		}
	}

	
	private void handleUserLogin(String userName) {
		showUserHead(userName);
	}
	
	
	public class UIHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case MsgDefine.MAIN_UI_HANDLER_USER_LOGIN:
				handleUserLogin((String) msg.obj);
				break;
			case MsgDefine.MAIN_UI_HANDLER_RECV_FILE_FINISH: {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String text;
						if (msg.arg1 == 1) {
							text = msg.obj.toString()
									+ getString(R.string.app_installed);
						} else {
							text = msg.obj.toString()
									+ getString(R.string.app_install_fail);
						}
						Toast.makeText(mContext, text, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
				break;
				
			case MsgDefine.MAIN_UI_HANDLER_HOTSPOTCREATE:
				refreshConnectUI(msg.arg1);
				break;
				
			case MsgDefine.MAIN_UI_HANDLER_SEND_PROGRESS:
				onUpdateSendProgress(msg.arg1,msg.arg2);
				break;
			}

		}

	}


	@Override
	public void pagerChange(int whichPager) {
		mPager.setCurrentItem(whichPager);
	}

	@Override
	public void showConnStatus(String info) {
		
	}

	@Override
	public void userKickedOutAll() {
		VibratorController.getController(PriShareSendActivity.this).vibrate();		
	}

	@Override
	public void userKickedOutSingle(String name) {
		hideUserHead(name);
	}
	
	@Override
	public void setMultiCount() {
			int size = mMultiMap.size() + mFileList.size();
			mShopNum.setText(String.format(getString(R.string.tabbar_multi_send),
					Integer.valueOf(size)));		
	}

	@Override
	public void showQrCode() {
		String contentString = mInterHandForMain.getWifiInfoToTrans();
		if (!contentString.equals("")) {
			//show wifi info in QRCode;  image size:width * height = QR_IMAGE_SIZE * QR_IMAGE_SIZE
			try {
				Bitmap qrCodeBitmap = EncodingHandler.createQRCode(contentString, DimensionUtils.getQrCodeDimen());
				mQrCode.setImageBitmap(qrCodeBitmap);
			} catch (WriterException e) {
				e.printStackTrace();
			}			
		}
	}
}

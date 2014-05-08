package cn.nd.social.privategallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.common.FlyAnimation;
import cn.nd.social.common.ImageViewer;
import cn.nd.social.common.VibratorController;
import cn.nd.social.data.MsgDBHelper;
import cn.nd.social.data.MsgProviderSingleton;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.prishare.component.MainListViewAdapter;
import cn.nd.social.prishare.component.SetTimeActivity;
import cn.nd.social.prishare.component.UtilsForPrivacy;
import cn.nd.social.prishare.items.CellItemBase;
import cn.nd.social.prishare.items.ImageCellItem;
import cn.nd.social.sendfile.SendFilesActivity;
import cn.nd.social.sendfile.SendMutipleItem;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.BitmapUtils;

public class ImageThumbnailViewer extends Activity {

	private final static int COLUMN_COUNT = 2;
	private final static int TAB_COUNT = 2;
	private RadioGroup mNavGroup;

	private CustomRadioButton mPrivacyButton;
	private CustomRadioButton mGalleryButton;

	private ImageView mNavIndicator;
	private CustomViewPager mViewPager;
	private int mIndicatorWidth;
	public static int[] tabTitle = { R.string.private_photos,
			R.string.system_photos };
	private LayoutInflater mInflater;
	private ThumbPagerAdapter mAdapter;
	private int currIndLeft = 0;
	
	public static ImageThumbnailViewer sThumbnailViewer;

	private View mBack;
	private View mAddFile;
	private View mModify;
	private TextView mTitle;
	private Context mContext;


	private View mShopLayout;
	private ImageView mShopCarImg;
	private TextView mShopNum;

	private RelativeLayout mRootContainer;

	private int mScreenWidth;
	private int mColumnWidth;

	private final static int REQUEST_CODE_ADD_FILE = 100;
	private Handler mWorkerHandler;
	private HandlerThread mWorkerThread;

	private int mCurrPage = 0;
	
	//tangtaotao_20140404 add
	private boolean mIsMultiSelectMode = false;
	// ////////////////////////////////////////////////

	public ArrayList<String> mConnectedUser = new ArrayList<String>();


	// ////////////////////////////////////////////////
	/*
	 * receive private gallery database change
	 */
	private BroadcastReceiver mDataBaseChange = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(PrivateGalleryProvider.ACITON_PRI_GALLERY_DATA_CHANGE)) {
				asyncQueryFileList();
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		sThumbnailViewer = this;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;
		mProvider = PrivateGalleryProvider.getInstance();
		mInflater = LayoutInflater.from(this);
		
		setContentView(R.layout.private_thumbnail_viewer);
		setupViews();
		setupPager();
		registerEvent();

		initWorkThread();
		
		registerReceiver(mDataBaseChange, 
				new IntentFilter(PrivateGalleryProvider.ACITON_PRI_GALLERY_DATA_CHANGE));
	}

	
	@Override
	protected void onDestroy() {
		sThumbnailViewer = null;
		unregisterReceiver(mDataBaseChange);
		super.onDestroy();
	}


	public ArrayList<String> getmConnectedUser() {
		return mConnectedUser;
	}

	public void setmConnectedUser(ArrayList<String> connectedUser) {
		this.mConnectedUser = connectedUser;
	}

	
	private void initWorkThread() {
		mWorkerThread = new HandlerThread("ResourceWorkerThread");
		mWorkerThread.setPriority(1);
		mWorkerThread.start();
		mWorkerHandler = new Handler(mWorkerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case WORKER_MSG_QUERY_FILE_LIST:
					queryFileList();
					break;
				case WORKER_MSG_EXPORT_FILE:
					GalleryGridItem gridItem = (GalleryGridItem) msg.obj;
					Utils.copyFile(gridItem.getItemPath(), gridItem.getItemOrgPath());
					try {
						ContentValues localContentValues = new ContentValues();
						localContentValues.put("_data",
								gridItem.getItemOrgPath());
						localContentValues.put("description", "photo");
						localContentValues.put("mime_type", "image/png");
						ContentResolver localContentResolver = getContentResolver();
						Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
						localContentResolver.insert(localUri,
								localContentValues);
					} catch(Exception e) {
						
					}
					new File(gridItem.getItemPath()).delete();
					new File(gridItem.getItemThumbPath()).delete();
					mProvider.deleteFile(gridItem.getItemId());
					queryFileList();
					this.post(new GetImageThumbnails());
					break;
				case WORKER_MSG_ADD_IMAGE_FILE:
					addImageFile((Uri) msg.obj);
					break;
				case WORKER_MSG_IMPORT_TO_PRIVATE:
					ImageCellItem item = (ImageCellItem) msg.obj;
					addPrivateFile(item.getItemPath());
					break;
				}
			}
		};
	}

	private void registerEvent() {

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mCurrPage = position;

				if (mNavGroup != null && mNavGroup.getChildCount() > position) {
					mNavGroup.getChildAt(position).performClick();
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

					TranslateAnimation animation = new TranslateAnimation(
							currIndLeft, ((RadioButton) mNavGroup
									.getChildAt(checkedId)).getLeft(), 0f, 0f);
					animation.setInterpolator(new LinearInterpolator());
					animation.setDuration(100);
					animation.setFillAfter(true);

					mNavIndicator.startAnimation(animation);

					mViewPager.setCurrentItem(checkedId);

					// record the distance between current position and leftmost
					currIndLeft = mNavGroup.getChildAt(checkedId).getLeft();
				}
			}
		});
		
		
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mAddFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addFile("image/*", REQUEST_CODE_ADD_FILE);
			}
		});

		mModify.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// showPopupWindow(v.getLeft(),v.getBottom(),v);
				if (!checkHasSendItem()) {
					Toast.makeText(mContext, "no file for transmitting",
							Toast.LENGTH_SHORT).show();
					return;
				}

				Intent intent = new Intent(ImageThumbnailViewer.this,
						SetTimeActivity.class);
				startActivityForResult(intent, 300);
			}
		});

		mShopCarImg.setOnClickListener(new ShopCarListener());
	}

	private void setupViews() {
		mBack = findViewById(R.id.btn_title_back);
		mAddFile = findViewById(R.id.btn_title_add);
		mRootContainer = (RelativeLayout) findViewById(R.id.private_root_container);
		
		mShopLayout = findViewById(R.id.rl_shop);
		mShopCarImg = (ImageView) findViewById(R.id.shop_car);
		mShopNum = (TextView) findViewById(R.id.shop_num);

		mModify = findViewById(R.id.btn_title_modify);
		mTitle = (TextView) findViewById(R.id.tv_title);
		mTitle.setText(R.string.pri_image_label);

		
		mNavGroup = (RadioGroup) findViewById(R.id.rg_nav_content);
		mNavIndicator = (ImageView) findViewById(R.id.iv_nav_indicator);
		mViewPager = (CustomViewPager) findViewById(R.id.view_pager);

		mIndicatorWidth = mScreenWidth / TAB_COUNT;
		LayoutParams cursorParam = mNavIndicator.getLayoutParams();
		cursorParam.width = mIndicatorWidth;
		mNavIndicator.setLayoutParams(cursorParam);

		
		mModify.setVisibility(View.GONE);//tangtaotao_20140404 temporary remove
		
		initNavigationGroup();

		// ///////////////////////////////////////////

	}
	

	private void initNavigationGroup() {
		mNavGroup.removeAllViews();
		// for (int i = 0; i < tabTitle.length; i++) {
		mPrivacyButton = (CustomRadioButton) mInflater.inflate(
				R.layout.private_nav_radiogroup_item, null);
		mPrivacyButton.setId(0);
		mPrivacyButton.setText(tabTitle[0]);
		mPrivacyButton.setLayoutParams(new LayoutParams(mIndicatorWidth,
				LayoutParams.MATCH_PARENT));
		mNavGroup.addView(mPrivacyButton);

		// /////////////////////////////////////////////////

		mGalleryButton = (CustomRadioButton) mInflater.inflate(
				R.layout.private_nav_radiogroup_item, null);
		mGalleryButton.setId(1);
		mGalleryButton.setText(tabTitle[1]);
		mGalleryButton.setLayoutParams(new LayoutParams(mIndicatorWidth,
				LayoutParams.MATCH_PARENT));

		mNavGroup.addView(mGalleryButton);

		mNavGroup.check(0);
	}

	private void setupPager() {
		mColumnWidth = mScreenWidth / COLUMN_COUNT;

		for (int i = 0; i < mPageArray.length; i++) {
			mPageArray[i] = new PageView();
		}
		mAdapter = new ThumbPagerAdapter();
		mViewPager.setAdapter(mAdapter);
	}

	private View loadView(int index) {

		View v;
		if (index == 2) {
			v = mInflater.inflate(R.layout.qe_not_support_layout, null);
		} else {
			v = mInflater.inflate(R.layout.private_gallery_page, null);
		}
		if (index == 0) {
			initPrivateGridView(v);
		} else if (index == 1) {
			initGridView(v);
		} else if (index == 2) {

		}
		return v;
	}

	public class ThumbPagerAdapter extends PagerAdapter {

		public final void destroyItem(ViewGroup group, int index, Object item) {
			group.removeView((View) item);
		}

		public final void finishUpdate(ViewGroup group) {
		}

		public final int getCount() {
			return mPageArray.length;
		}

		public final Object instantiateItem(ViewGroup group, int index) {
			if (!mPageArray[index].isLoaded) {
				mPageArray[index].v = loadView(index);
				mPageArray[index].isLoaded = true;
			}
			group.addView(mPageArray[index].v);
			return mPageArray[index].v;
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

	private PageView[] mPageArray = new PageView[2];

	private class PageView {
		Boolean isLoaded = false;
		View v = null;
	}

	private GalleryAdapter mGalleryAdapter;
	private GalleryListAdapter mPrivateAdapter;
	private GridView mGrid;
	private GridView mPrivateGrid;
	PrivateGalleryProvider mProvider;

	private void initPrivateGridView(View parent) {
		mPrivateGrid = (GridView) parent.findViewById(R.id.gridid);
		mPrivateGrid.setNumColumns(COLUMN_COUNT);
		mPrivateGrid.setColumnWidth((mScreenWidth - 20) / COLUMN_COUNT);
		setPrivateViewItemEvent();
		mWorkerHandler.post(new GetPrivateImageThumbnails());
		showWaiting(parent);
	}

	private void initGridView(View parent) {
		mGrid = (GridView) parent.findViewById(R.id.gridid);
		mGrid.setNumColumns(COLUMN_COUNT);
		mGrid.setColumnWidth(mColumnWidth);
		setGalleryViewItemEvent();
		mWorkerHandler.post(new GetImageThumbnails());
		showWaiting(parent);
	}

	void setGalleryViewItemEvent() {
		mGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if(!mIsMultiSelectMode) {
					showImageGallery(mGalleryAdapter.getItem(position), position);
					return;
				}

				if (isMultiSelectMode()) {

					CheckBox cb = ((GalleryAdapter.ViewHolder) view.getTag()).chkBox;
					if (cb.isChecked()) {
						cb.setChecked(false);
						mMultiMap.remove(mGalleryAdapter.getItem(position));
						mGalleryAdapter.getItem(position).setSelected(false);
						setMultiCount(mMultiMap);
						return;
					} else {
						cb.setChecked(true);
						mMultiMap.put(mGalleryAdapter.getItem(position), view);
						mGalleryAdapter.getItem(position).setSelected(true);
						setMultiCount(mMultiMap);
					}

				} else {
					enterMutliSelectMode(view, position, 1);
				}

				FlyAnimation flyAnim = new FlyAnimation(mContext);
				flyAnim.startFly(view,mShopCarImg,mRootContainer);

			}

		});
		mGrid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				displayContexMenu(v, position);
				return false;
			}

		});
	}

	void setPrivateViewItemEvent() {
		mPrivateGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				GalleryListAdapter.GalleryViewHolder holder = (GalleryListAdapter.GalleryViewHolder) view
						.getTag();
				if(!mIsMultiSelectMode) {
					showImagePrivate(holder.item, position);
					return;
				}
				
				if (isMultiSelectMode()) {
					CheckBox cb = holder.checkBox;
					if (cb.isChecked()) {
						cb.setChecked(false);
						mPrivacyList.remove(position);
						holder.item.setSelected(false);
						setMultiCount(mPrivacyList);
						return;
					} else {
						cb.setChecked(true);
						mPrivacyList.put(position, holder.item);
						holder.item.setSelected(true);
						setMultiCount(mPrivacyList);
					}

				} else {
					enterMutliSelectMode(view, position, 0);
				}

				
				FlyAnimation fly = new FlyAnimation(mContext);
				fly.startFly(view, mShopCarImg, mRootContainer);
				

			}

		});
		mPrivateGrid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				displayPopup(v, position);
				return false;
			}

		});
	}

	/** multiple selects **/

	private static Boolean sMultiMode = false;
	Map<CellItemBase, View> mMultiMap = new HashMap<CellItemBase, View>();
	Map<Integer, GalleryGridItem> mPrivacyList = new HashMap<Integer, GalleryGridItem>();

	public static boolean isMultiSelectMode() {
		return sMultiMode;
	}

	public void setMultiSelectMode(Boolean multiMode) {
		if (sMultiMode == multiMode) {
			return;
		}
		if (!multiMode) {
			mViewPager.setScanScroll(true);
			mPrivacyButton.setIsCanClick(true);
			mGalleryButton.setIsCanClick(true);
			mMultiMap.clear();
		}
		if (multiMode) {
			mViewPager.setScanScroll(false);
			mPrivacyButton.setIsCanClick(false);
			mGalleryButton.setIsCanClick(false);
			VibratorController.getController(this).vibrate();
		}
		sMultiMode = multiMode;
		int visible = multiMode ? View.VISIBLE : View.GONE;

		if (mGrid != null && mGrid.getChildCount() > 0) {
			for (int i = 0; i < mGrid.getChildCount(); i++) {
				View v = mGrid.getChildAt(i);
				GalleryAdapter.ViewHolder holder = (GalleryAdapter.ViewHolder) v
						.getTag();
				CheckBox cb = holder.chkBox;
				cb.setVisibility(visible);
				cb.setChecked(false);
				holder.item.setSelected(false);
			}
			List<ImageCellItem> itemSet = mGalleryAdapter.getData();
			for (CellItemBase item : itemSet) {
				item.setSelected(false);
			}
		}

		if (mPrivateGrid != null && mPrivateGrid.getChildCount() > 0) {
			for (int i = 0; i < mPrivateGrid.getChildCount(); i++) {
				View v = mPrivateGrid.getChildAt(i);
				GalleryListAdapter.GalleryViewHolder holder = (GalleryListAdapter.GalleryViewHolder) v
						.getTag();
				CheckBox cb = holder.checkBox;
				cb.setVisibility(visible);
				cb.setChecked(false);
				holder.item.setSelected(false);
			}
		}

	}

	private void setMultiCount(Map multiMap) {
		int size = multiMap.size();
		if (size == 0) {
			setMultiSelectMode(false);
		}
		mShopNum.setText(String.format(getString(R.string.tabbar_multi_send),
				Integer.valueOf(size)));
	}

	private boolean checkHasSendItem() {
		Set<CellItemBase> set = (Set<CellItemBase>) mMultiMap.keySet();
		return (set.size() > 0 || mPrivacyList.size() > 0);
	}

	private void enterMutliSelectMode(View view, int pos, int which) { // pos is
																		// useless

		setMultiSelectMode(true);
		if (which == 0) {
			GalleryListAdapter.GalleryViewHolder holder = ((GalleryListAdapter.GalleryViewHolder) view
					.getTag());
			holder.checkBox.setVisibility(View.VISIBLE);
			holder.checkBox.setChecked(true);
			holder.item.setSelected(true);
			mPrivacyList.put(pos, holder.item);
			setMultiCount(mPrivacyList);
		} else if (which == 1) {
			GalleryAdapter.ViewHolder holder = ((GalleryAdapter.ViewHolder) view
					.getTag());
			holder.chkBox.setVisibility(View.VISIBLE);
			holder.chkBox.setChecked(true);
			holder.item.setSelected(true);
			mMultiMap.put(holder.item, view);
			setMultiCount(mMultiMap);
		}

	}



	/**
	 * Class Collection
	 * 
	 * @author xls
	 * 
	 */
	class ShopCarListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {

			final MainListViewAdapter listAdapter;
			if (mCurrPage == 0) {
				listAdapter = new MainListViewAdapter(mPrivacyList.values());
			} else {
				listAdapter = new MainListViewAdapter(mMultiMap.keySet());
			}

			AlertDialog.Builder fileDialog = new AlertDialog.Builder(mContext);
			fileDialog.setTitle(R.string.list);
			fileDialog.setAdapter(listAdapter, null);
			fileDialog.setPositiveButton(getString(R.string.all_delete),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							AlertDialog.Builder deleteDialog = new AlertDialog.Builder(
									mContext);
							deleteDialog.setTitle(R.string.all_delete);
							deleteDialog.setPositiveButton(R.string.sure,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											clearSelectState();
										}
									});

							deleteDialog.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {

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

					if (mCurrPage == 0) {
						GalleryGridItem gItem = (GalleryGridItem) v.getTag();
						gItem.setSelected(false);

						View view = gItem.v.get();

						if (view != null && view instanceof CheckBox) {
							((CheckBox) view).setChecked(false);
						}

						mPrivacyList.remove(gItem.getPos());

						if (mPrivacyList.size() == 0) {
							diaglog.dismiss();
						}
						setMultiCount(mPrivacyList);
						listAdapter.removeItemFromArr(gItem);
					} else {

						CellItemBase cellItem = (CellItemBase) v.getTag();
						cellItem.setSelected(false);

						GalleryAdapter.ViewHolder hold = (GalleryAdapter.ViewHolder) mMultiMap
								.get(cellItem).getTag();
						if (hold.item == cellItem) {
							hold.chkBox.setChecked(false);
						}
						View view = cellItem.v.get();
						if (view != null && view instanceof CheckBox) {
							((CheckBox) view).setChecked(false);
						}
						mMultiMap.remove(cellItem);

						if (mMultiMap.size() == 0) {
							diaglog.dismiss();
						}
						setMultiCount(mMultiMap);
						listAdapter.removeItemFromArr(cellItem);
					}

					listAdapter.notifyDataSetChanged();

				}
			});
			diaglog.show();

		}
	}


	// ////////////////////////////////////////////////////
	class GetImageThumbnails implements Runnable {

		@Override
		public void run() {
			if (!cn.nd.social.util.Utils.isExternalStorageMounted()) {
				mWorkerHandler.postDelayed(this, 5000);
			}
			ArrayList<ImageCellItem> itemList = new ArrayList<ImageCellItem>();
			Cursor cursor = getAllThumbNails(getApplicationContext());
			mPathList.clear();
			if (cursor != null) {
				cursor.moveToFirst();
				while (cursor.moveToNext()) {
					itemList.add(new ImageCellItem(cursor.getLong(0), null,
							cursor.getString(1)));
					mPathList.add(cursor.getString(1));
				}
				cursor.close();
			}
			final ArrayList<ImageCellItem> cellList = itemList;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideWaiting(mPageArray[1].v);
					mGalleryAdapter = new GalleryAdapter(mContext,
							R.layout.private_gallery_cell, cellList,
							mColumnWidth);
					mGrid.setAdapter(mGalleryAdapter);
				}
			});

		}

	}

	public static Cursor getAllThumbNails(Context context) {
		Cursor cursor = null;
		if (cn.nd.social.util.Utils.isExternalStorageMounted()) {
			ContentResolver cr = context.getContentResolver();
			String[] projection = { MediaStore.Images.Thumbnails._ID,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
					MediaStore.Images.Media.DISPLAY_NAME, };
			String selection = MediaStore.Images.Media.DATA + " like ? or "
					+ MediaStore.Images.Media.DATA + " like ?";
			String[] selectionArg = new String[] { "%dcim%", "%pic%" };
			cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, selection, selectionArg, null);
		}

		return cursor;
	}

	class GetPrivateImageThumbnails implements Runnable {

		@Override
		public void run() {
			if (!cn.nd.social.util.Utils.isExternalStorageMounted()) {
				mWorkerHandler.postDelayed(this, 5000);
			}
			final Cursor cursor = mProvider.getFileList();
			onChangePrivateCursor(cursor);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideWaiting(mPageArray[0].v);
					mPrivateAdapter = new GalleryListAdapter(
							ImageThumbnailViewer.this,
							R.layout.private_gallery_cell, mColumnWidth);
					mPrivateGrid.setAdapter(mPrivateAdapter);
					mPrivateAdapter.changeCursor(cursor);
				}
			});

		}

	}

	private void hideWaiting(View parent) {
		parent.findViewById(R.id.gridprogresslay).setVisibility(View.GONE);
	}

	private void showWaiting(View parent) {
		parent.findViewById(R.id.gridprogresslay).setVisibility(View.VISIBLE);
	}

	private void showImageGallery(ImageCellItem item, int pos) {
		Intent intent = new Intent(this, ImageViewerActivity.class);
		intent.putExtra("filename", item.getItemPath());
		Bundle extras = new Bundle();
		extras.putStringArrayList("file_list", mPathList);
		intent.putExtras(extras);
		intent.putExtra("file_index", pos);
		startActivity(intent);
	}

	
	//tangtaotao@ND_20140307 TODO: implement a strategy to slide the image file and destory file
	private void showImagePrivate2(GalleryGridItem gridItem, int pos) {
		Intent intent = new Intent(this, ImageViewerActivity.class);
		intent.putExtra("filename", gridItem.getItemPath());
		Bundle extras = new Bundle();
		extras.putStringArrayList("file_list", mPrivatePathList);
		intent.putExtras(extras);
		intent.putExtra("file_index", pos);
		startActivity(intent);
	}
	
	private void showImagePrivate(GalleryGridItem gridItem, int pos) {
		Intent intent = new Intent(this, ImageViewer.class);
		String str = gridItem.getItemPath();
		intent.putExtra("filename", str);
		int bookmark = gridItem.getItemBookmark();
		if(bookmark == 1) {
			Cursor cursor = null;
			try {
				
				MsgProviderSingleton msgHistoryControl = MsgProviderSingleton.getInstance();
				cursor = msgHistoryControl.queryCursor(str);
				Log.e("open img cursor 2:", "queryfileEnd:"+cursor.getCount());
				if(cursor != null && cursor.getCount() > 0) {
					Log.e("open img cursor 3:", "queryfileEnd:"+cursor.getCount());
					cursor.moveToFirst();
					int expireTime = cursor.getInt(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_EXPIRETIME));
					long staticTime = cursor.getLong(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_STATICTIME));
					int status = cursor.getInt(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_STATUS));
					if (expireTime != -1) {
						intent.putExtra("type", MsgDefine.GRANT_FILE_AUTO_DESTROY);
					}else{
						intent.putExtra("type", 0);
					}		
					intent.putExtra("static", staticTime/1000);
					intent.putExtra("value", expireTime);											
					intent.putExtra("status",status);
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(cursor != null) {
					cursor.close();
				}
			}
			
		}
		startActivity(intent);
	}

	private void displayContexMenu(final View view, final int position) {
		// popup menu is only supported by api level 11
		final PopupMenu popup = new PopupMenu(this, view);
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				ImageCellItem cell = (ImageCellItem) mGalleryAdapter
						.getItem(position);
				switch (item.getItemId()) {
				case R.id.import_to_private:
					popup.dismiss();
					Message msg = mWorkerHandler.obtainMessage();
					msg.what = WORKER_MSG_IMPORT_TO_PRIVATE;
					msg.obj = cell;
					mWorkerHandler.sendMessage(msg);
					break;
				case R.id.open:
					popup.dismiss();
					showImageGallery(cell, position);
					break;

				}
				return false;
			}
		});
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.system_gallery_item_menu, popup.getMenu());
		popup.show();
	}

	private void displayPopup(final View view, final int position) {
		// popup menu is only supported by api level 11
		final PopupMenu popup = new PopupMenu(this, view);
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Cursor cursor = (Cursor) mPrivateAdapter.getItem(position);
				GalleryGridItem gridItem = new GalleryGridItem(
						CellItemBase.IMAGE_TYPE, cursor);
				switch (item.getItemId()) {
				case R.id.export:
					popup.dismiss();
					Message msg = mWorkerHandler.obtainMessage();
					msg.what = WORKER_MSG_EXPORT_FILE;
					msg.obj = gridItem;
					mWorkerHandler.sendMessage(msg);
					break;

				case R.id.delete:
					popup.dismiss();
					new File(gridItem.getItemPath()).delete();
					new File(gridItem.getItemThumbPath()).delete();
					mProvider.deleteFile(gridItem.getItemId());
					asyncQueryFileList();
					break;
				case R.id.open:
					popup.dismiss();
					showImagePrivate(gridItem, position);
					break;

				}
				return false;
			}
		});
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.private_gallery_item_menu, popup.getMenu());
		popup.show();
	}

	private void addFile(String type, int requestCode) {
		Intent intent = new Intent("android.intent.action.GET_CONTENT");
		intent.setType(type);
		intent.addCategory("android.intent.category.OPENABLE");
		try {
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException localActivityNotFoundException) {
			Toast.makeText(this, R.string.private_add_file_error, 0).show();
		}
	}

	
	SendMutipleItem mSendItems = new SendMutipleItem(mMultiMap.size(), mConnectedUser, 0);



	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case REQUEST_CODE_ADD_FILE:
			Message msg = mWorkerHandler.obtainMessage(
					WORKER_MSG_ADD_IMAGE_FILE, data.getData());
			mWorkerHandler.sendMessage(msg);
			break;
		}
		
		if (requestCode == 300) {
			if (resultCode == RESULT_OK) {
				String expireTime = data
						.getStringExtra(SetTimeActivity.EXPIRE_TIME);
				mTimeToExpire = UtilsForPrivacy.getExpireSec(expireTime);
				startSendItems();
			}
		}
//		super.onActivityResult(requestCode, resultCode, data);
	}

	ArrayList<String> mPathList = new ArrayList<String>();

	ArrayList<String> mPrivatePathList = new ArrayList<String>();
	ArrayList<String> mPrivateOrgPathList = new ArrayList<String>();

	private void addImageFile(Uri uri) {
		String[] imgs = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(this, uri, imgs, null, null,
				null);
		Cursor cursor = loader.loadInBackground();
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String pathName = cursor.getString(index);
		if (mPrivateOrgPathList.contains(pathName)) {
			Toast.makeText(mContext, "file alreay exist", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		addPrivateFile(pathName);

	}

	private void addPrivateFile(String pathName) {
		String fileName = new File(pathName).getName();

		PrivateItemEntity item = new PrivateItemEntity();
		item.createUtc = System.currentTimeMillis();
		item.name = String.valueOf(item.createUtc / 1000);
		item.path = Utils.getPrivateFilePath() + "/" + item.name;
		item.thumbPath = Utils.getPrivateThumbPath() + "/" + item.name;
		item.mimeType = Utils.getMimeType(Utils.getExtensionName(fileName));
		item.type = Utils.getImageType(Utils.getExtensionName(fileName));
		item.orgName = fileName;
		item.orgPath = pathName;
			
		if (BitmapUtils.extractThumbnail(item.orgPath, item.thumbPath)) {
			// Utils.moveFile(item.orgPath, item.path);
			Utils.copyFile(item.orgPath, item.path);
			// delete original file
			try {
				String params[] = new String[] { item.orgPath };
				getContentResolver().delete(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						MediaStore.Images.Media.DATA + " LIKE ?", params);
				new File(item.orgPath).delete();
			} catch (Exception e) {

			}
			mProvider.addFile(item);
			mWorkerHandler.post(new GetImageThumbnails());
			queryFileList();
		} else {
			Toast.makeText(this, "add private file error", Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void onChangePrivateCursor(Cursor cursor) {
		mPrivatePathList.clear();
		mPrivateOrgPathList.clear();
		if (cursor == null || cursor.getCount() == 0) {
			return;
		}
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int index = cursor
					.getColumnIndexOrThrow(PrivateGalleryDBHelper.COLUMN_FILE_PATH);
			String loc = cursor.getString(index);
			mPrivatePathList.add(loc);
			index = cursor
					.getColumnIndexOrThrow(PrivateGalleryDBHelper.COLUMN_FILE_ORG_PATH);
			loc = cursor.getString(index);
			mPrivateOrgPathList.add(loc);
		}
	}

	private final static int WORKER_MSG_QUERY_FILE_LIST = 10000;
	private final static int WORKER_MSG_ADD_IMAGE_FILE = 10001;
	private final static int WORKER_MSG_EXPORT_FILE = 10002;
	private final static int WORKER_MSG_DELETE_FILE = 10003;
	private final static int WORKER_MSG_IMPORT_TO_PRIVATE = 10004;

	private void asyncQueryFileList() {
		mWorkerHandler.sendEmptyMessage(WORKER_MSG_QUERY_FILE_LIST);
	}

	/**
	 * should not call this in UI thread
	 * */
	private void queryFileList() {
		final Cursor listCursor = mProvider.getFileList();
		onChangePrivateCursor(listCursor);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mPrivateAdapter.changeCursor(listCursor);
			}
		});
	}
	
	
	/////////////////////////////////////////////////////
	/**
	 * send files
	 */
	

	public void clearSelectState() {
		if (mCurrPage == 0) {
			Collection<GalleryGridItem> itemSet = mPrivacyList.values();
			for (GalleryGridItem it : itemSet) {
				it.setSelected(false);
				View view = it.v.get();
				if (view != null && view instanceof CheckBox) {
					((CheckBox) view).setChecked(false);
				}
			}
			mPrivacyList.clear();
			setMultiCount(mPrivacyList);
		} else if (mCurrPage == 1) {
			Set<CellItemBase> itemSet = mMultiMap.keySet();
			for (CellItemBase it : itemSet) {
				it.setSelected(false);
				GalleryAdapter.ViewHolder hold = (GalleryAdapter.ViewHolder) mMultiMap
						.get(it).getTag();
				if (hold.item == it) {
					hold.chkBox.setChecked(false);
				}
			}
			mMultiMap.clear();
			setMultiCount(mMultiMap);
		}

	}
	

	private CellItemBase[] mItemArr;
	private int mTimeToExpire = 0;
	
	public CellItemBase[] getmItemArr() {
		return mItemArr;
	}

	public void setmItemArr(CellItemBase[] mItemArr) {
		this.mItemArr = mItemArr;
	}
	
	

	private int mItemIdx = 0;

	private void startSendItems() {
		
		if(mCurrPage == 0){
			Collection<GalleryGridItem> set = mPrivacyList.values();
			mItemArr = new CellItemBase[set.size()];
			mItemIdx = 0;
			int i = 0;
			for (GalleryGridItem item : set) {
				mItemArr[i] = (CellItemBase)item;
				i++;
			}

			sendRemainingFiles();
			
		}else{
			Set<CellItemBase> set = (Set<CellItemBase>) mMultiMap.keySet();
			
			mItemArr = new CellItemBase[set.size()];
			mItemIdx = 0;
			int i = 0;
			for (CellItemBase item : set) {
				mItemArr[i] = item;
				i++;
			}

			sendRemainingFiles();
		}

		
	}
	

	
	public void sendRemainingFiles() {
		if (mItemArr != null && mItemArr.length > mItemIdx) {		
			sendItemSkipActivity(mTimeToExpire);
		} else {
			mItemArr = null;
			mItemIdx = 0;
			// after finish sending,we should clear the items selected before
			clearSelectState();
		}
	}		
	
	
	
	public void sendItemSkipActivity(int timeExpire) {
		Intent intent = new Intent(ImageThumbnailViewer.this, SendFilesActivity.class);
		intent.putExtra(SendFilesActivity.KEY_SEND_FILENAME, "");
		intent.putExtra(SendFilesActivity.KEY_DATA_PACKET_TYPE,
								AudioDataPacker.TYPE_WIFI_PRIVATE_SHARE);	
		intent.putExtra(SendFilesActivity.SEND_SOURCE, 1);
		intent.putExtra(SetTimeActivity.EXPIRE_TIME, mTimeToExpire);
		startActivity(intent);
	}
	

}

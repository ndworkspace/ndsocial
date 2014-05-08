package cn.nd.social;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.account.CACallBack;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.business.MeetingNotifierReceiver;
import cn.nd.social.card.CardUtil;
import cn.nd.social.common.Feedback;
import cn.nd.social.common.PopMenu;
import cn.nd.social.common.PopMenuItem;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.data.CardOpenHelper;
import cn.nd.social.data.CardProvider;
import cn.nd.social.tobedelete.MgrMenuIconView;
import cn.nd.social.ui.controls.AnimateViewPager;
import cn.nd.social.ui.controls.AnimateViewPager.TransitionEffect;
import cn.nd.social.ui.controls.InfinitePagerAdapter;
import cn.nd.social.updater.UpdateEventReceiver;
import cn.nd.social.updater.UpdateInitiator;
import cn.nd.social.util.Utils;

import com.nd.voice.VoiceEndpoint;
import com.nd.voice.meetingroom.activity.RoomListFrame;
import com.nd.voice.meetingroom.manager.User;

public class TabLauncherUI extends FragmentActivity implements OnClickListener{

	private static final String TAG = "TabLauncherUI";
	public static long VOICE_UID;

	
	public static TabLauncherUI sInstance = null;

	private Context mContext;
	private AsyncQueryHandler mQueryHandler;

	ArrayList<Long> mNeedSyncAddArray = new ArrayList<Long>();
	ArrayList<Long> mNeedSyncDelArray = new ArrayList<Long>();

	long mCurrSyncId = -1;
	boolean mSyncPending = false;
	
	TextView mTitleText;
	
	public static final int MENU_ITEM_QUIT = 160;
	public static final int MENU_ITEM_CHECK_UPDATE = 161;
	public static final int MENU_ITEM_CONTACTS = 162;
	public static final int MENU_ITEM_ABOUT = 163;
	public static final int MENU_ITEM_MSG_LIST = 164;
	public static final int MENU_SHARE_THE_APP = 165;
	public static final int MENU_ITEM_FEEDBACK = 166;
	
	public static final int CARD_QUERY_TOKEN_UNSYNCHRONIZED = 101;
	public static final int CARD_UPDATE_TOKEN_NETSYNC = 102;
	public static final int CARD_UPDATE_TOKEN_UNSYNCHRONIZED = 103;

	public static final int EVNET_NET_SYNC_ADD_FRIEND = 1000;
	public static final int EVNET_NET_SYNC_DEL_FRIEND = 1001;
	
	public AnimationSet mAnimInteract;
	
	////////////////////////////////////////
	public MgrMenuIconView mMenu;
	
	private TabActionBar mActionBar; 
	public boolean isMenuShow;
	FrameLayout mParentView;
	FrameLayout.LayoutParams mParentViewLayoutParams;
	
	boolean isAnmation;
		
	private BroadcastReceiver mMeetingNotify;
	private BroadcastReceiver mUpdateEvent;
	
	///////////////////////////////////////
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_launcher);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		/*auto update*/
		UpdateInitiator.checkAutoUpdate(UpdateInitiator.UpdateType.AUTO_CHECK);
		
		sInstance = this;
		mContext = this;
		
		mQueryHandler = new CardQueryHandler(getContentResolver());	

		mAnimInteract = (AnimationSet)AnimationUtils.loadAnimation(this, R.anim.tool_open);
		
		setupViews();

		registerListener();
		
	}
	
	
	private void setupViews() {	
		createFragments();
		selectTab(DEFAULT_TAB, true);
	}

	private void registerListener(){
		
		registerNetSyncObserver();
		
		mUpdateEvent = new UpdateEventReceiver(this);
		registerReceiver(mUpdateEvent, UpdateEventReceiver.getIntentFilter());		
		
		mMeetingNotify = new MeetingNotifierReceiver(this);		
		registerReceiver(mMeetingNotify, MeetingNotifierReceiver.getIntentFilter());
	}

	
	public interface TitleEventCallBack {		
		public void onRightBtnEvent(View v);
		public boolean onBackPressEvent();
	}
	

	////////////////////////////////////////////////////////

	
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {		
/*		if (Utils.isMyCardCreated()) {
			// update my card if necessary
			if (!Utils.getAppSharedPrefs().getBoolean(
					Utils.MY_CARD_UPLOAD_FLAG, false)
					&& CloundServer.getInstance().isNetworkReady()) { 
				CardUtil.CardData cardInfo = CardUtil.getMyCardInfo();
				String jsonStr = CardCodec.buildCardJsonString(cardInfo);
				if (jsonStr != null) {
					CloundServer.getInstance().getCARequest().updateCardInfo(jsonStr,
							mHandler);
				}
			}
		}*/
		super.onResume();
	}

	@Override
	protected void onStop() { 
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		//unregisterNetSyncObserver();
		unregisterReceiver(mUpdateEvent);
		unregisterReceiver(mMeetingNotify);
		unregisterNetSyncObserver();
		super.onDestroy();
	}


	private long mLastBackPressTime = 0;
	
	private boolean checkDoubleTapExit() {
		boolean flag = false;
		long currTime = System.currentTimeMillis();
		if(mLastBackPressTime != 0) {			
			if(currTime - mLastBackPressTime < 1000) {
				flag = true;
			}
		}
		mLastBackPressTime = currTime;
		return flag;
	}
	@Override
	public void onBackPressed() {
		if(isMenuShow){
			hide();
			return;
		}
		if(checkDoubleTapExit()) {
			exit();
		} else {
			Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
			//moveTaskToBack(false);
		}
	}

	public void exit() {
		finish();
		SocialApplication.getAppInstance().exit();
	}
	
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAConstant.APP_EVENT_UPLOAD_MY_CARD:
				if (msg.arg1 == CACallBack.RET_SUCCESS) {
					SharedPreferences.Editor editor = Utils.getAppSharedPrefs()
							.edit();
					editor.putBoolean(Utils.MY_CARD_UPLOAD_FLAG, true);
					editor.commit();
				} else {
					if (msg.obj instanceof String) {
						Log.e(TAG, "upload card error" + (String) msg.obj);
					} else {
						Log.e(TAG, "upload card error");
					}
				}
				break;
			case CAConstant.APP_EVENT_LOGOUT_RSP:
				dismissProgressDialog();
				if (msg.arg1 != 0) {
					Toast.makeText(mContext, "network error on logout",
							Toast.LENGTH_SHORT).show();
				}
				exit();
				break;

			case CAConstant.APP_EVENT_ADD_FRIEND_RSP:
				mSyncPending = false;
				if (msg.arg1 == 0) {// success
					Log.e(TAG, "mCurrSyncId:" + mCurrSyncId);
					if (mNeedSyncAddArray.size() > 0) {
						long syncUid = mNeedSyncAddArray.remove(0);
						updateSyncState(syncUid, 1);
						Log.e(TAG, "add friend, id:" + syncUid +" success");
						netSyncAddFriend();
					}
					CloundServer.getInstance().getFriendInfoAsync();
				}
				break;
				
			case CAConstant.APP_EVENT_DEL_FRIEND_RSP:
				break;
				
			case EVNET_NET_SYNC_ADD_FRIEND:
				netSyncAddFriend();
				break;
			case EVNET_NET_SYNC_DEL_FRIEND:
				// netSyncDelFriend();
				break;

			}
			super.handleMessage(msg);
		}

	};

	
	public final void updateSyncState(long uid, int status) {
		ContentValues values = new ContentValues();
		values.put(CardOpenHelper.COLUMN_NET_SYNC, status);
		String whereStr= ""+ CardOpenHelper.COLUMN_USER_ID+ "="+ uid;
		getContentResolver().update(
				Uri.parse(CardProvider.CONTENT_URI + "/" + uid), values, whereStr,
				null);
	}


	private void netSyncAddFriend() {
		if (mSyncPending) {
			return;
		}
		if (mNeedSyncAddArray.size() > 0) {
			mSyncPending = true;
			mCurrSyncId = mNeedSyncAddArray.get(0);
			CloundServer.getInstance().getCARequest().addFriend(mCurrSyncId, 0, null,
					mHandler);
		}

	}

	private void netSyncDelFriend(long userId) {
		CloundServer.getInstance().getCARequest().delFriend(userId, 0, mHandler);
	}

	private final ContentObserver mFriendChangeObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfUpdate) {
			mQueryHandler.startQuery(CARD_QUERY_TOKEN_UNSYNCHRONIZED, null,
					CardProvider.CONTENT_URI,
					CardUtil.CARD_LIST_UNSYNC_PROJECTION,
					CardOpenHelper.COLUMN_NET_SYNC + "=0", null, null);
		}
	};

	private void registerNetSyncObserver() {
		ContentResolver cr = getContentResolver();
		Uri uri = CardProvider.CONTENT_URI;
		cr.registerContentObserver(uri, false, mFriendChangeObserver);
		mQueryHandler.startQuery(CARD_QUERY_TOKEN_UNSYNCHRONIZED, null,
				CardProvider.CONTENT_URI, CardUtil.CARD_LIST_UNSYNC_PROJECTION,
				CardOpenHelper.COLUMN_NET_SYNC + "=0", null, null);
	}

	private void unregisterNetSyncObserver() {
		ContentResolver cr = getContentResolver();
		cr.unregisterContentObserver(mFriendChangeObserver);
	}

	public final class CardQueryHandler extends AsyncQueryHandler {
		public CardQueryHandler(ContentResolver contentResolver) {
			super(contentResolver);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor == null || cursor.getCount() == 0) {
				if(cursor != null) {
					cursor.close();
				}
				return;
			}
			switch (token) {
			case CARD_QUERY_TOKEN_UNSYNCHRONIZED:
				while (cursor.moveToNext()) {
					long uid = cursor.getLong(1);
					int index = cursor.getColumnIndex((CardOpenHelper.COLUMN_NET_SYNC));
					int netSync = cursor.getInt(index);
					if (uid != Utils.INVALID_USER_ID && netSync != 1) {
						mNeedSyncAddArray.add(uid);
					}
				}
				Message msg = mHandler.obtainMessage();
				msg.what = EVNET_NET_SYNC_ADD_FRIEND;
				mHandler.sendMessage(msg);
				break;
				
			default:
				break;
			}
			cursor.close();
		}
	}

	private TabPriShare mPriShareTab;
	private TabIM mIMTab;
	private RoomListFrame mBusinessTab;
	
	private AnimateViewPager mTabPager;
	
	private static final TabState DEFAULT_TAB = TabState.PRI_SHARE_TAB;
	
	private TabState mCurrentTab = null;
	private TabState oldTab = null;
	
	private void createFragments() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		mTabPager = (AnimateViewPager) findViewById(R.id.tab_pager);
		
		//set the cube effect of viewpager transition
		mTabPager.setTransitionEffect(TransitionEffect.CubeOut);
		TabPagerAdapter adapter = new TabPagerAdapter();
		PagerAdapter wrappedAdapter = new InfinitePagerAdapter(adapter);
		mTabPager.setAdapter(wrappedAdapter);
		
		//mTabPager.setAdapter(adapter);
		//mTabPager.setOffscreenPageLimit(2);
		
		mTabPager.setOnPageChangeListener(new TabPageChangeListener());
		mPriShareTab = (TabPriShare) fragmentManager.findFragmentByTag("TabPriShare");
		mIMTab = (TabIM) fragmentManager.findFragmentByTag("TabIM");
		mBusinessTab = (RoomListFrame) fragmentManager.findFragmentByTag("TabBusiness");
		if (mPriShareTab == null) {
			mPriShareTab = new TabPriShare();
			transaction.add(R.id.tab_pager, mPriShareTab, "TabPriShare");
		}
		
		if (mIMTab == null) {
			mIMTab = new TabIM();
			transaction.add(R.id.tab_pager, mIMTab, "TabIM");
				
		}
		if (mBusinessTab == null) {
			mBusinessTab = new RoomListFrame();
			transaction.add(R.id.tab_pager, mBusinessTab, "TabBusiness");
		}
		
		transaction.hide(mPriShareTab);
		transaction.hide(mIMTab);
		transaction.hide(mBusinessTab);
		
		transaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
	}
	
	public interface InvokeOptionsMenu {
		void onInvokeOptionsMenu(Menu menu,boolean isCurrent);
		boolean onItemSelected(MenuItem item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mCurrentTab == TabState.IM_TAB) {
			mIMTab.onInvokeOptionsMenu(menu,true);
		} else if(mCurrentTab == TabState.PRI_SHARE_TAB) {
			mPriShareTab.onInvokeOptionsMenu(menu,true);
		}else{
//			mBusinessTab.onInvokeOptionsMenu(menu,true);
		}
		return false;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			
		if(mPriShareTab.onItemSelected(item)) {
			return true;
		}
		if(mIMTab.onItemSelected(item)) {
			return true;
		}			 
//		if(mBusinessTab.onItemSelected(item)) {
//			return true;
//		}
		
		onMenuSelected(item.getItemId());
		return true;
	}

	public void showDefaultActionMenu(View v) {
		//cn.nd.social.account.usermanager.UserManager.sendConferenceInvitation("xxmeet", 1, "20140920", "memmo",new long[]{1,2021,3} );
		if(isMenuShow){
			hide();
		}else{
			show();
		}
		
//		showPopupMenu(v);
	}
	
	
	private void showPopupMenu(View title) {
		//View title = findViewById(R.id.title);
		final PopMenu menu = new PopMenu(this);	
		menu.addItem(new PopMenuItem(MENU_ITEM_CHECK_UPDATE, getString(R.string.check_update), 0));
		menu.addItem(new PopMenuItem(MENU_ITEM_MSG_LIST, getString(R.string.msg_list), 0));
		menu.addItem(new PopMenuItem(MENU_SHARE_THE_APP, getString(R.string.share_the_app), 0));
		//menu.addItem(new PopMenuItem(MENU_ITEM_ABOUT, getString(R.string.about), 0));
		menu.addItem(new PopMenuItem(MENU_ITEM_FEEDBACK, getString(R.string.feedback), 0));		
		menu.addItem(new PopMenuItem(MENU_ITEM_QUIT, getString(R.string.exit_application), 0));
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {				
				PopMenuItem item = (PopMenuItem)parent.getItemAtPosition(position);
				onMenuSelected(item.getItemId());				
				menu.dismiss();				
			}
		});
		menu.showAsDropDown(title);
	}

	private void onMenuSelected(int menuId) {
		switch(menuId) {
		case MENU_ITEM_QUIT:
			//TODO: logout first
			exit();
			break;
			
		case MENU_ITEM_CHECK_UPDATE:
			UpdateInitiator.checkAutoUpdate(UpdateInitiator.UpdateType.USER_TRIGGER);
			break;
			
		case MENU_ITEM_MSG_LIST:
			MessageActivity.enterMessageList(mContext);
			break;
			
		case MENU_SHARE_THE_APP:
			new ShareThisApp(TabLauncherUI.this).share();
			break;
		case MENU_ITEM_FEEDBACK:
			launchFeedback();
			break;
		default:
				break;
		}
	}
	
	public void launchFeedback() {
		Intent intent = new Intent(this,Feedback.class);
		startActivity(intent);
	}
	

	private ProgressDialog mProgress;
	
	private void showProgressDialog() {
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getText(R.string.wait_hint));
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(false);
		mProgress.show();
	}

	private void dismissProgressDialog() {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}
	}
	
	private Fragment getFragment(int tabIndex) {
		Fragment fragment;
		if (tabIndex == TabState.PRI_SHARE_TAB.ordinal()) {
			fragment = mPriShareTab;
		} else if (tabIndex == TabState.BUSINESS_TAB.ordinal()) {
			fragment = mBusinessTab;
		} else if(tabIndex == TabState.IM_TAB.ordinal()) {
			fragment = mIMTab;
		} else {
			throw new IllegalArgumentException("position: " + tabIndex);
		}
		return fragment;
	}
	
	private void selectTab(TabState tabState, boolean updateFragment) {
		if (tabState == null) {
			throw new NullPointerException();
		}
		if (tabState == mCurrentTab) { // critical, avoid recursive call
			return;
		}
		
		mCurrentTab = tabState;
		
		if (updateFragment) {
			//mTabPager.setCurrentItem(tabState.ordinal(), true);
			mTabPager.setCurrentItem(tabState.ordinal());
		}
        onTabSpecifiedAction(tabState);
    }
	
	private void onTabSpecifiedAction(TabState tabState) {

	}
	
	public static enum TabState {
		//TODO: NDConfig.FEATRUE_SHOW_IM
		IM_TAB ,PRI_SHARE_TAB,BUSINESS_TAB ;

		public static int getCount() {			
			return 3;
		}
		public static TabState fromInt(int index) {
			if (index == PRI_SHARE_TAB.ordinal())
				return PRI_SHARE_TAB;
			else if (index == BUSINESS_TAB.ordinal())
				return BUSINESS_TAB;
			else if(index == IM_TAB.ordinal())
				return IM_TAB;
			throw new IllegalArgumentException("Invalid value: " + index);
		}
	}

	private class TabPageChangeListener implements
			ViewPager.OnPageChangeListener {

		public void onPageScrollStateChanged(int paramInt) {
			if(paramInt == ViewPager.SCROLL_STATE_IDLE){
				if(mCurrentTab != oldTab){
					if(oldTab != null){
						Fragment oldFragment = getFragment(oldTab.ordinal());
						if(oldFragment instanceof TabFramentChangeListener){
							((TabFramentChangeListener)oldFragment).onFramentViewHide();
						}
					}
					oldTab = mCurrentTab;
				}
				Fragment currentFragment = getFragment(mCurrentTab.ordinal());
				if(currentFragment instanceof TabFramentChangeListener){
					((TabFramentChangeListener)currentFragment).onFramentViewShow();
				}
//				mPriShareTab = (TabPriShare) fragmentManager.findFragmentByTag("TabPriShare");
//				mIMTab = (TabIM) fragmentManager.findFragmentByTag("TabIM");
//				mBusinessTab = (RoomListFrame) fragmentManager.findFragmentByTag("TabBusiness");

			}
		}

		public void onPageScrolled(int paramInt1, float paramFloat,
				int paramInt2) {
		}

		public void onPageSelected(int index) {
			index = index % TabState.getCount();
			TabState tab = TabState.fromInt(index);
			selectTab(tab, false);
		}
	}
	
	
	private class TabPagerAdapter extends PagerAdapter {

		protected FragmentTransaction mCurTransaction = null;
		protected final FragmentManager mFragmentManager = getSupportFragmentManager();
		public TabPagerAdapter() {
		}
		@Override
		public int getCount() {
			return TabState.getCount();
		}

		@Override
		public int getItemPosition(Object obj) {
			int pos;
			if (obj == mPriShareTab)
				pos = TabState.PRI_SHARE_TAB.ordinal();
			else if (obj == mBusinessTab)
				pos = TabState.BUSINESS_TAB.ordinal();
			else if (obj == mIMTab)
				pos = TabState.IM_TAB.ordinal();
			else
				pos = -2;
			return pos;
		}

		@Override
		public Object instantiateItem(View view, int index) {
			if (mCurTransaction == null) {
				mCurTransaction = mFragmentManager.beginTransaction();
			}
			Fragment fragement = getFragment(index);
			mCurTransaction.show(fragement);
			mTabPager.setObjectForPosition(fragement, index);
			return fragement;
		}
		@Override
		public void destroyItem(View paramView, int paramInt, Object obj) {
			if (mCurTransaction == null)
				mCurTransaction = mFragmentManager.beginTransaction();
			mCurTransaction.hide((Fragment) obj);
		}

		@Override
		public void finishUpdate(View v) {
			if (mCurTransaction != null) {
				mCurTransaction.commitAllowingStateLoss();
				mCurTransaction = null;
				mFragmentManager.executePendingTransactions();
			}
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return ((Fragment) obj).getView() == view;
		}

		@Override
		public void restoreState(Parcelable parcel, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View v) {
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mPriShareTab.onFoucsChange(hasFocus);
	}
	

	public void addMenuViewToContent(){
		if(mParentView == null){
			mParentView = new FrameLayout(mContext);
	        
	        LinearLayout mainLayout = new LinearLayout(mContext);
	        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
	        mainLayout.setOrientation(LinearLayout.VERTICAL);
	        mParentView.setOnClickListener(this);
	        
	        mParentView.addView(mainLayout, mainLayoutParams);
	        
	        LinearLayout topLayout = new LinearLayout(mContext);
	        LinearLayout.LayoutParams topLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
	        topLayoutParams.weight = 1;
	        mainLayout.addView(topLayout, topLayoutParams);
	        
	        
	        //mMenu = new MgrMenuIconView(mContext);
	        mActionBar = new TabActionBar(this);
	        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        layoutParams.gravity = Gravity.BOTTOM | Gravity.CLIP_VERTICAL | Gravity.LEFT;
	        mainLayout.addView(mActionBar.getActionBarView(), layoutParams);
	        //tangtaotao_20140409 use slide bar instead
	        //mainLayout.addView(mMenu, layoutParams);
	        mParentView.setVisibility(View.GONE);
	        
	        //tangtaotao_20140409 use slide bar instead
	        //addmMenuListen();
	        
	        mParentViewLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		}
		if(mParentView.getParent() == null){
			addContentView(mParentView, mParentViewLayoutParams);
		}
	}
	
	
	public void removeMenuView(){
		if(mParentView.getParent() != null){
			((ViewGroup)mParentView.getParent()).removeView(mParentView);
		}
		
	}
	
	//tangtaotao_20140409 use slide bar instead

/*	private void addmMenuListen(){
		mMenu.setmListener(new MgrMenuListener() {
			
			@Override
			public void onMgrTreasure() {
				hide();
				//Intent intent = new Intent(TabLauncherUI.this,ImageThumbnailViewer.class);
				Intent intent = new Intent(TabLauncherUI.this,PrivateTreasure.class);
				startActivity(intent);
			}
			
			@Override
			public void onMgrMessageHistory() {
				hide();
//				isMenuShow = false;
				hide();
				MessageActivity.enterMessageList(TabLauncherUI.this);
				
			}
			
			@Override
			public void onMgrConnect() {
				hide();
				int currentItem = mTabPager.getCurrentItem() % 1000;
				int postion = currentItem%3;
				
				if(postion == 1){
					mTabPager.setCurrentItem(currentItem - 1,true);
				}else if(postion == 2){
					mTabPager.setCurrentItem(currentItem + 1,true);
				}
//				isMenuShow = false;
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onMgrCardShare() {
				hide();
//				isMenuShow = false;
				hide();
				Intent intent = new Intent(TabLauncherUI.this, CardListActivity.class);
				startActivity(intent);
			}
		});
	}*/
	
	/**
     * Shows view
     */
    public void show(){
    	if(isMenuShow){
    		return;
    	}
    	isMenuShow = true;
    	this.addMenuViewToContent();
        final Animation in = AnimationUtils.loadAnimation(this.mContext, R.anim.push_bottom_in);
//        in.setDuration(500);
//        mMenu.setVisibility(View.VISIBLE);
        mParentView.setVisibility(View.VISIBLE);
        mParentView.startAnimation(in);
        in.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				mParentView.setEnabled(false);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				 mParentView.setBackgroundColor(Color.BLACK);
			     mParentView.getBackground().setAlpha(50);
			     mParentView.setEnabled(true);
			}
		});
    }

    /**
     * Hides view
     */
    public void hide() {
    	if(!isMenuShow){
    		return;
    	}
    	isMenuShow = false;
    	final Animation out =  AnimationUtils.loadAnimation(this.mContext, R.anim.push_bottom_out);
//    	out.setDuration(500);
    	out.setFillAfter(true);
    	mParentView.setVisibility(View.GONE);
    	out.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				mParentView.setEnabled(false);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				mParentView.setBackgroundColor(Color.TRANSPARENT);
			     mParentView.getBackground().setAlpha(100);
				removeMenuView();
				mParentView.setEnabled(true);
				//tangtaotao_20140409 use slide bar
				if(mActionBar != null) {
					mActionBar.refresh();
				}
				
			}
		});
    	mParentView.startAnimation(out);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(isMenuShow){
			hide();
		}
	}
	
	public void disablePaging() {
		mTabPager.setPagingEnabled(false);
	}
	
	public void enablePaging() {
		mTabPager.setPagingEnabled(true);
	}
}

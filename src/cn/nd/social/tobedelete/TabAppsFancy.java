package cn.nd.social.tobedelete;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.TabLauncherUI.InvokeOptionsMenu;
import cn.nd.social.TabLauncherUI.TitleEventCallBack;
import cn.nd.social.card.TabMyCard;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.syncbrowsing.ui.SyncBrowserActivity;
import cn.nd.social.util.Utils;

import com.nd.voice.chatroom.EnterRoomActivity;

public class TabAppsFancy extends Fragment implements TitleEventCallBack,InvokeOptionsMenu {
	LayoutInflater mInflater;

	private TabLauncherUI mActivity;
	private View mRootView;

	private View mShareCard,mPrivateGallery,mPrivateShare,mMultiTalk;
	private View mSyncRead, mShareThisApp;
	private AnimationSet mAnimSet;
	
	
	private TextView mTitleText;
	private View mActionBtn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (TabLauncherUI) getActivity();
		mRootView = inflater.inflate(R.layout.main_tab_apps_fancy, container, false);
		mInflater = LayoutInflater.from(mActivity);
		setupViews();
		registerEvent();
		//(AnimationSet)AnimationUtils.loadAnimation(mActivity, R.anim.tool_open);
		mAnimSet = mActivity.mAnimInteract;
		return mRootView;
	}

	private void setupViews() {
		mShareCard = mRootView.findViewById(R.id.tool_share_card);
		mPrivateGallery = mRootView.findViewById(R.id.tool_private_gallery);
		mPrivateShare = mRootView.findViewById(R.id.tool_private_share);
		mMultiTalk = mRootView.findViewById(R.id.tool_multi_talk);
		mSyncRead = mRootView.findViewById(R.id.tool_sync_browse);
		mShareThisApp = mRootView.findViewById(R.id.tool_share_this);
		mShareThisApp.setVisibility(View.GONE);
		
		mShareCard.setVisibility(View.GONE);
		//mPrivateGallery.setVisibility(View.GONE);
		//mRootView.findViewById(R.id.first_row).setVisibility(View.GONE);
		
    	mTitleText = (TextView)mRootView.findViewById(R.id.main_title);
    	mActionBtn = mRootView.findViewById(R.id.right_btn);    	
    	mTitleText.setText(R.string.title_trans_affection);
	}
	
	public class ItemTouchListener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					v.startAnimation(mAnimSet);
					break;
				}
/*				case MotionEvent.ACTION_UP: {
					v.clearAnimation();
					break;
				}*/
			}
			return false;
		}
		
	}
	
	private ItemTouchListener mItemTouchListener = new ItemTouchListener();
	private void registerTouchEvent() {
		mShareCard.setOnTouchListener(mItemTouchListener);
		mPrivateGallery.setOnTouchListener(mItemTouchListener);
		mPrivateShare.setOnTouchListener(mItemTouchListener);
		mSyncRead.setOnTouchListener(mItemTouchListener);
		mMultiTalk.setOnTouchListener(mItemTouchListener);
		mShareThisApp.setOnTouchListener(mItemTouchListener);
	}
	
	private void registerEvent() {
		registerTouchEvent();
		
    	mActionBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mActivity.showDefaultActionMenu((View)v.getParent());
			}
		});
		
		mShareCard.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(mActivity, AppShareCard.class);
				Intent intent = new Intent(mActivity, TabMyCard.class);
				intent.putExtra(TabMyCard.LAUNCH_FROM, 1);
				startActivity(intent);
			}
		});
		
		mPrivateGallery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO: tony mail
				if(true) {
					Toast.makeText(mActivity, R.string.toymail_not_support, Toast.LENGTH_SHORT).show();
					return;
				}
/*				Intent intent = new Intent(mActivity,
						ImageThumbnailViewer.class);
				startActivity(intent);*/
			}
		});
		mPrivateShare.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

				if(isTetherSupported()) {					
					Intent intent = new Intent(mActivity,cn.nd.social.prishare.PriShareSendActivity.class);
					startActivity(intent);
				}
			}
		});
		
		mMultiTalk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				Intent intent = new Intent(mActivity, EnterRoomActivity.class);
				startActivity(intent);
			}
		});
		
		mSyncRead.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				if(isTetherSupported()) {
					Intent intent = new Intent(mActivity, SyncBrowserActivity.class);
					intent.putExtra(Utils.SYNC_READ_HOST_KEY, true);
					startActivity(intent);
				}
			}
		});
		
		mShareThisApp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				new ShareThisApp(mActivity).share();
			}
		});
	}


	
	private boolean isTetherSupported() {
		boolean support = true;
		ConnectivityManager mgr = (ConnectivityManager)mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class<?> cmClass = mgr.getClass();
		Class<?>[] argClasses = null;
		Object[] argObject = null;
		try {
			Method method = cmClass.getMethod("isTetheringSupported", argClasses);
			support = (Boolean) method.invoke(mgr, argObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(!support) {
			Toast.makeText(mActivity, "tether disabled", Toast.LENGTH_SHORT).show();
		}
		return support;
	}
	

	@Override
	public void onRightBtnEvent(View v) {
		mActivity.showDefaultActionMenu((View)mActionBtn.getParent());
	}

	@Override
	public boolean onBackPressEvent() {
		return false;
	}

	@Override
	public void onInvokeOptionsMenu(Menu menu, boolean isCurrent) {
		if(isCurrent) {
			mActivity.showDefaultActionMenu((View)mActionBtn.getParent());
		}		
	}

	@Override
	public boolean onItemSelected(MenuItem item) {
		return false;
	}

}
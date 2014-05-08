package cn.nd.social;

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
import android.view.animation.AnimationSet;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.TabLauncherUI.InvokeOptionsMenu;
import cn.nd.social.TabLauncherUI.TitleEventCallBack;
import cn.nd.social.card.TabMyCard;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.syncbrowsing.ui.SyncBrowserActivity;
import cn.nd.social.util.Utils;

import com.nd.voice.chatroom.EnterRoomActivity;
import com.nd.voice.meetingroom.activity.RoomListActivity;
import com.nd.voice.meetingroom.manager.MeetingEntity;

public class TabBusiness extends Fragment implements TitleEventCallBack,InvokeOptionsMenu {
	LayoutInflater mInflater;

	private TabLauncherUI mActivity;
	private View mRootView;

	private View mMultiTalk;
	private View mSyncRead;
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
		mAnimSet = mActivity.mAnimInteract;
		return mRootView;
	}

	private void setupViews() {
		mMultiTalk = mRootView.findViewById(R.id.tool_multi_talk);
		mSyncRead = mRootView.findViewById(R.id.tool_sync_browse);
    	mTitleText = (TextView)mRootView.findViewById(R.id.main_title);
    	mActionBtn = mRootView.findViewById(R.id.right_btn);    	
    	mTitleText.setText(R.string.title_communicate);
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
		mSyncRead.setOnTouchListener(mItemTouchListener);
		mMultiTalk.setOnTouchListener(mItemTouchListener);
	}
	
	private void registerEvent() {
		registerTouchEvent();
		
    	mActionBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mActivity.showDefaultActionMenu((View)v.getParent());
			}
		});
		
		
		mMultiTalk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				Intent intent = new Intent(mActivity, RoomListActivity.class);
				startActivity(intent);
			}
		});
		
		mSyncRead.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				Intent intent = new Intent(mActivity, EnterRoomActivity.class);
				startActivity(intent);
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
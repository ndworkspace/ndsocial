package cn.nd.social;

import java.lang.ref.WeakReference;

import com.activeandroid.query.Delete;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import cn.nd.social.account.BootActivity;
import cn.nd.social.account.CAUtils;
import cn.nd.social.card.TabMyCard;
import cn.nd.social.common.QuickActionBar;
import cn.nd.social.common.QuickActionItem;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.contacts.manager.MemberContact;
import cn.nd.social.tresure.PrivateTreasure;
import cn.nd.social.ui.controls.HorizontalListView;
import cn.nd.social.updater.UpdateInitiator;
import cn.nd.social.util.Utils;

public class TabActionBar {
	private TabLauncherUI mLauchActivity;
	private QuickActionBar mActionMenuView;
	
	public TabActionBar(TabLauncherUI activity) {
		mLauchActivity = activity;
		mActionMenuView = new QuickActionBar(activity);		
		init();
		mActionMenuView.show();
	}
	
	public QuickActionBar getActionBarView() {
		return mActionMenuView;
	}
	
	public void refresh() {
		mActionMenuView.refresh();
	}
	
	
	
	private void init() {
		mActionMenuView.addItems(QUICK_ACTION_ITEM);
		
		/* mActionBar.setOnItemClickListener*/
		mActionMenuView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position,long id) {		
				mLauchActivity.hide();				
				QuickActionItem item = (QuickActionItem)adapterView.getItemAtPosition(position);				
				onMenuSelected(ActionMenuId.fromInt(item.getItemId()));
				resetSelectView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				resetSelectView();				
			}
		});
		
		mActionMenuView.setOnFocusListener(new HorizontalListView.OnFocusListener() {
			
			@Override
			public void onItemFocus(AdapterView<?> parent, View child, int position, long id) {
				resetSelectView();
				child.setSelected(true);
				wkSelectView = new WeakReference<View>(child);
			}

			@Override
			public void onFocusNone(AdapterView<?> parent) {
				resetSelectView();				
			}
		}); 
		
	}

	
	private final static QuickActionItem [] QUICK_ACTION_ITEM = {
		new QuickActionItem(ActionMenuId.MENU_CARD.ordinal(), R.string.card, R.drawable.mgr_card_btn),
		new QuickActionItem(ActionMenuId.MENU_TREASURE.ordinal(), R.string.treasure, R.drawable.mgr_treasure_btn),
		new QuickActionItem(ActionMenuId.MENU_SHARE.ordinal(), R.string.share, R.drawable.mgr_share_btn),
		new QuickActionItem(ActionMenuId.MENU_UPDATE.ordinal(), R.string.update, R.drawable.mgr_update_btn),
		new QuickActionItem(ActionMenuId.MENU_ADVISE.ordinal(), R.string.advise, R.drawable.mgr_advise_btn),
		new QuickActionItem(ActionMenuId.MENU_EXIT.ordinal(), R.string.logout, R.drawable.mgr_exit_btn)
	};
	
	/**
	 * temporary solution
	 * */
	private WeakReference<View> wkSelectView = null;
	
	private void resetSelectView() {
		if(wkSelectView != null && wkSelectView.get() != null) {
			wkSelectView.get().setSelected(false);
		}
	}
	

	
	
	private void onMenuSelected(ActionMenuId menuId) {
		boolean handled = true;
		switch(menuId) {
		case MENU_CARD: {
			Intent intent = new Intent(mLauchActivity,TabMyCard.class);
			mLauchActivity.startActivity(intent);					
		}
			break;
			
		case MENU_TREASURE: {
			Intent intent = new Intent(mLauchActivity, PrivateTreasure.class);
			mLauchActivity.startActivity(intent);
		}
			break;
			
		case MENU_SHARE: {
			new ShareThisApp(mLauchActivity).share();
		}
			
			break;
			
		case MENU_UPDATE: {
			UpdateInitiator.checkAutoUpdate(UpdateInitiator.UpdateType.USER_TRIGGER);
		}
			break;
			
		case MENU_ADVISE: {
			mLauchActivity.launchFeedback();
		}
			break;
			
		case MENU_EXIT:{
			mLauchActivity.exit();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//reset user information
			CAUtils.resetUserInfo();
			new Delete().from(MemberContact.class).execute();
			Context appContext = Utils.getAppContext();
			
			Intent intent = new Intent(appContext,BootActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			appContext.startActivity(intent);
		}
			break;
			
		default:
			handled = false;
			break;
			
		}
	}
	
	
	private enum ActionMenuId {
		MENU_CARD,
		MENU_TREASURE,
		MENU_SHARE,
		MENU_UPDATE,
		MENU_ADVISE,
		MENU_EXIT;
		
		public static ActionMenuId fromInt(int index) {
			if(index == MENU_CARD.ordinal()) {
				return MENU_CARD;
			} else if(index == MENU_TREASURE.ordinal()) {
				return MENU_TREASURE;
			} else if(index == MENU_SHARE.ordinal()) {
				return MENU_SHARE;
			} else if(index == MENU_UPDATE.ordinal()) {
				return MENU_UPDATE;
			} else if(index == MENU_ADVISE.ordinal()) {
				return MENU_ADVISE;
			} else if(index == MENU_EXIT.ordinal()) {
				return MENU_EXIT;
			}
			throw new RuntimeException("ActionMenuId: error index");
		}
	}
	

	
}

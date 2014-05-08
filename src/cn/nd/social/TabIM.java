package cn.nd.social;


import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.nd.social.TabLauncherUI.InvokeOptionsMenu;
import cn.nd.social.TabLauncherUI.TitleEventCallBack;

import com.nd.voice.meetingroom.activity.AuthorizationFrame;

public class TabIM extends AuthorizationFrame implements TitleEventCallBack,InvokeOptionsMenu,TabFramentChangeListener {

	private TabLauncherUI mActivity;
	private View mRootView;

	private TextView mTitleText;
	private View mActionBtn;
	private ImageView mArrowLeft;
	private ImageView mArrowRight;
	
	private AnimationDrawable mAnimLeft;
	private AnimationDrawable mAnimRight;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mActivity = (TabLauncherUI)getActivity();
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = inflater.inflate(R.layout.main_tab_im_simple,null); 
        setupViews();
        registerEvent();
        return mRootView;
    }
    
    
    private void setupViews() {
    	mTitleText = (TextView)mRootView.findViewById(R.id.main_title);
    	mActionBtn = mRootView.findViewById(R.id.right_btn);
    	mTitleText.setText(R.string.title_trans_affection);
    	

    	//initAnim();
    	
    }
    
    private void initAnim() {
		mArrowLeft = (ImageView) mRootView.findViewById(R.id.left_arrow);

		mAnimLeft = (AnimationDrawable) mArrowLeft.getDrawable();
		
		mArrowRight = (ImageView) mRootView.findViewById(R.id.right_arrow);

		mAnimRight = (AnimationDrawable) mArrowRight.getDrawable();
		
		
		mAnimLeft.start();
		mAnimRight.start();
    }
    
    private void registerEvent() {
    	mActionBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mActivity.showDefaultActionMenu((View)v.getParent());
			}
		});
    }
    
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
    
    

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onRightBtnEvent(View v) {
		mActivity.showDefaultActionMenu((View)mActionBtn.getParent());
	}


	public class ItemListenerEvent implements OnItemClickListener {
		PopupWindow mPopup;
	
		ItemListenerEvent(PopupWindow popup) {
			mPopup = popup;
		}
	
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
	
			switch (pos) {
			case 0:
				mActivity.exit();
				break;
			default:
				break;
			}
	
			mPopup.dismiss();
		}
	
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
		
		boolean consume = false;
		if (item.getItemId() == TabLauncherUI.MENU_ITEM_CONTACTS) {
			consume = true;
		}		
		return consume;
	}
	

	@Override
	protected FrameLayout getRootFrameLayout() {
		// TODO Auto-generated method stub
		return (FrameLayout)mRootView;
	}


	@Override
	protected void loadData() {
		// TODO Auto-generated method stub
		
	}
	
}

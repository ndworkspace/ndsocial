package cn.nd.social;




import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.nd.dragdrop.DragController;
import cn.nd.dragdrop.DragController.DragListener;
import cn.nd.dragdrop.DragSource;
import cn.nd.dragdrop.DragSource.OnDragCompletedListener;
import cn.nd.social.TabLauncherUI.InvokeOptionsMenu;
import cn.nd.social.prishare.DragDropLayout;
import cn.nd.social.prishare.DropView;
import cn.nd.social.prishare.PriShareRecvActivity;
import cn.nd.social.prishare.PriShareSendActivity;

public class TabPriShare extends Fragment implements 
		InvokeOptionsMenu{

	private final static String TAG = "TabPriShare";
	private TabLauncherUI mLauncherActivity;
	private View mRootView;

	private DragDropLayout mDragLayer;
	
	private ImageView mDragView;
	private DropView mSend,mRecv;
	

	private View mActBtn;
	private TextView mTitleText;
	
	
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private AnimationDrawable mAnimUp;
	private AnimationDrawable mAnimDown;
	
	
	private DragController mDragController;

	
	private Handler mHandler = new Handler();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLauncherActivity = (TabLauncherUI) getActivity();
		
		mRootView = inflater.inflate(R.layout.main_tab_prishare, container, false);
		setupViews();
		registerEvent();
		
		return mRootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void setupViews() {
		mDragController = new DragController(mLauncherActivity);
		
		mActBtn = mRootView.findViewById(R.id.right_btn);
		mTitleText = (TextView)mRootView.findViewById(R.id.main_title);
		mTitleText.setText(R.string.title_share);
		mDragLayer = (DragDropLayout)mRootView.findViewById(R.id.drag_container);
		mDragView = (ImageView)mRootView.findViewById(R.id.drag_view);
		mSend = (DropView)mRootView.findViewById(R.id.send);
		mRecv = (DropView)mRootView.findViewById(R.id.recv);
		
		mArrowUp = (ImageView) mRootView.findViewById(R.id.arrow_up);

		mAnimUp = (AnimationDrawable) mArrowUp.getDrawable();
		
		
        
		mArrowDown = (ImageView) mRootView.findViewById(R.id.arrow_down);
		mAnimDown = (AnimationDrawable) mArrowDown.getDrawable();

		
		mDragLayer.setDragController(mDragController);
		
		mDragController.addDropTarget(mSend);
		mDragController.addDropTarget(mRecv);
		
		mAnimDown.start();
		mAnimUp.start();
	}
	
	OnDragCompletedListener mDragCompleteListener = new OnDragCompletedListener() {
		
		@Override
		public boolean onDropCompleted(View dragSource, View target, boolean success) {
			if(target == mSend) {
				Intent intent = new Intent(mLauncherActivity,PriShareSendActivity.class);
				startActivity(intent);
			} else if(target == mRecv) {
				Intent intent = new Intent(mLauncherActivity,PriShareRecvActivity.class);
				startActivity(intent);
			}
			mHandler.postDelayed(new Runnable() {					
				@Override
				public void run() {
					mDragView.setVisibility(View.VISIBLE);						
				}
			}, 1000);
			return false;
		}
	};
	
	private boolean startDrag(View v,DragSource dragSource) {
		dragSource.setDragCompletedListener(mDragCompleteListener);
		
		
		mDragController.startDrag(v, dragSource, dragSource,
				DragController.DRAG_ACTION_MOVE);
		return true;
	}

	private void registerEvent() {

/*		mDragView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View view) {
				if (view.isInTouchMode()) {
					return startDrag(mDragView,(DragSource)mDragView);
				}
				return true;
			}
		});*/
		
		mDragView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (v.isInTouchMode() && event.getAction() == MotionEvent.ACTION_DOWN) {
					return startDrag(mDragView,(DragSource)mDragView);
				}
				return true;
			}
		});
		
		mDragController.setDragListener(new DragListener() {
			
			@Override
			public void onDragStart(DragSource source, Object info, int dragAction) {
				mLauncherActivity.disablePaging();
			}
			
			@Override
			public void onDragEnd() {
				mLauncherActivity.enablePaging();			
			}
		});
		
		mActBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mLauncherActivity.showDefaultActionMenu(null);				
			}
		});
	}


	@Override
	public void onInvokeOptionsMenu(Menu menu, boolean isCurrent) {
		mLauncherActivity.showDefaultActionMenu(null);
	}

	@Override
	public boolean onItemSelected(MenuItem item) {
		return false;
	}
	
	public void onFoucsChange(boolean hasFocus) {

	}
	
}

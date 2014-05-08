package cn.nd.social.tobedelete;

import cn.nd.social.R;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.updater.UpdateInitiator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MgrMenuIconView extends LinearLayout{

	private View mRootView;
	private ImageView mMgrCardLayout;
	private ImageView mMgrConnect;
	private ImageView mMgrTreasure;
	private ImageView mMgrHistory;		
	private ImageView[] mImageArr = new ImageView[8];
	private int choose = 0;
	
	private ImageView mMgrShare;
	private ImageView mMgrUpdate;
	private ImageView mMgrAdvise;
	private ImageView mMgrExit;


	private Context mContext;
	
	private MgrMenuListener mListener;
	
	public MgrMenuIconView(Context context) {
		super(context);
		mContext = context;
		initView();
		initListener();
	}

	public MgrMenuIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
		initListener();
	}
	
	private void initView() {

		mRootView = LayoutInflater.from(mContext).inflate(
				R.layout.tab_mgr_menu_view, this);
		mMgrCardLayout = (ImageView) mRootView.findViewById(R.id.mgr_card);
		mMgrConnect = (ImageView) mRootView.findViewById(R.id.mgr_connect);
		mMgrTreasure = (ImageView) mRootView.findViewById(R.id.mgr_treasure);
		mMgrHistory = (ImageView) mRootView.findViewById(R.id.mgr_history);
		
		mMgrShare = (ImageView) mRootView.findViewById(R.id.mgr_share_img);
		mMgrUpdate = (ImageView) mRootView.findViewById(R.id.mgr_update_img);
		mMgrAdvise = (ImageView) mRootView.findViewById(R.id.mgr_advise_img);
		mMgrExit = (ImageView) mRootView.findViewById(R.id.mgr_exit_img);

		mImageArr[0] = mMgrConnect;
		mImageArr[1] = mMgrCardLayout;
		mImageArr[2] = mMgrTreasure;
		mImageArr[3] = mMgrHistory;
		mImageArr[4] = mMgrShare;
		mImageArr[5] = mMgrUpdate;
		mImageArr[6] = mMgrAdvise;
		mImageArr[7] = mMgrExit;



		mImageArr[0].setSelected(true);
		for (int i = 1; i < mImageArr.length; i++) {
			mImageArr[i].setSelected(false);
		}
		

	}
	public void switchIcon(int index){
		if (choose != index) {
			mImageArr[choose].setSelected(false);
			mImageArr[index].setSelected(true);
			choose = index;
		}
	}

	private void initListener() {

		mMgrCardLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (choose != 1) {
					mImageArr[choose].setSelected(false);
					mImageArr[1].setSelected(true);
					choose = 1;
				}
				mListener.onMgrCardShare();				

			}
		});

		mMgrConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 0) {
					mImageArr[choose].setSelected(false);
					mImageArr[0].setSelected(true);
					choose = 0;
				}
				mListener.onMgrConnect();
			}
		});

		mMgrTreasure.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 2) {
					mImageArr[choose].setSelected(false);
					mImageArr[2].setSelected(true);
					choose = 2;
				}
				mListener.onMgrTreasure();
			}
		});

		mMgrHistory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 3) {
					mImageArr[choose].setSelected(false);
					mImageArr[3].setSelected(true);
					choose = 3;
				}
				mListener.onMgrMessageHistory();
			}
		});
		
		mMgrShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (choose != 4) {
					mImageArr[choose].setSelected(false);
					mImageArr[4].setSelected(true);
					choose = 4;
				}
				new ShareThisApp(TabLauncherUI.sInstance).share();			

			}
		});

		mMgrUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 5) {
					mImageArr[choose].setSelected(false);
					mImageArr[5].setSelected(true);
					choose = 5;
				}
				UpdateInitiator.checkAutoUpdate(UpdateInitiator.UpdateType.USER_TRIGGER);
			}
		});

		mMgrAdvise.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 6) {
					mImageArr[choose].setSelected(false);
					mImageArr[6].setSelected(true);
					choose = 6;
				}
				TabLauncherUI.sInstance.launchFeedback();
			}
		});

		mMgrExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 7) {
					mImageArr[choose].setSelected(false);
					mImageArr[7].setSelected(true);
					choose = 7;
				}
				TabLauncherUI.sInstance.exit();
			}
		});

		
	}

	public void setmListener(MgrMenuListener mListener) {
		this.mListener = mListener;
	}
	
	public interface MgrMenuListener{
		
		void onMgrConnect();
		void onMgrCardShare();
		void onMgrTreasure();
		void onMgrMessageHistory();
		
	}
}

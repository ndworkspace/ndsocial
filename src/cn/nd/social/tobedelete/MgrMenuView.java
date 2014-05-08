package cn.nd.social.tobedelete;

import cn.nd.social.R;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.updater.UpdateInitiator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MgrMenuView extends LinearLayout {

	private View mRootView;
	private ImageView mMgrCardLayout;
	private ImageView mMgrConnect;
	private ImageView mMgrTreasure;
	private ImageView mMgrHistory;
	private ImageView[] mImageArr = new ImageView[4];
	private int choose = 0;

	private String[] mMenuArr = {
			getResources().getString(R.string.check_update),
			getResources().getString(R.string.share_the_app),
			getResources().getString(R.string.feedback),
			getResources().getString(R.string.exit_application) };

	private ListView mMgrList;
	private Context mContext;
	
	private MgrMenuListener mListener;
	

	public MgrMenuView(Context context) {
		super(context);
		mContext = context;
	}

	public MgrMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
		initListener();
	}

	private void initView() {

		mRootView = LayoutInflater.from(mContext).inflate(
				R.layout.tabmgr_menuview, this);
		mMgrCardLayout = (ImageView) mRootView.findViewById(R.id.mgr_card);
		mMgrConnect = (ImageView) mRootView.findViewById(R.id.mgr_connect);
		mMgrTreasure = (ImageView) mRootView.findViewById(R.id.mgr_treasure);
		mMgrHistory = (ImageView) mRootView.findViewById(R.id.mgr_history);

		mImageArr[0] = mMgrConnect;
		mImageArr[1] = mMgrCardLayout;
		mImageArr[2] = mMgrTreasure;
		mImageArr[3] = mMgrHistory;

		mMgrCardLayout.setAlpha(0.5f);
		mMgrConnect.setAlpha(1f);
		mMgrTreasure.setAlpha(0.5f);
		mMgrHistory.setAlpha(0.5f);

		mMgrList = (ListView) mRootView.findViewById(R.id.mgr_listview);
		mMgrList.setAdapter(new MgrMenuList());

	}
	public void switchIcon(int index){
		if (choose != index) {
			mImageArr[choose].setAlpha(0.5f);
			mImageArr[index].setAlpha(1f);
			choose = index;
		}
	}

	private void initListener() {

		mMgrCardLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (choose != 1) {
					mImageArr[choose].setAlpha(0.5f);
					mImageArr[1].setAlpha(1f);
					choose = 1;
				}
				mListener.onMgrCardShare();				

			}
		});

		mMgrConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 0) {
					mImageArr[choose].setAlpha(0.5f);
					mImageArr[0].setAlpha(1f);
					choose = 0;
				}
				mListener.onMgrConnect();
			}
		});

		mMgrTreasure.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 2) {
					mImageArr[choose].setAlpha(0.5f);
					mImageArr[2].setAlpha(1f);
					choose = 2;
				}
				mListener.onMgrTreasure();
			}
		});

		mMgrHistory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (choose != 3) {
					mImageArr[choose].setAlpha(0.5f);
					mImageArr[3].setAlpha(1f);
					choose = 3;
				}
				mListener.onMgrMessageHistory();
			}
		});

		mMgrList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

				switch (pos) {
				case 0:
					UpdateInitiator.checkAutoUpdate(UpdateInitiator.UpdateType.USER_TRIGGER);
					break;
				case 1:
					new ShareThisApp(TabLauncherUI.sInstance).share();
					break;
				case 2:
					TabLauncherUI.sInstance.launchFeedback();
					break;
				case 3:
					TabLauncherUI.sInstance.exit();
					break;

				default:
					break;
				}

			}
		});
	}

	public void setmListener(MgrMenuListener mListener) {
		this.mListener = mListener;
	}

	public class MgrMenuList extends BaseAdapter {

		@Override
		public int getCount() {
			return mMenuArr.length;
		}

		@Override
		public Object getItem(int pos) {
			return mMenuArr[pos];
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.mgr_menu_listview, null);
			}

			RelativeLayout l = (RelativeLayout) convertView
					.findViewById(R.id.mgr_item_lay);
			LayoutParams lp = (LayoutParams) l.getLayoutParams();
			
			if (pos == 3) {							
				lp.height = (int) getResources().getDimension(
						R.dimen.mgr_menu_specific_height);
				l.setLayoutParams(lp);
			}else{
				lp.height = (int) getResources().getDimension(
						R.dimen.mgr_menu_list_height);
				l.setLayoutParams(lp);
				
			}
			TextView text = (TextView) convertView
					.findViewById(R.id.mgr_menu_item);
			text.setText(mMenuArr[pos]);

			return convertView;
		}

	}
	
	public interface MgrMenuListener{
		
		void onMgrConnect();
		void onMgrCardShare();
		void onMgrTreasure();
		void onMgrMessageHistory();
		
	}

}

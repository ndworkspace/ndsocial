package cn.nd.social.common;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.ui.controls.HorizontalListView;
import cn.nd.social.util.DimensionUtils;

public class QuickActionBar extends LinearLayout{

	private View mRootView;

	private HorizontalListView mHorizotalList;

	private Context mContext;

/*	private View.OnClickListener mInternalListener;*/
	private ArrayList<QuickActionItem> mItemList = new ArrayList<QuickActionItem>();
	
	private int mItemWidth = 0;
	
	public QuickActionBar(Context context) {
		super(context);
		mContext = context;
		setupViews();
	}

	public QuickActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setupViews();
	}
	
	private void setupViews() {

		mRootView = LayoutInflater.from(mContext).inflate(
				R.layout.main_tab_action_menu, this);
		mHorizotalList = (HorizontalListView) mRootView.findViewById(R.id.horizontal_list);		
		mHorizotalList.setAdapter(mAdapter);
		int displayWidth = DimensionUtils.getDisplayWidth();
		mItemWidth = displayWidth / 4;
	}
	

	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
		mHorizotalList.setOnItemSelectedListener(listener);
	}
	

	public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
		mHorizotalList.setOnItemClickListener(listener);
	}
	

	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
		mHorizotalList.setOnItemLongClickListener(listener);
	}
	
	public void setOnFocusListener(HorizontalListView.OnFocusListener listener) {
		mHorizotalList.setOnFocusListener(listener);
	}
	
/*	public void setInternalListener(View.OnClickListener listener) {
		mInternalListener = listener;
	}*/
	
	public void addItems(QuickActionItem[] items) {
		for (QuickActionItem item : items)
			mItemList.add(item);
	}


	public void addItem(QuickActionItem item) {
		mItemList.add(item);
	}
	
	public void show() {
		mAdapter.notifyDataSetChanged();
	}
	
	public void refresh() {
		mHorizotalList.setAdapter(mAdapter);
	}
	
	

	
	
	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return mItemList.size();
		}

		@Override
		public QuickActionItem getItem(int position) {
			return mItemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//TODO: check convertView reusablity
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_tab_action_menu_item, null);
			View container = view.findViewById(R.id.ll_container);
			if(mItemWidth != 0) {				
				container.getLayoutParams().width = mItemWidth;
			}

			TextView title = (TextView) view.findViewById(R.id.tv_text);
			ImageView icon = (ImageView)view.findViewById(R.id.iv_icon);
			
			
			QuickActionItem item = getItem(position);
			title.setText(item.getTextRes());
			icon.setImageResource(item.getIconRes());
			view.setClickable(true);//set this to avoid ViewGroup dispatchSetPressed
			view.setTag(item);
			
			return view;
		}
		
	};
	
	


}

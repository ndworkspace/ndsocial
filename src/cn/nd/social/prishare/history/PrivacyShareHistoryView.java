package cn.nd.social.prishare.history;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.nd.social.R;

public class PrivacyShareHistoryView {
	private HistoryItemHelper mHisItemHelper;
	
	private HistoryListAdapter mHistoryListAdapter = null;

	private ListView mHistoryListView = null;
	
	private Context mContext;
	
	public PrivacyShareHistoryView(Activity act) {
		this.mContext = act;
		mHisItemHelper = new HistoryItemHelper(mContext);
	}

	public HistoryItemHelper getHistoryHelper(){
		return mHisItemHelper;
	}
	
	public void initHistoryView(View parent) {

		if (mHistoryListAdapter == null) {
			mHistoryListAdapter = new HistoryListAdapter(mContext);

			mHistoryListAdapter.setHistoryItemHelper(mHisItemHelper);

			mHisItemHelper.setListAdapter(mHistoryListAdapter);
		}

		mHistoryListView = (ListView) parent.findViewById(R.id.history_list);
		mHistoryListView.setAdapter(mHistoryListAdapter);

		setHistoryListViewItemEvent();
	}

	void setHistoryListViewItemEvent() {
		mHistoryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("PrivacyShareHistroyView", "History item click posotion : " + position);
				if (position < 3) {
					return;
				}

				int itemType = mHisItemHelper.getItemType(position - 3);
				if (itemType != 6) {
					return;
				}

				HistoryItemHelper.ItemData data = mHisItemHelper
						.getItem(position - 3);
				if (data != null) {
					Log.d("PrivacyShareHistroyView", "History item grant type : " + data.mGrantType
							+ " value : " + data.mGrantValue + " file name : "
							+ data.mFileName);

//					Intent intent = new Intent(mContext, ImageViewer.class);
//
//					intent.putExtra("type", data.mGrantType);
//					intent.putExtra("value", data.mGrantValue);
//
//					intent.putExtra("filename", data.mFileName);

//					mContext.startActivity(intent);
				}
			}
		});

		// mGrid.setOnItemLongClickListener(new CellItemLongClickListener());
	}
}

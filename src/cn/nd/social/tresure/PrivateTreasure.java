package cn.nd.social.tresure;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.privategallery.ImageThumbnailViewer;

public class PrivateTreasure extends Activity {

	Context mContext;
	LayoutInflater mInflater;

	private final static int[] TAB_TITLE_LOGO = { R.drawable.pri_tab_gallery,
			R.drawable.pri_tab_music, R.drawable.pri_tab_file,
			R.drawable.pri_tab_app };

	private final static int[] TAB_TITLE_TEXT = { R.string.qe_main_pic,
			R.string.qe_main_audio, R.string.qe_main_file, R.string.qe_main_app };

	private final static int[] TAB_TITLE_ID = { MsgDefine.FILE_TYPE_IMAGE,
			MsgDefine.FILE_TYPE_MEDIA, MsgDefine.FILE_TYPE_FILE,
			MsgDefine.FILE_TYPE_APP };

	private class TreasureItem {
		int strRes;
		int drawableRes;
		int itemId;

		TreasureItem(int str, int drawable, int itemId) {
			strRes = str;
			drawableRes = drawable;
			this.itemId = itemId;
		}
	}

	private ArrayList<TreasureItem> mItems = new ArrayList<TreasureItem>();
	private PrivateListAdapter mAdapter;

	public static void getToTreasure(Context context, int type) {

		switch (type) {
		case MsgDefine.FILE_TYPE_IMAGE: {
			Intent intent = new Intent(context, ImageThumbnailViewer.class);
			context.startActivity(intent);
		}
			break;

		case MsgDefine.FILE_TYPE_MEDIA: {

			Intent intent = new Intent(context, TreasureList.class);
			intent.putExtra(TreasureList.KEY_TREASURE_TYPE, type);
			context.startActivity(intent);
		}
			break;

		case MsgDefine.FILE_TYPE_FILE: {

			Intent intent = new Intent(context, TreasureList.class);
			intent.putExtra(TreasureList.KEY_TREASURE_TYPE, type);
			context.startActivity(intent);
		}
			break;

		case MsgDefine.FILE_TYPE_APP: {

			Intent intent = new Intent(context, TreasureList.class);
			intent.putExtra(TreasureList.KEY_TREASURE_TYPE, type);
			context.startActivity(intent);
		}
			break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mInflater = getLayoutInflater();
		setContentView(R.layout.treasure_activity);
		setupViews();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void setupViews() {
		TextView title = (TextView) findViewById(R.id.main_title);
		title.setText(R.string.treasure);

		View backBtn = findViewById(R.id.back_btn);
		backBtn.setVisibility(View.VISIBLE);

		ListView list = (ListView) findViewById(R.id.treasure_list);

		findViewById(R.id.right_btn).setVisibility(View.GONE);

		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		for (int i = 0; i < TAB_TITLE_LOGO.length; i++) {
			mItems.add(new TreasureItem(TAB_TITLE_TEXT[i], TAB_TITLE_LOGO[i],
					TAB_TITLE_ID[i]));
		}

		mAdapter = new PrivateListAdapter(mItems);
		list.setAdapter(mAdapter);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TreasureItem item = mAdapter.getItem(position);
				switch (item.itemId) {
				case MsgDefine.FILE_TYPE_IMAGE: {
					Intent intent = new Intent(mContext,
							ImageThumbnailViewer.class);
					startActivity(intent);
				}
					break;

				case MsgDefine.FILE_TYPE_MEDIA: {

					Intent intent = new Intent(mContext, TreasureList.class);
					intent.putExtra(TreasureList.KEY_TREASURE_TYPE, item.itemId);
					startActivity(intent);
				}
					break;

				case MsgDefine.FILE_TYPE_FILE: {

					Intent intent = new Intent(mContext, TreasureList.class);
					intent.putExtra(TreasureList.KEY_TREASURE_TYPE, item.itemId);
					startActivity(intent);
				}
					break;

				case MsgDefine.FILE_TYPE_APP: {

					Intent intent = new Intent(mContext, TreasureList.class);
					intent.putExtra(TreasureList.KEY_TREASURE_TYPE, item.itemId);
					startActivity(intent);
				}
					break;
				}
			}

		});

	}

	private class PrivateListAdapter extends BaseAdapter {
		ArrayList<TreasureItem> itemList;

		PrivateListAdapter(ArrayList<TreasureItem> itemList) {
			this.itemList = itemList;
		}

		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public TreasureItem getItem(int position) {
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v;
			if (convertView == null) {
				v = mInflater.inflate(R.layout.app_invite_list_item, parent,
						false);
				holder = new ViewHolder();

				holder.image = (ImageView) v
						.findViewById(R.id.iv_invite_item_icon);
				holder.text = (TextView) v
						.findViewById(R.id.tv_invite_item_ways);
				v.setTag(holder);
			} else {
				v = convertView;
				holder = (ViewHolder) v.getTag();
			}
			bindView(holder, position);

			return v;
		}

		private void bindView(ViewHolder holder, int position) {
			TreasureItem info = getItem(position);
			holder.image.setImageResource(info.drawableRes);
			holder.text.setTextColor(Color.WHITE);
			holder.text.setText(info.strRes);
		}

	}

	private class ViewHolder {
		ImageView image;
		TextView text;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}

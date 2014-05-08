package cn.nd.social.syncbrowsing.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.hotspot.MsgDefine;

public class SyncHistoryArrayAdapter extends ArrayAdapter<SyncHistoryItem> {

	private Context mContext;
	private List<SyncHistoryItem> mSyncList;

	public SyncHistoryArrayAdapter(Context context, int textViewResourceId,
			List<SyncHistoryItem> list) {
		super(context, textViewResourceId, list);

		mContext = context;
		mSyncList = list;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SyncHistoryItem item = mSyncList.get(position);
		int itemType = (int) item.getmHistoryType();

			switch (itemType) {
			case MsgDefine.FILE_TYPE_TITLE:
				return getTitleView(convertView, position, item);
			case MsgDefine.FILE_TYPE_FILE:
				return getFileView(convertView, position, item);
			case MsgDefine.FILE_TYPE_IMAGE:
				return getPicView(convertView, position, item);
			default:
				throw (new RuntimeException("unsupport view type"));				
			}
	}

	/*********************get different view ********************/
	private View getTitleView(View convertView, int pos,
			SyncHistoryItem item) {
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
						R.layout.sync_list_item_title, null);
		}

		TextView tv = (TextView) convertView
				.findViewById(R.id.sync_item_title);

		String str = item.getmHistoryName();
		tv.setText(str);
		return convertView;
	}

	private View getFileView(View convertView, int pos,
			SyncHistoryItem item) {

		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.sync_list_item_file, null);
		}
		

		TextView tv = (TextView) convertView
				.findViewById(R.id.sync_item_file);

		String str = item.getmHistoryName();
		tv.setText(str);
		
		return convertView;
	}

	private View getPicView(View convertView, int pos,
			SyncHistoryItem item) {
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
						R.layout.sync_list_item_img, null);
		}

		Bitmap bm = convertToBitmap(item.getmHistoryPath(), 100, 80);
		ImageView localImageView = (ImageView) convertView
				.findViewById(R.id.sync_item_img);

		localImageView.setImageBitmap(bm);
		return convertView;
	}

	public Bitmap convertToBitmap(String path, int w, int h) {

		BitmapFactory.Options opts = new BitmapFactory.Options();

		// 设置为ture只获取图片大小

		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

		// 返回为空

		BitmapFactory.decodeFile(path, opts);
		int width = opts.outWidth;
		int height = opts.outHeight;
		float scaleWidth = 0.f, scaleHeight = 0.f;

		if (width > w || height > h) {

			// 缩放
			scaleWidth = ((float) width) / w;
			scaleHeight = ((float) height) / h;
		}
		opts.inJustDecodeBounds = false;
		float scale = Math.max(scaleWidth, scaleHeight);
		opts.inSampleSize = (int) scale;
		WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(path, opts));

		return Bitmap.createScaledBitmap(weak.get(), w, h, true);

	}

	/*************************************************************/
	@Override
	public SyncHistoryItem getItem(int position) {
		return super.getItem(position);
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}
	
	

}

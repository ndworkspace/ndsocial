package cn.nd.social.privategallery;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import cn.nd.social.R;
import cn.nd.social.prishare.items.CellItemBase;

/**
 * adapter for private gallery
 * */
public class GalleryListAdapter extends CursorAdapter {

	private ImageThumbnailViewer mContext;
	LayoutInflater mInflater;
	private int mResId;
	private int mWidth;
	private int mHeight;

	GalleryListAdapter(ImageThumbnailViewer img,int resourceId,int width) {
		super(img, null, false); // auto-requery to false;
		mContext = img;
		mResId = resourceId;
		mInflater = (LayoutInflater) mContext
				.getSystemService("layout_inflater");
		mWidth = width;
		mHeight = mWidth * 3 /4;//height width scale: 3:4
	}

	@Override
	public Object getItem(int position) {
		return super.getItem(position); // cursor
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		GalleryViewHolder holder = (GalleryViewHolder) view.getTag();
		//temporary solution
		if (holder.item != null 
				&& holder.item.getPos() == cursor.getPosition()
				&& ImageThumbnailViewer.isMultiSelectMode()) {
			return;
		}
		
		GalleryGridItem item = new GalleryGridItem(CellItemBase.IMAGE_TYPE,cursor);
		try {
			holder.image.setImageBitmap(BitmapFactory.decodeFile(item.getItemThumbPath()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		holder.item = item;
		int pos = cursor.getPosition();		
		
		if (ImageThumbnailViewer.isMultiSelectMode()) {
			
			
			holder.checkBox.setVisibility(View.VISIBLE);
			if (mContext.mPrivacyList.containsKey(pos)) {				
				holder.checkBox.setChecked(true);
			}else holder.checkBox.setChecked(false);
			
		} else {
			holder.checkBox.setChecked(false);
			holder.checkBox.setVisibility(View.GONE);
		}
		
		item.v = new WeakReference<View>(holder.checkBox);
		
		if (mContext.mPrivacyList.containsKey(pos)) {
			mContext.mPrivacyList.put(pos,item);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mInflater.inflate(mResId, parent,
				false);
		GalleryViewHolder holder = new GalleryViewHolder();
		holder.image = ((ImageView) v.findViewById(R.id.icon));
		holder.checkBox = ((CheckBox) v.findViewById(R.id.checkbox));
		v.setTag(holder);
		
		/**adjust item width and height;*/
		ViewGroup.LayoutParams params = v.getLayoutParams();
		params.width = mWidth;
		params.height = mHeight;
		
		return v;
	}

	public final static class GalleryViewHolder {
		ImageView image;
		CheckBox checkBox;
		GalleryGridItem item;
	}

}

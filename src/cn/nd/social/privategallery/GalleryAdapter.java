package cn.nd.social.privategallery;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore.Images;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import cn.nd.social.R;
import cn.nd.social.prishare.items.ImageCellItem;

/**
 * adapter for system gallery
 * */
public class GalleryAdapter extends ArrayAdapter<ImageCellItem> {

	Context mContext;
	int resource;
	List<ImageCellItem> data;
	private LayoutInflater mInflater;
	private int mItemWidth;
	private int mItemHeight;

	public List<ImageCellItem> getData() {
		return data;
	}
	
	public GalleryAdapter(Context context, int resource,
			List<ImageCellItem> data,int itemWidth) {
		super(context, resource, data);
		this.mContext = context;
		this.resource = resource;
		this.data = data;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItemWidth = itemWidth;
		mItemHeight = mItemWidth * 3 / 4; //height width scale 3:4
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(resource, parent, false);			
			holder = new ViewHolder();
			holder.chkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
			holder.img = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		/**adjust item size*/
		ViewGroup.LayoutParams params = convertView.getLayoutParams();
		params.width = mItemWidth;
		params.height = mItemHeight;
		
		bindView(position, holder);

		convertView.setBackgroundColor(Color.TRANSPARENT);
		// when we scroll the gridview, we may creating the new item
		// in this case, we need to know the status of the checkbox


		return convertView;
	}

	void bindView(int position, ViewHolder holder) {
		ImageCellItem item = data.get(position);
		if (item == holder.item) return;
		
		Bitmap bm = Images.Thumbnails.getThumbnail(
				mContext.getContentResolver(), item.getId(),
				Images.Thumbnails.MICRO_KIND, null);
		if(holder.bm != null && !holder.bm.isRecycled()) {
			holder.bm.recycle();
		}
		holder.img.setImageBitmap(bm);
		holder.bm = bm;
		holder.item = item;
		if (ImageThumbnailViewer.isMultiSelectMode()) {
			holder.chkBox.setVisibility(View.VISIBLE);
			holder.chkBox.setChecked(item.isSelected());
		} else {
			holder.chkBox.setChecked(false);
			holder.chkBox.setVisibility(View.GONE);
		}
		item.v = new WeakReference<View>(holder.chkBox);
	}

	@Override
	public ImageCellItem getItem(int position) {
		return data.get(position);
	}

	public static class ViewHolder {
		ImageView img;
		CheckBox chkBox;
		Bitmap bm;
		ImageCellItem item;
	}

}

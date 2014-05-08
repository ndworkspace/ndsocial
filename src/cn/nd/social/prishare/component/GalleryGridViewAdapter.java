package cn.nd.social.prishare.component;

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
import cn.nd.social.prishare.PriShareSendActivity;
import cn.nd.social.prishare.items.ImageCellItem;

public class GalleryGridViewAdapter extends ArrayAdapter<ImageCellItem> {

	Context mContext;
	int resource;
	List<ImageCellItem> data;
	private LayoutInflater mInflater;

	public List<ImageCellItem> getData() {
		return data;
	}
	
	public GalleryGridViewAdapter(Context context, int resource,
			List<ImageCellItem> data) {
		super(context, resource, data);
		this.mContext = context;
		this.resource = resource;
		this.data = data;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Holder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(resource, parent, false);
			holder = new Holder();
			holder.chkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
			holder.img = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
			convertView.setBackgroundColor(Color.TRANSPARENT);
		} else {
			holder = (Holder) convertView.getTag();
		}

		bindView(position, holder);

		
		// when we scroll the gridview, we may creating the new item
		// in this case, we need to know the status of the checkbox


		return convertView;
	}

	void bindView(int position, Holder holder) {
		ImageCellItem item = data.get(position);
		if(holder.item == item) {
			return;
		}
		Bitmap bm = Images.Thumbnails.getThumbnail(
				mContext.getContentResolver(), item.getId(),
				Images.Thumbnails.MICRO_KIND, null);
		if(holder.bm != null && !holder.bm.isRecycled()) {
			holder.bm.recycle();
		}
		holder.img.setImageBitmap(bm);
		holder.bm = bm;
		// holder.img.setImageDrawable(item.getIcon());
		holder.item = item;
		if (PriShareSendActivity.isMultiSelectMode()) {
			//tangtaotao@ND_20140220 checkbox comment out
			//holder.chkBox.setVisibility(View.VISIBLE);
			holder.chkBox.setChecked(item.isSelected());
		} else {
			holder.chkBox.setChecked(false);
			//tangtaotao@ND_20140220 checkbox comment out
			//holder.chkBox.setVisibility(View.GONE);
		}
	}

	@Override
	public ImageCellItem getItem(int position) {
		return data.get(position);
	}

	public static class Holder extends GridViewAdapter.Holder {
		Bitmap bm;
	}

}

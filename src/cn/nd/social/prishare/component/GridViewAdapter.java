package cn.nd.social.prishare.component;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.prishare.PriShareSendActivity;
import cn.nd.social.prishare.items.AppCellItem;
import cn.nd.social.prishare.items.CellItemBase;

public class GridViewAdapter extends ArrayAdapter<AppCellItem> {

	Context mContext;
	int resource;
	List<AppCellItem> data;
	int imageHeight;
	private LayoutInflater mInflater;

	public List<AppCellItem> getData() {
		return data;
	}
	
	public GridViewAdapter(Context context, int resource,
			List<AppCellItem> data, int imgHeight) {
		super(context, resource, data);
		this.mContext = context;
		this.resource = resource;
		this.data = data;
		this.imageHeight = imgHeight;
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
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.subtitle = (TextView) convertView
					.findViewById(R.id.subtitle);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		bindView(position, holder);

		convertView.setBackgroundColor(Color.TRANSPARENT);
		// when we scroll the gridview, we may creating the new item
		// in this case, we need to know the status of the checkbox


		return convertView;
	}

	void bindView(int position, Holder holder) {
		
		AppCellItem item = data.get(position);
		if(holder.item == item) {
			return;
		}
		holder.img.setImageDrawable(item.getItemIcon());
		holder.title.setText(item.getFileShortName());
		holder.subtitle.setText(item.getAppSize());
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
	public AppCellItem getItem(int position) {
		return data.get(position);
	}

	public static class Holder {
		public ImageView img;
		public CheckBox chkBox;
		public TextView title;
		public TextView subtitle;
		public CellItemBase item;
	}

}

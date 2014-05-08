package cn.nd.social.prishare.component;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.prishare.PriShareSendActivity;
import cn.nd.social.prishare.items.AudioCellItem;

public class AudioListViewAdapter extends ArrayAdapter<AudioCellItem> {

	Context mContext;
	int resource;
	List<AudioCellItem> data;
	private LayoutInflater mInflater;

	public List<AudioCellItem> getData() {
		return data;
	}
	public AudioListViewAdapter(Context context, int resource,
			List<AudioCellItem> data) {
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
			holder.title = (TextView)convertView.findViewById(R.id.title);
			holder.subtitle = (TextView)convertView.findViewById(R.id.title2);
			holder.title3 = (TextView)convertView.findViewById(R.id.title3);
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		bindView(position, holder);

		//convertView.setBackgroundColor(Color.TRANSPARENT);



		return convertView;
	}

	void bindView(int position, Holder holder) {
		AudioCellItem item = data.get(position);
		if(item == holder.item) {
			return;
		}
		holder.img.setImageDrawable(item.getIcon());
		holder.title.setText(item.getTitle());
		holder.subtitle.setText(item.getArtist());
		holder.title3.setText(item.getFileSize());
		holder.item = item;
		// when we scroll the gridview, we may creating the new item
		// in this case, we need to know the status of the checkbox
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
	public AudioCellItem getItem(int position) {
		return data.get(position);
	}

	public static class Holder extends GridViewAdapter.Holder {
		TextView title3;
	}

}

package cn.nd.social.prishare.component;

import java.util.HashMap;
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
import cn.nd.social.prishare.items.FilesCellItem;

public class FilesGridViewAdapter extends ArrayAdapter<FilesCellItem> {

	Context mContext;
	int resource;
	List<FilesCellItem> data;
	int imageHeight;
	private LayoutInflater mInflater;
	private HashMap<String, View> mfileList;

	public FilesGridViewAdapter(Context context, int resource,
			List<FilesCellItem> data, int imgHeight,HashMap<String, View> fileList) {
		
		super(context, resource, data);
		
		this.mContext = context;
		
		this.resource = resource;
		
		this.data = data;
		
		this.imageHeight = imgHeight;
		
		this.mfileList = fileList;
		
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public List<FilesCellItem> getData() {
		return data;
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
//			holder.subtitle = (TextView) convertView
//					.findViewById(R.id.subtitle);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		bindView(position, holder);

		convertView.setBackgroundColor(Color.TRANSPARENT);
		


		return convertView;
	}

	void bindView(int position, Holder holder) {
		FilesCellItem item = data.get(position);
		if(item == holder.item) {
			return;
		}
		holder.img.setImageDrawable(item.getIcon());
		holder.title.setText(item.getFileShortName());
//		holder.subtitle.setText(item.getAppSize());
		holder.item = item;
		// when we scroll the gridview, we may creating the new item
		// in this case, we need to know the status of the checkbox
		if (PriShareSendActivity.isMultiSelectMode() && mfileList.containsKey(item.getItemPath())) {
			//tangtaotao@ND_20140220 checkbox comment out
			//holder.chkBox.setVisibility(View.VISIBLE);
			holder.chkBox.setChecked(true);
		} else {
			holder.chkBox.setChecked(false);
		}
		//tangtaotao@ND_20140220 checkbox
		holder.chkBox.setVisibility(
				item.isDirectory() ? View.GONE : View.VISIBLE);

	}

	@Override
	public FilesCellItem getItem(int position) {
		return data.get(position);
	}

	public static class Holder extends GridViewAdapter.Holder {
	}
}

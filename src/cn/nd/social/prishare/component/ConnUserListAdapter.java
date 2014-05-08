package cn.nd.social.prishare.component;

import java.util.List;

import cn.nd.social.R;
import cn.nd.social.util.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ConnUserListAdapter extends BaseAdapter {
	private List<String> mList;
	private LayoutInflater mInflater;
	private View.OnClickListener mKickListener;
	public ConnUserListAdapter(List<String> list) {
		mInflater = (LayoutInflater) Utils.getAppContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		ViewHolder holder;
		if(convertView == null) {
			holder = new ViewHolder();
			v = mInflater.inflate(R.layout.private_user_list_item, parent,false);
			holder.iv = (ImageView)v.findViewById(R.id.iv_user_icon);
			holder.tv = (TextView)v.findViewById(R.id.tv_user_label);
			holder.kickOut = (TextView)v.findViewById(R.id.tv_kickout);
			holder.kickOut.setOnClickListener(mKickListener);
			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ViewHolder)v.getTag();
		}
		bindView(position,holder);
		return v;
	}
	
	public void setOnKickoutListener(View.OnClickListener l) {
		mKickListener = l;
	}
	
	private void bindView(int position,ViewHolder holder) {
		String text = mList.get(position);
		holder.tv.setText(text);
		holder.kickOut.setTag(text);
	}

	private class ViewHolder {
		ImageView iv;
		TextView tv;
		TextView kickOut;
	}
}

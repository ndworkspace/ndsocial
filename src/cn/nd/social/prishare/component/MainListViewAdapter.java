package cn.nd.social.prishare.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.nd.social.R;
import cn.nd.social.prishare.items.CellItemBase;
import cn.nd.social.prishare.items.FilesCellItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainListViewAdapter extends BaseAdapter {


	Map multipleMap;
	HashMap<String, View> fList;
	Collection<CellItemBase> itemSet;
	Set<String> fileSet;
	ArrayList<CellItemBase> arr = new ArrayList<CellItemBase>();
	LayoutInflater mInflate;
	

	
	public MainListViewAdapter(Collection multi) {

		itemSet = multi;
		
		for(CellItemBase item:itemSet){
			arr.add(item);	
		}		
	
		mInflate = (LayoutInflater)cn.nd.social.util.Utils.getAppContext()
				.getSystemService(android.app.Service.LAYOUT_INFLATER_SERVICE);
	}

	public MainListViewAdapter(Map multi,HashMap<String, View> fileList) {

		this.multipleMap = multi;
		this.fList = fileList;
		itemSet = multipleMap.keySet();
		fileSet = (Set<String>) fileList.keySet();
		

		for(CellItemBase item:itemSet){
			arr.add(item);
	
		}
		
		for (String item : fileSet) {
			arr.add(FilesCellItem.getFilesCellItem(item));
		}
		
	
		mInflate = (LayoutInflater)cn.nd.social.util.Utils.getAppContext()
				.getSystemService(android.app.Service.LAYOUT_INFLATER_SERVICE);
	}
	public void removeItemFromArr(int position){
		arr.remove(position);
	}

	public void removeItemFromArr(CellItemBase it){
		arr.remove(it);
	}
	public int getCount() {
		
		return arr.size();
	}

	public CellItemBase getItem(int position) {
		return arr.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		convertView = mInflate.inflate(R.layout.qe_dialog_list, null);
		ImageView black = (ImageView) convertView
				.findViewById(R.id.file_icon);
		TextView blue = (TextView) convertView
				.findViewById(R.id.file_name);
		ImageButton check = (ImageButton) convertView
				.findViewById(R.id.file_button);
		check.setTag(getItem(position));
		
		black.setImageDrawable(arr.get(position).getItemIcon());
		blue.setText(arr.get(position).getFileShortName());

		check.setOnClickListener(mListener);
		return convertView;
	}
	View.OnClickListener mListener;
	public void setCheckListener(View.OnClickListener onClickListener){
		mListener = onClickListener;
		
	}
}
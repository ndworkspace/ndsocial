package cn.nd.social.common;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.nd.social.R;

/**
 * Popup Menu: like options menu, but more fancy
 * 
 * */
public class PopMenu {
	private ArrayList<PopMenuItem> itemList;
	private Context context;
	private PopupWindow popupWindow ;
	private ListView listView;
	private View rootView;

	public PopMenu(Context context) {

		this.context = context;

		itemList = new ArrayList<PopMenuItem>();
		
		rootView = LayoutInflater.from(context).inflate(R.layout.popmenu, null);        

        listView = (ListView)rootView.findViewById(R.id.listview);
        listView.setAdapter(new PopAdapter());
        
        popupWindow = new PopupWindow(context);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popup_right);
/*        popupWindow = new PopupWindow(rootView, 
        		LayoutParams.WRAP_CONTENT, 
        		LayoutParams.WRAP_CONTENT);   */     
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources()));
	}


	public void setOnItemClickListener(OnItemClickListener listener) {
		//this.listener = listener;
		listView.setOnItemClickListener(listener);
	}


	public void addItems(PopMenuItem[] items) {
		for (PopMenuItem item : items)
			itemList.add(item);
	}

	/**
	 * PopMenuItem.itemId is the identify for every menu
	 * */
	public void addItem(PopMenuItem item) {
		itemList.add(item);
	}
	
	/**
	 * show dropdown menu to the right and bottom of the refView
	 * refView: the anchor view
	 * */
	public void showAsDropDown(View refView) {
		int width = context.getResources().getDimensionPixelSize(R.dimen.popmenu_width);
		popupWindow.setWidth(width);
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setContentView(rootView);
		
		
		rootView.setFocusableInTouchMode(true);
		rootView.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_MENU) {
					dismiss();
					return true;
				}
				return false;
			}
		});
		
		
		int[] location = new int[2];
		refView.getLocationOnScreen(location);
/*		int wrapContent = WindowManager.LayoutParams.WRAP_CONTENT;
		rootView.measure(wrapContent, wrapContent);*/
		int xPos = location[0] + refView.getWidth() - width
				- context.getResources().getDimensionPixelSize(R.dimen.popmenu_right_padding);
		
		int yPos = location[1] +  refView.getHeight();

		popupWindow.showAtLocation(refView, Gravity.NO_GRAVITY, xPos, yPos);
	}
	

	public void dismiss() {
		popupWindow.dismiss();
	}


	private final class PopAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.popmenu_item, null);
				holder = new ViewHolder();

				convertView.setTag(holder);

				holder.text = (TextView) convertView.findViewById(R.id.tv);
				holder.icon = (ImageView) convertView.findViewById(R.id.iv);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(itemList.get(position).getText());
			if(itemList.get(position).getIconId() > 0){
				holder.icon.setImageResource(itemList.get(position).getIconId());
			}else{
				holder.icon.setVisibility(View.GONE);
			}
			
			return convertView;
		}

		private final class ViewHolder {
			TextView text;
			ImageView icon;
		}
	}
}

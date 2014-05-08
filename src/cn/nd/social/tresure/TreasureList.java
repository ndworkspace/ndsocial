package cn.nd.social.tresure;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.common.PopMenu;
import cn.nd.social.common.PopMenuItem;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.util.CommonUtils;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.file.FileIconHelper;
import cn.nd.social.util.file.IntentBuilder;

public class TreasureList extends Activity {

	private Context mContext;
	private LayoutInflater mInflater;
	private FileIconHelper mIconHelper;
	
	public static String KEY_TREASURE_TYPE = "treasure_type";
	private int mTreasureType;	
	private int mIconId;
	
	
	private ListView mList;
	
	
	private final static int MENU_ITEM_MEDIA_PLAY = 0;
	private final static int MENU_ITEM_MEDIA_DETAIL = 1;
	private final static int MENU_ITEM_FILE_VIEW = 2;
	private final static int MENU_ITEM_FILE_DETAIL = 3;
	private final static int MENU_ITEM_APP_INSTALL = 4;
	private final static int MENU_ITEM_APP_DETAIL = 5;
	
	private class TreasureItem {
		int drawableRes;
		String  path;
		String  shortName;
		TreasureItem(int drawable,String shortPath,String wholePath) {
			
			drawableRes = drawable;		
			shortName = shortPath;
			path = wholePath;
		}
	}
	
	private ArrayList<TreasureItem> mItems = new ArrayList<TreasureItem>();
	private PrivateListAdapter mAdapter;
	private FileObserver mFileObserver;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mInflater = getLayoutInflater();
		Intent intent = getIntent();
		mTreasureType = intent.getIntExtra(KEY_TREASURE_TYPE, MsgDefine.FILE_TYPE_FILE);
		
		mIconHelper = new FileIconHelper();
		setContentView(R.layout.treasure_activity);
		setupViews();		

	}
	 
	@Override
	protected void onNewIntent(Intent intent) {
		if(mTreasureType != intent.getIntExtra(KEY_TREASURE_TYPE, MsgDefine.FILE_TYPE_FILE)) {
			return;
		}
		super.onNewIntent(intent);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void ensureIcon() {
		int icon;
		switch(mTreasureType) {
		case MsgDefine.FILE_TYPE_FILE:
			icon = R.drawable.pri_tab_file;
			break;
		case MsgDefine.FILE_TYPE_APP:
			icon = R.drawable.pri_tab_app;
			break;
		case MsgDefine.FILE_TYPE_MEDIA:
			icon = R.drawable.pri_tab_music;
			break;
		default:
			icon = R.drawable.pri_tab_file;
			break;
		}
		mIconId = icon;
	}
	
	private String getTitleStr() {
		int resId;
		switch(mTreasureType) {
		case MsgDefine.FILE_TYPE_FILE:
			resId = R.string.pri_file_label;
			break;
		case MsgDefine.FILE_TYPE_APP:
			resId = R.string.pri_app_label;
			break;
		case MsgDefine.FILE_TYPE_MEDIA:
			resId = R.string.pri_media_label;
			break;
		default:
			resId =R.string.pri_file_label;
			break;
		}
		return getString(resId);
	}
	
	private void setupViews() {
		TextView title = (TextView) findViewById(R.id.main_title);
		title.setText(getTitleStr());

		View backBtn = findViewById(R.id.back_btn);
		backBtn.setVisibility(View.VISIBLE);
		
		mList = (ListView)findViewById(R.id.treasure_list);
		
		findViewById(R.id.right_btn).setVisibility(View.GONE);
		
		backBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		
		
		String folderPath = FilePathHelper.getPrivateSharePath(mTreasureType);
		


		
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				TreasureItem item = mAdapter.getItem(position);
				onTreasureItemSelect(v,item);
			}
				
		});
		
		mAdapter = new PrivateListAdapter(mItems);
		mList.setAdapter(mAdapter);
		
		startFileObserver(folderPath);
		
		refreshFileList(folderPath);

	}
	
	
	private void startFileObserver(final String path) {
		if(mFileObserver != null) {
			mFileObserver.stopWatching();
		}
		
		mFileObserver = new FileObserver(path,FileObserver.CREATE | FileObserver.DELETE ) {			
			@Override
			public void onEvent(int event, String file) {
				refreshFileList(path);				
			}
		};
		mFileObserver.startWatching();
	}
	
	private void refreshFileList(String path) {
		mItems.clear();
		
		
		
		File f = new File(path);
		if(!f.exists() || !f.isDirectory()) {
			Toast.makeText(mContext, "file path:" + path + "not exist", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
        File[] listFiles = f.listFiles();
        if (listFiles == null) {
        	return ;
        }
 
        ensureIcon();
        for (File child : listFiles) {
        	mItems.add(new TreasureItem(mIconId,child.getName() ,child.getPath()));
        }

        mAdapter.notifyDataSetChanged();
		
	}
	
	private void onTreasureItemSelect(View anchor,final TreasureItem item) {
		final PopMenu menu = new PopMenu(mContext);
		if(mTreasureType == MsgDefine.FILE_TYPE_MEDIA) {
			menu.addItem(new PopMenuItem(MENU_ITEM_MEDIA_PLAY, getString(R.string.treasure_play), 0));
			menu.addItem(new PopMenuItem(MENU_ITEM_MEDIA_DETAIL, getString(R.string.treasure_detail), 0));
		} else if(mTreasureType == MsgDefine.FILE_TYPE_APP) {
			menu.addItem(new PopMenuItem(MENU_ITEM_APP_INSTALL, getString(R.string.treasure_install), 0));
			menu.addItem(new PopMenuItem(MENU_ITEM_APP_DETAIL, getString(R.string.treasure_detail), 0));
		} else {
			menu.addItem(new PopMenuItem(MENU_ITEM_FILE_VIEW, getString(R.string.treasure_view), 0));
			menu.addItem(new PopMenuItem(MENU_ITEM_FILE_DETAIL, getString(R.string.treasure_detail), 0));
		}
		
		menu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				PopMenuItem menuItem = (PopMenuItem)parent.getItemAtPosition(position);
				switch(menuItem.getItemId()) {
				case MENU_ITEM_MEDIA_PLAY: {
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					File file = new File(item.path);
					intent.setDataAndType(Uri.fromFile(file), "audio/*");
					startActivity(intent);
				}					
					break;
					
				case MENU_ITEM_MEDIA_DETAIL:
					showFileDetail(item);
					break;
					
				case MENU_ITEM_APP_INSTALL:
					CommonUtils.installApkNormal(item.path);
					break;
					
				case MENU_ITEM_APP_DETAIL:
					showFileDetail(item);
					break;
					
				case MENU_ITEM_FILE_VIEW: {
					IntentBuilder.viewFile(mContext, item.path);
				}
					break;
					
				case MENU_ITEM_FILE_DETAIL:
					showFileDetail(item);
					break;
				
				}
				menu.dismiss();	
			}
		});
		menu.showAsDropDown(anchor);
	}
	
	
	private void showFileDetail(TreasureItem item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				mContext);
		builder.setTitle(R.string.detail);
		builder.setMessage(getString(R.string.file_path) + ": "
				+ item.path);
		builder.setPositiveButton(R.string.OK,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
	

	private class PrivateListAdapter extends BaseAdapter {
		ArrayList<TreasureItem> itemList;
		
		PrivateListAdapter(ArrayList<TreasureItem> itemList) {
			this.itemList = itemList;
		}

		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public TreasureItem getItem(int position) {
			return itemList.get(position);
		}


		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v;
			if (convertView == null) {
				v = mInflater.inflate(R.layout.app_invite_list_item, parent,
						false);
				holder = new ViewHolder();

				holder.image = (ImageView) v
						.findViewById(R.id.iv_invite_item_icon);
				holder.text = (TextView) v
						.findViewById(R.id.tv_invite_item_ways);
				v.setTag(holder);
			} else {
				v = convertView;
				holder = (ViewHolder) v.getTag();
			}
			bindView(holder, position);

			return v;
		}

		private void bindView(ViewHolder holder, int position) {
			TreasureItem info = getItem(position);
			//holder.image.setImageResource(info.drawableRes);
			mIconHelper.setIcon(info.path, holder.image);
			//holder.image.setImageResource(info.drawableRes);
			holder.text.setTextColor(Color.WHITE);
			holder.text.setText(info.shortName);
		}

	}
	
	
	private class ViewHolder {
		ImageView image;
		TextView text;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {  
        return super.onContextItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}

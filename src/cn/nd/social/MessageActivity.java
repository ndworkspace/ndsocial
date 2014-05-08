package cn.nd.social;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardViewer;
import cn.nd.social.common.ImageViewer;
import cn.nd.social.data.CardOpenHelper;
import cn.nd.social.data.CardProvider;
import cn.nd.social.data.MsgDBHelper;
import cn.nd.social.data.MsgProviderSingleton;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.util.Utils;

public class MessageActivity extends Activity {
	private ListView mList;
	private MsgCursorAdapter mListAdapter;	

	public static final String INTENAL_ACTION_MSG_DATABASE_CHANGE_NOTIFY = "cn.nd.social.msg_database_change_notify";

	
	private final static int MENU_DELETE = 0;
	private final static int MENU_VIEW = 1;
	private Context mContext;
	
	/*
	 * BroadcastReceiver
	 */
	private BroadcastReceiver mDatabaseChangeNotify = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e("TabMsg", "receive " + action);

			Cursor cursor = MsgProviderSingleton.getInstance()
					.getLastRecordList();
			mListAdapter.changeCursor(cursor);
		}
	};
	
	
	public static void enterMessageList(Context context) {
		Intent intent = new Intent(context,MessageActivity.class);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.msg_activity);
		setupViews();
		registerReceiver(mDatabaseChangeNotify, new IntentFilter(
				INTENAL_ACTION_MSG_DATABASE_CHANGE_NOTIFY));

	}
	 
	@Override
	public void onDestroy() {
		unregisterReceiver(mDatabaseChangeNotify);
		super.onDestroy();
	}
	
	private void setupViews() {
		mList = (ListView) findViewById(R.id.msg_list);

		View emptyView =  findViewById(R.id.empty);
		mList.setEmptyView(emptyView);

		View back = findViewById(R.id.back_btn);
		back.setVisibility(View.VISIBLE);
		
		View right_btn = findViewById(R.id.right_btn);
		right_btn.setVisibility(View.INVISIBLE);
		
		TextView main_title = (TextView)findViewById(R.id.main_title);
		main_title.setText(R.string.qe_main_more);
		
		back.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		
		initListAdapter();
		
		Cursor cursor = MsgProviderSingleton.getInstance().getLastRecordList();
		mListAdapter.changeCursor(cursor);
		
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				mListAdapter.notifyDataSetChanged();
				handler.postDelayed(this, 1000);
			}
		};
		handler.postDelayed(runnable, 1000);

	}
	

	private void initListAdapter() {
		mListAdapter = new MsgCursorAdapter(mContext, null);
		mList.setAdapter(mListAdapter);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position,
					long arg3) {				
				Cursor cursor = (Cursor)mListAdapter.getItem(position);
				openMsgItem(cursor);				
			}
		});
		
		//register long click callback
		mList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				Cursor cursor = mListAdapter.getCursor();
				String name = cursor.getString(2);
				menu.setHeaderTitle(name);
				menu.add(0,MENU_DELETE,0,R.string.delete_msg);
				menu.add(0,MENU_VIEW,0,R.string.look_detail);
			}
			
		});
		
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.getPosition() >= 0) {
             switch (item.getItemId()) {
            case MENU_DELETE:
            	 MsgProviderSingleton.getInstance().deleteRecord(cursor.getLong(0));
            	 Intent intent = new Intent(
							MessageActivity.INTENAL_ACTION_MSG_DATABASE_CHANGE_NOTIFY);

            	 mContext.sendBroadcast(intent);
                break;
           
            case MENU_VIEW:
            	openMsgItem(cursor);
                break;

            default:
                break;
            }
        }
        return super.onContextItemSelected(item);
	}

	private void openMsgItem(Cursor msgCursor) {
		String name = msgCursor.getString(2);
		
		int type = msgCursor.getInt(msgCursor.getColumnIndex(MsgDBHelper.MSG_LIST_FILETYPE));
		int status = msgCursor.getInt(msgCursor.getColumnIndex(MsgDBHelper.MSG_LIST_STATUS));
		long statictime = msgCursor.getLong(msgCursor.getColumnIndex(MsgDBHelper.MSG_LIST_STATICTIME));
		long createTime = msgCursor.getLong(msgCursor.getColumnIndex(MsgDBHelper.MSG_LIST_CREATETIME));
		int time = msgCursor.getInt(msgCursor.getColumnIndex(MsgDBHelper.MSG_LIST_EXPIRETIME));
		String path = msgCursor.getString(msgCursor.getColumnIndex(MsgDBHelper.MSG_LIST_FILEPATH));
		String NewName = msgCursor.getString(msgCursor.getColumnIndex(MsgDBHelper.MSG_LIST_NEW_NAME));
		
		String fileName = path + "/" + NewName;
		Log.e("Image dd", "1:"+ time + "," + statictime + "," + status);
		
		File file = new File(fileName);
		boolean e = file.exists();
		if (type == MsgDefine.FILE_TYPE_IMAGE) {
			if(!file.exists()){
				new  AlertDialog.Builder(this).setTitle("该文件已销毁" ).setPositiveButton("确定" ,  null ).show(); 
				return;
			}
			Intent intent = new Intent(mContext, ImageViewer.class);
			if (time != -1) {
				intent.putExtra("type", MsgDefine.GRANT_FILE_AUTO_DESTROY);
			}else{
				intent.putExtra("type", 0);
			}			
			intent.putExtra("status",status);
			intent.putExtra("static", statictime/1000); 
			intent.putExtra("value", time);
			intent.putExtra("filename", fileName);
			startActivity(intent);
			return;
		}
		
		//TODO: distinguish message type
		if(type == MsgDefine.FILE_TYPE_CARD){
			String title = msgCursor.getString(3);
			String selection = CardOpenHelper.COLUMN_NAME + "=?";		
			Cursor cursor = mContext.getContentResolver().query(
					CardProvider.CONTENT_URI, CardUtil.CARD_LIST_PROJECTION, selection, new String[] {name},null);
			if(cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				int cardId = cursor.getInt(0);
				Intent intent = new Intent(mContext, CardViewer.class);
				intent.putExtra("card_id", cardId);
				intent.putExtra("name", name);
				intent.putExtra("title", title);
				intent.putExtra("utc", createTime);
				startActivity(intent);
			} else {
				Intent intent = new Intent(mContext, CardViewer.class);
				intent.putExtra("card_id", 0);
				intent.putExtra("name", name);
				intent.putExtra("title", title);
				intent.putExtra("utc", createTime);
				intent.putExtra("content", msgCursor.getString(3));
				startActivity(intent);
			}
			
			if (cursor != null) {
				cursor.close();
			}
		}
		
		
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Cursor cursor = MsgProviderSingleton.getInstance()
				.getLastRecordList();
		mListAdapter.changeCursor(cursor);
		super.onResume();
	}
	
	public class MsgCursorAdapter extends CursorAdapter {
		protected boolean mIsScrolling = false;
		private final LayoutInflater mFactory;
		
		public void setIsScrolling(boolean isScrolling) {
			this.mIsScrolling = isScrolling;
		}

		public MsgCursorAdapter(Context context, Cursor c) {
			super(context, c, false); /* auto-requery to false */
			mFactory = LayoutInflater.from(context);
		}
		

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			ImageView icon =  (ImageView)view.findViewById(R.id.iv);
			
			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(String.valueOf(cursor.getString(2)));

			TextView title = (TextView) view.findViewById(R.id.title);
//			title.setText(cursor.getString(3));

			long createUtc = cursor.getLong(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_CREATETIME));

			Date date = new Date(createUtc);
			String dateTime = Utils.getFormatTime(date);
			TextView last_time = (TextView) view.findViewById(R.id.last_time);
			last_time.setText(dateTime);
						
//			Date dateCreate = new Date(createUtc);
//			String createDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dateCreate);
//			TextView create_time = (TextView) view.findViewById(R.id.create_time); 
//			create_time.setText(getString(R.string.file_create_time) + dateCreate.toLocaleString());
			
			int file_type = (int)cursor.getLong(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_FILETYPE));
			String send_driction = cursor.getString(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_SENDDIRECTION));
			long static_time = cursor.getLong(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_STATICTIME));
			long exp_time = cursor.getLong(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_EXPIRETIME));
//			int status_file = cursor.getInt(cursor.getColumnIndex(MsgDBHelper.MSG_LIST_STATUS));
			
			TextView expire_time = (TextView) view.findViewById(R.id.expire_time);		
			String titleContent = "";
			if(!send_driction.equals("recv")){
				titleContent += "接收";
			}else{
				titleContent += "发送";
			}
			if(file_type == MsgDefine.FILE_TYPE_APP){
				titleContent += "应用";
				icon.setImageResource(R.drawable.pri_share_app);
			}else if(file_type == MsgDefine.FILE_TYPE_CARD){
				titleContent += "名片";
//				icon.setImageResource(R.drawable.pri_share_tab_bg);
			}else if(file_type == MsgDefine.FILE_TYPE_FILE){
				titleContent += "文件";
				icon.setImageResource(R.drawable.pri_share_file);
			}else if(file_type == MsgDefine.FILE_TYPE_TITLE){
				titleContent += "文件";
				
			}else if(file_type == MsgDefine.FILE_TYPE_MEDIA){
				titleContent += "文件";
				icon.setImageResource(R.drawable.pri_share_music);
			}else if(file_type == MsgDefine.FILE_TYPE_IMAGE){
				titleContent += "图片";
				icon.setImageResource(R.drawable.pri_share_gallery);
			}
			
			title.setText(titleContent);
			
			if (send_driction.equals("recv") && file_type == MsgDefine.FILE_TYPE_IMAGE && exp_time != -1) {
//				Date date_expire = new Date(static_time);	
//				Date now = new Date();
				expire_time.setVisibility(View.VISIBLE);
				long subtime = static_time - Calendar.getInstance().getTimeInMillis();
				if(subtime>0){
					String timeString = null;
					if(subtime > 1000*60*60){
						 timeString = subtime/1000/60/60 + "小时";
					}else if(subtime > 1000*60){
						 timeString = subtime/1000/60 + "分钟";
					}else if(subtime > 1000){
						timeString = subtime/1000 + "秒";
					}
					expire_time.setText("该图片将在" + timeString + "后销毁");	
				}else{
					expire_time.setText("该图片已销毁");
				}
			}else{	
				expire_time.setVisibility(View.GONE);				
			}
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View item = mFactory.inflate(R.layout.main_tab_msg_item, parent,
					false);
			return item;
		}
	}
}

package cn.nd.social.syncbrowsing.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cn.nd.social.R;
import cn.nd.social.data.CardProvider;
import cn.nd.social.data.SyncHistoryOpenHelper;
import cn.nd.social.data.SyncHistoryProvider;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.net.PrivateSwitcher;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.Utils;
import cn.nd.social.util.file.FileOperationHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SyncBrowserDialogActivity extends Activity{

	private View mImportLayout;
	private RelativeLayout mSubLayout;
	private SyncHistoryProvider mProvider;
	private Cursor mCursor;
	private TextView mTextNull;
	private View mSampleBtn;
	private ListView mSyncHistoryListView = null;
	private Context mContext;
	
	private PrivateSwitcher mPrivateSwitcher;
	private final static String KEY_HIDE_SYCN_HIDE = "show_enter_sync_hint";
	
	public final static String KEY_RESELECT_DOC = "reselect_doc";
	public final static String KEY_RESELECTED = "reselected";
	
	private boolean mIsReselect = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_browser_history);
		
/*		mPrivateSwitcher = new PrivateSwitcher();
		mPrivateSwitcher.enterPrivateState(true);*/
		
		mContext = this;
		setupView();
		setupListenerEvent();
		
		mProvider = SyncHistoryProvider.getInstance();
		mCursor = mProvider.getLastHistory();
		if (mCursor == null || mCursor.getCount() == 0)  {
			
			//tangtaotao@NetDragon_20140227
			copySampleFileIfNeed();
			
			toggleView(true);
			if(mCursor != null) {
				mCursor.close();
			}
		} else {
			toggleView(false);			
			initSyncHistoryView();
		}
		if(!Utils.isExternalStorageMounted()) {
			showInfo(R.string.sdcard_no_exist);
		}
		
		//tangtaotao@NetDragon_20140217
		

	}
	
	
	private void toggleView(boolean showEmpty) {
		int emptyVisible = showEmpty ? View.VISIBLE : View.GONE;
		int normalVisible = showEmpty ? View.GONE : View.VISIBLE;
		mTextNull.setVisibility(emptyVisible);
		mSampleBtn.setVisibility(emptyVisible);
		mSubLayout.setVisibility(normalVisible);

	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		mIsReselect = intent.getBooleanExtra(KEY_RESELECT_DOC, false);		
		super.onNewIntent(intent);
	}
	
	
	private void setupView(){
		mImportLayout = findViewById(R.id.import_layout);
		mSubLayout = (RelativeLayout)findViewById(R.id.sync_sub_layout);
		mSyncHistoryListView = (ListView)findViewById(R.id.sync_list);
		mTextNull = (TextView)findViewById(R.id.db_null);
		mSampleBtn = findViewById(R.id.sample_file);
		
	}
	
	private void setupListenerEvent(){
		
		getContentResolver().registerContentObserver(SyncHistoryProvider.HISTORY_URI,
				false, mUriChangeObserver);
		
		//TODO: select file in this activity and onActivityFinish to launch activity
		mImportLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!Utils.isExternalStorageMounted()) {
					showInfo(R.string.sdcard_no_exist);
					return;
				}
				Intent intent = new Intent(SyncBrowserDialogActivity.this,FileViewActivity.class);
				startActivityForResult(intent, RESULT_FIRST_USER);
			}
		});
		
		findViewById(R.id.sync_reback_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				onBackPressed();
			}
		});
		
		mSampleBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String path = getSampleFilePath();
				finishReturn(path);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESULT_FIRST_USER){
			if(resultCode == RESULT_OK){
				String filePath = data.getStringExtra(HostSyncActivity.FILE_ID_KEY);
				finishReturn(filePath);
			}
		}
	}

	private String getSampleFilePath() {
		return FilePathHelper.getSyncPath()+ File.separator + "sample.pdf";
	}
	
	private boolean copySampleFileIfNeed() {
		boolean result = false;
		String fileName = getSampleFilePath();
		File destFile = new File(fileName);
		if(destFile.exists() ) {
			if(destFile.isFile()) {
				return true;
			}
			destFile.delete();//not a file, maybe a directory, delete first
		}
		
	    try {	     
	      InputStream input = getResources().openRawResource(R.raw.sample);
	      result = FileOperationHelper.copyToFile(input, destFile);
	    } catch (Exception e1) {
	        e1.printStackTrace();
	    } 
	    return result;
	}
	
	
	public void syncBack(View v){
		this.finish();
	}
	
	/*********************history list initialization****************************/
	

	private SyncHistoryArrayAdapter mSyncArrayAdapter = null;
	private ArrayList<SyncHistoryItem> mArray ;


	private void getHistoryArrayFromCursor(Cursor cursor){
		
		String last_date = null;

		if (cursor == null)  return;

			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				SyncHistoryItem item = new SyncHistoryItem();
				
				
				long time = cursor.getLong(cursor.getColumnIndex(SyncHistoryOpenHelper.COLUMN_TIME));			
				String current_date = transferTimeToDate(time);								
				
				/*cursor.isFirst() or !current_date.equals(last_date) is basically the same */
				if (cursor.isFirst()) {
					SyncHistoryItem itemTitle = new SyncHistoryItem();
					itemTitle.setmHistoryName(current_date);
					itemTitle.setmHistoryType(MsgDefine.FILE_TYPE_TITLE);
					itemTitle.setmHistoryPath(null);
					itemTitle.setmHistoryTime(time);
					mArray.add(itemTitle);
				}else if (!current_date.equals(last_date)) {
					SyncHistoryItem itemTitle = new SyncHistoryItem();
					itemTitle.setmHistoryName(current_date);
					itemTitle.setmHistoryType(MsgDefine.FILE_TYPE_TITLE);
					itemTitle.setmHistoryPath(null);
					itemTitle.setmHistoryTime(time);
					mArray.add(itemTitle);					
				}
				
				item.setmHistoryName(cursor.getString(1));
				item.setmHistoryType(cursor.getLong(2));
				item.setmHistoryPath(cursor.getString(3));
				item.setmHistoryTime(cursor.getLong(4));
				
				last_date = current_date;
				
				mArray.add(item);		
			}
			cursor.close();
			
	}
	
	@SuppressWarnings("deprecation")
	private String transferTimeToDate(long time){
		Date date = new Date(time);		
		int year = date.getYear() + 1900;
		int month = date.getMonth() + 1;
		int day = date.getDate();		
		String y_m_d = String.valueOf(year) + "-" + String.valueOf(month)+ "-" + String.valueOf(day);
		return y_m_d;		
	}
	
	private void initSyncHistoryView() {

		mArray = new ArrayList<SyncHistoryItem>();
		getHistoryArrayFromCursor(mCursor);

		mSyncArrayAdapter = new SyncHistoryArrayAdapter(mContext,0, mArray);
		mSyncHistoryListView.setAdapter(mSyncArrayAdapter);

		setSyncHistoryListItemEvent();		
	}
	
	
	
	void setSyncHistoryListItemEvent(){
		
		mSyncHistoryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long id) {
				SyncHistoryItem item = mArray.get(pos);
				//should not response on title
				if(item.getmHistoryType() == MsgDefine.FILE_TYPE_TITLE) { 
					return;
				}

				String path = item.getmHistoryPath();
				finishReturn(path);
			}
		});	
	}
	
	
	private void finishReturn(String path) {
		//save history record
		String name = FilePathHelper.getNameFromFilepath(path);
		long currSec = System.currentTimeMillis();	
		SyncHistoryProvider hisProvider = SyncHistoryProvider.getInstance();
		if (!hisProvider.queryAndUpdate(name, MsgDefine.FILE_TYPE_FILE,path, currSec)) {
			hisProvider.addHistory(name, 
					MsgDefine.FILE_TYPE_FILE, path, currSec);
		}		
		
		//check sdcard mount
		if(!Utils.isExternalStorageMounted()) {
			showInfo(R.string.sdcard_no_exist);
			return;
		}
		
		//what if file get deleted or moved...
		if(path == null || !(new File(path).exists())) {
			showInfo(R.string.sync_file_no_exist);
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(HostSyncActivity.FILE_ID_KEY, path);
		setResult(RESULT_OK, intent);
		this.finish();
		//TODO: update the list, the newly clicked item should be listed on top
	
	}
	
	/**
	 * when add or update a history record,the SyncHistoryProvider manually notify the change action,
	 * should update listview
	 * */
	private final ContentObserver mUriChangeObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfUpdate) {
			mCursor = mProvider.getLastHistory();
			if (mCursor == null || mCursor.getCount() == 0)  {
				toggleView(true);
				if(mCursor != null) {
					mCursor.close();
				}
				return;
			} else {
				toggleView(false);
			}
			
			initSyncHistoryView();
		}
	};
	
	private void showInfo(int resId) {
		Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		//mPrivateSwitcher.exitPrivateState();
		getContentResolver().unregisterContentObserver(mUriChangeObserver);
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(HostSyncActivity.getSyncActivity() != null) {
			HostSyncActivity.getSyncActivity().finish();
		}
		super.onBackPressed();
	}
}

package cn.nd.social.data;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cn.nd.social.MessageActivity;
import cn.nd.social.SocialApplication;
import cn.nd.social.hotspot.MsgDefine;

public class MsgProviderSingleton {
	// /////////////////////////////////////////////////////////////////////
	// singleton
	// /////////////////////////////////////////////////////////////////////
	private static MsgProviderSingleton _instance = null;
	public static final int STATIC_TIME = 600;

	synchronized public static MsgProviderSingleton getInstance() {
		if (_instance == null) {
			Context context = SocialApplication.getAppInstance();
			_instance = new MsgProviderSingleton(context);
		}
		return _instance;
	}

	public final Cursor getLastRecordList() {
		return mProvider.getLastRecordList();
	}

	public final long addRecord(long userId, String userName, String content,
			String filePath, String oriName, String newName,
			String SendDirection, Integer FileType, Integer validTime,
			Integer status) {
		return mProvider.addRecord(userId, userName, content, filePath,
				oriName, newName, SendDirection, FileType, validTime, status);
	}
	/**
	 * modify record status
	 * @param fileName
	 * @param status
	 * @return
	 */
	public final long updateRecord(String fileName,int status){
		return mProvider.updateRecord(fileName, status);
	}
	
	/**
	 * modify time of file destroy
	 * @param fileName
	 * @param deleteTime
	 * @return
	 */
	public final long updateRecord(String fileName,long deleteTime, boolean isDelete){
		return mProvider.updateRecord(fileName, deleteTime,isDelete);
	}

	public final long updateRecordBoth(String fileName,long deleteTime,int status,Context context){
		
		long flag = mProvider.updateRecordBoth(fileName, deleteTime, status);
		Intent intentAction = new Intent(MessageActivity.INTENAL_ACTION_MSG_DATABASE_CHANGE_NOTIFY);
		context.sendBroadcast(intentAction);
		return flag;
		
	}
	public final long deleteRecord(long id) {
		return mProvider.deleteRecord(id);
	}
	
	public final Cursor getTimeoutFile() {
		return mProvider.getTimeoutFile();
	}
	
	public final Cursor queryCursor(String fullPath){
		return mProvider.queryFile(fullPath);
	}

	// /////////////////////////////////////////////////////////////////////
	private MsgProvider mProvider = null;

	private MsgProviderSingleton(Context context) {
		mProvider = new MsgProvider(context);
	}

	public class MsgProvider {
		private MsgDBHelper mOpenHelper;

		public MsgProvider(Context context) {
			mOpenHelper = new MsgDBHelper(context);

			// addRecord(123, "test for msgProvider");
		}

		public final Cursor getLastRecordList() {
			return mOpenHelper.getReadableDatabase().query(
					MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, null, null, null,
					null, null, null);
		}

		public final long addRecord(long userId, String userName,
				String content, String filePath, String oriName,
				String newName, String SendDirection, Integer FileType,
				Integer validTime, Integer status) {
			ContentValues contentvalues = new ContentValues();

			contentvalues.put(MsgDBHelper.MSG_LIST_USER_ID,
					Long.valueOf(userId));
			contentvalues.put(MsgDBHelper.MSG_LIST_USER_NAME, userName);
			contentvalues.put(MsgDBHelper.MSG_LIST_CONTENT, content);

			contentvalues.put(MsgDBHelper.MSG_LIST_FILEPATH, filePath);
			contentvalues.put(MsgDBHelper.MSG_LIST_ORIGINAL_NAME, oriName);
			contentvalues.put(MsgDBHelper.MSG_LIST_NEW_NAME, newName);
			contentvalues
					.put(MsgDBHelper.MSG_LIST_SENDDIRECTION, SendDirection);
			contentvalues.put(MsgDBHelper.MSG_LIST_FILETYPE, FileType);

			Long currentTime = Calendar.getInstance().getTimeInMillis();
			if (validTime > STATIC_TIME) {
				contentvalues.put(MsgDBHelper.MSG_LIST_STATICTIME, currentTime
						+ validTime * 1000);
			} else {
				contentvalues.put(MsgDBHelper.MSG_LIST_STATICTIME,
						currentTime + STATIC_TIME * 1000);
			}

			contentvalues.put(MsgDBHelper.MSG_LIST_EXPIRETIME, validTime);

			contentvalues.put(MsgDBHelper.MSG_LIST_CREATETIME, currentTime);

			contentvalues.put(MsgDBHelper.MSG_LIST_STATUS, status);

			return mOpenHelper.getWritableDatabase()
					.insert(MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, null,
							contentvalues);

			// todo : insert msg_detail_list record
		}

		
		public final long updateRecord(String fileName,Integer status) {
			
			ContentValues contentvalues = new ContentValues();			
			contentvalues.put(MsgDBHelper.MSG_LIST_STATUS, status);

			String[] newName = fileName.split("/");
			String[] args = {newName[newName.length - 1]};
			
			long x = mOpenHelper.getWritableDatabase()
			.update(MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, contentvalues,
					"newname=?",args);	
			return x;
		}
		
		public final long updateRecordBoth(String fileName,long deleteTime,Integer status) {
			
			ContentValues contentvalues = new ContentValues();			
			contentvalues.put(MsgDBHelper.MSG_LIST_STATUS, status);
			contentvalues.put(MsgDBHelper.MSG_LIST_STATICTIME, deleteTime);	

			String[] newName = fileName.split("/");
			String[] args = {newName[newName.length - 1]};
			
			long x = mOpenHelper.getWritableDatabase()
			.update(MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, contentvalues,
					"newname=?",args);	
			return x;
		}
		
		public final Cursor queryFile(String fullPath) {
			String[] newName = fullPath.split("/");
			String[] args = {newName[newName.length - 1]};
	
			Cursor s = mOpenHelper.getReadableDatabase().query(
					MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, null,  
					MsgDBHelper.MSG_LIST_NEW_NAME + "=?", args,
					null, null, null);
			Log.e("open img cursor 1:", "queryfile:"+s.getCount());
			return s;
		}
		

		

		
		public final Cursor getTimeoutFile() {
			long currTime = System.currentTimeMillis();
			String whereArgs[] = new String[2];
			whereArgs[0] = "" + currTime;
			whereArgs[1] = "" + MsgDefine.STATUS_NEED_DELETE;
			Cursor s = mOpenHelper.getReadableDatabase().query(
					MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, null,  MsgDBHelper.MSG_LIST_STATICTIME + " <= ? AND " 
							+ MsgDBHelper.MSG_LIST_STATUS + ">=?"  , whereArgs,
					null, null, null);
			return s;
		}
		
		public final long updateRecord(String fileName,long deleteTime,boolean isDelete) {
			
			ContentValues contentvalues = new ContentValues();
			contentvalues.put(MsgDBHelper.MSG_LIST_STATICTIME, deleteTime);	

			String[] newName = fileName.split("/");
			String[] args = {newName[newName.length - 1]};
	
			
			return mOpenHelper.getWritableDatabase()
					.update(MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, contentvalues,
							"newname=?",args);
		}
			

		public final long deleteRecord(long id) {
			String whereClause = MsgDBHelper.COLUMN_ID + "=?";
			String[] whereArgs = new String[1];
			whereArgs[0] = String.valueOf(id);
			return mOpenHelper.getWritableDatabase().delete(
					MsgDBHelper.TABLE_MSG_LAST_RECORD_LIST, whereClause,
					whereArgs);
		}
	}
}

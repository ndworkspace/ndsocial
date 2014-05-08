package cn.nd.social.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.nd.social.SocialApplication;
import cn.nd.social.data.MsgDBHelper;
import cn.nd.social.services.ISocialService.FileControlPara;

public class FileControlProvider {
	// /////////////////////////////////////////////////////////////////////
		// singleton
		// /////////////////////////////////////////////////////////////////////
		private static FileControlProvider _instance = null;
		public static final int DEFAULT_STORE_TIME = 60 * 30;//secs
		
		private MsgDBHelper mOpenHelper;
		
		private FileControlProvider(Context context) {
			mOpenHelper = new MsgDBHelper(context);
		}
		
		synchronized public static FileControlProvider getInstance() {
			if (_instance == null) {
				Context context = SocialApplication.getAppInstance();
				_instance = new FileControlProvider(context);
			}
			return _instance;
		}

		public final Cursor getTimeoutFile() {
			long currTime = System.currentTimeMillis() /1000;
			String selection = MsgDBHelper.COLUMN_FC_STATIC_TIME + "<=" + currTime;
			String whereArgs[] = new String[1];
			whereArgs[0] = "" + currTime;
			return mOpenHelper.getReadableDatabase().query(
					MsgDBHelper.TABLE_FC_PRIVATE, null,  MsgDBHelper.COLUMN_FC_STATIC_TIME + " <= ?", whereArgs,
					null, null, null);
		}
		
		public final Cursor getValidFile() {
			return mOpenHelper.getReadableDatabase().query(
					MsgDBHelper.TABLE_FC_PRIVATE, null, null, null,
					null, null, null);
		}

		public final Cursor queryFile(String filename) {
			SQLiteDatabase sqlitedatabase = mOpenHelper.getReadableDatabase();
			String whereArgs[] = new String[1];
			whereArgs[0] = filename;
			return sqlitedatabase.query(MsgDBHelper.TABLE_FC_PRIVATE, null,
					MsgDBHelper.COLUMN_FC_FILENAME + " = ?", whereArgs, null,
					null, null);
		}
		
		public final long addRecord(FileControlPara file) {
			ContentValues contentvalues = new ContentValues();
			contentvalues.put(MsgDBHelper.COLUMN_FC_FILENAME, file.fileName);
			contentvalues.put(MsgDBHelper.COLUMN_FC_CREATE_TIME, file.startTime);
			long storeTime;
			if(file.expireTime > file.staticTime) {
				storeTime = file.expireTime;
			} else {
				storeTime = file.staticTime;
			}
			long staticTime = file.startTime + storeTime;
			contentvalues.put(MsgDBHelper.COLUMN_FC_STATIC_TIME, staticTime);
			contentvalues.put(MsgDBHelper.COLUMN_FC_EXPIRE_TIME, file.expireTime);
			contentvalues.put(MsgDBHelper.COLUMN_FC_CONTROL_TYPE, file.controlType);
			contentvalues.put(MsgDBHelper.COLUMN_FC_STATE, 0);//reserved
			return mOpenHelper.getWritableDatabase().insert(
					MsgDBHelper.TABLE_FC_PRIVATE, null, contentvalues);
		}
		
		public final long updateRecord(String fileName,long deleteTime) {			
			ContentValues contentvalues = new ContentValues();
			contentvalues.put(MsgDBHelper.COLUMN_FC_STATIC_TIME, deleteTime);	

			String[] args = {fileName};
			return mOpenHelper.getWritableDatabase()
					.update(MsgDBHelper.TABLE_FC_PRIVATE, contentvalues,
							"filename=?",args);
		}
	
		public final long deleteRecord(long fieldId) {
			String whereClause = MsgDBHelper.COLUMN_FC_FILEID + "=" + fieldId;
			return mOpenHelper.getWritableDatabase().delete(
					MsgDBHelper.TABLE_FC_PRIVATE, whereClause,null);
		}
		
		
		public final long deleteRecord(String fileName) {
			String whereClause = MsgDBHelper.COLUMN_FC_FILENAME + "=" + fileName;
			return mOpenHelper.getWritableDatabase().delete(
					MsgDBHelper.TABLE_FC_PRIVATE, whereClause,null);
		}


		public final void updateExpireTime(long fileId, long expireTime) {
			ContentValues contentvalues = new ContentValues();
			contentvalues.put(MsgDBHelper.COLUMN_FC_STATIC_TIME, expireTime);
			SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
			String whereArgs[] = new String[1];
			whereArgs[0] = String.valueOf(fileId);
			sqlitedatabase.update(MsgDBHelper.TABLE_FC_PRIVATE, contentvalues, "_id=?", whereArgs);
		}
		
		///////////////////////////////////end//////////////////////////

}

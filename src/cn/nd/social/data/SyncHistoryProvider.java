package cn.nd.social.data;

import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import cn.nd.social.SocialApplication;
import cn.nd.social.util.Utils;

public class SyncHistoryProvider {
	// /////////////////////////////////////////////////////////////////////
	// singleton
	// /////////////////////////////////////////////////////////////////////

	public static final String[] HISTORY_LIST_PROJECTION = {
			SyncHistoryOpenHelper.COLUMN_ID, SyncHistoryOpenHelper.COLUMN_NAME,
			SyncHistoryOpenHelper.COLUMN_TYPE,
			SyncHistoryOpenHelper.COLUMN_PATH,
			SyncHistoryOpenHelper.COLUMN_TIME };

	public static final Uri HISTORY_URI = Uri
			.parse("content://sync_history_db");

	private static SyncHistoryProvider sInstance = null;
	
	
	private SyncHistoryOpenHelper mOpenHelper;

	synchronized public static SyncHistoryProvider getInstance() {
		if (sInstance == null) {
			Context context = SocialApplication.getAppInstance();
			sInstance = new SyncHistoryProvider(context);
		}
		return sInstance;
	}

	public final Cursor getLastHistory() {
		return mOpenHelper.getReadableDatabase().query(
				SyncHistoryOpenHelper.TABLE_HISTORY, HISTORY_LIST_PROJECTION,
				null, null, null, null,
				SyncHistoryOpenHelper.COLUMN_TIME + " COLLATE LOCALIZED DESC");
	}

	public final long addHistory(String filename, long type, String path,
			long time) {
		ContentValues contentvalues = new ContentValues();

		contentvalues.put(SyncHistoryOpenHelper.COLUMN_NAME, filename);
		contentvalues.put(SyncHistoryOpenHelper.COLUMN_PATH, path);
		contentvalues.put(SyncHistoryOpenHelper.COLUMN_TYPE, type);
		contentvalues.put(SyncHistoryOpenHelper.COLUMN_TIME, time);

		ContentResolver cr = Utils.getAppContext().getContentResolver();
		cr.notifyChange(HISTORY_URI, null);

		return mOpenHelper.getWritableDatabase().insert(
				SyncHistoryOpenHelper.TABLE_HISTORY, null, contentvalues);

	}

	public final boolean queryAndUpdate(String filename, long type,
			String path, long time) {
		SQLiteDatabase database = mOpenHelper.getReadableDatabase();

		Cursor cursor = database.rawQuery("select * from "
				+ SyncHistoryOpenHelper.TABLE_HISTORY + " where "
				+ SyncHistoryOpenHelper.COLUMN_PATH + " =?",
				new String[] { path });
		if (cursor == null)
			return false;
		if (cursor.getCount() == 0) {
			cursor.close();
			return false;
		}
		//TODO:
		//problem: what if today and yesterday both have the same pdf file,
		//cursor.moveToFirst() may refer to yesterday's, so today's doc does not get updated
		cursor.moveToFirst();
		long t = cursor.getLong(cursor
				.getColumnIndex(SyncHistoryOpenHelper.COLUMN_TIME));
		Date cursor_d = new Date(t);
		Date curr_d = new Date(time);
		long distance = (time - t) / (24 * 60 * 60 * 1000);
		if (distance < 1 && curr_d.getDate() == cursor_d.getDate()) {
			ContentValues v = new ContentValues();
			v.put(SyncHistoryOpenHelper.COLUMN_NAME, filename);
			v.put(SyncHistoryOpenHelper.COLUMN_PATH, path);
			v.put(SyncHistoryOpenHelper.COLUMN_TYPE, type);
			v.put(SyncHistoryOpenHelper.COLUMN_TIME, time);
			mOpenHelper.getWritableDatabase().update(
					SyncHistoryOpenHelper.TABLE_HISTORY, v,
					SyncHistoryOpenHelper.COLUMN_ID + "=?",
					new String[] { String.valueOf(cursor.getLong(0)) });
			ContentResolver cr = Utils.getAppContext().getContentResolver();
			cr.notifyChange(HISTORY_URI, null);
		}
		cursor.close();
		return true;
	}

	public final long deleteRecord(String filename, long type, String path,
			long time) {
		String whereClause = SyncHistoryOpenHelper.COLUMN_ID + "=? AND " 
			+ SyncHistoryOpenHelper.COLUMN_TYPE + "=? AND "
			+ SyncHistoryOpenHelper.COLUMN_PATH + "=? AND "
			+ SyncHistoryOpenHelper.COLUMN_TIME + "=?";
		String[] whereArgs = new String[4];
		whereArgs[0] = filename;
		whereArgs[1] = String.valueOf(type);
		whereArgs[2] = path;
		whereArgs[3] = String.valueOf(time);
		return mOpenHelper.getWritableDatabase().delete(
				SyncHistoryOpenHelper.TABLE_HISTORY, whereClause, whereArgs);
	}

	private SyncHistoryProvider(Context context) {
		mOpenHelper = new SyncHistoryOpenHelper(context);
	}

}

package cn.nd.social.prishare.history;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import cn.nd.social.SocialApplication;
import cn.nd.social.data.MsgDBHelper;
import cn.nd.social.prishare.history.HistoryItemHelper.ItemData;

public class HistoryDBProviderSingleton {
	// /////////////////////////////////////////////////////////////////////
	// singleton
	// /////////////////////////////////////////////////////////////////////
	private static HistoryDBProviderSingleton _instance = null;

	synchronized public static HistoryDBProviderSingleton getInstance() {
		if (_instance == null) {
			Context context = SocialApplication.getAppInstance();
			_instance = new HistoryDBProviderSingleton(context);
		}
		return _instance;
	}

	public final Cursor getAllRecord() {
		return mProvider.getAllRecord();
	}

	public final long addRecord(ItemData data) {
		return mProvider.addRecord(data);
	}

	// /////////////////////////////////////////////////////////////////////
	private HistoryDBProvider mProvider = null;

	private HistoryDBProviderSingleton(Context context) {
		mProvider = new HistoryDBProvider(context);
	}

	public class HistoryDBProvider {
		private MsgDBHelper mOpenHelper;

		public HistoryDBProvider(Context context) {
			mOpenHelper = new MsgDBHelper(context);
		}

		public final Cursor getAllRecord() {
			return mOpenHelper.getReadableDatabase().query(
					MsgDBHelper.TABLE_QE_HISTORY, null, null, null, null, null,
					null);
		}

		public final long addRecord(ItemData data) {
			Log.d("History DB Provider Singleton", "add Record");

			ContentValues contentvalues = new ContentValues();

			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_USER_ID, 0L);
			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_SEND_NAME,
					data.mSendUserName);
			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_RECV_NAME,
					data.mRecvUserName);

			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_APP_NAME,
					data.mAppName);
			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_FILE_NAME,
					data.mFileName);

			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_FILE_SIZE,
					data.mFileSize);
			
			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_FILE_TYPE,
					data.mFileType);

			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_GRANT_TYPE,
					data.mGrantType);
			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_GRANT_VALUE,
					data.mGrantValue);
			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_GRANT_RESERVE,
					data.mGrantReserve);

			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_PROGRESS,
					data.mProgress);
			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_INSTALL, 0L);

			contentvalues.put(MsgDBHelper.COLUMN_QE_HISTORY_UTC, Calendar
					.getInstance().getTimeInMillis());

			return mOpenHelper.getWritableDatabase().insert(
					MsgDBHelper.TABLE_QE_HISTORY, null, contentvalues);

			// todo : insert msg_detail_list record
		}
	}
}

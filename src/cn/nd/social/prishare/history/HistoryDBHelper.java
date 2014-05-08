package cn.nd.social.prishare.history;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDBHelper extends SQLiteOpenHelper {

	private Context mContext;

	private static final String DATABASE_NAME = "ndsocial.db";

	// public static final String TABLE_QE_HISTORY = "qe_history";
	//
	// public static final String COLUMN_ID = "_id";
	//
	// // msg_last_record_list
	// public static final String COLUMN_QE_HISTORY_USER_ID = "userid";
	// public static final String COLUMN_QE_HISTORY_SEND_NAME = "sendname";
	// public static final String COLUMN_QE_HISTORY_RECV_NAME = "recvname";
	// public static final String COLUMN_QE_HISTORY_APP_NAME = "appname";
	// public static final String COLUMN_QE_HISTORY_FILE_NAME = "filename";
	// public static final String COLUMN_QE_HISTORY_PROGRESS = "progress";
	// public static final String COLUMN_QE_HISTORY_INSTALL = "installflag";
	// public static final String COLUMN_QE_HISTORY_UTC = "utc";

	public HistoryDBHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);

		mContext = context;
	}

	public HistoryDBHelper(Context context, CursorFactory factory, int version) {
		super(context, DATABASE_NAME, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// db.execSQL("DROP TABLE IF EXISTS " + TABLE_QE_HISTORY);
		//
		// createTables(db);
	}

	// private void createTables(SQLiteDatabase db) {
	//
	// db.execSQL("CREATE TABLE " + TABLE_QE_HISTORY + "(" + COLUMN_ID
	// + " INTEGER PRIMARY KEY AUTOINCREMENT,"
	// + COLUMN_QE_HISTORY_USER_ID + " INTEGER NOT NULL,"
	// + COLUMN_QE_HISTORY_SEND_NAME + " TEXT NOT NULL,"
	// + COLUMN_QE_HISTORY_RECV_NAME + " TEXT NOT NULL,"
	// + COLUMN_QE_HISTORY_APP_NAME + " TEXT NOT NULL,"
	// + COLUMN_QE_HISTORY_FILE_NAME + " TEXT NOT NULL,"
	// + COLUMN_QE_HISTORY_PROGRESS + " INTEGER NOT NULL,"
	// + COLUMN_QE_HISTORY_INSTALL + " INTEGER NOT NULL,"
	// + COLUMN_QE_HISTORY_UTC + " INTEGER NOT NULL" + ");");
	//
	// db.execSQL("CREATE INDEX folderNameIndex ON " + TABLE_QE_HISTORY + " ("
	// + COLUMN_QE_HISTORY_USER_ID + ");");
	// }

}

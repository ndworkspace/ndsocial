package cn.nd.social.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SyncHistoryOpenHelper extends SQLiteOpenHelper {
	private Context mContext;
	private static final String DATABASE_NAME = "synchistory.db";
	public static final String TABLE_HISTORY = "synchistory";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TYPE = "filetype";
	public static final String COLUMN_PATH = "path";	
	public static final String COLUMN_TIME = "time";


	public SyncHistoryOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
		mContext = context;
	}

	public SyncHistoryOpenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
		createTables(db);

	}

	private void createTables(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_HISTORY + "(" + COLUMN_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME
				+ " TEXT," + COLUMN_TYPE + " INTEGER," + COLUMN_PATH + " TEXT,"
				+ COLUMN_TIME + " INTEGER" + ");");
		
	}
}

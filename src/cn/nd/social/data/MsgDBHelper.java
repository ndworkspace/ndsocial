package cn.nd.social.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MsgDBHelper extends SQLiteOpenHelper {

	private Context mContext;

	private static final String DATABASE_NAME = "ndsocial.db";

	public static final String TABLE_MSG_LAST_RECORD_LIST = "msg_last_record_list";
	public static final String TABLE_MSG_DETAIL_LIST = "msg_detail_list";

	public static final String COLUMN_ID = "_id";
	public final static String NullValue = "";

	// msg_last_record_list
	public static final String MSG_LIST_USER_ID = "userid";
	public static final String MSG_LIST_USER_NAME = "username";
	public static final String MSG_LIST_CONTENT = "content";
	public static final String MSG_LIST_FILEPATH = "filepath";
	public static final String MSG_LIST_ORIGINAL_NAME = "originalname";
	public static final String MSG_LIST_NEW_NAME = "newname";
	public static final String MSG_LIST_SENDDIRECTION = "direction";
	public static final String MSG_LIST_FILETYPE = "filetype";
	public static final String MSG_LIST_STATICTIME = "statictime";
	public static final String MSG_LIST_EXPIRETIME = "expiretime";
	public static final String MSG_LIST_CREATETIME = "createtime";
	public static final String MSG_LIST_STATUS = "status";
	

	// msg_detail_list
	public static final String COLUMN_MSG_DETAIL_LIST_USER_ID = "userid";
	public static final String COLUMN_MSG_DETAIL_LIST_CONTENT = "content";
	public static final String COLUMN_MSG_DETAIL_LIST_UTC = "utc";

	public MsgDBHelper(Context context) {
		super(context, DATABASE_NAME, null, 2);
		mContext = context;
	}

	public MsgDBHelper(Context context, CursorFactory factory, int version) {
		super(context, DATABASE_NAME, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
		createTriggers(db);

		createQETables(db);

		createFileControlTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSG_LAST_RECORD_LIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSG_DETAIL_LIST);

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_QE_HISTORY);

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FC_PRIVATE);

		createTables(db);
		createTriggers(db);

		createQETables(db);

		createFileControlTables(db);
	}

	private void createTables(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + TABLE_MSG_LAST_RECORD_LIST + "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MSG_LIST_USER_ID + " INTEGER NOT NULL,"
				+ MSG_LIST_USER_NAME + " TEXT NOT NULL,"
				+ MSG_LIST_CONTENT + " TEXT NOT NULL,"
				+ MSG_LIST_FILEPATH + " TEXT NOT NULL,"
				+ MSG_LIST_ORIGINAL_NAME + " TEXT NOT NULL,"
				+ MSG_LIST_NEW_NAME + " TEXT NOT NULL,"
				+ MSG_LIST_SENDDIRECTION + " TEXT NOT NULL,"
				+ MSG_LIST_FILETYPE + " INTEGER NOT NULL,"
				+ MSG_LIST_STATICTIME + " INTEGER NOT NULL,"
				+ MSG_LIST_EXPIRETIME + " INTEGER NOT NULL," 
				+ MSG_LIST_CREATETIME + " INTEGER NOT NULL,"
				+ MSG_LIST_STATUS + " INTEGER NOT NULL"+ ");");

		db.execSQL("CREATE TABLE " + TABLE_MSG_DETAIL_LIST + "(" + COLUMN_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMN_MSG_DETAIL_LIST_USER_ID + " INTEGER NOT NULL,"
				+ COLUMN_MSG_DETAIL_LIST_CONTENT + " TEXT NOT NULL,"
				+ COLUMN_MSG_DETAIL_LIST_UTC + " INTEGER NOT NULL" + ");");

		db.execSQL("CREATE INDEX folderNameIndex ON " + TABLE_MSG_DETAIL_LIST
				+ " (" + COLUMN_ID + ");");
	}

	private void createTriggers(SQLiteDatabase db) {
		// StringBuilder strBuilder = new StringBuilder();
		// db.execSQL("CREATE TRIGGER folder_deleted BEFORE DELETE ON folder BEGIN DELETE FROM file WHERE folder_id = OLD._id; END;");
		//
		// strBuilder = new StringBuilder();
		// strBuilder.append("CREATE TRIGGER file_delete ");
		// strBuilder.append("AFTER DELETE ON file BEGIN ");
		// strBuilder.append("UPDATE folder SET file_count = file_count - 1 WHERE _id = OLD.folder_id; ");
		// strBuilder.append("END;");
		// db.execSQL(strBuilder.toString());
		//
		// strBuilder = new StringBuilder();
		// strBuilder.append("CREATE TRIGGER file_insert ");
		// strBuilder.append("BEFORE INSERT ON file BEGIN ");
		// strBuilder.append("UPDATE folder SET file_count = file_count + 1 WHERE _id = NEW.folder_id; ");
		// strBuilder.append("END;");
		// db.execSQL(strBuilder.toString());
		//
		// strBuilder = new StringBuilder();
		// strBuilder.append("CREATE TRIGGER file_update ");
		// strBuilder.append("AFTER UPDATE OF folder_id ON file BEGIN ");
		// strBuilder.append("UPDATE folder SET file_count = file_count + 1 WHERE _id = NEW.folder_id; ");
		// strBuilder.append("UPDATE folder SET file_count = file_count - 1 WHERE _id = OLD.folder_id; ");
		// strBuilder.append("END;");
		// db.execSQL(strBuilder.toString());
	}

	public static final String TABLE_QE_HISTORY = "qe_history";

	// msg_last_record_list
	public static final String COLUMN_QE_HISTORY_USER_ID = "userid";
	public static final String COLUMN_QE_HISTORY_SEND_NAME = "sendname";
	public static final String COLUMN_QE_HISTORY_RECV_NAME = "recvname";
	public static final String COLUMN_QE_HISTORY_APP_NAME = "appname";
	public static final String COLUMN_QE_HISTORY_FILE_NAME = "filename";
	public static final String COLUMN_QE_HISTORY_FILE_SIZE = "filesize";
	public static final String COLUMN_QE_HISTORY_FILE_TYPE = "filetype";
	public static final String COLUMN_QE_HISTORY_GRANT_TYPE = "grant_type";
	public static final String COLUMN_QE_HISTORY_GRANT_VALUE = "grant_value";
	public static final String COLUMN_QE_HISTORY_GRANT_RESERVE = "grant_reserve";
	public static final String COLUMN_QE_HISTORY_PROGRESS = "progress";
	public static final String COLUMN_QE_HISTORY_INSTALL = "installflag";
	public static final String COLUMN_QE_HISTORY_UTC = "utc";

	private void createQETables(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + TABLE_QE_HISTORY + "(" + COLUMN_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMN_QE_HISTORY_USER_ID + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_SEND_NAME + " TEXT NOT NULL,"
				+ COLUMN_QE_HISTORY_RECV_NAME + " TEXT NOT NULL,"
				+ COLUMN_QE_HISTORY_APP_NAME + " TEXT NOT NULL,"
				+ COLUMN_QE_HISTORY_FILE_NAME + " TEXT NOT NULL,"
				+ COLUMN_QE_HISTORY_FILE_SIZE + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_FILE_TYPE + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_GRANT_TYPE + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_GRANT_VALUE + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_GRANT_RESERVE + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_PROGRESS + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_INSTALL + " INTEGER NOT NULL,"
				+ COLUMN_QE_HISTORY_UTC + " INTEGER NOT NULL" + ");");

		db.execSQL("CREATE INDEX QEHistoryIndex ON " + TABLE_QE_HISTORY + " ("
				+ COLUMN_QE_HISTORY_USER_ID + ");");
	}

	public static final String TABLE_FC_PRIVATE = "fc_private";
	public static final String COLUMN_FC_FILEID = "fileid";
	public static final String COLUMN_FC_FILENAME = "filename";
	public static final String COLUMN_FC_CREATE_TIME = "createtime";
	public static final String COLUMN_FC_STATIC_TIME = "static_time";
	public static final String COLUMN_FC_EXPIRE_TIME = "expire_time";
	public static final String COLUMN_FC_CONTROL_TYPE = "control_type";
	public static final String COLUMN_FC_STATE = "state";

	private void createFileControlTables(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_FC_PRIVATE + "(" + COLUMN_FC_FILEID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_FC_FILENAME
				+ " TEXT NOT NULL," + COLUMN_FC_CREATE_TIME
				+ " INTEGER NOT NULL," + COLUMN_FC_STATIC_TIME
				+ " INTEGER NOT NULL," + COLUMN_FC_EXPIRE_TIME
				+ " INTEGER NOT NULL," + COLUMN_FC_CONTROL_TYPE
				+ " INTEGER NOT NULL," + COLUMN_FC_STATE + " INTEGER NOT NULL"
				+ ");");

		// db.execSQL("CREATE INDEX QEHistoryIndex ON " + TABLE_QE_HISTORY +
		// " ("
		// + COLUMN_QE_HISTORY_USER_ID + ");");
	}

}

package cn.nd.social.privategallery;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PrivateGalleryDBHelper extends SQLiteOpenHelper {
	private Context mContext;
	private static final String DATABASE_NAME = "privategallery.db";
	public static final String TABLE_FILE = "file";
	public static final String TABLE_FOLDER = "folder";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FILE_NAME = "name";
	public static final String COLUMN_FILE_FOLDER_ID = "folder_id";
	public static final String COLUMN_FILE_TYPE = "type";
	public static final String COLUMN_FILE_PATH = "path";
	public static final String COLUMN_FILE_THUMB_PATH = "thumb_path";
	public static final String COLUMN_FILE_MIMETYPE = "mime_type";
	public static final String COLUMN_FILE_ORG_NAME = "org_name";
	public static final String COLUMN_FILE_ORG_PATH = "org_path";
	public static final String COLUMN_FILE_BOOKMARK = "bookmark";
	public static final String COLUMN_FILE_ORIENTATION = "orientation";
	public static final String COLUMN_FILE_ORG_HEADER = "org_file_header";
	public static final String COLUMN_FILE_ORG_HEADER_BLOB = "org_file_header_blob";
	public static final String COLUMN_FILE_ENCRYPTION = "encripted";
	public static final String COLUMN_FILE_UTC = "create_date_utc";

	public static final String COLUMN_FOLDER_NAME = "name";
	public static final String COLUMN_FOLDER_FILE_COUNT = "file_count";
	public static final String COLUMN_FOLDER_IMAGE_FILE_ID = "folder_image_file_id";
	public static final String COLUMN_FOLDER_TYPE = "type";
	public static final String COLUMN_FOLDER_UTC = "create_date_utc";

	public PrivateGalleryDBHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
		mContext = context;
	}

	public PrivateGalleryDBHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
		createTriggers(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDER);

		createTables(db);
		createTriggers(db);
	}

	private void createTables(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + TABLE_FILE + "(" + COLUMN_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_FILE_NAME
				+ " TEXT NOT NULL," + COLUMN_FILE_FOLDER_ID
				+ " INTEGER NOT NULL," + COLUMN_FILE_TYPE
				+ " INTEGER NOT NULL," + COLUMN_FILE_PATH + " TEXT NOT NULL,"
				+ COLUMN_FILE_THUMB_PATH + " TEXT NOT NULL,"
				+ COLUMN_FILE_MIMETYPE + " TEXT NOT NULL,"
				+ COLUMN_FILE_ORG_NAME + " TEXT NOT NULL,"
				+ COLUMN_FILE_ORG_PATH + " TEXT NOT NULL,"
				+ COLUMN_FILE_BOOKMARK + " INTEGER NOT NULL DEFAULT 0,"
				+ COLUMN_FILE_ORIENTATION + " INTEGER NOT NULL DEFAULT 0,"
				+ COLUMN_FILE_ORG_HEADER + " TEXT,"
				+ COLUMN_FILE_ORG_HEADER_BLOB + " BLOB,"
				+ COLUMN_FILE_ENCRYPTION + " INTEGER NOT NULL DEFAULT 0,"
				+ COLUMN_FILE_UTC + " INTEGER NOT NULL" + ");");

		db.execSQL("CREATE TABLE " + TABLE_FOLDER + "(" + COLUMN_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_FOLDER_NAME
				+ " TEXT," + COLUMN_FOLDER_FILE_COUNT
				+ " INTEGER NOT NULL DEFAULT 0," + COLUMN_FOLDER_IMAGE_FILE_ID
				+ " INTEGER NOT NULL DEFAULT -1," + COLUMN_FOLDER_TYPE
				+ " INTEGER NOT NULL DEFAULT 0," + COLUMN_FOLDER_UTC
				+ " INTEGER" + ");");

		db.execSQL("CREATE INDEX folderNameIndex ON " + TABLE_FOLDER + " ("
				+ COLUMN_FOLDER_NAME + ");");
	}

	private void createTriggers(SQLiteDatabase db) {
		StringBuilder strBuilder = new StringBuilder();
		db.execSQL("CREATE TRIGGER folder_deleted BEFORE DELETE ON folder BEGIN DELETE FROM file WHERE folder_id = OLD._id; END;");

		strBuilder = new StringBuilder();
		strBuilder.append("CREATE TRIGGER file_delete ");
		strBuilder.append("AFTER DELETE ON file BEGIN ");
		strBuilder
				.append("UPDATE folder SET file_count = file_count - 1 WHERE _id = OLD.folder_id; ");
		strBuilder.append("END;");
		db.execSQL(strBuilder.toString());

		strBuilder = new StringBuilder();
		strBuilder.append("CREATE TRIGGER file_insert ");
		strBuilder.append("BEFORE INSERT ON file BEGIN ");
		strBuilder
				.append("UPDATE folder SET file_count = file_count + 1 WHERE _id = NEW.folder_id; ");
		strBuilder.append("END;");
		db.execSQL(strBuilder.toString());

		strBuilder = new StringBuilder();
		strBuilder.append("CREATE TRIGGER file_update ");
		strBuilder.append("AFTER UPDATE OF folder_id ON file BEGIN ");
		strBuilder
				.append("UPDATE folder SET file_count = file_count + 1 WHERE _id = NEW.folder_id; ");
		strBuilder
				.append("UPDATE folder SET file_count = file_count - 1 WHERE _id = OLD.folder_id; ");
		strBuilder.append("END;");
		db.execSQL(strBuilder.toString());
	}
}

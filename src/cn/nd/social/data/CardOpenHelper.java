package cn.nd.social.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CardOpenHelper extends SQLiteOpenHelper {
	private Context mContext;
	private static final String DATABASE_NAME = "contactlist.db";
	public static final String TABLE_CONTACT = "cardcontent";
	public static final String TABLE_CARD_NET = "cardnet";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_MOBILE = "mobile";
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_EMAIL = "email";
	public static final String COLUMN_ADDR = "address";
	public static final String COLUMN_COMPANY = "company";
	public static final String COLUMN_FAVORITE = "favorite";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_NET_SYNC = "net_synchronize";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_BG_ID = "bg_id";
	public static final String COLUMN_AVATAR_ID = "avatar_id";
	public static final String COLUMN_AVATAR_URL = "avatar_url";

	public static final String NET_COLUMN_ID = "_id";
	public static final String NET_COLUMN_USER_ID = "net_user_id";
	public static final String NET_COLUMN_SYCN = "net_sync";
	public static final String NET_COLUMN_STATUS = "card_status";
	public static final String NET_COLUMN_TIMESTAMP = "net_timestamp";

	public CardOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, 2);
		mContext = context;
	}

	public CardOpenHelper(Context context, String name, CursorFactory factory,
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
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARD_NET);
		createTables(db);

	}

	private void createTables(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_CONTACT + "(" + COLUMN_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME
				+ " TEXT," + COLUMN_TITLE + " TEXT," + COLUMN_MOBILE + " TEXT,"
				+ COLUMN_PHONE + " TEXT," + COLUMN_EMAIL + " TEXT,"
				+ COLUMN_ADDR + " TEXT," + COLUMN_COMPANY + " TEXT,"
				+ COLUMN_FAVORITE + " INTEGER," + COLUMN_NET_SYNC + " INTEGER,"
				+ COLUMN_USER_ID + " INTEGER," + COLUMN_TIME + " INTEGER,"
				+ COLUMN_BG_ID + " INTEGER,"+ COLUMN_AVATAR_ID + " INTEGER,"
				+ COLUMN_AVATAR_URL + " TEXT"
				+ ");");

		db.execSQL("CREATE TABLE " + TABLE_CARD_NET + "(" + NET_COLUMN_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + NET_COLUMN_USER_ID
				+ " INTEGER," + NET_COLUMN_SYCN + " INTEGER,"
				+ NET_COLUMN_STATUS + " INTEGER," + NET_COLUMN_TIMESTAMP
				+ " INTEGER" + ");");
	}
}

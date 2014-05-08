package cn.nd.social.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class CardProvider extends ContentProvider {

	private CardOpenHelper mOpenHelper;
	private static final String AUTHORITY = "cn.nd.social.card";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri UPDATE_URI = Uri.parse("content://" + AUTHORITY
			+ "/update");
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/cards";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/card";
	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int CARDS = 1;
	private static final int CARD_ID = 2;
	private static final int CARD_NAME = 3;
	private static final int CARD_TITLE = 4;
	private static final int CARD_MOBILE = 5;
	private static final int CARD_PHONE = 6;
	private static final int CARD_EMAIL = 7;
	private static final int CARD_ADDR = 8;
	private static final int CARD_COMPANY = 9;
	private static final int CARD_USER_ID = 10;
	private static final int CARD_NETY_SYNC = 11;

	private static final int CARD_FAVORITE = 10;

	static {
		URI_MATCHER.addURI(AUTHORITY, null, CARDS);
		URI_MATCHER.addURI(AUTHORITY, "#", CARD_ID);
		URI_MATCHER.addURI(AUTHORITY, "name", CARD_NAME);
		URI_MATCHER.addURI(AUTHORITY, "title", CARD_TITLE);
		URI_MATCHER.addURI(AUTHORITY, "mobile", CARD_MOBILE);
		URI_MATCHER.addURI(AUTHORITY, "phone", CARD_PHONE);
		URI_MATCHER.addURI(AUTHORITY, "email", CARD_EMAIL);
		URI_MATCHER.addURI(AUTHORITY, "address", CARD_ADDR);
		URI_MATCHER.addURI(AUTHORITY, "company", CARD_COMPANY);
		URI_MATCHER.addURI(AUTHORITY, "favorite", CARD_FAVORITE);
		URI_MATCHER.addURI(AUTHORITY, "userid", CARD_USER_ID);
		URI_MATCHER.addURI(AUTHORITY, "netsync", CARD_NETY_SYNC);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rowsDeleted = 0;
		int match = URI_MATCHER.match(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (match) {
		case CARDS:
			db.delete(CardOpenHelper.TABLE_CONTACT, selection, selectionArgs);
			break;
		case CARD_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(CardOpenHelper.TABLE_CONTACT,
						CardOpenHelper.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = db.delete(CardOpenHelper.TABLE_CONTACT,
						CardOpenHelper.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		case CARD_FAVORITE:

			break;
		default:

			break;
		}
		ContentResolver cr = getContext().getContentResolver();
		cr.notifyChange(CONTENT_URI, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = URI_MATCHER.match(uri);
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.insert(CardOpenHelper.TABLE_CONTACT, null, values);
		ContentResolver cr = getContext().getContentResolver();
		cr.notifyChange(uri, null);
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mOpenHelper = new CardOpenHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// only one table currently
		queryBuilder.setTables(CardOpenHelper.TABLE_CONTACT);
		switch (URI_MATCHER.match(uri)) {
		case CARDS:

			break;
		case CARD_ID:
			queryBuilder.appendWhere(CardOpenHelper.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		case CARD_NAME:
			queryBuilder.appendWhere(CardOpenHelper.COLUMN_NAME + "="
					+ uri.getLastPathSegment());
			break;
		case CARD_TITLE:
			break;
		case CARD_COMPANY:
			break;
		case CARD_MOBILE:
			break;
		case CARD_PHONE:
			break;
		case CARD_EMAIL:
			break;
		case CARD_ADDR:
			break;
		case CARD_FAVORITE:
			break;
		default:
			break;
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		switch (URI_MATCHER.match(uri)) {
		case CARD_USER_ID:
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			db.update(CardOpenHelper.TABLE_CONTACT, values, selection, null);
			ContentResolver cr = getContext().getContentResolver();
			cr.notifyChange(UPDATE_URI, null);
			return 1;
		default:
			break;
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		if(selection != null) {
			db.update(CardOpenHelper.TABLE_CONTACT, values, selection, selectionArgs);
		} else {
			String where = CardOpenHelper.COLUMN_ID + "="
					+ uri.getLastPathSegment();
			db.update(CardOpenHelper.TABLE_CONTACT, values, where, null);
		}
		ContentResolver cr = getContext().getContentResolver();
		cr.notifyChange(UPDATE_URI, null);
		return 0;
	}

}

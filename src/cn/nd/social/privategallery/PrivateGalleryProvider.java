package cn.nd.social.privategallery;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.nd.social.SocialApplication;

/**
 * singleton provider
 * */
public class PrivateGalleryProvider {
	private PrivateGalleryDBHelper mOpenHelper;
	public static String ACITON_PRI_GALLERY_DATA_CHANGE = "cn.nd.social.pri_gallery_data";

	private PrivateGalleryProvider(Context context) {
		mOpenHelper = new PrivateGalleryDBHelper(context);
	}

	private static PrivateGalleryProvider sInstance;

	public static synchronized PrivateGalleryProvider getInstance() {
		if (sInstance == null) {
			Context context = SocialApplication.getAppInstance();
			sInstance = new PrivateGalleryProvider(context);
		}
		return sInstance;
	}

	public final Cursor getFileList() {
		return mOpenHelper.getReadableDatabase().query(
				PrivateGalleryDBHelper.TABLE_FILE, null, null, null, null,
				null, null);
	}

	public final Cursor getFilesInFolder(long folderId) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		String[] folderStr = new String[1];
		folderStr[0] = String.valueOf(folderId);
		return db.query(PrivateGalleryDBHelper.TABLE_FILE, null,
				"folder_id = ?", folderStr, null, null, null);
	}

	public final Cursor queryImageFiles(long folderId) {
		SQLiteDatabase sqlitedatabase = mOpenHelper.getReadableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(folderId);
		return sqlitedatabase.query("file", null,
				"folder_id = ? AND mime_type LIKE 'image/%'", whereArgs, null,
				null, null);
	}

	public final void updateFileOrientation(long fileId, int orientation) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put("orientation", Integer.valueOf(orientation));
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(fileId);
		sqlitedatabase.update("file", contentvalues, "_id=?", whereArgs);
	}

	public final void updateFolderId(long fileId, long folderId) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put("folder_id", Long.valueOf(folderId));
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(fileId);
		sqlitedatabase.update("file", contentvalues, "_id=?", whereArgs);
	}

	public final void updateFileName(long fileId, String fileName) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put("name", fileName);
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(fileId);
		sqlitedatabase.update("file", contentvalues, "_id=?", whereArgs);
	}

	public final void updateFilePath(long id, String path, String thumbPath,
			String orgPath) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put("path", path);
		contentvalues.put("thumb_path", thumbPath);
		contentvalues.put("org_path", orgPath);
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(id);
		int result = sqlitedatabase.update("file", contentvalues, "_id=?", whereArgs);
		if(result > 0) {
			onDataChange();
		}
	}

	public final boolean updateEncryptState(long id, boolean isEncrypt) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put("encripted", Boolean.valueOf(isEncrypt));
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(id);
		return sqlitedatabase.update("file", contentvalues, "_id=?", whereArgs) > 0;
	}

	public final boolean updateFileHeaderBlob(long id, byte headerBlob[]) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put("org_file_header_blob", headerBlob);
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(id);
		return sqlitedatabase.update("file", contentvalues, "_id=?", whereArgs) > 0;
	}

	public final boolean deleteFile(long id) {
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = String.valueOf(id);
		boolean result =  sqlitedatabase.delete("file", "_id=?", whereArgs) > 0;
		if(result) {
			onDataChange();
		}
		return result;
	}

	public final boolean deleteFile(String  pathName) {
		SQLiteDatabase sqlitedatabase = mOpenHelper.getWritableDatabase();
		String whereArgs[] = new String[1];
		whereArgs[0] = pathName;
		boolean result = sqlitedatabase.delete("file", "path=?", whereArgs) > 0;
		if(result) {
			onDataChange();
		}
		return result;
	}
	
	public final long addFile(PrivateItemEntity item) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put("name", item.name);
		contentvalues.put("folder_id", Long.valueOf(item.folderId));
		contentvalues.put("mime_type", item.mimeType);
		contentvalues.put("org_name", item.orgName);
		contentvalues.put("org_path", item.orgPath);
		contentvalues.put("path", item.path);
		contentvalues.put("type", item.type);
		contentvalues.put("thumb_path", item.thumbPath);
		contentvalues.put("bookmark", item.bookmark);
		contentvalues.put("orientation", item.orientation);
		contentvalues.put("create_date_utc", item.createUtc);
		contentvalues.put("org_file_header_blob", item.headerBlob);
		contentvalues.put("encripted", item.isEncrypt ? 1 : 0);
		long result = mOpenHelper.getWritableDatabase().insert("file", null,
				contentvalues);
		if(result > 0 ) {
			onDataChange();
		}
		return result;
	}
	
	private void onDataChange() {
		Intent intent = new Intent(ACITON_PRI_GALLERY_DATA_CHANGE);
		Context context = SocialApplication.getAppInstance();
		context.sendBroadcast(intent);
	}

}

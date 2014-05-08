package cn.nd.social.privategallery;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import cn.nd.social.hotspot.Utils;
import cn.nd.social.prishare.items.CellItemBase;

public class GalleryGridItem extends CellItemBase{

	private Cursor mCursor;
	private int idColumn;
	private int nameColumn;
	private int folderColumn;
	private int typeColumn;
	private int pathColumn;
	private int thumbPathColumn;
	private int mineTypeColumn;
	private int orgPathColumn;
	private int orgNameColumn;
	private int utcColumn;
	private int headerBlobColumn;
	private int encryptColumn;
	private int orientationColumn;
	private int bookmarkColumn;
	
	
	private Drawable icon;
	private String path;
	private String thumbPath;
	private String shortName;
	private int pos;
	private int id;
	private String orgPath;
	
	public GalleryGridItem(int type, Cursor cursor) {
		super(IMAGE_TYPE);
		getItemFromCursor(cursor);
		Bitmap bp = BitmapFactory.decodeFile(mCursor.getString(thumbPathColumn));
		this.icon = new BitmapDrawable(bp);
		this.path = mCursor.getString(pathColumn);
		this.shortName = Utils.getFileName(this.path); 
		this.pos = cursor.getPosition();
		this.id = mCursor.getInt(idColumn);
		this.orgPath = mCursor.getString(orgPathColumn);
		this.thumbPath = mCursor.getString(thumbPathColumn);
	}

	public void getItemFromCursor(Cursor cursor) {
		mCursor = cursor;
		idColumn = mCursor.getColumnIndex("_id");
		nameColumn = mCursor.getColumnIndex("name");
		folderColumn = mCursor.getColumnIndex("folder_id");
		typeColumn = mCursor.getColumnIndex("type");
		pathColumn = mCursor.getColumnIndex("path");
		thumbPathColumn = mCursor.getColumnIndex("thumb_path");
		mineTypeColumn = mCursor.getColumnIndex("mime_type");
		orgPathColumn = mCursor.getColumnIndex("org_path");
		orgNameColumn = mCursor.getColumnIndex("org_name");
		utcColumn = mCursor.getColumnIndex("create_date_utc");
		headerBlobColumn = mCursor.getColumnIndex("org_file_header_blob");
		encryptColumn = mCursor.getColumnIndex("encripted");
		orientationColumn = mCursor.getColumnIndex("orientation");
		bookmarkColumn = mCursor.getColumnIndex("bookmark");// tangtaotao may be
															// wrong
	}
	
	public final Cursor getItemCursor() {
		return mCursor;
	}
	
	public final String getItemName() {
		return mCursor.getString(nameColumn);
	}

	public final String getItemOrgName() {
		return mCursor.getString(orgNameColumn);
	}
	public final int getItemBookmark() {
		return mCursor.getInt(bookmarkColumn);
	}

	public final long getItemId() {
		return id;
	}



	public final String getItemOrgPath() {
		return orgPath;
	}
	

	public final String getItemThumbPath() {
		return thumbPath;
	}
	
	

	public final PrivateItemEntity buildItemEntity() {
		PrivateItemEntity entity = new PrivateItemEntity();
		entity.id = mCursor.getInt(idColumn);
		entity.name = mCursor.getString(nameColumn);
		entity.folderId = mCursor.getLong(folderColumn);
		entity.type = mCursor.getInt(typeColumn);
		entity.path = mCursor.getString(pathColumn);
		entity.thumbPath = mCursor.getString(thumbPathColumn);
		entity.mimeType = mCursor.getString(mineTypeColumn);
		entity.orgPath = mCursor.getString(orgPathColumn);
		entity.orgName = mCursor.getString(orgNameColumn);
		entity.createUtc = mCursor.getLong(utcColumn);
		entity.orientation = mCursor.getInt(orientationColumn);
		entity.bookmark = mCursor.getInt(bookmarkColumn);
		if (mCursor.getInt(encryptColumn) == 1) {
			entity.isEncrypt = true;
		} else {
			entity.isEncrypt = false;
		}
		entity.headerBlob = mCursor.getBlob(headerBlobColumn);

		return entity;
	}

	@Override
	public Drawable getItemIcon() {		
		return icon;
	}

	@Override
	public String getFileShortName() {
		return shortName;
	}

	@Override
	public String getItemPath() {
		return path;
	}
	
	public int getPos(){
		return pos;
	}
}

package cn.nd.social.privategallery;

import java.io.File;

import android.provider.MediaStore;
import android.util.Log;
import cn.nd.social.util.BitmapUtils;

public class PrivateItemEntity {
	private final static String TAG = "PrivateItemEntity";
	
	public long id;
	public String name;
	public long folderId;
	public int type; // enum
	public String path;
	public String thumbPath;
	public String mimeType;
	public String orgPath;
	public String orgName;
	public long createUtc;
	public byte[] headerBlob;
	public boolean isEncrypt;
	public int orientation;
	public int bookmark; //user bookmark as filecontrol type

	public final String getFilePathSmall() {
		return path + "_small";
	}

	public final String getFilePath() {
		return path + "_";
	}

	public static final PrivateItemEntity from(PrivateItemOrginalInfo fileInfo, long createUtc) {
		PrivateItemEntity item = new PrivateItemEntity();
		item.createUtc = createUtc;
		item.name = String.valueOf(item.createUtc / 1000);
		item.path = Utils.getPrivateFilePath() + "/" + item.name;
		item.thumbPath = Utils.getPrivateThumbPath() + "/" + item.name;
		item.mimeType = Utils.getMimeType(Utils
				.getExtensionName(fileInfo.filename));
		item.type = Utils.getImageType(Utils
				.getExtensionName(fileInfo.filename));
		item.orgName = fileInfo.filename;
		item.orgPath = fileInfo.orgPath;

		item.bookmark = fileInfo.controlType;
		
		return item;
	}
	
	public static final boolean addFileToPrivateGallery(
			PrivateGalleryProvider provider, PrivateItemEntity item) {
		if(!BitmapUtils.extractThumbnail(item.orgPath,item.thumbPath)) {
			return false;
		}
		
		Log.d(TAG, "Move file to [" + item.path + "] from [" + item.orgPath + "]");

		Utils.copyFile(item.orgPath, item.path);
		//delete original file
		try {
			String params[] = new String[]{item.orgPath};
			cn.nd.social.util.Utils.getAppContext().getContentResolver()
						.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
								MediaStore.Images.Media.DATA + " LIKE ?", params);
			new File(item.orgPath).delete();
		} catch(Exception e) {
			
		}
		return provider.addFile(item) > 0;
	}

	public static final boolean storePrivateFile(
			final PrivateGalleryProvider provider,
			final PrivateItemOrginalInfo fileInfo) {
		/*
		 * new Thread() {
		 * 
		 * @Override public void run() { PrivateItemEntity item =
		 * from(fileInfo); addFileToDataBase(provider, item); } }.start();
		 */
		PrivateItemEntity item = from(fileInfo,System.currentTimeMillis());
		boolean result = addFileToPrivateGallery(provider, item);
		return result;

	}

	public static class PrivateItemOrginalInfo {
		public String filename;
		public String orgPath;
		public int type;
		public int controlType = 0;
	}
}

package cn.nd.social.util;

import java.io.File;

import android.content.res.Resources;
import android.os.Environment;
import cn.nd.social.R;
import cn.nd.social.hotspot.MsgDefine;

/**
 * FilePathHelper intends for providing path query
 * all path using in this app should be configured here
 * */
public class FilePathHelper {
	private final static String sRootPath;
	private final static String sSyncDir;

	private final static String sTmpPath ;
	private final static String sPrivateSharePath;
	
	private final static String sPrivateGalley;
	private final static String sPrivateApp;
	private final static String sPrivateMedia;
	private final static String sPrivateFile;
	
	static {
		
		sRootPath = Environment.getExternalStorageDirectory() + File.separator
				+ "ndsocial";
		File dir = new File(sRootPath);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		sSyncDir = sRootPath + File.separator + "sync";
		dir = new File(sSyncDir);
		if (!dir.exists()) {
			dir.mkdir();
		}	
		
		
		sTmpPath = sRootPath + File.separator + "tmp";
		
		dir = new File(sTmpPath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		sPrivateSharePath  = sRootPath + File.separator + "private_share";

		dir = new File(sPrivateSharePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		
		
		Resources res = Utils.getAppContext().getResources();
		
		sPrivateGalley = sPrivateSharePath + File.separator
						+ res.getString(R.string.pri_image_label);
		dir = new File(sPrivateGalley);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		

		sPrivateMedia = sPrivateSharePath + File.separator
						+ res.getString(R.string.pri_media_label);
		
		dir = new File(sPrivateMedia);
		if(!dir.exists()) {
			dir.mkdirs();
		}

		sPrivateApp = sPrivateSharePath + File.separator
						+ res.getString(R.string.pri_app_label);
		
		dir = new File(sPrivateApp);
		if(!dir.exists()) {
			dir.mkdirs();
		}

		sPrivateFile = sPrivateSharePath + File.separator
						+ res.getString(R.string.pri_file_label);
		dir = new File(sPrivateFile);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
	}
	
	public static String getWaveAckFile() {
		return sTmpPath + File.separator + "ackData.wav";
	}
	
	public static String getWaveTransFile() {
		return sTmpPath + File.separator + "transData.wav";
	}

	public static String getTmpFilePath() {
		return sTmpPath;
	}
	
	public static String getPrivateSharePath(int fileType) {
		final String path;
		switch(fileType) {
		case MsgDefine.FILE_TYPE_APP:
			path = sPrivateApp;
			break;
		case MsgDefine.FILE_TYPE_IMAGE:
			path = sPrivateGalley;
			break;
		case MsgDefine.FILE_TYPE_MEDIA:
			path = sPrivateMedia;
			break;
		case MsgDefine.FILE_TYPE_FILE:
			path = sPrivateFile;
			break;
		default:
			path = sPrivateFile;
			break;
		}
		return path;
	}

	public static String getSyncPath() {
		return sSyncDir;
	}

	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator))
			return path1 + path2;
	
		return path1 + File.separator + path2;
	}

	public static String getExtFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(dotPosition + 1, filename.length());
		}
		return "";
	}

	public static String getNameFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(0, dotPosition);
		}
		return "";
	}

	public static String getPathFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(0, pos);
		}
		return "";
	}

	public static String getNameFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(pos + 1);
		}
		return filepath;
		// return "";
	}
}

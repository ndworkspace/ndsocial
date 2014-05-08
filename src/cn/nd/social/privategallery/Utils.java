package cn.nd.social.privategallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class Utils {
	public final static String PRIVATE_GALLERY_PASSWD = "private_passwd";
	public final static int MIN_PASSWD_LEN = 4;
	public final static String PRIVATE_THUMB_PATH = "ndsocial/privategallery/thumb";
	public final static String PRIVATE_FILE_PATH = "ndsocial/privategallery/file";
	

	public static String getPrivateFilePath() {
		String dirStr = Environment.getExternalStorageDirectory() + "/"
				+ PRIVATE_FILE_PATH;
		File dir = new File(dirStr);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dirStr;
	}

	public static String getPrivateThumbPath() {
		String dirStr = Environment.getExternalStorageDirectory() + "/"
				+ PRIVATE_THUMB_PATH;
		File dir = new File(dirStr);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dirStr;
	}
	
	public static String getPrivateThumbFileByFilePath(String pathName) {
		File temp = new File(pathName.trim());
		String fileName = temp.getName();
		String thumbPathName = getPrivateThumbPath() + "/" + fileName;
		return thumbPathName;
	}

	public static void moveFile(String inputPathName, String outputPath) {

		InputStream in = null;
		OutputStream out = null;
		try {

			in = new FileInputStream(inputPathName);
			out = new FileOutputStream(outputPath);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;

			// write the output file
			out.flush();
			out.close();
			out = null;

			// delete the original file
			new File(inputPathName).delete();

		} catch (FileNotFoundException fnfe1) {
			Log.e("tag", fnfe1.getMessage());
		} catch (Exception e) {
			Log.e("tag", e.getMessage());
		}

	}

	public static void copyFile(String inputPathName, String outputPath) {

		InputStream in = null;
		OutputStream out = null;
		try {

			in = new FileInputStream(inputPathName);
			out = new FileOutputStream(outputPath);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;

			// write the output file
			out.flush();
			out.close();
			out = null;
		} catch (FileNotFoundException fnfe1) {
			Log.e("tag", fnfe1.getMessage());
		} catch (Exception e) {
			Log.e("tag", e.getMessage());
		}

	}
	
	
	
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}

	public static String getMimeType(String fileExtentsion) {
		String ext = fileExtentsion.toLowerCase();
		String mimeType = "";
		if (ext.equals("jpg")) {
			mimeType = "image/jpeg";
		} else if (ext.equals("png")) {
			mimeType = "image/png";
		}
		return mimeType;
	}

	public static int getImageType(String fileExtentsion) {
		String ext = fileExtentsion.toLowerCase();
		int type = 0;
		if (ext.equals("jpg")) {
			type = 1;
		} else if (ext.equals("png")) {
			type = 2;
		}
		return type;
	}
	
	public static int getSampleSize(BitmapFactory.Options options,int targetWidth,int targetHeigth) {
		final int heightRatio = Math.round((float) options.outHeight
				/ (float) targetHeigth);
		final int widthRatio = Math.round((float) options.outWidth
				/ (float) targetWidth);
		int inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		return inSampleSize;
	}

	
}

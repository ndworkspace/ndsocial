package cn.nd.social.prishare;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
import cn.nd.social.R;
import cn.nd.social.prishare.items.AudioCellItem;
import cn.nd.social.prishare.items.FilesCellItem;
import cn.nd.social.prishare.items.ImageCellItem;
import cn.nd.social.util.Utils;

public class PriUtils {

	public static Cursor getAllAudio(Context context) {
		Cursor cursor = null;
		if (Utils.isExternalStorageMounted()) {
			ContentResolver cr = context.getContentResolver();
			String[] projection = { MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.DISPLAY_NAME };
			/*
			 * String selection = MediaStore.Audio.Media.DATA + " like ? or " +
			 * MediaStore.Audio.Media.DATA + " like ?";
			 */
			// String[] selectionArg = new String[] { "%dcim%", "%music%" };
			cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					projection, null, null, null);
		}
	
		return cursor;
	}

	public static List<Map<String, Object>> getAPPInstalled(Context context) {
	
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		Intent mainintent = new Intent(Intent.ACTION_MAIN, null);
		mainintent.addCategory(Intent.CATEGORY_LAUNCHER);
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packageinfo = pm.getInstalledPackages(0);
	
		int count = packageinfo.size();
		for (int i = 0; i < count; i++) {
			PackageInfo pinfo = packageinfo.get(i);
			ApplicationInfo appInfo = pinfo.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { // ignore
																		// system
																		// app
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("app_logo", pinfo.applicationInfo.loadIcon(pm));
				map.put("app_name", pinfo.applicationInfo.loadLabel(pm));
				map.put("package_name", pinfo.applicationInfo.packageName);
				map.put("app_dir", pinfo.applicationInfo.sourceDir);
				listItems.add(map);
			}
		}
		return listItems;
	}

	public static Cursor getAllThumbNails(Context context) {
		Cursor cursor = null;
		if (Utils.isExternalStorageMounted()) {
			ContentResolver cr = context.getContentResolver();
			String[] projection = { MediaStore.Images.Thumbnails._ID,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
					MediaStore.Images.Media.DISPLAY_NAME, };
			String selection = MediaStore.Images.Media.DATA + " like ? or "
					+ MediaStore.Images.Media.DATA + " like ? or "
					+ MediaStore.Images.Media.DATA + " like ?";
			String[] selectionArg = new String[] { "%dcim%", "%pic%",
					"%camera%" };
	
			cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, selection, selectionArg, null);
		}
	
		return cursor;
	}
	
	
	public static class FileComparator implements Comparator<FilesCellItem> {
		public int compare(FilesCellItem file1, FilesCellItem file2) {
			if (file1.isDirectory() && !file2.isDirectory()) {
				return -1;
			}

			if (!file1.isDirectory() && file2.isDirectory()) {
				return 1;
			}

			return file1.getFileShortName().compareTo(file2.getFileShortName());
		}
	}
	
	
	public static ArrayList<ImageCellItem> getThumbList() {
		ArrayList<ImageCellItem> itemList = new ArrayList<ImageCellItem>();
		Cursor cursor = PriUtils.getAllThumbNails(Utils.getAppContext());
		if (cursor != null) {
			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				itemList.add(new ImageCellItem(cursor.getLong(0), null,
						cursor.getString(1)));
			}
			cursor.close();
		}
		return itemList;
	}
	
	
	public static ArrayList<AudioCellItem> getAudioItemList() {
		ArrayList<AudioCellItem> itemList = new ArrayList<AudioCellItem>();
		Cursor cursor = PriUtils.getAllAudio(Utils.getAppContext());
		if (cursor != null) {
			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				itemList.add(new AudioCellItem(cursor.getLong(0), null,
						cursor.getString(2), cursor.getString(3), cursor
								.getString(1)));
			}
			cursor.close();
		}
		return itemList;
	}

	public static int getExpireSec(String timeStr) {
		if (timeStr == null || timeStr.equals("0")) {
			return PriShareConstant.INFINITE_TIME;
		}
		String[] arr = timeStr.split(",");
		int hour = Integer.valueOf(arr[0]);
		int min = Integer.valueOf(arr[1]);
		int sec = Integer.valueOf(arr[2]);
		return hour * 3600 + min * 60 + sec;
	}
	
	
	public static void showQuitDialog(Context context, DialogInterface.OnClickListener quitListener) {
		new AlertDialog.Builder(context)
		.setTitle(context.getString(R.string.hint))
		.setMessage(R.string.quit_share_hint)
		.setPositiveButton(R.string.yes,
				quitListener) 
		.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						dialog.dismiss();
					}
				}).show();

}
		
}



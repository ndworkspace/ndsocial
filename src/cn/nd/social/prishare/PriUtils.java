package cn.nd.social.prishare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
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
		
}

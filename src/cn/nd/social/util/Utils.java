package cn.nd.social.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import cn.nd.social.SocialApplication;
import cn.nd.social.util.file.FileInfo;

public class Utils {
	public static final String INTENT_STR_USRE_NAME = "u_name";
	public static final String MY_CARD_BUILT_FLAG = "is_my_card_built";
	public static final String MY_CARD_UPLOAD_FLAG = "is_my_card_uploaded";
	public final static String SYNC_READ_HOST_KEY = "sync_read";
	public final static String SYNC_HOST_NO_SELECT = "no_select";
	public final static String SYNC_READ_PATH = "path";

	public final static String MY_CARD_EDIT_TYPE = "edit_type";

	public final static int MY_CARD_EDIT_TYPE_BOOT = 1;

	public static final long INVALID_USER_ID = -1;

	
	private static final int SDK_API_VERSION;
	private static final boolean SDK_API_14;
	
	static {
		SDK_API_VERSION = Build.VERSION.SDK_INT;
		SDK_API_14 = Build.VERSION.SDK_INT >= 14;
	}
	
	public static int getSdkVersion() {
		return SDK_API_VERSION;
	}
	

	public static boolean isAndroidVersionGood() {
		return SDK_API_14;
	}

	public static Context getAppContext() {
		return SocialApplication.getAppInstance();
	}

	public static SharedPreferences getAppSharedPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getAppContext());
	}


	public static boolean isMyCardCreated() {
		return getAppSharedPrefs().getBoolean(MY_CARD_BUILT_FLAG, false);
	}

	public static boolean isEmptyTrimmed(String str) {
		return (str == null) || (str.trim().equals(""));
	}

	public static String nonNullString(String str) {
		if (str != null)
			return str;
		return "";
	}



	public static boolean isExternalStorageMounted() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static FileInfo GetFileInfo(File f, FilenameFilter filter,
			boolean showHidden) {
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		File lFile = new File(filePath);
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = f.getName();
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		if (NDConfig.FILE_BROWSER_SHOW_FILE_COUNT && lFileInfo.IsDir) {
			int lCount = 0;
			File[] files = lFile.listFiles(filter);

			// null means we cannot access this dir
			if (files == null) {
				return null;
			}

			for (File child : files) {
				if ((!child.isHidden() || showHidden)) {
					lCount++;
				}
			}
			lFileInfo.Count = lCount;

		} else {

			lFileInfo.fileSize = lFile.length();

		}
		return lFileInfo;
	}

	public static boolean shouldShowFile(String path) {
		return shouldShowFile(new File(path));
	}

	public static boolean shouldShowFile(File file) {
		boolean show = true;
		// TODO: just for debug
		if (show)
			return true;

		if (file.isHidden())
			return false;

		if (file.getName().startsWith("."))
			return false;

		return true;
	}

	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static long getLocaldeviceId(Context context) {
		long deviceId;
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String str = telephonyManager.getDeviceId();
		if (str == null || str.trim().length() == 0) {
			deviceId = (long) ((Math.random() * 1000000000000L) + 1L);
		} else {
			try {
				deviceId = Long.valueOf(str, 16);
			} catch (Exception e) {
				deviceId = (long) (Math.random() * 1000000000000L + 1L);
			}
		}
		return deviceId;
	}
	
    public static String getLocaldeviceStrId(Context context) {
    	TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String str = telephonyManager.getDeviceId();
		return str;
    }
	
	
	@SuppressLint("SimpleDateFormat")
	public static String getFormatTime(Date date) {
		String todySDF = "HH:mm";
		String yesterDaySDF = "昨天";
		String otherSDF = "M月d日";
		SimpleDateFormat sfd = null;
		String time = "";
		Calendar dateCalendar = Calendar.getInstance();
		dateCalendar.setTime(date);
		Date now = new Date();
		Calendar targetCalendar = Calendar.getInstance();
		targetCalendar.setTime(now);
		targetCalendar.set(Calendar.HOUR_OF_DAY, 0);
		targetCalendar.set(Calendar.MINUTE, 0);
		if (dateCalendar.after(targetCalendar)) {
			sfd = new SimpleDateFormat(todySDF);
			time = sfd.format(date);
			return time;
		} else {
			targetCalendar.add(Calendar.DATE, -1);
			if (dateCalendar.after(targetCalendar)) {
				sfd = new SimpleDateFormat(yesterDaySDF);
				time = sfd.format(date);
				return time;
			}
		}
		sfd = new SimpleDateFormat(otherSDF);
		time = sfd.format(date);
		return time;
	}
	
	public static void playTurnOutVoice() {
		AssetFileDescriptor afd;
		
		try {
			afd = Utils.getAppContext().getAssets().openFd("radar_pop.mp3");
			MediaPlayer player = new MediaPlayer();				
			player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
			player.prepare();
			player.start();
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.stop();
					mp.release();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package cn.nd.social.util;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * FormatUtils intends for providing format utility
 * date format,String format, size format etc;
 * */
public class FormatUtils {
	
	public static String humanReadableFileSize(long size) {
		double d1;
		double d2;
		if (size == 0L) {
			d1 = 0.0D;
			d2 = 0.0D;
		} else {
			d1 = Math.floor(Math.log(size) / Math.log(1024.0D));
			if (d1 > 4.0D)
				d1 = 4.0D;
			d2 = size / Math.pow(1024.0D, d1);
		}

		String str = "%.0f %s";
		if (d1 - (int) d1 > 0.0D)
			str = "%,.2f %s";

		return String.format(str, Double.valueOf(d1),
				orderOfMagToString((int) d2));
	}
	
	private static String orderOfMagToString(int type) {
		switch (type) {
		default:
			return "";
		case 0:
			return "B";
		case 1:
			return "KB";
		case 2:
			return "MB";
		case 3:
			return "GB";
		case 4:
			return "TB";
		}
	}

	public static int dpToPx(Resources res, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				res.getDisplayMetrics());
	}

	// storage, G M K B
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;
	
		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}

	public static String formatDateString(Context context, long time) {
		DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(context);
		DateFormat timeFormat = android.text.format.DateFormat
				.getTimeFormat(context);
		Date date = new Date(time);
		return dateFormat.format(date) + " " + timeFormat.format(date);
	}
}

package cn.nd.social.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * UnitConverter intends for converting the unit
 * sec to hour, dp to px etc;
 * */
public class UnitConverter {
	public static int dpToPx(Resources res, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				res.getDisplayMetrics());
	}
}

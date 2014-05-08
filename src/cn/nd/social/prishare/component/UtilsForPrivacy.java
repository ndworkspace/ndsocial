package cn.nd.social.prishare.component;

public class UtilsForPrivacy {

	
	public static int getExpireSec(String timeStr) {
		if (timeStr == null) {
			return 0;
		}
		String[] arr = timeStr.split(",");
		int hour = Integer.valueOf(arr[0]);
		int min = Integer.valueOf(arr[1]);
		int sec = Integer.valueOf(arr[2]);
		return hour * 3600 + min * 60 + sec;
	}
	
}

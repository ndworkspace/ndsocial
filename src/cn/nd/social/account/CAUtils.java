package cn.nd.social.account;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import NDCSdk.INDCClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import cn.nd.social.util.Utils;

public class CAUtils {
	public static final String LAST_USER = "last_user_name";
	public static final String USER_NAME = "user_name";
	public static final String USER_PWD = "user_pwd";
	public static final String USER_ID = "user_id";
	public static final String USER_VERIFY_STATUS = "user_verify_status";
	public static final String FIRST_TIME_REG = "frist_time_reg";
	
	public static final String USER_INFO_UPDATE = "user_info_update";
	
	
	public static final String BINDED_IMEI = "bind_imei";
	public static final String IS_IMEI_BINDED = "is_imei_binded";
	
	
	public static interface BusinessCallback {
		void onBusinessBack(short bsType,byte[]data,int len);
		void onError(int errorCode,String info);
	}
	
	
	public static boolean isNetworkConnected() {  
		Context context = Utils.getAppContext();
	    if (context != null) {  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                .getSystemService(Context.CONNECTIVITY_SERVICE);  
	        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();  
	        if (netInfo != null) {  
	            return netInfo.isConnected();  
	        }  
	    }  
	    return false;  
	}
	
	
	/**account related*/
	public static void resetUserInfo() {
		CAUtils.saveLastUser(getUserName());
		
		CAUtils.setUserInfoNeedUpdate(false);
		CAUtils.saveVerifyState(false);
		CAUtils.saveUserName("");
		CAUtils.savePwd("");
		CAUtils.saveUserId(Utils.INVALID_USER_ID);
	}
	
	public static String getUserName() {
		return Utils.getAppSharedPrefs().getString(CAUtils.USER_NAME, "");
	}

	public static String getPasswd() {
		return Utils.getAppSharedPrefs().getString(CAUtils.USER_PWD, "");
	}
	
	public static boolean getVerifyState() {
		return Utils.getAppSharedPrefs().getBoolean(USER_VERIFY_STATUS, false);
	}
	
	public static long getUserId() {
		return Utils.getAppSharedPrefs().getLong(USER_ID, Utils.INVALID_USER_ID);
	}
	
	public static boolean getHasRegBefore() {
		return Utils.getAppSharedPrefs().getBoolean(FIRST_TIME_REG, false);
	}
	
	
	public static boolean isUserInfoNeedUpdate() {
		return Utils.getAppSharedPrefs().getBoolean(USER_INFO_UPDATE, false);
	}
	
	public static String getLastUser() {
		return Utils.getAppSharedPrefs().getString(CAUtils.LAST_USER, "");
	}
	
	
	public static void saveUserName(String userName) {
		Utils.getAppSharedPrefs().edit().putString(CAUtils.USER_NAME, userName).commit();
	}
	public static void savePwd(String pwd) {
		Utils.getAppSharedPrefs().edit().putString(CAUtils.USER_PWD, pwd).commit();
	}
	
	public static void saveVerifyState( boolean verified) {
		SharedPreferences.Editor editor = Utils.getAppSharedPrefs().edit();
		editor.putBoolean(USER_VERIFY_STATUS, verified).commit();
	}
	
	public static void setHasReged( boolean isFirst) {
		SharedPreferences.Editor editor = Utils.getAppSharedPrefs().edit();
		editor.putBoolean(FIRST_TIME_REG, isFirst).commit();
	}
	
	public static void saveUserId(long userId) {
		SharedPreferences.Editor editor = Utils.getAppSharedPrefs().edit();
		editor.putLong(USER_ID, userId);
		editor.commit();
	}

	public static void setUserInfoNeedUpdate(boolean needUpdate) {
		SharedPreferences.Editor editor = Utils.getAppSharedPrefs().edit();
		editor.putBoolean(USER_INFO_UPDATE, needUpdate).commit();
	}
	
	public static void saveLastUser(String userName) {
		Utils.getAppSharedPrefs().edit().putString(CAUtils.LAST_USER, userName).commit();
	}
	/**account end*/
	

	public static int rspToRequestCode(String returnCode) {
		if (returnCode.equals(INDCClient.RT_CA_REGISTER_RESP)) {
			return CAConstant.REGISTER_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_LOGIN_RESP)) {
			return CAConstant.LOGIN_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_LOGOUT_RESP)) {
			return CAConstant.LOGOUT_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_UPDATE_USER_INFO_RESP)) {
			return CAConstant.UPDATE_USER_INFO_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_QUERY_USER_INFO_RESP)) {
			return CAConstant.QUERY_USER_INFO_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_ADD_FRIEND_RESP)) {
			return CAConstant.ADD_FRIEND_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_DEL_FRIEND_RESP)) {
			return CAConstant.DEL_FRIEND_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_QUERY_FRIEND_INFO_RESP)) {
			return CAConstant.QUERY_FRIEND_INFO_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_UPDATE_CARD_INFO_RESP)) {
			return CAConstant.UPDATE_CARD_INFO_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_QUERY_CARD_INFO_RESP)) {
			return CAConstant.QUERY_CARD_INFO_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_BS_REQUEST_RESP)) {
			return CAConstant.SEND_MSG_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_QEERY_FRIEND_RESP)) {
			return CAConstant.QUERY_FRIEND_REQ;
		} else if (returnCode.equals(INDCClient.RT_CA_ADD_ALIAS_RESP)) {
			return CAConstant.ADD_ALIAS;
		} else if (returnCode.equals(INDCClient.RT_CA_CHECK_PHONE_FRIEND_RSP)) {
			return CAConstant.CHECK_PHONE_FRIEND;
		}
		return 0;
	}
	



	public static boolean isIMEIBinded(SharedPreferences prefs) {
		return prefs.getBoolean(CAUtils.IS_IMEI_BINDED, false);
	}

	public static String getBindedIMEI(SharedPreferences prefs) {
		return prefs.getString(CAUtils.BINDED_IMEI, "");
	}

	public static ArrayList<Long> parseIdList(String idList) {
		ArrayList<Long> idListArray = new ArrayList<Long>();
		String list = idList;
		String groupStart = list.substring("idlist:[".length());
		String[] groups = groupStart.split(":");
		if (groups.length < 2) {
			return idListArray;
		}
		// groups[1] like [23,24,25]]]

		String idstr = groups[1].substring(1, groups[1].indexOf("]"));
		String[] idArray = idstr.split(",");
		for (String id : idArray) {
			idListArray.add(Long.parseLong(id));
		}
		return idListArray;
	}

	public static String constructIdList(long[] idList) {
		StringBuilder strBuild = new StringBuilder("");
		
		strBuild.append("[");
		for (int i = 0; i < idList.length; i++) {
			if (i != idList.length - 1) {
				strBuild.append(idList[i]);
				strBuild.append(",");
			} else {
				strBuild.append(idList[i]);
			}
		}
		strBuild.append("]");
		// idlist end

		return strBuild.toString();
	}

	
/*	public static String constructInvitation(String theme, String timeStr,
			long localMeetingId, long hostid, String hostName,String notes, long[] idList) {
		JSONObject jobj = new JSONObject();
		try {
			jobj.put(CMD, MEETING_CMD_PREFIX + MEETING_CMD_ADD);
			jobj.put("title", theme);
			jobj.put("time", timeStr);
			jobj.put("hostname", hostName);
			jobj.put("hostid", String.valueOf(hostid));
			jobj.put("notes", notes);
			String idStr = "[";
			for(int i=0; i<idList.length;i++) {
				idStr += idList[i];
				if(i != idList.length-1) {
					idStr +=",";
				}
			}
			idStr += "]";
			jobj.put("idlist",idStr );
		} catch (JSONException e) {
			Log.e("CAUtils","construcInvitation error, corrupted data");
			e.printStackTrace();
			return null;
		}
		Log.e("CAUtils", "cautils" + jobj.toString());
		return jobj.toString();
	}*/
	
	
	public static String buildJsonLikePairs(String key,String value) {
		String pairKey = JSONObject.quote(key);
		String pairValue = JSONObject.quote(value);
		return pairKey+":" + pairValue;
	}
	
	public static void parseNotify(String jstr) {
		try {
			JSONObject jobj = new JSONObject(jstr);
			String str = jobj.getString("hostuid");
			str = jobj.getString("idlist");
			str = removeSideBracket(str);
			String []arr = str.split(",");
			long []ids = new long[arr.length];
			for(int i=0;i<arr.length;i++) {
				ids[i] = Long.parseLong(arr[i]);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static String removeSideBracket(String str) {
		if(str == null || str.equals("")) {
			return str;
		}
		
		if(str.startsWith("[")) {
			str = str.substring(1);
		}
		if(str.endsWith("]")) {
			str = str.substring(0,str.length() -1);
		}
		return str;
	}
	
	public static String getHeartBeatMsg() {
		JSONObject jobj = new JSONObject();
		try {
			jobj.put("time", System.currentTimeMillis());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jobj.toString();
	}

	
}

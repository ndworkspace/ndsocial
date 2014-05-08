package cn.nd.social.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.nd.social.card.CardUtil.CardData;

public class UserInfoCodec {
	private static JSONObject constructCard2Json(CardData card) {
		JSONObject cardObj = new JSONObject();
		try {
			cardObj.put("nick_name", card.name);
			cardObj.put("birthday", "1970-01-01");
			cardObj.put("email", card.email);
			cardObj.put("idcard", "empty");
			cardObj.put("gender", "1");
			cardObj.put("mobile_phone", card.mobile);
			cardObj.put("profession", "empty");
			cardObj.put("work_address", card.addr);
			cardObj.put("home_address", card.addr);
			cardObj.put("image", String.valueOf(card.avatarId));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return cardObj;
	}

	public static String buildUserInfoJsonString(CardData card) {
		JSONObject jobj = constructCard2Json(card);
		if (jobj == null) {
			return null;
		}
		String jstr = jobj.toString();
		Log.i("UserInfoCodec","user info string:" +jstr);
		return jstr;
	}

	public static NetUserInfo parseUserInfoJson(String userInfo) {
		NetUserInfo netUserInfo = new NetUserInfo();
		try {
			JSONObject jobj = new JSONObject(userInfo);
			netUserInfo.nickname = jobj.getString("nick_name");
			//card.title = cardObj.optString(CardUtil.TITLE_STR);
			netUserInfo.mobile = jobj.getString("username");
			netUserInfo.phone = jobj.getString("mobile_phone");
			netUserInfo.email = jobj.getString("email");
			netUserInfo.addr = jobj.optString("work_addr");
			//card.company = cardObj.getString(CardUtil.COMPANY_STR);
			netUserInfo.faceId = Integer.valueOf(jobj.getString("image"));
			if(jobj.has("userid")) {
				netUserInfo.userId = Long.valueOf(jobj.getString("userid"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch(Exception e) {
			return null;
		}
		return netUserInfo;
	}
	
	public static NetUserInfo parseUserInfo(JSONObject jobj){
		NetUserInfo netUserInfo = new NetUserInfo();
		try {
			netUserInfo.nickname = jobj.getString("nick_name");
			//card.title = cardObj.optString(CardUtil.TITLE_STR);
			netUserInfo.mobile = jobj.getString("username");
			netUserInfo.phone = jobj.getString("mobile_phone");
			netUserInfo.email = jobj.getString("email");
			netUserInfo.addr = jobj.optString("work_addr");
			//card.company = cardObj.getString(CardUtil.COMPANY_STR);
			netUserInfo.faceId = Integer.valueOf(jobj.getString("image"));
			if(jobj.has("userid")) {
				netUserInfo.userId = Long.valueOf(jobj.getString("userid"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch(Exception e) {
			return null;
		}
		return netUserInfo;
	}
	
	public static NetUserInfo getNetUserInfoByCard(CardData card) {
		NetUserInfo userInfo = new NetUserInfo();
		
		userInfo.nickname = card.name;
		userInfo.mobile = card.mobile;
		userInfo.phone = card.phone;
		userInfo.email = card.email;
		userInfo.faceId = card.avatarId;
		
		userInfo.addr = card.addr;
		
		return userInfo;
		
	}
	
	public static class NetUserInfo {
		public String nickname;
		
		/**user name*/
		public String mobile;
		
		public String phone;
		public String email;
		public String addr;
		public int faceId;
		public long userId;
	}
}

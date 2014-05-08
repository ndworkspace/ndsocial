package cn.nd.social.net;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountInfoParser {
	public static void parseContactFriendInfo(String info, List<String>friendMobile,List<String>notFriendMobiles,List<Long>notFriendIds) {
		try {
			JSONObject jinfo = new JSONObject(info);
			JSONArray jArray =  jinfo.getJSONArray("PHONELIST");
			for(int i=0;i <jArray.length();i++) {
				JSONObject jobj = (JSONObject)jArray.get(i);
				String phone = jobj.getString("PHONE");
				String isFriend = jobj.getString("FRIEND");
				String uid = jobj.getString("UID");
				if("YES".equals(isFriend)) {
					friendMobile.add(phone);
				} else {
					notFriendMobiles.add(phone);
					notFriendIds.add(Long.parseLong(uid));
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}

package cn.nd.test;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.util.MD5Encrypt;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class TestUserMgr {
	public static void testQueryContact() {
		List<String>contactMobiles = new ArrayList<String>();
		contactMobiles.add("53800");
		contactMobiles.add("53900");
		contactMobiles.add("53700");
		contactMobiles.add("53600");
		contactMobiles.add("53000");
		List<String>encryptedList = new ArrayList<String>();

		for (String mobile : contactMobiles) {
			String encrypt = MD5Encrypt.getMD5(mobile);
			encryptedList.add(encrypt);
		}

		new UserManager(sUserMgrCbk).queryContactFriend(encryptedList);
	}
	
	private static UserManagerCallBack sUserMgrCbk = new UserManagerCallBack() {

		@Override
		public void onGetUpdateUserInfoCallBack(long memberId, User userinfo,
				boolean success, String msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetFriendListCallBack(long memberId, List<User> friends,
				boolean success, String msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoginCallBack(String username, User user,
				boolean success, String msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRegisterCallBack(User user, boolean success, String msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAddFriendCallBack(long memberId, boolean success,
				String msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onQueryContactFriendCallBack(List<String> friendMobiles,
				List<String> noFriendMobiles, List<Long> noFriendUids,
				boolean success, String msg) {
			
			List<String>contactMobiles = new ArrayList<String>();
			contactMobiles.add("53800");
			contactMobiles.add("53900");
			contactMobiles.add("53700");
			contactMobiles.add("53600");
			contactMobiles.add("53000");
			List<String>encryptedList = new ArrayList<String>();

			for (String mobile : contactMobiles) {
				String encrypt = MD5Encrypt.getMD5(mobile);
				encryptedList.add(encrypt);
			}
			
			for(String friend :friendMobiles) {
				
				if(encryptedList.contains(friend)) {
					int i = encryptedList.indexOf(friend);
					Log.e("Testusermgr","contactfriend,is friend:" + contactMobiles.get(i));
				}
			}
			
			for(String friend :noFriendMobiles) {
				
				if(encryptedList.contains(friend)) {
					int i = encryptedList.indexOf(friend);
					Log.e("Testusermgr","contactfriend,not friend:" + contactMobiles.get(i));
				}
			}
			
			for(Long friend :noFriendUids) {
				Log.e("Testusermgr","contactfriend,not friend uid:" + friend);
			}
		}
		
	};
}

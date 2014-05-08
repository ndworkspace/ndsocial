package cn.nd.test;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.util.MD5Encrypt;

public class TestUserMgr {
	public static void testQueryContact() {
		List<String>contactMobiles = new ArrayList<String>();
		contactMobiles.add("53800");
		contactMobiles.add("53900");
		contactMobiles.add("53600");
		List<String>encryptedList = new ArrayList<String>();

		for (String mobile : contactMobiles) {
			String encrypt = MD5Encrypt.getMD5(mobile);
			encryptedList.add(encrypt);
		}

		new UserManager().queryContactFriend(encryptedList);
	}
}

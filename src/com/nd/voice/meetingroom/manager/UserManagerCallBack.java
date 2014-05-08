package com.nd.voice.meetingroom.manager;

import java.util.List;

public interface UserManagerCallBack {
	
	void onGetUpdateUserInfoCallBack(long memberId,User userinfo,boolean success,String msg);
	
	void onGetFriendListCallBack(long memberId,List<User> friends,boolean success,String msg);
	
	void onLoginCallBack(String username,User user,boolean success,String msg);

	void onRegisterCallBack(User user,boolean success,String msg);
	
	void onAddFriendCallBack(long memberId,boolean success,String msg);
	
	void onQueryContactFriendCallBack(List<String> friendMobiles,List<String> noFriendMobiles,boolean success,String msg);
	
}

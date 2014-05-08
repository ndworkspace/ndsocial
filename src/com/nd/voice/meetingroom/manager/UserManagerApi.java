package com.nd.voice.meetingroom.manager;

import java.util.List;

public interface UserManagerApi {
	
	public boolean isFirstUse();
	public boolean hasValidUser();
	
	public User getMyInfo();
	public User getUserInfoLocal(long memberId);
	public List<User> getMyFriendList();
	public void logout();
	
	public void getUpdateUserInfo(long memberId);
	public void getFriendList(long memberId);
	public void login(String username,String password);
	public void register(User user);
	
	public void addFriend(long memberId);
	public boolean isOnline();
	
	public void queryContactFriend(List<String> contactMobiles);
	
	public void setCallBack(UserManagerCallBack callback);
}

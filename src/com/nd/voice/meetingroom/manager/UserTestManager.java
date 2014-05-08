
package com.nd.voice.meetingroom.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserTestManager implements UserManagerApi{
	
	private UserManagerCallBack callBack;
	
	private static List<User> allUserList = new ArrayList<User>();
	
	private static User myInfo;
	
	
	public UserTestManager() {
		super();
		init();
	}
	
	public UserTestManager(UserManagerCallBack callBack){
		super();
		init();
		this.callBack = callBack;
	}
	
	private void init(){
		if(allUserList.size() == 0){
			for (int i = 0; i < 7; i++) {
				User user = new User();
				user.setUserid(548700l + i);
				user.setUserName("name" + i);
				user.setNickName("小" + i);
				user.setPhone("1379888888" + i);
				allUserList.add(user);
			}
			myInfo = getRandomUser();
		}
	}
	
	public void setMyInfo(User myInfo) {
		this.myInfo = myInfo;
	}

	public User getMyInfo() {
		return myInfo;
	}


	public List<User> getFriendList(String username) {
		return allUserList;
	}

	public void login(String username, String password) {
		for(User user : allUserList){
			if(user.getUserName().equals(username)){
				myInfo = user;
				this.callBack.onLoginCallBack(username, user, true, null);
				return;
			}
		}
		this.callBack.onLoginCallBack(username, null, false, "用户名或密码错误");
	}

	public List<User> getMyFriendList() {
		return getFriendList(myInfo.getUserName());
	}
	
	public User getRandomUser(){
		int temp = new Random().nextInt() % (allUserList.size()-1);
		return allUserList.get(Math.abs(temp));
	}


	@Override
	public User getUserInfoLocal(long memberId) {
		for(User user : allUserList){
			if(user.getUserid() == memberId){
				return user;
			}
		}
		return null;
	}


	@Override
	public void getUpdateUserInfo(long memberId) {
		for(User user : allUserList){
			if(user.getUserid() == memberId){
				this.callBack.onGetUpdateUserInfoCallBack(memberId, user, true, null);
				return;
			}
		}
		this.callBack.onGetUpdateUserInfoCallBack(memberId, null, false, "未找到该用户");
	}


	@Override
	public void getFriendList(long memberId) {
		// TODO Auto-generated method stub
		this.callBack.onGetFriendListCallBack(memberId, allUserList, true, null);
	}


	@Override
	public void register(User user) {
		// TODO Auto-generated method stub
		user.setUserid(allUserList.get(allUserList.size()-1).getUserid() + 1);
		allUserList.add(user);
		this.callBack.onRegisterCallBack(user, true, null);
	}


	@Override
	public void setCallBack(UserManagerCallBack callback) {
		// TODO Auto-generated method stub
		this.callBack = callback;
	}


	@Override
	public void logout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFirstUse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasValidUser() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addFriend(long memberId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOnline() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void queryContactFriend(List<String> contactMobiles) {
		// TODO Auto-generated method stub
		this.callBack.onQueryContactFriendCallBack(new ArrayList<String>(), new ArrayList<String>(), true, null);
	}

}

package cn.nd.social.account.usermanager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.nd.social.account.CAUtils;
import cn.nd.social.account.CloundServer;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardUtil.CardData;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class UserManager implements UserManagerApi {
	public UserManager() {
	}
	public UserManager(UserManagerCallBack cbk) {
		setCallBack(cbk);
	}
	
	public interface GetFriendInfoCbk {
		void onGetFriendInfo(ArrayList<User> friendsInfo);
	}
	
	public boolean hasLogined() {
		return CloundServer.getInstance().hasLogin();
	}

	
	private User getFriendInfo(long userId) {		
		User userInfo = null;
		List<User> userInfoList = this.getMyFriendList();
		for(User info:userInfoList) {
			if(info.getUserid() == userId) {
				userInfo = info;
				break;
			}
		}
		return userInfo;
	}
	

	
	@Override
	public User getMyInfo() {
		User myInfo = CloundServer.getInstance().getSelfInfo();
		if(myInfo != null) {
			CardData cardData = CardUtil.getSelfCardData();
			myInfo.setDefaultFace(cardData.avatarId);
			myInfo.setNickName(cardData.name);
		}
		return myInfo;
	}
	
	@Override
	public User getUserInfoLocal(long memberId) {
		return getFriendInfo(memberId);
	}
	
	/**
	 * original friends info
	 * may out of date
	 * use startAsyncQueryFriendInfo to get updated friends info
	 * */
	@Override
	public List<User> getMyFriendList() {		
		ArrayList<User> friendList =  CloundServer.getInstance().getFriendInfo();
		return friendList;
	}

		
	@Override
	public void getUpdateUserInfo(long memberId) {
		//temporary solution
		CloundServer.getInstance().setUserMgrCbk(mWeakRefUserMgrCbk!=null?mWeakRefUserMgrCbk.get():null);
		
		CloundServer.getInstance().queryFriendInfo(memberId);

	}

	@Override
	public void getFriendList(long memberId) {		
		//temporary solution
		CloundServer.getInstance().setUserMgrCbk(mWeakRefUserMgrCbk!=null?mWeakRefUserMgrCbk.get():null);
		
		
		CloundServer.getInstance().getFriendInfoAsync();
		

	}
	
	
	@Override
	public void login(String username, String password) {
		//temporary solution
		CloundServer.getInstance().setUserMgrCbk(mWeakRefUserMgrCbk!=null?mWeakRefUserMgrCbk.get():null);
		
		
		CloundServer.getInstance().login(username, password);
		

	}


	@Override
	public void register(User user) {
		
		//temporary solution
		CloundServer.getInstance().setUserMgrCbk(mWeakRefUserMgrCbk!=null?mWeakRefUserMgrCbk.get():null);
		
		CloundServer.getInstance().register(user);
	}

	WeakReference<UserManagerCallBack> mWeakRefUserMgrCbk;
	
	@Override
	public void setCallBack(UserManagerCallBack callback) {
		mWeakRefUserMgrCbk = new WeakReference<UserManagerCallBack>(callback);
	}
	
	@Override
	public void logout() {
		CloundServer.getInstance().logout();
	}
	
	@Override
	public boolean isFirstUse() {
		return !CAUtils.getHasRegBefore();
	}
	@Override
	public boolean hasValidUser() {
		return CAUtils.getVerifyState();		
	}
	
	@Override
	public void addFriend(long memberId) {
		//temporary solution
		CloundServer.getInstance().setUserMgrCbk(mWeakRefUserMgrCbk!=null? mWeakRefUserMgrCbk.get():null);
		CloundServer.getInstance().addFriend(memberId);
	}
	
	@Override
	public boolean isOnline() {
		return CloundServer.getInstance().hasLogin();
	}
	
	
	@Override
	public void queryContactFriend(List<String> contactMobiles){
		CloundServer.getInstance().setUserMgrCbk(mWeakRefUserMgrCbk!=null? mWeakRefUserMgrCbk.get():null);
		List<String>encryptedList = contactMobiles;
		

		
		JSONArray jArray = new JSONArray();
		for(String encryptMobile:encryptedList) {
			jArray.put(encryptMobile);
		}
		
		JSONObject jobj = new JSONObject();
		try {
			jobj.put("PHONELIST", jArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String mobileStr = jobj.toString();
				
		CloundServer.getInstance().queryContactFriend(mobileStr.getBytes());
		
	}
}

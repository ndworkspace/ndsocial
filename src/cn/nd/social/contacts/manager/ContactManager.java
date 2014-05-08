package cn.nd.social.contacts.manager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class ContactManager implements UserManagerCallBack{
	
	UserManagerApi mApi;
	List<MemberContact> mContacts;
	WeakReference<ContactManagerCallBack> mWeakCallBack;
	
	public ContactManager(ContactManagerCallBack callback){
		super();
		mWeakCallBack = new WeakReference<ContactManagerCallBack>(callback);
	}
	
	public void queryContactMembers(List<MemberContact> contacts){
		mContacts = contacts;
		mApi =  new UserManager();
		mApi.setCallBack(this);
		List<String> contactMobiles = getContactMobiles(contacts);
		mApi.queryContactFriend(contactMobiles);
	}
	
	private List<String> getContactMobiles(List<MemberContact> contacts){
		HashSet<String> set = new HashSet<String>();
		for(MemberContact item : contacts){
			set.add(item.getMobileMD5());
		}
		return new ArrayList<String>(set);
	}

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
	public void onLoginCallBack(String username, User user, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegisterCallBack(User user, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddFriendCallBack(long memberId, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQueryContactFriendCallBack(List<String> friendMobiles,
			List<String> noFriendMobiles,boolean success,String msg) {
		if(success){
			for(MemberContact item : mContacts){
				if(friendMobiles.contains(item.getMobileMD5())){
					item.setFriend(true);
				}else if(noFriendMobiles.contains(item.getMobileMD5())){
					item.setFriend(false);
				}
			}
			ContactDBHelper.getInstance().save(mContacts);
		}
		mContacts = null;
		mWeakCallBack.get().onQueryContactMembersCallBack(success, msg);
	}
}

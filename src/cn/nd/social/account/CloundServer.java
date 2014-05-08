package cn.nd.social.account;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

import NDCSdk.INDCClient;
import NDCSdk.NDCClient;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import cn.nd.social.BuildConfig;
import cn.nd.social.account.business.BusinessEventRsp;
import cn.nd.social.account.business.BusinessMeetingManager;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardUtil.CardData;
import cn.nd.social.net.AccountInfoParser;
import cn.nd.social.net.UserInfoCodec;
import cn.nd.social.net.UserInfoCodec.NetUserInfo;
import cn.nd.social.util.LogToFile;
import cn.nd.social.util.Utils;

public class CloundServer {	
	private INDCClient mCAClient;
	private CAProcessThread mCAProcess;
	private CARequest mCARequest;

	private static CloundServer sCloundServer; 
	
	private boolean mIsInited = false;
	
	private boolean mHasLoged =false;
	private String mLogedUser = "";
	private String mPasswd = "";
	private long mUserId = Utils.INVALID_USER_ID;
	
	private User mSelfInfo;
	private ArrayList<Long> mFriendsIdList = new ArrayList<Long>();
	private ArrayList<User> mFriendsInfoList = new ArrayList<User>();
	
	
	private WeakReference<UserManagerCallBack> mWeakRefCbk = null;
	
	private CloundServer() {		
	}
	
	public static CloundServer getInstance() {
		if(sCloundServer == null) {
			synchronized (CloundServer.class) {
				if(sCloundServer == null) {
					sCloundServer = new CloundServer();
				}
			}
		}
		return sCloundServer;
	}
	
	public void init() {
		if(mIsInited) {
			return;
		}
		connectToClound();
		mIsInited = true;
		
		//init default user data
		mSelfInfo = new User();
		mSelfInfo.setUserid(CAUtils.getUserId());
		mSelfInfo.setUserName(CAUtils.getUserName());
		mSelfInfo.setNickName(CAUtils.getUserName());
	}
	
	public void reConnect() {
		if(mCAProcess == null) {
			connectToClound();
		} else {
			mCAProcess.resetConnect(mConnectCbk);
		}
		mIsInited = true;
		setHasLogin(false);
	}
	
	public void fini() {
		if(!mIsInited) {
			return;
		}
		mIsInited = false;
		
		disconnectClound();
				
		cleanData();
		
		sCloundServer = null;		
	}
	
	private void cleanData() {
		BusinessMeetingManager.resetData();	
		setHasLogin(false);
		mLogedUser = "";
		mPasswd = "";
	}
	
	/**
	 * called from uplayer
	 * */
	public void login(String name,String pwd) {
		if(mCARequest == null){
			CloundServer.getInstance().init();
		}
		if(!mCARequest.login(name, pwd, mCbkDispatcher)) {
			if(hasUserMgrCbk()) {
				mWeakRefCbk.get().onLoginCallBack(null, null, false, "网络不给力");
			}
		} else {
			mLogedUser = name;
			mPasswd  = pwd;
		}
	}
	
	public void logout() {
		mCARequest.logout(mCbkDispatcher);
	}
	
	public void addFriend(long friendId) {
		mCARequest.addFriend(friendId, 0, null,mCbkDispatcher);
	}
	
	
	private User mRegUser; 
	
	public void register(User user) {		
		if(!mCARequest.registerUser(user.getUserName(), user.getPassword(), mCbkDispatcher)) {
			if(hasUserMgrCbk()) {
				mWeakRefCbk.get().onRegisterCallBack(null,false, "网络不给力");
			}
		} else {
			mLogedUser = user.getUserName();
			mPasswd = user.getPassword();
			mRegUser = user;
		}
		
	}
	
	public void queryFriendInfo(long uid) {
		if(!mCARequest.queryFriendInfo(uid, mCbkDispatcher)) {
			if(hasUserMgrCbk()) {
				mWeakRefCbk.get().onGetUpdateUserInfoCallBack(-1, null, false, "网络不给力");				
			}
		}
		
	}
	
	
	public void queryContactFriend(byte[] data) {
		if(!mCARequest.queryContactFriend(data,mCbkDispatcher)) {
			if(hasUserMgrCbk()) {
				//TODO:
							
			}
		}
	}
	
	public boolean hasLogin() {
		return mHasLoged;
	}
	
	public void setHasLogin(boolean hasLoged) {
		mHasLoged = hasLoged;
	}
	
	public long getUserId() {
		if (mUserId == Utils.INVALID_USER_ID) {
			mUserId = CAUtils.getUserId();
		}
		return mUserId;
	}
	
	public void setUserId(long userId) {
		mUserId = userId;
	}
	
	public String getLogedUser() {
		if(mLogedUser == null || "".equals(mLogedUser) ) {
			mLogedUser = CAUtils.getUserName();	
		}
		return mLogedUser;
	}
	
	public String getLogPasswd() {
		if(mPasswd == null || "".equals(mPasswd) ) {
			mPasswd = CAUtils.getPasswd();
		}
		return mPasswd;
	}


	public void setLogeduser(String user) {
		mLogedUser = user;
	}
	
	public CARequest getCARequest() {
		return mCARequest;
	}
	
	public boolean isNetworkReady() {
		return mCAProcess.isCAClientConnected();
	}
	
	public boolean checkLogin() {
		boolean ready = hasLogin() && CAUtils.isNetworkConnected();
		return ready;
	}
	
	
	public boolean checkServerConnected() {
		boolean ready = mCAProcess.isCAClientConnected() && CAUtils.isNetworkConnected();
		return ready;
	}
	
	public void setUserMgrCbk(UserManagerCallBack cbk) {
		mWeakRefCbk = new WeakReference<UserManagerCallBack>(cbk);
	}
		
	public ArrayList<User> getFriendInfo() {
		return mFriendsInfoList;
	}
	
	public User getFriendInfo(long memberId) {
		for(User user:mFriendsInfoList) {
			if(user.getUserid() == memberId) {
				return user;
			}
		}
		return null;
	}
	
	
	
	public User getSelfInfo() {
		return mSelfInfo;
	}
	

	public void getFriendInfoAsync() {
		if(!mCARequest.queryFriendList(0, mCbkDispatcher)) {
			if(mWeakRefCbk != null && mWeakRefCbk.get() != null) {
				mWeakRefCbk.get().onGetFriendListCallBack(-1, null, false, "网络不给力");
			}
		}
	}
	
	
	public void getFriendInfoAsync(long memberId) {		
		if(!mCARequest.queryUserInfo(memberId, mCbkDispatcher)) {
			if(mWeakRefCbk != null && mWeakRefCbk.get() != null) {
				mWeakRefCbk.get().onGetUpdateUserInfoCallBack(-1, null, false, "网络不给力");				
			}
		} 
	}
	
	/**
	 * meeting business
	 * */
	public boolean  sendMeetingMsg(byte[]data, int actionType,String sequence) {
		return mCARequest.sendMeetingBSMsg(data,actionType,sequence);
	}
	
	
	/**
	 * sync business
	 * */
	public boolean  sendSyncMsg(byte[]data, int actionType) {
		return mCARequest.sendSyncBSMsg(data,actionType);
	}
	
	
	private boolean hasUserMgrCbk() {
		return mWeakRefCbk != null && mWeakRefCbk.get() != null;
	}
	
	private Handler mCbkDispatcher = new Handler() {		
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {			
			case CAConstant.APP_EVENT_REGISTER_RSP:
				if (msg.arg1 == CACallBack.RET_SUCCESS) {
					
					onRegisterSuccess();
					
					if(hasUserMgrCbk()) {
						if(mRegUser != null) {
							mRegUser.setUserid(getUserId());
						}
						mWeakRefCbk.get().onRegisterCallBack(mRegUser,true, null);
					}
				} else {
					if(hasUserMgrCbk()) {
						String errorStr = "网络错误";
						if(msg.obj != null && msg.obj instanceof String) {
							errorStr = (String)msg.obj;
						}
						mWeakRefCbk.get().onRegisterCallBack(null,false, errorStr);
					}
				}
				break;
				
			case CAConstant.APP_EVENT_LOGIN_RSP:
				if (msg.arg1 == CACallBack.RET_SUCCESS) {
					onLoginSuccess(false);					

				}  else if(msg.arg1 == CACallBack.RET_TIMEOUT) {
					if(hasUserMgrCbk()) {
						mWeakRefCbk.get().onLoginCallBack(null, null, false, "服务器未响应");
					}
				} else {
					if(hasUserMgrCbk()) {
						mWeakRefCbk.get().onLoginCallBack(null, null, false, "用户名或密码错误");
					}
				}
				break;
				
			case CAConstant.APP_EVENT_QUERY_FRIEND_RSP:
				if(msg.arg1 == CACallBack.RET_SUCCESS) {
					
					parseFriendList((String)msg.obj);			

					if(hasUserMgrCbk()) {
						mWeakRefCbk.get().onGetFriendListCallBack(getUserId(), getFriendInfo(), true, null);
					}
				} else {
					if (hasUserMgrCbk()) {
						mWeakRefCbk.get().onGetFriendListCallBack(getUserId(),
								null, false, "服务器返回错误");
					}
				}
				break;
				
			case CAConstant.APP_EVENT_QUERY_USER_INFO_RSP:
				if(msg.arg1 == CACallBack.RET_SUCCESS) {
					User user = parseUserInfo((String)msg.obj);
					if(hasUserMgrCbk()) {
						mWeakRefCbk.get().onGetUpdateUserInfoCallBack(user.getUserid(), user, true, "查询成功");
					}
				} else {
					if(hasUserMgrCbk()) {
						mWeakRefCbk.get().onGetUpdateUserInfoCallBack(-1, null, false, "服务器返回错误");				
					}
				}
				break;
				
			case CAConstant.APP_EVENT_UPDATE_INFO_RSP:
				if(msg.arg1 == CACallBack.RET_SUCCESS) {
					CAUtils.setUserInfoNeedUpdate(false);
					Log.i("CloundServer", "update info success");
				}
				break;
				
			case CAConstant.APP_EVENT_ADD_FRIEND_RSP:			
				if(msg.arg1 == CACallBack.RET_SUCCESS) {					
					if(hasUserMgrCbk()) {
						//mWeakRefCbk.get().onAddFriendCallBack((Long)msg.obj, true,"添加成功");
					}
				} else {
					if(hasUserMgrCbk()) {
						//mWeakRefCbk.get().onAddFriendCallBack(0, false, "服务器返回错误");				
					}
				}
				break;
				
			case CAConstant.APP_EVENT_CHECK_PHONE_FRIEND_RSP:
				if(msg.arg1 == CACallBack.RET_SUCCESS) {
					ArrayList<String>friendMobile = new ArrayList<String>();
					ArrayList<String>notFriendMobiles = new ArrayList<String>();
					AccountInfoParser.parseContactFriendInfo((String)msg.obj, friendMobile, notFriendMobiles);
					if(hasUserMgrCbk()) {
						//mWeakRefCbk.get().onQueryContactFriendCallBack((Long)msg.obj, true,"添加成功");
					}
				} else {
					if(hasUserMgrCbk()) {
						//mWeakRefCbk.get().onQueryContactFriendCallBack(null,null, false, "服务器返回错误");				
					}
				}
				break;
				
			default:
				break;
			}
		}
	};
	
	
	
	
	private void onRegisterSuccess() {
		if(mLogedUser != null && !mLogedUser.equals("")) {
			CAUtils.saveUserName(mLogedUser);
		}
		
		if(mPasswd != null && !mPasswd.equals("")) {
			CAUtils.savePwd(mPasswd);
		}
		
		CAUtils.saveVerifyState(true);
				
		if(mRegUser != null) {
			CardUtil.saveCardFromUser(mRegUser);			
		}
		
		//TODO: important
		//important: delete the old card database when register a new user
		CardUtil.deleteCardList();
	}
	
	
	private boolean mNeedUpdateCardDb = false;	
	
	private void removeCardDbIfNeed() {
		String lastUser = CAUtils.getLastUser();
		//if(!mLogedUser.equals(lastUser)) {		
			mNeedUpdateCardDb = true;
		//}
		CAUtils.saveLastUser(mLogedUser);
	}
	

	
	private void onLoginSuccess(boolean autoLogin) {
		if(mLogedUser != null && !mLogedUser.equals("")) {
			CAUtils.saveUserName(mLogedUser);
		}
		
		if(mPasswd != null && !mPasswd.equals("")) {
			CAUtils.savePwd(mPasswd);
		}
		
		removeCardDbIfNeed();
		
		CAUtils.saveVerifyState(true);
		
		if(mSelfInfo == null) {
			mSelfInfo = new User();
		}
		mSelfInfo.setUserName(mLogedUser);
		
		mSelfInfo.setUserid(mUserId);
		//TODO:temporary solution, server should contain all the user info
		CardData cardData = CardUtil.getSelfCardData();
		mSelfInfo.setDefaultFace(cardData.avatarId);
		mSelfInfo.setNickName(cardData.name);
		
		
		//query user info
		if(CAUtils.isUserInfoNeedUpdate()) {
			if(!autoLogin) {
				if(hasUserMgrCbk()) {
					mWeakRefCbk.get().onLoginCallBack(mLogedUser, mSelfInfo, true, null);
				}
			}
			autoUpdateSelfInfo();
		} else {
			querySelfInfo();
		}
		
	}

	/**
	 * do this in the UI thread
	 * */
	private void connectToClound() {
		CAResponse response = new CAResponse();
		
		response.setNotifyResponser(null);
		response.setBusinessResponser(BusinessEventRsp.getInstance());
		
		CACallBack cbk = new CACallBack(response.getCbkHandler());

		mCAClient = new NDCClient();
		
		mCARequest = new CARequest(mCAClient, response.getCbkHandler());
		mCARequest.init();
		
		mCAProcess = new CAProcessThread(cbk,mCAClient,mConnectCbk);
		mCAProcess.init();		
	}
	

	private void disconnectClound() {
		if(hasLogin()) {
			mCARequest.logout(null);
			mHasLoged = false;
		}
		
		if(mCAProcess != null ) {
			mCAProcess.fini();			
			mCAProcess = null;
		}
		
		if(mCARequest != null ) {
			mCARequest.fini();
			mCARequest = null;
		}
	}
	
	
	private Handler mInternalHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {			
			case CAConstant.APP_EVENT_LOGIN_RSP:
				if (msg.arg1 == CACallBack.RET_SUCCESS) {
					onLoginSuccess(true);			
				}  else {					
					Toast.makeText(Utils.getAppContext(), "login error", Toast.LENGTH_SHORT).show();
				}
				debugShowInfoInUI("login response:" + msg.arg1);
				break;
				
			case CAConstant.APP_EVENT_QUERY_FRIEND_RSP:
				if(msg.arg1 == CACallBack.RET_SUCCESS) {
					parseFriendList((String)msg.obj);
				} else {
					Log.e("CloundServer","query friend error");
				}
				debugShowInfoInUI("query friend response:" + msg.arg1);
				break;
				
			case CAConstant.APP_EVENT_QUERY_USER_INFO_RSP:
				if(msg.arg1 == CACallBack.RET_SUCCESS) {
					parseUserInfo((String)msg.obj);
				} else {
					Log.e("CloundServer","query user info error");
				}
				queryFriends();
				break;
				
			case CAConstant.APP_EVENT_UPDATE_INFO_RSP:
				if(msg.arg1 == CACallBack.RET_SUCCESS) {
					CAUtils.setUserInfoNeedUpdate(false);
				}
				queryFriends();
				break;
			
			default:
				break;
			}
		}
	};
	

	
	private boolean queryFriends() {
		debugShowInfoInUI("start query friend");
		return mCARequest.queryFriendList(0,mInternalHandler);		
	}
	
	private boolean querySelfInfo() {
		Log.i("CloundServer","querySelfInfo");
		return mCARequest.queryUserInfo(getUserId(),mInternalHandler);
	}
	
	
	public boolean updateSelfInfo() {
		CardData cardData = CardUtil.getSelfCardData();
		return mCARequest.updateUserInfo(UserInfoCodec.buildUserInfoJsonString(cardData), mCbkDispatcher);
	}
	
	private boolean autoUpdateSelfInfo() {
		CardData cardData = CardUtil.getSelfCardData();
		return mCARequest.updateUserInfo(UserInfoCodec.buildUserInfoJsonString(cardData), mInternalHandler);
	}

	
	private void parseFriendList(String userList) {
		LogToFile.d("CloundServer", "parseFriendList: "+ userList);
		
		try {
			JSONObject jList = new JSONObject(userList); 
			JSONArray jArray = jList.getJSONArray("idlist");
			mFriendsIdList.clear();
			mFriendsInfoList.clear();
			
			for(int i=0; i<jArray.length(); i++) {
				JSONObject jobj = (JSONObject)jArray.get(i);				
				if(jobj == null) {
					continue;
				}
				NetUserInfo netUser = UserInfoCodec.parseUserInfo(jobj);
				
				User user = new User();
				user.setUserid(netUser.userId);
				user.setAddress(netUser.addr);
				user.setDefaultFace(netUser.faceId);
				user.setEmail(netUser.email);
				user.setMobile(netUser.mobile);
				user.setNickName(netUser.nickname);
				user.setUserName(netUser.mobile);
				
				mFriendsIdList.add(user.getUserid());
				mFriendsInfoList.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(mNeedUpdateCardDb) {
			mNeedUpdateCardDb = false;
			CardUtil.saveCardByUser(mFriendsInfoList);
		}
		
	}
	

	
	
	//TODO: implement
	private User parseUserInfo(String userInfo) {
		NetUserInfo netUser = UserInfoCodec.parseUserInfoJson(userInfo);
		if(netUser.userId == getUserId()) {
			mSelfInfo.setAddress(netUser.addr);
			mSelfInfo.setDefaultFace(netUser.faceId);
			mSelfInfo.setEmail(netUser.email);
			mSelfInfo.setMobile(netUser.mobile);
			mSelfInfo.setNickName(netUser.nickname);
			CardUtil.saveCardFromUser(mSelfInfo);
			
			if(hasUserMgrCbk()) {
				mWeakRefCbk.get().onLoginCallBack(mLogedUser, mSelfInfo, true, null);
			}
			return mSelfInfo;
		}
		
		return null;
	}
	
	
	private void login() {
		mCARequest.login(getLogedUser(), getLogPasswd(), mInternalHandler);
	}
	
	private CAProcessThread.ConnectCbk mConnectCbk = new CAProcessThread.ConnectCbk() {
		@Override
		public void onServerConnected() {
			if(CAUtils.getVerifyState()) {
				login();
			}
			Log.i("CloundServer","server connected");
			debugShowInfoInUI("server connected,register");
		}

		@Override
		public void onServerConnectBreak() {
				
		}		
	};
	
	private void debugShowInfoInUI(String info) {
		if(BuildConfig.DEBUG) {
			showInfoInUI(info);
		}
	}
	
	//TODO: just for debug
	private void showInfoInUI(final String info) {
		mInternalHandler.post(new Runnable() {			
			@Override
			public void run() {
				Toast.makeText(Utils.getAppContext(), info, Toast.LENGTH_SHORT).show();	
				Log.i("CloundServer",info);
			}
		});
		
	}
}

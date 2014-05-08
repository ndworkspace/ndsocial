package cn.nd.social.account.business;

import NDCSdk.INDCClient;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import cn.nd.social.SocialApplication;
import cn.nd.social.account.CACallBack;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;

public class NDCRequest {
	private INDCClient caclient;
	private Handler mainHandler;
	private RequestHandler mRequestHandler;
	HandlerThread thread;
	public NDCRequest(INDCClient caclient, Handler mainHandler) {
		this.caclient = caclient;
		this.mainHandler = mainHandler;

	}

	public void init() {
		thread = new HandlerThread("request_handler");
		thread.setDaemon(true);
		thread.start();
		mRequestHandler = new RequestHandler(thread.getLooper());
	}

	public void fini() {		
		try {
			if(thread != null && thread.isAlive()) {
				mRequestHandler.getLooper().quit();
				thread.join(1000L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Handler getRequestHandler() {
		return mRequestHandler;
	}
	
	
	private boolean isOnline() {
		return CloundServer.getInstance().checkLogin();
	}
	
	private void sendNetworkErrorRsp(Handler handler,int reqCode) {
		Message messge = handler.obtainMessage();
		messge.what = reqCode;
		messge.arg1 = CACallBack.RET_FAIL;
		handler.sendMessage(messge);
	}

	
	public boolean queryFriendList(final int version,Object cbk) {	
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.QUERY_FRIEND_REQ;
		
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0,"", null, cbk);
		
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.QueryFriend(version);			
			}
		});
		
		return true;
	}
	
	public boolean queryFriendInfo(final long uid,Object cbk) {
		if(!isOnline()) {
			return false;
		}
		
		int reqCode = CAConstant.QUERY_FRIEND_INFO_REQ;
		
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0,"", null, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.QueryFriendInfo((int)uid);			
			}
		});
		return true;
	}
	
	
	public boolean queryContactFriend(final byte[]data,Object cbk) {
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.CHECK_PHONE_FRIEND;

		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0,"", data, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.checkPhoneFriend(data);			
			}
		});
		
		return true;
	}
	
	
	public boolean queryUserInfo(Object cbk) {
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.QUERY_USER_INFO_REQ;
		
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0,"", null, cbk);
		
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.QueryUserInfo();			
			}
		});
		return true;
	}
	
	
	public boolean registerUser(final String name, final String passwd,Object extraInfo, Object cbk) {
		if(!CloundServer.getInstance().checkServerConnected()) {
			return false;
		}
		int reqCode = CAConstant.REGISTER_REQ;
		
		NDCData.RegisterData regData = new NDCData.RegisterData(name,passwd,extraInfo);
		
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0,"", regData, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.Register(name, passwd);			
			}
		});
		return true;
	}
	
	public boolean addAlias(final String aliasName, final short type, Object cbk) {
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.ADD_ALIAS;
		NDCData.AliasData aliasData = new NDCData.AliasData(aliasName,type);
		
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0,"", aliasData, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.AddAlias(aliasName, type);	
			}
		});
		return true;
	}

	public boolean login(final String userName,final String passwd,Object extraInfo, Object cbk) {
		if(!CloundServer.getInstance().checkServerConnected()) {
			return false;
		}
		int reqCode = CAConstant.LOGIN_REQ;
		NDCData.LoginData loginData = new NDCData.LoginData(userName,passwd,extraInfo);
		
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",loginData, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.Login(userName, passwd);	
			}
		});
		return true;
	}

	public boolean logout(Object cbk) {
		if(!CloundServer.getInstance().checkServerConnected()) {
			return false;
		}
		
		int reqCode = CAConstant.LOGOUT_REQ;		
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",null, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.Logout();	
			}
		});
		return true;
	}

	public boolean updateCardInfo(final String cardInfo, Object cbk) {
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.UPDATE_CARD_INFO_REQ;
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",cardInfo, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.UpdateBSCardInfo(cardInfo);
			}
		});
		
		return true;
	}

	public boolean updateUserInfo(final String userInfo, Object cbk) {
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.UPDATE_USER_INFO_REQ;
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",userInfo, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.UpdateUserInfo(userInfo);
			}
		});
		
		return true;
	}

	public boolean addFriend(final long uid, final int type, String group, Object cbk) {
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.ADD_FRIEND_REQ;
		final String groupName;
		if (group == null) {
			groupName = "default";
		} else {
			groupName = group;
		}
		
		NDCData.AddFriendData addFriendData = new NDCData.AddFriendData(uid,type,groupName);
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",addFriendData, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.AddFriend((int)uid, type, groupName);
			}
		});

		return true;
	}

	public boolean delFriend(final long uid, final int type, Object cbk) {
		if(!isOnline()) {
			return false;
		}
		int reqCode = CAConstant.DEL_FRIEND_REQ;
		NDCData.DelFriendData delFriendData = new NDCData.DelFriendData(uid,type);
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",delFriendData, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				caclient.DelFriend((int)uid, type);
			}
		});
		
		return true;
	}
	
	
	public boolean sendMeetingBSMsg( final byte[]bsData, int actionType, String sequence, Object cbk) {		
		if(!isOnline()) {			
			NDCData.BSMsgData bsErrorMsgData = new NDCData.BSMsgData(bsData,CAConstant.BS_MEETING,actionType,sequence);
			return false;
		}
		
		int reqCode = CAConstant.SEND_MSG_REQ;
		
		NDCData.BSMsgData bsMsgData = new NDCData.BSMsgData(bsData,CAConstant.BS_MEETING,actionType,sequence);
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",bsMsgData, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				boolean result = caclient.SendMsg(CAConstant.BS_MEETING, bsData);
			}
		});
		return true;
	}
	
	
	public boolean sendSyncBSMsg(final byte[]bsData, int actionType,String sequence,Object cbk) {
		if(!isOnline()) {
			SocialApplication.getAppInstance().sendToastMessage("网络不给力，无法进行同步浏览");
			return false;
		}
		
		int reqCode = CAConstant.SEND_MSG_REQ;
		
		NDCData.BSMsgData bsMsgData = new NDCData.BSMsgData(bsData,CAConstant.BS_MEETING,actionType,sequence);
		NDCMsgReq.getInstance().addPendingRequest(reqCode, 0, "",bsMsgData, cbk);
		mRequestHandler.post(new Runnable() {			
			@Override
			public void run() {
				boolean result = caclient.SendMsg(CAConstant.BS_MEETING, bsData);
				if(!result) {
					Message timeoutMsg = mainHandler.obtainMessage();
					timeoutMsg.what = CAConstant.NETWOKR_EVENT_TIMEOUT;
					timeoutMsg.arg1 = CAConstant.SEND_MSG_REQ;
					mainHandler.sendMessage(timeoutMsg);
					return;
				}
			}
		});

		return true;
	}


	public class RequestHandler extends Handler {

		public RequestHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

		}
	}
	
}

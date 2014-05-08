package cn.nd.social.account;

import cn.nd.social.SocialApplication;
import cn.nd.social.account.business.MeetingUtils.BSMeetingNetworkError;
import NDCSdk.INDCClient;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class CARequest {
	private INDCClient caclient;
	private Handler mainHandler;
	private RequestHandler mRequestHandler;
	HandlerThread thread;
	public CARequest(INDCClient caclient, Handler mainHandler) {
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
	
	/**
	 *method delegate start
	 * TODO: set this in a single class, not mess up with socialapplication
	 * */
	private boolean isReqPending(int reqCode) {
		return SocialApplication.getAppInstance().isReqPending(reqCode);
	}

	private void setReqPending(int reqCode, Handler handler) {
		SocialApplication.getAppInstance().setReqPending(reqCode,handler);
	}
	
	private Handler getReqHandler(int reqCode) {
		return SocialApplication.getAppInstance().getReqHandler(reqCode);
	}

	/**
	 * method delegate end
	 * */
	
	
	private void sendNetworkErrorRsp(Handler handler,int reqCode) {
		Message messge = handler.obtainMessage();
		messge.what = reqCode;
		messge.arg1 = CACallBack.RET_FAIL;
		handler.sendMessage(messge);
	}

	public boolean queryMultiFriendInfo(String idList,Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.QUERY_MULTI_FRIEND_INFO_REQ;
		if (isReqPending(reqCode)) {
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.uid = CloundServer.getInstance().getUserId();
		netMsg.requestCode = reqCode;
		netMsg.obj = idList;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);
		setReqPending(reqCode, handler);
		return true;
	}
	
	public boolean queryFriendList(int version,Handler handler) {	
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.QUERY_FRIEND_REQ;
/*		if (isReqPending(reqCode)) {
			return false;
		}*/
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.arg1 = version;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);
		setReqPending(reqCode, handler);
		return true;
	}
	
	public boolean queryFriendInfo(long uid,Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.QUERY_FRIEND_INFO_REQ;
		if (isReqPending(reqCode)) {
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.uid = uid;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);
		setReqPending(reqCode, handler);
		return true;
	}
	
	
	public boolean queryContactFriend(byte[]data,Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.CHECK_PHONE_FRIEND;
		if (isReqPending(reqCode)) {
			return false;
		}
		
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.obj = data;
		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;
		
		mRequestHandler.sendMessage(msg);
		setReqPending(reqCode, handler);
		
		return true;
	}
	
	
	public boolean queryUserInfo(long uid,Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.QUERY_USER_INFO_REQ;
/*		if (isReqPending(reqCode)) {
			return false;
		}*/
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.uid = uid;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);
		setReqPending(reqCode, handler);
		return true;
	}
	
	
	public boolean registerUser(String name, String passwd, Handler handler) {
		if(!CloundServer.getInstance().checkServerConnected()) {
			return false;
		}
		int reqCode = CAConstant.REGISTER_REQ;
		if (isReqPending(reqCode)) {
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.str1 = name;
		netMsg.str2 = passwd;
		netMsg.requestCode = reqCode;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}
	
	public boolean addAlias(String aliasName, short type, Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.ADD_ALIAS;
		if (isReqPending(reqCode)) {
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.obj = aliasName;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.arg1 = type;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}

	public boolean login(String userName, String passwd, Handler handler) {
		if(!CloundServer.getInstance().checkServerConnected()) {
			return false;
		}
		int reqCode = CAConstant.LOGIN_REQ;
		if (isReqPending(reqCode)) {
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.str1 = userName;
		netMsg.str2 = passwd;
		netMsg.requestCode = reqCode;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}

	public boolean logout(Handler handler) {
		if(!CloundServer.getInstance().checkServerConnected()) {
			return false;
		}
		int reqCode = CAConstant.LOGOUT_REQ;
		if (isReqPending(reqCode)) {
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;
		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}

	public boolean updateCardInfo(String cardInfo, Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.UPDATE_CARD_INFO_REQ;
		if (isReqPending(reqCode)) {
			return false;
		}

		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.str1 = cardInfo;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;

		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}

	public boolean updateUserInfo(String userInfo, Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			return false;
		}
		int reqCode = CAConstant.UPDATE_USER_INFO_REQ;
/*		if (isReqPending(reqCode) && getReqHandler(reqCode) != handler ) {
			return false;
		}*/
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.str1 = userInfo;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.obj = netMsg;

		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}

	public boolean addFriend(long uid, int type, String group, Handler handler) {
		if(!CloundServer.getInstance().checkLogin()) {
			sendNetworkErrorRsp(handler,CAConstant.APP_EVENT_ADD_FRIEND_RSP);
			return false;
		}
		int reqCode = CAConstant.ADD_FRIEND_REQ;
		if (group == null) {
			group = "default";
		}
		if (isReqPending(reqCode)) {
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.str1 = group;
		netMsg.uid = uid;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.arg1 = type;
		msg.obj = netMsg;

		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}

	public boolean delFriend(long uid, int type, Handler handler) {
		int reqCode = CAConstant.DEL_FRIEND_REQ;
		if (isReqPending(reqCode)) {
			sendNetworkErrorRsp(handler,CAConstant.APP_EVENT_DEL_FRIEND_RSP);
			return false;
		}
		NetMessageEntity netMsg = new NetMessageEntity();
		netMsg.requestCode = reqCode;
		netMsg.uid = uid;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.arg1 = type;
		msg.obj = netMsg;

		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}
	
	
	public boolean sendMeetingBSMsg( byte[]bsData, int actionType, String sequence) {
		if (!CloundServer.getInstance().checkLogin()) {
			BSMeetingNetworkError error = new BSMeetingNetworkError();
			error.info = "网络不给力";
			error.sequence = sequence;
			error.actionType = actionType;
			Message networkMsg = mainHandler.obtainMessage();
			networkMsg.what = CAConstant.NETWORK_EVENT_ERROR;
			networkMsg.arg1 = CAConstant.NETWOKR_ERROR_BS_MEETING_TYPE;
			networkMsg.obj = error;
			mainHandler.sendMessage(networkMsg);
			return false;
		}
		
		int reqCode = CAConstant.SEND_MSG_REQ;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
	
		
		BSMeetingNetworkError errorInfo = new BSMeetingNetworkError();
		errorInfo.info = "网络不给力";
		errorInfo.sequence = sequence;
		errorInfo.actionType = actionType;
		
		BSMessageEntity entity = new BSMessageEntity();
		entity.bsData = bsData;
		entity.bsType = CAConstant.BS_MEETING;
		entity.dataLen = bsData.length;
		entity.obj = errorInfo;
		
		msg.arg1 = CAConstant.BS_MEETING;
		msg.arg2 = bsData.length;
		msg.obj = entity;
		mRequestHandler.sendMessage(msg);

		return true;
	}
	
	
	public boolean sendSyncBSMsg(byte[]bsData, int actionType) {
		if (!CloundServer.getInstance().checkLogin()) {
			SocialApplication.getAppInstance().sendToastMessage("网络不给力，无法进行同步浏览");
			return false;
		}
		
		int reqCode = CAConstant.SEND_MSG_REQ;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
	
		
		BSMeetingNetworkError errorInfo = new BSMeetingNetworkError();
		errorInfo.info = "网络不给力";
		errorInfo.actionType = actionType;
		
		BSMessageEntity entity = new BSMessageEntity();
		entity.bsData = bsData;
		entity.bsType = CAConstant.BS_MEETING;
		entity.dataLen = bsData.length;
		entity.obj = errorInfo;
		
		msg.arg1 = CAConstant.BS_MEETING;
		msg.arg2 = bsData.length;
		msg.obj = entity;
		mRequestHandler.sendMessage(msg);

		return true;
	}
	
	/**obsolete*/
	/*public boolean sendBSMsg(short nBSType, byte[]BSData, int len, Handler handler) {
		
		if (!CloundServer.getInstance().isNetworkReady()) {
			SocialApplication.getAppInstance().sendToastMessage("network is not ready!!");
			return false;
		}
		
		int reqCode = CAConstant.SEND_MSG_REQ;
		if (isReqPending(reqCode) && getPendingHandler(reqCode) != handler) {
			return false;
		}
		NetMessageObj netMsg = new NetMessageObj();
		netMsg.requestCode = reqCode;
		netMsg.obj = BSData;

		Message msg = mRequestHandler.obtainMessage();
		msg.what = reqCode;
		msg.arg1 = nBSType;
		msg.arg2 = len;
		msg.obj = netMsg;

		mRequestHandler.sendMessage(msg);

		setReqPending(reqCode, handler);
		return true;
	}*/

	public class RequestHandler extends Handler {

		public RequestHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			NetMessageEntity entity = null;
			if (msg.obj instanceof NetMessageEntity) {
				entity = (NetMessageEntity) msg.obj;
			}
			switch (msg.what) {
			case CAConstant.REGISTER_REQ:
				Log.e("CARequest", "register,username:" + entity.str1 + " passwd:" + entity.str2);
				caclient.Register(entity.str1, entity.str2);
				break;
				
			case CAConstant.LOGIN_REQ:
				caclient.Login(entity.str1, entity.str2);
				break;
				
			case CAConstant.LOGOUT_REQ:
				caclient.Logout();
				break;
				
			case CAConstant.UPDATE_USER_INFO_REQ:
				Log.i("CARequest","UPDATE_USER_INFO_REQ");
				caclient.UpdateUserInfo(entity.str1);
				break;
				
			case CAConstant.QUERY_USER_INFO_REQ:
				caclient.QueryUserInfo();
				break;
				
			case CAConstant.ADD_FRIEND_REQ:
				caclient.AddFriend((int)entity.uid, msg.arg1, entity.str1);
				break;
				
			case CAConstant.DEL_FRIEND_REQ:
				caclient.DelFriend((int)entity.uid, msg.arg1);
				break;
				
			case CAConstant.QUERY_FRIEND_INFO_REQ:
				caclient.QueryFriendInfo((int)entity.uid);
				break;
				
			case CAConstant.UPDATE_CARD_INFO_REQ:
				caclient.UpdateBSCardInfo(entity.str1);
				break;
				
			case CAConstant.QUERY_CARD_INFO_REQ:
				caclient.QueryBSCardInfo((int)entity.uid);
				break;
				
			case CAConstant.SEND_MSG_REQ: {				
					BSMessageEntity bsEntity = (BSMessageEntity)msg.obj;
					boolean result = caclient.SendMsg(bsEntity.bsType, bsEntity.bsData);
					if(!result) {
						Message timeoutMsg = mainHandler.obtainMessage();
						timeoutMsg.what = CAConstant.NETWOKR_EVENT_TIMEOUT;
						timeoutMsg.arg1 = msg.what;
						if(msg.what == CAConstant.SEND_MSG_REQ) {
							timeoutMsg.obj = msg.obj;
						}
						mainHandler.sendMessage(timeoutMsg);
						return;
					}
				}
				break;
				
			case CAConstant.QUERY_FRIEND_REQ:
				caclient.QueryFriend(msg.arg1);
				break;
/*			case CAConstant.QUERY_MULTI_FRIEND_INFO_REQ:
				caclient.queryMultiFriendInfo((String)netObj.obj);
				break;*/
			case CAConstant.ADD_ALIAS:
				caclient.AddAlias((String)entity.obj, (short)msg.arg1);
				break;
				
			case CAConstant.CHECK_PHONE_FRIEND:
				caclient.checkPhoneFriend((byte[])entity.obj);
				break;
				
			default:
				break;
			}
			
			//TODO: just for temporary
			SocialApplication.getAppInstance().setCurrReqCode(msg.what); 

			// timeout checking
			Message timeoutMsg = mainHandler.obtainMessage();
			timeoutMsg.what = CAConstant.NETWOKR_EVENT_TIMEOUT;
			timeoutMsg.arg1 = msg.what;
			if(msg.what == CAConstant.SEND_MSG_REQ) {
				timeoutMsg.obj = msg.obj;
			}
			mainHandler.sendMessageDelayed(timeoutMsg, 15000);
			super.handleMessage(msg);
			

		}
	}

	
	
	
	/**
	 * TODO: build an NetMessageRequest ArrayList
	 * */
	public class NetMessageEntity {
		private int requestCode;
		private long uid;
		private String str1;
		private String str2;
		private Object obj;
		private Handler handler;
		private long timestamp;

		public NetMessageEntity() {
			timestamp = System.currentTimeMillis();
		}
	}

	
	public class BSMessageEntity {
		public short bsType;
		public Object obj;
		public byte[] bsData;
		public int dataLen;
		public long timestamp;

		public BSMessageEntity() {
			timestamp = System.currentTimeMillis();
		}
	}
}

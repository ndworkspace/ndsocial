package cn.nd.social.account.business;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import cn.nd.social.SocialApplication;
import cn.nd.social.account.CACallBack;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CARequest.BSMessageEntity;
import cn.nd.social.account.CAUtils;
import cn.nd.social.account.CAUtils.BusinessCallback;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.business.MeetingUtils.BSMeetingNetworkError;
import cn.nd.social.account.business.NDCMsgReq.PendingReq;

public class NDCResponse {
	
	public final static String TAG = "CAResponse";
	
	public NDCResponse() {
		
	}
	
	public  interface NotifyResponser {
		void onNotify(int notifyCode,Object obj);
	}
	private NotifyResponser mNotifyResponser;
	private BusinessCallback mBusinessCbk;
	public void setNotifyResponser(NotifyResponser responser) {
		mNotifyResponser = responser;
	}
	
	public void setBusinessResponser(BusinessCallback cbk) {
		mBusinessCbk = cbk;
	}
	
	public Handler getCbkHandler() {
		return mHandler;
	}
	
	
	private void parseErrorEvent(Message msg)  {
		switch(msg.arg1) {
		case CAConstant.NETWOKR_ERROR_BS_MEETING_TYPE:
			BSMeetingNetworkError errorInfo = (BSMeetingNetworkError)msg.obj;
			BusinessMeetingManager.onMeetingReqError(errorInfo);
			break;
			
		default:
			if(getCurrReqCode() != 0) {
				parseNetResponse(getCurrReqCode(), CACallBack.RET_NET_ERROR, null,
						(String) msg.obj,false);
			}
			Toast.makeText(getApp(), "网络错误",
					Toast.LENGTH_SHORT).show();
			break;
		}		
	}
	private void parseTimeoutError(Message msg)  {
		switch(msg.arg1) {
		case CAConstant.SEND_MSG_REQ:
			BSMessageEntity entity = (BSMessageEntity)msg.obj;
			if(entity.bsType == CAConstant.BS_MEETING) {
				BSMeetingNetworkError errorInfo = (BSMeetingNetworkError)entity.obj;
				BusinessMeetingManager.onMeetingReqError(errorInfo);
			}
			break;
			
		default:
			if (getCurrReqCode() != 0) {
				parseNetResponse(msg.arg1, CACallBack.RET_TIMEOUT, null,
						"网络超时",false);
				Log.e("CAResponse","timeout:" + msg.arg1);
			}
			break;
		}		
	}
	
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what != CAConstant.NETWOKR_EVENT_TIMEOUT) {
				removeMessages(CAConstant.NETWOKR_EVENT_TIMEOUT);
			}
			switch(msg.what) {
			
			case CAConstant.NETWORK_PLATFORM_EVENT_RETURN:
				parseNetResponse(msg.arg1,msg.arg2,msg.obj);
				break;
				
			case CAConstant.NETWORK_BUSINESS_EVENT_RETURN:
				onBusinessResponse((short)msg.arg1,(byte[])msg.obj,msg.arg2);
				break;
				
			case CAConstant.NETWORK_EVENT_NOTIFY:
				if(mNotifyResponser != null) {
					mNotifyResponser.onNotify(msg.arg1,msg.obj);
				}
				Log.e("CAResponse","receive network notify event");
				break;
				
			case CAConstant.NETWORK_EVENT_ERROR:
				parseErrorEvent(msg);
				break;

			case CAConstant.NETWOKR_EVENT_TIMEOUT:
				parseTimeoutError(msg);
				break;
				
			default:
				break;
			}
		}
	};
	
	private void parseNetResponse(int reqCode, int nReturnCode, Object data) {
		parseNetResponse(reqCode, nReturnCode, data, null,true);
	}
	
	private void onBusinessResponse(short bsType,byte[]data,int len) {
		if(mBusinessCbk != null) {
			mBusinessCbk.onBusinessBack(bsType, data, len);
		}
	}
	
	private void onBusinessError(int errorCode, String info) {
		if(mBusinessCbk != null) {
			mBusinessCbk.onError(errorCode, info);
		}
	}
	
	
	/**
	 *method delegate start
	 * TODO: set this in a single class, not mess up with socialapplication
	 * */	
	private SocialApplication getApp() {
		return SocialApplication.getAppInstance();
	}
	
	private int getCurrReqCode() {
		return getApp().getCurrReqCode();
	}
	
	private boolean isReqPending(int reqCode) {
		return getApp().isReqPending(reqCode);
	}
	
	private Handler getReqHandler(int reqCode) {
		return getApp().getReqHandler(reqCode);
	}
	
	private void setUserId(long userId) {
		CloundServer.getInstance().setUserId(userId);
	}
	
	
	private void removeReqPending(int reqCode) {
		getApp().removeReqPending(reqCode);
	}
	
	private void setHasLogin(boolean hasLogin) {
		CloundServer.getInstance().setHasLogin(hasLogin);
	}
	/**
	 * method delegate end
	 * */

	private int mapReturnCode(int returnCode) {
		if(returnCode == CACallBack.RET_SUCCESS) {
			return CACallBack.RET_SUCCESS;
		} else if(returnCode == CACallBack.RET_TIMEOUT) {
			returnCode = CACallBack.RET_TIMEOUT;
		}
		return returnCode;
	}
	
	public void parseNetResponse(int reqCode, int nReturnCode, Object data,
			String errorInfo,boolean normalReturn) {	
		switch (reqCode) {
		case CAConstant.REGISTER_REQ:
			if (nReturnCode == CACallBack.RET_SUCCESS) {
				long uid = -1;
				if(data instanceof Long) {
					uid = (Long)data;					
				} else if(data instanceof Integer) {
					uid = (Integer)data;
				}
				setUserId(uid);
				CAUtils.saveUserId(uid);
				CAUtils.setHasReged(true);
			} else {
				Log.e("CAResponse","register error,code:" + nReturnCode);
				if(nReturnCode == CACallBack.RET_FAIL) {
					errorInfo = "用户名已存在";
				}
			}
			if (isReqPending(reqCode)) {				
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_REGISTER_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;

		case CAConstant.LOGIN_REQ:
			if (nReturnCode == CACallBack.RET_SUCCESS) {
				long uid = -1;
				if(data instanceof Long) {
					uid = (Long)data;					
				} else if(data instanceof Integer) {
					uid = (Integer)data;
				}
				setUserId(uid);
				CAUtils.saveUserId(uid);
				setHasLogin(true);
				CAUtils.setHasReged(true);
			} else {
				if(normalReturn) { //indicate user or password is invalid, reset verify status: 
					CAUtils.saveVerifyState(false);
				}
				Log.e("CAResponse","login error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_LOGIN_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;

		case CAConstant.LOGOUT_REQ:
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_LOGOUT_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;
			
		

			
		case CAConstant.ADD_FRIEND_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","add friend error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_ADD_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (msg.arg1 == 0) {
						Long friendId = null;
						if(data instanceof Long) {
							friendId = (Long)data;					
						} else if(data instanceof Integer) {
							friendId = (long)((Integer)data);
						}
						msg.obj = friendId;
						
					} else if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}

			break;
			
		case CAConstant.DEL_FRIEND_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","del friend error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {				
				Handler handler=getReqHandler(reqCode);
				
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_DEL_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (msg.arg1 == 0) {
						if(data instanceof Long) {
							msg.obj = (Long)data;					
						} else if(data instanceof Integer) {
							msg.obj = (Integer)data;
						}	
					} else if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;
			

			
		case CAConstant.UPDATE_CARD_INFO_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","update error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_UPLOAD_MY_CARD;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
				Log.i(TAG, "upload card info success");
			}
			break;
			
		case CAConstant.QUERY_CARD_INFO_REQ:

			break;
			
		case CAConstant.SEND_MSG_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","business error,code:" + nReturnCode);
			}
			
			onBusinessError(nReturnCode,"time out");

			break;
		
		case CAConstant.QUERY_FRIEND_REQ:			
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","query friend error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {
				Handler handler = getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_QUERY_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if(nReturnCode == CACallBack.RET_SUCCESS) {
						msg.obj = data;
					} else if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
				Log.i(TAG, "query friend return");
			}
			break;
			
		case CAConstant.QUERY_FRIEND_INFO_REQ:
			
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","query friend error,code:" + nReturnCode);
			}			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_QUERY_USER_INFO_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
				Log.i(TAG, "query friend info return");
			}
			break;
		case CAConstant.ADD_ALIAS:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","add alias error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_ADD_ALIAS_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}			
			break;
			
		case CAConstant.QUERY_USER_INFO_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","query user info error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_QUERY_USER_INFO_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (msg.arg1 == CACallBack.RET_SUCCESS) {
						msg.obj = data;					
					} else {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}			
			break;
			
		case CAConstant.UPDATE_USER_INFO_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","update user info error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_UPDATE_INFO_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}			
			break;
			
		case CAConstant.CHECK_PHONE_FRIEND:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","check phone info error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_CHECK_PHONE_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if(msg.arg1 == CACallBack.RET_SUCCESS) {
						msg.obj = data;
					} else {
						msg.obj = errorInfo;
					}
					
					handler.sendMessage(msg);
				}
			}
			break;
			
		default:
			break;
		}
		removeReqPending(reqCode);
	}
	
	public void dispatchNetResponse(int reqCode, int nReturnCode, Object data,
			String errorInfo,boolean normalReturn) {
		boolean isReqPending = NDCMsgReq.getInstance().isRequestPending(reqCode);
		if(isReqPending) {
			 PendingReq req = NDCMsgReq.getInstance().removePendingReqByCode(reqCode);
			 switch(reqCode) {
			 	
			 }
		}
		
		switch (reqCode) {
		case CAConstant.REGISTER_REQ:
			if (nReturnCode == CACallBack.RET_SUCCESS) {
				long uid = -1;
				if(data instanceof Long) {
					uid = (Long)data;					
				} else if(data instanceof Integer) {
					uid = (Integer)data;
				}
				setUserId(uid);
				CAUtils.saveUserId(uid);
				CAUtils.setHasReged(true);
			} else {
				Log.e("CAResponse","register error,code:" + nReturnCode);
				if(nReturnCode == CACallBack.RET_FAIL) {
					errorInfo = "用户名已存在";
				}
			}
			if (isReqPending(reqCode)) {				
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_REGISTER_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;

		case CAConstant.LOGIN_REQ:
			if (nReturnCode == CACallBack.RET_SUCCESS) {
				long uid = -1;
				if(data instanceof Long) {
					uid = (Long)data;					
				} else if(data instanceof Integer) {
					uid = (Integer)data;
				}
				setUserId(uid);
				CAUtils.saveUserId(uid);
				setHasLogin(true);
				CAUtils.setHasReged(true);
			} else {
				if(normalReturn) { //indicate user or password is invalid, reset verify status: 
					CAUtils.saveVerifyState(false);
				}
				Log.e("CAResponse","login error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_LOGIN_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;

		case CAConstant.LOGOUT_REQ:
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_LOGOUT_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;
			
		

			
		case CAConstant.ADD_FRIEND_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","add friend error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_ADD_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (msg.arg1 == 0) {
						Long friendId = null;
						if(data instanceof Long) {
							friendId = (Long)data;					
						} else if(data instanceof Integer) {
							friendId = (long)((Integer)data);
						}
						msg.obj = friendId;
						
					} else if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}

			break;
			
		case CAConstant.DEL_FRIEND_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","del friend error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {				
				Handler handler=getReqHandler(reqCode);
				
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_DEL_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (msg.arg1 == 0) {
						if(data instanceof Long) {
							msg.obj = (Long)data;					
						} else if(data instanceof Integer) {
							msg.obj = (Integer)data;
						}	
					} else if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}
			break;
			

			
		case CAConstant.UPDATE_CARD_INFO_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","update error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_UPLOAD_MY_CARD;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
				Log.i(TAG, "upload card info success");
			}
			break;
			
		case CAConstant.QUERY_CARD_INFO_REQ:

			break;
			
		case CAConstant.SEND_MSG_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","business error,code:" + nReturnCode);
			}
			
			onBusinessError(nReturnCode,"time out");

			break;
		
		case CAConstant.QUERY_FRIEND_REQ:			
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","query friend error,code:" + nReturnCode);
			}
			if (isReqPending(reqCode)) {
				Handler handler = getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_QUERY_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if(nReturnCode == CACallBack.RET_SUCCESS) {
						msg.obj = data;
					} else if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
				Log.i(TAG, "query friend return");
			}
			break;
			
		case CAConstant.QUERY_FRIEND_INFO_REQ:
			
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","query friend error,code:" + nReturnCode);
			}			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_QUERY_USER_INFO_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
				Log.i(TAG, "query friend info return");
			}
			break;
		case CAConstant.ADD_ALIAS:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","add alias error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_ADD_ALIAS_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}			
			break;
			
		case CAConstant.QUERY_USER_INFO_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","query user info error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_QUERY_USER_INFO_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (msg.arg1 == CACallBack.RET_SUCCESS) {
						msg.obj = data;					
					} else {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}			
			break;
			
		case CAConstant.UPDATE_USER_INFO_REQ:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","update user info error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_UPDATE_INFO_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if (errorInfo != null) {
						msg.obj = errorInfo;
					}
					handler.sendMessage(msg);
				}
			}			
			break;
			
		case CAConstant.CHECK_PHONE_FRIEND:
			if(nReturnCode != CACallBack.RET_SUCCESS) {
				Log.e("CAResponse","check phone info error,code:" + nReturnCode);
			}
			
			if (isReqPending(reqCode)) {
				Handler handler=getReqHandler(reqCode);
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = CAConstant.APP_EVENT_CHECK_PHONE_FRIEND_RSP;
					msg.arg1 = mapReturnCode(nReturnCode);
					if(msg.arg1 == CACallBack.RET_SUCCESS) {
						msg.obj = data;
					} else {
						msg.obj = errorInfo;
					}
					
					handler.sendMessage(msg);
				}
			}
			break;
			
		default:
			break;
		}
		removeReqPending(reqCode);
	}
}

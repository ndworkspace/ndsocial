package cn.nd.social.account;

import NDCSdk.INDCCallback;
import NDCSdk.INDCClient;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;
import cn.nd.social.util.Utils;

/**
 * CACallBack will get invoked by the CAProcessThread,
 * need to send the network result to UI thread,
 * so use UI thread's cbkHandler to transmit message
 * (cbkHandler is in CAResponse)*/
public class CACallBack implements INDCCallback {
	public static final int RET_SUCCESS = 0;
	public static final int RET_FAIL = -1;
	public static final int RET_TIMEOUT = -2;
	public static final int RET_NET_ERROR = -3;
	private Handler cbkHandler;
	public CACallBack(Handler handler) {
		this.cbkHandler = handler;
	}

	@Override
	public void OnError(int nError, String errorInfo) {
		if(errorInfo == null || errorInfo.equals("")) {
			errorInfo = Utils.getAppContext().getString(R.string.net_error);
		}
		sendNetError(nError, errorInfo);
	}

	@Override
	public void OnNotify(int notifyCode, Object notify) {
		if(checkNetBreak(notifyCode,notify)) {
			return;			
		}
		sendNetNotify(notifyCode, notify);
	}
	
	
	/**
	 * platform message response
	 * */
	@Override
	public void OnMessage(String requestCode, int nReturnCode, Object value) {
		if(value instanceof String) {
			String data = (String)value;
			Log.e("CACallBack","OnMessage reqcode:"+ requestCode+ " content:"+  new  String(data));
		}
		
		sendNetReturn(requestCode,nReturnCode, value);
	}

	/**
	 * business message response
	 * */
	@Override
	public void OnMessage(short bsType, byte[] data, int nLen) {
		Log.e("CACallBack","OnMessage type"+ bsType + " len" + nLen);
		sendBusinessReturn(bsType,data,nLen);		
	}
	
	private void sendNetNotify(int notifyCode, Object value) {
		Message msg = cbkHandler.obtainMessage();
		msg.what = CAConstant.NETWORK_EVENT_NOTIFY;
		msg.arg1 = notifyCode;
		msg.obj = value;
		cbkHandler.sendMessage(msg);		
	}
	
	private void sendNetError(int errCode, String info) {
		Message msg = cbkHandler.obtainMessage();
		msg.what = CAConstant.NETWORK_EVENT_ERROR;
		msg.arg1 = errCode;
		msg.obj = info;
		cbkHandler.sendMessage(msg);		
	}
	
	private void sendNetReturn(String rspCode, int nReturnCode, Object value) {
		Message msg = cbkHandler.obtainMessage();
		msg.what = CAConstant.NETWORK_PLATFORM_EVENT_RETURN;
		msg.arg1 = CAUtils.rspToRequestCode(rspCode);
		msg.arg2 = nReturnCode;
		msg.obj = value;
		cbkHandler.sendMessage(msg);
		SocialApplication.getAppInstance().setCurrReqCode(0);		
	}


	private void sendBusinessReturn(short bsType, byte[] data, int nLen) {		
		Message msg = cbkHandler.obtainMessage();
		SocialApplication.getAppInstance().setCurrReqCode(0);
		msg.what = CAConstant.NETWORK_BUSINESS_EVENT_RETURN;
		msg.arg1= bsType;
		msg.arg2 = nLen;
		msg.obj  = data;		
		cbkHandler.sendMessage(msg);
		
	}
	
	private boolean checkNetBreak(int notifyCode,Object notify) {
		if(notifyCode == INDCClient.MSG_CONNECTION_BREAK_NOTIFY) {
			try {
				if((Integer)notify == INDCClient.MSG_CONNECTION_BREAK_NOTIFY) {
					//reconnect to clound
					CloundServer.getInstance().reConnect();
					return true;
				}
			} catch(Exception e) {
				
			}
			
		}
		return false;
	}
}

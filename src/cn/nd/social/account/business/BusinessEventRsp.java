package cn.nd.social.account.business;

import java.lang.ref.WeakReference;

import android.util.Log;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CAUtils.BusinessCallback;
import cn.nd.social.account.business.MeetingUtils.MeetingRsp;
import cn.nd.social.account.business.MeetingUtils.TransferMsg;
import cn.nd.social.syncbrowsing.manager.IClientNetMsgReceiver;
import cn.nd.social.syncbrowsing.manager.SyncMsgFactory;


public class BusinessEventRsp implements BusinessCallback {
	
	private final static String TAG = "BusinessEventRsp";
	private static BusinessEventRsp sInstance;
	

	private BusinessEventRsp() {
	}
	
	public static BusinessEventRsp getInstance() {
		if(sInstance == null) {
			synchronized (BusinessEventRsp.class) {
				if(sInstance == null) {
					sInstance = new BusinessEventRsp();
				}
			}
		}
		return sInstance;
	}
	
	
	

	@Override
	public void onBusinessBack(short bsType, byte[] data, int len) {
		if(bsType == CAConstant.BS_MEETING) {
			
			MeetingRsp rsp = MeetingUtils.parseBSMeeting(data);
			if(rsp == null) {
				Log.e("BusinessEventRsp","onBusinessBack error:" + bsType + "data:" + new String(data));
				return;
			}
			String action = rsp.action;
			
			if(action.equals(MeetingUtils.MEETING_TRANSFER_RECV_NOTIFY)) {
				TransferMsg transMsg = (TransferMsg)rsp;
				IClientNetMsgReceiver receiver = syncReceiver != null?syncReceiver.get():null;
				SyncMsgFactory.parseSyncPacket(transMsg.rawData, receiver);
			} else {
				MeetingMsgReceiver.onMeetingBusiness(rsp,action);
			}
			
		}  else {
			Log.e(TAG,"onBusinessBack, business type not support yet,type:" + bsType);
		}
		
	}

	
	@Override
	public void onError(int errorCode, String info) {
			
	}	
	
	private WeakReference<IClientNetMsgReceiver> syncReceiver;
	public void setSyncReadReceiver(IClientNetMsgReceiver receiver) {
		syncReceiver = new WeakReference<IClientNetMsgReceiver>(receiver);
	}

}

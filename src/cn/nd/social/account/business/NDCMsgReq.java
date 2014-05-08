package cn.nd.social.account.business;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class NDCMsgReq {
	private static NDCMsgReq sMeetingMsgReq = new NDCMsgReq();
	private ArrayList<PendingReq> mPendingReqArray = new ArrayList<PendingReq>(); 
	
	private final static int MAX_SENDING_MSG_NUM = 5;
	
	private NDCMsgReq() {
		
	}
	
	public static NDCMsgReq getInstance() {
		return sMeetingMsgReq;
	}
	
	public void reset() {
		mPendingReqArray = new ArrayList<PendingReq>();
	}
	
	public void addPendingRequest(int reqCode,int localMsgId,String sequence,
														Object reqContent,Object cbk) {
		PendingReq req = new PendingReq();
		req.requestCode= reqCode;
		req.sequence = sequence;
		req.localMsgId = localMsgId;
		req.status = ReqStatus.WAITING_RSP;
		req.reqContent = reqContent;
		req.cbk= new WeakReference<Object>(cbk);
		mPendingReqArray.add(req);
	}
	
	public boolean isRequestPending(int reqCode) {
		for(PendingReq req:mPendingReqArray) {
			if(reqCode == req.requestCode) {
				return true;
			}
		}
		return false;
	}
	
	public PendingReq getFirstPendingReq(int reqCode) {
		for(PendingReq req:mPendingReqArray) {
			if(reqCode == req.requestCode) {
				return req;
			}
		}
		return null;
	}
	
	
	public boolean isRequestPending(int reqCode,int localMsgId) {
		for(PendingReq req:mPendingReqArray) {
			if(reqCode == req.requestCode && localMsgId == req.localMsgId) {
				return true;
			}
		}
		return false;
	}
	
	public PendingReq removePendingReqBySeq(String sequence) {
		if(sequence == null) {
			return null;
		}
		for(PendingReq req:mPendingReqArray) {
			if(sequence.equals(req.sequence)) {
				mPendingReqArray.remove(req);
				return req;
			}
		}
		return null;
	}
	
	public PendingReq removePendingReqByCode(int reqCode) {
		for(PendingReq req:mPendingReqArray) {
			if(reqCode == req.requestCode) {
				mPendingReqArray.remove(req);
				return req;
			}
		}
		return null;
	}
	
	
	public PendingReq removePendingReqByMsgId(int msgId) {
		for(PendingReq req:mPendingReqArray) {
			if(msgId == req.localMsgId) {
				mPendingReqArray.remove(req);
				return req;
			}
		}		
		return null;
	}
	
	public enum ReqStatus {
		WAITING_SEND,
		WAITING_RSP,
		GET_RSP,
		CANCEL,
		TIMEOUT,
	}
	
	public static class PendingReq {
		String sequence;
		int requestCode;
		int localMsgId;
		ReqStatus status;
		Object reqContent;
		WeakReference<Object> cbk;
		boolean result;
	}
	
	
	
}

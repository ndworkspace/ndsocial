package cn.nd.social.account.business;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.business.MeetingUtils.BSMeetingNetworkError;
import cn.nd.social.account.business.MeetingUtils.MeetingQueryType;

import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingManagerApi;
import com.nd.voice.meetingroom.manager.MeetingManagerCallBack;
import com.nd.voice.meetingroom.manager.User;

public class BusinessMeetingManager implements MeetingManagerApi {

	private final static String TAG = "BusinessMeetingManager";
	
	private static List<MeetingEntity> sMyList = new  ArrayList<MeetingEntity>();
	private static List<MeetingEntity> sOtherList = new  ArrayList<MeetingEntity>();
	private static HashMap<String,WeakReference<MeetingManagerCallBack>> sHashSeq = new  HashMap<String,WeakReference<MeetingManagerCallBack>>();
	
	private static HashMap<String,String> sHashSeqTimestamp = new  HashMap<String,String>();
	
	public static void resetData() {
		sMyList = new  ArrayList<MeetingEntity>();
		sOtherList = new  ArrayList<MeetingEntity>();
		sHashSeq = new  HashMap<String,WeakReference<MeetingManagerCallBack>>();
		sHashSeqTimestamp = new  HashMap<String,String>();
	}
	
	public static MeetingManagerCallBack getCbkFromBySeq(String seq) {
		WeakReference<MeetingManagerCallBack> weakRef = sHashSeq.get(seq);
		return weakRef != null? weakRef.get():null;
	}
	
	public static String getExtraInfoFromSeq(String seq) {
		if(sHashSeqTimestamp.containsKey(seq)) {
			return sHashSeqTimestamp.get(seq);
		}
		return "";
	}
	
	public static void removeCbkMap(String seq) {
		sHashSeq.remove(seq);
	}
	
	public static void removeExtraInfoMap(String seq) {
		sHashSeqTimestamp.remove(seq);
	}
	
	public BusinessMeetingManager() {
		
	}
	
	private MeetingManagerCallBack mMeetingMgrCbk;
	public BusinessMeetingManager(MeetingManagerCallBack cbk) {
		setCallBack(cbk);
	}
	public void init() {		
	}
	
	public void fini() {

	}


	@Override
	public void getDetailByMeetingId(String roomId) {
		long uid = CloundServer.getInstance().getUserId();
		String sequence = MeetingUtils.getSequceString(uid,"-detailquery");
		String jstr = MeetingUtils.constructQueryMeetingByRoomId(roomId, uid, sequence);
		CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), 
				CAConstant.LOCAL_MSG_ID_MEETING_QUERY_DETAIL, sequence);
		sHashSeq.put(sequence, new WeakReference<MeetingManagerCallBack>(mMeetingMgrCbk));
	}

	@Override
	public void addMeetingEntity(MeetingEntity meetingEntity,
			List<User> users) {
		long[]idList = new long[users.size()];
		for(int i=0;i<users.size();i++) {
			idList[i] = users.get(i).getUserid();
		}
		
		String timeStr = meetingEntity.getMeetingTime();
		
		long uid = CloundServer.getInstance().getUserId();
		String hostname = CloundServer.getInstance().getSelfInfo().getNickName();
		if(hostname == null || hostname.equals("")) {
			hostname = CloundServer.getInstance().getLogedUser();
		}
		String sequence = MeetingUtils.getSequceString(uid,"-add");
		String jstr = MeetingUtils.constructInvitation(meetingEntity.getTitle(), timeStr, 
																			uid,hostname,sequence,idList);
		
		CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), 
				CAConstant.LOCAL_MSG_ID_MEETING_ADD, sequence);
		sHashSeq.put(sequence, new WeakReference<MeetingManagerCallBack>(mMeetingMgrCbk));
		
		sHashSeqTimestamp.put(sequence, timeStr);
		
		Log.e(TAG,"add meeting:" + jstr);
	}

	
	@Override
	public void getMeetingList(String memberId) {
		long uid = CloundServer.getInstance().getUserId();
		String sequence = MeetingUtils.getSequceString(uid,"-listquery");
		String jstr = MeetingUtils.constructQueryMeeting(MeetingQueryType.ALL,uid, sequence);
		CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), 
				CAConstant.LOCAL_MSG_ID_MEETING_QUERY_ALL, sequence);		
		sHashSeq.put(sequence, new WeakReference<MeetingManagerCallBack>(mMeetingMgrCbk));
	}
	
	@Override
	public void setCallBack(MeetingManagerCallBack callback) {
		//BusinessEventRsp.getInstance().setMeetingMgrCbk(callback);
		mMeetingMgrCbk = callback;		
	}



	@Override
	public List<MeetingEntity> getMyMeetingListLocal() {
		
		return sMyList;
	}

	synchronized public static void removeMeeting(String meetingId) {
		if(meetingId == null) {
			Log.e(TAG,"removeMeeting:error meetingId");
			return;
		}
		for(MeetingEntity entity :sMyList) {
			if(entity != null && meetingId.equals(entity.getMeetingid())) {
				sMyList.remove(entity);
			}
		}
		
		for(MeetingEntity entity :sOtherList) {
			if(entity != null && meetingId.equals(entity.getMeetingid())) {
				sOtherList.remove(entity);
			}
		}
	}
	
	
	synchronized public static void addToLocalMeeting(MeetingEntity addEntity,boolean mine) {
		if(addEntity == null) {
			Log.e(TAG,"addToLocalMeeting error null entity");
			return;
		}
		
		if(addEntity.getMeetingid() == null) {
			Log.e(TAG,"addToLocalMeeting error: meetingID null");
			return;
		}
		if(mine) {			
			for(MeetingEntity entity :sMyList) {
				if(entity == null) {
					continue;
				}
				if(addEntity.getMeetingid().equals(entity.getMeetingid())) {
					return;
				}
			}
			sMyList.add(addEntity);
		} else {		
			for(MeetingEntity entity :sOtherList) {
				if(entity == null) {
					continue;
				}
				if(addEntity.getMeetingid().equals(entity.getMeetingid())) {
					return;
				}
			}
			sOtherList.add(addEntity);
		}
		
	}
	
	
	@Override
	public List<MeetingEntity> getOtherMeetingListLocal() {		
		return sOtherList;
	}
	
	public static void setMyMeetingList(List<MeetingEntity>list)  {
		sMyList = list;
	}
	
	public static void setInvitedMeetingList(List<MeetingEntity>list)  {
		sOtherList = list;
	}

	@Override
	public void delMeetingEntity(String meetingId) {
		long uid = CloundServer.getInstance().getUserId();
		String sequence = MeetingUtils.getSequceString(uid,"-cancel" + "-roomid-" + meetingId);
		String jstr = MeetingUtils.constructCancelMeeting(meetingId, "empty-title",uid,sequence);
		CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), 
				CAConstant.LOCAL_MSG_ID_MEETING_CANCEL, sequence);
		sHashSeq.put(sequence, new WeakReference<MeetingManagerCallBack>(mMeetingMgrCbk));
		
		Log.e(TAG,"del meeting:" + jstr);
	}

	@Override
	public void accpetMeeting(String meetingId, String notes) {
		String jstr = MeetingUtils.constructInviteNotifyAck(MeetingUtils.InviteAckType.ACCEPT, 
													"empty-title", meetingId,notes);
		
		CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), 
								CAConstant.LOCAL_MSG_ID_MEETING_ACCEPT, "");
		
		Log.e(TAG,"accept meeting:" + jstr);
	}

	@Override
	public void refuseMeeting(String meetingId, String notes) {
		String jstr = MeetingUtils.constructInviteNotifyAck(MeetingUtils.InviteAckType.REFUSE, 
				"empty-title", meetingId,notes);
		CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), 
				CAConstant.LOCAL_MSG_ID_MEETING_REFUSE, "");
	}

	@Override
	public void readedMeeting(String meetingId, String notes) {
		//TODO: implement
	}
	
	
	@Override
	public void addMeetingMember(String meetingId, List<Long> userids) {
		
		long[]idList = new long[userids.size()];
		for(int i=0;i<userids.size();i++) {
			idList[i] = userids.get(i);
		}

		
		
		long uid = CloundServer.getInstance().getUserId();
		String hostname = CloundServer.getInstance().getLogedUser();
		String sequence = MeetingUtils.getSequceString(uid,null);
		String jstr = MeetingUtils.constructAddMeetingMember(meetingId,uid,hostname,sequence,idList);
		
		CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), 
				CAConstant.LOCAL_MSG_ID_MEETING_APPEND_MEMBER, sequence);
		sHashSeq.put(sequence, new WeakReference<MeetingManagerCallBack>(mMeetingMgrCbk));		
	}
	
	public static void onMeetingReqError(BSMeetingNetworkError errorInfo) {
		MeetingManagerCallBack cbk = BusinessMeetingManager.getCbkFromBySeq(errorInfo.sequence);

			switch(errorInfo.actionType) {
			case CAConstant.LOCAL_MSG_ID_MEETING_ADD:
				if(cbk != null) {
					cbk.onAddMeetingEntityCallBack(null, false, errorInfo.info);
				}
				break;
			case CAConstant.LOCAL_MSG_ID_MEETING_CANCEL:
				if(cbk != null) {
					cbk.onDelMeetingentityCallBack(null, false, errorInfo.info);
				}				
				break;
			case CAConstant.LOCAL_MSG_ID_MEETING_QUERY_ALL:
				if(cbk != null) {
					cbk.onGetMeetingListCallBack(null,null, false, errorInfo.info);
				}	
				break;
			case CAConstant.LOCAL_MSG_ID_MEETING_QUERY_DETAIL:
				if(cbk != null) {
					cbk.onGetDetailByMeetingIdCallBack(null, null,false, errorInfo.info);
				}	
				break;
				
			case CAConstant.LOCAL_MSG_ID_MEETING_ACCEPT:
				if(cbk != null) {
					cbk.onAccpetMeetingCallBack(null, false, errorInfo.info);
				}
				break;
			case CAConstant.LOCAL_MSG_ID_MEETING_REFUSE:
				if(cbk != null) {
					cbk.onRefuseMeetingCallBack(null, false, errorInfo.info);
				}
				break;
			case CAConstant.LOCAL_MSG_ID_MEETING_APPEND_MEMBER:
				if(cbk != null) {
					cbk.onAddMeetingMemberCallBack("", false, "网络错误");
				}
			}
	}

	
}

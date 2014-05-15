package cn.nd.social.account.business;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.business.MeetingUtils.AddDocRsp;
import cn.nd.social.account.business.MeetingUtils.AddMeetingMemberRsp;
import cn.nd.social.account.business.MeetingUtils.AddMeetingRsp;
import cn.nd.social.account.business.MeetingUtils.CancelMeetingRsp;
import cn.nd.social.account.business.MeetingUtils.MeetingRsp;
import cn.nd.social.account.business.MeetingUtils.QueryListRsp;
import cn.nd.social.account.business.MeetingUtils.QueryRoomRsp;

import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingManagerCallBack;


public class MeetingMsgReceiver{
	
	private final static String TAG = "MeetingMsgReceiver";

	
	public static void onMeetingBusiness(MeetingRsp rsp,String action) {
		try {
			if(action.equals(MeetingUtils.MEETING_ADD_RSP)) {
				AddMeetingRsp addRsp = (AddMeetingRsp)rsp;
				boolean success = false;
				if("success".equals(addRsp.result)) {
					success = true;
				}
				MeetingManagerCallBack meetingCbk = BusinessMeetingManager.getCbkFromBySeq(addRsp.seqence);
				String meetingTime = BusinessMeetingManager.getExtraInfoFromSeq(addRsp.seqence);
				BusinessMeetingManager.removeCbkMap(addRsp.seqence);
				BusinessMeetingManager.removeExtraInfoMap(addRsp.seqence);
				
				MeetingDetailEntity entity = new MeetingDetailEntity();
				entity.setTitle(addRsp.title);
				entity.setMeetingid(addRsp.roomId);
				entity.setHostName(CloundServer.getInstance().getLogedUser());
				entity.setHostUserId(CloundServer.getInstance().getUserId());
				entity.setMeetingTime(meetingTime);
				BusinessMeetingManager.addToLocalMeeting(entity,true);
			
				
				if(meetingCbk != null) {
					meetingCbk.onAddMeetingEntityCallBack(entity, success, null);
				}
				
			} else if(action.equals(MeetingUtils.MEETING_ADD_MEMBER_RSP)) {
				AddMeetingMemberRsp addMemberRsp = (AddMeetingMemberRsp)rsp;
				boolean success = false;
				if("success".equals(addMemberRsp.result)) {
					success = true;
				}
				MeetingManagerCallBack meetingCbk = BusinessMeetingManager.getCbkFromBySeq(addMemberRsp.seqence);
				BusinessMeetingManager.removeCbkMap(addMemberRsp.seqence);
				
				if(meetingCbk != null) {
					meetingCbk.onAddMeetingMemberCallBack(addMemberRsp.roomId, success, null);
				}
				
			}  else if(action.equals(MeetingUtils.MEETING_QUERY_LIST_RSP)) {
				QueryListRsp queryRsp = (QueryListRsp)rsp;
				boolean success = false;
				if("success".equals(queryRsp.result)) {
					success = true;
				}
				
				MeetingManagerCallBack meetingCbk = BusinessMeetingManager.getCbkFromBySeq(queryRsp.seqence);
				if(meetingCbk == null) {
					Log.e(TAG,"onMeetingBusiness, MeetingManagerCallBack not set,action:"+ action);
					return;
				}

				long uid = CloundServer.getInstance().getUserId();
				//List<MeetingEntity> list = MeetingUtils.getMeetingList(queryRsp.jArrayMeetingList);
				List<MeetingEntity> myList = MeetingUtils.getMeetingList(queryRsp.jArrCreateList);
				List<MeetingEntity> invitedList = MeetingUtils.getMeetingList(queryRsp.jArrInviteList);
				List<MeetingEntity> list = new ArrayList<MeetingEntity>();
				BusinessMeetingManager.setMyMeetingList(myList);						
				BusinessMeetingManager.setInvitedMeetingList(invitedList);
				list.addAll(myList);
				list.addAll(invitedList);
				meetingCbk.onGetMeetingListCallBack(String.valueOf(uid), list, success, null);

			} else if(action.equals(MeetingUtils.MEETING_QUERY_ROOM_RSP)) {
				QueryRoomRsp roomRsp = (QueryRoomRsp)rsp;
				boolean success = false;
				if("success".equals(roomRsp.result)) {
					success = true;
				}
				
				MeetingManagerCallBack meetingCbk = BusinessMeetingManager.getCbkFromBySeq(roomRsp.seqence);
				BusinessMeetingManager.removeCbkMap(roomRsp.seqence);
				if(meetingCbk == null) {
					Log.e(TAG,"onMeetingBusiness, MeetingManagerCallBack not set,action:"+ action);
					return;
				}
				MeetingDetailEntity entity = MeetingUtils.getMeetingDetail(roomRsp.meetingInfo,roomRsp.memberInfo);
				if(entity == null) {
					meetingCbk.onGetDetailByMeetingIdCallBack(null, entity, false, "网络错误");
				} else {
					meetingCbk.onGetDetailByMeetingIdCallBack(entity.getMeetingid(), entity, success, null);
				}
				

			} else if(action.equals(MeetingUtils.MEETING_CANCEL_RSP)) {
				CancelMeetingRsp cancelRsp = (CancelMeetingRsp)rsp;
				boolean success = false;
				if("success".equals(cancelRsp.result)) {
					success = true;
				}
				String roomid = "0";
				String seq = cancelRsp.seqence;
				String roomHint = "roomid-";
				
				if(seq != null && seq.contains(roomHint)) {
					roomid = seq.substring(seq.indexOf(roomHint)+ roomHint.length());
				}
				BusinessMeetingManager.removeMeeting(roomid);
				MeetingManagerCallBack meetingCbk = BusinessMeetingManager.getCbkFromBySeq(seq);
				BusinessMeetingManager.removeCbkMap(seq);
				
				if(meetingCbk == null) {
					Log.e(TAG,"onMeetingBusiness, MeetingManagerCallBack not set,action:"+ action);
					return;
				}
				meetingCbk.onDelMeetingentityCallBack(roomid, success, null);
			} else if(action.equals(MeetingUtils.MEETING_ADD_DOCUMENT_RSP))  {
				AddDocRsp addDocRsp = (AddDocRsp)rsp;
				MeetingManagerCallBack meetingCbk = BusinessMeetingManager.getCbkFromBySeq(addDocRsp.action);
				BusinessMeetingManager.removeCbkMap(addDocRsp.action);
				
				if(meetingCbk == null) {
					Log.e(TAG,"onMeetingBusiness, MeetingManagerCallBack not set,action:"+ action);
					return;
				}
				boolean success = "success".equals(addDocRsp.result);
				
				meetingCbk.onAddMeetingDoc(addDocRsp.roomid, addDocRsp.filename, success, null);
				
			} else {
				MeetingEventNotify.onNotify(rsp);
			}
			
		} catch(Exception e) {
			Log.e(TAG,"onMeetingBusiness error");
			e.printStackTrace();
		}
	}

}

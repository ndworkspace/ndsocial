package com.nd.voice.meetingroom.manager;

import java.util.List;

public interface MeetingManagerCallBack {
	
	public void onGetMeetingListCallBack(String memberId,List<MeetingEntity> list,boolean success,String msg);
	
	public void onGetDetailByMeetingIdCallBack(String meetingId,MeetingDetailEntity entity,boolean success,String msg);
	
	public void onAddMeetingEntityCallBack(MeetingDetailEntity entity,boolean success,String msg);
	
	public void onDelMeetingentityCallBack(String meetingId,boolean success,String msg);
	
	public void onAccpetMeetingCallBack(String meetingId,boolean success,String msg);
	
	public void onRefuseMeetingCallBack(String meetingId,boolean success,String msg);
	
	public void onAddMeetingMemberCallBack(String meetingId,boolean success,String msg);
	
	public void onAddMeetingDoc(String meetingId,String filename,boolean success,String msg);

}

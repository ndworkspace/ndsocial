package com.nd.voice.meetingroom.manager;

import java.util.List;

public interface MeetingManagerApi {
	
	public List<MeetingEntity> getMyMeetingListLocal(); //获取自己预约的
	
	public List<MeetingEntity> getOtherMeetingListLocal();
	
	public void getMeetingList(String memberId);
	
	public void getDetailByMeetingId(String meetingId);
	
	public void addMeetingEntity(MeetingEntity meetingEntity,List<User> users);
	
	public void delMeetingEntity(String meetingId);
	
	public void setCallBack(MeetingManagerCallBack callBack);
	
	public void accpetMeeting(String meetingId,String message);
	
	public void refuseMeeting(String meetingId,String message);
	
	public void readedMeeting(String meetingId,String message);
	
	public void addMeetingMember(String meetingId,List<Long> userids);
	
}

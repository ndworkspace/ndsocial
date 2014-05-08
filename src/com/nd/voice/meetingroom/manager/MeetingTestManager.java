package com.nd.voice.meetingroom.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MeetingTestManager implements MeetingManagerApi{
	
	private static List<MeetingEntity> allMeetings = new ArrayList<MeetingEntity>();
	
	private static Map<String,List<User>> meetingUserMap = new HashMap<String, List<User>>();
	
	private static MeetingTestManager single = null;
	
	private MeetingManagerCallBack mCallback;
	
	private UserManagerApi mUserManager;
	
	public synchronized  static MeetingTestManager getInstance(){
		if(single == null){
			single = new MeetingTestManager();
			single.init();
		}
		return single;
	}
	
	public MeetingTestManager(MeetingManagerCallBack callBack) {
		super();
		this.mCallback = callBack;
		init();
	}
	
	public MeetingTestManager() {
		super();
		init();
	}

	private void init(){
		mUserManager = new UserTestManager();
		if(meetingUserMap.isEmpty()){
			for (int i = 0; i < 2; i++) {
				MeetingEntity entity = new MeetingEntity();
				entity.setTitle("关于会议建立的讨论" + i);
				entity.setMeetingTime("4-15 15:30");
				entity.setMeetingid(entity.getHostUserId() + "001101" + i);
				allMeetings.add(entity);
				meetingUserMap.put(entity.getMeetingid(), mUserManager.getMyFriendList());
			}
			
			for (int i = 0; i < 4; i++) {
				MeetingEntity entity = new MeetingEntity();
				User user = mUserManager.getUserInfoLocal(548700l + i);
				entity.setHostName(user.getNickName());
				entity.setHostUserId(user.getUserid());
				entity.setTitle("小讨论" + i);
				entity.setMeetingTime("4-15 17:30");
				entity.setMeetingid(entity.getHostUserId() + "001101" + i);
				allMeetings.add(entity);
				meetingUserMap.put(entity.getMeetingid(), mUserManager.getMyFriendList());
			}
		}
	}

	public List<MeetingEntity> getMyMeetingListLocal() {
		List<MeetingEntity> list = new ArrayList<MeetingEntity>();
		for(MeetingEntity entity : allMeetings){
			if(entity.getHostUserId() == mUserManager.getMyInfo().getUserid()){
				list.add(entity);
			}
		}
		return list;
	}

	public List<MeetingEntity> getOtherMeetingListLocal() {
		// TODO Auto-generated method stub
		List<MeetingEntity> list = new ArrayList<MeetingEntity>();
		for(MeetingEntity entity : allMeetings){
			if(entity.getHostUserId() != mUserManager.getMyInfo().getUserid()){
				list.add(entity);
			}
		}
		return list;
	}

	public void getDetailByMeetingId(String meetingId) {
		MeetingEntity _entity = null;
		for(MeetingEntity entity : allMeetings){
			if(entity.getMeetingid().equals(meetingId)){
				_entity = entity;
				break;
			}
		}
		List<User> users = meetingUserMap.get(meetingId);
		ArrayList<User> friends = new ArrayList<User>(users);
		friends.remove(mUserManager.getUserInfoLocal(_entity.getHostUserId()));
		MeetingDetailEntity detailEntity = new MeetingDetailEntity(_entity);
		detailEntity.addUsers(friends);
		detailEntity.setHostUserInfo(mUserManager.getUserInfoLocal(_entity.getHostUserId()));
		mCallback.onGetDetailByMeetingIdCallBack(meetingId, detailEntity,true,null);
	}

	public void addMeetingEntity(MeetingEntity meetingEntity,
			List<User> users) {
		meetingEntity.setMeetingid(meetingEntity.getHostName() + "meeting" + allMeetings.size());
		allMeetings.add(meetingEntity);
		meetingUserMap.put(meetingEntity.getMeetingid(), users);
		MeetingDetailEntity detailEntity = new MeetingDetailEntity(meetingEntity);
		detailEntity.addUsers(users);
		mCallback.onAddMeetingEntityCallBack(detailEntity,true,null);
	}


	@Override
	public void getMeetingList(String memberId) {
		// TODO Auto-generated method stub
		mCallback.onGetMeetingListCallBack(memberId, allMeetings,true,null);
	}


	@Override
	public void delMeetingEntity(String meetingId) {
		// TODO Auto-generated method stub
		for (MeetingEntity entity : allMeetings) {
			if(entity.getMeetingid().equals(meetingId)){
				allMeetings.remove(entity);
				meetingUserMap.remove(meetingId);
			}
		}
		mCallback.onDelMeetingentityCallBack(meetingId, true, null);
	}


	@Override
	public void setCallBack(MeetingManagerCallBack callBack) {
		// TODO Auto-generated method stub
		mCallback = callBack;
	}

	@Override
	public void accpetMeeting(String meetingId, String message) {
		// TODO Auto-generated method stub
		this.mCallback.onAccpetMeetingCallBack(meetingId, true, null);
	}

	@Override
	public void refuseMeeting(String meetingId, String message) {
		// TODO Auto-generated method stub
		this.mCallback.onRefuseMeetingCallBack(meetingId,true,null);
	}

	@Override
	public void readedMeeting(String meetingId, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMeetingMember(String meetingId, List<Long> userids) {
		// TODO Auto-generated method stub
		
	}




}

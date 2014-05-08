package com.nd.voice.meetingroom.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeetingDetailEntity extends MeetingEntity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8326526336496701195L;

	User hostUserInfo;//主持人信息
	
	List<MeetingUser> users;//参与人员


	public List<MeetingUser> getUsers() {
		return users;
	}

	public void setUsers(List<MeetingUser> users) {
		this.users = users;
	}
	
	public long[] getUserIdArray() {
		if(users == null || users.size() == 0) {
			return null;
		}
		long []idArray = new long[users.size()]; 
		for(int i=0; i<users.size();i++) {
			idArray[i] = users.get(i).getUserid();
		}
		return idArray;
	}

	public User getHostUserInfo() {
		return hostUserInfo;
	}

	public void setHostUserInfo(User hostUserInfo) {
		this.hostUserInfo = hostUserInfo;
	}

	public MeetingDetailEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MeetingDetailEntity(MeetingEntity entity) {
		super();
		this.setHostName(entity.getHostName());
		this.setHostUserId(entity.getHostUserId());
		this.setMeetingid(entity.getMeetingid());
		this.setMeetingTime(entity.getMeetingTime());
		this.setTitle(entity.getTitle());
	}

	
	public void addUsers(List<User> users){
		if(this.users == null){
			this.users = new ArrayList<MeetingUser>();
			for (User user : users) {
				MeetingUser mUser = new MeetingUser(user);
				mUser.setMeetingId(this.getMeetingid());
				this.users.add(mUser);
			}
		}
	}	
	
}

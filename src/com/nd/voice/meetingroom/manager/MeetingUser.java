package com.nd.voice.meetingroom.manager;

import java.io.Serializable;


public class MeetingUser extends User implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2359375776504001384L;
	
	
	public static final int REPLYSTATE_UNDEFINE = 0;
	public static final int REPLYSTATE_ACCEPTED = 1;
	public static final int REPLYSTATE_REJECTED = 2;
	
	private String meetingId;
	private int replyState;
	
	public MeetingUser() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MeetingUser(User user){
		super();
		this.setUserid(user.getUserid());
		this.setUserName(user.getUserName());
		this.setMobile(user.getMobile());
		this.setNickName(user.getNickName());
		this.setAddress(user.getAddress());
		this.setDefaultFace(user.getDefaultFace());
		this.setEmail(user.getEmail());
		this.setCompany(user.getCompany());
		this.setPassword(user.getPassword());
		this.setPhone(user.getPhone());
	}
	
	
	public String getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}
	public int getReplyState() {
		return replyState;
	}
	public void setReplyState(int replyState) {
		this.replyState = replyState;
	}
	
	public String getReplyStateString(){
		String stateString = "已发送邀请" ;
		switch (replyState) {
		case REPLYSTATE_UNDEFINE:
			break;
		case REPLYSTATE_ACCEPTED:
			stateString = "已接受邀请";
			break;
		case REPLYSTATE_REJECTED:
			stateString = "已拒绝邀请";
			break;
		default:
			break;
		}
		return stateString;
	}
	
	
}

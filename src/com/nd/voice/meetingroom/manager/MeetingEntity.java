package com.nd.voice.meetingroom.manager;

import java.io.Serializable;

import cn.nd.social.account.usermanager.UserManager;

public class MeetingEntity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5070439487183806461L;
	
	
	public static final int REPLYSTATE_ACCEPTED = 1;
	public static final int REPLYSTATE_REJECTED = 2;
	public static final int REPLYSTATE_UNDEFINE = 0;
	
	private String meetingid;//会议编号
	private String title;//标题
	private String hostName; //主持人
	private long hostUserId;//主持人编号
	private String meetingTime;//开始时间
	private String endTime;//过期时间
	
	private int replyState;//回复状态
	private boolean isAlarmed;//是否已设闹钟
	private boolean isDestory;//是否已被销毁
	
	public MeetingEntity(String meetingid, String title, String hostName,
			String meetingTime) {
		super();
		this.meetingid = meetingid;
		this.title = title;
		this.hostName = hostName;
		this.meetingTime = meetingTime;
	}
	
	public MeetingEntity(String meetingid, String title, String meetingTime) {
		super();
		this.meetingid = meetingid;
		this.title = title;
		this.meetingTime = meetingTime;
	}
	
	public MeetingEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getMeetingUid(){
		return meetingid + "@nd.social";
	}
	
	public String getMeetingid() {
		return meetingid;
	}
	public void setMeetingid(String meetingid) {
		this.meetingid = meetingid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getMeetingTime() {
		return meetingTime;
	}
	public void setMeetingTime(String meetingTime) {
		this.meetingTime = meetingTime;
	}
	public long getHostUserId() {
		return hostUserId;
	}
	public void setHostUserId(long hostUserId) {
		this.hostUserId = hostUserId;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getReplyState() {
		return replyState;
	}

	public void setReplyState(int replyState) {
		this.replyState = replyState;
	}

	public boolean isAlarmed() {
		return isAlarmed;
	}

	public void setAlarmed(boolean isAlarmed) {
		this.isAlarmed = isAlarmed;
	}

	public boolean isDestory() {
		return isDestory;
	}

	public void setDestory(boolean isDestory) {
		this.isDestory = isDestory;
	}
	
	
	public static String getMeetingIdByUid(String meetingUid) {
		if(meetingUid == null ) {
			return null;
		}
		final String postFix = "@nd.social";
		if(meetingUid.endsWith(postFix)) {
			return meetingUid.substring(0,meetingUid.length() - postFix.length());
		}
		return meetingUid;
	}

	
	
}

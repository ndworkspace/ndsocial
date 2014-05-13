package com.nd.voice.meetingroom.manager;

import java.io.Serializable;
import java.util.Random;

import cn.nd.social.R;


public class User implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1376371800187065393L;
	
	private long userid;//用户编号
	private String userName;
	private String nickName;
	private int state; //状态   0:未审核    1:   
	private String phone;
	private String faceUrl;//头像地址
	private String password;//密码
	private String address;
	private String mobile;
	private String company;
	private String email;
	private int defaultFace;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getNickName() {
		if(nickName != null){
			return nickName;
		}
		return userName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getFaceUrl() {
		return faceUrl;
	}
	public void setFaceUrl(String faceUrl) {
		this.faceUrl = faceUrl;
	}
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getDefaultFace() {
		return defaultFace;
	}
	public void setDefaultFace(int defaultFace) {
		this.defaultFace = defaultFace;
	}
	
	public void getRandomFace(){
		int random = new Random().nextInt(5) + 1;
		this.setDefaultFace(random);
	}
	
	public int getDefaultFaceResource(){
		int resource = 1;
		switch (this.defaultFace) {
		case 1:
			resource = R.drawable.face1;
			break;
		case 2:
			resource = R.drawable.face2;
			break;
		case 3:
			resource = R.drawable.face3;
			break;
		case 4:
			resource = R.drawable.face4;
			break;
		case 5:
			resource = R.drawable.face5;
			break;
		default:
			resource = R.drawable.face1;
			break;
		}
		return resource;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (userid ^ (userid >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userid != other.userid)
			return false;
		return true;
	}
	
	public String  getUserUid(){
		return userid + "@nd.social";
	}
	
	public static long getUserIdByUid(String uid){
		String postFix = "@nd.social";
		if(uid.contains(postFix)) {
			String str = uid.substring(0, uid.length() - "@nd.social".length());
			return Long.parseLong(str);
		} else {
			return Long.parseLong(uid);
		}
		
	}
	
	public static String getMeetingUserId(long userid){
		return userid + "@nd.social";
	}
}

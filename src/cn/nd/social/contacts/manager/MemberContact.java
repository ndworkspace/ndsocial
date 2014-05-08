package cn.nd.social.contacts.manager;

import cn.nd.social.util.Utils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "MemberContacts")
public class MemberContact extends Model{
	
	@Column(name = "PHONENUMBER")
	private String phoneNumber;
	
	@Column(name = "CONTACTNAME")
	private String contactName;
	
	@Column(name = "CONTACTID")
	private Long contactid;
	
	@Column(name = "FRIENDFLAG")
	private int friendFlag = 2;//1.未加为好友   2.非用户   3.已加为好友
	
	@Column(name = "UID")
	private Long uid;
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public int getFriendFlag() {
		return friendFlag;
	}
	public void setFriendFlag(int friendFlag) {
		this.friendFlag = friendFlag;
	}
	public Boolean isFriend() {
		return friendFlag==3;
	}
	public boolean isMember(){
		return friendFlag!=2;
	}
	
	public void setFriend(Boolean isFriend) {
		if(isFriend){
			friendFlag = 3;
		}else{
			friendFlag = 1;
		}
	}
	public Long getContactid() {
		return contactid;
	}
	public void setContactid(Long contactid) {
		this.contactid = contactid;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	
	public String getMobileMD5(){
		return Utils.getMD5(this.getPhoneNumber());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemberContact other = (MemberContact) obj;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		return true;
	}
	
}

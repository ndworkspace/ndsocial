package cn.nd.social.account.business;

import cn.nd.social.SocialApplication;
import cn.nd.social.account.CACallBack;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.business.MeetingUtils.BSMeetingNetworkError;
import NDCSdk.INDCClient;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class NDCData {
	public static class LoginData {
		public LoginData(String name,String pwd,Object extra) {
			this.userName = name;
			this.passwd = pwd;
			this.extraInfo = extra;
		}
		public String userName;
		public String passwd;
		public Object extraInfo;
	}
	
	public static class RegisterData {
		public RegisterData(String name,String pwd,Object extra) {
			this.userName = name;
			this.passwd = pwd;
			this.extraInfo = extra;
		}
		public String userName;
		public String passwd;
		public Object extraInfo;
	}
	
	public static class AliasData {
		public AliasData(String alias,short type) {
			this.alias = alias;
			this.type = type;
		}
		public String alias;
		public short type;
	}
	
	public static class AddFriendData {
		public AddFriendData(long uid,int type,String group) {
			this.uid = uid;
			this.group = group;
			this.type = type;
		}
		public long uid;
		public String group;
		public int type;
	}
	
	public static class DelFriendData {
		public DelFriendData(long uid,int type) {
			this.uid = uid;
			this.type = type;
		}
		public long uid;
		public int type;
	}
	
	
	public static class BSMsgData {
		public BSMsgData(byte[]data,int bsType,int subType,String seq) {
			this.data = data;
			this.bsType = bsType;
			this.subType = subType;
			this.seq = seq;
		}
		public byte[] data;
		public int bsType;
		public int subType;
		public String seq;
	}
}

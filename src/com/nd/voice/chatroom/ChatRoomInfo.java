package com.nd.voice.chatroom;



public class ChatRoomInfo {

	private long uidCreater;

	private int resIdCreater;
	
	private long roomId;
	
	private String roomName;
	
	private String timeEnterRoom;
	
	public ChatRoomInfo(long uid,String rmName,String time) {
		uidCreater = uid;
		roomId = uid;
		roomName = rmName;
		timeEnterRoom = time;
	}	

	public long getUidCreater() {
		return uidCreater;
	}

	public int getResIdCreater() {
		return resIdCreater;
	}

	public long getRoomId() {
		return roomId;
	}

	public String getRoomName() {
		return roomName;
	}

	public String getTimeEnterRoom() {
		return timeEnterRoom;
	}




	public class usrInfo{
		private long uid;
		private int resId;
		private String usrName;
		
		public long getUid() {
			return uid;
		}
		public void setUid(long uid) {
			this.uid = uid;
		}
		public int getResId() {
			return resId;
		}
		public void setResId(int resId) {
			this.resId = resId;
		}
		public String getUsrName() {
			return usrName;
		}
		public void setUsrName(String usrName) {
			this.usrName = usrName;
		}
		
		
	}

	
}


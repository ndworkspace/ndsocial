package cn.nd.social.account.usermanager;

public class UserInfo {

	public UserInfo(String userName, Long userId) {
			this.userName = userName;
			this.userId = userId;

		}
		
		private String userName;
		private long userId;
		private String extraInfo;
		private String alias;
		private String nickname;
		private long updateTimestamp;
		
		public String getUserName() {
			return userName;
		}
		public long getUserId() {
			return userId;
		}
		public String getNickname() {
			return nickname;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public void setUserId(long userId) {
			this.userId = userId;
		}
		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		
}

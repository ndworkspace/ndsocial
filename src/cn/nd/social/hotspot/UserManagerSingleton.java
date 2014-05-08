package cn.nd.social.hotspot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Build;
import android.util.Log;

public class UserManagerSingleton {
	// /////////////////////////////////////////////////////////////////////
	// singleton
	// /////////////////////////////////////////////////////////////////////
	private static UserManagerSingleton _instance = null;

	public static UserManagerSingleton getInstance() {
		if (_instance == null) {
			synchronized (UserManagerSingleton.class) {
				if (_instance == null) {
					_instance = new UserManagerSingleton();
				}
			}
		}
		return _instance;
	}

	// /////////////////////////////////////////////////////////////////////
	// internal class define
	// /////////////////////////////////////////////////////////////////////
	public static class UserInfo {
		String userName;
		String ip;
		String mac;
	}

	public static class FileSendInfo {
		int sendSequence;
		int recvSequence;
		short fileDataSN;
		String sendTo;
		String appName;
		String fileName;
		String fileFullName;
		int fileSize;
		int fileType;
		int grantType;
		int grantValue;
		int grantReserve;
		String md5;
		FileInputStream fileInput;

		public FileSendInfo() {
			sendSequence = 0;
			recvSequence = 0;
			fileDataSN = 0;
			fileSize = 0;
			
			fileType = 0;
			
			grantType = 0;
			grantValue = 0;
			grantReserve = 0;
		}
	}

	public static class FileReceiveInfo {
		short fileDataSN;
		String userName;
		String appName;
		String fileName;
		int fileSize;
		int fileType;
		int grantType;
		int grantValue;
		int grantReserve;
		byte[] md5;
		int receivedSize;
		FileOutputStream fileOutput;

		public FileReceiveInfo() {
			fileDataSN = 0;
			fileSize = 0;
			receivedSize = 0;

			fileType = 0;
			
			grantType = 0;
			grantValue = 0;
			grantReserve = 0;
		}
	}

	// /////////////////////////////////////////////////////////////////////
	// method
	// /////////////////////////////////////////////////////////////////////
	private int mCurSequence = 10000000;

	private UserInfo mSelfUserInfo = null;
	private Map<String, UserInfo> mUserMap = null;
	private Map<Integer, FileSendInfo> mFileSendMap = null;
	private Map<Integer, FileReceiveInfo> mFileReceivingMap = null;

	private UserManagerSingleton() {
		if (mFileSendMap == null) {
			mFileSendMap = new HashMap<Integer, FileSendInfo>();
		}

		if (mFileReceivingMap == null) {
			mFileReceivingMap = new HashMap<Integer, FileReceiveInfo>();
		}

		if (mSelfUserInfo == null) {
			mSelfUserInfo = new UserInfo();
		}

		if (mUserMap == null) {
			mUserMap = new HashMap<String, UserInfo>();
		}
	}

	public void clear() {
		if (mUserMap != null) {
			mUserMap.clear();
		}

		if (mFileSendMap != null) {
			mFileSendMap.clear();
		}

		if (mFileReceivingMap != null) {
			mFileReceivingMap.clear();
		}
	}

	public int getSequence() {
		return mCurSequence++;
	}

	// user info
	public boolean addUserInfo(UserInfo userInfo, boolean isLocal) {
		if (isLocal) {
			//TODO : add IMEI or ip address as suffix
			mSelfUserInfo.ip = Utils.getLocalIpAddressAlter();
			String selfIp = mSelfUserInfo.ip;
			if(selfIp != null && selfIp.contains(".")) {
				int start = selfIp.lastIndexOf(".");
				mSelfUserInfo.userName = Build.MANUFACTURER + selfIp.substring(start);
			} else {
				mSelfUserInfo.userName = Build.MANUFACTURER;
			}
			
			
			Log.e("user manager", "local ip : " + mSelfUserInfo.ip);

			mSelfUserInfo.mac = "MAC ABCD";
		} else {
			mUserMap.put(userInfo.userName, userInfo);
		}

		return true;
	}

	public UserInfo delUserInfo(String userName) {
		return mUserMap.remove(userName);
	}

	public UserInfo getUserInfo(String name, boolean isLocal) {
		if (isLocal) {
			return mSelfUserInfo;
		} else {
			return mUserMap.get(name);
		}
	}

	public Set<UserInfo> getRegisterUsersInfo() {
		if (mUserMap.isEmpty()) {
			return null;
		}

		Set<UserInfo> usersInfo = new HashSet<UserInfo>();

		for (Map.Entry entry : mUserMap.entrySet()) {
			UserInfo info = (UserInfo) entry.getValue();

			usersInfo.add(info);
		}

		return usersInfo;
	}

	// send info
	public boolean addFileSendInfo(int sequence, FileSendInfo fileSendInfo) {
		mFileSendMap.put(sequence, fileSendInfo);

		return true;
	}

	public FileSendInfo delFileSendInfo(int sequence) {
		return mFileSendMap.remove(sequence);
	}

	public FileSendInfo getFileSendInfo(int sequence) {
		return mFileSendMap.get(sequence);
	}

	public FileSendInfo getFileSendInfo(String fileName) {
		for (Map.Entry entry : mFileSendMap.entrySet()) {

			Object key = entry.getKey();

			FileSendInfo info = (FileSendInfo) entry.getValue();
			if (info.fileFullName.contentEquals(fileName)) {

				return info;
			}
		}

		return null;
	}

	// receive info
	public boolean addFileReceiveInfo(int sequence,
			FileReceiveInfo fileReceiveInfo) {
		mFileReceivingMap.put(sequence, fileReceiveInfo);

		return true;
	}

	public FileReceiveInfo delFileReceiveInfo(int sequence) {
		return mFileReceivingMap.remove(sequence);
	}

	public FileReceiveInfo getFileReceiveInfo(int sequence) {
		return mFileReceivingMap.get(sequence);
	}

	public FileReceiveInfo getFileReceiveInfo(String userName, String fileName) {
		for (Map.Entry entry : mFileReceivingMap.entrySet()) {

			Object key = entry.getKey();

			FileReceiveInfo info = (FileReceiveInfo) entry.getValue();
			if (info.userName.contentEquals(userName)
					&& info.fileName.contentEquals(fileName)) {

				return info;
			}
		}

		return null;
	}

}

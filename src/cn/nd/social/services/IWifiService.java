package cn.nd.social.services;

import android.os.Parcel;
import android.os.Parcelable;


public interface IWifiService {

	//broadcast intent filter action string
	public static final String INTENAL_ACTION_WIFI_SERVICE_NOTIFY = "cn.nd.social.wifi_service_notify";
	
	//wifi type
	public final static int WIFI_TYPE_NONE = 0;
	public final static int WIFI_TYPE_WIFI = 1;
	public final static int WIFI_TYPE_HOTSPOT = 2;
	
	//wifi state
	public final static int WIFI_STATE_NONE = 0;
	public final static int WIFI_STATE_HOTSOPT_OPENING = 1;
	public final static int WIFI_STATE_HOTSOPT_OPEN_SUCCESS = 2;
	public final static int WIFI_STATE_HOTSOPT_OPEN_FAILED = 3;
	
	public final static int WIFI_STATE_WIFI_CONNECTING = 5;
	public final static int WIFI_STATE_WIFI_CONNECT_SUCCESS = 6;
	public final static int WIFI_STATE_WIFI_CONNECT_FAILED = 7;
	
	// export method
	public abstract int			openWifi(NDWifiInfo info);
	public abstract int			closeWifi();
	
	public abstract NDWifiInfo	getNDWifiInfo();
	
	/// class : NDWifiInfo
	public static class NDWifiInfo implements Parcelable {
		// hotspot type
		public int		mWifiType;
		
		public String	mSsid;
		public String	mPasswd;
		
		public int		mState;
		
		public String	mLocalIP;

		public NDWifiInfo(int wifiType, String ssid, String passwd){
			mWifiType = wifiType;
			
			mSsid = ssid;
			mPasswd = passwd;
			
			mState = WIFI_STATE_NONE;
		}
		
		public NDWifiInfo(int wifiType, String ssid, String passwd, int state, String localIP){
			mWifiType = wifiType;
			
			mSsid = ssid;
			mPasswd = passwd;
			
			mState = state;
			
			mLocalIP = localIP;
		}
		
	    private NDWifiInfo(Parcel in) {
	    	mWifiType = in.readInt();
	    	
	    	mSsid = in.readString();
	    	mPasswd = in.readString();
	    	
	    	mState = in.readInt();
	    	
	    	mLocalIP = in.readString();
	    }
		
	    @Override  
	    public int describeContents() {  
	        return 0;  
	    }
	    
	    @Override  
	    public void writeToParcel(Parcel parcel, int flag) {  
	        parcel.writeInt(mWifiType);
	        
	        parcel.writeString(mSsid);
	        parcel.writeString(mPasswd);
	        
	        parcel.writeInt(mState);
	        
	        parcel.writeString(mLocalIP);
	    }
	    
	    public static final Creator<NDWifiInfo> CREATOR = new Creator<NDWifiInfo>() {  
	        public NDWifiInfo createFromParcel(Parcel in) {  
	            return new NDWifiInfo(in);  
	        }  
	  
	        public NDWifiInfo[] newArray(int size) {  
	            return new NDWifiInfo[size];  
	        }  
	    };
	}
}

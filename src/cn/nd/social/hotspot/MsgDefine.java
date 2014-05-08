package cn.nd.social.hotspot;

public class MsgDefine {
	// /MSG DEFINE
	// activity request code [0 - 99]
	public static final short ACTIVITY_MSG_CREATE_HOTSPOT = 9999;

	// internal [1 - 9999]
	// handler

	public static final short HANDLER_NOTIFY_INFO = 1;

	public static final short HANDLER_MSG_CREATE_HOTSPOT = 10;
	public static final short HANDLER_MSG_CONNECT_WIFI = 11;
	public static final short HANDLER_MSG_KICKOUT_USER = 21;

	public static final short HANDLER_MSG_FILE_SEND_REQ = 100;
	public static final short HANDLER_MSG_FILE_SEND_FIN = 102;

	// network service
	public static final short HANDLER_MSG_LISTENED = 201;
	public static final short HANDLER_MSG_NEW_CONNECT = 202;

	// network connect
	public static final short HANDLER_MSG_CONNECTED = 301;

	// network [10000 - 19999]
	// user register
	public static final short NETWORK_MSG_LOGIN = 10000;
	public static final short NETWORK_MSG_LOGIN_ACK = 10001;
	public static final short NETWORK_MSG_LOGOUT = 10002;

	public static final short NETWORK_BROADCAST_MSG_LOGIN = 10005;
	public static final short NETWORK_BROADCAST_MSG_LOGOUT = 10006;

	public static final short NETWORK_MSG_KICKOUT = 10010;
	public static final short NETWORK_MSG_HEARTBEAT = 10099;

	// file send
	public static final short NETWORK_MSG_FILE_SEND_REQ = 10100;
	public static final short NETWORK_MSG_FILE_SEND_REQ_ACK = 10101;

	public static final short NETWORK_MSG_FILE_SEND_FIN = 10102;

	public static final short NETWORK_MSG_FILE_DATA = 10106;
	public static final short NETWORK_MSG_FILE_DATA_ACK = 10107;

	// sync browsing
	public static final short NETWORK_SYNC_BROWSING_SHAKEHAND = 10200;
	public static final short NETWORK_SYNC_BROWSING_SHAKEHAND_ACK = 10201;
	
	public static final short NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST = 10210;
	
	public static final short NETWORK_SYNC_BROWSING_BROADCAST_ACTION = 10212;
	public static final short NETWORK_SYNC_BROWSING_BROADCAST_ACTION_ACK = 10213;
	
	public static final short NETWORK_SYNC_BROWSING_PAGE_REQUEST = 10220;
	public static final short NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK = 10221;
	
	public static final short NETWORK_MSG_TYPE_END = 19999;
	
	
	

	// grant type define
	public final static int GRANT_FILE_AUTO_DESTROY = 100001;
	public final static int GRANT_APK_SILENT_INSTALL = 100002;

	
	//file type define
	public final static int FILE_TYPE_UNKNOWN = 0;
	public final static int FILE_TYPE_APP = 1;
	public final static int FILE_TYPE_IMAGE = 2;
	public final static int FILE_TYPE_MEDIA = 3;
	public final static int FILE_TYPE_FILE = 4;
	public final static int FILE_TYPE_TITLE = 5;
	public final static int FILE_TYPE_CARD = 6;
	
	//Main.java UI message
	public static final short MAIN_UI_HANDLER_CONNSTATUS = 30001;
	public static final short MAIN_UI_HANDLER_BE_KICKOUT_IF = 30002;
	public static final short MAIN_UI_HANDLER_BE_KICKOUT_ELES = 30003;
	public static final short MAIN_UI_HANDLER_USER_LOGIN = 30004;
	public static final short MAIN_UI_HANDLER_SEND_FILE = 30005;
	public static final short MAIN_UI_HANDLER_RECV_FILE_FINISH = 30006 ;
	public static final short MAIN_UI_HANDLER_HOTSPOTCREATE = 30007;
	
	//Status of Message History in DB
	public static final int STATUS_DO_NOTHING = 0;
	public static final int STATUS_HAS_BEEN_DESTROIED = 1;
	public static final int STATUS_NEED_DELETE = 2;
	public static final int STATUS_NEED_DELETE_AND_OPENED = 3; 
	
	
	
	
}

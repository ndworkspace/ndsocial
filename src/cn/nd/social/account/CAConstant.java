package cn.nd.social.account;

public class CAConstant {

	/**
	 * NDC net protocol start
	 * */
	//register request [VALUE：USERID TYPE：LONG]
	public static final int REGISTER_REQ 				= 10001;
	//login requst [VALUE：USERID TYPE：LONG]
	public static final int LOGIN_REQ 				    = 10002;
	//logout request[]
	public static final int LOGOUT_REQ				    = 10003;
	//update user request[]
	public static final int UPDATE_USER_INFO_REQ		= 10004;
	//check user request [VALUE: USERINFO, TYPE: JSON STRING]
	public static final int QUERY_USER_INFO_REQ		    = 10005;
	//add friend request [VALUE：USERID, TYPE：LONG]
	public static final int ADD_FRIEND_REQ			    = 10006;
	//delete friend request [VALUE：USERID, TYPE：LONG]
	public static final int DEL_FRIEND_REQ			  	= 10007;
	//query friend info request [VALUE：IEND INFO, TYPE：JSON STRING]
	public static final int QUERY_FRIEND_INFO_REQ		= 10008;
	//update card request [VALUE：USERID, TYPE：LONG]
	public static final int UPDATE_CARD_INFO_REQ		= 10009;
	//query code request [VALUE：CARD INFO, TYPE：JSON STRING]
	public static final int QUERY_CARD_INFO_REQ			= 10010;
	//business request [VALUE：BSDATA, TYPE：BYTE]
	public static final int SEND_MSG_REQ				= 10011;
	public static final int EXTRA_LOGIN_REQ       	    = 10012;
	
	public static final int QUERY_FRIEND_REQ			= 10013;
	public static final int QUERY_MULTI_FRIEND_INFO_REQ	= 10014;
	public static final int ADD_ALIAS					= 10015;
	public static final int CHECK_PHONE_FRIEND		    = 10016;
	/*** NDC net protocol end*/
	
	
	
	//business type: short
	public static final short BS_MEETING	= 2012;
	
	
	//public static final short BS_SYNC_READ	= BS_MEETING;
	
	//business type: short
/*	public static final short BS_REQUEST_REQ_ADD_MEETING	= 10016;
	public static final short BS_REQUEST_REQ_ACCEPT_MEETING	= 10017;
	public static final short BS_REQUEST_REQ_REFUSE_MEETING	= 10018;
	public static final short BS_REQUEST_REQ_READ_MEETING	= 10019;
	public static final short BS_REQUEST_REQ_GET_MY_MEETING	= 10020;
	public static final short BS_REQUEST_REQ_GET_OTHERS_MEETING	= 10021;*/
	
	
	
	
	
	public final static int NOTIFY_CODE_INVITE_MEETING      = 300;
	
	
	public final static int APP_EVENT_REGISTER_RSP          = 10;
	public final static int APP_EVENT_LOGIN_RSP             = 11;
	public final static int APP_EVENT_UPLOAD_MY_CARD        = 12;
	public final static int APP_EVENT_ADD_FRIEND_RSP        = 13;
	public final static int APP_EVENT_DEL_FRIEND_RSP        = 14;
	public final static int APP_EVENT_LOGOUT_RSP            = 15;
	public final static int APP_EVENT_BS_RSP                = 16;
	public final static int APP_EVENT_QUERY_FRIEND_RSP      = 17;
	public final static int APP_EVENT_QUERY_USER_INFO_RSP   = 18;
	public final static int APP_EVENT_ADD_ALIAS_RSP         = 19;
	public final static int APP_EVENT_UPDATE_INFO_RSP       = 20;
	public final static int APP_EVENT_CHECK_PHONE_FRIEND_RSP       = 21;
	
	
	public static final int NETWORK_PLATFORM_EVENT_RETURN  = 1001;
	public static final int NETWORK_EVENT_NOTIFY           = 1002;
	public static final int NETWORK_EVENT_ERROR            = 1003;
	public static final int NETWOKR_EVENT_TIMEOUT          = 1004;
	public static final int NETWORK_BUSINESS_EVENT_RETURN  = 1005;
	

	
	public static final int LOCAL_MSG_ID_MEETING_ADD = 1000;
	public static final int LOCAL_MSG_ID_MEETING_CANCEL = 1001;
	public static final int LOCAL_MSG_ID_MEETING_QUERY_ALL = 1002;
	public static final int LOCAL_MSG_ID_MEETING_QUERY_MY_LIST = 1003;
	public static final int LOCAL_MSG_ID_MEETING_QUERY_OTHER_LIST = 1004;
	public static final int LOCAL_MSG_ID_MEETING_QUERY_DETAIL = 1005;
	public static final int LOCAL_MSG_ID_MEETING_REFUSE = 1006;
	public static final int LOCAL_MSG_ID_MEETING_ACCEPT = 1007;
	public static final int LOCAL_MSG_ID_MEETING_READ = 1008;
	public static final int LOCAL_MSG_ID_MEETING_APPEND_MEMBER = 1009;
	public static final int LOCAL_MSG_ID_ADD_DOC = 1010;
	public static final int LOCAL_MSG_ID_SUGGEST = 1011;
	
	public static final int NETWOKR_ERROR_BS_MEETING_TYPE    = 100000;
	
	

	public static final int LOCAL_MSG_ID_SYNC_ENTER = 1100;
	public static final int LOCAL_MSG_ID_SYNC_HANDSHAKE = 1101;
	public static final int LOCAL_MSG_ID_SYNC_NOTIFY_PAGE = 1102;
	public static final int LOCAL_MSG_ID_SYNC_ACTION = 1103;
	public static final int LOCAL_MSG_ID_SYNC_EXIT = 1104;
}

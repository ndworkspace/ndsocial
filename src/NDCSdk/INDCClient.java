package NDCSdk;

public interface INDCClient {
	//ע�᷵�أ��� VALUE��USERID�� TYPE��LONG��
	public static final String RT_CA_REGISTER_RESP 				= "on_user_retister";
	
	//��½���أ��� VALUE��USERID�� TYPE��LONG��
	public static final String RT_CA_LOGIN_RESP 				= "on_user_login";
	
	//�ǳ����أ��ۣ�
	public static final String RT_CA_LOGOUT_RESP				= "on_user_logout";
	
	//�����û����أ��ۣ�
	public static final String RT_CA_UPDATE_USER_INFO_RESP		= "on_update_user_info";
	
	//��ѯ�û����أ���VALUE: USERINFO, TYPE: JSON STRING��
	public static final String RT_CA_QUERY_USER_INFO_RESP		= "on_query_user_info";
	
	//���Ӻ��ѷ��أ���VALUE��USERID, TYPE��LONG��
	public static final String RT_CA_ADD_FRIEND_RESP			= "on_add_friend";
	
	//ɾ����ѷ��أ���VALUE��USERID, TYPE��LONG��
	public static final String RT_CA_DEL_FRIEND_RESP			= "on_del_friend";
	
	//��ѯ�����б?�أ���VALUE��USERID, TYPE��LONG��
	public static final String RT_CA_QEERY_FRIEND_RESP			= "on_query_friend";
	
	//��ѯ���ѷ��أ���VALUE��IEND INFO, TYPE��JSON STRING��
	public static final String RT_CA_QUERY_FRIEND_INFO_RESP		= "on_query_friend_info";
	
	//������Ƭ���أ���VALUE��USERID, TYPE��LONG��
	public static final String RT_CA_UPDATE_CARD_INFO_RESP		= "on_update_card_info";
	
	//��ѯ��Ƭ���أ���VALUE��CARD INFO, TYPE��JSON STRING��
	public static final String RT_CA_QUERY_CARD_INFO_RESP		= "on_query_card_info";	
	
	//ҵ����ʾ���أ���VALUE��BSDATA, TYPE��BYTE�ۣݣ�
	public static final String RT_CA_BS_REQUEST_RESP			= "on_business_response";	
	
	//ҵ����ʾ���أ��ۣ�
	public static final String RT_CA_ADD_ALIAS_RESP				= "on_add_alias";
	
	//ҵ����ʾ���أ��ۣ�
	public static final String RT_CA_DEL_ALIAS_RESP				= "on_del_alias";
	
	public static final String RT_CA_CHECK_PHONE_FRIEND_RSP		= "on_check_phone_friend";
	
	
	public static final int MSG_CONNECTION_BREAK_NOTIFY = 111111;

	public static final short BS_ACCOUNT_MSG = 100;

	public static final short BS_HEART_BEAT_MSG = 400;

	
	public boolean Init(String strIp, short nPort, INDCCallback cb, byte Version);
	public void Process();
	
	public boolean SendMsg(short nBSType, byte[] BSData);
	public boolean Register(String strName, String strPassword);
	public boolean Login(String strName, String strPassword);
	public boolean Login(String strName, String strPassword, short extType, byte[] extData);
	public boolean Logout();
	public boolean AddAlias(String AliasName, short nType);
	public boolean DelAlias(String AliasName);
	public boolean UpdateUserInfo(String info);
	public boolean QueryUserInfo();
	public boolean AddFriend(int lUid, int nType, String strGroupName);
	public boolean DelFriend(int lUid, int nDelType);
	public boolean QueryFriend(int lFreindVer);
	public boolean QueryFriendInfo(int lUid);
	public boolean UpdateBSCardInfo(String cardInfo);
	public boolean QueryBSCardInfo(int userId);
	public boolean checkPhoneFriend(byte[]data);
	public boolean sendHeartBeat(byte[]data);	
}

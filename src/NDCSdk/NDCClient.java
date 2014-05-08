package NDCSdk;


import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;

import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;

import android.util.Log;

public class NDCClient implements INDCClient {	
	public static final short MSG_CA_LOGIN_REQ 			= 101;
	public static final short MSG_CA_LOGIN_RESP 		= 102;
	public static final short MSG_CA_NOTIFY 			= 103;
	public static final short MSG_CA_NOTIFY_ACK 		= 104;
	public static final short MSG_CA_REGISTER_REQ 		= 105;
	public static final short MSG_CA_REGISTER_RESP 		= 106;
	
	public static final short MSG_CA_QUERY_REQ			= 111;
	public static final short MSG_CA_QUERY_RESP			= 112;
	public static final short MSG_CA_UPDATE_REQ			= 113;
	public static final short MSG_CA_UPDATE_RESP		= 114;
	public static final short MSG_CA_ADD_ALIAS_REQ		= 115;
	public static final short MSG_CA_ADD_ALIAS_RESP		= 116;
	public static final short MSG_CA_DEL_ALIAS_REQ		= 117;
	public static final short MSG_CA_DEL_ALIAS_RESP		= 118;
	
	public static final short MSG_CA_ADD_FRIEND_REQ		= 201;
	public static final short MSG_CA_ADD_FRIEND_RESP	= 202;	
	public static final short MSG_CA_FRIEND_REQUEST_REQ		= 203;
	public static final short MSG_CA_FRIEND_REQUEST_RESP	= 204;	
	public static final short MSG_CA_DEL_FRIEND_REQ		= 205;
	public static final short MSG_CA_DEL_FRIEND_RESP	= 206;	
	public static final short MSG_CA_QUERY_FRIEND_REQ	= 207;
	public static final short MSG_CA_QUERY_FRIEND_RESP	= 208;	
	public static final short MSG_CA_QUERY_FRIEND_INFO_REQ	= 209;
	public static final short MSG_CA_QUERY_FRIEND_INFO_RESP	= 210;
	
	
	public static final short MSG_CA_CARD_UPDATE_REQ	= 301;
	public static final short MSG_CA_CARD_UPDATE_RESP	= 302;
	public static final short MSG_CA_CARD_QUERY_REQ		= 303;
	public static final short MSG_CA_CARD_QUERY_RESP	= 304;
	
	
	public static final short MSG_CAB_REQ       		= 1001;
	public static final short MSG_CAB_RESP      		= 1002;
	public static final short MSG_CAB_NOTIFY    		= 1003;
	public static final short MSG_CAB_NOTIFY_ACK 		= 1004;
	
	private String mIp;
	private short  mPort;
	private INDCCallback mcb;	
	private byte mVersion;
	
	//账号信息	
	public  NDCUserInfo 	mUserInfo = new NDCUserInfo();
	
	//SOCKET 读句柄
	private Socket 			msocket = null;
	private InputStream 	mIn;
	private OutputStream 	mOut;
	
	byte[] inputByte = new byte[1024];
	
	//数据加解密对象
	private NDCCryptor mCryptor = new NDCCryptor();
	
	//网络消息缓冲
	private byte[] mRecvBuf = new byte[400*1024];
	private int mDataLen = 0;
		
	
	//消息工厂
	private MsgFactory mFactory = new MsgFactory();


	public boolean Init(String strIp, short nPort, INDCCallback cb, byte Version) {
		mIp 	= strIp;
		mPort 	= nPort;
		mcb 	= cb;
		mVersion = Version;
		
		try {
			msocket = Connect(mIp, mPort);			
			if (!msocket.isConnected())
			{
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}	


	public void Process() 
	{
		Receive();
	}
	
	//PRIVATE FUNCTION
	private Socket Connect(String ip, short port) throws IOException
	{
		if (msocket != null)
			msocket.close();
		
		msocket = new Socket(ip, port);
		
		mIn = msocket.getInputStream();
		mOut = msocket.getOutputStream();
		
		
		return msocket;
	}

	
	//发送数据给NDC平台
	public boolean SendMsg(short nBSType, byte[] BSData) 
	{
		/* for loopback test		
		 * if(nBSType == CAConstant.BS_SYNC_READ) {
			byte bNdcData[] = (mFactory.new MsgNDCRequest((int)mUserInfo.mUserId, CAConstant.BS_SYNC_READ, BSData, mVersion)).getBytes();
			Log.e("SendMsg","dataSize:"+ bNdcData.length);
			localTest(bNdcData.length, bNdcData);
			return true;
		}*/
		
		
		byte bNdcData[] = (mFactory.new MsgNDCRequest((int)mUserInfo.mUserId, nBSType, BSData, mVersion)).getBytes();

		try {
			byte data1[] = mCryptor.Encrypt(bNdcData, bNdcData.length);
			mOut.write(data1);
			
		} catch(SocketTimeoutException soTimeout) {
			Log.e("NDCClient","SendMsg SocketTimeoutException",soTimeout);
			CloundServer.getInstance().reConnect();
			return false;
		} catch(IOException e) {
			CloundServer.getInstance().reConnect();
			Log.e("NDCClient","SendMsg socket IOException",e);
			return false;
		} catch(Exception e) {
			Log.e("NDCClient","SendMsg Exception",e);
			return false;
		}
		return true;
	}
		
	private void Receive()
	{
		try 
		{
			if (msocket.isClosed())
			{
				Connect(mIp, mPort);
				return;
			}
			while (mIn.available() > 0) 
			{
				int length = mIn.read(inputByte, 0, inputByte.length);
				if (length > 0) {
					byte data[] = mCryptor.Decrypt(inputByte, length);
					System.arraycopy(data, 0, mRecvBuf, mDataLen, length);
					mDataLen += length;
					
					ByteConv.Offset nGetOffset = new ByteConv.Offset();
					ByteConv.Offset nOffset = new ByteConv.Offset();
					while(mDataLen > 12)
					{						
						nOffset.value = nGetOffset.value;						
						byte version[] = ByteConv.ReadByte(mRecvBuf, nOffset, 1);
						int msgSize = 0;
						short msgType = 0;
						int nUid = 0;
						boolean bHasPacket = false;
						
						switch(version[0])
						{
						case 1:
							ByteConv.ReadByte(mRecvBuf, nOffset, 1); //丢弃保留位
							msgSize = ByteConv.ReadShort(mRecvBuf, nOffset);
							
							if (msgSize <= mDataLen)
							{
								msgType = ByteConv.ReadShort(mRecvBuf, nOffset);
								nUid    = ByteConv.ReadInt(mRecvBuf, nOffset);
								bHasPacket = true;
							}													
							break;
						case 2:
							byte bsize[] = ByteConv.ReadByte(mRecvBuf, nOffset, 3);
							
							msgSize = ((bsize[0] & 0xFF) << 16) | ((bsize[1] & 0xFF) << 8) | bsize[2] & 0xFF;							
							
							if (msgSize <= mDataLen)
							{
								msgType = ByteConv.ReadShort(mRecvBuf, nOffset);
								nUid    = ByteConv.ReadInt(mRecvBuf, nOffset);
								bHasPacket = true;
							}							
							break;							
						case 3:
							break;
						case 4:
							break;
						default:
							break;						
						}
						
						if (bHasPacket)
						{
							nGetOffset.value += 12; //丢弃平台头，下面取业务数据				
							byte Data[] = ByteConv.getByte(mRecvBuf, nGetOffset, msgSize-12);							
							OnMessage(msgType, Data, msgSize-12);
							mDataLen -= msgSize;
						}
						else
						{
							break;
						}
					}
					
					if (mDataLen > 0)
					{
						System.arraycopy(mRecvBuf, nGetOffset.value, mRecvBuf, 0, mDataLen);
					}				
				}
				else
				{
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * for loopback test
	 */
	private void localTest(int datalen,byte[]data) {

		mDataLen = datalen;
		System.arraycopy(data, 0, mRecvBuf, 0, mDataLen);

		ByteConv.Offset nGetOffset = new ByteConv.Offset();
		ByteConv.Offset nOffset = new ByteConv.Offset();
		while (mDataLen > 12) {
			nOffset.value = nGetOffset.value;
			byte version[] = ByteConv.ReadByte(mRecvBuf, nOffset, 1);
			int msgSize = 0;
			short msgType = 0;
			int nUid = 0;
			boolean bHasPacket = false;

			switch(version[0])
			{
			case 1:
				ByteConv.ReadByte(mRecvBuf, nOffset, 1); //������λ
				
				msgSize = ByteConv.ReadShort(mRecvBuf, nOffset);
				
				if (msgSize <= mDataLen)
				{
					msgType = ByteConv.ReadShort(mRecvBuf, nOffset);
					nUid    = ByteConv.ReadInt(mRecvBuf, nOffset);
					bHasPacket = true;
				}													
				break;
			case 2:
				byte bsize[] = ByteConv.ReadByte(mRecvBuf, nOffset, 3);
				
				msgSize = ((bsize[0] & 0xFF) << 16) | ((bsize[1] & 0xFF) << 8) | bsize[2] & 0xFF;
				
				if (msgSize <= mDataLen)
				{
					msgType = ByteConv.ReadShort(mRecvBuf, nOffset);
					nUid    = ByteConv.ReadInt(mRecvBuf, nOffset);
					bHasPacket = true;
				}							
				break;							
			case 3:
				break;
			case 4:
				break;
			default:
				break;						
			}

			if (bHasPacket) {
				nGetOffset.value += 12; // ����ƽ̨ͷ������ȡҵ�����
				byte Data[] = ByteConv.getByte(mRecvBuf, nGetOffset,
						msgSize - 12);
				OnMessage(msgType, Data, msgSize - 12);
				mDataLen -= msgSize;
			} else {
				break;
			}
		}

		if (mDataLen > 0) {
			System.arraycopy(mRecvBuf, nGetOffset.value, mRecvBuf, 0, mDataLen);
		}			

	}
	
	private int OnMessage(short msgType, byte[] Data, int nLen)
	{

		switch (msgType)
		{
		case 100: 
			OnAccountMsg(Data, nLen);
			break;
		default:
			OnCABSResp(msgType, Data, nLen);
			break;
		}		
		
		return nLen;
	
	}
	
	
	private int OnAccountMsg(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		short nSize = ByteConv.getShort(Data, nOffset);
		short nMsgId = ByteConv.getShort(Data, nOffset);
		int nBodySize = (int) (nSize-4);
		byte Msg[] = new byte[nSize-4];
		System.arraycopy(Data, 4, Msg, 0, nBodySize);
		
		switch (nMsgId)
		{
		case MSG_CA_LOGIN_RESP:
			OnLogin(Msg, nBodySize);
			break;
		case MSG_CA_NOTIFY:
			OnCANotify(Msg, nBodySize);
			break;

		case NDCClient.MSG_CAB_NOTIFY:
			OnCABSNotify(Msg, nBodySize);
			break;
		case NDCClient.MSG_CA_REGISTER_RESP:
			OnRegister(Msg, nBodySize);
			break;
		case NDCClient.MSG_CA_UPDATE_RESP:
			OnUpdateUserInfo(Msg, nBodySize);
			break;
		case NDCClient.MSG_CA_QUERY_RESP:
			OnQueryUserInfo(Msg, nBodySize);
			break;			
		case NDCClient.MSG_CA_ADD_FRIEND_RESP:
			OnAddFriend(Msg, nBodySize);
			break;			
		case NDCClient.MSG_CA_DEL_FRIEND_RESP:
			OnDelFriend(Msg, nBodySize);
			break;
		case NDCClient.MSG_CA_QUERY_FRIEND_RESP:
			OnQueryFriend(Msg, nBodySize);
			break;
		case NDCClient.MSG_CA_QUERY_FRIEND_INFO_RESP:
			OnQueryFriendInfo(Msg, nBodySize);
			break;			
		case NDCClient.MSG_CA_CARD_UPDATE_RESP:
			OnUpdateBSCardInfo(Msg, nBodySize);
			break;			
		case NDCClient.MSG_CA_CARD_QUERY_RESP:
			OnQueryBSCardInfo(Msg, nBodySize);
			break;	
		case NDCClient.MSG_CA_ADD_ALIAS_RESP:
			OnAddAlias(Msg, nBodySize);
			break;			
		case NDCClient.MSG_CA_DEL_ALIAS_RESP:
			OnDelAlias(Msg, nBodySize);
			break;			
			
		default:
			break;				
		}	
		
		return nSize;
		
	}	
	
	
	//平台通知消息
	private void OnCANotify(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int nNotifyId = ByteConv.getInt(Data, nOffset);
		//暂时不处理
		
		//响应ACK
		try {
			MsgFactory.MsgNotiyfAck notifyAck = mFactory.new MsgNotiyfAck(nNotifyId);
			SendMsg((short)100, notifyAck.getBytes());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//平台返回业务数据
	private void OnCABSResp(short btype, byte[] Data, int nLen)
	{
//		ByteConv.Offset nOffset = new ByteConv.Offset();
//		int nResult = ByteConv.getInt(Data, nOffset);
//		short nBSType = ByteConv.getShort(Data, nOffset);
//		byte bBSData[] = ByteConv.getByte(Data, nOffset, nLen-nOffset.value);
//		if (nResult != 0)
//		{
//			String ErrorInfo = "业务类型：" + nBSType;
//			mcb.OnError(nResult, ErrorInfo);
//			return;
//		}
		
		mcb.OnMessage(btype, Data, nLen);
	}
	
	//业务通知数据
	private void OnCABSNotify(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int nNotifyId = ByteConv.getInt(Data, nOffset);
		short nBSType = ByteConv.getShort(Data, nOffset);
		byte BSData[]	= ByteConv.getByte(Data, nOffset, nLen-nOffset.value);
		mcb.OnNotify(nBSType, BSData);
		
		//响应ACK
//		try {
//			MsgFactory.MsgNotiyfAck notifyAck = mFactory.new MsgNotiyfAck(nNotifyId);
//			SendMsg(notifyAck.getBytes());
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
	}
	
	//账号注册
	public boolean Register(String strName, String strPassword)
	{
		if (strName == "") return false;
		
		MsgFactory.MsgRegister register = 
			mFactory.new MsgRegister(strName, strPassword);
		SendMsg((short)100, register.getBytes());
		return true;
	}
	
	private void OnRegister(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int lUserId = ByteConv.getInt(Data, nOffset);
		int  nResult = ByteConv.getInt(Data, nOffset);
		
		mcb.OnMessage(INDCClient.RT_CA_REGISTER_RESP, nResult, lUserId);
	}
	
	//登陆平台
	public boolean Login(String strName, String strPassword) {
		this.Login(strName, strPassword, (short)0, null);
		return true;
	}
	
	public boolean Login(String strName, String strPassword, short extType, byte[] extData)
	{
		MsgFactory.MsgLogin login = 
			mFactory.new MsgLogin(strName, strPassword, extType, extData);
		return SendMsg((short)100,login.getBytes());
	}
	
	//登陆平台结果
	private void OnLogin(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		mUserInfo.mUserId = ByteConv.getInt(Data, nOffset);
		int nResult = ByteConv.getInt(Data, nOffset);
				
		if (nResult == 0)
		{
			//认证成功
		}
		else
		{
			//认证失败
		}
		
		mcb.OnMessage(INDCClient.RT_CA_LOGIN_RESP, nResult, mUserInfo.mUserId);
	}

	//登出平台
	public boolean Logout() 
	{
		try
		{
			msocket.close();
			msocket = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mcb.OnMessage(INDCClient.RT_CA_LOGOUT_RESP, 0, "");
		return true;
	}
	
	//更新用户信息
	public boolean UpdateUserInfo(String info)
	{		
		MsgFactory.MsgUpdateUserInfo msgUpdateUserInfo = 
			mFactory.new MsgUpdateUserInfo( info);
		SendMsg((short)100, msgUpdateUserInfo.getBytes());
		return true;
	}
	
	private void OnUpdateUserInfo(byte[] Data, int nLen)
	{		
		Log.e("NDCClient","OnUpdateUserInfo");
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int  nResult = ByteConv.getInt(Data, nOffset);	
		mcb.OnMessage(INDCClient.RT_CA_UPDATE_USER_INFO_RESP, nResult, 0);
	}
	
	
	//查询用户信息
	public boolean QueryUserInfo()
	{		
		MsgFactory.MsgQueryUserInfo msgQueryUserInfo = 
			mFactory.new MsgQueryUserInfo();
		SendMsg((short)100, msgQueryUserInfo.getBytes());
		return true;
	}
	
	private void OnQueryUserInfo(byte[] Data, int nLen)
	{		
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int   	nResult = ByteConv.getInt(Data, nOffset);		
		String 	userInfo = ByteConv.getString(Data, nOffset, nLen-nOffset.value);
		
		mcb.OnMessage(INDCClient.RT_CA_QUERY_USER_INFO_RESP, nResult, userInfo);
	}

	/////////////////////////////////////////////////////////////////////////////
	//增加朋友
	public boolean AddFriend(int lUid, int nType, String strGroup)
	{
		if (strGroup == "") return false;
		
		MsgFactory.MsgAddFriend msgFriend = 
			mFactory.new MsgAddFriend(lUid, nType, strGroup);
		SendMsg((short)100, msgFriend.getBytes());
		return true;
	}
	
	private void OnAddFriend(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int  lUserId = ByteConv.getInt(Data, nOffset);
		int   nResult = ByteConv.getInt(Data, nOffset);
		
		mcb.OnMessage(INDCClient.RT_CA_ADD_FRIEND_RESP, nResult, lUserId);
	}
	
	//增加朋友
	public boolean DelFriend(int lUid, int nDelType)
	{
		MsgFactory.MsgDelFriend msgFriend = 
			mFactory.new MsgDelFriend(lUid, nDelType);
		SendMsg((short)100, msgFriend.getBytes());
		return true;
	}
		
	private void OnDelFriend(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int  lUserId = ByteConv.getInt(Data, nOffset); 
		int   nResult = ByteConv.getInt(Data, nOffset);
		
		mcb.OnMessage(INDCClient.RT_CA_DEL_FRIEND_RESP, nResult, lUserId);
	}
	
	//查询朋友列表
	public boolean QueryFriend(int lFreindVer)
	{
		MsgFactory.MsgQueryFriend msgFriend = 
			mFactory.new MsgQueryFriend(lFreindVer); 
		SendMsg((short)100, msgFriend.getBytes());
		return true;
	}
	
	private void OnQueryFriend(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();

		int nResult = ByteConv.getInt(Data, nOffset);
		int nVersion = ByteConv.getInt(Data, nOffset);
		String userInfo = ByteConv.getString(Data, nOffset, nLen-nOffset.value);
		
		mcb.OnMessage(INDCClient.RT_CA_QEERY_FRIEND_RESP, nResult, userInfo);
	}

	//查询朋友信息
	public boolean QueryFriendInfo(int lUid)
	{
		MsgFactory.MsgQueryFriendInfo friendInfo = 
			mFactory.new MsgQueryFriendInfo(lUid,0);
		SendMsg((short)100, friendInfo.getBytes());
		return true;
	}
	
	private void OnQueryFriendInfo(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		@SuppressWarnings("unused")
		int  lUserId = ByteConv.getInt(Data, nOffset);
		int   nResult = ByteConv.getInt(Data, nOffset);
		String 	userInfo = ByteConv.getString(Data, nOffset, nLen-nOffset.value);
		
		mcb.OnMessage(INDCClient.RT_CA_QUERY_FRIEND_INFO_RESP, nResult, userInfo);
	}
	
	///////////////////////////////////////////////////////////////
	//更新名片信息
	public boolean UpdateBSCardInfo(String cardInfo)
	{
		MsgFactory.MsgUpdateCardInfo msgCardInfo = 
			mFactory.new MsgUpdateCardInfo(cardInfo);
		SendMsg((short)100, msgCardInfo.getBytes());
		return true;
	}
	
	private void OnUpdateBSCardInfo(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int   nResult = ByteConv.getInt(Data, nOffset);
		
		mcb.OnMessage(INDCClient.RT_CA_UPDATE_CARD_INFO_RESP, nResult, 0);
	}
	
	//查询名片信息
	public boolean QueryBSCardInfo(int userId)
	{
		MsgFactory.MsgQueryBSCardInfo msgCardInfo = 
			mFactory.new MsgQueryBSCardInfo();
		SendMsg((short)100, msgCardInfo.getBytes());
		return true;
	}
	
	private void OnQueryBSCardInfo(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		@SuppressWarnings("unused")
		int   nResult = ByteConv.getInt(Data, nOffset);
		String 	dInfo = ByteConv.getString(Data, nOffset, nLen-nOffset.value);
		
		mcb.OnMessage(INDCClient.RT_CA_QUERY_CARD_INFO_RESP, nResult, dInfo);
	}
	
	public boolean AddAlias(String AliasName, short nType)
	{
		MsgFactory.MsgAddAlias msgAlias = 
			mFactory.new MsgAddAlias(mUserInfo.mUserId, nType, AliasName);
		SendMsg((short)100, msgAlias.getBytes());
		return true;
	}
	
	private void OnAddAlias(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int   nResult = ByteConv.getInt(Data, nOffset);
		
		mcb.OnMessage(INDCClient.RT_CA_ADD_ALIAS_RESP, nResult, 0);
	}
	
	public boolean DelAlias(String AliasName)
	{
		MsgFactory.MsgDelAlias msgAlias = 
				mFactory.new MsgDelAlias(mUserInfo.mUserId, AliasName);
		SendMsg((short)100, msgAlias.getBytes());
		return true;
	}
	
	private void OnDelAlias(byte[] Data, int nLen)
	{
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int   nResult = ByteConv.getInt(Data, nOffset);
		
		mcb.OnMessage(INDCClient.RT_CA_DEL_ALIAS_RESP, nResult, 0);
	}


	@Override
	public boolean checkPhoneFriend(byte[] data) {
		MsgFactory.MsgCheckPhoneFriend checkPhoneFriend = 
				mFactory.new MsgCheckPhoneFriend(data);
		SendMsg((short)100, checkPhoneFriend.getBytes());
		return true;
	}
	
	private void onCheckPhoneFriend(byte[] data, int nLen) {		
		ByteConv.Offset nOffset = new ByteConv.Offset();
		int   nResult = ByteConv.getInt(data, nOffset);
		String 	friendInfo = ByteConv.getString(data, nOffset, nLen-nOffset.value);
		//TODO change the return type
		mcb.OnMessage(INDCClient.RT_CA_CHECK_PHONE_FRIEND_RSP, nResult, friendInfo);
	}
	
}

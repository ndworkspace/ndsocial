package NDCSdk;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;


public class MsgFactory {
	private static final short PROTOCOL_VERSION = 1;	
	
	class MsgLogin {
		private ByteArrayOutputStream bin;

		public MsgLogin(String name, String password, short extType, byte[] extData)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = 104;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_LOGIN_REQ);			//type
				out.writeShort(MsgFactory.PROTOCOL_VERSION);	    //Э��汾��				
				
				byte bName[] = new byte[64];
				byte bPassword[] = new byte[32];
				System.arraycopy(name.getBytes(), 0, bName, 0, name.getBytes().length);
				System.arraycopy(password.getBytes(), 0, bPassword, 0, password.getBytes().length);
										
				out.write(bName);
				out.write(bPassword);
				out.writeShort(extType);
				
				if (extType != 0)
				{
					out.write(extData);
				}				
			}
			catch (IOException e)
			{
				
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	public class MsgNotiyfAck
	{
		private ByteArrayOutputStream bin;
		
		public MsgNotiyfAck(int NotifyId)
		{
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);
			try
			{
				Short nTotalLen = 8;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_NOTIFY_ACK);		//type
				out.writeInt(NotifyId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgNDCRequest
	{
		private ByteArrayOutputStream bin;
		MsgNDCRequest(int nUserId, int nBSType, byte[] BSData, byte version)
		{
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);
			
	//		byte version[] = new byte[1];
			//version[0]= 2;
			
			int nTotalLen = 0;			
			byte r[] = new byte[2];
			r[0] = 0;
			r[1] = 0;
			byte len[] = new byte[3];
						
			try
			{
				switch (version)
				{
				case 1:
					nTotalLen = (int)(12 + BSData.length);
					len[1] = (byte) (nTotalLen & 0x000000ff);
					len[0] = (byte) ((nTotalLen>>8) & 0x000000ff);
					
					out.write(version);								//version
					out.write(r, 0, 1);								//����
					out.write(len, 0, 2);							//size				
					out.writeShort(nBSType);		    			//type				
					out.writeInt(nUserId);							//�û�ID
					out.write(r, 0, 2);								//����
					out.write(BSData, 0, BSData.length);
					
					break;
				case 2:
					nTotalLen = (int)(12 + BSData.length);
					len[2] = (byte) (nTotalLen & 0x000000ff);
					len[1] = (byte) ((nTotalLen>>8) & 0x000000ff);
					len[0] = (byte) ((nTotalLen>>16) & 0x000000ff);
					
					out.write(version);								//version
					out.write(len, 0, 3);							//size				
					out.writeShort(nBSType);		    			//type				
					out.writeInt(nUserId);							//�û�ID
					out.write(r, 0, 2);								//����
					out.write(BSData, 0, BSData.length);					
					
					break;
				case 3:
//					nTotalLen = (int)(16 + BSData.length);
//					len[0] = (byte) (nTotalLen & 0x000000ff);
//					len[1] = (byte) ((nTotalLen>>8) & 0x000000ff);
//
//					
//					out.write(version);								//version
//					out.write(r, 0, 1);								//����
//					out.write(len, 0, 2);							//size				
//					out.writeShort(nBSType);		    			//type				
//					out.writeInt(nUserId);							//�û�ID
//					out.writeShort(0);								//����
//					out.write(BSData, 0, BSData.length);
					
					break;
				case 4:
					break;
				default:
					break;
				}			
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}

	class MsgRegister {
		private ByteArrayOutputStream bin;
		public MsgRegister(String name, String password)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				Short nTotalLen = 102;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_REGISTER_REQ);		//type
				out.writeShort(MsgFactory.PROTOCOL_VERSION);	    //Э��汾��
				
				byte bName[] 		= new byte[64];
				byte bPassword[] 	= new byte[32];
				
				System.arraycopy(name.getBytes(), 0, bName, 0, name.getBytes().length);				
				System.arraycopy(password.getBytes(), 0, bPassword, 0, password.getBytes().length);

				out.write(bName);
				out.write(bPassword);					
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgAddFriend {
		private ByteArrayOutputStream bin;
		public MsgAddFriend(int friendId, int nType, String GroupName)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = 74;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_ADD_FRIEND_REQ);		//type
				
				byte bName[] 		= new byte[64];				
				System.arraycopy(GroupName.getBytes(), 0, bName, 0, GroupName.getBytes().length);				

				out.writeInt(friendId);
				out.writeShort(nType);
				out.write(bName);
					
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgDelFriend {
		private ByteArrayOutputStream bin;
		public MsgDelFriend(int friendId, int nType)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = 10;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_DEL_FRIEND_REQ);		//type	

				out.writeShort(nType);
				out.writeInt(friendId);				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgQueryFriend {
			private ByteArrayOutputStream bin;
			public MsgQueryFriend(int lFriendVer)
			{			
				bin = new ByteArrayOutputStream();   
				DataOutputStream out = new DataOutputStream(bin);			
				
				try{
					short nTotalLen = 8;
					out.writeShort(nTotalLen);      						//size
					out.writeShort(NDCClient.MSG_CA_QUERY_FRIEND_REQ);		//type	
					out.writeInt(lFriendVer);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}		
			}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgQueryUserInfo {
		private ByteArrayOutputStream bin;
		public MsgQueryUserInfo()
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = 4;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_QUERY_REQ);		//type	
		
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	
	class MsgQueryFriendInfo {
		private ByteArrayOutputStream bin;
		public MsgQueryFriendInfo(int uid,int ver)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = 4;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_QUERY_FRIEND_INFO_REQ);		//type	
				out.writeInt(uid);
				out.writeInt(ver);
		
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgUpdateUserInfo {
		private ByteArrayOutputStream bin;
		public MsgUpdateUserInfo(String info)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = (short) (4 + info.getBytes().length);
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_UPDATE_REQ);			//type	
				out.write(info.getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgUpdateCardInfo {
		private ByteArrayOutputStream bin;
		public MsgUpdateCardInfo( String info)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = (short) (4 + info.getBytes().length);
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_CARD_UPDATE_REQ);	//type	
				out.write(info.getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
//	class MsgUpdateBSCardInfo {
//		private ByteArrayOutputStream bin;
//		public MsgUpdateBSCardInfo(long uId, String info)
//		{			
//			bin = new ByteArrayOutputStream();   
//			DataOutputStream out = new DataOutputStream(bin);			
//			
//			try{
//				short nTotalLen = (short) (12 + info.length());
//				out.writeShort(nTotalLen);      					//size
//				out.writeShort(NDCClient.MSG_CA_CARD_UPDATE_REQ);	//type	
//				out.writeLong(uId);
//				out.write(info.getBytes());
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}		
//		}
//		
//		public byte [] getBytes()
//		{
//			return bin.toByteArray();
//		}
//	}
	
	class MsgQueryBSCardInfo {
		private ByteArrayOutputStream bin;
		public MsgQueryBSCardInfo()
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);			
			
			try{
				short nTotalLen = 4;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_CARD_QUERY_RESP);	//type	
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	
	class MsgAddAlias {
		private ByteArrayOutputStream bin;
		public MsgAddAlias(int uId, short type, String alias)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);
			
			try{
				Short nTotalLen = 70;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_ADD_ALIAS_REQ);		//type	
				out.writeShort(type);
				
				byte bName[] = new byte[64];				
				System.arraycopy(alias.getBytes(), 0, bName, 0, alias.getBytes().length);
				out.write(bName);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	class MsgDelAlias {
		private ByteArrayOutputStream bin;
		public MsgDelAlias(int uId, String alias)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);
			
			try{
				short nTotalLen = 68;
				out.writeShort(nTotalLen);      					//size
				out.writeShort(NDCClient.MSG_CA_DEL_ALIAS_REQ);		//type	
			
				byte bName[] = new byte[64];				
				System.arraycopy(alias.getBytes(), 0, bName, 0, alias.getBytes().length);
				out.write(bName);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	
	class MsgCheckPhoneFriend {
		private ByteArrayOutputStream bin;
		public MsgCheckPhoneFriend(byte[]data)
		{			
			bin = new ByteArrayOutputStream();   
			DataOutputStream out = new DataOutputStream(bin);
			
			try{
				short nTotalLen = (short) (4 + data.length);
				out.writeShort(nTotalLen);
				out.writeShort(NDCClient.MSG_CA_PHONE_CHECK_REQ);		//type	
				out.write(data);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}		
		}
		
		public byte [] getBytes()
		{
			return bin.toByteArray();
		}
	}
	
	
}




package cn.nd.social.syncbrowsing.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.business.MeetingUtils;
import cn.nd.social.util.Utils;

public class SyncMsgFactory {
	
	public final static String ACTION_RECV_SYNC_ENTER = "cn.nd.social.sync-enter";
	
	public final static boolean ADD_JSON_HEADER = true;
	
	public static byte[] getSyncEnterMsg(String meetingId) {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();   
		DataOutputStream out = new DataOutputStream(bin);
		try {
			out.writeInt(CAConstant.LOCAL_MSG_ID_SYNC_ENTER);
			out.write(meetingId.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bin.toByteArray();
	}
	
	
	public static byte[] getSyncHandshakeMsg(int width,int height,int pageCount,int curPage) {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();   
		DataOutputStream out = new DataOutputStream(bin);
		try {
			out.writeInt(CAConstant.LOCAL_MSG_ID_SYNC_HANDSHAKE);
			out.writeInt(width);
			out.writeInt(height);
			out.writeInt(pageCount);
			out.writeInt(curPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bin.toByteArray();
	}
	
	
	public static byte[] getSyncNotifyPageMsg(int page,byte[]pageData,int width,int height,int pageCount) {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();   
		DataOutputStream out = new DataOutputStream(bin);
		try {
			out.writeInt(CAConstant.LOCAL_MSG_ID_SYNC_NOTIFY_PAGE);
			out.writeInt(pageCount);
			out.writeInt(width);
			out.writeInt(height);
			
			out.writeInt(page);
			out.writeInt(pageData.length);
			out.write(pageData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bin.toByteArray();
	}
	
	public static byte[] getSyncSendAction(int page,byte[]action) {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();   
		DataOutputStream out = new DataOutputStream(bin);
		try {
			out.writeInt(CAConstant.LOCAL_MSG_ID_SYNC_ACTION);
			out.writeInt(page);
			out.writeInt(action.length);
			out.write(action);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bin.toByteArray();
	}
	
	
	public static byte[] getSyncExitAction() {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();   
		DataOutputStream out = new DataOutputStream(bin);
		try {
			out.writeInt(CAConstant.LOCAL_MSG_ID_SYNC_EXIT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bin.toByteArray();
	}
	
	
	public static byte[] getSyncPacketHeader(String meetingId) {
		JSONObject jobj = new JSONObject();
		try {
			jobj.put(MeetingUtils.ACTION,MeetingUtils.MEETING_ACTION_TRANSFER_REQ);
			jobj.put(MeetingUtils.MEETING_KEY_BS_ACTION,"sync");
			jobj.put(MeetingUtils.MEETING_KEY_ROOMID, meetingId);			
			jobj.put(MeetingUtils.MEETING_KEY_SEQUENCE, String.valueOf(System.currentTimeMillis()));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String jstr = jobj.toString();
		return jstr.getBytes();
	}
	
	
	public static byte[] contactHeadAndContent(byte[]header,byte[]content) {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();   
		DataOutputStream out = new DataOutputStream(bin);
		try {
			//out.writeInt(header.length);
			out.write(header);
			out.write("\r\n".getBytes());
			out.writeInt(content.length);
			out.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bin.toByteArray();
	}
	
	
	public static void parseSyncPacket(byte []data,IClientNetMsgReceiver receiver) {
		ByteArrayInputStream bin = new ByteArrayInputStream(data);   
		DataInputStream in = new DataInputStream(bin);
		try {
			String roomid = "";
			if(SyncMsgFactory.ADD_JSON_HEADER) {
/*				int headerLen = in.readInt();
				byte[]header = new byte[headerLen];
				in.read(header, 0, headerLen);*/
				//String jHeaderStr = new String(header);
				
				String str = new String(data);
				
				try {
					JSONObject jobj = new JSONObject(str);
					roomid = jobj.getString(MeetingUtils.MEETING_KEY_ROOMID);
					int headLen = jobj.toString().getBytes().length;
					byte []head = new byte[headLen + "\r\n".length()];
					in.read(head);
					
				} catch (JSONException e) {
					e.printStackTrace();
					
				}
				int msgLen = in.readInt();
			}
			
			int msgType = in.readInt();
			if(msgType != CAConstant.LOCAL_MSG_ID_SYNC_ENTER
					&& msgType != CAConstant.LOCAL_MSG_ID_SYNC_NOTIFY_PAGE
					&& receiver == null) {
				return;
			}
			switch(msgType) {
			case CAConstant.LOCAL_MSG_ID_SYNC_ENTER: {
				//String meetingid = new String(in.readUTF());
				Intent intent = new Intent(ACTION_RECV_SYNC_ENTER);
				intent.putExtra("meetingid", roomid);
				Utils.getAppContext().sendBroadcast(intent);
			}
				
				break;
				
			case CAConstant.LOCAL_MSG_ID_SYNC_HANDSHAKE: {
				int width = in.readInt();
				int height = in.readInt();
				int count = in.readInt();
				int currpage = in.readInt();
				receiver.onRecvHandshake(0, count, currpage, width, height);
			}
				break;
				
			case CAConstant.LOCAL_MSG_ID_SYNC_NOTIFY_PAGE:{
				
				int pageCount = in.readInt();
				int width = in.readInt();
				int height = in.readInt();
				
				
				int page = in.readInt();
				int pageDataLen = in.readInt();
				
				byte []buffer = new byte[pageDataLen];
				in.read(buffer, 0, pageDataLen);
				if(receiver == null) {
					Intent intent = new Intent(ACTION_RECV_SYNC_ENTER);
					intent.putExtra("meetingid", roomid);
					
					intent.putExtra("hasextra", true);
					
					Bundle extras = new Bundle();
					extras.putInt("pagecount", pageCount);
					extras.putInt("width", width);
					extras.putInt("height", height);
					extras.putInt("page", page);
					extras.putByteArray("pagedata", buffer);
					
					intent.putExtras(extras);
					
					Utils.getAppContext().sendBroadcast(intent);
				} else {
					receiver.onRecvNotifyPage(page, buffer, 0);
				}
			}
				break;
				
			case CAConstant.LOCAL_MSG_ID_SYNC_ACTION: {
				int page = in.readInt();
				int dataLen = in.readInt();
				byte []buffer = new byte[dataLen];
				in.read(buffer, 0, dataLen);
				receiver.onRecvAction(page, 0, buffer);
			}
				break;
				
			case CAConstant.LOCAL_MSG_ID_SYNC_EXIT:
				receiver.onRecvExitSync();
				break;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}

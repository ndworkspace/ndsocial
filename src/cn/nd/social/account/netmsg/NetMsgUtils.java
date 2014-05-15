package cn.nd.social.account.netmsg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.business.MeetingUtils;

public class NetMsgUtils {
	
	public final static String JSON_SEP_STRING = "\r\n";
	
	
	private final static String SEQUENCE_SEP = "-";
	
	
	
	public final static String ACTION_SUGGEST = "777-suggest";
	
	public final static String ACTION_SUGGEST_RSP = "777-suggest-response";
	
	public static void sendFeedBack(String content, int type) {
		long uid = CloundServer.getInstance().getUserId();
		String seq = getSequceString(uid, null);
		JSONObject jobj = new JSONObject();
		try {
			jobj.put(MeetingUtils.ACTION, ACTION_SUGGEST);
			jobj.put("content", content);
			jobj.put("type", String.valueOf(type));
			jobj.put(MeetingUtils.MEETING_KEY_SEQUENCE, seq);
			String jstr = jobj.toString();
			//TODO:
			CloundServer.getInstance().sendMeetingMsg(jstr.getBytes(), CAConstant.LOCAL_MSG_ID_SUGGEST, seq);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	

	public static String getSequceString(long pre,String post) {
		if(post == null) {
			return String.valueOf(pre) + SEQUENCE_SEP 
					+ String.valueOf(System.currentTimeMillis());
		} else {
			return String.valueOf(pre) + SEQUENCE_SEP 
					+ String.valueOf(System.currentTimeMillis()) + post;
		}
		
	}
	
	

	

	public static byte[] concateHeadAndContent(byte[]header,byte[]content) {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();   
		DataOutputStream out = new DataOutputStream(bin);
		try {
			//out.writeInt(header.length);
			out.write(header);
			out.write(JSON_SEP_STRING.getBytes());
			out.writeInt(content.length);
			out.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bin.toByteArray();
	}
	
	

}

package cn.nd.social.account.business;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingUser;
import com.nd.voice.meetingroom.manager.User;

public class MeetingUtils {
	
	private final static String TAG = "MeetingUtils";
	/******************protocol start******************/
	
	public final static String ACTION = "action";	

	/** meeting action start */
	public final static String MEETING_ACTION_ADD_REQ ="meeting-add";
	public final static String MEETING_ACTION_QUERY_LIST_REQ ="meeting-list";
	public final static String MEETING_ACTION_QUERY_ROOM_REQ ="meeting-query";
	public final static String MEETING_ACTION_CANCEL_REQ ="meeting-cancel";
	
	public final static String MEETING_ACTION_ADD_MEMBER_REQ ="meeting-append-member";
	
	public final static String MEETING_ACTION_TRANSFER_REQ ="meeting-transfer";
	
	
	
	public final static String MEETING_ADD_RSP ="meeting-add-response";
	public final static String MEETING_QUERY_LIST_RSP ="meeting-list-response";
	public final static String MEETING_QUERY_ROOM_RSP ="meeting-query-response";
	public final static String MEETING_CANCEL_RSP ="meeting-cancel-response";
	
	public final static String MEETING_ADD_MEMBER_RSP ="meeting-append-member-response";
	
	
	public final static String MEETING_TRANSFER_RECV_NOTIFY ="meeting-transfer-receive";
	
	/**server -> client*/
	public final static String MEETING_INVITE_NOTIFY ="meeting-invite";
	
	public final static String MEETING_INVITE_NOTIFY_ACK ="meeting-invite-response";
	
	/**server -> client*/
	public final static String MEETING_INVITE_RESULT_NOTIFY ="meeting-invite-notify";
	
	/**server -> client*/
	public final static String MEETING_CANCEL_NOTIFY ="meeting-cancel-notify";
	
	/**server -> client*/
	public final static String MEETING_START_NOTIFY ="meeting-notify";
	
	
	/**server -> client*/
	public final static String MEETING_APPEND_MEMBER_NOTIFY ="meeting-append-member-notify";

	/** meeting action end*/

	
	
	public final static String MEETING_KEY_TITLE ="title";
	public final static String MEETING_KEY_RESULT ="result";
	public final static String MEETING_KEY_TIME ="time";
	public final static String MEETING_KEY_HOST_NAME ="hostname";
	public final static String MEETING_KEY_HOST_UID ="hostid";
	public final static String MEETING_KEY_IDLIST ="idlist";
	public final static String MEETING_KEY_ROOMID ="roomid";
	public final static String MEETING_KEY_CONTENT ="content";
	public final static String MEETING_KEY_INVITE_ACK ="response";
	public final static String MEETING_KEY_SEQUENCE ="seq";
	public final static String MEETING_KEY_NOTES ="notes";
	public final static String MEETING_KEY_MEETING_LIST ="meeting-list";
	public final static String MEETING_KEY_MEETING_CREATE_LIST ="meeting-create-list";
	public final static String MEETING_KEY_MEETING_INVITE_LIST ="meeting-invite-list";
	public final static String MEETING_KEY_MEETING_INFO ="meeting-info";
	public final static String MEETING_KEY_MEMBER_INFO ="member-info";
	
	public final static String MEETING_KEY_UID ="userid";
	
	public final static String MEETING_KEY_MEETING_INFO_ID ="member-id";
	public final static String MEETING_KEY_MEETING_INFO_NAME ="member-name";
	public final static String MEETING_KEY_MEMBER_INFO_STATUS ="status";
	
	
	public final static String MEETING_KEY_BS_ACTION ="bs-action";
	
	
	
	
	
	public final static String SEQUENCE_SEP = "-";
	public final static String MEETING_QUERY_TYPE_ALL ="all";
	public final static String MEETING_QUERY_TYPE_CREATE ="create";
	public final static String MEETING_QUERY_TYPE_INVITE ="invite";
	
	public final static String MEETING_INVITE_TYPE_ACCEPT ="accept";
	public final static String MEETING_INVITE_TYPE_REFUSE ="refuse";
	
	public final static String MEETING_NOTIFY_TYPE_START ="meeting-start";
	
	/******************protocol end******************/
	
	public static class BSMeetingNetworkError {
		public String info;
		public String sequence;
		public int  actionType;
	}
	
	public enum MeetingQueryType {
		ALL,
		CREATE,
		INVITE;
		
		public static String getTypeString(MeetingQueryType type) {
			String typeStr = null;
			switch(type) {
			case ALL:
				typeStr = MEETING_QUERY_TYPE_ALL;
				break;
			case CREATE:
				typeStr = MEETING_QUERY_TYPE_CREATE;
				break;
			case INVITE:
				typeStr = MEETING_QUERY_TYPE_INVITE;
				break;
			default:
				throw new RuntimeException("type not defined");

			}
			return typeStr;
		}
	}
	
	
	public enum InviteAckType {
		ACCEPT,
		REFUSE;
		
		public static String getTypeString(InviteAckType type) {
			String typeStr = null;
			switch(type) {
			case ACCEPT:
				typeStr = MEETING_INVITE_TYPE_ACCEPT;
				break;
			case REFUSE:
				typeStr = MEETING_INVITE_TYPE_REFUSE;
				break;
			default:
				throw new RuntimeException("type not defined");

			}
			return typeStr;
		}
	}
	
	public static abstract class MeetingRsp  {
		public String action;
		public MeetingRsp(String act) {
			this.action = act;
		}
	}
	
	public static class AddMeetingRsp extends MeetingRsp {
		public AddMeetingRsp(String act) {
			super(act);
		}
		public String result;
		public String title;
		public String roomId;
		public String seqence;
	}
	
	public static class AddMeetingMemberRsp extends MeetingRsp {
		public AddMeetingMemberRsp(String act) {
			super(act);
		}
		public String result;
		public String roomId;
		public String seqence;
	}
	
	public static class CancelMeetingRsp extends MeetingRsp {
		public CancelMeetingRsp(String act) {
			super(act);
		}
		public String result;
		public String title;
		public String seqence;
	}
	
	
	public static class InviteMeetingNotify extends MeetingRsp {
		public InviteMeetingNotify(String act) {
			super(act);
		}
		public String title;
		public String timeStr;
		public String hostname;
		public String hostuid;
		public String idlist;
		public String roomid;
	}
	
	public static class InviteResultNotify extends MeetingRsp {
		public InviteResultNotify(String act) {
			super(act);
		}
		public String title;
		public String ackType;
		public String hostid;
		public String roomid;
	}
	
	
	public static class StartMeetingNotify extends MeetingRsp {
		public StartMeetingNotify(String act) {
			super(act);
		}
		public String title;
		public String content;
		public String roomid;
	}
	
	public static class CancelMeetingNotify extends MeetingRsp {
		public CancelMeetingNotify(String act) {
			super(act);
		}
		public String title;
		public String roomid;
	}
	
	public static class QueryListRsp extends MeetingRsp{
		public QueryListRsp(String act) {
			super(act);
		}
		public String result;
		public String seqence;
		/**a json array string for meetinglist*/
		public String jArrayMeetingList;
		public String jArrCreateList;
		public String jArrInviteList;
	}
	
	public static class QueryRoomRsp extends MeetingRsp{
		public QueryRoomRsp(String act) {
			super(act);
		}
		public String result;
		public String seqence;
		public String roomid;
		public String meetingInfo;
		/**a json array string for memberInfo*/
		public String memberInfo;
	}
	
	public static class AppendMemberNotify extends MeetingRsp {
		public AppendMemberNotify(String act) {
			super(act);
		}
		public String title;
		public String roomid;
		public String idlist;		
	}
	
	public static class TransferMsg extends MeetingRsp {
		public TransferMsg(String act) {
			super(act);
		}
		public String msg;
		public byte[] rawData;
	}
	
	public static class MeetingMemberInfo {
		String uid;
		String name;
		String acceptStatus;
	}
	
	public static String removeSizeQuote(String str) {
		if(str== null) {
			return null;
		}
		if(str.startsWith("\"") && str.endsWith("\"") && str.length() > 2) {
			return str.substring(1,str.length());
		}
		return str;
	}
	
	public static List<MeetingMemberInfo> parseIdListWithStatus(String listStr) {
		List<MeetingMemberInfo> statusList = new ArrayList<MeetingMemberInfo>();
		String temp = listStr;
		while(temp.contains("{")) {
			int left = temp.indexOf("{");
			int right = temp.indexOf("}");
			String singleUser = temp.substring(left+1,right);
			String []userStatus = singleUser.split(",");
			MeetingMemberInfo participant = new MeetingMemberInfo();
			participant.uid = userStatus[0];
			participant.acceptStatus = removeSizeQuote(userStatus[1]);
			statusList.add(participant);
			
			temp = temp.substring(right);
		}
		return statusList;
	}
	
	public static List<User> getUserList(List<MeetingMemberInfo> list) {
		List<User> userList = new ArrayList<User>();
		for(int i =0;i<list.size();i++) {
			long userId = Long.parseLong(list.get(i).uid);			
			userList.add(CloundServer.getInstance().getFriendInfo(userId));
		}
		return userList;
	}
	
	
	private static User getUserInfo(long userid) {
		UserManager userMgr = new UserManager();
		User self = userMgr.getMyInfo();
		if(self != null && userid == self.getUserid()) {
			return userMgr.getMyInfo();
		}
		User user = userMgr.getUserInfoLocal(userid);
		if(user != null) {
			return user;
		}
		return null;
	}
	
	public static MeetingDetailEntity getMeetingDetail(String meetingInfo,String memberInfo) {
		MeetingDetailEntity entity = new MeetingDetailEntity();
		try {
			JSONObject jobj = new JSONObject(meetingInfo);
			String hostname = jobj.getString(MEETING_KEY_HOST_NAME);
			long hostid = Long.parseLong(jobj.getString(MEETING_KEY_HOST_UID));
			entity.setTitle(jobj.getString(MEETING_KEY_TITLE));
			entity.setMeetingTime(jobj.getString(MEETING_KEY_TIME));
			entity.setHostName(hostname);
			entity.setMeetingid(jobj.optString(MEETING_KEY_ROOMID));
			entity.setHostUserId(hostid);
			
			
			User hostUser = getUserInfo(hostid);
			if(hostUser == null) {
				hostUser = new User();
				hostUser.setUserName(hostname);
				hostUser.setUserid(hostid);
				hostUser.setNickName(hostname);
			}
			entity.setHostUserInfo(hostUser);
			
			List<MeetingUser> list = new ArrayList<MeetingUser>();
			if(memberInfo != null) {
				JSONArray jmemberArray = new JSONArray(memberInfo);				
				for(int i=0;i < jmemberArray.length();i++) {
					JSONObject jmemberInfo = (JSONObject)jmemberArray.get(i);
					String idStr = jmemberInfo.getString(MEETING_KEY_MEETING_INFO_ID);
					String name = jmemberInfo.getString(MEETING_KEY_MEETING_INFO_NAME);
					int status = Integer.parseInt(jmemberInfo.getString(MEETING_KEY_MEMBER_INFO_STATUS));
					int replyStatus = MeetingUser.REPLYSTATE_UNDEFINE;
					if(status == 1)	{	
						replyStatus = MeetingUser.REPLYSTATE_ACCEPTED;
					} else if(status == 2) {
						replyStatus = MeetingUser.REPLYSTATE_REJECTED;
					}
					MeetingUser user = new MeetingUser();
					user.setUserid(Long.parseLong(idStr));
					user.setUserName(name);
					user.setNickName(name);
					
					user.setReplyState(replyStatus);
					
					list.add(user);
				}
			}
			entity.setUsers(list);
			
		} catch (JSONException e) {			
			e.printStackTrace();
			return null;
		}
		return entity;
	}
	
	
	public static List<MeetingEntity>  getMyMeeting(String jArrayMeetingList) {
		ArrayList<MeetingEntity> entityList = new ArrayList<MeetingEntity>();
		try {
			JSONArray jarray = new JSONArray(jArrayMeetingList);
			for(int i=0;i<jarray.length();i++) {
				JSONObject jobj = (JSONObject)jarray.get(i);
				long uid = Long.parseLong(jobj.getString(MEETING_KEY_HOST_UID));
				if(uid == CloundServer.getInstance().getUserId() ) {
					MeetingEntity entity = new MeetingEntity();
					entity.setTitle(jobj.getString(MEETING_KEY_TITLE));
					entity.setMeetingTime(jobj.getString(MEETING_KEY_TIME));
					entity.setHostName(jobj.getString(MEETING_KEY_HOST_NAME));
					entity.setMeetingid(jobj.getString(MEETING_KEY_ROOMID));
					entity.setHostUserId(uid);
					entityList.add(entity);
				}
			}
			
		} catch (JSONException e) {			
			e.printStackTrace();
			return null;
		}
		return entityList;
	}
	
	public static List<MeetingEntity>  getInvitedMeeting(String jArrayMeetingList) {
		ArrayList<MeetingEntity> entityList = new ArrayList<MeetingEntity>();
		try {
			JSONArray jarray = new JSONArray(jArrayMeetingList);
			for(int i=0;i<jarray.length();i++) {
				JSONObject jobj = (JSONObject)jarray.get(i);
				long uid = Long.parseLong(jobj.getString(MEETING_KEY_HOST_UID));
				if(uid != CloundServer.getInstance().getUserId() ) {
					MeetingEntity entity = new MeetingEntity();
					entity.setTitle(jobj.getString(MEETING_KEY_TITLE));
					entity.setMeetingTime(jobj.getString(MEETING_KEY_TIME));
					entity.setHostName(jobj.getString(MEETING_KEY_HOST_NAME));
					entity.setMeetingid(jobj.getString(MEETING_KEY_ROOMID));
					entity.setHostUserId(uid);					
					entityList.add(entity);
				}
			}
			
		} catch (JSONException e) {			
			e.printStackTrace();
			return null;
		}
		return entityList;
	}
	
	
	public static List<MeetingEntity>  getMeetingList(String jArrayMeetingList) {
		ArrayList<MeetingEntity> entityList = new ArrayList<MeetingEntity>();
		try {
			JSONArray jarray = new JSONArray(jArrayMeetingList);
			for(int i=0;i<jarray.length();i++) {
				JSONObject jobj = (JSONObject)jarray.get(i);
				MeetingEntity entity = new MeetingEntity();
				entity.setTitle(jobj.getString(MEETING_KEY_TITLE));
				entity.setMeetingTime(jobj.getString(MEETING_KEY_TIME));
				entity.setHostName(jobj.getString(MEETING_KEY_HOST_NAME));
				entity.setMeetingid(jobj.getString(MEETING_KEY_ROOMID));
				entity.setHostUserId(Long.parseLong(jobj.getString(MEETING_KEY_HOST_UID)));
				entity.setReplyState(MeetingEntity.REPLYSTATE_ACCEPTED);
				entityList.add(entity);
			}
			
		} catch (JSONException e) {			
			e.printStackTrace();
			return null;
		}
		return entityList;
	}
	
	
	public static class ServerRspMsgFacotry {
		public static MeetingRsp getServerMsg(String action,String jstr) {
			MeetingRsp meetingRsp = null;
			try {
				JSONObject jobj = new JSONObject(jstr);
				if(action.equals(MEETING_ADD_RSP)) {
					AddMeetingRsp rsp = new AddMeetingRsp(action);
					
					rsp.result = jobj.getString(MEETING_KEY_RESULT);
					rsp.title = jobj.getString(MEETING_KEY_TITLE);
					rsp.roomId = jobj.getString(MEETING_KEY_ROOMID);
					rsp.seqence = jobj.getString(MEETING_KEY_SEQUENCE);
					
					meetingRsp = rsp;
					
				} else if(action.equals(MEETING_ADD_MEMBER_RSP)) {
					AddMeetingMemberRsp rsp = new AddMeetingMemberRsp(action);
					
					rsp.result = jobj.getString(MEETING_KEY_RESULT);
					rsp.roomId = jobj.getString(MEETING_KEY_ROOMID);
					rsp.seqence = jobj.getString(MEETING_KEY_SEQUENCE);
					
					meetingRsp = rsp;
					
				} else if(action.equals(MEETING_QUERY_LIST_RSP)) {
					QueryListRsp rsp = new QueryListRsp(action);
					rsp.result = jobj.getString(MEETING_KEY_RESULT);
					rsp.seqence = jobj.getString(MEETING_KEY_SEQUENCE);
					//rsp.jArrayMeetingList = jobj.getString(MEETING_KEY_MEETING_LIST);
					rsp.jArrCreateList = jobj.getString(MEETING_KEY_MEETING_CREATE_LIST);
					rsp.jArrInviteList = jobj.getString(MEETING_KEY_MEETING_INVITE_LIST);
					
					meetingRsp = rsp;
					
					Log.i(TAG,"QueryMeetingRsp,jArrCreateList:" + rsp.jArrCreateList);
					
				}  else if(action.equals(MEETING_QUERY_ROOM_RSP)) {
					QueryRoomRsp rsp = new QueryRoomRsp(action);
					rsp.result = jobj.getString(MEETING_KEY_RESULT);
					rsp.seqence = jobj.getString(MEETING_KEY_SEQUENCE);
					rsp.roomid = jobj.getString(MEETING_KEY_ROOMID);
					rsp.meetingInfo = jobj.getString(MEETING_KEY_MEETING_INFO);
					rsp.memberInfo = jobj.getString(MEETING_KEY_MEMBER_INFO);
					meetingRsp = rsp;
					
					Log.i(TAG,"QueryRoomRsp,meetingInfo:" + rsp.meetingInfo);
					
				} else if(action.equals(MEETING_CANCEL_RSP)) {
					CancelMeetingRsp rsp = new CancelMeetingRsp(action);
					rsp.result = jobj.getString(MEETING_KEY_RESULT);
					rsp.title = jobj.getString(MEETING_KEY_TITLE);
					rsp.seqence = jobj.getString(MEETING_KEY_SEQUENCE);
					
					meetingRsp = rsp;
					
				} else if(action.equals(MEETING_INVITE_NOTIFY)) {
					InviteMeetingNotify rsp = new InviteMeetingNotify(action);
					rsp.title = jobj.getString(MEETING_KEY_TITLE);
					rsp.timeStr = jobj.getString(MEETING_KEY_TIME);
					rsp.hostname = jobj.getString(MEETING_KEY_HOST_NAME);
					rsp.hostuid= jobj.getString(MEETING_KEY_HOST_UID);
					rsp.idlist = jobj.getString(MEETING_KEY_IDLIST);
					rsp.roomid = jobj.getString(MEETING_KEY_ROOMID);					
					
					meetingRsp = rsp;
					
				} else if(action.equals(MEETING_INVITE_RESULT_NOTIFY)){
					InviteResultNotify rsp = new InviteResultNotify(action);
					rsp.title = jobj.getString(MEETING_KEY_TITLE);
					rsp.ackType = jobj.getString(MEETING_KEY_INVITE_ACK);
					rsp.hostid = jobj.getString(MEETING_KEY_UID);
					rsp.roomid = jobj.getString(MEETING_KEY_ROOMID);
					
					meetingRsp = rsp;
					
				} else if(action.equals(MEETING_START_NOTIFY)){
					StartMeetingNotify rsp = new StartMeetingNotify(action);
					rsp.title = jobj.getString(MEETING_KEY_TITLE);
					rsp.content = jobj.getString(MEETING_KEY_CONTENT);
					rsp.roomid = jobj.getString(MEETING_KEY_ROOMID);
					
					meetingRsp = rsp;
					
				} else if(action.equals(MEETING_CANCEL_NOTIFY)) {
					CancelMeetingNotify rsp = new CancelMeetingNotify(action);
					rsp.title = jobj.getString(MEETING_KEY_TITLE);
					rsp.roomid = jobj.getString(MEETING_KEY_ROOMID);
					
					meetingRsp = rsp;
				} else if(action.equals(MEETING_APPEND_MEMBER_NOTIFY)) {
					AppendMemberNotify rsp = new AppendMemberNotify(action);
					rsp.title = jobj.getString(MEETING_KEY_TITLE);
					rsp.roomid = jobj.getString(MEETING_KEY_ROOMID);
					rsp.idlist = jobj.getString(MEETING_KEY_IDLIST);
					
					meetingRsp = rsp;					
				}
			} catch(JSONException e) {
				Log.e(TAG, "ServerRspMsgFacotry error, JSONException:",e);
				e.printStackTrace();
				return null;
			}
			
			return meetingRsp;
		}
	}
	


	public static ArrayList<Long> parseIdList(String idList) {
		ArrayList<Long> idListArray = new ArrayList<Long>();
		String list = idList;
		String groupStart = list.substring("idlist:[".length());
		String[] groups = groupStart.split(":");
		if (groups.length < 2) {
			return idListArray;
		}
		// groups[1] like [23,24,25]]]

		String idstr = groups[1].substring(1, groups[1].indexOf("]"));
		String[] idArray = idstr.split(",");
		for (String id : idArray) {
			idListArray.add(Long.parseLong(id));
		}
		return idListArray;
	}

	public static String constructIdList(long[] idList) {
		StringBuilder strBuild = new StringBuilder("");
		
		strBuild.append("[");
		for (int i = 0; i < idList.length; i++) {
			if (i != idList.length - 1) {
				strBuild.append(idList[i]);
				strBuild.append(",");
			} else {
				strBuild.append(idList[i]);
			}
		}
		strBuild.append("]");
		// idlist end

		return strBuild.toString();
	}

	
	
	private static String buildJsonLikePairs(String key,String value) {
		String pairKey = JSONObject.quote(key);
		String pairValue = JSONObject.quote(value);
		return pairKey+":" + pairValue;
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
	
	public static String constructInvitation(String title,  String timeStr,
						long hostid, String hostName, String sequence,long[] idList) {
		StringBuilder strBuild = new StringBuilder("{");
		
		strBuild.append(buildJsonLikePairs(ACTION,MEETING_ACTION_ADD_REQ));
		strBuild.append(",");
		// title
		strBuild.append(buildJsonLikePairs(MEETING_KEY_TITLE,title));
		strBuild.append(",");

		// timestamp
		strBuild.append(buildJsonLikePairs(MEETING_KEY_TIME,timeStr));
		strBuild.append(",");

		// hostName
		strBuild.append(buildJsonLikePairs(MEETING_KEY_HOST_NAME,hostName));
		strBuild.append(",");

		// hostuid
		strBuild.append(buildJsonLikePairs(MEETING_KEY_HOST_UID,String.valueOf(hostid)));
		strBuild.append(",");

		// idList
		strBuild.append(buildJsonLikePairs(MEETING_KEY_IDLIST,constructIdList(idList)));
		strBuild.append(",");
		
		//operation sequence
		if(sequence == null || sequence.equals("") ) {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,getSequceString(hostid,"-add")));
		} else {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,sequence));
		}
	
		strBuild.append("}");
		String jstr = strBuild.toString();
		Log.e(TAG, "addmeeting:" + jstr);
		return jstr;
	}
	
	
	public static String constructAddMeetingMember(String meetingId,
			long hostid, String hostName, String sequence, long[] idList) {
		StringBuilder strBuild = new StringBuilder("{");

		strBuild.append(buildJsonLikePairs(ACTION, MEETING_ACTION_ADD_MEMBER_REQ));
		strBuild.append(",");
		strBuild.append(buildJsonLikePairs(MEETING_KEY_ROOMID, meetingId));
		strBuild.append(",");



		// idList
		strBuild.append(buildJsonLikePairs(MEETING_KEY_IDLIST,
				constructIdList(idList)));
		strBuild.append(",");

		// operation sequence
		if (sequence == null || sequence.equals("")) {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,
					getSequceString(hostid, "-add")));
		} else {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE, sequence));
		}

		strBuild.append("}");
		String jstr = strBuild.toString();
		Log.i(TAG, "addmeeting:" + jstr);
		return jstr;
	}
	
	public static String constructCancelMeeting(String roomid,String title,long hostid,String sequence) {
		StringBuilder strBuild = new StringBuilder("{");
		
		strBuild.append(buildJsonLikePairs(ACTION,MEETING_ACTION_CANCEL_REQ));
		strBuild.append(",");
		
		// title
		strBuild.append(buildJsonLikePairs(MEETING_KEY_TITLE,title));
		strBuild.append(",");
		
		//roomid
		strBuild.append(buildJsonLikePairs(MEETING_KEY_ROOMID,roomid));
		strBuild.append(",");
	
		//operation sequence
		if(sequence == null || sequence.equals("") ) {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,getSequceString(hostid,"-cancel")));
		} else {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,sequence));
		}

		strBuild.append("}");
		String jstr = strBuild.toString();
		Log.i(TAG, "cancelmeeting:" + jstr);
		return jstr;
	}
	
	public static String constructQueryMeeting(MeetingQueryType queryType,long hostid,String sequence) {
		StringBuilder strBuild = new StringBuilder("{");
		
		strBuild.append(buildJsonLikePairs(ACTION,MEETING_ACTION_QUERY_LIST_REQ));
		strBuild.append(",");
		
		//query type
		strBuild.append(buildJsonLikePairs(MEETING_KEY_CONTENT,
												MeetingQueryType.getTypeString(queryType)));
		strBuild.append(",");
		
	
		//operation sequence
		if(sequence == null || sequence.equals("") ) {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,getSequceString(hostid,"-listquery")));
		} else {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,sequence));
		}

		strBuild.append("}");
		String jstr = strBuild.toString();
		Log.i(TAG, "QueryMeeting:" + jstr);
		return jstr;
	}
	
	public static String constructQueryMeetingByRoomId(String roomId,long hostid,String sequence) {
		StringBuilder strBuild = new StringBuilder("{");
		
		strBuild.append(buildJsonLikePairs(ACTION,MEETING_ACTION_QUERY_ROOM_REQ));
		strBuild.append(",");
		
		//query type
		strBuild.append(buildJsonLikePairs(MEETING_KEY_ROOMID,roomId));
		strBuild.append(",");
		
	
		//operation sequence
		if(sequence == null || sequence.equals("") ) {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,getSequceString(hostid,"-detailquery")));
		} else {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_SEQUENCE,sequence));
		}

		strBuild.append("}");
		String jstr = strBuild.toString();
		Log.i(TAG, "query detail:" + jstr);
		return jstr;
	}
	
	
	public static String constructInviteNotifyAck(InviteAckType ackType,String title,
			String roomId,String notes) {
		StringBuilder strBuild = new StringBuilder("{");
		
		strBuild.append(buildJsonLikePairs(ACTION,MEETING_INVITE_NOTIFY_ACK));
		strBuild.append(",");
		
		//title
		strBuild.append(buildJsonLikePairs(MEETING_KEY_TITLE,title));
		strBuild.append(",");
		
		//accept or refuse
		strBuild.append(buildJsonLikePairs(MEETING_KEY_INVITE_ACK,InviteAckType.getTypeString(ackType)));
		strBuild.append(",");
		
		//roomid
		strBuild.append(buildJsonLikePairs(MEETING_KEY_ROOMID,roomId));
		strBuild.append(",");
		
		//notes(reason)
		if(notes != null && !notes.equals("")) {
			strBuild.append(buildJsonLikePairs(MEETING_KEY_NOTES,notes));
		}
		
		strBuild.append("}");
		String jstr = strBuild.toString();
		Log.i(TAG, "invite ack:" + jstr);
		return jstr;
	}
	
	
	public static MeetingRsp parseBSMeeting(byte []data) {
		MeetingRsp rsp = null;
		String jstr = new String(data);		
		try {
			JSONObject jobj = new JSONObject(jstr);
			String action = jobj.getString(ACTION);
			if(action.equals(MEETING_TRANSFER_RECV_NOTIFY)) {
				TransferMsg transMsg = new TransferMsg(MEETING_TRANSFER_RECV_NOTIFY);
				transMsg.rawData = data;
				return transMsg;
			}
			rsp = ServerRspMsgFacotry.getServerMsg(action,jstr);
		} catch (JSONException e) {
			e.printStackTrace();
			//TODO: other kind of msg
			
		}
		return rsp;		
	}
	
	
	
	public static String removeSideBracket(String str) {
		if(str == null || str.equals("")) {
			return str;
		}
		
		if(str.startsWith("[")) {
			str = str.substring(1);
		}
		if(str.endsWith("]")) {
			str = str.substring(0,str.length() -1);
		}
		return str;
	}
	
}

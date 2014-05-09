package com.nd.voice;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cn.nd.social.util.LogToFile;

import com.nd.voice.meetingroom.manager.User;

public class VoiceEndpoint implements VoiceSDKCallback {	
	public interface ConferenceCallback {
		public void onSetUid(long uid, boolean success);
		public void onDisconnect();
		public void onDebug(String d);
		public void onJoinConference(boolean success);
		public void onLeaveConference();
		public void onConferenceMemberEnter(long uid);
		public void onConferenceMemberLeave(long uid);
		public void onConferenceMediaOption(int cmd, int value);
		public void onConferenceAudioEnergy(long uid, int energy);
	}
	
	//Inner network
	//private static final String VOICE_SERVER = "192.168.19.57";
	//public network
	private static final String VOICE_SERVER ="voice.91.com";//"192.168.213.35"
//	private static final short VOICE_PORT = 9601;
	private static final short VOICE_PORT = 29601;
	private static final int VE_SETUID = 1000;
	private static final int VE_DISCONN = 1001;
	private static final int VE_DEBUG = 1002;
	
	private static final int VE_CONF_JOIN = 2000;
	private static final int VE_CONF_LEAVE = 2001;
	private static final int VE_CONF_MENBER_JOIN = 2002;
	private static final int VE_CONF_MENBER_LEAVE = 2003;
	private static final int VE_CONF_MEDIA_OPTION = 2004;
	private static final int VE_CONF_AUDIO_ENERGY = 2005;
	
	private static VoiceEndpoint instance_;

	private Handler handler_ = null;
	private VoiceAPI voiceAPI_ = null;
	private ConferenceCallback  conf_callback_ = null;
	private Map<Short,String> idMap;

	private class VoiceEventArg
	{
		String conf;
		long uid;
		int arg1;
		int arg2;
	}

	
	public static VoiceEndpoint instance() {
		if(instance_ == null) {
			instance_ = new VoiceEndpoint();
			instance_.init();
		}
		return instance_;
	}
	
	public static VoiceAPI api() {
		return instance().voiceAPI_;
	}
	
	public static void setUID(long userid) {
		instance().voiceAPI_.authorize(User.getMeetingUserId(userid), "", "");
	}
	
	public static void join(Context ctx, String conf, ConferenceCallback callback) {
		instance_.conf_callback_ = callback;
		instance_.voiceAPI_.join(ctx, conf);
	}
	
	public static void leave(String conf) {
//		instance_.conf_callback_ = null;
		instance_.voiceAPI_.leave(conf);
	}
	
	private int init() {
		idMap = new HashMap<Short,String>();
        voiceAPI_ = new VoiceAPI();       
        voiceAPI_.load();
        
        handler_ = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		switch(msg.what) {
        		case VE_SETUID:
        			if(conf_callback_ != null) {    
        				Log.e("setuid", msg.obj.toString());
        				if (msg.obj instanceof Long) {
							Long id = (Long)msg.obj;
							conf_callback_.onSetUid(id, true);
						}else{
							VoiceEventArg arg = (VoiceEventArg)msg.obj;        				
	        				conf_callback_.onSetUid(arg.uid, arg.arg1 != 0);
						}
        				
        			}
        			break;
        		case VE_DISCONN:
        			if(conf_callback_ != null) {        				
        				VoiceEventArg arg = (VoiceEventArg)msg.obj;
        				conf_callback_.onDisconnect();
        			}
        			break;
        		case VE_DEBUG:
        			if(conf_callback_ != null) {        			
        				conf_callback_.onDebug(msg.obj.toString());
        			}
        			break;
        		case VE_CONF_JOIN:
        			if(conf_callback_ != null) {        				
        				VoiceEventArg arg = (VoiceEventArg)msg.obj;
        				conf_callback_.onJoinConference(arg.arg1 != 0);
        			}
        			break;
        		case VE_CONF_LEAVE:
        			if(conf_callback_ != null) {
        				VoiceEventArg arg = (VoiceEventArg)msg.obj;
        				conf_callback_.onLeaveConference();
        			}
        			break;
        		case VE_CONF_MENBER_JOIN:
        			if(conf_callback_ != null) {
        				VoiceEventArg arg = (VoiceEventArg)msg.obj;
        				conf_callback_.onConferenceMemberEnter(arg.uid);
        			}
        			break;
        		case VE_CONF_MENBER_LEAVE:
        			if (conf_callback_ != null) {
        				VoiceEventArg arg = (VoiceEventArg)msg.obj;
        				conf_callback_.onConferenceMemberLeave(arg.uid);
					}
        		case VE_CONF_MEDIA_OPTION:
        			if(conf_callback_ != null) {
        				VoiceEventArg arg = (VoiceEventArg)msg.obj;
        				conf_callback_.onConferenceMediaOption(arg.arg1, arg.arg2);
        			}
        			break;
        		case VE_CONF_AUDIO_ENERGY:
        			if(conf_callback_ != null) {
        				VoiceEventArg arg = (VoiceEventArg)msg.obj;
        				conf_callback_.onConferenceAudioEnergy(arg.uid,	arg.arg1);
        			}
        			break;

        		}
        	}
        };
        
        //init voice api
        voiceAPI_.init(VOICE_SERVER, VOICE_PORT, this);
        
		return 0;
	}
	
	public int onAuthorize(String uid, int retcode){
		long userid = User.getUserIdByUid(uid);
		return onSetUid(userid, retcode==0);
	}
	
	public int onSetUid(long userid, boolean success) {		
		Message msg = handler_.obtainMessage();
		msg.what = VE_SETUID;
		msg.obj = userid;
		msg.arg1 = success ? 1 : 0;
		this.handler_.sendMessage(msg);
		return 0;
	}

	public int onDisconnect() {
		Message msg = handler_.obtainMessage();
		msg.what = VE_DISCONN;
		this.handler_.sendMessage(msg);
		return 0;
	}
	
	public int onJoinConference(String conf, int retcode){
		if(retcode != 0) {
			LogToFile.e("VoiceEndPoint","onJoinConference error,retcode:" + retcode);
		}
		return onJoinConference(conf,retcode==0);
	}

	public int onJoinConference(String conf,boolean success) {	
		Message msg = handler_.obtainMessage();
		msg.what = VE_CONF_JOIN;
		VoiceEventArg arg = new VoiceEventArg();
		arg.conf = conf;
		arg.arg1 = success ? 1 : 0;;
		msg.obj = arg;
		this.handler_.sendMessage(msg);
		return 0;
	}

	public int onLeaveConference(String conf, int retcode){
		return onLeaveConference(conf);
	}
	
	public int onLeaveConference(String conf) {
		Message msg = handler_.obtainMessage();
		msg.what = VE_CONF_LEAVE;
		VoiceEventArg arg = new VoiceEventArg();
		arg.conf = conf;
		msg.obj = arg;
		this.handler_.sendMessage(msg);
		return 0;
	}
	
	public int onConferenceMemberEnter(String conf, String uid, short member_id){
		idMap.put(member_id, uid);
		return onConferenceMemberEnter(conf,User.getUserIdByUid(uid));
	}

	public int onConferenceMemberEnter(String conf, long uid) {
		Message msg = handler_.obtainMessage();
		msg.what = VE_CONF_MENBER_JOIN;
		VoiceEventArg arg = new VoiceEventArg();
		arg.conf = conf;
		arg.uid = uid;
		msg.obj = arg;
		this.handler_.sendMessage(msg);
		return 0;
	}
	
	public int onConferenceMemberLeave(String conf, short member_id){
		String uid = idMap.get(member_id);
		idMap.remove(member_id);
		return onConferenceMemberLeave(conf,User.getUserIdByUid(uid));
	}

	public int onConferenceMemberLeave(String conf, long uid) {
		Message msg = handler_.obtainMessage();
		msg.what = VE_CONF_MENBER_LEAVE;
		VoiceEventArg arg = new VoiceEventArg();
		arg.conf = conf;
		arg.uid = uid;
		msg.obj = arg;
		this.handler_.sendMessage(msg);
		return 0;
	}
	
	public int onConferenceMediaOption(String conf, int cmd, int value) {
		Message msg = handler_.obtainMessage();
		msg.what = VE_CONF_MEDIA_OPTION;
		VoiceEventArg arg = new VoiceEventArg();
		arg.conf = conf;
		arg.arg1 = cmd;
		arg.arg2 = value;
		msg.obj = arg;
		this.handler_.sendMessage(msg);
		return 0;
	}

	public int onConferenceAudioEnergy(String conf, short member_id, int energy){
		String uid = idMap.get(member_id);
		if(uid == null){
			return 0;
		}
		return onConferenceAudioEnergy(conf,User.getUserIdByUid(uid),energy);
	}
	
	public int onConferenceAudioEnergy(String conf, long uid, int energy) {
		Message msg = handler_.obtainMessage();
		msg.what = VE_CONF_AUDIO_ENERGY;
		VoiceEventArg arg = new VoiceEventArg();
		arg.conf = conf;
		arg.uid = uid;
		arg.arg1 = energy;
		msg.obj = arg;
		this.handler_.sendMessage(msg);
		return 0;
	}

	public int onDebug(String d) {
		Message msg = handler_.obtainMessage();
		msg.what = VE_DEBUG;
		msg.obj = d;
		this.handler_.sendMessage(msg);
		return 0;
	}

	public int onRecordStop(int error, ByteBuffer buf) {
		return 0;
	}

	public int onPlayStop(int error) {
		return 0;
	}
}

package com.nd.voice;

import java.nio.ByteBuffer;


public class VoiceAPI {
	//media option command
	//volume value 0~255
	//static final int SET_SPEAKER_VOLUME = 0;
	//static final int GET_SPEAKER_VOLUME = 10;

	//mute value: 0 cancel mute, 1 set mute
	static final int SET_PLAYOUT_MUTE = 2;
	static final int GET_PLAYOUT_MUTE = 12;
	static final int SET_RECORDING_MUTE = 3;
	static final int GET_RECORDING_MUTE = 13;

	//audio gain with percent, 0~500
	//static final int SET_GAIN = 21;
	//static final int GET_GAIN = 22;
	
	//loudspeaker 0 off, 1 on, default on
	static final int SET_LOUDSPEAKER = 23;
	static final int GET_LOUDSPEAKER = 24;

	//voice change
	static final int SET_VOICE_CHANGE = 25;
	
	static final int VOICE_CHANGE_IDLE = 0;//
	static final int VOICE_CHANGE_MAN_TO_WOMAN = 1;
	static final int VOICE_CHANGE_MAN_TO_ORC = 2;
	static final int VOICE_CHANGE_WOMAN_TO_MAN = 3;
	static final int VOICE_CHANGE_WOMAN_TO_ELE = 4;
	static final int VOICE_CHANGE_INTERPHONE = 5;
	
	public void load() {
		String a = "";
		a.getBytes();
		System.loadLibrary("voiceAPI");
	}

	public native int init(String host, short port, VoiceSDKCallback callback);
	public native int fini();
	
	public native int authorize(String uid, String session_id, String session_key );
	public native int join(Object context, String conf);
	public native int leave(String conf);
	public native int setKeypressMode(String conf, boolean setup);
	public native int notifyKeydown(String conf, boolean keydown);
	public native int mediaOption(String conf, int cmd, int value);
	
	public native int recordStart(Object context);
	public native int recordStop();
	
	//should use direct ByteBuffer
	public native int playStart(Object context, boolean loudspeaker, ByteBuffer buf);
	public native int playStop();
	
//	public int setSpeakerVolume(String conf, int value) {
//		return mediaOption(conf, SET_SPEAKER_VOLUME, value);
//	}
	
//	public int getSpeakerVolume(String conf) {
//		return mediaOption(conf, GET_SPEAKER_VOLUME, 0);
//	}

	public int setPlayoutMute(String conf, boolean mute) {
		return mediaOption(conf, SET_PLAYOUT_MUTE, mute ? 1 : 0);
	}
	
	public int getPlayoutMute(String conf, boolean mute) {
		return mediaOption(conf, GET_PLAYOUT_MUTE, mute ? 1 : 0);
	}
	
	public int setRecordingMute(String conf, boolean mute) {		
		return mediaOption(conf, SET_RECORDING_MUTE, mute ? 1 : 0);
	}
	
	public int getRecordingMute(String conf, boolean mute) {		
		return mediaOption(conf, GET_RECORDING_MUTE, mute ? 1 : 0);
	}
	
//	public int setAudioGain(String conf, int value) {		
//		return mediaOption(conf, SET_GAIN, value);
//	}
	
//	public int getAudioGain(String conf, int value) {		
//		return mediaOption(conf, GET_GAIN, value);
//	}
	
	public int setLoudspeaker(String conf, boolean enable) {		
		return mediaOption(conf, SET_LOUDSPEAKER, enable ? 1 : 0);
	}
	
	public int getLoudspeaker(String conf) {		
		return mediaOption(conf, GET_LOUDSPEAKER, 0);
	}
	
	public int voiceChange(String conf, int value) {		
		return mediaOption(conf, SET_VOICE_CHANGE, value);
	}
}

package com.nd.voice;

import java.nio.ByteBuffer;

public interface VoiceSDKCallback {
	public int onAuthorize(String uid, int retcode);
	public int onDebug(String d);
	public int onJoinConference(String conf, int retcode);
	public int onLeaveConference(String conf, int retcode);
	public int onConferenceMemberEnter(String conf, String uid, short member_id);
	public int onConferenceMemberLeave(String conf, short member_id);
	public int onConferenceMediaOption(String conf, int cmd, int value);
	public int onConferenceAudioEnergy(String conf, short member_id, int energy);
	public int onRecordStop(int error, ByteBuffer buf);
	public int onPlayStop(int error);
}

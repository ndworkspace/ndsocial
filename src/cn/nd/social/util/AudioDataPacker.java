package cn.nd.social.util;

import java.io.File;
import java.io.IOException;

import com.example.ofdmtransport.Modulation;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;


public abstract class AudioDataPacker {
	// type is from one to nine
	private int mType = 0;
	public static final int TYPE_INVALID = -1;
	public static final int TYPE_CARD_STRING = 1;
	public static final int TYPE_WIFI_PRIVATE_SHARE = 2;
	public static final int TYPE_WIFI_SYNC_READ = 3;
	public static final int TYPE_WIFI_CARDS_SHARE = 4;
	private final static String HEADER = "h-";
	private final static String TAIL = "-t";

	public AudioDataPacker(int type) {
		if (0 < type && type < 10)
			mType = type;
		else
			throw new RuntimeException("type should be in the range of 1 to 9");
	}

	public String packAudioData(Object obj) {
		String str = (String)obj;
		StringBuilder strBuilder = new StringBuilder(HEADER
				+ String.valueOf(mType) + ":");
		strBuilder.append(str);
		strBuilder.append(TAIL);
		return strBuilder.toString();
	}

	public final String unPackAudioData(String data) {
		String rawData = data.substring(data.indexOf(":") + 1, data.length()
				- TAIL.length());
		return rawData;
	}

	public final static int getType(String data) {
		try {
			String typeStr = data.substring(HEADER.length(), data.indexOf(":"));
			return Integer.valueOf(typeStr);
		} catch (Exception e) {
			return TYPE_INVALID;
		}
	}
	
	
	public static void playModulationAck() {
		if(Modulation.MODULATION_OPTION_PLAY_ACK) {	
			try {			
				if(Modulation.genReplyFile(FilePathHelper.getWaveAckFile())) {
					AssetFileDescriptor afd;
					afd = Utils.getAppContext().getAssets().openFd("ackAudio.wav");
					MediaPlayer mplayer = new MediaPlayer();
					mplayer.setVolume(20, 20);
					mplayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
					mplayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
					//mplayer.setDataSource(FilePathHelper.getWaveAckFile());
					mplayer.prepare();
					mplayer.start();
				}
				else {
					Log.e("ModulationAck", "Modulation GenReplyFile return failed.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public abstract boolean extractData(String str,Object obj);
}
